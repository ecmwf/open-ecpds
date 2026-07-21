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

import static ecmwf.common.ectrans.ECtransOptions.USER_PORTAL_TRIGGER_EVENT;

import java.io.Closeable;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.sshd.common.AttributeRepository.AttributeKey;
import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.sftp.server.DirectoryHandle;
import org.apache.sshd.sftp.server.FileHandle;
import org.apache.sshd.sftp.server.SftpFileSystemAccessor;
import org.apache.sshd.sftp.server.SftpSubsystemEnvironment;
import org.apache.sshd.sftp.server.SftpSubsystemProxy;

import ecmwf.common.ecaccess.EccmdException;
import ecmwf.common.ecaccess.FileListElement;
import ecmwf.common.ecaccess.StarterServer;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.ProxyEvent;
import ecmwf.common.technical.ProxySocket;
import ecmwf.common.text.Format;
import ecmwf.ecpds.mover.MoverServer;

/**
 * @author <a href="mailto:dev@mina.apache.org">Apache MINA SSHD Project</a>
 */
public class MinaFileSystemAccessor implements SftpFileSystemAccessor {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(MinaFileSystemAccessor.class);

    /** The time. */
    private static long time = System.currentTimeMillis();

    /** The open files. */
    private final Map<String, RemoteFile> openFiles = new ConcurrentHashMap<>();

    /** Per-session file-attribute cache key (path → FileListElement). */
    private static final AttributeKey<Map<String, FileListElement>> FILE_ATTR_CACHE_KEY = new AttributeKey<>();

    /** The Constant mover. */
    private static final MoverServer mover = StarterServer.getInstance(MoverServer.class);

    /** The local host. */
    private static final String localHost = mover.getRoot();

    /**
     * The Class RemoteFile.
     */
    private static class RemoteFile {
        /** The subsystem. */
        SftpSubsystemProxy subsystem;

        /** The fileListElement. */
        private final FileListElement fileListElement;

        /**
         * Instantiates a new remote file.
         *
         * @param f
         *            the f
         */
        RemoteFile(final SftpSubsystemProxy subsystem, final FileListElement fileListElement) {
            this.subsystem = subsystem;
            this.fileListElement = fileListElement;
        }

        /**
         * Gets the subsystem.
         *
         * @return the subsystem
         */
        public SftpSubsystemProxy getSftpSubsystemProxy() {
            return subsystem;
        }

        /**
         * Gets the file.
         *
         * @return the file
         */
        public FileListElement getFile() {
            return fileListElement;
        }

        /**
         * Gets the file attributes.
         *
         * @return the file attributes
         */
        public BasicFileAttributes getFileAttributes() {
            return getFileAttributes(fileListElement);
        }

        /**
         * Gets the file attributes.
         *
         * @param f
         *            the f
         *
         * @return the file attributes
         */
        public static final BasicFileAttributes getFileAttributes(final FileListElement f) {
            return new BasicFileAttributes() {
                @Override
                public long size() {
                    if (f.getSize() != null) {
                        var size = Long.parseLong(f.getSize());
                        return size > 0 ? size : 0;
                    } else
                        return 0;
                }

                @Override
                public FileTime lastModifiedTime() {
                    return FileTime.fromMillis(f.getTime());
                }

                @Override
                public FileTime lastAccessTime() {
                    return FileTime.fromMillis(f.getTime());
                }

                @Override
                public boolean isSymbolicLink() {
                    return f.getRight().charAt(0) == 'l';
                }

                @Override
                public boolean isRegularFile() {
                    return f.getRight().charAt(0) == '-';
                }

                @Override
                public boolean isOther() {
                    return !(isSymbolicLink() || isRegularFile() || isDirectory());
                }

                @Override
                public boolean isDirectory() {
                    return f.getRight().charAt(0) == 'd';
                }

                @Override
                public Object fileKey() {
                    return null;
                }

                @Override
                public FileTime creationTime() {
                    return FileTime.fromMillis(f.getTime());
                }
            };
        }
    }

    /**
     * The Class OpenFile.
     */
    private abstract static class OpenFile extends RemoteFile implements Closeable {

        /**
         * Instantiates a new open file.
         *
         * @param f
         *            the f
         * @param flags
         *            the flags
         */
        OpenFile(final SftpSubsystemProxy subsystem, final FileListElement f) {
            super(subsystem, f);
        }
    }

    /**
     * The Class ReadFile.
     */
    private final class ReadFile extends OpenFile {
        /** The closed. */
        private final AtomicBoolean closed = new AtomicBoolean(false);

        /** The bytes count. */
        private long bytesCount = 0;

        /** The start. */
        private long start = 0;

        /** Current position in the backend stream. */
        private long pos = 0;

        /** The proxy. */
        private ProxySocket proxy = null;

        /** The in. */
        private InputStream in = null;

        /** Buffer used for forward-seek skipping. */
        private static final int SKIP_BUF_SIZE = 65536;

        /**
         * Instantiates a new read file.
         *
         * @param subsystem
         *            the subsystem
         * @param fileListElement
         *            the file list element
         */
        ReadFile(final SftpSubsystemProxy subsystem, final FileListElement fileListElement) {
            super(subsystem, fileListElement);
        }

