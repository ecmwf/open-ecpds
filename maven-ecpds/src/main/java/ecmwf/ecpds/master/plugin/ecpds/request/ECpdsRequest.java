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

package ecmwf.ecpds.master.plugin.ecpds.request;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.text.Util.isNotEmpty;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.text.Format;

/**
 * The Class ECpdsRequest.
 */
public abstract class ECpdsRequest implements Serializable, Cloneable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 4784528033138742791L;

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ECpdsRequest.class);

    /** The Constant _req. */
    private static final Logger _req = LogManager.getLogger("ECpdsRequestLogs");

    /**
     * Process.
     *
     * @param ratio
     *            the ratio
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract void process(long ratio) throws IOException;

    /**
     * Gets the timestamp.
     *
     * @return the timestamp
     */
    public abstract long getTIMESTAMP();

    /**
     * Sets the timestamp.
     *
     * @param timestamp
     *            the new timestamp
     */
    public abstract void setTIMESTAMP(long timestamp);

    /**
     * Gets the type.
     *
     * @return the type
     */
    public abstract String getTYPE();

    /**
     * Store.
     */
    public void store() {
        try {
            _req.info(toString());
        } catch (final Throwable t) {
        }
    }

    /**
     * Clone.
     *
     * @return the object
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (final CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return _toString(this);
    }

    /**
     * From string.
     *
     * @param string
     *            the string
     *
     * @throws IllegalAccessException
     *             the illegal access exception
     */
    public void fromString(final String string) throws IllegalAccessException {
        _fromString(this, string);
    }

    /**
     * Fill the object with the values found in the text line.
     *
     * @param object
     *            the object
     * @param string
     *            the string
     *
     * @throws IllegalAccessException
     *             the illegal access exception
     */
    private static final void _fromString(final Object object, final String string) throws IllegalAccessException {
        final var tokens = string.split("[\\[\\]]");
        final var tags = Arrays.copyOfRange(tokens, 1, tokens.length);
        String name = null;
        for (String token : tags) {
            token = token.trim();
            if (name == null) {
                name = token;
            } else {
                try {
                    final var field = object.getClass().getDeclaredField("_" + name);
                    final Type type = field.getType();
                    if (type.equals(String.class)) {
                        field.set(object, token);
                    }
                    if (type.equals(Boolean.class)) {
                        field.set(object, Boolean.parseBoolean(token));
                    }
                    if (type.equals(Integer.class)) {
                        field.set(object, Integer.parseInt(token));
                    }
                    if (type.equals(Long.class)) {
                        field.set(object, Long.parseLong(token));
                    }
                } catch (final NoSuchFieldException e) {
                    _log.warn("Field " + name + " no found for " + object.getClass().getName());
                } finally {
                    name = null;
                }
            }
        }
    }

    /**
     * Dump the content of the request in a text line (only the not-null fields and not-empty fields are shown).
     *
     * @param object
     *            the object
     *
     * @return the string
     */
    private static final String _toString(final Object object) {
        final var fields = object.getClass().getDeclaredFields();
        final var result = new StringBuilder();
        for (final Field field : fields) {
            final var fieldName = field.getName();
            if (fieldName.startsWith("_")) {
                try {
                    final var fieldValue = field.get(object);
                    if (fieldValue != null) {
                        final var value = String.valueOf(fieldValue);
                        if (value.length() > 0) {
                            result.append("[").append(fieldName.substring(1)).append("] ")
                                    .append(Format.trimString(value, "")).append(" ");
                        }
                    }
                } catch (final Exception e) {
                }
            }
        }
        return result.toString().trim();
    }

    /**
     * Change date.
     *
     * @param metadata
     *            the metadata
     * @param timestamp
     *            the timestamp
     * @param ratio
     *            the ratio
     *
     * @return the string
     */
    public static String changeDate(final String metadata, final long timestamp, final long ratio) {
        final var format = "yyyyMMdd";
        final var result = new StringBuilder();
        if (isNotEmpty(metadata)) {
            for (String value : metadata.split(",")) {
                if (value.toLowerCase().startsWith("date=")) {
                    final var originalDate = value.substring(5);
                    value = "date=" + changeDate(format, originalDate, timestamp, ratio);
                }
                result.append(((result.length() > 0 ? "," : "") + value).trim());
            }
        }
        return result.toString();
    }

    /**
     * Change date.
     *
     * @param format
     *            the format
     * @param date
     *            the date
     * @param timestamp
     *            the timestamp
     * @param ratio
     *            the ratio
     *
     * @return the string
     */
    public static String changeDate(final String format, final String date, final long timestamp, final long ratio) {
        final var currentDate = Format.toTime(format, Format.formatTime(format, timestamp));
        final var delay = (System.currentTimeMillis() - currentDate) / ratio;
        final var originalDate = Format.toTime(format, date);
        if (originalDate != -1) {
            return Format.formatTime(format, originalDate + delay);
        }
        return date;
    }

    /**
     * Change timefile.
     *
     * @param timefile
     *            the timefile
     * @param timestamp
     *            the timestamp
     * @param ratio
     *            the ratio
     *
     * @return the string
     */
    public static String changeTimefile(final String timefile, final long timestamp, final long ratio) {
        final var delay = (System.currentTimeMillis() - timestamp) / ratio;
        final var originalDate = Long.parseLong(timefile) * 1000L;
        return String.valueOf((originalDate + delay) / 1000L);
    }
}
