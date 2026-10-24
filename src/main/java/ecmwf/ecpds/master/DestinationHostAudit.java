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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import ecmwf.common.database.Association;
import ecmwf.common.database.ChangeLog;
import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.DataBaseObject;
import ecmwf.common.database.Destination;
import ecmwf.common.database.ECpdsBase;
import ecmwf.common.database.Host;

/** Credential-free snapshots for Destination Host history, using the existing CHANGE_LOG table. */
final class DestinationHostAudit {
    record State(String destination, String host, String nickname, String address, boolean active, int priority) {
        static State of(final Association association, final Host host) {
            return new State(association.getDestinationName(), host.getName(), host.getNickname(), host.getHost(),
                    host.getActive(), association.getPriority());
        }
    }

    private final String operation;
    private final List<State> before;
    private final State proposed;

    DestinationHostAudit(final String operation, final List<State> before, final State proposed) {
        this.operation = operation;
        this.before = List.copyOf(before);
        this.proposed = proposed;
    }

    static DestinationHostAudit capture(final ECpdsBase database, final String operation, final DataBaseObject argument)
            throws DataBaseException {
        if (argument instanceof final Association association
                && List.of("insert", "update", "remove").contains(operation)) {
            final var host = database.getHost(association.getHostName());
            final var proposed = State.of(association, host);
            final var before = "insert".equals(operation) ? List.<State> of() : List.of(State
                    .of(database.getAssociation(association.getDestinationName(), association.getHostName()), host));
            return new DestinationHostAudit(operation, before, proposed);
        }
        if (argument instanceof final Host host && List.of("update", "remove").contains(operation)) {
            return new DestinationHostAudit(operation, allocations(database, host), null);
        }
        return new DestinationHostAudit(operation, List.of(), null);
    }

    static List<State> allocations(final ECpdsBase database, final Host host) throws DataBaseException {
        final List<State> states = new ArrayList<>();
        final var original = database.getHost(host.getName());
        for (final Destination destination : database.getDestinationsByHostName(host.getName())) {
            states.add(State.of(database.getAssociation(destination.getName(), host.getName()), original));
        }
        return states;
    }

    List<ChangeLog> complete(final DataBaseObject target, final String user, final boolean successful) {
        if (!successful) {
            return List.of();
        }
        final List<ChangeLog> changes = new ArrayList<>();
        if (proposed != null) {
            final var original = before.isEmpty() ? proposed : before.getFirst();
            switch (operation) {
            case "insert" -> changes.add(change(original, proposed, "Host allocated", false, true, true, true, user));
            case "remove" -> changes
                    .add(change(original, original, "Host removed from destination", true, false, true, true, user));
            case "update" -> {
                if (target instanceof final Association association
                        && original.priority() != association.getPriority()) {
                    final var updated = new State(original.destination(), original.host(), original.nickname(),
                            original.address(), original.active(), association.getPriority());
                    changes.add(change(original, updated, "Host priority changed", true, true, true, true, user));
                }
            }
            default -> {
            }
            }
        } else {
            for (final State state : before) {
                if ("remove".equals(operation)) {
                    changes.add(change(state, state, "Host deleted (global)", true, false, true, false, user));
                } else if (target instanceof final Host host && state.active() != host.getActive()) {
                    final var updated = new State(state.destination(), state.host(), state.nickname(), state.address(),
                            host.getActive(), state.priority());
                    changes.add(change(state, updated,
                            host.getActive() ? "Host enabled (global)" : "Host disabled (global)", true, true, true,
                            true, user));
                }
            }
        }
        return changes;
    }

    static ChangeLog allocation(final State state, final String user) {
        return change(state, state, "Host allocated", false, true, true, true, user);
    }

    private static ChangeLog change(final State before, final State after, final String event,
            final boolean wasAllocated, final boolean isAllocated, final boolean existed, final boolean exists,
            final String user) {
        final var change = new ChangeLog();
        change.setKeyName("DES_NAME");
        change.setKeyValue(before.destination());
        change.setWebUserId(user);
        change.setTime(new Timestamp(System.currentTimeMillis()));
        change.setOldObject(snapshot(before, event, wasAllocated, existed));
        change.setNewObject(snapshot(after, event, isAllocated, exists));
        return change;
    }

    private static String snapshot(final State state, final String event, final boolean allocated,
            final boolean exists) {
        return "[AUDIT_HOST_EVENT]" + event + "\n[HOS_NAME]" + singleLine(state.host()) + "\n[HOS_NICKNAME]"
                + singleLine(state.nickname()) + "\n[HOS_HOST]" + singleLine(state.address()) + "\n[ASO_ALLOCATED]"
                + allocated + "\n[HOS_EXISTS]" + exists + "\n[HOS_ACTIVE]" + state.active() + "\n[ASO_PRIORITY]"
                + (allocated ? state.priority() : "-") + "\n";
    }

    private static String singleLine(final String value) {
        return value == null ? "" : value.replace('\r', ' ').replace('\n', ' ');
    }
}
