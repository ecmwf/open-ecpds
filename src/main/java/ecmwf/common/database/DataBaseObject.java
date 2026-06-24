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
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.text.Util.isNotEmpty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;

import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;

import ecmwf.common.text.Format;

/**
 * The Class DataBaseObject.
 */
public abstract class DataBaseObject implements Serializable, Cloneable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 4784528033138742791L;

    /** The Constant TAG_ACROSS_MULTIPLE_LINES. */
    public static final String TAG_ACROSS_MULTIPLE_LINES = "*** (across-multiple-lines) ***";

    /** The Constant TAG_END_OF_LINES. */
    public static final String TAG_END_OF_LINES = "*** (end-of-lines) ***";

    /** The Constant ECTRANS_LASTUPDATE. */
    private static final String ECTRANS_LASTUPDATE = "ectrans.lastupdate";

    /** The Constant HTTP_TOKEN_EXPIRY — hidden from display and diffs (managed automatically). */
    private static final String HTTP_TOKEN_EXPIRY = "http.tokenExpiry";

    /** The Constant HTTP_TOKEN_VALUE — hidden from display and diffs (managed automatically). */
    private static final String HTTP_TOKEN_VALUE = "http.tokenValue";

    /** The Constant MAXIMUM_FIELD_SIZE. */
    private static final int MAXIMUM_FIELD_SIZE = 256;

    /** The collection size. */
    protected int collectionSize = -1;

    /**
     * Gets the collection size.
     *
     * @return the collection size
     */
    public int getCollectionSize() {
        return collectionSize;
    }

    /**
     * Sets the collection size.
     *
     * @param collectionSize
     *            the new collection size
     */
    public void setCollectionSize(final int collectionSize) {
        this.collectionSize = collectionSize;
    }

    /**
     * {@inheritDoc}
     *
     * Clone.
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
     * {@inheritDoc}
     *
     * To string.
     */
    @Override
    public String toString() {
        return toString(true);
    }

    /**
     * Converts into string.
     *
     * @param onSingleLine
     *            the on single line
     *
     * @return the string
     */
    public String toString(final boolean onSingleLine) {
        return toString(this, onSingleLine);
    }

    /**
     * Compare.
     *
     * @param object
     *            the object
     * @param mergeOriginalRevised
     *            the merge original revised
     *
     * @return the string
     */
    public String compare(final DataBaseObject object, final boolean mergeOriginalRevised) {
        return compare(this, object, mergeOriginalRevised);
    }

    /**
     * Converts into string.
     *
     * @param object
     *            the object
     * @param onSingleLine
     *            the on single line
     *
     * @return the string
     */
    public static final String toString(final Object object, final boolean onSingleLine) {
        final var result = new StringBuilder();
        for (final Field field : object.getClass().getDeclaredFields()) {
            final var fieldName = field.getName();
            if (fieldName.indexOf("_") == 3) {
                try {
                    var value = valueOf(field.get(object));
                    if ("HOS_DATA".equals(fieldName)) {
                        // We have to remove the "ectrans.lastupdate" and "http.tokenExpiry" parameters as
                        // they are irrelevant for the display!
                        value = remove(ECTRANS_LASTUPDATE, value);
                        value = remove(HTTP_TOKEN_EXPIRY, value);
                        value = remove(HTTP_TOKEN_VALUE, value);
                    }
                    if (!onSingleLine) {
                        // This is meant to be used in an email!
                        final var cr = value.indexOf("\n") != -1;
                        result.append("[").append(fieldName).append("]")
                                .append(cr ? "\n" + TAG_ACROSS_MULTIPLE_LINES + "\n" : " ")
                                .append(value.isEmpty() ? "(empty)" : value)
                                .append(cr ? "\n" + TAG_END_OF_LINES + "\n" : "\n");
                    } else {
                        // This is meant to be displayed in the log file!
                        if (value.length() > MAXIMUM_FIELD_SIZE) {
                            value = value.substring(0, MAXIMUM_FIELD_SIZE) + " (...)";
                        }
                        result.append("[").append(fieldName).append("] ").append(Format.trimString(value, "(empty)"))
                                .append(" ");
                    }
                } catch (final Exception e) {
                    // Ignored
                }
            }
        }
        return result.toString().trim();
    }

    /**
     * Converts into string.
     *
     * @param object
     *            the object
     *
     * @return the string
     */
    public static final String toString(final Object object) {
        return toString(object, false);
    }

    /**
     * Compare 2 Objects.
     *
     * @param object1
     *            the object1
     * @param object2
     *            the object2
     * @param mergeOriginalRevised
     *            the merge original revised
     *
     * @return the string
     */
    public static final String compare(final Object object1, final Object object2, final boolean mergeOriginalRevised) {
        final var result = new StringBuilder();
        for (final Field field : object1.getClass().getDeclaredFields()) {
            final var fieldName = field.getName();
            if (fieldName.indexOf("_") == 3) {
                try {
                    var value1 = valueOf(field.get(object1));
                    var value2 = valueOf(field.get(object2));
                    if ("HOS_DATA".equals(fieldName)) {
                        // We have to remove the "ectrans.lastupdate" and "http.tokenExpiry" parameters as
                        // they are irrelevant for the comparison!
                        value1 = remove(ECTRANS_LASTUPDATE, value1);
                        value1 = remove(HTTP_TOKEN_EXPIRY, value1);
                        value1 = remove(HTTP_TOKEN_VALUE, value1);
                        value2 = remove(ECTRANS_LASTUPDATE, value2);
                        value2 = remove(HTTP_TOKEN_EXPIRY, value2);
                        value2 = remove(HTTP_TOKEN_VALUE, value2);
                    }
                    if (!value1.equals(value2)) {
                        result.append("[").append(fieldName).append("] ");
                        for (final DiffRow row : DiffRowGenerator.create().showInlineDiffs(true)
                                .mergeOriginalRevised(mergeOriginalRevised).inlineDiffByWord(true).oldTag(_ -> "~")
                                .newTag(_ -> "**").build().generateDiffRows(Arrays.asList(value1.split("\n")),
                                        Arrays.asList(value2.split("\n")))) {
                            if (mergeOriginalRevised) {
                                result.append(row.getOldLine());
                            } else {
                                result.append(row.getOldLine()).append(" | ").append(row.getNewLine());
                            }
                            result.append("\n");
                        }
                    }
                } catch (final Exception e) {
                    // Ignored
                }
            }
        }
        return result.toString().trim();
    }

    /**
     * Value of.
     *
     * @param obj
     *            the obj
     *
     * @return the string
     */
    private static String valueOf(final Object obj) {
        return obj == null ? "(null)" : obj.toString();
    }

    /**
     * Removes the.
     *
     * @param parameterName
     *            the name of the parameter to remove
     * @param source
     *            the source string
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    /**
     * Removes all hidden/internal options from a raw HOS_DATA properties string. These options are managed
     * automatically by the system and should not be visible or editable by the user in the host editor. The same set of
     * options is stripped in {@link #toString(Object, boolean)} and {@link #compare(DataBaseObject, boolean)}.
     *
     * @param properties
     *            the raw properties text (the portion before {@code ECtransSetup.SEPARATOR})
     *
     * @return the properties text with hidden options removed
     *
     * @throws IOException
     *             if reading the properties string fails
     */
    public static String removeHiddenOptions(final String properties) throws IOException {
        var result = remove(ECTRANS_LASTUPDATE, properties);
        result = remove(HTTP_TOKEN_EXPIRY, result);
        result = remove(HTTP_TOKEN_VALUE, result);
        return result;
    }

    private static final String remove(final String parameterName, final String source) throws IOException {
        final var sb = new StringBuilder();
        final var reader = new BufferedReader(new StringReader(source));
        final var startsWith = parameterName + " = \"";
        String line;
        while ((line = reader.readLine()) != null) {
            if (!(line.startsWith(startsWith) && line.endsWith("\""))) {
                sb.append(line).append("\n");
            }
        }
        // Remove the last carriage return if there is one?
        final var length = sb.length();
        if (length > 0) {
            sb.deleteCharAt(length - 1);
        }
        return sb.toString();
    }

    /**
     * Strim.
     *
     * @param field
     *            the field
     * @param length
     *            the length
     *
     * @return the string
     */
    protected static final String strim(final String field, final int length) {
        if (field != null && field.length() > length) {
            return field.substring(0, length);
        }
        return field;
    }

    /**
     * Integer to string.
     *
     * @param param
     *            the param
     *
     * @return the string
     */
    protected static final String integerToString(final Integer param) {
        if (param != null) {
            return param.toString();
        }
        return null;
    }

    /**
     * String to integer.
     *
     * @param param
     *            the param
     *
     * @return the integer
     */
    protected static final Integer stringToInteger(final String param) {
        if (isNotEmpty(param)) {
            return Integer.parseInt(param);
        }
        return null;
    }

    /**
     * String to long.
     *
     * @param param
     *            the param
     *
     * @return the long
     */
    protected static final Long stringToLong(final String param) {
        if (isNotEmpty(param)) {
            return Long.parseLong(param);
        }
        return null;
    }

    /**
     * Integer to int.
     *
     * @param param
     *            the param
     * @param defaultValue
     *            the default value
     *
     * @return the int
     */
    protected static final int integerToInt(final Integer param, final int defaultValue) {
        if (param != null) {
            return param;
        }
        return defaultValue;
    }

    /**
     * String to string.
     *
     * @param param
     *            the param
     * @param defaultValue
     *            the default value
     *
     * @return the string
     */
    protected static final String stringToString(final String param, final String defaultValue) {
        if (param != null) {
            return param;
        }
        return defaultValue;
    }

    /**
     * Big decimal to timestamp.
     *
     * @param param
     *            the param
     *
     * @return the timestamp
     */
    protected static final Timestamp bigDecimalToTimestamp(final BigDecimal param) {
        if (param != null) {
            return new Timestamp(param.longValue());
        }
        return null;
    }

    /**
     * Timestamp to big decimal.
     *
     * @param param
     *            the param
     *
     * @return the big decimal
     */
    protected static final BigDecimal timestampToBigDecimal(final Timestamp param) {
        if (param != null) {
            return new BigDecimal(param.getTime());
        }
        return null;
    }
}
