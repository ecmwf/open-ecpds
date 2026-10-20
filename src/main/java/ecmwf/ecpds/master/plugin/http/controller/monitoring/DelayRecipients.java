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

package ecmwf.ecpds.master.plugin.http.controller.monitoring;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.home.monitoring.DestinationProductStatusHome;
import ecmwf.ecpds.master.plugin.http.home.monitoring.ProductStatusHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.model.monitoring.ProductStatus;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Works out who is concerned by a delay: the destinations receiving a (product, cycle) combination, using the same data
 * as the destinations table at the bottom of /do/monitoring, and their contact emails.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 8.2.0
 *
 * @since 2026-10-12
 */
public final class DelayRecipients {

    private DelayRecipients() {
        // Hiding constructor!
    }

    /**
     * The result of a resolution.
     *
     * @param destinations
     *            the concerned destinations, with their contact emails (possibly empty)
     * @param emails
     *            the de-duplicated emails of all the destinations
     */
    public record Result(Map<String, String> destinations, TreeSet<String> emails) {
    }

    /**
     * Gets the available cycles for each product, from the monitoring data.
     *
     * @return the sorted cycles, keyed by product name
     *
     * @throws Exception
     *             if the monitoring data cannot be read
     */
    public static Map<String, TreeSet<String>> cyclesPerProduct() throws Exception {
        final Map<String, TreeSet<String>> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (final var ps : ProductStatusHome.findFromMemory().values()) {
            if (ps.getProduct() != null && ps.getTime() != null && !ps.getTime().isEmpty()) {
                result.computeIfAbsent(ps.getProduct(), _ -> new TreeSet<>()).add(ps.getTime());
            }
        }
        return result;
    }

    /**
     * Finds the nearest schedule time across all known cycles for each product. A negative value means that at least
     * one cycle is already late; cycles without a schedule are ignored.
     *
     * @return shortest minutes before schedule, keyed by product name
     *
     * @throws Exception
     *             if the monitoring data cannot be read
     */
    public static Map<String, Long> minutesBeforeSchedulePerProduct() throws Exception {
        final Map<String, Long> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (final ProductStatus ps : ProductStatusHome.findFromMemory().values()) {
            if (ps.getProduct() == null || ps.getTime() == null || ps.getTime().isEmpty()) {
                continue;
            }
            final var minutes = ps.getMinutesBeforeSchedule();
            if (minutes != Integer.MIN_VALUE) {
                result.merge(ps.getProduct(), minutes, Math::min);
            }
        }
        return result;
    }

    /**
     * Gets the minutes before schedule for each known product and cycle.
     *
     * @return minutes before schedule keyed by "product@cycle"
     *
     * @throws Exception
     *             if the monitoring data cannot be read
     */
    public static Map<String, Long> minutesBeforeSchedulePerCycle() throws Exception {
        final Map<String, Long> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (final ProductStatus ps : ProductStatusHome.findFromMemory().values()) {
            if (ps.getProduct() == null || ps.getTime() == null || ps.getTime().isEmpty()) {
                continue;
            }
            final var minutes = ps.getMinutesBeforeSchedule();
            if (minutes != Integer.MIN_VALUE) {
                result.put(ps.getProduct() + "@" + ps.getTime(), minutes);
            }
        }
        return result;
    }

    /**
     * Gets the generation-status color for each known product and cycle.
     *
     * @return generation status keyed by "product@cycle"
     *
     * @throws Exception
     *             if the monitoring data cannot be read
     */
    public static Map<String, Integer> generationStatusPerCycle() throws Exception {
        final Map<String, Integer> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (final ProductStatus ps : ProductStatusHome.findFromMemory().values()) {
            if (ps.getProduct() != null && ps.getTime() != null && !ps.getTime().isEmpty()) {
                result.put(ps.getProduct() + "@" + ps.getTime(), ps.getGenerationStatus());
            }
        }
        return result;
    }

    /**
     * Gets the generation status of the cycle with the shortest time remaining for each product.
     *
     * @return generation status keyed by product name
     *
     * @throws Exception
     *             if the monitoring data cannot be read
     */
    public static Map<String, Integer> generationStatusPerProduct() throws Exception {
        final Map<String, Integer> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        final Map<String, Long> nearest = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (final ProductStatus ps : ProductStatusHome.findFromMemory().values()) {
            if (ps.getProduct() == null || ps.getTime() == null || ps.getTime().isEmpty()) {
                continue;
            }
            final var minutes = ps.getMinutesBeforeSchedule();
            if (minutes != Integer.MIN_VALUE
                    && (!nearest.containsKey(ps.getProduct()) || minutes < nearest.get(ps.getProduct()))) {
                nearest.put(ps.getProduct(), minutes);
                result.put(ps.getProduct(), ps.getGenerationStatus());
            }
        }
        return result;
    }

    /**
     * Resolves the destinations and emails for a selection of cells.
     *
     * @param cells
     *            the cells, as "product@time" keys
     * @param includeNonMonitored
     *            also include the destinations not showing in the monitor
     *
     * @return the destinations and emails
     *
     * @throws Exception
     *             if the data cannot be read
     */
    public static Result resolve(final Collection<String> cells, final boolean includeNonMonitored) throws Exception {
        final var monitored = new TreeSet<String>();
        for (final var d : DestinationHome.findAllShowingInMonitor()) {
            monitored.add(d.getName());
        }
        final var contacts = MasterManager.getMI().getContacts();
        final var destinations = new TreeMap<String, String>();
        final var emails = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        final var wanted = new TreeSet<>(cells);
        for (final var dps : DestinationProductStatusHome.findFromMemory().values()) {
            if (dps == null || !dps.isPresent() || !wanted.contains(dps.getProduct() + "@" + dps.getTime())) {
                continue;
            }
            final var name = dps.getDestinationName();
            if (!includeNonMonitored && !monitored.contains(name) || destinations.containsKey(name)) {
                continue;
            }
            final var found = contacts == null ? null : contacts.get(name);
            destinations.put(name, found == null ? "" : found);
            if (found != null) {
                for (final var email : found.split("[,;]")) {
                    if (!email.isBlank()) {
                        emails.add(email.trim());
                    }
                }
            }
        }
        return new Result(destinations, emails);
    }
}
