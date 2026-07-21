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

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A virtual NIO {@link FileSystemProvider} that backs all SFTP path operations. Every path is treated as accessible and
 * directory-like so that Mina SSHD's internal NIO checks ({@code Files.exists()}, {@code Files.isDirectory()}, etc.)
 * succeed before control reaches the real {@code SftpFileSystemAccessor} overrides in {@link MinaFileSystemAccessor},
 * which query the actual OpenECPDS data portal API for attributes.
 */
final class VirtualEcpdsFileSystemProvider extends FileSystemProvider {

    static final VirtualEcpdsFileSystemProvider INSTANCE = new VirtualEcpdsFileSystemProvider();

    private static final FileTime EPOCH = FileTime.fromMillis(0L);

    private VirtualEcpdsFileSystemProvider() {
    }

    @Override
    public String getScheme() {
        return "ecpds";
    }

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) {
        return VirtualEcpdsFileSystem.INSTANCE;
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        return VirtualEcpdsFileSystem.INSTANCE;
    }

    @Override
    public Path getPath(URI uri) {
        return new VirtualEcpdsPath(uri.getPath());
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
            throws IOException {
        throw new UnsupportedOperationException("Use SftpFileSystemAccessor.openFile");
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter)
            throws IOException {
        throw new UnsupportedOperationException("Use SftpFileSystemAccessor.openDirectory");
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        throw new UnsupportedOperationException("Use SftpFileSystemAccessor.createDirectory");
    }

    @Override
    public void delete(Path path) throws IOException {
        throw new UnsupportedOperationException("Use SftpFileSystemAccessor.removeFile");
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        throw new UnsupportedOperationException("Use SftpFileSystemAccessor.copyFile");
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        throw new UnsupportedOperationException("Use SftpFileSystemAccessor.renameFile");
    }

    @Override
    public boolean isSameFile(Path path, Path path2) {
        return path.normalize().equals(path2.normalize());
    }

    @Override
    public boolean isHidden(Path path) {
        return false;
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * All virtual paths are considered accessible; real authorization is enforced by {@link MinaFileSystemAccessor}
     * through the OpenECPDS {@code UserSession}.
     */
    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        // Always succeed — let UserSession handle the real authorization.
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        return null;
    }

    /**
     * Returns a map where every path looks like an accessible directory by default. When the path is a
     * {@link VirtualEcpdsPath} with an explicit {@link VirtualEcpdsPath#knownIsDirectory()} value (populated from the
     * per-session cache in {@link MinaFileSystemAccessor#resolveLocalFilePath}), the correct {@code isDirectory} /
     * {@code isRegularFile} flags are returned so that Mina SSHD's internal {@code Files.isDirectory()} check in
     * {@code doRemoveFile} does not reject a file as "is a folder".
     */
    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        final Boolean knownDir = (path instanceof VirtualEcpdsPath vp) ? vp.knownIsDirectory() : null;
        // null = unknown → treat as directory so openDirectory proceeds.
        final boolean isDir = knownDir == null || knownDir;
        final var map = new HashMap<String, Object>();
        map.put("isDirectory", isDir);
        map.put("isRegularFile", !isDir);
        map.put("isSymbolicLink", Boolean.FALSE);
        map.put("isOther", Boolean.FALSE);
        map.put("size", 0L);
        map.put("lastModifiedTime", EPOCH);
        map.put("lastAccessTime", EPOCH);
        map.put("creationTime", EPOCH);
        return map;
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options)
            throws IOException {
        if (type.isAssignableFrom(BasicFileAttributes.class)) {
            final Boolean knownDir = (path instanceof VirtualEcpdsPath vp) ? vp.knownIsDirectory() : null;
            final boolean isDir = knownDir == null || knownDir;
            return type.cast(new VirtualFileAttributes(isDir));
        }
        throw new UnsupportedOperationException("Unsupported attributes type: " + type);
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
        // Virtual FS does not persist attributes.
    }

    private static final class VirtualFileAttributes implements BasicFileAttributes {
        private final boolean directory;

        VirtualFileAttributes(boolean directory) {
            this.directory = directory;
        }

        @Override
        public FileTime lastModifiedTime() {
            return EPOCH;
        }

        @Override
        public FileTime lastAccessTime() {
            return EPOCH;
        }

        @Override
        public FileTime creationTime() {
            return EPOCH;
        }

        @Override
        public boolean isRegularFile() {
            return !directory;
        }

        @Override
        public boolean isDirectory() {
            return directory;
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
            return 0L;
        }

        @Override
        public Object fileKey() {
            return null;
        }
    }
}
