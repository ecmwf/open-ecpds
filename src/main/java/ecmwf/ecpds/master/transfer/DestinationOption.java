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

package ecmwf.ecpds.master.transfer;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.Destination;
import ecmwf.common.technical.Cnf;
import ecmwf.common.text.Format;

/**
 * The Class DestinationOption.
 */
public final class DestinationOption {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(DestinationOption.class);

    /** The Constant UNKNOWN_TYPE. */
    public static final String UNKNOWN_TYPE = "Unknown";

    /** The Constant TYPE_USER_AND_GROUP. */
    public static final String TYPE_USER_AND_GROUP = "type";

    /** The Constant DELETE_FROM_SPOOL_ON_EXPIRATION. */
    private static final int DELETE_FROM_SPOOL_ON_EXPIRATION = 0;

    /** The Constant DELETE_FROM_SPOOL_ON_SUCCESS. */
    private static final int DELETE_FROM_SPOOL_ON_SUCCESS = 1;

    /** The Constant IF_TARGET_EXIST_OVERWRITE. */
    private static final int IF_TARGET_EXIST_OVERWRITE = 0;

    /** The Constant IF_TARGET_EXIST_RESUME. */
    private static final int IF_TARGET_EXIST_RESUME = 1;

    /** The Constant ON_HOST_FAILURE_NEXT_AND_STAY. */
    private static final int ON_HOST_FAILURE_NEXT_AND_STAY = 0;

    /** The Constant ON_HOST_FAILURE_NEXT_AND_RETRY. */
    private static final int ON_HOST_FAILURE_NEXT_AND_RETRY = 1;

    /** The Constant CATEGORY_ACQUISITION. */
    private static final int CATEGORY_ACQUISITION = 0;

    /** The Constant CATEGORY_DISSEMINATION. */
    private static final int CATEGORY_DISSEMINATION = 1;

    /** The Constant CATEGORY_TIME_CRITICAL. */
    private static final int CATEGORY_TIME_CRITICAL = 2;

    /** The Constant CATEGORY_OTHER. */
    private static final int CATEGORY_OTHER = 3;

    /** The Constant DATE_FORMAT. */
    private static final String DATE_FORMAT = "yyyyMMdd";

    /** The Constant deleteFromSpool. */
    public static final List<String> deleteFromSpool = List.of("On expiration", "On success");

    /** The Constant ifTargetExist. */
    public static final List<String> ifTargetExist = List.of("Overwrite", "Resume");

    /** The Constant onHostFailure. */
    public static final List<String> onHostFailure = List.of("Next and stay", "Next and retry");

    /** The Constant TYPES_LIST. */
    private static final List<TypeEntry> TYPES_LIST = new ArrayList<>();

    /** The if target exist flag. */
    private final int ifTargetExistFlag;

    /** The delete from spool flag. */
    private final int deleteFromSpoolFlag;

    /** The on host failure flag. */
    private final int onHostFailureFlag;

    /** Entry for each type. */
    public static final class TypeEntry implements Serializable {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 5452672480241186218L;

        /** The pos. */
        final int pos; // Position in the menu

        /** The id. */
        final int id; // Identifier of the type

        /** The category. */
        final int category; // Category where this type belong to

        /** The label. */
        final String label; // Label to display in the menu

        /**
         * Instantiates a new type entry.
         *
         * @param pos
         *            the pos
         * @param id
         *            the id
         * @param category
         *            the category
         * @param label
         *            the label
         *
         * @throws NumberFormatException
         *             the number format exception
         */
        private TypeEntry(final String pos, final String id, final String category, final String label)
                throws NumberFormatException {
            this(Integer.parseInt(pos), Integer.parseInt(id), Integer.parseInt(category), label);
        }

        /**
         * Instantiates a new type entry.
         *
         * @param posAndId
         *            the pos and id
         * @param category
         *            the category
         * @param label
         *            the label
         */
        private TypeEntry(final int posAndId, final int category, final String label) {
            this(posAndId, posAndId, category, label);
        }

        /**
         * Instantiates a new type entry.
         *
         * @param pos
         *            the pos
         * @param id
         *            the id
         * @param category
         *            the category
         * @param label
         *            the label
         */
        private TypeEntry(final int pos, final int id, final int category, final String label) {
            this.pos = pos;
            this.id = id;
            this.category = category < CATEGORY_OTHER ? category : CATEGORY_OTHER;
            this.label = label;
            log.debug("Adding type: pos={}, id={}, category={}, label={}", pos, id, category, label);
            TYPES_LIST.add(this);
        }

