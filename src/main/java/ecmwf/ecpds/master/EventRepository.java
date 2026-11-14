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
import static ecmwf.common.text.Util.isEmpty;
import java.util.ArrayList;
import javax.management.timer.Timer;
import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.Publication;
import ecmwf.common.ecaccess.StorageRepository;
import ecmwf.common.ectrans.ECtransOptions;
import ecmwf.common.technical.Cnf;
import ecmwf.ecpds.master.transfer.StatusFactory;
import ecmwf.ecpds.master.transfer.TransferManagement;

/**
 * Handles queued transfer publications and native notification capture.
 */
final class EventRepository extends StorageRepository<EventRepository.QueuedTransferEvent> {
    private final MasterServer _master;

    /**
     * Instantiates a new event repository.
     *
     * @param name
     *            the name
     */
    EventRepository(final MasterServer _master, final String name) {
        // Bind the Master before superclass construction and field initialization.
        this._master = _master;
        super(name, Cnf.at("StorageRepository", "eventSize", 5),
                Cnf.at("StorageRepository", "eventDelay", 30 * Timer.ONE_SECOND));
        setMaxAuthorisedSize(Cnf.at("StorageRepository", "eventMaxAuthorisedSize", 0));
    }

    /**
     * Gets the status.
     *
     * @param transfer
     *            the transfer
     *
     * @return the status
     */
    @Override
    public String getStatus(final EventRepository.QueuedTransferEvent event) {
        return event.transfer.getStatusCode();
    }

    /**
     * Update.
     *
     * @param transfer
     *            the transfer
     *
     * @throws Exception
     *             the exception
     */
    @Override
    public void update(final EventRepository.QueuedTransferEvent queued) throws Exception {
        final var transfer = queued.transfer;
        final var code = transfer.getStatusCode();
        final var selectedConnectOptions = new StringBuilder();
        final var currentConnectOptions = transfer.getConnectOptions();
        final var standby = StatusFactory.HOLD.equals(code);
        if (standby && isEmpty(currentConnectOptions)) {
            // We have to build the connect options as the file was not transmitted and
            // there was no connect options configured!
            selectedConnectOptions.append(TransferManagement.getConnectOptions(transfer, transfer.getTarget(),
                    transfer.getMoverName(), new ArrayList<>()));
        } else {
            // We use the connect options configured during the push!
            selectedConnectOptions.append(currentConnectOptions);
        }
        if (!selectedConnectOptions.isEmpty()) {
            selectedConnectOptions.append(";standby=" + standby);
            // Insert into the Publication table, to be processed by the event scheduler for
            // the global notification system!
            if (!_master.eventScriptContent.isEmpty()) {
                insertPublication(transfer, selectedConnectOptions);
            }
            final var destination = transfer.getDestination();
            if (DESTINATION_MQTT.getECtransSetup(destination.getData())
                    .getBoolean(ECtransOptions.DESTINATION_MQTT_PUBLISH)) {
                // Insert into the Publication table, to be processed by the event scheduler for
                // the MQTT notification system!
                insertPublication(transfer, selectedConnectOptions.append(MasterServer.MQTT_TOKEN));
            }
        } else {
            MasterServer._log.error("Publication cancelled (no options found)");
        }
        if (_master.notificationHistory != null && queued.nativeEvent != null) {
            _master.notificationHistory.capture(queued.nativeEvent);
        }
    }

    /**
     * Insert publication.
     *
     * @param transfer
     *            the transfer
     * @param selectedConnectOptions
     *            the selected connect options
     */
    // Utility method to insert a new publication entry in the database
    private void insertPublication(final DataTransfer transfer, final StringBuilder selectedConnectOptions) {
        final var publication = new Publication();
        publication.setDataTransferId(transfer.getId());
        publication.setOptions(selectedConnectOptions.toString());
        publication.setScheduledTime(transfer.getScheduledTime());
        publication.setProcessedTime(null);
        publication.setDone(false);
        try {
            _master.getDataBase().insert(publication, true);
            if (publication.getOptions().endsWith(MasterServer.MQTT_TOKEN)) {
                _master.getMQTTActivity().queued(publication.getId(), transfer.getId(),
                        transfer.getDestination().getName(), transfer.getTarget());
            }
        } catch (final Throwable t) {
            MasterServer._log.error("Publication NOT recorded: {}", publication, t);
        }
    }

    /**
     * Queued transfer state with its prepared native notification.
     */
    static final class QueuedTransferEvent {
        private final DataTransfer transfer;
        private final NotificationHistory.Event nativeEvent;

        QueuedTransferEvent(final DataTransfer transfer, final NotificationHistory.Event nativeEvent) {
            this.transfer = transfer;
            this.nativeEvent = nativeEvent;
        }

        @Override
        public int hashCode() {
            return transfer.hashCode();
        }

        @Override
        public boolean equals(final Object other) {
            return other instanceof EventRepository.QueuedTransferEvent event && transfer.equals(event.transfer);
        }
    }
}