        /**
         * _open proxy socket.
         *
         * @param offset
         *            the offset
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        private void openProxySocket(final long offset) throws IOException {
            _log.debug("Opening ProxySocket (offset={})", offset);
            try {
                final var path = getFile().getName();
                final var url = getURL(getSftpSubsystemProxy(), path);
                proxy = getFileSystem(getSftpSubsystemProxy(), url).getProxySocketInput(url.dir, offset);
                in = proxy.getDataInputStream();
            } catch (final EccmdException e) {
                throw new IOException(e.getMessage());
            } finally {
                // Record start time only once, regardless of any re-opens for seeking.
                if (start == 0L) {
                    start = System.currentTimeMillis();
                }
            }
        }

        /**
         * Read.
         *
         * @param from
         *            the from
         * @param buffer
         *            the buffer
         * @param bufStart
         *            the buf start
         * @param len
         *            the len
         *
         * @return the int
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public synchronized int read(final long from, final byte[] buffer, final int bufStart, final int len)
                throws IOException {
            if (in == null) {
                // First read: open the stream at the requested offset.
                openProxySocket(from);
                pos = from;
            } else if (from < pos) {
                // Backward seek: close the current stream and reopen at the new offset.
                _log.debug("Backward seek: {} -> {}, reopening stream", pos, from);
                closeQuietly(proxy);
                closeQuietly(in);
                proxy = null;
                in = null;
                openProxySocket(from);
                pos = from;
            } else if (from > pos) {
                // Forward seek: skip ahead in the current stream.
                final var skip = new byte[SKIP_BUF_SIZE];
                var remaining = from - pos;
                while (remaining > 0) {
                    final var n = in.read(skip, 0, (int) Math.min(remaining, SKIP_BUF_SIZE));
                    if (n < 0) {
                        break;
                    }
                    pos += n;
                    remaining -= n;
                }
            }
            final var n = readFully(in, buffer, bufStart, len);
            if (n > 0) {
                pos += n;
                bytesCount += n;
            }
            return n;
        }

        /**
         * Close.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void close() throws IOException {
            if (!closed.compareAndSet(false, true)) {
                // Was already closed!
                _log.debug("Already closed");
                return;
            }
            _log.debug("Closing ReadFile");
            closeQuietly(proxy);
            closeQuietly(in);
            try {
                if (proxy != null) {
                    final var info = getAuthenticationInfo(getSftpSubsystemProxy());
                    final var setup = info.session().getECtransSetup();
                    // Populating with the transfer rate informations!
                    if (setup == null || setup.getBoolean(USER_PORTAL_TRIGGER_EVENT)) {
                        final var event = new ProxyEvent(proxy);
                        event.setProtocol("sftp");
                        event.setLocalHost(localHost);
                        event.setRemoteHost(info.host());
                        event.setUserType(ProxyEvent.UserType.DATA_USER);
                        event.setUserName(info.user());
                        event.setStartTime(start);
                        event.setDuration(System.currentTimeMillis() - start);
                        event.setSent(bytesCount);
                    }
                    info.session().check(proxy);
                }
            } catch (final EccmdException e) {
                throw new IOException(e.getMessage());
            } finally {
                proxy = null;
                in = null;
            }
        }
    }

    /**
     * The Class WriteFile.
     */
    private final class WriteFile extends OpenFile {
        /** The closed. */
        private final AtomicBoolean closed = new AtomicBoolean(false);

        /** The index. */
        long index = 0;

        /** The bytes count. */
        long bytesCount = 0;

        /** The start. */
        long start = 0;

        /** The proxy. */
        ProxySocket proxy = null;

        /** The out. */
        OutputStream out = null;

        /**
         * Instantiates a new write file.
         *
         * @param f
         *            the f
         * @param flags
         *            the flags
         */
        WriteFile(final SftpSubsystemProxy subsystem, final FileListElement fileListElement) {
            super(subsystem, fileListElement);
        }

        /**
         * _open proxy socket.
         *
         * @param offset
         *            the offset
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        private void openProxySocket(final long offset) throws IOException {
            _log.debug("Opening ProxySocket (offset={})", offset);
            try {
                final var url = getURL(getSftpSubsystemProxy(), getFile().getName());
                proxy = getFileSystem(getSftpSubsystemProxy(), url).getProxySocketOutput(url.dir, offset, 640);
                out = proxy.getDataOutputStream();
            } catch (final EccmdException e) {
                throw new IOException(e.getMessage());
            } finally {
                start = System.currentTimeMillis();
            }
        }

        /**
         * Write.
         *
         * @param from
         *            the from
         * @param buffer
         *            the buffer
         * @param off
         *            the off
         * @param len
         *            the len
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public synchronized void write(final long from, final byte[] buffer, final int off, final int len)
                throws IOException {
            if (out == null) {
                openProxySocket(from);
                index = from;
            }
            if (from != index) {
                throw new IOException("out of range (" + from + "!=" + index + ")");
            }
            index += len;
            out.write(buffer, off, len);
            bytesCount += len;
        }

        /**
         * Close.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void close() throws IOException {
            if (!closed.compareAndSet(false, true)) {
                // Was already closed!
                _log.debug("Already closed");
                return;
            }
            _log.debug("Closing WriteFile");
            closeQuietly(proxy);
            closeQuietly(out);
            try {
                if (proxy != null) {
                    final var info = getAuthenticationInfo(getSftpSubsystemProxy());
                    final var setup = info.session().getECtransSetup();
                    // Populating with the transfer rate informations!
                    if (setup == null || setup.getBoolean(USER_PORTAL_TRIGGER_EVENT)) {
                        final var event = new ProxyEvent(proxy);
                        event.setProtocol("sftp");
                        event.setLocalHost(localHost);
                        event.setRemoteHost(info.host());
                        event.setUserType(ProxyEvent.UserType.DATA_USER);
                        event.setUserName(info.user());
                        event.setStartTime(start);
                        event.setDuration(System.currentTimeMillis() - start);
                        event.setSent(bytesCount);
                        event.setUpload(true);
                    }
                    info.session().check(proxy);
                }
            } catch (final EccmdException e) {
                throw new IOException(e.getMessage());
            } finally {
                proxy = null;
                out = null;
            }
        }
    }

    /**
     * The Class OpenDirectory.
     */
    private static final class OpenDirectory extends RemoteFile {
        /** The children. */
        final FileListElement[] children;

        /** The readpos. */
        int readpos = 0;

        /**
         * Instantiates a new open directory.
         *
         * @param f
         *            the f
         * @param children
         *            the children
         */
        public OpenDirectory(SftpSubsystemProxy subsystem, final FileListElement f, final FileListElement[] children) {
            super(subsystem, f);
            this.children = children;
        }

        /**
         * Gets the children.
         *
         * @return the children
         *
         * @throws EOFException
         *             the EOF exception
         */
        public FileListElement[] getChildren() throws EOFException {
            final var count = children.length - readpos < 100 ? children.length - readpos : 100;
            if (count <= 0) {
                throw new EOFException("There are no more files");
            }
            final var files = new FileListElement[count];
            for (var i = 0; i < count; i++) {
                files[i] = children[readpos + i];
            }
            readpos += count;
            return files;
        }
    }

