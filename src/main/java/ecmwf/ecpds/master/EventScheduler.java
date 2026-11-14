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

import static ecmwf.common.ectrans.ECtransGroups.Module.DESTINATION_MQTT;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_MQTT_CLIENT_ID;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_MQTT_CONTENT_TYPE;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_MQTT_EXPIRY_INTERVAL;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_MQTT_INLINE_MAX_BYTES;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_MQTT_PAYLOAD;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_MQTT_QOS;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_MQTT_RETAIN;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_MQTT_TOPIC;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.timer.Timer;
import javax.script.ScriptException;
import org.graalvm.polyglot.Value;
import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.Destination;
import ecmwf.common.database.Publication;
import ecmwf.common.database.TransferHistory;
import ecmwf.common.ecaccess.MBeanScheduler;
import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.mbean.MBeanManager;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.ScriptManager;
import ecmwf.common.technical.ThreadService.ConfigurableRunnable;
import ecmwf.common.text.Format;
import ecmwf.common.text.Options;
import ecmwf.ecpds.master.transfer.DestinationOption;
import ecmwf.ecpds.master.transfer.TransferScheduler;

/**
 * The Class EventScheduler.
 */
public final class EventScheduler extends MBeanScheduler {
    private final MasterServer _master;

    /** The _time out event thread. */
    private long _timeOutEventThread = Cnf.durationAt("Scheduler", "timeOutEventThread", -1);

    /** The _max event threads. */
    private int _maxEventThreads = Cnf.at("Scheduler", "maxEventThreads", 200);

    /** The _event threads. */
    private final Map<Long, EventThread> _eventThreads = new ConcurrentHashMap<>();

    /** The _to remove. */
    private final List<Long> _toRemove = Collections.synchronizedList(new ArrayList<>());

    /** The _current key. */
    private Long _currentKey = null;

    /**
     * Instantiates a new event scheduler.
     *
     * @param name
     *            the name
     */
    public EventScheduler(final MasterServer _master, final String name) {
        // Bind the Master before superclass construction and field initialization.
        this._master = _master;
        super(name);
        setDelay(Cnf.durationAt("Scheduler", "eventScheduler", 5 * Timer.ONE_SECOND));
        setJammedTimeout(Cnf.durationAt("Scheduler", "eventSchedulerJammedTimeout", 10 * Timer.ONE_MINUTE));
        setTimeRanges(Cnf.listOfTimeRangesAt("Scheduler", "eventSchedulerTimeRanges"));
    }

    /**
     * Gets the event threads count.
     *
     * @return the event threads count
     */
    public int getEventThreadsCount() {
        return _eventThreads.size() - _toRemove.size();
    }

    /**
     * Gets the activity.
     *
     * @return the activity
     */
    @Override
    public String getActivity() {
        return "Publication=" + (_currentKey == null ? "[none]" : _currentKey) + ",eventThreads="
                + _eventThreads.size();
    }

    /**
     * Gets the key.
     *
     * @param publication
     *            the publication
     *
     * @return the string
     */
    private long _getKey(final Publication publication) {
        return publication.getId();
    }

    /**
     * Gets the thread list.
     *
     * @return the thread list
     */
    public String getThreadList() {
        final var status = new StringBuilder();
        final var currentTime = System.currentTimeMillis();
        for (final EventThread thread : _eventThreads.values()) {
            final Publication publication;
            final long key;
            if (thread != null && !_toRemove.contains(key = _getKey(publication = thread.publication))) {
                status.append(("Publication " + key + " for DataTransfer " + publication.getDataTransferId() + " ("
                        + Format.formatDuration(thread.time, currentTime) + ")").replace(' ', '_').replace('-', '_'))
                        .append(" ");
            }
        }
        return status.toString().trim();
    }

