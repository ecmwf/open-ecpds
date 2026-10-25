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

import java.io.Serializable;

/** Journal health and runtime controls; nullable queue values mean no database measurement is available. */
public record OperationalEventJournalStatus(boolean enabled, String owner, boolean stopped, boolean persistenceDegraded,
        long persistenceFailures, long lastPersistenceFailureAt, long lastPersistedAt, boolean deliveryDegraded,
        String deliveryFailureStage, long lastDeliveryFailureAt, long lastExportAt, long inFlightCount,
        Long pendingCount, Long oldestAdmissionAt, long observedAt, String queueError, boolean collectionPaused,
        boolean deliveryPaused, long warningPendingCount, long criticalPendingCount, long warningOldestAgeMillis,
        long criticalOldestAgeMillis) implements Serializable {

    private static final long serialVersionUID = 1L;

    public static OperationalEventJournalStatus disabled() {
        return new OperationalEventJournalStatus(false, "", false, false, 0, 0, 0, false, "", 0, 0, 0, null, null,
                System.currentTimeMillis(), null, false, false, 1000, 10000, 300000, 1800000);
    }

    public OperationalEventJournalStatus withQueue(final Long count, final Long oldest, final String error) {
        return new OperationalEventJournalStatus(enabled, owner, stopped, persistenceDegraded, persistenceFailures,
                lastPersistenceFailureAt, lastPersistedAt, deliveryDegraded, deliveryFailureStage,
                lastDeliveryFailureAt, lastExportAt, inFlightCount, count, oldest, System.currentTimeMillis(), error,
                collectionPaused, deliveryPaused, warningPendingCount, criticalPendingCount, warningOldestAgeMillis,
                criticalOldestAgeMillis);
    }

    public boolean danger() {
        return enabled && (persistenceDegraded || stopped || criticalBacklog());
    }

    public boolean attention() {
        return enabled && (danger() || deliveryDegraded || collectionPaused || deliveryPaused || persistenceFailures > 0
                || queueError != null || warningBacklog());
    }

    public boolean criticalBacklog() {
        return pendingCount != null
                && (pendingCount >= criticalPendingCount || oldestAgeMillis() >= criticalOldestAgeMillis);
    }

    public boolean warningBacklog() {
        return pendingCount != null
                && (pendingCount >= warningPendingCount || oldestAgeMillis() >= warningOldestAgeMillis);
    }

    private long oldestAgeMillis() {
        return oldestAdmissionAt == null ? 0 : Math.max(0, observedAt - oldestAdmissionAt);
    }

    public String state() {
        return !enabled ? "Disabled"
                : stopped ? "Stopped" : persistenceDegraded ? "Degraded" : criticalBacklog() ? "Critical backlog"
                        : deliveryPaused || collectionPaused ? "Paused" : queueError != null ? "Statistics unavailable"
                                : deliveryDegraded ? "Retrying" : warningBacklog() ? "Backlog warning" : "Running";
    }
}
