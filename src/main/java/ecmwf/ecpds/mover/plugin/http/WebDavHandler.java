/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * In applying the License, ECMWF does not waive the privileges and immunities
 * granted to it by virtue of its status as an inter-governmental organization
 * nor does it submit to any jurisdiction.
 */

package ecmwf.ecpds.mover.plugin.http;

/**
 * ECMWF Product Data Store (OpenECPDS) Project.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.webdav.DavCompliance;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceIteratorImpl;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavServletRequest;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.DavSessionProvider;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.WebdavRequest;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.lock.ActiveLock;
import org.apache.jackrabbit.webdav.lock.LockDiscovery;
import org.apache.jackrabbit.webdav.lock.LockInfo;
import org.apache.jackrabbit.webdav.lock.LockManager;
import org.apache.jackrabbit.webdav.lock.Scope;
import org.apache.jackrabbit.webdav.lock.SimpleLockManager;
import org.apache.jackrabbit.webdav.lock.SupportedLock;
import org.apache.jackrabbit.webdav.lock.Type;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.property.ResourceType;
import org.apache.jackrabbit.webdav.property.PropEntry;
import org.apache.jackrabbit.webdav.server.AbstractWebdavServlet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import ecmwf.common.ecaccess.EccmdException;
import ecmwf.common.ecaccess.FileListElement;
import ecmwf.common.ecaccess.NativeAuthenticationProvider;
import ecmwf.common.ecaccess.UserSession;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.ProxySocket;

/**
 * Jetty handler exposing the ECPDS virtual file system as WebDAV.
 */
public final class WebDavHandler extends AbstractHandler {
    /** The logger. */
    private static final Logger _log = LogManager.getLogger(WebDavHandler.class);

    /** Shared lock manager. */
    private static final SimpleLockManager LOCK_MANAGER = new SimpleLockManager();

    private final String contextPath;
    private final WebDavServlet servlet;

    /**
     * Creates a new handler.
     *
     * @param contextPath
     *            mounted context path
     */
    public WebDavHandler(final String contextPath) {
        this.contextPath = normalizeContextPath(contextPath);
        servlet = new WebDavServlet(this.contextPath);
        try {
            servlet.init(new SimpleServletConfig());
        } catch (final ServletException e) {
            throw new IllegalStateException("Unable to initialize WebDAV servlet", e);
        }
    }

    @Override
    public void handle(final String target, final Request baseRequest, final HttpServletRequest request,
            final HttpServletResponse response) throws IOException, ServletException {
        if (!target.startsWith(contextPath)) {
            return;
        }
        _log.debug("WebDAV {} {}", request.getMethod(), target);
        // Jackrabbit builds hrefPrefix as getContextPath() + getServletPath().
        // Outside a proper servlet container both return null, so we wrap the request
        // to supply the fixed values Jackrabbit needs to strip the prefix from URIs.
        final var wrapped = new HttpServletRequestWrapper(request) {
            @Override
            public String getContextPath() {
                return contextPath;
            }

            @Override
            public String getServletPath() {
                return "";
            }
        };
        servlet.service(wrapped, response);
        _log.debug("WebDAV {} {} -> {}", request.getMethod(), target, response.getStatus());
        baseRequest.setHandled(true);
    }