    /**
     * The Interface FileSystem.
     */
    private interface FileSystem {
        /**
         * Gets the path.
         *
         * @param path
         *            the path
         *
         * @return the path
         *
         * @throws FileNotFoundException
         *             the file not found exception
         */
        String getPath(String path) throws FileNotFoundException;

        /**
         * Mkdir.
         *
         * @param dir
         *            the dir
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        void mkdir(String dir) throws EccmdException, IOException;

        /**
         * Gets the file list element.
         *
         * @param path
         *            the path
         *
         * @return the file list element
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        FileListElement getFileListElement(String path) throws EccmdException, IOException;

        /**
         * Gets the file list.
         *
         * @param path
         *            the path
         * @param options
         *            the options
         *
         * @return the file list
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        FileListElement[] getFileList(String path, String options) throws EccmdException, IOException;

        /**
         * Gets the file size.
         *
         * @param source
         *            the source
         *
         * @return the file size
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        long getFileSize(String source) throws EccmdException, IOException;

        /**
         * Delete file.
         *
         * @param source
         *            the source
         * @param force
         *            the force
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        void deleteFile(String source, boolean force) throws EccmdException, IOException;

        /**
         * Move file.
         *
         * @param source
         *            the source
         * @param target
         *            the target
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        void moveFile(String source, String target) throws EccmdException, IOException;

        /**
         * Rmdir.
         *
         * @param dir
         *            the dir
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        void rmdir(String dir) throws EccmdException, IOException;

        /**
         * Gets the proxy socket input.
         *
         * @param source
         *            the source
         * @param offset
         *            the offset
         *
         * @return the proxy socket input
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        ProxySocket getProxySocketInput(String source, long offset) throws EccmdException, IOException;

        /**
         * Gets the proxy socket output.
         *
         * @param target
         *            the target
         * @param offset
         *            the offset
         * @param umask
         *            the umask
         *
         * @return the proxy socket output
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        ProxySocket getProxySocketOutput(String target, long offset, int umask) throws EccmdException, IOException;
    }

    /**
     * The Class URL.
     */
    private static final class URL {
        /** The domain. */
        String domain = null;

        /** The dir. */
        String dir = null;
    }

    /**
     * Gets the url.
     *
     * @param path
     *            the path
     *
     * @return the url
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private final URL getURL(SftpSubsystemProxy subsystem, String path) throws IOException {
        // Paths are clean POSIX strings like "/", "/DATA", "/DATA/file.txt" — no prefix to strip.
        path = path.length() == 0 ? "." : path;
        final var url = new URL();
        var result = ".";
        final var token = new StringTokenizer(path.replace('\\', '/'), "/");
        while (token.hasMoreElements()) {
            if (!".".equals(path = token.nextToken())) {
                result += "/" + path;
            }
        }
        // First we check if the user profile required a specific domain?
        var defaultDomain = getAuthenticationInfo(subsystem).session().getDefaultDomain();
        // Then if no specific domain is requested in the profile we check the
        // default domain in the configuration file!
        defaultDomain = defaultDomain == null ? Cnf.at("FtpDomainList", "*") : defaultDomain;
        if (defaultDomain == null || "*".equals(defaultDomain)) {
            final var hiddenDomainList = Cnf.listAt("SshPlugin", "hidden");
            for (final String domainName : Cnf.at("FtpDomainList", new Hashtable<>()).keySet()) {
                if (!"*".equals(domainName) && !hiddenDomainList.contains(domainName)) {
                    final var domainValue = Cnf.at("FtpDomainList", domainName, null);
                    if (domainValue == null) {
                        continue;
                    }
                    if (result.toString().toLowerCase().startsWith("./" + domainName.toLowerCase())) {
                        url.domain = domainValue.toUpperCase();
                        url.dir = result.substring(2 + domainName.length());
                        while (url.dir.startsWith("/")) {
                            url.dir = url.dir.substring(1);
                        }
                    }
                }
            }
            if (url.domain == null && !".".equals(result)) {
                throw new IOException("Failure");
            }
        } else if (!"DATA".equalsIgnoreCase(defaultDomain)) {
            final var domainValue = Cnf.at("FtpDomainList", defaultDomain, null);
            if (domainValue == null) {
                throw new IOException("Failure");
            }
            url.domain = domainValue.toUpperCase();
        } else {
            url.domain = "DATA:";
        }
        if (url.dir == null) {
            url.dir = result.toString();
        }
        _log.debug("Get FileSystem: {} ({}) -> (dir: {}, domain: {})",
                getAuthenticationInfo(subsystem).session().getUser(), path, url.dir, url.domain);
        return url;
    }

    /**
     * Gets the file system.
     *
     * @param url
     *            the url
     *
     * @return the file system
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws EccmdException
     *             the eccmd exception
     */
    private final FileSystem getFileSystem(final SftpSubsystemProxy subsystem, final URL url) {
        return url.domain == null ? (FileSystem) new RootFileSystem(subsystem)
                : (FileSystem) new DomainFileSystem(url.domain, subsystem);
    }

    /**
     * The Class DomainFileSystem.
     */
    private final class DomainFileSystem implements FileSystem {
        /** The currentDomain. */
        final String currentDomain;

        /** The info **/
        AuthenticationInfo info;

        /**
         * Instantiates a new domain file system.
         *
         * @param domain
         *            the domain
         */
        DomainFileSystem(final String domain, final SftpSubsystemProxy subsystem) {
            this.currentDomain = domain;
            this.info = getAuthenticationInfo(subsystem);
        }

        /**
         * Gets the path.
         *
         * @param path
         *            the path
         *
         * @return the path
         *
         * @throws FileNotFoundException
         *             the file not found exception
         */
        @Override
        public String getPath(final String path) throws FileNotFoundException {
            return "[" + info.user() + "]" + currentDomain + Format.normalizePath(path);
        }

