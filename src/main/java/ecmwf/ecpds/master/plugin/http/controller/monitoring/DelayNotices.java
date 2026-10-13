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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.ecpds.master.MasterManager;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * The register of the delay notices, kept in the DELAY_NOTICE table of the database (see the Master's DataBaseImpl).
 * The status, creation and version are columns; everything else is a JSON document. References (DLY-YYYY-NNNN) are
 * allocated atomically by the Master, so several Monitors can register notices at the same time, and modifications use
 * optimistic locking (the version column) so that two operators changing the same notice cannot overwrite each other.
 * Notices are kept until an operator deletes them.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 8.2.0
 *
 * @since 2026-10-12
 */
public final class DelayNotices {

    /** SYS_CONFIG group of the settings (default Cc/Bcc). */
    private static final String SETTINGS_GROUP = "DelayNoticeSettings";

    /** The fields held by columns rather than by the JSON content. */
    private static final List<String> COLUMNS = List.of("ref", "status", "createdAt", "createdBy", "updatedAt",
            "version");

    /** How many times a modification is retried when another writer got in first. */
    private static final int MAX_ATTEMPTS = 5;

    /** The database accepts only Latin-1 so all the other characters are escaped in the JSON. */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
    }

    /** A change of a notice. */
    @FunctionalInterface
    public interface Change {

        /**
         * Applies the change.
         *
         * @param notice
         *            the notice
         *
         * @throws Exception
         *             if the change is refused
         */
        void apply(Map<String, Object> notice) throws Exception;
    }

    private DelayNotices() {
        // Hiding constructor!
    }

    private static Map<String, Object> parse(final Map<String, String> row) throws Exception {
        final Map<String, Object> notice = MAPPER.readValue(row.get("content"),
                new TypeReference<LinkedHashMap<String, Object>>() {
                });
        for (final var column : COLUMNS) {
            notice.put(column, "version".equals(column) ? Integer.valueOf(row.get(column)) : row.get(column));
        }
        return notice;
    }

    private static String content(final Map<String, Object> notice) throws Exception {
        final var copy = new LinkedHashMap<>(notice);
        COLUMNS.forEach(copy::remove);
        return MAPPER.writeValueAsString(copy);
    }

    /**
     * Loads the most recent notices.
     *
     * @param limit
     *            the maximum number of notices
     *
     * @return the notices, most recent first
     *
     * @throws Exception
     *             if they cannot be read
     */
    public static List<Map<String, Object>> list(final int limit) throws Exception {
        final var result = new ArrayList<Map<String, Object>>();
        for (final var row : MasterManager.getDB().getDelayNotices("%", limit)) {
            try {
                result.add(parse(row));
            } catch (final Exception e) {
                // A damaged notice must not hide the others.
            }
        }
        return result;
    }

    /**
     * Gets a notice.
     *
     * @param ref
     *            the reference
     *
     * @return the notice, or null
     *
     * @throws Exception
     *             if it cannot be read
     */
    public static Map<String, Object> get(final String ref) throws Exception {
        final var rows = MasterManager.getDB().getDelayNotices(ref, 1);
        return rows.isEmpty() ? null : parse(rows.get(0));
    }

    /**
     * Registers a new notice; the reference is allocated by the Master.
     *
     * @param notice
     *            the content (the "ref" is added to it)
     * @param status
     *            the initial status
     * @param createdBy
     *            the user
     *
     * @return the reference
     *
     * @throws Exception
     *             if it cannot be saved
     */
    public static String create(final Map<String, Object> notice, final String status, final String createdBy)
            throws Exception {
        final var ref = MasterManager.getDB().createDelayNotice(Calendar.getInstance().get(Calendar.YEAR), status,
                createdBy, content(notice));
        notice.put("ref", ref);
        return ref;
    }

    /**
     * Modifies a notice atomically: the notice is read, changed by the given function and written back only if nobody
     * changed it in the meantime, otherwise the whole operation is retried on the fresh version.
     *
     * @param ref
     *            the reference
     * @param change
     *            the change (it may throw IllegalArgumentException to refuse it and, being possibly run again, must not
     *            have side effects)
     *
     * @return the notice after the change
     *
     * @throws Exception
     *             if the notice is unknown, the change is refused or it cannot be saved
     */
    public static Map<String, Object> modify(final String ref, final Change change) throws Exception {
        for (var attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            final var notice = get(ref);
            if (notice == null) {
                throw new IllegalArgumentException("Unknown notice: " + ref);
            }
            final var version = (Integer) notice.get("version");
            change.apply(notice);
            if (MasterManager.getDB().updateDelayNotice(ref, version, String.valueOf(notice.get("status")),
                    content(notice))) {
                notice.put("version", version + 1);
                return notice;
            }
        }
        throw new IllegalStateException("Notice " + ref + " is being modified by someone else, please retry");
    }

    /**
     * Deletes a notice.
     *
     * @param ref
     *            the reference
     *
     * @throws Exception
     *             if it cannot be deleted
     */
    public static void delete(final String ref) throws Exception {
        MasterManager.getDB().deleteDelayNotice(ref);
    }

    /**
     * Gets the default Cc recipients.
     *
     * @return the addresses, or an empty string
     *
     * @throws Exception
     *             if the setting cannot be read
     */
    public static String defaultCc() throws Exception {
        return MasterManager.getDB().getMonitorConfigGroup(SETTINGS_GROUP).getOrDefault("defaultCc", "");
    }

    /**
     * Sets the default Cc recipients.
     *
     * @param cc
     *            the addresses
     *
     * @throws Exception
     *             if it cannot be saved
     */
    public static void setDefaultCc(final String cc) throws Exception {
        MasterManager.getDB().setMonitorConfigValue(SETTINGS_GROUP, "defaultCc", cc == null ? "" : cc.trim());
    }

    /**
     * Gets the default Bcc recipients.
     *
     * @return the addresses, or an empty string
     *
     * @throws Exception
     *             if the setting cannot be read
     */
    public static String defaultBcc() throws Exception {
        return MasterManager.getDB().getMonitorConfigGroup(SETTINGS_GROUP).getOrDefault("defaultBcc", "");
    }

    /**
     * Sets the default Bcc recipients.
     *
     * @param bcc
     *            the addresses
     *
     * @throws Exception
     *             if it cannot be saved
     */
    public static void setDefaultBcc(final String bcc) throws Exception {
        MasterManager.getDB().setMonitorConfigValue(SETTINGS_GROUP, "defaultBcc", bcc == null ? "" : bcc.trim());
    }
}