    private static String normalizeContextPath(final String value) {
        if (value == null || value.isBlank() || "/".equals(value.trim())) {
            return "/webdav";
        }
        var path = value.trim();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    /** Servlet config backed by Cnf. */
    private static final class SimpleServletConfig implements ServletConfig {
        @Override
        public String getServletName() {
            return "WebDavServlet";
        }

        @Override
        public javax.servlet.ServletContext getServletContext() {
            return null;
        }

        @Override
        public String getInitParameter(final String name) {
            return Cnf.at("HttpPlugin", name, null);
        }

        @Override
        public java.util.Enumeration<String> getInitParameterNames() {
            return Collections.enumeration(Collections.emptyList());
        }
    }

    /** WebDAV servlet. */
    private static final class WebDavServlet extends AbstractWebdavServlet {
        private static final long serialVersionUID = 1L;

        private final String contextPath;
        private final DavSessionProvider sessionProvider = new EcpdsDavSessionProvider();
        private final DavLocatorFactory locatorFactory;
        private final DavResourceFactory resourceFactory;

        WebDavServlet(final String contextPath) {
            this.contextPath = contextPath;
            locatorFactory = new EcpdsDavLocatorFactory(contextPath);
            resourceFactory = new EcpdsDavResourceFactory();
        }

        @Override
        protected boolean isPreconditionValid(final WebdavRequest request, final DavResource resource) {
            return request.matchesIfHeader(resource);
        }

        @Override
        public DavSessionProvider getDavSessionProvider() {
            return sessionProvider;
        }

        @Override
        public void setDavSessionProvider(final DavSessionProvider davSessionProvider) {
            throw new UnsupportedOperationException("Static session provider");
        }

        @Override
        public DavLocatorFactory getLocatorFactory() {
            return locatorFactory;
        }

        @Override
        public void setLocatorFactory(final DavLocatorFactory locatorFactory) {
            throw new UnsupportedOperationException("Static locator factory");
        }

        @Override
        public DavResourceFactory getResourceFactory() {
            return resourceFactory;
        }

        @Override
        public void setResourceFactory(final DavResourceFactory resourceFactory) {
            throw new UnsupportedOperationException("Static resource factory");
        }

        public LockManager getLockManager() {
            return LOCK_MANAGER;
        }

        @Override
        protected boolean isCreateAbsoluteURI() {
            return false;
        }

        @Override
        public String getInitParameter(final String name) {
            if (INIT_PARAM_AUTHENTICATE_HEADER.equals(name)) {
                return "Basic realm=\"OpenECPDS WebDAV\"";
            }
            if (INIT_PARAM_CREATE_ABSOLUTE_URI.equals(name)) {
                return "false";
            }
            return Cnf.at("HttpPlugin", name, super.getInitParameter(name));
        }
    }

    /** Session provider. */
    private static final class EcpdsDavSessionProvider implements DavSessionProvider {
        @Override
        public boolean attachSession(final WebdavRequest request) throws DavException {
            final var authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Basic ")) {
                throw new DavException(DavServletResponse.SC_UNAUTHORIZED, "Authentication required");
            }
            try {
                final var decoded = new String(Base64.getDecoder().decode(authHeader.substring(6)),
                        StandardCharsets.UTF_8);
                final var creds = decoded.split(":", 2);
                final var username = creds[0];
                final var password = creds.length > 1 ? creds[1] : "";
                final var userSession = NativeAuthenticationProvider.getInstance()
                        .getUserSession(request.getRemoteAddr(), username, password, "webdav", () -> {
                        });
                _log.debug("WebDAV session attached for user={} from={}", username, request.getRemoteAddr());
                request.setDavSession(new EcpdsDavSession(userSession, username));
                return true;
            } catch (final Exception e) {
                _log.debug("WebDAV authentication failed", e);
                throw new DavException(DavServletResponse.SC_UNAUTHORIZED, "Invalid credentials");
            }
        }

        @Override
        public void releaseSession(final WebdavRequest request) {
            final var davSession = request.getDavSession();
            if (davSession instanceof EcpdsDavSession ecpdsSession) {
                _log.debug("WebDAV session released for user={}", ecpdsSession.getUsername());
                ecpdsSession.getUserSession().close(true);
            }
            request.setDavSession(null);
        }
    }

    /** Session wrapper. */
    private static final class EcpdsDavSession implements DavSession {
        private final UserSession userSession;
        private final String username;
        private final Set<String> lockTokens = Collections.synchronizedSet(new HashSet<>());
        private final Set<Object> references = Collections.synchronizedSet(new HashSet<>());

        EcpdsDavSession(final UserSession userSession, final String username) {
            this.userSession = userSession;
            this.username = username;
        }

        UserSession getUserSession() {
            return userSession;
        }

        String getUsername() {
            return username;
        }

        @Override
        public void addReference(final Object reference) {
            references.add(reference);
        }

        @Override
        public void removeReference(final Object reference) {
            references.remove(reference);
        }