        /**
         * Mkdir.
         *
         * @param dir
         *            the dir
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void mkdir(final String dir) throws EccmdException, IOException {
            info.session().mkdir(getPath(dir));
        }

        /**
         * Gets the file list element.
         *
         * @param path
         *            the path
         *
         * @return the file list element
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public FileListElement getFileListElement(final String path) throws EccmdException, IOException {
            return info.session().getFileListElement(getPath(path));
        }

        /**
         * Gets the file list.
         *
         * @param path
         *            the path
         * @param options
         *            the options
         *
         * @return the file list
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public FileListElement[] getFileList(final String path, final String options)
                throws EccmdException, IOException {
            return info.session().getFileList(getPath(path), options);
        }

        /**
         * Gets the file size.
         *
         * @param source
         *            the source
         *
         * @return the file size
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public long getFileSize(final String source) throws EccmdException, IOException {
            return info.session().getFileSize(getPath(source));
        }

        /**
         * Delete file.
         *
         * @param source
         *            the source
         * @param force
         *            the force
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void deleteFile(final String source, final boolean force) throws EccmdException, IOException {
            info.session().deleteFile(getPath(source), force);
        }

        /**
         * Move file.
         *
         * @param source
         *            the source
         * @param target
         *            the target
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void moveFile(final String source, final String target) throws EccmdException, IOException {
            info.session().moveFile(getPath(source), target);
        }

        /**
         * Rmdir.
         *
         * @param dir
         *            the dir
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void rmdir(final String dir) throws EccmdException, IOException {
            info.session().rmdir(getPath(dir));
        }

        /**
         * Gets the proxy socket input.
         *
         * @param source
         *            the source
         * @param offset
         *            the offset
         *
         * @return the proxy socket input
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public ProxySocket getProxySocketInput(final String source, final long offset)
                throws EccmdException, IOException {
            return info.session().getProxySocketInput(getPath(source), offset);
        }

        /**
         * Gets the proxy socket output.
         *
         * @param target
         *            the target
         * @param offset
         *            the offset
         * @param umask
         *            the umask
         *
         * @return the proxy socket output
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public ProxySocket getProxySocketOutput(final String target, final long offset, final int umask)
                throws EccmdException, IOException {
            return info.session().getProxySocketOutput(getPath(target), offset, umask);
        }
    }

    /**
     * The Class RootFileSystem.
     */
    private final class RootFileSystem implements FileSystem {

        /** The subsystem **/
        AuthenticationInfo info;

        /**
         * Instantiates a new domain file system.
         *
         * @param domain
         *            the domain
         */
        RootFileSystem(final SftpSubsystemProxy subsystem) {
            info = getAuthenticationInfo(subsystem);
        }

        /**
         * Gets the domain list.
         *
         * @return the file list element[]
         */
        private FileListElement[] getDomainList() {
            final List<FileListElement> list = new ArrayList<>();
            final var hiddenDomainList = Cnf.listAt("SshPlugin", "hidden");
            for (final String key : Cnf.at("FtpDomainList", new Hashtable<>()).keySet()) {
                if (!"*".equals(key) && !hiddenDomainList.contains(key)) {
                    final var element = new FileListElement();
                    element.setRight("drwxr-x---");
                    element.setUser("" + info.profile().getIncomingUser().getId());
                    element.setGroup("ecpds");
                    element.setSize("2048");
                    element.setTime(time);
                    element.setName(key.toUpperCase());
                    list.add(element);
                }
            }
            return list.toArray(new FileListElement[list.size()]);
        }

        /**
         * Mkdir.
         *
         * @param dir
         *            the dir
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void mkdir(final String dir) throws EccmdException, IOException {
            throw new IOException("Permission denied");
        }

        /**
         * Gets the file list element.
         *
         * @param path
         *            the path
         *
         * @return the file list element
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public FileListElement getFileListElement(final String path) throws EccmdException, IOException {
            final var element = new FileListElement();
            element.setRight("drwxr-x---");
            element.setUser("" + info.profile().getIncomingUser().getId());
            element.setGroup("ecpds");
            element.setSize("2048");
            element.setTime(time);
            element.setName(path);
            return element;
        }

        /**
         * Gets the path.
         *
         * @param path
         *            the path
         *
         * @return the path
         *
         * @throws FileNotFoundException
         *             the file not found exception
         */
        @Override
        public String getPath(final String path) throws FileNotFoundException {
            throw new FileNotFoundException("Permission denied");
        }

        /**
         * Gets the file list.
         *
         * @param path
         *            the path
         * @param options
         *            the options
         *
         * @return the file list
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public FileListElement[] getFileList(final String path, final String options)
                throws EccmdException, IOException {
            return getDomainList();
        }

        /**
         * Gets the file size.
         *
         * @param source
         *            the source
         *
         * @return the file size
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public long getFileSize(final String source) throws EccmdException, IOException {
            return -1;
        }

        /**
         * Delete file.
         *
         * @param source
         *            the source
         * @param force
         *            the force
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void deleteFile(final String source, final boolean force) throws EccmdException, IOException {
            throw new IOException("Permission denied");
        }

        /**
         * Move file.
         *
         * @param source
         *            the source
         * @param target
         *            the target
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void moveFile(final String source, final String target) throws EccmdException, IOException {
            throw new IOException("Permission denied");
        }

        /**
         * Rmdir.
         *
         * @param dir
         *            the dir
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void rmdir(final String dir) throws EccmdException, IOException {
            throw new IOException("Permission denied");
        }

        /**
         * Gets the proxy socket input.
         *
         * @param source
         *            the source
         * @param offset
         *            the offset
         *
         * @return the proxy socket input
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public ProxySocket getProxySocketInput(final String source, final long offset)
                throws EccmdException, IOException {
            throw new IOException("Permission denied");
        }

        /**
         * Gets the proxy socket output.
         *
         * @param target
         *            the target
         * @param offset
         *            the offset
         * @param umask
         *            the umask
         *
         * @return the proxy socket output
         *
         * @throws EccmdException
         *             the eccmd exception
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public ProxySocket getProxySocketOutput(final String target, final long offset, final int umask)
                throws EccmdException, IOException {
            throw new IOException("Permission denied");
        }
    }

    /**
     * SeekableByteChannel wrapper around a ReadFile for SFTP download.
     */
    private final class ReadFileChannel implements SeekableByteChannel {
        private final ReadFile readFile;
        private long position = 0;
        private boolean open = true;

