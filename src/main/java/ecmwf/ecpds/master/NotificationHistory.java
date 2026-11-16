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
package ecmwf.ecpds.master;

import java.io.File;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.DataBase;
import ecmwf.common.database.DataFile;
import ecmwf.common.database.DataFileVersion;
import ecmwf.common.database.Destination;
import ecmwf.common.database.NotificationHistoryStore;
import ecmwf.common.database.NotificationOutboxStore;
import ecmwf.common.ecaccess.EccmdException;
import ecmwf.common.ectrans.ECtransGroups;
import ecmwf.common.ectrans.ECtransOptions;
import ecmwf.common.technical.Cnf;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.transfer.DestinationOption;

/** Optional durable history of the existing transfer notification trigger. */
public final class NotificationHistory implements AutoCloseable {
    private static final Logger LOG = LogManager.getLogger(NotificationHistory.class);
    private static final ObjectMapper JSON = new ObjectMapper();
    private final NotificationHistoryStore store;
    private final NotificationOutboxStore outbox;
    private final long retention = Cnf.durationAt("NotificationHistory", "retention", 7L * 24 * 60 * 60 * 1000);
    private final long maxBytes = Cnf.at("NotificationHistory", "maxPayloadBytes", 1024L * 1024 * 1024);
    private final long checkpointRetention = Cnf.durationAt("NotificationHistory", "checkpointRetention",
            30L * 24 * 60 * 60 * 1000);
    private final long maxOutboxBytes = Cnf.at("NotificationHistory", "maxOutboxBytes", 1024L * 1024 * 1024);
    private final String mqttTopicPrefix = Cnf.at("NotificationHistory", "mqttTopicPrefix", "open-ecpds/notifications");
    private final java.util.concurrent.ScheduledExecutorService cleaner;
    private final java.util.concurrent.ScheduledExecutorService publisher;
    private final java.util.Set<String> publishingMovers = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private final ThreadPoolExecutor writer;

    record Event(long transferId, String destination, String target, String payload,
            NotificationOutboxStore.Request outbox) {
    }

    public record Replay(long ceiling, long removedThrough, long removedAt, long scannedThrough, boolean hasMore,
            boolean historyGap, List<NotificationHistoryStore.Entry> entries) implements Serializable {
    }

    NotificationHistory(final DataBase database) {
        this(database, null);
    }

    NotificationHistory(final MasterServer master) {
        this(master.getDataBase(), master::publishNotificationOutbox);
    }

