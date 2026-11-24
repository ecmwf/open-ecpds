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
package ecmwf.ecpds.master.plugin.http.controller.transfer.destination;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import ecmwf.common.database.ECpdsBase;
import ecmwf.common.technical.ExecutorRepository;
import ecmwf.ecpds.master.plugin.http.home.transfer.DataTransferHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.Host;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.web.model.users.User;

final class DestinationAccess {
    private static final String BASE = "/do/transfer/destination/";
    private static final int BULK_PARALLELISM = 50;

    private DestinationAccess() {
    }

    static boolean canRead(final User user, final String destination) {
        if (user == null || destination == null || destination.isBlank() || destination.indexOf('/') >= 0
                || destination.indexOf('\\') >= 0 || destination.indexOf('?') >= 0 || destination.indexOf('#') >= 0) {
            return false;
        }
        try {
            return user.hasAccess(BASE + destination);
        } catch (final Exception e) {
            return false;
        }
    }

    static DataTransfer requireTransfer(final String id, final String destination) throws TransferException {
        if (destination == null || !destination.equals(DataTransferHome.findDestinationByPrimaryKeyUncached(id))) {
            throw new TransferException("Selected data transfer does not belong to destination '" + destination + "'");
        }
        final var transfer = DataTransferHome.findByPrimaryKey(id);
        requireTransfer(transfer, destination);
        return transfer;
    }

    private static DataTransfer requireTransfer(final String id, final String destination,
            final Map<String, String> current) throws TransferException {
        requireDestination(current.get(id), destination);
        final var transfer = DataTransferHome.findByPrimaryKey(id);
        requireTransfer(transfer, destination);
        return transfer;
    }

    private static void requireDestination(final String current, final String destination) throws TransferException {
        if (destination == null || !destination.equals(current)) {
            throw new TransferException("Selected data transfer does not belong to destination '" + destination + "'");
        }
    }

    static void validateTransfers(final Collection<String> ids, final String destination,
            final List<Exception> exceptions) {
        final Map<String, String> current;
        try {
            current = DataTransferHome.findDestinationsByPrimaryKeysUncached(ids);
        } catch (final TransferException e) {
            exceptions.add(e);
            return;
        }
        for (final var id : ids) {
            try {
                requireDestination(current.get(id), destination);
            } catch (final TransferException e) {
                exceptions.add(e);
            }
        }
    }

    static Map<String, DataTransfer> loadTransfers(final Collection<String> ids, final String destination)
            throws TransferException {
        final var current = DataTransferHome.findDestinationsByPrimaryKeysUncached(ids);
        final Map<String, DataTransfer> transfers = new LinkedHashMap<>();
        for (final var id : ids) {
            try {
                transfers.put(id, requireTransfer(id, destination, current));
            } catch (final TransferException _) {
                // Missing, foreign or inconsistent cached transfers are excluded.
            }
        }
        return transfers;
    }

    @FunctionalInterface
    interface TransferOperation {
        void execute(DataTransfer transfer) throws Exception;
    }

    static void executeTransfers(final Collection<String> ids, final String destination,
            final List<Exception> exceptions, final AtomicLong processedCount, final TransferOperation operation) {
        final var snapshot = new ArrayList<>(ids);
        final var batchSize = Math.max(1,
                Math.min(ECpdsBase.TRANSFER_DESTINATION_BATCH_SIZE, snapshot.size() / BULK_PARALLELISM));
        final List<List<String>> batches = new ArrayList<>();
        for (var from = 0; from < snapshot.size(); from += batchSize) {
            batches.add(snapshot.subList(from, Math.min(from + batchSize, snapshot.size())));
        }
        new ExecutorRepository<>(100, BULK_PARALLELISM, exceptions, new AtomicLong(), batches) {
            @Override
            public void exec(final List<String> batch) throws Exception {
                // Refresh inside the worker, not when queued or during the initial preflight.
                final var current = DataTransferHome.findDestinationsByPrimaryKeysUncached(batch);
                for (final var id : batch) {
                    try {
                        operation.execute(requireTransfer(id, destination, current));
                        processedCount.incrementAndGet();
                    } catch (final Exception e) {
                        exceptions.add(e);
                    }
                }
            }
        };
    }

    static void requireTransfer(final DataTransfer transfer, final String destination) throws TransferException {
        if (transfer == null || destination == null || !destination.equals(transfer.getDestinationName())) {
            throw new TransferException("Selected data transfer does not belong to destination '" + destination + "'");
        }
    }

    static void requireHost(final Destination destination, final Host host) throws TransferException {
        for (final var pair : destination.getHostsAndPriorities()) {
            if (pair.getName() instanceof Host associated && associated.getName().equals(host.getName())) {
                return;
            }
        }
        throw new TransferException(
                "Host '" + host.getName() + "' does not belong to destination '" + destination.getName() + "'");
    }
}