        ReadFileChannel(final ReadFile readFile) {
            this.readFile = readFile;
        }

        @Override
        public int read(final ByteBuffer dst) throws IOException {
            if (!open) {
                throw new IOException("Channel is closed");
            }
            final int len = dst.remaining();
            if (len == 0) {
                return 0;
            }
            final byte[] buf = new byte[len];
            final int n = readFile.read(position, buf, 0, len);
            if (n > 0) {
                dst.put(buf, 0, n);
                position += n;
            }
            return n;
        }

        @Override
        public int write(final ByteBuffer src) throws IOException {
            throw new UnsupportedOperationException("Read-only channel");
        }

        @Override
        public long position() {
            return position;
        }

        @Override
        public SeekableByteChannel position(final long newPosition) {
            this.position = newPosition;
            return this;
        }

        @Override
        public long size() {
            return readFile.getFileAttributes().size();
        }

        @Override
        public SeekableByteChannel truncate(final long size) {
            throw new UnsupportedOperationException("Read-only channel");
        }

        @Override
        public boolean isOpen() {
            return open;
        }

        @Override
        public void close() throws IOException {
            if (open) {
                open = false;
                readFile.close();
            }
        }
    }

    /**
     * SeekableByteChannel wrapper around a WriteFile for SFTP upload.
     */
    private final class WriteFileChannel implements SeekableByteChannel {
        private final WriteFile writeFile;
        private long position = 0;
        private boolean open = true;

        WriteFileChannel(final WriteFile writeFile) {
            this.writeFile = writeFile;
        }

        @Override
        public int read(final ByteBuffer dst) throws IOException {
            throw new UnsupportedOperationException("Write-only channel");
        }

        @Override
        public int write(final ByteBuffer src) throws IOException {
            if (!open) {
                throw new IOException("Channel is closed");
            }
            final int len = src.remaining();
            if (len == 0) {
                return 0;
            }
            final byte[] buf = new byte[len];
            src.get(buf);
            writeFile.write(position, buf, 0, len);
            position += len;
            return len;
        }

        @Override
        public long position() {
            return position;
        }

        @Override
        public SeekableByteChannel position(final long newPosition) {
            this.position = newPosition;
            return this;
        }

        @Override
        public long size() {
            return position;
        }

        @Override
        public SeekableByteChannel truncate(final long size) {
            throw new UnsupportedOperationException("Truncate not supported");
        }

        @Override
        public boolean isOpen() {
            return open;
        }

        @Override
        public void close() throws IOException {
            if (open) {
                open = false;
                writeFile.close();
            }
        }
    }

    /**
     * _read fully.
     *
     * @param in
     *            the in
     * @param b
     *            the b
     * @param off
     *            the off
     * @param len
     *            the len
     *
     * @return the int
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static final int readFully(final InputStream in, final byte[] b, final int off, final int len)
            throws IOException {
        var read = 0;
        var count = 0;
        do {
            count = in.read(b, off + read, len - read);
            if (count < 0) {
                read = read == 0 ? count : read;
                break;
            }
            read += count;
        } while (read < len);
        return read;
    }

    /**
     * _close quietly.
     *
     * @param out
     *            the out
     */
    private static final void closeQuietly(final Closeable out) {
        if (out != null) {
            try {
                out.close();
            } catch (final Throwable t) {
                // Ignored
            }
        }
    }

    /**
     * Close all files open.
     */
    public void closeFilesystem() {
        synchronized (openFiles) {
            for (final RemoteFile file : openFiles.values()) {
                if (file instanceof final OpenFile openFile) {
                    try {
                        openFile.close();
                    } catch (final IOException e) {
                        // Ignored
                    }
                }
            }
            openFiles.clear();
        }
    }

    private static AuthenticationInfo getAuthenticationInfo(final SftpSubsystemProxy subsystem) {
        final AuthenticationInfo info = subsystem.getSession().getAttribute(AuthenticationInfo.AUTHENTICATION_INFO);
        if (info != null)
            return info;
        else
            throw new UnsupportedOperationException("User not logged in");
    }

    /**
     * Returns (and lazily creates) the per-session file-attribute cache.
     */
    private static Map<String, FileListElement> getSessionCache(final SftpSubsystemProxy subsystem) {
        var session = subsystem.getSession();
        var cache = session.getAttribute(FILE_ATTR_CACHE_KEY);
        if (cache == null) {
            cache = new ConcurrentHashMap<>();
            session.setAttribute(FILE_ATTR_CACHE_KEY, cache);
        }
        return cache;
    }

    /**
     * Build a file-attributes map from a {@link FileListElement} for the given NIO view name.
     */
    private static Map<String, Object> buildFileAttributes(final FileListElement fle, final String view) {
        final var attrs = new LinkedHashMap<String, Object>();
        final var fa = RemoteFile.getFileAttributes(fle);
        attrs.put("lastModifiedTime", fa.lastModifiedTime());
        attrs.put("lastAccessTime", fa.lastAccessTime());
        attrs.put("creationTime", fa.creationTime());
        attrs.put("size", fa.size());
        attrs.put("isRegularFile", fa.isRegularFile());
        attrs.put("isDirectory", fa.isDirectory());
        attrs.put("isSymbolicLink", fa.isSymbolicLink());
        attrs.put("isOther", fa.isOther());
        attrs.put("fileKey", fa.fileKey());
        // Always include permissions — Mina SSHD calls IoUtils.getPermissions(path.toFile()) via
        // resolveMissingFileAttributeValue when this key is absent, which fails for our virtual paths.
        attrs.put("permissions", toPermissions(fle.getRight()));
        return attrs;
    }