    NotificationHistory(final DataBase database,
            final java.util.function.Predicate<NotificationOutboxStore.Delivery> publish) {
        store = new NotificationHistoryStore(database);
        outbox = new NotificationOutboxStore(database);
        if (retention <= 0 || maxBytes <= 0 || checkpointRetention <= 0 || maxOutboxBytes <= 0
                || mqttTopicPrefix == null || mqttTopicPrefix.isBlank() || mqttTopicPrefix.startsWith("$")
                || mqttTopicPrefix.endsWith("/") || mqttTopicPrefix.contains("+") || mqttTopicPrefix.contains("#")
                || mqttTopicPrefix.indexOf('\0') >= 0 || mqttTopicPrefix.length() > 300) {
            throw new IllegalArgumentException("Notification history limits must be positive");
        }
        writer = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(4096), r -> {
            final var thread = new Thread(r, "NotificationHistoryWriter");
            thread.setDaemon(true);
            return thread;
        });
        cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
            final var thread = new Thread(r, "NotificationHistoryCleanup");
            thread.setDaemon(true);
            return thread;
        });
        cleaner.scheduleWithFixedDelay(() -> {
            try {
                store.prune(retention, maxBytes);
                store.pruneCheckpoints(checkpointRetention);
            } catch (final RuntimeException e) {
                LOG.warn("Notification history cleanup failed", e);
            }
        }, 0, 5, TimeUnit.SECONDS);
        if (publish == null) {
            publisher = null;
        } else {
            publisher = Executors.newScheduledThreadPool(4, r -> {
                final var thread = new Thread(r, "NotificationOutboxPublisher");
                thread.setDaemon(true);
                return thread;
            });
            for (var i = 0; i < 4; i++) {
                publisher.scheduleWithFixedDelay(() -> publishPending(publish), 0, 2, TimeUnit.SECONDS);
            }
        }
    }

    Event event(final DataTransfer transfer) throws Exception {
        if (!isCaptureEnabled(transfer)) {
            return null;
        }
        return event(transfer, UUID.randomUUID().toString());
    }

    static boolean isCaptureEnabled(final DataTransfer transfer) {
        final var destination = transfer.getDestination();
        return transfer.getEvent() && destination != null
                && (isAvisoEnabled(destination)
                        || ECtransGroups.Module.DESTINATION_MQTT.getECtransSetup(destination.getData())
                                .getBoolean(ECtransOptions.DESTINATION_MQTT_CLOUD_EVENTS));
    }

    private static boolean isAvisoEnabled(final Destination destination) {
        return ECtransGroups.Module.DESTINATION_NOTIFICATION.getECtransSetup(destination.getData())
                .getBoolean(ECtransOptions.DESTINATION_NOTIFICATION_AVISO);
    }

    private Event event(final DataTransfer transfer, final String id) throws Exception {
        final var destination = transfer.getDestinationName();
        final var target = transfer.getTarget();
        final var event = JSON.createObjectNode();
        event.put("specversion", "1.0");
        event.put("id", id);
        event.put("source", "urn:open-ecpds:notification");
        event.put("type", "org.openecpds.file.available.v1");
        event.put("time", Instant.now().toString());
        event.put("subject", destination);
        event.put("datacontenttype", "application/json");
        final var data = event.putObject("data");
        data.put("destination", destination);
        data.put("target", target);
        data.put("dataTransferId", transfer.getId());
        final var file = (DataFile) transfer.getDataFile().clone();
        data.put("dataFileId", file.getId());
        data.put("fileInstance", file.getFileInstance() == null ? 0 : file.getFileInstance());
        data.put("etag", DataFileVersion.etag(file));
        data.put("portalPath",
                transfer.getDestination().getGroupByDate()
                        ? DestinationOption.formatDate(transfer.getDestination(), transfer.getScheduledTime().getTime())
                                + "/" + new File(target).getName()
                        : target);
        data.put("size", transfer.getSize());
        data.put("status", transfer.getStatusCode());
        final var payload = JSON.writeValueAsString(event);
        final var options = ECtransGroups.Module.DESTINATION_MQTT.getECtransSetup(transfer.getDestination().getData());
        final var topic = mqttTopicPrefix + "/"
                + java.net.URLEncoder.encode(destination, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
        final var request = options.getBoolean(ECtransOptions.DESTINATION_MQTT_CLOUD_EVENTS)
                ? new NotificationOutboxStore.Request(id, topic, maxOutboxBytes) : null;
        return new Event(transfer.getId(), destination, target, payload, request);
    }

    void capture(final DataTransfer transfer) {
        try {
            capture(event(transfer));
        } catch (final Exception e) {
            LOG.error("Notification history event was not recorded for transfer {}", transfer.getId(), e);
        }
    }

    void capture(final Event event) {
        if (event == null) {
            return;
        }
        try {
            writer.execute(() -> {
                try {
                    store.append(event.destination(), event.target(), event.payload(), retention, maxBytes,
                            event.outbox());
                } catch (final RuntimeException e) {
                    LOG.error("Notification history event was not recorded for transfer {}", event.transferId(), e);
                }
            });
        } catch (final RuntimeException e) {
            LOG.error("Notification history event was not recorded for transfer {}", event.transferId(), e);
        }
    }

    void captureWithTransfer(final DataTransfer transfer, final DataTransfer expected) throws Exception {
        if (expected == null) {
            throw new IllegalArgumentException("Notification transfer snapshot is required");
        }
        if (!isCaptureEnabled(transfer)) {
            throw new IllegalStateException("Native notification capture is disabled for destination");
        }
        final var event = event(transfer, strictEventId(transfer));
        store.appendWithTransfer(transfer, expected, event.payload(), retention, maxBytes, event.outbox());
    }

    boolean captureWithTransfer(final DataTransfer transfer) throws Exception {
        return captureWithTransfer(transfer, strictEventId(transfer));
    }

    boolean captureWithTransfer(final DataTransfer transfer, final String eventId) throws Exception {
        if (!isCaptureEnabled(transfer)) {
            throw new IllegalStateException("Native notification capture is disabled for destination");
        }
        final var event = event(transfer, eventId);
        return store.appendWithTransferFenced(transfer, event.payload(), retention, maxBytes, event.outbox());
    }

    String strictEventId(final DataTransfer transfer) {
        final var finishTime = transfer.getFinishTime();
        final var key = "open-ecpds:" + transfer.getId() + ":" + transfer.notificationRevisionForCapture() + ":"
                + transfer.getStatusCode() + ":" + transfer.getSent() + ":"
                + (finishTime == null ? "" : finishTime.getTime());
        return UUID.nameUUIDFromBytes(key.getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString();
    }

    void validateTransferRevisionSchema() {
        store.validateTransferRevisionSchema();
    }

    void publishPending(final java.util.function.Predicate<NotificationOutboxStore.Delivery> publish) {
        try {
            for (var i = 0; i < 16 && !Thread.currentThread().isInterrupted(); i++) {
                final var delivery = outbox.claim(60000, publishingMovers);
                if (delivery == null) {
                    return;
                }
                if (!publishingMovers.add(delivery.mover())) {
                    outbox.retry(delivery, 5000);
                    continue;
                }
                boolean accepted;
                try {
                    accepted = publish.test(delivery);
                } catch (final RuntimeException e) {
                    LOG.warn("Notification outbox delivery {} to {} failed", delivery.id(), delivery.mover(), e);
                    accepted = false;
                } finally {
                    publishingMovers.remove(delivery.mover());
                }
                if (accepted) {
                    outbox.complete(delivery);
                } else {
                    final var delay = Math.min(300000L, 5000L << Math.min(delivery.attempts() - 1, 6));
                    outbox.retry(delivery, delay);
                }
            }
        } catch (final RuntimeException e) {
            LOG.warn("Notification outbox delivery failed", e);
        }
    }

    Replay replayAviso(final IncomingProfile profile, final String destination, final String target, final long after,
            final Long upper, final Long fromTime, final Long toTime, final int limit) throws Exception {
        final var root = authorizeDestination(profile, destination);
        final var defaultDestination = profile.getECtransSetup().getString(ECtransOptions.USER_PORTAL_DESTINATION);
        final var hasDefaultDestination = defaultDestination != null && !defaultDestination.isBlank();
        final var page = store.replayAviso(destination, after, upper, fromTime, toTime, limit, retention);
        final List<NotificationHistoryStore.Entry> visible = new ArrayList<>();
        for (final var entry : page.entries()) {
            if (target != null && !target.equals(entry.target())) {
                continue;
            }
            try {
                final var relative = Format.normalizePath(entry.target());
                final var path = "[" + profile.getIncomingUser().getId() + "]DATA:"
                        + (hasDefaultDestination ? "" : "/" + root) + relative;
                profile.checkPermission("get", path);
                visible.add(entry);
            } catch (final EccmdException | java.io.FileNotFoundException ignored) {
                // A notification must not reveal a target excluded by the user's path permissions.
            }
        }
        return new Replay(page.ceiling(), page.removedThrough(), page.removedAt(), page.scannedThrough(),
                page.hasMore(), page.historyGap(), List.copyOf(visible));
    }

    long currentAvisoSequence(final IncomingProfile profile, final String destination) throws Exception {
        authorizeDestination(profile, destination);
        return store.currentSequence();
    }

    private static String authorizeDestination(final IncomingProfile profile, final String destination)
            throws Exception {
        final var allowed = profile.getDestinations().stream()
                .filter(d -> d.getActive() && d.getName().equals(destination)).findFirst()
                .orElseThrow(() -> new EccmdException("Permission denied"));
        if (!isAvisoEnabled(allowed)) {
            throw new EccmdException("Aviso is disabled for destination");
        }
        final var root = ECtransGroups.Module.DESTINATION_INCOMING.getECtransSetup(allowed.getData())
                .get(ECtransOptions.DESTINATION_INCOMING_ROOT_DIR, allowed.getName());
        if (root == null || root.isBlank() || root.contains("/") || root.contains("\\") || ".".equals(root)
                || "..".equals(root)) {
            throw new EccmdException("Invalid destination root");
        }
        final var resolved = profile.getDestinations().stream()
                .filter(d -> d.getActive() && root.equals(ECtransGroups.Module.DESTINATION_INCOMING
                        .getECtransSetup(d.getData()).get(ECtransOptions.DESTINATION_INCOMING_ROOT_DIR, d.getName())))
                .findFirst().orElseThrow(() -> new EccmdException("Permission denied"));
        if (!resolved.getName().equals(destination)) {
            throw new EccmdException("Permission denied");
        }
        final var defaultDestination = profile.getECtransSetup().getString(ECtransOptions.USER_PORTAL_DESTINATION);
        if (defaultDestination != null && !defaultDestination.isBlank() && !root.equals(defaultDestination)) {
            throw new EccmdException("Permission denied");
        }
        return root;
    }

    @Override
    public void close() {
        cleaner.shutdownNow();
        if (publisher != null) {
            publisher.shutdownNow();
        }
        writer.shutdown();
        try {
            if (!writer.awaitTermination(5, TimeUnit.SECONDS)) {
                LOG.warn("Notification history writer did not finish before shutdown");
                writer.shutdownNow();
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            writer.shutdownNow();
        }
    }
}