    /**
     * Gets the MBean info.
     *
     * @return the MBean info
     */
    @Override
    public MBeanInfo getMBeanInfo() {
        return MBeanManager.addMBeanInfo(super.getMBeanInfo(),
                "The EventScheduler is used to manage the event threads.",
                new MBeanAttributeInfo[] {
                        new MBeanAttributeInfo("ThreadCount", "java.lang.Integer",
                                "ThreadCount: number of event thread(s) currently running.", true, false, false),
                        new MBeanAttributeInfo("MaximumEventThreads", "java.lang.Integer",
                                "MaximumEventThreads: maximum number of parallel events.", true, true, false),
                        new MBeanAttributeInfo("EventThreadsTimeout", "java.lang.Long",
                                "EventThreadsTimeout: milliseconds an event thread may run before being interrupted (-1 disables this check).",
                                true, true, false),
                        new MBeanAttributeInfo("ThreadList", "java.lang.String",
                                "ThreadList: active event thread(s) shown as publication id and elapsed running time.",
                                true, false, false) },
                new MBeanOperationInfo[] { new MBeanOperationInfo("reloadEventScript",
                        "reloadEventScript(): reload event script content", null, "void", MBeanOperationInfo.ACTION) });
    }

    /**
     * Gets the attribute.
     *
     * @param attributeName
     *            the attribute name
     *
     * @return the attribute
     *
     * @throws AttributeNotFoundException
     *             the attribute not found exception
     * @throws MBeanException
     *             the MBean exception
     */
    @Override
    public Object getAttribute(final String attributeName) throws AttributeNotFoundException, MBeanException {
        try {
            if ("ThreadCount".equals(attributeName)) {
                return _eventThreads.size();
            }
            if ("ThreadList".equals(attributeName)) {
                return getThreadList();
            }
            if ("MaximumEventThreads".equals(attributeName)) {
                return _maxEventThreads;
            }
            if ("EventThreadsTimeout".equals(attributeName)) {
                return _timeOutEventThread;
            }
        } catch (final Exception e) {
            MasterServer._log.warn("Getting an MBean attribute", e);
            throw new MBeanException(e);
        }
        return super.getAttribute(attributeName);
    }

    /**
     * Sets the attribute.
     *
     * @param name
     *            the name
     * @param value
     *            the value
     *
     * @return true, if successful
     *
     * @throws InvalidAttributeValueException
     *             the invalid attribute value exception
     * @throws MBeanException
     *             the MBean exception
     */
    @Override
    public boolean setAttribute(final String name, final Object value)
            throws InvalidAttributeValueException, MBeanException {
        if ("MaximumEventThreads".equals(name)) {
            _maxEventThreads = (Integer) value;
            return true;
        }
        if ("EventThreadsTimeout".equals(name)) {
            _timeOutEventThread = (Long) value;
            return true;
        }
        return super.setAttribute(name, value);
    }

    /**
     * Invoke.
     *
     * @param operationName
     *            the operation name
     * @param params
     *            the params
     * @param signature
     *            the signature
     *
     * @return the object
     *
     * @throws NoSuchMethodException
     *             the no such method exception
     * @throws MBeanException
     *             the MBean exception
     */
    @Override
    public Object invoke(final String operationName, final Object[] params, final String[] signature)
            throws NoSuchMethodException, MBeanException {
        try {
            if ("reloadEventScript".equals(operationName) && signature.length == 0) {
                _master.eventScriptContent.replace(0, _master.eventScriptContent.length(),
                        Cnf.fileContentAt("Scheduler", "eventScriptFile", ""));
                return true;
            }
        } catch (final Exception e) {
            MasterServer._log.warn("Invoking the {} MBean method", operationName, e);
            throw new MBeanException(e);
        }
        throw new NoSuchMethodException(operationName);
    }

