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
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.List;
import java.util.Set;

/**
 * A singleton virtual {@link FileSystem} used to back all SFTP path operations. Every {@link VirtualEcpdsPath} returned
 * by {@link MinaFileSystemAccessor#resolveLocalFilePath} belongs to this file system, so all NIO calls
 * ({@code Files.exists()}, {@code Files.isDirectory()}, {@code Files.readAttributes()}, etc.) are handled by
 * {@link VirtualEcpdsFileSystemProvider} rather than the real local filesystem.
 */
final class VirtualEcpdsFileSystem extends FileSystem {

    static final VirtualEcpdsFileSystem INSTANCE = new VirtualEcpdsFileSystem();

    private VirtualEcpdsFileSystem() {
    }

    @Override
    public FileSystemProvider provider() {
        return VirtualEcpdsFileSystemProvider.INSTANCE;
    }

    @Override
    public void close() throws IOException {
        // Singleton — never close.
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public String getSeparator() {
        return "/";
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        return List.of(new VirtualEcpdsPath("/"));
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return List.of();
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return Set.of("basic");
    }

    @Override
    public Path getPath(String first, String... more) {
        if (more == null || more.length == 0) {
            return new VirtualEcpdsPath(first);
        }
        final var sb = new StringBuilder(first);
        for (final var s : more) {
            if (!sb.isEmpty() && sb.charAt(sb.length() - 1) != '/') {
                sb.append('/');
            }
            sb.append(s);
        }
        return new VirtualEcpdsPath(sb.toString());
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WatchService newWatchService() throws IOException {
        throw new UnsupportedOperationException();
    }
}