        @Override
        public void addLockToken(final String token) {
            lockTokens.add(token);
        }

        @Override
        public String[] getLockTokens() {
            return lockTokens.toArray(new String[0]);
        }

        @Override
        public void removeLockToken(final String token) {
            lockTokens.remove(token);
        }
    }

    /** Locator factory. */
    private static final class EcpdsDavLocatorFactory implements DavLocatorFactory {
        private final String contextPath;

        EcpdsDavLocatorFactory(final String contextPath) {
            this.contextPath = contextPath;
        }

        @Override
        public DavResourceLocator createResourceLocator(final String prefix, final String href) {
            var path = href;
            try {
                final var uri = URI.create(href);
                if (uri.getPath() != null) {
                    path = uri.getPath();
                }
            } catch (final Exception _) {
                // Ignore invalid URI and treat href as path.
            }
            var effectivePrefix = prefix == null || prefix.isBlank() ? contextPath : prefix;
            if (path.startsWith(effectivePrefix)) {
                path = path.substring(effectivePrefix.length());
            }
            return new EcpdsDavResourceLocator(contextPath, normalizeResourcePath(path));
        }

        @Override
        public DavResourceLocator createResourceLocator(final String prefix, final String workspacePath,
                final String resourcePath) {
            return new EcpdsDavResourceLocator(prefix == null || prefix.isBlank() ? contextPath : prefix,
                    normalizeResourcePath(resourcePath));
        }

        @Override
        public DavResourceLocator createResourceLocator(final String prefix, final String workspacePath,
                final String path, final boolean isResourcePath) {
            return createResourceLocator(prefix, workspacePath, path);
        }
    }

    /** Locator implementation. */
    private static final class EcpdsDavResourceLocator implements DavResourceLocator {
        private final String prefix;
        private final String resourcePath;

        EcpdsDavResourceLocator(final String prefix, final String resourcePath) {
            this.prefix = prefix;
            this.resourcePath = resourcePath;
        }

        @Override
        public String getPrefix() {
            return prefix;
        }

        @Override
        public String getResourcePath() {
            return resourcePath;
        }

        @Override
        public String getWorkspacePath() {
            return "";
        }

        @Override
        public String getWorkspaceName() {
            return null;
        }

        @Override
        public boolean isSameWorkspace(final DavResourceLocator locator) {
            return true;
        }

        @Override
        public boolean isSameWorkspace(final String workspaceName) {
            return true;
        }

        @Override
        public String getHref(final boolean isCollection) {
            final var path = prefix + ("/".equals(resourcePath) ? "" : resourcePath);
            if (isCollection && !path.endsWith("/")) {
                return path + "/";
            }
            return path.isEmpty() ? "/" : path;
        }

        @Override
        public boolean isRootLocation() {
            return "/".equals(resourcePath);
        }

        @Override
        public DavLocatorFactory getFactory() {
            return new EcpdsDavLocatorFactory(prefix);
        }

        @Override
        public String getRepositoryPath() {
            return resourcePath;
        }