    /**
     * Next step.
     *
     * @return the int
     */
    @Override
    public int nextStep() {
        final var start = System.currentTimeMillis();
        var processed = 0;
        try (var it = _master.getECpdsBase().getPublicationIterator(2 * _maxEventThreads)) {
            while (isRunning() && it.hasNext()) {
                try {
                    final var publication = it.next();
                    if (publication.getDone()) {
                        MasterServer._log.warn("Publication-{} already processed", publication.getId());
                        continue;
                    }
                    if (getEventThreadsCount() >= _maxEventThreads) {
                        break;
                    }
                    _currentKey = _getKey(publication);
                    synchronized (_eventThreads) {
                        if (!_eventThreads.containsKey(_currentKey)) {
                            final var thread = new EventThread(publication);
                            thread.setThreadNameAndCookie(null, null, "Publication-" + _currentKey, null);
                            thread.execute(true);
                            _eventThreads.put(_currentKey, thread);
                            processed++;
                        }
                    }
                } catch (final Throwable t) {
                    MasterServer._log.warn("nextStep", t);
                }
            }
            synchronized (_toRemove) {
                for (final long key : _toRemove) {
                    _eventThreads.remove(key);
                }
                _toRemove.clear();
            }
            // Check the expired EventThreads
            if (_timeOutEventThread > 0) {
                final var currentTime = System.currentTimeMillis();
                for (final EventThread thread : _eventThreads.values()) {
                    if (thread != null && System.currentTimeMillis() - thread.time > _timeOutEventThread) {
                        if (MasterServer._log.isWarnEnabled()) {
                            MasterServer._log.warn("EventThread Publication-{} expired({})",
                                    _getKey(thread.publication), Format.formatDuration(thread.time, currentTime));
                        }
                        thread.interrupt();
                    }
                }
            }
            final var duration = System.currentTimeMillis() - start;
            if (MasterServer._log.isDebugEnabled() && duration > 60000) {
                MasterServer._log.debug("Step completed in {}: {} Publication(s) processed for notification",
                        Format.formatDuration(duration), processed);
            }
        } catch (final Throwable t) {
            MasterServer._log.warn("Error in scheduler", t);
        }
        // If there are still files to be processed then there is no reason
        // to wait!
        return processed == 0 ? NEXT_STEP_DELAY : NEXT_STEP_CONTINUE;
    }

    /**
     * The Class EventThread.
     */
    private final class EventThread extends ConfigurableRunnable {

        /** The time. */
        private final long time = System.currentTimeMillis();

        /** The publication. */
        private Publication publication = null;

        /**
         * Instantiates a new event thread.
         *
         * @param publication
         *            the publication
         */
        private EventThread(final Publication publication) {
            this.publication = publication;
        }

