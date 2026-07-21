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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

/**
 * An immutable {@link Path} backed by {@link VirtualEcpdsFileSystem}. All NIO operations (existence, attribute reads)
 * are routed to {@link VirtualEcpdsFileSystemProvider}, which returns generic directory-like attributes. The real
 * per-entry attributes are provided by {@link MinaFileSystemAccessor#readFileAttributes}, which queries the OpenECPDS
 * data portal API.
 *
 * <p>
 * The string representation is a clean POSIX-style path, e.g. {@code "/"}, {@code "/DATA"}, or
 * {@code "/DATA/2026/file.txt"}. This is what {@link MinaFileSystemAccessor#getURL} receives to determine the OpenECPDS
 * domain and sub-path.
 * </p>
 */
final class VirtualEcpdsPath implements Path {

    /** Normalized POSIX-style path string. */
    private final String pathStr;

    /**
     * Known directory status: {@code TRUE} = directory, {@code FALSE} = regular file, {@code null} = unknown.
     * <p>
     * When null, {@link VirtualEcpdsFileSystemProvider} defaults to {@code isDirectory=true} so that Mina SSHD's
     * internal {@code Files.isDirectory()} check allows {@code openDirectory} to proceed. When explicitly set to
     * {@code false} (populated from the per-session cache in {@link MinaFileSystemAccessor#resolveLocalFilePath}), the
     * provider returns {@code isDirectory=false} so that Mina's pre-condition check in {@code doRemoveFile} does not
     * reject a file as "is a folder".
     */
    private final Boolean isDirectory;

    VirtualEcpdsPath(String path) {
        this(path, null);
    }

    VirtualEcpdsPath(String path, Boolean isDirectory) {
        this.pathStr = normalize(path);
        this.isDirectory = isDirectory;
    }

