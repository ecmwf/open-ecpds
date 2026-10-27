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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.home.monitoring.DestinationProductStatusHome;
import ecmwf.ecpds.master.plugin.http.home.monitoring.ProductStatusHome;
import ecmwf.ecpds.master.plugin.http.home.monitoring.ProductStepStatusHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.model.monitoring.GenerationMonitoringStatus;
import ecmwf.ecpds.master.plugin.http.model.monitoring.ProductStatus;
import ecmwf.ecpds.master.plugin.http.model.monitoring.ProductStepStatus;
import ecmwf.ecpds.master.plugin.http.model.transfer.Status;

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

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(DelayRecipients.class);

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
     * Gets the available cycles for each product, from the monitoring data. Products whose generic (all-types) Product
     * Description disables Delay Notification are left out.
     *
     * @return the sorted cycles, keyed by product name
     *
     * @throws Exception
     *             if the monitoring data cannot be read
     */
    public static Map<String, TreeSet<String>> cyclesPerProduct() throws Exception {
        final var excluded = excludedProducts();
        final Map<String, TreeSet<String>> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (final var ps : ProductStatusHome.findFromMemory().values()) {
            if (ps.getProduct() != null && !excluded.contains(ps.getProduct()) && ps.getTime() != null
                    && !ps.getTime().isEmpty()) {
                result.computeIfAbsent(ps.getProduct(), _ -> new TreeSet<>()).add(ps.getTime());
            }
        }
        return result;
    }

    /**
     * Gets each type's worst generation status for each product and cycle, from the present steps shown on the summary
     * page. As with the cycle status, a fully completed type is counted as OK.
     *
     * @return generation statuses keyed by "product@cycle", then by type (sorted)
     *
     * @throws Exception
     *             if the monitoring data cannot be read
     */
    public static Map<String, Map<String, Integer>> typesPerCycle() throws Exception {
        final Map<String, Map<String, Integer>> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        final Map<String, TreeSet<String>> incomplete = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (final ProductStepStatus step : ProductStepStatusHome.findAll()) {
            final var type = step.getType();
            if (step.isPresent() && step.getProduct() != null && step.getTime() != null && !step.getTime().isEmpty()
                    && type != null && !type.isBlank()) {
                final var key = step.getProduct() + "@" + step.getTime();
                result.computeIfAbsent(key, _ -> new TreeMap<>()).merge(type, step.getGenerationStatus(), Math::max);
                if (!Status.DONE.equals(step.getGenerationStatusCode())) {
                    incomplete.computeIfAbsent(key, _ -> new TreeSet<>()).add(type);
                }
            }
        }
        result.forEach((key, types) -> types
                .replaceAll((type, status) -> incomplete.containsKey(key) && incomplete.get(key).contains(type) ? status
                        : GenerationMonitoringStatus.STATUS_OK));
        return result;
    }

    /**
     * Gets the products whose generic Product Description disables Delay Notification. If the descriptions cannot be
     * read, no product is excluded.
     *
     * @return the excluded product names (case-insensitive)
     */
    private static Set<String> excludedProducts() {
        final Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        try {
            for (final var m : MasterManager.getDB().getProductMetadata()) {
                if (m.isGeneric() && !m.isDelayNotification()) {
                    result.add(m.getProduct());
                }
            }
        } catch (final Exception e) {
            _log.warn("Cannot read the product descriptions, no product excluded from Delay Notification", e);
        }
        return result;
    }

    /**
     * Gets, for each known product and cycle, the most urgent step offset: the smallest minutes before schedule across
     * its steps, as listed on the summary page (completed steps use their completion time). The cycle's overall offset
     * (earliest step schedule) is used when no step is known.
     *
     * @return minutes before schedule keyed by "product@cycle"
     *
     * @throws Exception
     *             if the monitoring data cannot be read
     */
    public static Map<String, Long> minutesBeforeSchedulePerCycle() throws Exception {
        final Map<String, Long> steps = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (final ProductStepStatus step : ProductStepStatusHome.findAll()) {
            final var minutes = step.getMinutesBeforeSchedule();
            if (step.isPresent() && step.getProduct() != null && step.getTime() != null
                    && minutes != Integer.MIN_VALUE) {
                steps.merge(step.getProduct() + "@" + step.getTime(), minutes, Math::min);
            }
        }
        final Map<String, Long> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (final ProductStatus ps : ProductStatusHome.findFromMemory().values()) {
            if (ps.getProduct() == null || ps.getTime() == null || ps.getTime().isEmpty()) {
                continue;
            }
            final var key = ps.getProduct() + "@" + ps.getTime();
            final var minutes = steps.containsKey(key) ? steps.get(key) : ps.getMinutesBeforeSchedule();
            if (minutes != Integer.MIN_VALUE) {
                result.put(key, minutes);
            }
        }
        return result;
    }

    /**
     * Finds the most urgent cycle for each product. A negative value means that at least one cycle has a step past its
     * schedule; cycles without a schedule are ignored.
     *
     * @param perCycle
     *            the minutes before schedule keyed by "product@cycle"
     *
     * @return shortest minutes before schedule, keyed by product name
     */
    public static Map<String, Long> minutesBeforeSchedulePerProduct(final Map<String, Long> perCycle) {
        final Map<String, Long> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        perCycle.forEach((key, minutes) -> result.merge(key.substring(0, key.lastIndexOf('@')), minutes, Math::min));
        return result;
    }

    /**
     * Gets the generation status for each known product and cycle, as shown on the summary page.
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
     * Gets the generation status of the most urgent cycle for each product.
     *
     * @param perCycleMinutes
     *            the minutes before schedule keyed by "product@cycle"
     * @param perCycleStatus
     *            the generation status keyed by "product@cycle"
     *
     * @return generation status keyed by product name
     */
    public static Map<String, Integer> generationStatusPerProduct(final Map<String, Long> perCycleMinutes,
            final Map<String, Integer> perCycleStatus) {
        final Map<String, Integer> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        final Map<String, Long> nearest = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        perCycleMinutes.forEach((key, minutes) -> {
            final var product = key.substring(0, key.lastIndexOf('@'));
            final var status = perCycleStatus.get(key);
            if (status != null && (!nearest.containsKey(product) || minutes < nearest.get(product))) {
                nearest.put(product, minutes);
                result.put(product, status);
            }
        });
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