        /**
         * Configurable run.
         */
        @Override
        public void configurableRun() {
            final var key = _getKey(publication);
            final var mqtt = publication.getOptions().endsWith(MasterServer.MQTT_TOKEN);
            try {
                publication.setProcessedTime(new Timestamp(System.currentTimeMillis()));
                final var selectedConnectOptions = publication.getOptions();
                final var dataTransferId = publication.getDataTransferId();
                MasterServer._events.info("Starting processing for Publication-{} (DataTransfer-{}): {}",
                        publication.getId(), dataTransferId, selectedConnectOptions);
                final var transfer = _master.getECpdsBase().getDataTransferObject(dataTransferId);
                if (mqtt) {
                    _master.getMQTTActivity().queued(publication.getId(), dataTransferId,
                            transfer == null ? "" : transfer.getDestination().getName(),
                            transfer == null ? "" : transfer.getTarget());
                    _master.getMQTTActivity().processing(publication.getId());
                }
                final long remainingMilliseconds;
                try {
                    if (transfer != null && (remainingMilliseconds = transfer.getExpiryTime().getTime()
                            - System.currentTimeMillis()) > 0) { // We found it and it is not expired!
                        if (selectedConnectOptions.endsWith(MasterServer.MQTT_TOKEN)) { // This is an MQTT request
                            final var destination = transfer.getDestination();
                            final var setup = DESTINATION_MQTT.getECtransSetup(replace(
                                    selectedConnectOptions.substring(0,
                                            selectedConnectOptions.length() - MasterServer.MQTT_TOKEN.length()),
                                    destination.getData()));
                            final var event = new java.util.LinkedHashMap<String, Object>(Map.of("filename",
                                    transfer.getTarget(), "destination", transfer.getDestinationName(), "size",
                                    transfer.getDataFile().getSize(), "id",
                                    new java.util.UUID(transfer.getDataFileId() * 1000, transfer.getId() * 1000)
                                            .toString(),
                                    "dataFileId", Long.toString(transfer.getDataFileId()), "transferId",
                                    Long.toString(transfer.getId()), "publishedAt", java.time.Instant.now().toString(),
                                    "expiresAt", transfer.getExpiryTime().toInstant().toString()));
                            final var content = MQTTInlineContent.readBase64(transfer.getDataFile().getSize(),
                                    setup.getInteger(DESTINATION_MQTT_INLINE_MAX_BYTES), () -> {
                                        final var proxy = TransferScheduler.get(transfer, 0, -1);
                                        final var socketConfig = proxy.getSocketConfig();
                                        socketConfig.setConnectTimeOut(5000);
                                        socketConfig.setSoTimeOut(5000);
                                        return proxy.getDataInputStream();
                                    });
                            if (content != null) {
                                event.put("contentBase64", content);
                            }
                            final var eventJson = MasterServer._JSON_MAPPER.writeValueAsString(event);
                            final MasterServer.MQTTMessage message = ScriptManager.exec(setup.getScriptLanguage(),
                                    Map.of("mqttEventJson", eventJson), setup.getScriptContent(), value -> {
                                        final var topic = getMQTTTopic(destination, setup, transfer, value);
                                        if (topic != null && !topic.isBlank()) {
                                            final var payload = setup.getString(DESTINATION_MQTT_PAYLOAD, value);
                                            if (payload != null && !payload.isBlank()) {
                                                MasterServer._log.debug("Payload detected for DataTransfer-{}",
                                                        dataTransferId);
                                                return new MasterServer.MQTTMessage(topic,
                                                        setup.getInteger(DESTINATION_MQTT_QOS, value),
                                                        setup.getOptionalDuration(DESTINATION_MQTT_EXPIRY_INTERVAL,
                                                                value).orElse(Duration.ofMillis(remainingMilliseconds))
                                                                .toMillis(),
                                                        setup.getString(DESTINATION_MQTT_CONTENT_TYPE, value),
                                                        setup.getString(DESTINATION_MQTT_CLIENT_ID, value), payload,
                                                        setup.getBoolean(DESTINATION_MQTT_RETAIN, value));
                                            } else {
                                                _master.getMQTTActivity().finish(publication.getId(), "SKIPPED",
                                                        "Empty MQTT payload");
                                                MasterServer._log.warn(
                                                        "No payload for DataTransfer-{}; ignoring & publication set to done -> {}",
                                                        dataTransferId, value);
                                            }
                                        } else {
                                            _master.getMQTTActivity().finish(publication.getId(), "SKIPPED",
                                                    "Empty MQTT topic");
                                            MasterServer._log.warn(
                                                    "No topic for DataTransfer-{}; ignoring & publication set to done",
                                                    dataTransferId);
                                        }
                                        return null;
                                    });
                            // Broker completion is outside the script deadline: a slow broker must not trigger
                            // a script retry and duplicate an otherwise successful publication.
                            if (message != null) {
                                final var notificationSummary = _master.publishToMQTTBrokerConfirmed(transfer,
                                        publication.getId(), message.topic(), message.qos(), message.expiryInterval(),
                                        message.contentType(), message.clientId(), message.payload(), message.retain());
                                // Bypass completion hooks/accounting; a history failure must not retry the publish.
                                final var history = new TransferHistory();
                                history.setDataTransfer(transfer);
                                history.setDataTransferId(transfer.getId());
                                history.setDestinationName(transfer.getDestinationName());
                                history.setStatusCode(transfer.getStatusCode());
                                history.setTime(new Timestamp(System.currentTimeMillis()));
                                history.setComment(notificationSummary);
                                history.setSent(transfer.getSent());
                                history.setUserStatus(transfer.getUserStatus());
                                try {
                                    _master.getDataBase().insert(history, true);
                                } catch (final Exception e) {
                                    MasterServer._log.error("MQTT notification history NOT recorded for Publication-{}",
                                            publication.getId(), e);
                                }
                            }
                        } else if (!_master.eventScriptContent.isEmpty()) { // This is an event.js request (e.g. Aviso)
                            ScriptManager.exec(ScriptManager.JS,
                                    replace(selectedConnectOptions, _master.eventScriptContent.toString()));
                        }
                    } else {
                        if (mqtt) {
                            _master.getMQTTActivity().finish(publication.getId(), "SKIPPED",
                                    "Transfer is missing or expired");
                        }
                        MasterServer._log.warn(
                                "DataTransfer-{} not found or expired; ignoring & publication set to done",
                                dataTransferId);
                    }
                    publication.setDone(true);
                } catch (final ScriptException e) {
                    if (mqtt) {
                        _master.getMQTTActivity().finish(publication.getId(), "FAILED",
                                "MQTT payload script failed; see Master event log");
                    }
                    MasterServer._events.error("Event invocation failed ({})", e.getMessage(), e);
                } finally {
                    _master.getDataBase().tryUpdate(publication);
                }
            } catch (final Throwable t) {
                if (mqtt) {
                    _master.getMQTTActivity().interrupted(publication.getId());
                }
                MasterServer._events.warn("Running EventThread {}", key, t);
            } finally {
                _toRemove.add(key);
                MasterServer._events.info("Publication-{}: {}", publication.getId(), publication.getDone());
            }
        }