    /** Returns the known directory status, or {@code null} if not yet determined. */
    Boolean knownIsDirectory() {
        return isDirectory;
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    /**
     * Normalizes a path string: collapse {@code .} and {@code ..} segments, deduplicate slashes, and preserve
     * absolute/relative distinction.
     */
    private static String normalize(String path) {
        if (path == null || path.isEmpty()) {
            return ".";
        }
        path = path.replace('\\', '/');
        final boolean absolute = path.startsWith("/");
        final var parts = new ArrayDeque<String>();
        for (final var seg : path.split("/")) {
            if (seg.isEmpty() || ".".equals(seg)) {
                continue;
            }
            if ("..".equals(seg)) {
                if (!parts.isEmpty()) {
                    parts.pollLast();
                }
            } else {
                parts.addLast(seg);
            }
        }
        if (parts.isEmpty()) {
            return absolute ? "/" : ".";
        }
        final var sb = new StringBuilder();
        if (absolute) {
            sb.append('/');
        }
        final var it = parts.iterator();
        sb.append(it.next());
        while (it.hasNext()) {
            sb.append('/').append(it.next());
        }
        return sb.toString();
    }

    /** Returns the segments (name elements) of this path, excluding the leading {@code /} when absolute. */
    private String[] segments() {
        if ("/".equals(pathStr) || ".".equals(pathStr)) {
            return new String[0];
        }
        final var raw = pathStr.startsWith("/") ? pathStr.substring(1) : pathStr;
        return raw.split("/");
    }

    // -----------------------------------------------------------------------
    // Path interface
    // -----------------------------------------------------------------------

    @Override
    public FileSystem getFileSystem() {
        return VirtualEcpdsFileSystem.INSTANCE;
    }

    @Override
    public boolean isAbsolute() {
        return pathStr.startsWith("/");
    }

    @Override
    public Path getRoot() {
        return isAbsolute() ? new VirtualEcpdsPath("/", Boolean.TRUE) : null;
    }

    @Override
    public Path getFileName() {
        if ("/".equals(pathStr)) {
            return null;
        }
        final var idx = pathStr.lastIndexOf('/');
        // File name component — type unknown, inherit from this path.
        return new VirtualEcpdsPath(idx < 0 ? pathStr : pathStr.substring(idx + 1), isDirectory);
    }

    @Override
    public Path getParent() {
        if ("/".equals(pathStr) || !pathStr.contains("/")) {
            return null;
        }
        final var idx = pathStr.lastIndexOf('/');
        // Parent is always a directory.
        return new VirtualEcpdsPath(idx == 0 ? "/" : pathStr.substring(0, idx), Boolean.TRUE);
    }

    @Override
    public int getNameCount() {
        return segments().length;
    }

    @Override
    public Path getName(int index) {
        final var segs = segments();
        if (index < 0 || index >= segs.length) {
            throw new IllegalArgumentException("Index " + index + " out of range for path: " + pathStr);
        }
        return new VirtualEcpdsPath(segs[index]);
    }

    @Override
    public Path subpath(int beginIndex, int endIndex) {
        final var segs = segments();
        if (beginIndex < 0 || endIndex > segs.length || beginIndex >= endIndex) {
            throw new IllegalArgumentException(
                    "Invalid subpath [" + beginIndex + "," + endIndex + "] for path: " + pathStr);
        }
        return new VirtualEcpdsPath(String.join("/", Arrays.copyOfRange(segs, beginIndex, endIndex)));
    }

    @Override
    public boolean startsWith(Path other) {
        return pathStr.equals(other.toString()) || pathStr.startsWith(other.toString() + "/");
    }

    @Override
    public boolean endsWith(Path other) {
        final var o = other.toString();
        return pathStr.equals(o) || pathStr.endsWith("/" + o);
    }

    @Override
    public Path normalize() {
        return this; // Already normalized in constructor.
    }

    @Override
    public Path resolve(Path other) {
        if (other.isAbsolute()) {
            return other instanceof VirtualEcpdsPath ? other : new VirtualEcpdsPath(other.toString());
        }
        final var otherStr = other.toString();
        if (otherStr.isEmpty() || ".".equals(otherStr)) {
            return this;
        }
        final var base = "/".equals(pathStr) ? "/" : pathStr + "/";
        return new VirtualEcpdsPath(base + otherStr);
    }

    @Override
    public Path resolveSibling(Path other) {
        final var parent = getParent();
        return parent == null ? other : parent.resolve(other);
    }

    @Override
    public Path relativize(Path other) {
        final var otherStr = other.toString();
        if (pathStr.equals(otherStr)) {
            return new VirtualEcpdsPath(".");
        }
        final var prefix = "/".equals(pathStr) ? "/" : pathStr + "/";
        if (otherStr.startsWith(prefix)) {
            return new VirtualEcpdsPath(otherStr.substring(prefix.length()));
        }
        throw new IllegalArgumentException("Cannot relativize '" + other + "' against '" + this + "'");
    }

    @Override
    public URI toUri() {
        return URI.create("ecpds://" + pathStr);
    }

    @Override
    public Path toAbsolutePath() {
        return isAbsolute() ? this : new VirtualEcpdsPath("/" + pathStr);
    }

    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        return toAbsolutePath();
    }

    @Override
    public File toFile() {
        throw new UnsupportedOperationException("VirtualEcpdsPath is not backed by a real file");
    }

    @Override
    public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Path> iterator() {
        final var segs = segments();
        if (segs.length == 0) {
            return Collections.emptyIterator();
        }
        return Arrays.stream(segs).map(s -> (Path) new VirtualEcpdsPath(s)).iterator();
    }

    @Override
    public int compareTo(Path other) {
        return pathStr.compareTo(other.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VirtualEcpdsPath other)) {
            return false;
        }
        return pathStr.equals(other.pathStr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathStr);
    }

    /** Returns the clean POSIX path string, e.g. {@code "/"}, {@code "/DATA"}, {@code "/DATA/2026/file.txt"}. */
    @Override
    public String toString() {
        return pathStr;
    }
}