        /**
         * Gets the label.
         *
         * @return the label
         */
        public String getLabel() {
            return label;
        }

        /**
         * Gets the id.
         *
         * @return the id
         */
        public int getId() {
            return id;
        }
    }

    static {
        // Loading types from configuration. Format is "pos.id.category=label".
        for (final Map.Entry<String, String> entry : Cnf.at("DestinationTypes", Map.of()).entrySet()) {
            final var key = entry.getKey();
            Exception exception = null;
            var recorded = false;
            try {
                final var keys = key.split("\\.");
                if (keys.length == 3) {
                    new TypeEntry(keys[0], keys[1], keys[2], entry.getValue());
                    recorded = true;
                }
            } catch (final Exception ignored) {
                exception = ignored;
            } finally {
                if (!recorded) {
                    log.warn("Skipping destination option: {} (expecting pos.id.category=label)", key, exception);
                }
            }
        }
        // Default values if no valid configuration found!
        if (TYPES_LIST.isEmpty()) {
            new TypeEntry(0, CATEGORY_ACQUISITION, "Acquisition");
            new TypeEntry(1, CATEGORY_DISSEMINATION, "Dissemination");
            new TypeEntry(2, CATEGORY_TIME_CRITICAL, "Time Critical");
        } else {
            // Sorting so that in appears in the correct order
            TYPES_LIST.sort(Comparator.comparingInt(entry -> entry.pos));
        }
    }

    /**
     * Gets the list.
     *
     * @return the list
     */
    public static List<TypeEntry> getList() {
        return TYPES_LIST;
    }

    /**
     * Sets the list.
     *
     * @param list
     *            the new list
     */
    public static void setList(final List<TypeEntry> list) {
        log.debug("Setting list with {} element(s)", list.size());
        TYPES_LIST.clear();
        TYPES_LIST.addAll(list);
    }

    /**
     * Instantiates a new destination option.
     *
     * @param destination
     *            the destination
     */
    public DestinationOption(final Destination destination) {
        ifTargetExistFlag = destination.getIfTargetExist();
        deleteFromSpoolFlag = destination.getKeepInSpool();
        onHostFailureFlag = destination.getOnHostFailure();
    }

    /**
     * Get the type list ordered by the pos field.
     *
     * @param addAllSelectors
     *            if set add all selectors
     *
     * @return the ordered list of types
     */
    public static List<Map.Entry<Integer, String>> getTypes(final boolean addAllSelectors) {
        final List<Map.Entry<Integer, String>> result = new ArrayList<>();
        if (addAllSelectors) {
            result.add(Map.entry(-1, "All Categories"));
            if (count(CATEGORY_ACQUISITION) > 1) {
                result.add(Map.entry(-2, "All Acquisition"));
            }
            if (count(CATEGORY_DISSEMINATION) > 1) {
                result.add(Map.entry(-3, "All Dissemination"));
            }
            if (count(CATEGORY_TIME_CRITICAL) > 1) {
                result.add(Map.entry(-4, "All Time Critical"));
            }
            if (count(CATEGORY_OTHER) > 1) {
                result.add(Map.entry(-5, "All Other"));
            }
        }
        result.addAll(TYPES_LIST.stream().map(entry -> Map.entry(entry.id, entry.label)).toList());
        return result;
    }

    /**
     * Check if the id exists.
     *
     * @param id
     *            the id
     *
     * @return if it exists
     */
    public static boolean exists(final int id) {
        return TYPES_LIST.stream().anyMatch(entry -> entry.id == id);
    }

    /**
     * Utility method to count how many entries are recorded for a category.
     *
     * @param category
     *            the category
     *
     * @return the long
     */
    private static long count(final int category) {
        return TYPES_LIST.stream().filter(entry -> entry.category == category).count();
    }

    /**
     * Get the label for an id.
     *
     * @param id
     *            the id
     *
     * @return the label for the id
     */
    public static String getLabel(final String id) {
        try {
            return getLabel(Integer.parseInt(id));
        } catch (final NumberFormatException e) {
            return UNKNOWN_TYPE;
        }
    }

    /**
     * Get the type entry for a label.
     *
     * @param label
     *            the label
     *
     * @return the type entry for the label
     */
    public static TypeEntry getTypeEntry(final String label) {
        final var entry = TYPES_LIST.stream().filter(e -> e.label.equals(label)).findFirst();
        return entry.isPresent() ? entry.get() : null;
    }

