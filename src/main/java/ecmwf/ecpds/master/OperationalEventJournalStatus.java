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

/** Read-only journal health; nullable queue values mean that no database measurement is available. */
public record OperationalEventJournalStatus(boolean enabled, String owner, boolean stopped, boolean persistenceDegraded,
        long persistenceFailures, long lastPersistenceFailureAt, long lastPersistedAt, boolean deliveryDegraded,
        String deliveryFailureStage, long lastDeliveryFailureAt, long lastExportAt, long inFlightCount,
        Long pendingCount, Long oldestAdmissionAt, long observedAt, String queueError) implements Serializable {

    private static final long serialVersionUID = 1L;

    public static OperationalEventJournalStatus disabled() {
        return new OperationalEventJournalStatus(false, "", false, false, 0, 0, 0, false, "", 0, 0, 0, null, null,
                System.currentTimeMillis(), null);
    }

    public OperationalEventJournalStatus withQueue(final Long count, final Long oldest, final String error) {
        return new OperationalEventJournalStatus(enabled, owner, stopped, persistenceDegraded, persistenceFailures,
                lastPersistenceFailureAt, lastPersistedAt, deliveryDegraded, deliveryFailureStage,
                lastDeliveryFailureAt, lastExportAt, inFlightCount, count, oldest, System.currentTimeMillis(), error);
    }

    public boolean danger() {
        return enabled && (persistenceDegraded || stopped);
    }

    public boolean attention() {
        return enabled && (danger() || deliveryDegraded || persistenceFailures > 0 || queueError != null);
    }

    public String state() {
        return !enabled ? "Disabled" : stopped ? "Stopped" : persistenceDegraded ? "Degraded"
                : queueError != null ? "Statistics unavailable" : deliveryDegraded ? "Retrying" : "Running";
    }
}