        /**
         * Build the MQTT topic. It is either defined in the destination setup, or by default it is using the
         * destination name. If the topic ends with a '/' (the default when no topic is specified), the target name is
         * appended.
         *
         * @param destination
         *            the destination
         * @param setup
         *            the setup
         * @param transfer
         *            the transfer
         * @param value
         *            the value
         *
         * @return the MQTT topic
         */
        private static String getMQTTTopic(final Destination destination, final ECtransSetup setup,
                final DataTransfer transfer, final Value value) {
            final var topic = setup.getOptionalString(DESTINATION_MQTT_TOPIC, value)
                    .orElse(destination.getName() + "/");
            return (topic.endsWith("/") ? topic + getTarget(destination, transfer) : topic).replaceAll("/+", "/");
        }

        /**
         * Gets the target.
         *
         * @param destination
         *            the destination
         * @param transfer
         *            the transfer
         *
         * @return the target
         */
        private static String getTarget(final Destination destination, final DataTransfer transfer) {
            final var target = transfer.getTarget();
            if (destination.getGroupByDate()) {
                // We want to have the target in the form "date/filename"
                return DestinationOption.formatDate(destination, transfer.getDataFile().getTimeBase().getTime()) + "/"
                        + new File(target).getName();
            }
            // We want the target including all sub-directories if any
            return target;
        }

        /**
         * Replace.
         *
         * @param options
         *            the options
         * @param body
         *            the body
         *
         * @return the string
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        private static String replace(final String options, final String body) throws IOException {
            final var sb = new Options(options, ";\n").replace(new StringBuilder(body));
            int index;
            while ((index = sb.indexOf("$metadata[")) != -1) {
                final var last = sb.indexOf("]", index + 10);
                if (last == -1) {
                    throw new IOException("Malformed metadata in notification configuration: \"" + sb + "\"");
                }
                if (MasterServer._events.isWarnEnabled()) {
                    MasterServer._events.warn("Metadata \"{}\" not found", sb.substring(index, last + 1));
                }
                sb.delete(index, last + 1);
            }
            return sb.toString();
        }
    }
}