    /**
     * Convert a Unix permissions string (e.g. {@code "drwxr-x---"}) to a {@link PosixFilePermission} set.
     */
    private static Set<PosixFilePermission> toPermissions(final String rights) {
        final var perms = EnumSet.noneOf(PosixFilePermission.class);
        if (rights == null || rights.length() < 10) {
            return perms;
        }
        if (rights.charAt(1) == 'r')
            perms.add(PosixFilePermission.OWNER_READ);
        if (rights.charAt(2) == 'w')
            perms.add(PosixFilePermission.OWNER_WRITE);
        if (rights.charAt(3) == 'x' || rights.charAt(3) == 's')
            perms.add(PosixFilePermission.OWNER_EXECUTE);
        if (rights.charAt(4) == 'r')
            perms.add(PosixFilePermission.GROUP_READ);
        if (rights.charAt(5) == 'w')
            perms.add(PosixFilePermission.GROUP_WRITE);
        if (rights.charAt(6) == 'x' || rights.charAt(6) == 's')
            perms.add(PosixFilePermission.GROUP_EXECUTE);
        if (rights.charAt(7) == 'r')
            perms.add(PosixFilePermission.OTHERS_READ);
        if (rights.charAt(8) == 'w')
            perms.add(PosixFilePermission.OTHERS_WRITE);
        if (rights.charAt(9) == 'x' || rights.charAt(9) == 't')
            perms.add(PosixFilePermission.OTHERS_EXECUTE);
        return perms;
    }

    /**
     * Invoked in order to resolve remote file paths reference by the client into ones accessible by the server
     *
     * @param subsystem
     *            The SFTP subsystem instance that manages the session
     * @param rootDir
     *            The default root directory used to resolve relative paths - a.k.a. the {@code chroot} location
     * @param remotePath
     *            The remote path - separated by '/'
     *
     * @return The local {@link Path}
     *
     * @throws IOException
     *             If failed to resolve the local path
     * @throws InvalidPathException
     *             If bad local path specification
     *
     * @see SftpSubsystemEnvironment#getDefaultDirectory() SftpSubsystemEnvironment#getDefaultDirectory()
     */
    @Override
    public Path resolveLocalFilePath(SftpSubsystemProxy subsystem, Path rootDir, String remotePath)
            throws IOException, InvalidPathException {
        final AuthenticationInfo info = getAuthenticationInfo(subsystem);
        _log.debug("resolveLocalFilePath: '{}' '{}' '{}'", info.session().getUser(), rootDir, remotePath);
        // Return a VirtualEcpdsPath — backed by VirtualEcpdsFileSystem — so that all
        // Mina SSHD NIO checks (Files.exists, Files.isDirectory, Files.readAttributes)
        // are handled by VirtualEcpdsFileSystemProvider and never touch the real local FS.
        final Path result;
        if (remotePath == null || remotePath.isEmpty() || ".".equals(remotePath)) {
            // Map the user's default directory to the virtual root — always a directory.
            result = new VirtualEcpdsPath("/", Boolean.TRUE);
        } else {
            // Convert to forward slashes; ensure absolute so the client always sees /domain/path.
            var p = remotePath.replace('\\', '/');
            if (!p.startsWith("/")) {
                p = "/" + p;
            }
            // Look up the per-session cache (populated by openDirectory) to determine whether
            // this path is a file or directory. This allows Mina's internal Files.isDirectory()
            // check in doRemoveFile (and similar) to return the correct answer without going
            // through the accessor's readFileAttributes override.
            final FileListElement cached = getSessionCache(subsystem).get(p);
            final Boolean knownDir = cached != null ? RemoteFile.getFileAttributes(cached).isDirectory() : null;
            result = new VirtualEcpdsPath(p, knownDir);
        }
        _log.debug("resolveLocalFilePath >>> RESULT: '{}'", result);
        return result;
    }

    @Override
    public NavigableMap<String, Object> resolveReportedFileAttributes(SftpSubsystemProxy subsystem, Path file,
            int flags, NavigableMap<String, Object> attrs, LinkOption... options) throws IOException {
        final AuthenticationInfo info = getAuthenticationInfo(subsystem);
        _log.debug("resolveReportedFileAttributes: '{}' '{}' '{}' '{}' '{}'", info.session().getUser(), file, flags,
                attrs, options);
        return attrs;
    }

    @Override
    public void applyExtensionFileAttributes(SftpSubsystemProxy subsystem, Path file, Map<String, byte[]> extensions,
            LinkOption... options) throws IOException {
        final AuthenticationInfo info = getAuthenticationInfo(subsystem);
        _log.debug("IGNORED >>> applyExtensionFileAttributes: '{}' '{}' '{}' '{}'", info.session().getUser(), file,
                extensions, options);
        // Ignored
    }

    @Override
    public void putRemoteFileName(SftpSubsystemProxy subsystem, Path path, Buffer buf, String name, boolean shortName)
            throws IOException {
        final AuthenticationInfo info = getAuthenticationInfo(subsystem);
        _log.debug("putRemoteFileName: '{}' '{}' '{}' '{}' '{}'", info.session().getUser(), path, buf, name, shortName);
        buf.putString(name);
    }

    @Override
    public SeekableByteChannel openFile(SftpSubsystemProxy subsystem, FileHandle fileHandle, Path file, String handle,
            Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        final AuthenticationInfo info = getAuthenticationInfo(subsystem);
        _log.debug("openFile: '{}' '{}' '{}' '{}' '{}' '{}'", info.session().getUser(), fileHandle, file, handle,
                options, attrs);
        final var optList = Arrays.asList(options.toArray(new OpenOption[0]));
        final boolean isWrite = optList.contains(StandardOpenOption.WRITE)
                || optList.contains(StandardOpenOption.APPEND) || optList.contains(StandardOpenOption.CREATE)
                || optList.contains(StandardOpenOption.CREATE_NEW)
                || optList.contains(StandardOpenOption.TRUNCATE_EXISTING);
        // Build a FileListElement whose name is the full local path so that ReadFile /
        // WriteFile can resolve it back through getURL().
        final var fle = new FileListElement();
        fle.setName(file.toString());
        fle.setTime(System.currentTimeMillis());
        if (isWrite) {
            fle.setRight("-rw-r-----");
            fle.setSize("0");
            final var wf = new WriteFile(subsystem, fle);
            openFiles.put(handle, wf);
            _log.debug("openFile >>> WRITE handle='{}' path='{}'", handle, file);
            return new WriteFileChannel(wf);
        } else {
            // Try to populate size from the virtual FS.
            try {
                final var url = getURL(subsystem, file.toString());
                final var stat = getFileSystem(subsystem, url).getFileListElement(url.dir);
                fle.setRight(stat.getRight() != null ? stat.getRight() : "-r--r-----");
                fle.setSize(stat.getSize() != null ? stat.getSize() : "0");
                fle.setTime(stat.getTime() > 0 ? stat.getTime() : System.currentTimeMillis());
            } catch (final EccmdException | IOException e) {
                fle.setRight("-r--r-----");
                fle.setSize("0");
            }
            final var rf = new ReadFile(subsystem, fle);
            openFiles.put(handle, rf);
            _log.debug("openFile >>> READ handle='{}' path='{}'", handle, file);
            return new ReadFileChannel(rf);
        }
    }

