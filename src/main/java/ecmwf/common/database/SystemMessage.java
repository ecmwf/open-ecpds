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

package ecmwf.common.database;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Represents a system-wide, time-bounded warning/maintenance message stored in the SYSTEM_MESSAGE table. Managed from
 * the Monitor UI (Admin Tasks &rarr; System Messages) and shown as a banner on the Monitor UI landing page
 * ({@code /do/start}) and on the Data Portal, to every user, for as long as the current time falls within the
 * message's start/end window. Messages are never displayed outside their window and require no manual cleanup, though
 * they remain listed in the admin page (for history) until explicitly deleted.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2026-09-24
 */

import java.math.BigDecimal;
import java.util.Objects;

/**
 * The Class SystemMessage.
 */
public class SystemMessage extends DataBaseObject {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** Severity level: informational, blue banner. */
    public static final String LEVEL_INFO = "info";

    /** Severity level: warning, orange banner (default). */
    public static final String LEVEL_WARNING = "warning";

    /** Severity level: danger, red banner. */
    public static final String LEVEL_DANGER = "danger";

    /** Auto-increment primary key. */
    protected Long SYM_ID;

    /** The message text, shown as-is (plain text) in the banner. */
    protected String SYM_MESSAGE;

    /** The severity level: "info", "warning" or "danger". */
    protected String SYM_LEVEL;

    /** Start of the display window (millis since epoch). */
    protected BigDecimal SYM_START_TIME;

    /** End of the display window (millis since epoch). */
    protected BigDecimal SYM_END_TIME;

    /** Login of the admin who created/last saved this message. */
    protected String SYM_CREATED_BY;

    /** Creation timestamp (millis since epoch). */
    protected BigDecimal SYM_CREATED_AT;

    /** Instantiates a new SystemMessage. */
    public SystemMessage() {
    }

    public long getId() {
        return SYM_ID != null ? SYM_ID : 0;
    }

    public void setId(final long id) {
        SYM_ID = id;
    }

    public String getMessage() {
        return SYM_MESSAGE;
    }

    public void setMessage(final String message) {
        SYM_MESSAGE = message;
    }

    public String getLevel() {
        return SYM_LEVEL != null ? SYM_LEVEL : LEVEL_WARNING;
    }

    public void setLevel(final String level) {
        SYM_LEVEL = level;
    }

    public long getStartTime() {
        return SYM_START_TIME != null ? SYM_START_TIME.longValue() : 0;
    }

    public void setStartTime(final long time) {
        SYM_START_TIME = BigDecimal.valueOf(time);
    }

    public long getEndTime() {
        return SYM_END_TIME != null ? SYM_END_TIME.longValue() : 0;
    }

    public void setEndTime(final long time) {
        SYM_END_TIME = BigDecimal.valueOf(time);
    }

    public String getCreatedBy() {
        return SYM_CREATED_BY;
    }

    public void setCreatedBy(final String createdBy) {
        SYM_CREATED_BY = createdBy;
    }

    public long getCreatedAt() {
        return SYM_CREATED_AT != null ? SYM_CREATED_AT.longValue() : 0;
    }

    public void setCreatedAt(final long time) {
        SYM_CREATED_AT = BigDecimal.valueOf(time);
    }

    /**
     * Checks if this message is currently active, i.e. the given time falls within its start/end window (inclusive).
     *
     * @param now
     *            the current time, in millis since epoch
     *
     * @return true, if this message should currently be displayed
     */
    public boolean isActiveAt(final long now) {
        return now >= getStartTime() && now <= getEndTime();
    }

    /** Formatter for the start/end window, e.g. "24 Sep 2026, 12:30" (date omitted from the end if same day). */
    private static final java.time.format.DateTimeFormatter TIMEFRAME_DATE_TIME_FORMAT = java.time.format.DateTimeFormatter
            .ofPattern("d MMM yyyy, HH:mm");

    private static final java.time.format.DateTimeFormatter TIMEFRAME_TIME_FORMAT = java.time.format.DateTimeFormatter
            .ofPattern("HH:mm");

    /**
     * Formats the start/end window as a human-readable UTC timeframe, e.g. "24 Sep 2026, 12:30 - 13:30 UTC" (same day)
     * or "24 Sep 2026, 12:30 - 25 Sep 2026, 06:00 UTC" (spanning multiple days). Intended to be shown alongside the
     * free-text message so the reason for the outage does not need to also repeat the schedule.
     *
     * @return the formatted timeframe, e.g. "24 Sep 2026, 12:30 - 13:30 UTC"
     */
    public String getFormattedTimeframe() {
        final var zone = java.time.ZoneOffset.UTC;
        final var start = java.time.Instant.ofEpochMilli(getStartTime()).atZone(zone);
        final var end = java.time.Instant.ofEpochMilli(getEndTime()).atZone(zone);
        final var sameDay = start.toLocalDate().equals(end.toLocalDate());
        return start.format(TIMEFRAME_DATE_TIME_FORMAT) + " - "
                + (sameDay ? end.format(TIMEFRAME_TIME_FORMAT) : end.format(TIMEFRAME_DATE_TIME_FORMAT)) + " UTC";
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof final SystemMessage m && Objects.equals(SYM_ID, m.SYM_ID);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(SYM_ID);
    }
}
