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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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
    private final Map<Long, Set<UUID>> sessions = new HashMap<>();
    private final Map<Long, UUID> fences = new HashMap<>();
    private final UUID incarnation = UUID.randomUUID();
    private final Path directory;
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
        for (final var host : hosts) {
            sessions.put(host, new HashSet<>());
            final var path = marker(host);
            if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                fences.put(host, readMarker(path));
            }
        }
        try (final var entries = Files.list(directory)) {
            if (entries.anyMatch(path -> path.getFileName().toString().endsWith(".fence")
                    && hosts.stream().noneMatch(host -> marker(host).equals(path)))) {
                throw new IOException("A fenced Host was removed from legacy session tracking");
            }
        }
    }

    synchronized Closeable admit(final String name) throws IOException {
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
        final var id = UUID.randomUUID();
        active.add(id);
        count++;
        return () -> release(host, id);
    }

    private synchronized void release(final long host, final UUID id) {
        if (sessions.get(host).remove(id)) {
            count--;
        }
    }

    synchronized Snapshot fence(final long host, final UUID cutover) throws IOException {
        if (cutover == null || !sessions.containsKey(host)
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
        try (final var parent = FileChannel.open(directory, StandardOpenOption.READ)) {
            parent.force(true);
        }
        return snapshot(host);
    }

    synchronized Snapshot snapshot(final long host) throws IOException {
        if (!sessions.containsKey(host)) {
            throw new IOException("Host has no legacy session tracking");
        }
        return new Snapshot(incarnation, fences.get(host), sessions.get(host).size());
    }

    private Path marker(final long host) {
        return directory.resolve(host + ".fence");
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