    @Override
    public FileLock tryLock(SftpSubsystemProxy subsystem, FileHandle fileHandle, Path file, String handle,
            Channel channel, long position, long size, boolean shared) throws IOException {
        final AuthenticationInfo info = getAuthenticationInfo(subsystem);
        _log.debug("tryLock: '{}' '{}' '{}' '{}' '{}' '{}' '{}' '{}'", info.session().getUser(), fileHandle, file,
                handle, channel, position, size, shared);
        if (!(channel instanceof FileChannel)) {
            throw new StreamCorruptedException("Non file channel to lock: " + channel);
        }

        return ((FileChannel) channel).lock(position, size, shared);
    }

    @Override
    public void syncFileData(SftpSubsystemProxy subsystem, FileHandle fileHandle, Path file, String handle,
            Channel channel) throws IOException {
        final AuthenticationInfo info = getAuthenticationInfo(subsystem);
        _log.debug("IGNORED >>> syncFileData: '{}' '{}' '{}' '{}' '{}'", info.session().getUser(), fileHandle, file,
                handle, channel);
        // Ignored
    }

    /**
     * Called to inform the accessor that it should close the file
     *
     * @param subsystem
     *            The SFTP subsystem instance that manages the session
     * @param fileHandle
     *            The {@link FileHandle} representing the created channel - may be {@code null} if not invoked within
     *            the context of such a handle (special cases)
     * @param file
     *            The requested <U>local</U> file {@link Path} - same one returned by
     *            {@link #resolveLocalFilePath(SftpSubsystemProxy, Path, String) resolveLocalFilePath}
     * @param handle
     *            The assigned file handle through which the remote peer references this file
     * @param channel
     *            The original {@link Channel} that was returned by
     *            {@link #openFile(SftpSubsystemProxy, FileHandle, Path, String, Set, FileAttribute...)}
     * @param options
     *            The original options used to open the channel
     *
     * @throws IOException
     *             If failed to execute the request
     */
    @Override
    public void closeFile(SftpSubsystemProxy subsystem, FileHandle fileHandle, Path file, String handle,
            Channel channel, Set<? extends OpenOption> options) throws IOException {
        final AuthenticationInfo info = getAuthenticationInfo(subsystem);
        _log.debug("closeFile: '{}' '{}' '{}' '{}' '{}'  '{}'", info.session().getUser(), fileHandle, file, handle,
                channel, options);
        // Close via our virtual file tracking map when available.
        final var remoteFile = openFiles.remove(handle);
        if (remoteFile instanceof final OpenFile openFile) {
            try {
                openFile.close();
            } catch (final IOException e) {
                _log.warn("closeFile: error closing handle '{}': {}", handle, e.getMessage());
            }
            return;
        }
        // Fallback: close the raw channel directly (should not normally happen).
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
    }

    /**
     * Called when a new directory stream is requested
     *
     * @param subsystem
     *            The SFTP subsystem instance that manages the session
     * @param dirHandle
     *            The {@link DirectoryHandle} representing the stream
     * @param dir
     *            The requested <U>local</U> directory {@link Path} - same one returned by
     *            {@link #resolveLocalFilePath(SftpSubsystemProxy, Path, String) resolveLocalFilePath}
     * @param handle
     *            The assigned directory handle through which the remote peer references this directory
     * @param linkOptions
     *            The {@link LinkOption}s - OK if {@code null}/empty.
     *
     * @return The opened {@link DirectoryStream}
     *
     * @throws IOException
     *             If failed to open
     */
    @Override
    public DirectoryStream<Path> openDirectory(SftpSubsystemProxy subsystem, DirectoryHandle dirHandle, Path dir,
            String handle, LinkOption... linkOptions) throws IOException {
        final AuthenticationInfo info = getAuthenticationInfo(subsystem);
        _log.debug("openDirectory: '{}' '{}' '{}' '{}' '{}'", info.session().getUser(), dirHandle, dir, handle,
                linkOptions);
        final var dirPath = dir.toString();
        final var url = getURL(subsystem, dirPath);
        final FileListElement f;
        try {
            f = getFileSystem(subsystem, url).getFileListElement(url.dir);
        } catch (final EccmdException e) {
            throw new IOException(e.getMessage());
        }
        // Use right field (e.g. "drwxr-x---") rather than the dir boolean flag which
        // may not be set by all FileSystem implementations.
        if (!RemoteFile.getFileAttributes(f).isDirectory()) {
            throw new FileNotFoundException(dirPath + " is not a directory");
        }
        final var result = new ArrayList<Path>();
        final var sessionCache = getSessionCache(subsystem);
        try {
            final FileListElement[] children = getFileSystem(subsystem, url).getFileList(url.dir, "n");
            final var od = new OpenDirectory(subsystem, f, children);
            boolean hasMore = true;
            while (hasMore) {
                try {
                    for (final FileListElement d : od.getChildren()) {
                        // Build the child path with the correct isDirectory flag so that
                        // VirtualEcpdsFileSystemProvider can answer Files.isDirectory() correctly
                        // without needing session context.
                        final var childIsDir = RemoteFile.getFileAttributes(d).isDirectory();
                        final Path entryPath = new VirtualEcpdsPath(
                                dir.toString().equals("/") ? "/" + d.getName() : dir + "/" + d.getName(), childIsDir);
                        result.add(entryPath);
                        // Cache attributes so readFileAttributes avoids extra backend round-trips.
                        sessionCache.put(entryPath.toString(), d);
                    }
                } catch (final EOFException e) {
                    hasMore = false;
                }
            }
        } catch (final EccmdException e) {
            throw new IOException(e.getMessage());
        }
        return new VirtualDirectoryStream(result);
    }