        @Override
        public int hashCode() {
            return Objects.hash(prefix, resourcePath);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof EcpdsDavResourceLocator other)) {
                return false;
            }
            return Objects.equals(prefix, other.prefix) && Objects.equals(resourcePath, other.resourcePath);
        }
    }

    /** Resource factory. */
    private static final class EcpdsDavResourceFactory implements DavResourceFactory {
        @Override
        public DavResource createResource(final DavResourceLocator locator, final DavServletRequest request,
                final DavServletResponse response) throws DavException {
            final var session = request.getDavSession();
            if (!(session instanceof EcpdsDavSession ecpdsSession)) {
                throw new DavException(DavServletResponse.SC_UNAUTHORIZED, "Missing WebDAV session");
            }
            return new EcpdsDavResource(locator, this, ecpdsSession, LOCK_MANAGER);
        }

        @Override
        public DavResource createResource(final DavResourceLocator locator, final DavSession session)
                throws DavException {
            if (!(session instanceof EcpdsDavSession ecpdsSession)) {
                throw new DavException(DavServletResponse.SC_UNAUTHORIZED, "Missing WebDAV session");
            }
            return new EcpdsDavResource(locator, this, ecpdsSession, LOCK_MANAGER);
        }
    }

    /** Resource implementation. */
    private static final class EcpdsDavResource implements DavResource {
        private final DavResourceLocator locator;
        private final EcpdsDavResourceFactory factory;
        private final EcpdsDavSession session;
        private LockManager lockManager;

        EcpdsDavResource(final DavResourceLocator locator, final EcpdsDavResourceFactory factory,
                final EcpdsDavSession session, final LockManager lockManager) {
            this.locator = locator;
            this.factory = factory;
            this.session = session;
            this.lockManager = lockManager;
        }

        /**
         * Converts a WebDAV resource path (e.g. "/destname/file") to the internal FtpURL format expected by
         * UserDataSpace: "[user]DATA:/destname/file".
         */
        private String toFtpPath(final String resourcePath) {
            return "[" + session.getUserSession().getUser() + "]DATA:" + resourcePath;
        }

        @Override
        public String getComplianceClass() {
            return DavCompliance.concatComplianceClasses(new String[] { DavCompliance._1_, DavCompliance._2_ });
        }

        @Override
        public String getSupportedMethods() {
            return METHODS;
        }

        @Override
        public boolean exists() {
            return readMetadata(false).exists;
        }

        @Override
        public boolean isCollection() {
            return readMetadata(false).collection;
        }

        @Override
        public String getDisplayName() {
            if (locator.isRootLocation()) {
                return "/";
            }
            final var path = locator.getResourcePath();
            final var slash = path.lastIndexOf('/');
            return slash >= 0 ? path.substring(slash + 1) : path;
        }

        @Override
        public DavResourceLocator getLocator() {
            return locator;
        }

        @Override
        public String getResourcePath() {
            return locator.getResourcePath();
        }

        @Override
        public String getHref() {
            return locator.getHref(isCollection());
        }

        @Override
        public long getModificationTime() {
            return readMetadata(false).modificationTime;
        }

        @Override
        public void spool(final OutputContext outputContext) throws IOException {
            _log.debug("WebDAV GET {}", getResourcePath());
            final var metadata = readMetadata(true);
            outputContext.setModificationTime(metadata.modificationTime);
            if (metadata.collection) {
                outputContext.setContentType("text/html; charset=UTF-8");
                return;
            }
            outputContext.setContentType(getContentType());
            outputContext.setContentLength(metadata.contentLength);
            if (!outputContext.hasStream()) {
                return;
            }
            ProxySocket proxy = null;
            try {
                proxy = session.getUserSession().getProxySocketInput(toFtpPath(getResourcePath()), 0);
                try (InputStream in = proxy.getDataInputStream(); OutputStream out = outputContext.getOutputStream()) {
                    in.transferTo(out);
                }
                session.getUserSession().check(proxy);
            } catch (final EccmdException e) {
                throw new IOException("Unable to read " + getResourcePath(), e);
            } finally {
                if (proxy != null) {
                    proxy.close();
                }
            }
        }

        @Override
        public DavPropertyName[] getPropertyNames() {
            return getProperties().getPropertyNames();
        }

        @Override
        public DavProperty<?> getProperty(final DavPropertyName name) {
            return getProperties().get(name);
        }

        @Override
        public DavPropertySet getProperties() {
            final var metadata = readMetadata(false);
            final var properties = new DavPropertySet();
            properties.add(new DefaultDavProperty<>(DavPropertyName.DISPLAYNAME, getDisplayName()));
            properties.add(
                    new ResourceType(metadata.collection ? ResourceType.COLLECTION : ResourceType.DEFAULT_RESOURCE));
            properties.add(new DefaultDavProperty<>(DavPropertyName.CREATIONDATE,
                    formatCreationDate(metadata.modificationTime)));
            properties.add(new DefaultDavProperty<>(DavPropertyName.GETLASTMODIFIED,
                    formatLastModified(metadata.modificationTime)));
            if (!metadata.collection) {
                properties.add(new DefaultDavProperty<>(DavPropertyName.GETCONTENTLENGTH, metadata.contentLength));
                properties.add(new DefaultDavProperty<>(DavPropertyName.GETCONTENTTYPE, getContentType()));
            }
            final var supportedLock = new SupportedLock();
            supportedLock.addEntry(Type.WRITE, Scope.EXCLUSIVE);
            properties.add(supportedLock);
            properties.add(new LockDiscovery(getLocks()));
            return properties;
        }

        @Override
        public void setProperty(final DavProperty<?> property) throws DavException {
            throw new DavException(DavServletResponse.SC_FORBIDDEN, "Property updates are not supported");
        }

        @Override
        public void removeProperty(final DavPropertyName propertyName) throws DavException {
            throw new DavException(DavServletResponse.SC_FORBIDDEN, "Property updates are not supported");
        }

        @Override
        public MultiStatusResponse alterProperties(final List<? extends PropEntry> changeList) {
            final var response = new MultiStatusResponse(getHref(), null);
            for (final PropEntry entry : changeList) {
                if (entry instanceof DavProperty<?> property) {
                    response.add(property, DavServletResponse.SC_FORBIDDEN);
                } else if (entry instanceof DavPropertyName name) {
                    response.add(name, DavServletResponse.SC_FORBIDDEN);
                }
            }
            return response;
        }

        @Override
        public DavResource getCollection() {
            if (locator.isRootLocation()) {
                return null;
            }
            final var path = parentPath(getResourcePath());
            try {
                return factory.createResource(locator.getFactory().createResourceLocator(locator.getPrefix(), "", path),
                        session);
            } catch (final DavException e) {
                throw new IllegalArgumentException("Unable to resolve parent resource", e);
            }
        }

        @Override
        public void addMember(final DavResource resource, final InputContext inputContext) throws DavException {
            final var targetPath = resource.getResourcePath();
            _log.debug("WebDAV addMember {} (hasStream={})", targetPath, inputContext.hasStream());
            if (!exists() || !isCollection()) {
                throw new DavException(DavServletResponse.SC_CONFLICT);
            }
            // Creating a resource directly under root means creating a destination, which
            // is system-managed and cannot be done via WebDAV.
            if ("/".equals(parentPath(targetPath))) {
                throw new DavException(DavServletResponse.SC_FORBIDDEN,
                        "Cannot create resources at the root level: destinations are system-managed");
            }
            try {
                if (!inputContext.hasStream()) {
                    session.getUserSession().mkdir(toFtpPath(targetPath));
                    return;
                }
                final var parent = parentPath(targetPath);
                if (!"/".equals(parent)) {
                    ensureCollection(parent);
                }
                ProxySocket proxy = null;
                try {
                    proxy = session.getUserSession().getProxySocketOutput(toFtpPath(targetPath), 0, 0640);
                    try (InputStream in = inputContext.getInputStream();
                            OutputStream out = proxy.getDataOutputStream()) {
                        in.transferTo(out);
                    }
                    session.getUserSession().check(proxy);
                } finally {
                    if (proxy != null) {
                        proxy.close();
                    }
                }
            } catch (final EccmdException | IOException e) {
                throw asDavException("Unable to store " + targetPath, e);
            }
        }

        @Override
        public DavResourceIterator getMembers() {
            _log.debug("WebDAV PROPFIND getMembers {}", getResourcePath());
            final var metadata = readMetadata(false);
            if (!metadata.exists || !metadata.collection) {
                return DavResourceIteratorImpl.EMPTY;
            }
            try {
                final var children = session.getUserSession().getFileList(toFtpPath(getResourcePath()), "");
                final List<DavResource> resources = new ArrayList<>();
                for (final FileListElement child : children) {
                    final var childPath = childPath(getResourcePath(), child.getName());
                    resources.add(factory.createResource(
                            locator.getFactory().createResourceLocator(locator.getPrefix(), "", childPath), session));
                }
                return new DavResourceIteratorImpl(resources);
            } catch (final Exception e) {
                _log.debug("Unable to list WebDAV members for {}", getResourcePath(), e);
                return DavResourceIteratorImpl.EMPTY;
            }
        }

        @Override
        public void removeMember(final DavResource member) throws DavException {
            final var path = member.getResourcePath();
            _log.debug("WebDAV DELETE {}", path);
            try {
                final var info = ((EcpdsDavResource) member).readMetadata(true);
                if (info.collection) {
                    session.getUserSession().rmdir(toFtpPath(path));
                } else {
                    session.getUserSession().deleteFile(toFtpPath(path), true);
                }
            } catch (final EccmdException | IOException e) {
                throw asDavException("Unable to delete " + path, e);
            }
        }

        @Override
        public void move(final DavResource destination) throws DavException {
            final var target = destination.getResourcePath();
            _log.debug("WebDAV MOVE {} -> {}", getResourcePath(), target);
            try {
                ensureCollection(parentPath(target));
                session.getUserSession().moveFile(toFtpPath(getResourcePath()), toFtpPath(target));
            } catch (final EccmdException | IOException e) {
                throw asDavException("Unable to move to " + target, e);
            }
        }

        @Override
        public void copy(final DavResource destination, final boolean shallow) throws DavException {
            _log.debug("WebDAV COPY {} -> {} (shallow={})", getResourcePath(), destination.getResourcePath(), shallow);
            copyRecursive(getResourcePath(), destination.getResourcePath(), shallow);
        }

        @Override
        public boolean isLockable(final Type type, final Scope scope) {
            return Type.WRITE.equals(type) && Scope.EXCLUSIVE.equals(scope);
        }

        @Override
        public boolean hasLock(final Type type, final Scope scope) {
            return getLock(type, scope) != null;
        }

        @Override
        public ActiveLock getLock(final Type type, final Scope scope) {
            return lockManager != null ? lockManager.getLock(type, scope, this) : null;
        }

        @Override
        public ActiveLock[] getLocks() {
            final var lock = getLock(Type.WRITE, Scope.EXCLUSIVE);
            return lock == null ? new ActiveLock[0] : new ActiveLock[] { lock };
        }

        @Override
        public ActiveLock lock(final LockInfo reqLockInfo) throws DavException {
            _log.debug("WebDAV LOCK {}", getResourcePath());
            final var lock = lockManager.createLock(reqLockInfo, this);
            session.addLockToken(lock.getToken());
            return lock;
        }

        @Override
        public ActiveLock refreshLock(final LockInfo reqLockInfo, final String lockToken) throws DavException {
            final var lock = lockManager.refreshLock(reqLockInfo, lockToken, this);
            session.addLockToken(lock.getToken());
            return lock;
        }

        @Override
        public void unlock(final String lockToken) throws DavException {
            _log.debug("WebDAV UNLOCK {} token={}", getResourcePath(), lockToken);
            lockManager.releaseLock(lockToken, this);
            session.removeLockToken(lockToken);
        }

        @Override
        public void addLockManager(final LockManager lockmgr) {
            lockManager = lockmgr;
        }

        @Override
        public DavResourceFactory getFactory() {
            return factory;
        }

        @Override
        public DavSession getSession() {
            return session;
        }

        private void copyRecursive(final String source, final String target, final boolean shallow)
                throws DavException {
            final var metadata = readMetadata(true);
            try {
                if (metadata.collection) {
                    ensureCollection(target);
                    if (!shallow) {
                        final var children = session.getUserSession().getFileList(toFtpPath(source), "");
                        for (final FileListElement child : children) {
                            final var childSource = childPath(source, child.getName());
                            final var childTarget = childPath(target, child.getName());
                            factory.createResource(
                                    locator.getFactory().createResourceLocator(locator.getPrefix(), "", childSource),
                                    session)
                                    .copy(factory.createResource(locator.getFactory()
                                            .createResourceLocator(locator.getPrefix(), "", childTarget), session),
                                            false);
                        }
                    }
                    return;
                }
                ensureCollection(parentPath(target));
                session.getUserSession().copyFile(toFtpPath(source), toFtpPath(target), true);
            } catch (final EccmdException | IOException e) {
                throw asDavException("Unable to copy to " + target, e);
            }
        }

        private void ensureCollection(final String path) throws IOException, EccmdException {
            if (path == null || "/".equals(path)) {
                return;
            }
            final var parent = parentPath(path);
            if (!"/".equals(parent)) {
                ensureCollection(parent);
            }
            final var metadata = readMetadata(path, false);
            if (!metadata.exists) {
                _log.debug("WebDAV ensureCollection mkdir {}", path);
                session.getUserSession().mkdir(toFtpPath(path));
            }
        }

        private ResourceMetadata readMetadata(final boolean strict) {
            return readMetadata(getResourcePath(), strict);
        }

        private ResourceMetadata readMetadata(final String path, final boolean strict) {
            if ("/".equals(path)) {
                return new ResourceMetadata(true, true, 0L, System.currentTimeMillis());
            }
            try {
                final var element = session.getUserSession().getFileListElement(toFtpPath(path));
                final var size = parseSize(element.getSize());
                final var metadata = new ResourceMetadata(true, element.isDirectory(),
                        element.isDirectory() ? 0L : size,
                        element.getTime() > 0 ? element.getTime() : System.currentTimeMillis());
                _log.debug("WebDAV readMetadata {} -> exists=true collection={} size={}", path, metadata.collection,
                        metadata.contentLength);
                return metadata;
            } catch (final Exception e) {
                _log.debug("WebDAV readMetadata {} -> not found ({})", path, e.getMessage());
                if (strict) {
                    throw new IllegalStateException("Resource not available: " + path, e);
                }
                return new ResourceMetadata(false, false, -1L, System.currentTimeMillis());
            }
        }

        private static long parseSize(final String value) {
            if (value == null || value.isBlank()) {
                return 0L;
            }
            try {
                return Long.parseLong(value.trim());
            } catch (final NumberFormatException e) {
                return 0L;
            }
        }

        private String getContentType() {
            final var type = URLConnection.guessContentTypeFromName(getDisplayName());
            return type != null ? type : "application/octet-stream";
        }

        private static String formatCreationDate(final long time) {
            return DateTimeFormatter.ISO_INSTANT
                    .format(Instant.ofEpochMilli(time > 0 ? time : System.currentTimeMillis()));
        }

        private static String formatLastModified(final long time) {
            synchronized (DavConstants.modificationDateFormat) {
                return DavConstants.modificationDateFormat
                        .format(new java.util.Date(time > 0 ? time : System.currentTimeMillis()));
            }
        }

        private static DavException asDavException(final String message, final Exception exception) {
            _log.debug(message, exception);
            final int status;
            if (exception instanceof EccmdException) {
                final var msg = exception.getMessage() != null ? exception.getMessage().toLowerCase() : "";
                if (msg.contains("not found") || msg.contains("no such") || msg.contains("not exist")) {
                    status = DavServletResponse.SC_NOT_FOUND;
                } else if (msg.contains("not allowed") || msg.contains("permission") || msg.contains("forbidden")
                        || msg.contains("access denied")) {
                    status = DavServletResponse.SC_FORBIDDEN;
                } else {
                    status = DavServletResponse.SC_CONFLICT;
                }
            } else {
                status = DavServletResponse.SC_INTERNAL_SERVER_ERROR;
            }
            return new DavException(status, message + ": " + exception.getMessage());
        }
    }

    /** Resource metadata. */
    private record ResourceMetadata(boolean exists, boolean collection, long contentLength, long modificationTime) {
    }

    private static String normalizeResourcePath(final String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }
        var value = path.replace('\\', '/');
        if (!value.startsWith("/")) {
            value = "/" + value;
        }
        while (value.contains("//")) {
            value = value.replace("//", "/");
        }
        if (value.length() > 1 && value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String parentPath(final String path) {
        final var normalized = normalizeResourcePath(path);
        final var slash = normalized.lastIndexOf('/');
        if (slash <= 0) {
            return "/";
        }
        return normalized.substring(0, slash);
    }

    private static String childPath(final String parent, final String child) {
        final var base = "/".equals(parent) ? "" : normalizeResourcePath(parent);
        return normalizeResourcePath(base + "/" + child);
    }
}