    /**
     * Get the label for an id.
     *
     * @param id
     *            the id
     *
     * @return the label for the id
     */
    public static String getLabel(final int id) {
        final var entry = TYPES_LIST.stream().filter(e -> e.id == id).findFirst();
        return entry.isPresent() ? entry.get().label : UNKNOWN_TYPE;
    }

    /**
     * Utility method to get the category for an id.
     *
     * @param id
     *            the id
     *
     * @return the category
     */
    private static int getCategory(final int id) {
        final var entry = TYPES_LIST.stream().filter(e -> e.id == id).findFirst();
        return entry.isPresent() ? entry.get().category : -1;
    }

    /**
     * Checks if is acquisition.
     *
     * @param id
     *            the id
     *
     * @return true, if is acquisition
     */
    public static boolean isAcquisition(final int id) {
        return getCategory(id) == CATEGORY_ACQUISITION;
    }

    /**
     * Checks if is dissemination.
     *
     * @param id
     *            the id
     *
     * @return true, if is dissemination
     */
    public static boolean isDissemination(final int id) {
        return getCategory(id) == CATEGORY_DISSEMINATION;
    }

    /**
     * Checks if is time critical.
     *
     * @param id
     *            the id
     *
     * @return true, if is time critical
     */
    public static boolean isTimeCritical(final int id) {
        return getCategory(id) == CATEGORY_TIME_CRITICAL;
    }

    /**
     * Utility method to get all types for a category.
     *
     * @param category
     *            the category
     *
     * @return the all
     */
    private static String getAll(final int category) {
        return TYPES_LIST.stream().filter(entry -> entry.category == category).map(entry -> String.valueOf(entry.id))
                .collect(Collectors.joining(","));
    }

    /**
     * Gets the list of ids for the selected category.
     *
     * @param category
     *            the category
     *
     * @return true, if is time critical
     */
    public static String getTypeIds(final String category) {
        if ("-1".equals(category)) { // All Types
            return "";
        }
        if ("-2".equals(category)) { // All Acquisition
            return getAll(CATEGORY_ACQUISITION);
        }
        if ("-3".equals(category)) { // All Dissemination
            return getAll(CATEGORY_DISSEMINATION);
        }
        if ("-4".equals(category)) { // All Time Critical
            return getAll(CATEGORY_TIME_CRITICAL);
        }
        if ("-5".equals(category)) { // All Other
            return getAll(CATEGORY_OTHER);
        }
        // Selected category
        return category;
    }

    /**
     * If target exist resume.
     *
     * @return true, if successful
     */
    public boolean ifTargetExistResume() {
        return ifTargetExistFlag == IF_TARGET_EXIST_RESUME;
    }

    /**
     * If target exist overwrite.
     *
     * @return true, if successful
     */
    public boolean ifTargetExistOverwrite() {
        return ifTargetExistFlag == IF_TARGET_EXIST_OVERWRITE;
    }

    /**
     * Delete from spool on success.
     *
     * @return true, if successful
     */
    public boolean deleteFromSpoolOnSuccess() {
        return deleteFromSpoolFlag == DELETE_FROM_SPOOL_ON_SUCCESS;
    }

    /**
     * Delete from spool on expiration.
     *
     * @return true, if successful
     */
    public boolean deleteFromSpoolOnExpiration() {
        return deleteFromSpoolFlag == DELETE_FROM_SPOOL_ON_EXPIRATION;
    }

    /**
     * On host failure next and retry.
     *
     * @return true, if successful
     */
    public boolean onHostFailureNextAndRetry() {
        return onHostFailureFlag == ON_HOST_FAILURE_NEXT_AND_RETRY;
    }

    /**
     * On host failure next and stay.
     *
     * @return true, if successful
     */
    public boolean onHostFailureNextAndStay() {
        return onHostFailureFlag == ON_HOST_FAILURE_NEXT_AND_STAY;
    }

    /**
     * Parse the date according to the format specified in the Destination, or the default one if it is not specified.
     *
     * @param destination
     *            the formatted date
     * @param date
     *            the date
     *
     * @return the date
     */
    public static long parseDate(final Destination destination, final String date) {
        final var dateFormat = destination.getDateFormat();
        return Format.toTime(dateFormat != null ? dateFormat : DATE_FORMAT, date);
    }

    /**
     * Format the date according to the format specified in the Destination, or the default one if it is not specified.
     *
     * @param destination
     *            the date
     * @param date
     *            the date
     *
     * @return the formatted date
     */
    public static String formatDate(final Destination destination, final long date) {
        final var dateFormat = destination.getDateFormat();
        return Format.formatTime(dateFormat != null ? dateFormat : DATE_FORMAT, date);
    }
}
