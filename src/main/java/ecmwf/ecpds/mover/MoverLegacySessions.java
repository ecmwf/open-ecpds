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
 */

package ecmwf.ecpds.mover;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import ecmwf.common.technical.Cnf;

/** Counts actual module lifetimes, not dispatch acknowledgements or transfer status updates. */
final class MoverLegacySessions {
    private static final int CAPACITY = 16384;
    // Closing a second channel for the same inode can release a process-wide POSIX lock.
    private static final Set<Object> OPEN_LEASES = new HashSet<>();
    private final Map<Long, Set<UUID>> sessions = new HashMap<>();
    private final Map<Long, UUID> fences = new HashMap<>();
    private final Map<Long, UUID> incarnations = new HashMap<>();
    private final Set<Long> uncertain = new HashSet<>();
    private final Path directory;
    private final FileChannel lease;
    private final FileLock leaseLock;
    private final Object leaseIdentity;
    private boolean closed;
    private int count;

    record Snapshot(UUID incarnation, UUID cutover, int sessions) {
    }

    static MoverLegacySessions configured() throws IOException {
        if (!Cnf.at("PasswordDelivery", "trackLegacySessions", false)) {
            return null;
        }
        final var hosts = new HashSet<Long>();
        try {
            for (final var value : Cnf.at("PasswordDelivery", "legacySessionHosts", "").split(",", -1)) {
                final var host = Long.parseLong(value.trim());
                if (host <= 0 || !hosts.add(host) || hosts.size() > 4096) {
                    throw new IllegalArgumentException();
                }
            }
            return new MoverLegacySessions(Path.of(Cnf.at("PasswordDelivery", "legacySessionStateDirectory", "")),
                    hosts);
        } catch (final IllegalArgumentException _) {
            throw new IOException("Invalid legacy session tracking configuration");
        }
    }

    MoverLegacySessions(final Path directory, final Set<Long> hosts) throws IOException {
        if (!directory.isAbsolute() || hosts.isEmpty() || hosts.size() > 4096 || hosts.stream().anyMatch(h -> h <= 0)) {
            throw new IOException("Invalid legacy session tracking configuration");
        }
        this.directory = directory;
        Files.createDirectories(directory,
                PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------")));
        final var permissions = Files.getPosixFilePermissions(directory, LinkOption.NOFOLLOW_LINKS);
        if (Files.isSymbolicLink(directory) || !Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)
                || permissions.contains(PosixFilePermission.GROUP_WRITE)
                || permissions.contains(PosixFilePermission.OTHERS_WRITE)) {
            throw new IOException("Unsafe legacy session tracking directory");
        }
        final var lockPath = directory.resolve(".tracking.lock");
        if (!Files.exists(lockPath, LinkOption.NOFOLLOW_LINKS)) {
            try {
                Files.createFile(lockPath);
            } catch (final java.nio.file.FileAlreadyExistsException _) {
                // Another tracker created the file before acquiring its lock.
            }
        }
        final var attributes = Files.readAttributes(lockPath, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        final var identity = attributes.fileKey();
        if (!attributes.isRegularFile() || identity == null) {
            throw new IOException("Invalid legacy session tracking lock");
        }
        synchronized (OPEN_LEASES) {
            if (!OPEN_LEASES.add(identity)) {
                throw new IOException("Legacy session tracking directory is already in use");
            }
        }
        FileChannel channel = null;
        try {
            channel = FileChannel.open(lockPath, StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS);
            final FileLock acquired;
            try {
                acquired = channel.tryLock();
            } catch (final OverlappingFileLockException _) {
                throw new IOException("Legacy session tracking directory is already in use");
            }
            if (acquired == null) {
                throw new IOException("Legacy session tracking directory is already in use");
            }
            lease = channel;
            leaseLock = acquired;
            leaseIdentity = identity;
            for (final var host : hosts) {
                sessions.put(host, new HashSet<>());
                incarnations.put(host, UUID.randomUUID());
                final var path = marker(host);
                if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                    fences.put(host, readMarker(path));
                }
                final var drained = drainedMarker(host);
                if (Files.exists(drained, LinkOption.NOFOLLOW_LINKS)) {
                    incarnations.put(host, readDrainedMarker(drained, fences.get(host)));
                }
                final var active = activeMarker(host);
                if (Files.exists(active, LinkOption.NOFOLLOW_LINKS)) {
                    if (!Files.isRegularFile(active, LinkOption.NOFOLLOW_LINKS)) {
                        throw new IOException("Invalid legacy session admission evidence");
                    }
                    if (!Files.exists(drained, LinkOption.NOFOLLOW_LINKS)) {
                        uncertain.add(host);
                    }
                }
            }
            try (final var entries = Files.list(directory)) {
                if (entries.anyMatch(path -> (path.getFileName().toString().endsWith(".fence")
                        || path.getFileName().toString().endsWith(".drained")
                        || path.getFileName().toString().endsWith(".active"))
                        && hosts.stream().noneMatch(host -> marker(host).equals(path)
                                || drainedMarker(host).equals(path) || activeMarker(host).equals(path)))) {
                    throw new IOException("A Host with legacy session evidence was removed from tracking");
                }
            }
        } catch (final IOException | RuntimeException e) {
            if (channel != null) {
                channel.close();
            }
            synchronized (OPEN_LEASES) {
                OPEN_LEASES.remove(identity);
            }
            throw e;
        }
    }

