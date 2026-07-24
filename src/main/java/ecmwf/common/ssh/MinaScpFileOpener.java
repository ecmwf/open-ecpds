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

package ecmwf.common.ssh;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.9
 * @since 2025-07-23
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.sshd.common.session.Session;
import org.apache.sshd.scp.common.ScpFileOpener;
import org.apache.sshd.scp.common.ScpSourceStreamResolver;
import org.apache.sshd.scp.common.ScpTargetStreamResolver;
import org.apache.sshd.scp.common.helpers.ScpTimestampCommandDetails;

import ecmwf.common.ecaccess.EccmdException;
import ecmwf.common.ecaccess.FileListElement;
import ecmwf.common.ecaccess.UserSession;

/**
 * SCP file-opener that routes all I/O through the virtual ECPDS user session. This enables SCP downloads and uploads
 * via the same backend as SFTP, using {@link UserSession#getProxySocketInput} and
 * {@link UserSession#getProxySocketOutput} for streaming, and {@link UserSession#getFileListElement} for metadata.
 */
public final class MinaScpFileOpener implements ScpFileOpener {

    /** Default permissions: rw-r--r-- (regular file). */
    private static final Set<PosixFilePermission> DEFAULT_FILE_PERMS = Collections
            .unmodifiableSet(EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.GROUP_READ, PosixFilePermission.OTHERS_READ));

    /** Default permissions: rwxr-xr-x (directory). */
    private static final Set<PosixFilePermission> DEFAULT_DIR_PERMS = Collections.unmodifiableSet(EnumSet.of(
            PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE,
            PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_EXECUTE, PosixFilePermission.OTHERS_READ,
            PosixFilePermission.OTHERS_EXECUTE));

    /**
     * Resolves the SCP command path to a NIO {@link Path}. The returned path is always absolute and is used only as a
     * virtual-path carrier — it is never opened on the real local filesystem.
     */
    @Override
    public Path resolveLocalPath(final Session session, final FileSystem fileSystem, final String path)
            throws IOException {
        // Use the default filesystem as a path carrier; the string value is our virtual path
        final var fs = fileSystem != null ? fileSystem : FileSystems.getDefault();
        return path.startsWith("/") ? fs.getPath(path) : fs.getPath("/" + path);
    }

    @Override
    public boolean sendAsDirectory(final Session session, final Path path, final LinkOption... options)
            throws IOException {
        try {
            return userSession(session).getFileListElement(virtualPath(path)).isDirectory();
        } catch (final EccmdException e) {
            return false;
        }
    }

    @Override
    public boolean sendAsRegularFile(final Session session, final Path path, final LinkOption... options)
            throws IOException {
        try {
            return !userSession(session).getFileListElement(virtualPath(path)).isDirectory();
        } catch (final EccmdException e) {
            return false;
        }
    }

    @Override
    public BasicFileAttributes getLocalBasicFileAttributes(final Session session, final Path path,
            final LinkOption... options) throws IOException {
        try {
            return new VirtualFileAttributes(userSession(session).getFileListElement(virtualPath(path)));
        } catch (final EccmdException e) {
            throw new NoSuchFileException(virtualPath(path), null, e.getMessage());
        }
    }

    @Override
    public Set<PosixFilePermission> getLocalFilePermissions(final Session session, final Path path,
            final LinkOption... options) throws IOException {
        try {
            return userSession(session).getFileListElement(virtualPath(path)).isDirectory() ? DEFAULT_DIR_PERMS
                    : DEFAULT_FILE_PERMS;
        } catch (final EccmdException e) {
            return DEFAULT_FILE_PERMS;
        }
    }

    /**
     * Returns the virtual children of a directory. Uses {@link UserSession#getFileList} to enumerate entries and maps
     * each name to a child path under {@code path}.
     */
    @Override
    public DirectoryStream<Path> getLocalFolderChildren(final Session session, final Path path) throws IOException {
        try {
            final FileListElement[] elements = userSession(session).getFileList(virtualPath(path));
            final List<Path> children = new ArrayList<>();
            if (elements != null) {
                for (final var e : elements) {
                    children.add(path.resolve(e.getName()));
                }
            }
            return new ListDirectoryStream(children);
        } catch (final EccmdException e) {
            throw new IOException("Cannot list directory: " + virtualPath(path), e);
        }
    }

    /**
     * Returns virtual paths whose names match {@code pattern} inside the directory at {@code path}. If {@code path}
     * points to a regular file, returns it directly.
     */
    @Override
    public Iterable<Path> getMatchingFilesToSend(final Session session, final Path path, final String pattern)
            throws IOException {
        if (pattern == null || pattern.isEmpty()) {
            return Collections.emptyList();
        }
        // Resolve through the virtual filesystem first
        try {
            final var element = userSession(session).getFileListElement(virtualPath(path));
            if (!element.isDirectory()) {
                return Collections.singletonList(path);
            }
        } catch (final EccmdException e) {
            // Path not found — return empty
            return Collections.emptyList();
        }
        // Directory: list children and apply glob pattern
        final PathMatcher matcher = path.getFileSystem().getPathMatcher("glob:" + pattern);
        final List<Path> result = new ArrayList<>();
        try (final var children = getLocalFolderChildren(session, path)) {
            for (final var child : children) {
                if (matcher.matches(child.getFileName())) {
                    result.add(child);
                }
            }
        }
        return result;
    }

    /**
     * Resolves the incoming target path for an SCP upload. If the target path is an existing virtual directory the
     * uploaded file is placed inside it (as {@code path/name}); otherwise the file is created at {@code path} directly.
     */
    @Override
    public Path resolveIncomingFilePath(final Session session, final Path path, final String name,
            final boolean recursive, final Set<PosixFilePermission> perms, final ScpTimestampCommandDetails time)
            throws IOException {
        try {
            if (userSession(session).getFileListElement(virtualPath(path)).isDirectory()) {
                return path.resolve(name);
            }
        } catch (final EccmdException ignored) {
            // Target path does not exist — use it as the destination filename
        }
        return path;
    }

    /**
     * Resolves the root receive location for an SCP upload. Returns {@code path} unchanged; directory creation, if
     * needed, is handled by the backend.
     */
    @Override
    public Path resolveIncomingReceiveLocation(final Session session, final Path path, final boolean recursive,
            final boolean shouldBeDir, final boolean preserve) throws IOException {
        return path;
    }

    // ---- Stream openers ------------------------------------------------------------------

    @Override
    public InputStream openRead(final Session session, final Path file, final long size,
            final Set<PosixFilePermission> permissions, final OpenOption... options) throws IOException {
        try {
            return userSession(session).getProxySocketInput(virtualPath(file), 0).getDataInputStream();
        } catch (final EccmdException e) {
            throw new IOException("Cannot open " + virtualPath(file) + " for reading: " + e.getMessage(), e);
        }
    }

    @Override
    public OutputStream openWrite(final Session session, final Path file, final long size,
            final Set<PosixFilePermission> permissions, final OpenOption... options) throws IOException {
        try {
            return userSession(session).getProxySocketOutput(virtualPath(file), 0, 0640).getDataOutputStream();
        } catch (final EccmdException e) {
            throw new IOException("Cannot open " + virtualPath(file) + " for writing: " + e.getMessage(), e);
        }
    }

    // ---- Resolver factories --------------------------------------------------------------

    @Override
    public ScpSourceStreamResolver createScpSourceStreamResolver(final Session session, final Path path)
            throws IOException {
        final FileListElement element;
        try {
            element = userSession(session).getFileListElement(virtualPath(path));
        } catch (final EccmdException e) {
            throw new NoSuchFileException(virtualPath(path), null, e.getMessage());
        }
        return new VirtualScpSourceStreamResolver(path, element, this, session);
    }

    @Override
    public ScpTargetStreamResolver createScpTargetStreamResolver(final Session session, final Path path)
            throws IOException {
        return new VirtualScpTargetStreamResolver(path, this, session);
    }

    // ---- Helpers -------------------------------------------------------------------------

    /**
     * Extracts the {@link UserSession} from the MINA SSH session attributes.
     *
     * @throws IOException
     *             if the session has not been authenticated
     */
    static UserSession userSession(final Session session) throws IOException {
        final var info = session.getAttribute(AuthenticationInfo.AUTHENTICATION_INFO);
        if (info == null) {
            throw new IOException("SCP session not authenticated");
        }
        return info.session();
    }

    /**
     * Returns the virtual path string (always absolute) encoded in a NIO {@link Path}.
     */
    static String virtualPath(final Path path) {
        final var s = path.toString();
        return s.startsWith("/") ? s : "/" + s;
    }

    // ---- Inner types ---------------------------------------------------------------------

    /**
     * Exposes {@link FileListElement} metadata as {@link BasicFileAttributes}.
     */
    private static final class VirtualFileAttributes implements BasicFileAttributes {

        private final FileListElement element;

        VirtualFileAttributes(final FileListElement element) {
            this.element = element;
        }

        @Override
        public FileTime lastModifiedTime() {
            return FileTime.fromMillis(element.getTime());
        }

        @Override
        public FileTime lastAccessTime() {
            return lastModifiedTime();
        }

        @Override
        public FileTime creationTime() {
            return lastModifiedTime();
        }

        @Override
        public boolean isRegularFile() {
            return !element.isDirectory();
        }

        @Override
        public boolean isDirectory() {
            return element.isDirectory();
        }

        @Override
        public boolean isSymbolicLink() {
            return false;
        }

        @Override
        public boolean isOther() {
            return false;
        }

        @Override
        public long size() {
            try {
                return Long.parseLong(element.getSize().trim());
            } catch (final NumberFormatException e) {
                return 0L;
            }
        }

        @Override
        public Object fileKey() {
            return null;
        }
    }

    /**
     * SCP source resolver for a virtual file (download).
     */
    private record VirtualScpSourceStreamResolver(Path path, FileListElement element, MinaScpFileOpener opener,
            Session session) implements ScpSourceStreamResolver {

        @Override
        public String getFileName() {
            return path.getFileName().toString();
        }

        @Override
        public Path getEventListenerFilePath() {
            return path;
        }

        @Override
        public Collection<PosixFilePermission> getPermissions() {
            return DEFAULT_FILE_PERMS;
        }

        @Override
        public ScpTimestampCommandDetails getTimestamp() {
            // ScpTimestampCommandDetails expects seconds since epoch
            final long secs = TimeUnit.MILLISECONDS.toSeconds(element.getTime());
            return new ScpTimestampCommandDetails(secs, secs);
        }

        @Override
        public long getSize() {
            try {
                return Long.parseLong(element.getSize().trim());
            } catch (final NumberFormatException e) {
                return 0L;
            }
        }

        @Override
        public InputStream resolveSourceStream(final Session session, final long size,
                final Set<PosixFilePermission> perms, final OpenOption... options) throws IOException {
            return opener.openRead(session, path, size, perms, options);
        }
    }

    /**
     * SCP target resolver for a virtual file (upload).
     */
    private record VirtualScpTargetStreamResolver(Path path, MinaScpFileOpener opener, Session session)
            implements ScpTargetStreamResolver {

        @Override
        public Path getEventListenerFilePath() {
            return path;
        }

        @Override
        public OutputStream resolveTargetStream(final Session session, final String name, final long length,
                final Set<PosixFilePermission> perms, final OpenOption... options) throws IOException {
            // If target is an existing virtual directory, place the file inside it
            Path target = path;
            try {
                if (userSession(session).getFileListElement(virtualPath(path)).isDirectory()) {
                    target = path.resolve(name);
                }
            } catch (final EccmdException ignored) {
                // Target does not exist — use it as the destination filename directly
            }
            return opener.openWrite(session, target, length, perms, options);
        }

        @Override
        public void postProcessReceivedData(final String fileName, final boolean preserve,
                final Set<PosixFilePermission> perms, final ScpTimestampCommandDetails timestamp) {
            // No post-processing needed: the backend handles data persistence
        }
    }

    /**
     * Simple {@link DirectoryStream} backed by a pre-populated {@link List}.
     */
    private static final class ListDirectoryStream implements DirectoryStream<Path> {

        private final List<Path> paths;

        ListDirectoryStream(final List<Path> paths) {
            this.paths = paths;
        }

        @Override
        public Iterator<Path> iterator() {
            return paths.iterator();
        }

        @Override
        public void close() {
            // Nothing to close
        }
    }
}