    /**
     * Called when a directory stream is no longer required
     *
     * @param subsystem
     *            The SFTP subsystem instance that manages the session
     * @param dirHandle
     *            The {@link DirectoryHandle} representing the stream - may be {@code null} if not invoked within the
     *            context of such a handle (special cases)
     * @param dir
     *            The requested <U>local</U> directory {@link Path} - same one returned by
     *            {@link #resolveLocalFilePath(SftpSubsystemProxy, Path, String) resolveLocalFilePath}
     * @param handle
     *            The assigned directory handle through which the remote peer references this directory
     * @param ds
     *            The disposed {@link DirectoryStream}
     *
     * @throws IOException
     *             If failed to open
     */
    @Override
    public void closeDirectory(SftpSubsystemProxy subsystem, DirectoryHandle dirHandle, Path dir, String handle,
            DirectoryStream<Path> ds) throws IOException {
        final AuthenticationInfo info = getAuthenticationInfo(subsystem);
        _log.debug("closeDirectory: '{}' '{}' '{}' '{}' '{}'", info.session().getUser(), dirHandle, dir, handle, ds);
        if (ds == null) {
            return; // debug breakpoint
        }
        ds.close();
    }

    @Override
    public Map<String, ?> readFileAttributes(SftpSubsystemProxy subsystem, Path file, String view,
            LinkOption... options) throws IOException {
        final AuthenticationInfo info = getAuthenticationInfo(subsystem);
        _log.debug("readFileAttributes: '{}' '{}' '{}' '{}'", info.session().getUser(), file, view, options);
        // Consult the per-session cache populated by openDirectory first.
        FileListElement fle = getSessionCache(subsystem).get(file.toString());
        if (fle == null) {
            // Direct stat — look up via the virtual filesystem.
            try {
                final var url = getURL(subsystem, file.toString());
                fle = getFileSystem(subsystem, url).getFileListElement(url.dir);
            } catch (final EccmdException e) {
                throw new IOException(e.getMessage());
            }
        }
        return buildFileAttributes(fle, view);
    }

    @Override
    public void setFileAttribute(SftpSubsystemProxy subsystem, Path file, String view, String attribute, Object value,
            LinkOption... options) throws IOException {
        throw new UnsupportedOperationException("Attribute set not supported for " + file);
    }

    @Override
    public void setFileOwner(SftpSubsystemProxy subsystem, Path file, Principal value, LinkOption... options)
            throws IOException {
        throw new UnsupportedOperationException("Owner view not supported for " + file);
    }

    @Override
    public void setGroupOwner(SftpSubsystemProxy subsystem, Path file, Principal value, LinkOption... options)
            throws IOException {
        throw new UnsupportedOperationException("Group set not supported for " + file);
    }

    @Override
    public void setFilePermissions(SftpSubsystemProxy subsystem, Path file, Set<PosixFilePermission> perms,
            LinkOption... options) throws IOException {
        throw new UnsupportedOperationException("Permissions set not supported for " + file);
    }

    @Override
    public void setFileAccessControl(SftpSubsystemProxy subsystem, Path file, List<AclEntry> acl, LinkOption... options)
            throws IOException {
        throw new UnsupportedOperationException("ACL set not supported for " + file);
    }

    @Override
    public void createDirectory(SftpSubsystemProxy subsystem, Path path) throws IOException {
        final AuthenticationInfo info = getAuthenticationInfo(subsystem);
        _log.debug("createDirectory: '{}' '{}'", info.session().getUser(), path);
        try {
            final var url = getURL(subsystem, path.toString());
            getFileSystem(subsystem, url).mkdir(url.dir);
        } catch (final EccmdException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public void createLink(SftpSubsystemProxy subsystem, Path link, Path existing, boolean symLink) throws IOException {
        throw new UnsupportedOperationException("Link not supported");
    }

    @Override
    public void renameFile(SftpSubsystemProxy subsystem, Path oldPath, Path newPath, Collection<CopyOption> opts)
            throws IOException {
        final AuthenticationInfo info = getAuthenticationInfo(subsystem);
        _log.debug("renameFile: '{}' '{}' '{}' '{}'", info.session().getUser(), oldPath, newPath, opts);
        try {
            final var urlOld = getURL(subsystem, oldPath.toString());
            final var urlNew = getURL(subsystem, newPath.toString());
            getFileSystem(subsystem, urlOld).moveFile(urlOld.dir, urlNew.dir);
        } catch (final EccmdException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public void copyFile(SftpSubsystemProxy subsystem, Path src, Path dst, Collection<CopyOption> opts)
            throws IOException {
        final AuthenticationInfo info = getAuthenticationInfo(subsystem);
        _log.debug("copyFile: '{}' '{}' '{}' '{}'", info.session().getUser(), src, dst, opts);
        try {
            final var urlSrc = getURL(subsystem, src.toString());
            final var urlDst = getURL(subsystem, dst.toString());
            // Delegate to the backend; erase destination if REPLACE_EXISTING was requested.
            final boolean erase = opts != null
                    && opts.stream().anyMatch(o -> o == java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            info.session().copyFile(urlSrc.dir, urlDst.dir, erase);
        } catch (final EccmdException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public void removeFile(SftpSubsystemProxy subsystem, Path path, boolean isDirectory) throws IOException {
        final AuthenticationInfo info = getAuthenticationInfo(subsystem);
        _log.debug("removeFile: '{}' '{}' '{}'", info.session().getUser(), path, isDirectory);
        try {
            final var url = getURL(subsystem, path.toString());
            if (isDirectory) {
                getFileSystem(subsystem, url).rmdir(url.dir);
            } else {
                getFileSystem(subsystem, url).deleteFile(url.dir, false);
            }
        } catch (final EccmdException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public boolean noFollow(Collection<?> opts) {
        _log.debug("noFollow: '{}'", opts);
        for (var opt : opts) {
            if (LinkOption.NOFOLLOW_LINKS.equals(opt)) {
                return true;
            }
        }
        return false;
    }
}