    synchronized Closeable admit(final String name) throws IOException {
        requireOpen();
        final long host;
        try {
            host = Long.parseLong(name);
        } catch (final NumberFormatException _) {
            return null;
        }
        final var active = sessions.get(host);
        if (active == null) {
            return null;
        }
        if (fences.containsKey(host) || count >= CAPACITY) {
            throw new IOException("Legacy password session admission refused");
        }
        if (active.isEmpty()) {
            persistActive(host);
        }
        final var id = UUID.randomUUID();
        active.add(id);
        count++;
        return () -> release(host, id);
    }

    private synchronized void release(final long host, final UUID id) throws IOException {
        if (sessions.get(host).remove(id)) {
            count--;
            if (sessions.get(host).isEmpty() && !uncertain.contains(host)) {
                if (fences.containsKey(host)) {
                    persistDrained(host);
                }
                Files.delete(activeMarker(host));
                syncDirectory();
            }
        }
    }

    synchronized Snapshot fence(final long host, final UUID cutover) throws IOException {
        requireOpen();
        if (cutover == null || !sessions.containsKey(host) || uncertain.contains(host)
                || fences.containsKey(host) && !cutover.equals(fences.get(host))) {
            throw new IOException("Legacy session cutover refused");
        }
        // Keep admission closed even if persisting the marker fails.
        fences.put(host, cutover);
        final var path = marker(host);
        if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            if (!cutover.equals(readMarker(path))) {
                throw new IOException("Legacy session cutover changed");
            }
        } else {
            try (final var file = FileChannel.open(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE,
                    LinkOption.NOFOLLOW_LINKS)) {
                final var bytes = ByteBuffer.wrap(cutover.toString().getBytes(StandardCharsets.US_ASCII));
                while (bytes.hasRemaining()) {
                    file.write(bytes);
                }
                file.force(true);
            }
        }
        try (final var persisted = FileChannel.open(path, StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS)) {
            persisted.force(true);
        }
        syncDirectory();
        if (sessions.get(host).isEmpty()) {
            persistDrained(host);
        }
        return snapshot(host);
    }

    synchronized Snapshot snapshot(final long host) throws IOException {
        requireOpen();
        if (!sessions.containsKey(host) || uncertain.contains(host)) {
            throw new IOException("Host has no confirmed predecessor session state");
        }
        return new Snapshot(incarnations.get(host), fences.get(host), sessions.get(host).size());
    }

    synchronized void close() throws IOException {
        if (closed) {
            return;
        }
        if (count != 0) {
            throw new IOException("Cannot close legacy session tracking with active sessions");
        }
        closed = true;
        leaseLock.release();
        lease.close();
        synchronized (OPEN_LEASES) {
            OPEN_LEASES.remove(leaseIdentity);
        }
    }

    private void requireOpen() throws IOException {
        if (closed) {
            throw new IOException("Legacy session tracking is closed");
        }
    }

    private Path marker(final long host) {
        return directory.resolve(host + ".fence");
    }

    private Path drainedMarker(final long host) {
        return directory.resolve(host + ".drained");
    }

    private Path activeMarker(final long host) {
        return directory.resolve(host + ".active");
    }

    private void persistActive(final long host) throws IOException {
        final var path = activeMarker(host);
        if (Files.exists(path, LinkOption.NOFOLLOW_LINKS) && !Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("Invalid legacy session admission evidence");
        }
        try (final var file = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                LinkOption.NOFOLLOW_LINKS)) {
            file.force(true);
        }
        syncDirectory();
    }

    private void persistDrained(final long host) throws IOException {
        final var path = drainedMarker(host);
        final var cutover = fences.get(host);
        final var incarnation = incarnations.get(host);
        if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            if (!incarnation.equals(readDrainedMarker(path, cutover))) {
                throw new IOException("Legacy session drain evidence changed");
            }
        } else {
            try (final var file = FileChannel.open(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE,
                    LinkOption.NOFOLLOW_LINKS)) {
                final var bytes = ByteBuffer.wrap((cutover + "\n" + incarnation).getBytes(StandardCharsets.US_ASCII));
                while (bytes.hasRemaining()) {
                    file.write(bytes);
                }
                file.force(true);
            }
        }
        try (final var persisted = FileChannel.open(path, StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS)) {
            persisted.force(true);
        }
        syncDirectory();
    }

    private void syncDirectory() throws IOException {
        try (final var parent = FileChannel.open(directory, StandardOpenOption.READ)) {
            parent.force(true);
        }
    }

    private static UUID readDrainedMarker(final Path path, final UUID cutover) throws IOException {
        if (cutover == null) {
            throw new IOException("Legacy session drain evidence has no fence");
        }
        try (final var file = FileChannel.open(path, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS)) {
            if (file.size() != 73) {
                throw new IOException("Invalid legacy session drain evidence");
            }
            final var bytes = ByteBuffer.allocate(73);
            while (bytes.hasRemaining()) {
                if (file.read(bytes) < 0) {
                    throw new IOException("Incomplete legacy session drain evidence");
                }
            }
            final var value = new String(bytes.array(), StandardCharsets.US_ASCII);
            final var incarnation = UUID.fromString(value.substring(37));
            if (!value.equals(cutover + "\n" + incarnation)) {
                throw new IOException("Invalid legacy session drain evidence");
            }
            return incarnation;
        } catch (final IllegalArgumentException _) {
            throw new IOException("Invalid legacy session drain evidence");
        }
    }

    private static UUID readMarker(final Path path) throws IOException {
        try (final var file = FileChannel.open(path, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS)) {
            if (file.size() != 36) {
                throw new IOException("Invalid legacy session fence");
            }
            final var bytes = ByteBuffer.allocate(36);
            while (bytes.hasRemaining()) {
                if (file.read(bytes) < 0) {
                    throw new IOException("Incomplete legacy session fence");
                }
            }
            final var value = new String(bytes.array(), StandardCharsets.US_ASCII);
            final var id = UUID.fromString(value);
            if (!id.toString().equals(value)) {
                throw new IOException("Invalid legacy session fence");
            }
            return id;
        } catch (final IllegalArgumentException _) {
            throw new IOException("Invalid legacy session fence");
        }
    }
}
