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

package ecmwf.common.text;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.text.Util.isNotEmpty;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.management.timer.Timer;
import javax.script.ScriptException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.DataBaseException;
import ecmwf.common.rmi.SocketConfig;
import ecmwf.common.technical.ScriptManager;

/**
 * The Class Format.
 */
public final class Format {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(Format.class);

    /** The Constant CRLFb. */
    private static final byte[] CRLFb = { (byte) 0x0d, (byte) 0x0a };

    /** The Constant _dontNeedEncoding. */
    private static final BitSet _dontNeedEncoding;

    /** The _dflt enc name. */
    private static String _dfltEncName = null;

    /** The Constant HEXA_LOOKUP. */
    private static final char[] HEXA_LOOKUP = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f' };

    /** The Constant EMAIL_REGEX. */
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$";

    static {
        _dontNeedEncoding = new BitSet(256);
        int i;
        for (i = 'a'; i <= 'z'; i++) {
            _dontNeedEncoding.set(i);
        }
        for (i = 'A'; i <= 'Z'; i++) {
            _dontNeedEncoding.set(i);
        }
        for (i = '0'; i <= '9'; i++) {
            _dontNeedEncoding.set(i);
        }
        _dontNeedEncoding.set(' ');
        _dontNeedEncoding.set('-');
        _dontNeedEncoding.set('_');
        _dontNeedEncoding.set('.');
        _dontNeedEncoding.set('*');
        _dfltEncName = Charset.defaultCharset().displayName();
    }

    /**
     * Instantiates a new format.
     */
    private Format() {
        // Hiding implicit public constructor!
    }

    /**
     * Convert unix 2 sql wildcards and also escape the special sql characters '%' and '_'.
     *
     * @param unix
     *            the unix
     *
     * @return the string
     */
    public static String unix2sqlWildcards(final String unix) {
        final var buffer = new StringBuilder();
        for (final char c : unix.toCharArray()) {
            switch (c) {
            case '%':
                buffer.append("\\%");
                break;
            case '_':
                buffer.append("\\_");
                break;
            case '*':
                buffer.append("%");
                break;
            case '?':
                buffer.append("_");
                break;
            default:
                buffer.append(c);
                break;
            }
        }
        return buffer.toString();
    }

    /**
     * _format number.
     *
     * @param myDouble
     *            the my double
     *
     * @return the string
     */
    private static String _formatNumber(final double myDouble) {
        final var myDoubleMultiplied = myDouble * 100;
        final double myDoubleRounded = Math.round(myDoubleMultiplied);
        final var myDoubleDivided = myDoubleRounded / 100.0;
        return String.valueOf(myDoubleDivided);
    }

    /**
     * Check if this is a valid email.
     *
     * @param email
     *            the email
     *
     * @return valid
     */
    public static boolean isValidEmail(final String email) {
        return email != null && !email.isBlank()
                && Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE).matcher(email).matches();
    }

    /**
     * Check if this is a valid identifier (with letters, digits and allowed characters).
     *
     * @param id
     *            the identifier
     * @param allowed
     *            the allowed
     *
     * @return valid
     */
    public static boolean isValidId(final String id, final String allowed) {
        if (id == null) {
            return false;
        }
        final var result = id.trim();
        if (result.isEmpty()) {
            return false;
        }
        for (final char c : result.toCharArray()) {
            if (!Character.isDigit(c) && !Character.isLetter(c) && allowed.indexOf(c) == -1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Escape html.
     *
     * @param s
     *            the s
     *
     * @return the string
     */
    public static String escapeHTML(final String s) {
        final var out = new StringBuilder(Math.max(16, s.length()));
        for (var i = 0; i < s.length(); i++) {
            final var c = s.charAt(i);
            if (c > 127 || c == '"' || c == '<' || c == '>' || c == '&') {
                out.append("&#");
                out.append((int) c);
                out.append(';');
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    /**
     * Format size.
     *
     * @param bytes
     *            the bytes
     *
     * @return the string
     */
    public static String formatSize(final long bytes) {
        final var absBytes = Math.abs(bytes);
        if (absBytes >= 1152921504606846976d) {
            final var eb = bytes / 1152921504606846976d;
            return _formatNumber(eb) + " Ebytes";
        }
        if (absBytes >= 1125899906842624d) {
            final var pb = bytes / 1125899906842624d;
            return _formatNumber(pb) + " Pbytes";
        } else if (absBytes >= 1099511627776d) {
            final var tb = bytes / 1099511627776d;
            return _formatNumber(tb) + " Tbytes";
        } else if (absBytes >= 1073741824) {
            final var gb = bytes / 1073741824d;
            return _formatNumber(gb) + " Gbytes";
        } else if (absBytes >= 1048576) {
            final var mb = bytes / 1048576d;
            return _formatNumber(mb) + " Mbytes";
        } else if (absBytes >= 1024) {
            final var kb = bytes / 1024d;
            return _formatNumber(kb) + " Kbytes";
        } else {
            return bytes + " byte" + (absBytes > 1 ? "s" : "");
        }
    }

    /**
     * Convert Bytes in Mbits.
     *
     * @param bytes
     *            the bytes
     *
     * @return the string
     */
    public static double toMBits(final long bytes) {
        final var bits = bytes * 8;
        final var mb = (double) bits / 1048576;
        return Math.round(mb * 100.0) / 100.0;
    }

    /**
     * Format time.
     *
     * @param date
     *            the date
     *
     * @return the string
     */
    public static String formatTime(final Timestamp date) {
        return date == null ? null : formatTime(date.getTime());
    }

    /**
     * Format time.
     *
     * @param date
     *            the date
     *
     * @return the string
     */
    public static String formatTime(final long date) {
        if (date <= 0) {
            return "-";
        }
        final var df = new Date(date);
        final var dt = new GregorianCalendar();
        dt.setTime(df);
        final var sdf = new SimpleDateFormat(
                new GregorianCalendar().get(Calendar.YEAR) != dt.get(Calendar.YEAR) ? "MMM dd yyyy" : "MMM dd HH:mm",
                Locale.ENGLISH);
        return sdf.format(df);
    }

    /**
     * Format current time.
     *
     * @return the string
     */
    public static String formatCurrentTime() {
        return formatTime(System.currentTimeMillis());
    }

    /**
     * Format time.
     *
     * @param format
     *            the format
     * @param date
     *            the date
     *
     * @return the string
     */
    public static String formatTime(final String format, final long date) {
        if (date <= 0) {
            return "-";
        }
        final var df = new Date(date);
        final var dt = new GregorianCalendar();
        dt.setTime(df);
        return new SimpleDateFormat(format).format(df);
    }

    /**
     * Format duration.
     *
     * @param start
     *            the start
     * @param stop
     *            the stop
     *
     * @return the string
     */
    public static String formatDuration(final long start, final long stop) {
        return formatDuration(stop - start);
    }

    /**
     * Converts into list.
     *
     * @param <E>
     *            the element type
     * @param vector
     *            the vector
     *
     * @return the string
     */
    public static <E> String toList(final Vector<E> vector) {
        final var result = new StringBuilder();
        final var size = vector.size();
        for (var i = 0; i < size; i++) {
            result.append(result.length() > 0 ? i == size - 1 ? " and " : ", " : "").append(vector.elementAt(i));
        }
        return result.toString();
    }

    /**
     * Converts into list.
     *
     * @param <E>
     *            the element type
     * @param list
     *            the vector
     *
     * @return the string
     */
    public static <E> String toList(final List<E> list) {
        final var result = new StringBuilder();
        final var size = list.size();
        for (var i = 0; i < size; i++) {
            result.append(result.length() > 0 ? i == size - 1 ? " and " : ", " : "").append(list.get(i));
        }
        return result.toString();
    }

    /**
     * Format duration.
     *
     * @param duration
     *            the duration
     *
     * @return the string
     */
    public static String formatDuration(final long duration) {
        if (duration < 1000) {
            return (duration < 0 ? 0 : duration) + "ms";
        }
        var seconds = (double) duration / (double) 1000;
        var remaining = 0L;
        final String ext;
        if (seconds > 60) {
            seconds = seconds / 60;
            if (seconds > 60) {
                seconds = seconds / 60;
                remaining = duration - (long) seconds * 3600000L;
                ext = "h";
            } else {
                remaining = duration - (long) seconds * 60000L;
                ext = "m";
            }
        } else {
            remaining = duration - (long) seconds * 1000L;
            ext = "s";
        }
        final var result = String.valueOf(seconds);
        final var pos = result.indexOf('.');
        return (result.substring(0, pos > -1 ? pos : result.length()) + ext
                + (remaining > 0 ? ' ' + formatDuration(remaining) : "")).trim();
    }

    /**
     * Format boolean.
     *
     * @param bool
     *            the bool
     *
     * @return the string
     */
    public static String formatBoolean(final boolean bool) {
        return bool ? "yes" : "no";
    }

    /**
     * Format long.
     *
     * @param n
     *            the n
     * @param width
     *            the width
     *
     * @return the string
     */
    public static String formatLong(final long n, final int width) {
        return formatLong(n, width, false);
    }

    /**
     * Format long.
     *
     * @param n
     *            the n
     * @param width
     *            the width
     * @param zero
     *            the zero
     *
     * @return the string
     */
    public static String formatLong(final long n, final int width, final boolean zero) {
        final var num = new StringBuilder(Long.toString(n));
        final var spaces = width - num.length();
        for (var i = 0; i < spaces; i++) {
            num.insert(0, zero ? '0' : ' ');
        }
        return num.toString();
    }

    /**
     * Format the transfer rate in GBytes/s or MBytes/s or KBytes/s or Bytes/s depending of the size.
     *
     * @param size
     *            the size
     * @param duration
     *            the duration
     *
     * @return the string
     */
    public static String formatRate(final long size, final long duration) {
        return formatSize((long) (size / ((double) (duration <= 0 ? 1 : duration) / (double) 1000))) + "/s";
    }

    /**
     * Get the transfer rate in Mbits/s.
     *
     * @param size
     *            the size
     * @param duration
     *            the duration
     *
     * @return the string
     */
    public static double getMBitsPerSeconds(final long size, final long duration) {
        return toMBits((long) (size / ((double) (duration <= 0 ? 1 : duration) / (double) 1000)));
    }

    /**
     * Format percentage.
     *
     * @param size
     *            the size
     * @param sent
     *            the sent
     *
     * @return the string
     */
    public static String formatPercentage(final long size, final long sent) {
        return size == 0 || sent - size == 0 ? "100.00%"
                : new DecimalFormat("0.00").format((double) sent / size * 100) + "%";
    }

    /**
     * Format percentage with the of, over and left to comment.
     *
     * @param size
     *            the size
     * @param sent
     *            the sent
     *
     * @return the string
     */
    public static String formatPercentageOf(final long size, final long sent) {
        final var formattedSize = formatSize(size);
        final var diff = sent - size;
        if (size == 0 || diff == 0) {
            return "100% of " + formattedSize; // Nothing to do!
        }
        final var result = new StringBuilder();
        final var percentage = (int) (100d * sent / size);
        if (percentage == 100 || percentage == 0) { // In progress
            final var formattedSent = formatSize(sent);
            if (formattedSize.equals(formattedSent)) {
                result.append(formatSize(Math.abs(diff))).append(" ").append(diff > 0 ? "over" : "left to");
            } else {
                result.append(formattedSent).append(" of");
            }
        } else {
            result.append(percentage).append("% of");
        }
        return result.append(" ").append(formattedSize).toString();
    }

    /**
     * Format string.
     *
     * @param text
     *            the text
     * @param width
     *            the width
     *
     * @return the string
     */
    public static String formatString(final String text, final int width) {
        return formatString(text, width, ' ');
    }

    /**
     * Format string.
     *
     * @param text
     *            the text
     * @param width
     *            the width
     *
     * @return the string
     */
    public static String formatString(final Object text, final int width) {
        return formatString(String.valueOf(text), width, ' ', true);
    }

    /**
     * Format string.
     *
     * @param text
     *            the text
     * @param width
     *            the width
     * @param onRight
     *            the on right
     *
     * @return the string
     */
    public static String formatString(final Object text, final int width, final boolean onRight) {
        return formatString(String.valueOf(text), width, ' ', onRight);
    }

    /**
     * Format string.
     *
     * @param text
     *            the text
     * @param width
     *            the width
     * @param empty
     *            the empty
     *
     * @return the string
     */
    public static String formatString(final String text, final int width, final char empty) {
        return formatString(text, width, empty, true);
    }

    /**
     * Format string.
     *
     * @param text
     *            the text
     * @param width
     *            the width
     * @param empty
     *            the empty
     * @param onRight
     *            the on right
     *
     * @return the string
     */
    public static String formatString(String text, final int width, final char empty, final boolean onRight) {
        if (text == null) {
            text = "(none)";
        }
        final var spaces = width - text.length();
        for (var i = 0; i < spaces; i++) {
            text = onRight ? text + empty : empty + text;
        }
        return text;
    }

    /**
     * Parses the duration from ISO 8601 format. Is it based on the ISO-8601 duration format?
     *
     * @param duration
     *            the duration
     *
     * @return the duration
     */
    private static Duration parseDurationFromISO8601Format(final String duration) {
        try {
            return duration != null ? Duration.parse(duration.trim()) : null;
        } catch (final DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parse the duration with default value if no unit specified.
     *
     * @param duration
     *            the duration string
     * @param defaultInMillisec
     *            default unit in milliseconds
     *
     * @return the duration
     *
     * @throws NumberFormatException
     *             the number format exception
     */
    private static long parseDuration(final String duration, final long defaultInMillisec)
            throws NumberFormatException {
        // First check if this is not an ISO8601 duration?
        final var fromISO = parseDurationFromISO8601Format(duration);
        if (fromISO != null) {
            return fromISO.toMillis();
        }
        // We are expecting the old format!
        try {
            final var toParse = duration.trim();
            final var length = toParse.length() - 1;
            switch (toParse.charAt(length)) {
            case 'y':
                return Long.parseLong(toParse.substring(0, length)) * Timer.ONE_WEEK * 52;
            case 'w':
                return Long.parseLong(toParse.substring(0, length)) * Timer.ONE_WEEK;
            case 'd':
                return Long.parseLong(toParse.substring(0, length)) * Timer.ONE_DAY;
            case 'h':
                return Long.parseLong(toParse.substring(0, length)) * Timer.ONE_HOUR;
            case 'm':
                return Long.parseLong(toParse.substring(0, length)) * Timer.ONE_MINUTE;
            case 's':
                return Long.parseLong(toParse.substring(0, length)) * Timer.ONE_SECOND;
            case 'z':
                return Long.parseLong(toParse.substring(0, length));
            default:
                // Let's use the default!
                final var result = Long.parseLong(toParse) * defaultInMillisec;
                if (defaultInMillisec == Timer.ONE_HOUR) {
                    _log.warn("Defaulting to unit in hours");
                }
                return result;
            }
        } catch (final Exception ignored) {
            // Ignored!
        }
        throw new NumberFormatException("Duration format error: " + duration);
    }

    /**
     * Parse the duration with default value in milliseconds.
     *
     * @param duration
     *            the duration string
     *
     * @return the duration
     *
     * @throws java.lang.NumberFormatException
     *             the number format exception
     */
    public static long parseDurationWithDefaultInMillis(final String duration) throws NumberFormatException {
        return parseDuration(duration, 1);
    }

    /**
     * Gets the duration with default value in hours.
     *
     * @param duration
     *            the duration
     *
     * @return the duration
     *
     * @throws java.lang.NumberFormatException
     *             the number format exception
     */
    public static long getDuration(final String duration) throws NumberFormatException {
        return parseDuration(duration, Timer.ONE_HOUR);
    }

    /**
     * Format value.
     *
     * @param value
     *            the value
     * @param width
     *            the width
     *
     * @return the string
     */
    public static String formatValue(final long value, final int width) {
        return Format.formatString(String.valueOf(value), width, '0', false);
    }

    /**
     * Trim string.
     *
     * @param text
     *            the text
     * @param maxSize
     *            the max size
     *
     * @return the string
     */
    public static String trimString(final String text, final int maxSize) {
        return maxSize > 0 && text != null && text.length() > maxSize ? text.substring(0, maxSize) : text;
    }

    /**
     * Format until.
     *
     * @param lastDate
     *            the last date
     *
     * @return the string
     */
    public static String formatUntil(final long lastDate) {
        double newValue;
        final String ext;
        if ((newValue = lastDate) <= -1) {
            return null;
        }
        newValue = (System.currentTimeMillis() - newValue) / 1000;
        if (newValue > 60) {
            newValue = newValue / 60;
            if (newValue > 60) {
                return Format.formatTime(lastDate);
            } else {
                ext = "minute" + (newValue > 1 ? "s" : "");
            }
        } else {
            ext = "second" + (newValue > 1 ? "s" : "");
        }
        final var result = String.valueOf(newValue);
        final var pos = result.indexOf('.');
        return result.substring(0, pos > -1 ? pos : result.length()) + ' ' + ext;
    }

    /**
     * From xml value.
     *
     * @param target
     *            the target
     *
     * @return the string
     */
    public static String fromXMLValue(String target) {
        int ind1;
        int ind2;
        final var result = new StringBuilder();
        while ((ind1 = target.indexOf("%")) != -1 && (ind2 = target.substring(ind1).indexOf(";")) != -1) {
            final var c = Integer.parseInt(target.substring(ind1 + 1, ind1 + ind2));
            result.append(target.substring(0, ind1)).append((char) c);
            target = target.substring(ind1 + ind2 + 1);
        }
        return result.append(target).toString();
    }

    /**
     * Hide.
     *
     * @param value
     *            the value
     *
     * @return the string
     */
    public static String hide(final Object value) {
        final var newValue = new StringBuilder();
        for (var i = 0; i < (value != null ? String.valueOf(value).length() : 0); i++) {
            newValue.append("*");
        }
        return newValue.toString();
    }

    /**
     * Windows to unix.
     *
     * @param text
     *            the text
     *
     * @return the string
     */
    public static String windowsToUnix(final String text) {
        final var data = text.getBytes();
        return new String(windowsToUnix(data, 0, data.length));
    }

    /**
     * Windows to unix.
     *
     * @param data
     *            the data
     * @param offset
     *            the offset
     * @param len
     *            the len
     *
     * @return the byte[]
     */
    public static byte[] windowsToUnix(final byte[] data, final int offset, final int len) {
        final var out = new ByteArrayOutputStream();
        for (var i = offset; i < offset + len; i++) {
            final var c = data[i];
            if (c == CRLFb[0] && i + 1 < data.length && data[i + 1] == CRLFb[1]) {
                continue;
            }
            out.write(c);
        }
        return out.toByteArray();
    }

    /**
     * Unix to windows.
     *
     * @param text
     *            the text
     *
     * @return the string
     */
    public static String unixToWindows(final String text) {
        final var out = new ByteArrayOutputStream();
        final var in = new ByteArrayInputStream(text.getBytes());
        final var dis = new BufferedReader(new InputStreamReader(in));
        String line;
        try {
            while ((line = dis.readLine()) != null) {
                out.write((line + "\r\n").getBytes());
            }
        } catch (final IOException e) {
            _log.debug(e);
            return text;
        }
        return new String(out.toByteArray());
    }

    /**
     * Replace all.
     *
     * @param target
     *            the target
     * @param name
     *            the name
     * @param value
     *            the value
     */
    public static void replaceAll(final StringBuilder target, final String name, final Object value) {
        int index;
        final var newValue = value != null ? String.valueOf(value) : "";
        final var lenght = name.length();
        while ((index = target.toString().toLowerCase().indexOf(name.toLowerCase())) != -1) {
            target.delete(index, index + lenght);
            target.insert(index, newValue);
        }
    }

    /**
     * Replace all. Also check if there are no parameters configured after the parameter name (.e.g $target[2..3]) in
     * target.
     *
     * @param target
     *            the target
     * @param name
     *            the name
     * @param value
     *            the value
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void replaceAllExt(final StringBuilder target, final String name, final String value)
            throws IOException {
        final var result = new StringBuilder();
        int index;
        try {
            while ((index = target.toString().toLowerCase().indexOf(name)) != -1) {
                result.append(target.substring(0, index));
                target.delete(0, index + name.length());
                final String option;
                if (target.indexOf("[") == 0 && (index = target.indexOf("]")) != -1) {
                    option = target.substring(1, index);
                    target.delete(0, index + 1);
                } else {
                    option = null;
                }
                if (option != null && !option.isEmpty()) {
                    // A range is specified (e.g. $name[1..2], $name[2] or
                    // $name[-2])
                    final int i;
                    if ((i = option.indexOf("..")) != -1) {
                        // We have a start and end index specified
                        final var first = Integer.parseInt(option.substring(0, i));
                        final var second = Integer.parseInt(option.substring(i + 2));
                        target.insert(0, value.substring(first, second));
                    } else {
                        // We only have a start position
                        final var first = Integer.parseInt(option);
                        if (first >= 0) {
                            // starting from the beginning of the string
                            target.insert(0, value.substring(first));
                        } else {
                            // Starting from the end of the string
                            target.insert(0, value.substring(value.length() + first));
                        }
                    }
                } else {
                    target.insert(0, value);
                }
            }
            target.insert(0, result);
        } catch (final Throwable t) {
            throw new IOException("replacing " + name + " in metadata");
        }
    }

    /**
     * Replace all. Also check if there are no parameters configured after the parameter name (.e.g $target[2..3]) in
     * target.
     *
     * @param target
     *            the target
     * @param name
     *            the name
     * @param value
     *            the value
     *
     * @return the string
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static String replaceAllExt(final String target, final String name, final String value) throws IOException {
        final var result = new StringBuilder(target);
        replaceAllExt(result, name, value);
        return result.toString();
    }

    /**
     * Replace all.
     *
     * @param target
     *            the target
     * @param name
     *            the name
     * @param value
     *            the value
     *
     * @return the string
     */
    public static String replaceAll(final String target, final String name, final Object value) {
        final var result = new StringBuilder(target);
        replaceAll(result, name, value);
        return result.toString();
    }

    /**
     * Converts into time.
     *
     * @param time
     *            the time
     *
     * @return the long
     */
    public static long toTime(final String time) {
        final var format1 = new SimpleDateFormat("MMM dd yyyy");
        final var format2 = new SimpleDateFormat("MMM dd HH:mm yyyy");
        final var pos = new ParsePosition(0);
        Date date;
        if ((time != null
                && (date = format2.parse(time + " " + new GregorianCalendar().get(Calendar.YEAR), pos)) != null)
                || (time != null && (date = format1.parse(time, pos)) != null)) {
            return date.getTime();
        }
        return -1;
    }

    /**
     * Converts into long.
     *
     * @param format
     *            the format
     * @param time
     *            the time
     *
     * @return the long
     */
    public static long toTime(final String format, final String time) {
        final var sdf = new SimpleDateFormat(format);
        final var pos = new ParsePosition(0);
        final Date date;
        if (time != null && (date = sdf.parse(time, pos)) != null) {
            return date.getTime();
        }
        return -1;
    }

    /**
     * Converts into xml value.
     *
     * @param target
     *            the target
     *
     * @return the string
     */
    public static String toXMLValue(final String target) {
        final var result = new StringBuilder();
        for (var i = 0; i < target.length(); i++) {
            final var c = target.charAt(i);
            result.append(Character.isLetterOrDigit(c) || c == ' ' || c == ',' || c == '(' || c == ')' || c == '#'
                    || c == '=' || c == '*' ? String.valueOf(c) : "%" + (int) c + ";");
        }
        return result.toString();
    }

    /**
     * Trim string.
     *
     * @param message
     *            the message
     * @param empty
     *            the empty
     *
     * @return the string
     */
    public static String trimString(final String message, final String empty) {
        return message == null || message.isEmpty() ? empty
                : message.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ').trim();
    }

    /**
     * Converts into hexa.
     *
     * @param bytes
     *            the bytes
     *
     * @return the string
     */
    public static String toHexa(final byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        final var s = new StringBuilder(bytes.length * 2);
        for (final byte element : bytes) {
            s.append(HEXA_LOOKUP[element >>> 4 & 0x0f]);
            s.append(HEXA_LOOKUP[element & 0x0f]);
        }
        return s.toString();
    }

    /**
     * Return a hash of the string.
     *
     * @param bytes
     *            the bytes
     *
     * @return the string
     */
    public static String getHash(final byte[] bytes) {
        try {
            return toHexa(MessageDigest.getInstance("SHA3-256").digest(bytes));
        } catch (final NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * Converts into bytes.
     *
     * @param hexa
     *            the hexa
     *
     * @return the byte[]
     *
     * @throws java.lang.NumberFormatException
     *             the number format exception
     */
    public static byte[] toBytes(final String hexa) throws NumberFormatException {
        if (hexa == null) {
            return null;
        }
        final var bytes = new byte[hexa.length() / 2];
        for (var i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hexa.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

    /**
     * Converts into bytes.
     *
     * @param object
     *            the object
     *
     * @return the byte[]
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static byte[] toBytes(final Object object) throws IOException {
        if (object == null) {
            return null;
        }
        final var ostream = new ByteArrayOutputStream();
        final var p = new ObjectOutputStream(ostream);
        p.writeObject(object);
        p.flush();
        return ostream.toByteArray();
    }

    /**
     * Converts into object.
     *
     * @param bytes
     *            the bytes
     *
     * @return the object
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws java.lang.ClassNotFoundException
     *             the class not found exception
     */
    public static Object toObject(final byte[] bytes) throws IOException, ClassNotFoundException {
        if (bytes == null) {
            return null;
        }
        final var istream = new ByteArrayInputStream(bytes);
        final var p = new ObjectInputStream(istream);
        final var obj = p.readObject();
        istream.close();
        return obj;
    }

    /**
     * Gets the host address.
     *
     * @param host
     *            the host
     *
     * @return the host address
     */
    public static String getHostAddress(final String host) {
        try {
            return InetAddress.getByName(host).getHostAddress();
        } catch (final UnknownHostException e) {
            return host;
        }
    }

    /**
     * Gets the host name.
     *
     * @param host
     *            the host
     *
     * @return the host name
     */
    public static String getHostName(final String host) {
        try {
            return InetAddress.getByName(host).getHostName();
        } catch (final UnknownHostException e) {
            return host;
        }
    }

    /**
     * Gets the parameters.
     *
     * @param message
     *            the message
     * @param token
     *            the token
     *
     * @return the parameters
     */
    public static String[] getParameters(final String message, final String token) {
        final var tokenizer = new StringTokenizer(message, token);
        final var size = tokenizer.countTokens();
        final var result = new String[size];
        for (var i = 0; i < size; i++) {
            result[i] = tokenizer.nextToken();
        }
        return result;
    }

    /**
     * Gets the message.
     *
     * @param t
     *            the t
     * @param defaultMessage
     *            the default message
     *
     * @return the message
     */
    public static String getLastMessage(final Throwable t, final String defaultMessage) {
        if (t == null) {
            return defaultMessage;
        }
        final var cause = t.getCause();
        if (cause != null) {
            return getLastMessage(cause, defaultMessage);
        } else {
            final var message = t.getMessage();
            return isNotEmpty(message) ? message : defaultMessage;
        }
    }

    /**
     * Gets the message.
     *
     * @param t
     *            the t
     * @param defaultMessage
     *            the default message
     * @param limit
     *            the limit
     *
     * @return the message
     */
    public static String getMessage(Throwable t, final String defaultMessage, final int limit) {
        // The original message
        var m = t == null || t instanceof NullPointerException ? "Internal error" : t.getMessage();
        // Are there causes to this exception?
        Throwable c;
        // Let's go through all the causes. If we have a DataBaseException then
        // we stop there as the following exceptions are not relevant for the
        // user!
        String p = null;
        while (t != null && (c = t.getCause()) != null && !(t instanceof DataBaseException)) {
            // Can we get a message from the cause exception?
            var cm = c instanceof NullPointerException ? "Internal error" : c.getMessage();
            if (cm != null && (cm = cm.trim()).length() > 0) {
                // There is indeed a message. If the message is surrounded by
                // parenthesis let's remove them!
                if (cm.startsWith("(") && cm.endsWith(")")) {
                    cm = cm.substring(1, cm.length() - 1).trim();
                }
                if (m != null && (m = m.trim()).length() > 0) {
                    // Make sure we don't have duplicated messages
                    if (!cm.equalsIgnoreCase(p)) {
                        // Make sure we don't have the last message which ends
                        // with this message: e1 (e2) <- e2
                        if (m.toLowerCase().endsWith("(" + cm.toLowerCase() + ")")) {
                            // Remove the duplicate!
                            m = m.substring(0, m.length() - (cm.length() + 2)).trim();
                        }
                        // Let's add it in addition to the original message
                        // (except if we would have e1 <- e1)!
                        if (!m.toLowerCase().endsWith(cm.toLowerCase())) {
                            m += " <- " + cm.substring(0, 1).toUpperCase() + cm.substring(1);
                            p = cm;
                        }
                    }
                } else {
                    // There is no original message so let's use it instead!
                    p = m = cm;
                }
            }
            // Move to the next cause!
            t = c;
        }
        // Use the message or the name of the class if there is no message. If there is
        // a message then we make sure there are no special characters in it. Also,
        // remove non-ASCII characters and sequences of space (just one)!
        final var result = (m != null && (m = m.replaceAll("[\n\r]", "").trim()).length() > 0
                ? m.substring(0, 1).toUpperCase() + m.substring(1)
                : defaultMessage != null ? defaultMessage : getClassName(t)).replaceAll("[^\\x00-\\x7F]", "").trim()
                        .replaceAll(" +", " ");
        // Do we have to limit the size of the message?
        if (limit <= 0 || result.length() <= limit) {
            return result;
        }
        // We have to shorten the message
        if (limit > 6) {
            return result.substring(0, limit - 6).trim() + " (...)";
        } else {
            return result.substring(0, limit);
        }
    }

    /**
     * Gets the message.
     *
     * @param t
     *            the t
     *
     * @return the message
     */
    public static String getMessage(final Throwable t) {
        return getMessage(t, null, 0);
    }

    /**
     * Creates a {@link RemoteException} containing a concise message and the local hostname, intended for use across
     * RMI boundaries. The stack trace is explicitly cleared to reduce native memory usage and minimize data transfer
     * during remote exception serialization.
     *
     * @param message
     *            the message
     *
     * @return a {@link RemoteException} with a trimmed stack trace and formatted message
     */
    public static RemoteException getRemoteException(final String message) {
        _log.warn(message);
        var e = new RemoteException("[" + SocketConfig.getLocalAddress() + "]: " + message);
        e.setStackTrace(new StackTraceElement[0]);
        return e;
    }

    /**
     * Creates a {@link RemoteException} containing a concise message and the local hostname, intended for use across
     * RMI boundaries. The stack trace is explicitly cleared to reduce native memory usage and minimize data transfer
     * during remote exception serialization.
     *
     * @param message
     *            the message
     * @param t
     *            the original {@link Throwable} to extract the message from
     *
     * @return a {@link RemoteException} with a trimmed stack trace and formatted message
     */
    public static RemoteException getRemoteException(final String message, final Throwable t) {
        return getRemoteException(message + " <- " + getMessage(t, null, 0));
    }

    /**
     * Creates a {@link RemoteException} containing a concise message and the local hostname, intended for use across
     * RMI boundaries. The stack trace is explicitly cleared to reduce native memory usage and minimize data transfer
     * during remote exception serialization.
     *
     * @param t
     *            the original {@link Throwable} to extract the message from
     *
     * @return a {@link RemoteException} with a trimmed stack trace and formatted message
     */
    public static RemoteException getRemoteException(final Throwable t) {
        return getRemoteException("Exception in RMI call", t);
    }

    /**
     * Decode.
     *
     * @param s
     *            the s
     *
     * @return the string
     *
     * @throws java.io.UnsupportedEncodingException
     *             the unsupported encoding exception
     */
    public static String decode(final String s) throws UnsupportedEncodingException {
        var needToChange = false;
        final var sb = new StringBuilder();
        final var numChars = s.length();
        var i = 0;
        while (i < numChars) {
            var c = s.charAt(i);
            switch (c) {
            case '+':
                sb.append(' ');
                i++;
                needToChange = true;
                break;
            case '%':
                try {
                    final var bytes = new byte[(numChars - i) / 3];
                    var pos = 0;
                    while (i + 2 < numChars && c == '%') {
                        bytes[pos++] = (byte) Integer.parseInt(s.substring(i + 1, i + 3), 16);
                        i += 3;
                        if (i < numChars) {
                            c = s.charAt(i);
                        }
                    }
                    if (i < numChars && c == '%') {
                        throw new IllegalArgumentException("URLDecoder: Incomplete trailing escape (%) pattern");
                    }
                    sb.append(new String(bytes, 0, pos, _dfltEncName));
                } catch (final NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "URLDecoder: Illegal hex characters in escape (%) pattern - " + e.getMessage());
                }
                needToChange = true;
                break;
            default:
                sb.append(c);
                i++;
                break;
            }
        }
        return needToChange ? sb.toString() : s;
    }

    /**
     * Gets the class name.
     *
     * @param object
     *            the object
     *
     * @return the class name
     */
    public static String getClassName(final Object object) {
        if (object == null) {
            return null;
        }
        final var className = object instanceof Class ? ((Class<?>) object).getName() : object.getClass().getName();
        return className.substring(className.lastIndexOf('.') + 1);
    }

    /**
     * The Class DuplicatedChooseScore.
     */
    public static final class DuplicatedChooseScore extends Exception {
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = -2675503634979160345L;

        /** The _score. */
        final int _score;

        /**
         * Instantiates a new duplicated choose score.
         *
         * @param score
         *            the score
         */
        DuplicatedChooseScore(final int score) {
            _score = score;
        }

        /**
         * Gets the score.
         *
         * @return the score
         */
        public int getScore() {
            return _score;
        }
    }

    /**
     * Find the best score to select the line. If no match is found return the first line.
     *
     * /data1/ecmwf/atmos/ ($dataTransfer[target].=DA) /data1/ecmwf/atmos/DA/ ($dataTransfer[target].=DW)
     * /data1/ecmwf/atmos/DW/ ($dataTransfer[target].=EA) /data1/ecmwf/atmos/EA/ ($dataTransfer[target].=EW)
     * /data1/ecmwf/atmos/EW/ ($dataTransfer[target].=S1) /data1/ecmwf/atmos/S1/ ($dataTransfer[target].=M1)
     * /data1/ecmwf/atmos/M1/ ($dataTransfer[target].=T1) /data1/ecmwf/atmos/T1/
     *
     * @param options
     *            the options to choose from.
     *
     * @return the selection
     *
     * @throws ecmwf.common.text.Format.DuplicatedChooseScore
     *             the duplicated choose score
     * @throws javax.script.ScriptException
     *             the script exception
     */
    public static String choose(final String options) throws DuplicatedChooseScore, ScriptException {
        final var chooseFrom = options.trim();
        if (chooseFrom.startsWith("$(") && chooseFrom.endsWith(")")) {
            // This is a script so we just evaluate it and use the result string!
            return choose(ScriptManager.exec(String.class, ScriptManager.JS,
                    chooseFrom.substring(2, chooseFrom.length() - 1)));
        }
        final var reader = new BufferedReader(new StringReader(chooseFrom));
        var highScore = 0;
        var duplicated = false;
        String path = null;
        String line;
        int index;
        try {
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("(") && (index = line.indexOf(")")) != -1) {
                    final var currentPath = line.substring(index + 1).trim();
                    var score = 0;
                    final var tokenizer = new StringTokenizer(line.substring(1, index), ";,");
                    while (tokenizer.hasMoreElements()) {
                        final var token = tokenizer.nextToken();
                        if ((index = token.indexOf("==")) != -1) {
                            final var part1 = token.substring(0, index).trim();
                            var part2 = token.substring(index + 2).trim();
                            var matchFound = false;
                            if (part2.startsWith("{") && part2.endsWith("}")) {
                                part2 = part2.substring(1, part2.length() - 1);
                                try {
                                    matchFound = part1.matches(part2);
                                } catch (final PatternSyntaxException e) {
                                    _log.warn("Pattern matching {} -> {}", part1, part2, e);
                                    matchFound = false;
                                }
                            } else {
                                matchFound = part1.equals(part2);
                            }
                            score += matchFound ? 1 : 0;
                        } else {
                            if ((index = token.indexOf("!=")) != -1) {
                                score += !token.substring(0, index).trim().equals(token.substring(index + 2).trim()) ? 1
                                        : 0;
                            } else {
                                if ((index = token.indexOf(".=")) != -1) {
                                    score += token.substring(0, index).trim()
                                            .startsWith(token.substring(index + 2).trim()) ? 1 : 0;
                                } else {
                                    if ((index = token.indexOf("=.")) != -1) {
                                        score += token.substring(0, index).trim()
                                                .endsWith(token.substring(index + 2).trim()) ? 1 : 0;
                                    }
                                }
                            }
                        }
                    }
                    if ((score > 0 && score >= highScore) && !(duplicated = score == highScore)) {
                        highScore = score;
                        path = currentPath;
                    }
                } else if (line.trim().length() > 0) {
                    path = line;
                }
            }
        } catch (final IOException e) {
            _log.warn(e);
        }
        if (duplicated) {
            throw new DuplicatedChooseScore(highScore);
        }
        return path;
    }

    /**
     * The options are in the following format: (.= /readme.txt) standby=yes (== {avhrr_n.*}) standby=never;delay=2h And
     * this method looks for the first line which apply to the selected key. For example, if the key is /etc/readme.txt
     * then the first line will be selected and the method will return "standby=yes". The operator for comparison are
     * the following: ".=" starts with; "==" equals to, if the second part is between {} then it is considered as a
     * regular expression (regex); "=." ends with; "!=" different than.
     *
     * @param key
     *            the key to look for.
     * @param options
     *            the options to choose from.
     *
     * @return the selection or null if nothing is found
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static String choose(final String key, final String options) throws IOException {
        // Let's read the list line be line and see if we can find an entry
        // for the current Host!
        final var reader = new BufferedReader(new StringReader(options));
        String result = null;
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            result = line;
            if (line.length() >= 6) {
                final var first = line.indexOf("(");
                final var last = line.indexOf(")");
                if (first == 0 && last != -1 && last - first > 3) {
                    // We found an entry with an operator so let's see if we
                    // have found the correct line?
                    final var operator = line.substring(first + 1, last).trim();
                    final var comp = operator.substring(2).trim();
                    // If the value is in {} then with the "==" operator it
                    // is considered as a regular expression!
                    if (operator.startsWith("==")
                            && (comp.startsWith("{") && comp.endsWith("}")
                                    && key.matches(comp.substring(1, comp.length() - 1)) || key.equals(comp))
                            || operator.startsWith("!=") && !key.equals(comp)
                            || operator.startsWith(".=") && key.startsWith(comp)
                            || operator.startsWith("=.") && key.endsWith(comp)) {
                        // We found the line!
                        result = line.substring(last + 1).trim();
                        break;
                    }
                    // Not the correct line, make sure we won't use it!
                    result = null;
                }
            } else // Bad format for the line, so let's skip it
            if (line.length() > 0) {
                _log.warn("Skipping entry: {}", line);
            }
        }
        return result;
    }

    /**
     * Object to string.
     *
     * @param object
     *            the object
     *
     * @return the string
     */
    public static String objectToString(final Object object) {
        if (object instanceof Boolean || object instanceof Long || object instanceof Integer || object instanceof Double
                || object instanceof Float || object instanceof String || object instanceof Date
                || !(object instanceof Serializable)) {
            return String.valueOf(object);
        }
        final Class<?> c = object.getClass();
        final var result = new StringBuilder();
        final var theMethods = c.getMethods();
        for (var i = 0; i < theMethods.length; i++) {
            final var method = theMethods[i].getName();
            if (!(method.startsWith("get") && method.length() > 3 && theMethods[i].getParameterTypes() != null
                    && theMethods[i].getParameterTypes().length == 0)) {
                continue;
            }
            try {
                result.append(i == 0 ? "" : " ").append(Character.toLowerCase(method.charAt(3)))
                        .append(method.substring(4)).append("=[").append(theMethods[i].invoke(object)).append("]");
            } catch (final Exception e) {
                _log.debug(method, e);
            }
        }
        return result.toString();
    }

    /**
     * Clean the path by removing all occurrences of "///" by "/", all "\" by "/" and all trailing "/".
     *
     * @param path
     *            the path
     *
     * @return the string
     */
    public static String getCleanPath(final String path) {
        return path.replaceAll("\\\\+", "/").replaceAll("/+", "/").replaceAll("/+$", "");
    }

    /**
     * Normalize path.
     *
     * @param path
     *            the path
     *
     * @return the string
     *
     * @throws java.io.FileNotFoundException
     *             the file not found exception
     */
    public static String normalizePath(final String path) throws FileNotFoundException {
        return normalizePath(path, false);
    }

    /**
     * Normalize path.
     *
     * @param path
     *            the path
     * @param keepEndTrailer
     *            the keep end trailer
     *
     * @return the string
     *
     * @throws FileNotFoundException
     *             the file not found exception
     */
    private static String normalizePath(final String path, final boolean keepEndTrailer) throws FileNotFoundException {
        final List<String> array = new ArrayList<>();
        var index = 0;
        final var token = new StringTokenizer(path.replace('\\', '/'), "/");
        while (token.hasMoreElements()) {
            final var value = token.nextToken();
            if (!".".equals(value)) {
                if ("..".equals(value)) {
                    if (--index < 0) {
                        throw new FileNotFoundException(path);
                    }
                    array.remove(index);
                } else {
                    array.add(index++, value);
                }
            }
        }
        final var result = new StringBuilder();
        for (final String element : array) {
            result.append("/").append(element);
        }
        return (result.isEmpty() ? "/" : result.toString())
                + (keepEndTrailer && (path.endsWith("/") || path.endsWith("\\")) ? "/" : "");
    }

    /**
     * Compress.
     *
     * @param str
     *            the str
     *
     * @return the string
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static String compress(final String str) throws IOException {
        if (str == null || str.isEmpty()) {
            return str;
        }
        final var out = new ByteArrayOutputStream();
        final var gzip = new GZIPOutputStream(out, Deflater.BEST_COMPRESSION);
        gzip.write(str.getBytes());
        gzip.close();
        return toHexa(out.toByteArray());
    }

    /**
     * Uncompress.
     *
     * @param str
     *            the str
     *
     * @return the string
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static String uncompress(final String str) throws IOException {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return uncompress(toBytes(str));
    }

    /**
     * Uncompress.
     *
     * @param bytes
     *            the bytes
     *
     * @return the string
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static String uncompress(final byte[] bytes) throws IOException {
        if (bytes == null) {
            return null;
        }
        final var result = new StringBuilder();
        final var in = new ByteArrayInputStream(bytes);
        final var gzip = new GZIPInputStream(in);
        final var array = new byte[256];
        int read;
        while ((read = gzip.read(array)) != -1) {
            result.append(new String(array, 0, read, "ISO-8859-1"));
        }
        return result.toString();
    }

    /**
     * Converts into string.
     *
     * @param str
     *            the str
     * @param maxWidth
     *            the max width
     *
     * @return the string
     */
    private static String _toString(final String str, final int maxWidth) {
        return str != null && str.length() > maxWidth ? str.substring(0, maxWidth) : str;
    }

    /**
     * Clean text content.
     *
     * @param text
     *            the text
     *
     * @return the string
     */
    public static String cleanTextContent(final String text) {
        // strips off all non-ASCII characters, erases all the ASCII control characters
        // and removes non-printable characters from Unicode.
        return text.replaceAll("[^\\x00-\\x7F]", "").replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").replaceAll("\\p{C}", "")
                .trim();
    }

    /**
     * Gets the ftp list.
     *
     * @param permission
     *            the permission
     * @param user
     *            the user
     * @param group
     *            the group
     * @param size
     *            the size
     * @param time
     *            the time
     * @param name
     *            the name
     *
     * @return the ftp list
     */
    public static String getFtpList(final String permission, final String user, final String group, final String size,
            final long time, final String name) {
        final var sb = new StringBuilder();
        final var fmt = new Formatter(sb);
        fmt.format("%s    1 %-8s %-8s", permission, _toString(user, 8), _toString(group, 8));
        if (size != null && size.length() > 10) {
            fmt.format(" %s", size);
        } else {
            fmt.format(" %10s", _toString(size, 10));
        }
        fmt.format(" %-12s %s", formatTime(time), name);
        fmt.close();
        return sb.toString();
    }

    /**
     * _matches.
     *
     * @param pattern
     *            the pattern
     * @param text
     *            the text
     *
     * @return true, if successful
     */
    public static boolean matches(final String pattern, final String text) {
        // add sentinel so don't need to worry about *'s at end of pattern
        final var aText = text + '\0';
        final var aPattern = pattern + '\0';
        final var N = aPattern.length();
        var states = new boolean[N + 1];
        var old = new boolean[N + 1];
        old[0] = true;
        for (var i = 0; i < aText.length(); i++) {
            final var c = aText.charAt(i);
            states = new boolean[N + 1]; // Initialised to false
            for (var j = 0; j < N; j++) {
                final var p = aPattern.charAt(j);
                // hack to handle *'s that match 0 characters
                if (old[j] && p == '*') {
                    old[j + 1] = true;
                }
                if (old[j] && p == c) {
                    states[j + 1] = true;
                }
                if (old[j] && p == '?') {
                    states[j + 1] = true;
                }
                if (old[j] && p == '*') {
                    states[j] = true;
                }
                if (old[j] && p == '*') {
                    states[j + 1] = true;
                }
            }
            old = states;
        }
        return states[N];
    }

    /**
     * Extract search parameters.
     *
     * This method is extracting the parameters in the following form: "param1=value1 param2=space\\ char
     * param3=equal\\=char" (the ' ' and = characters can be escaped)
     *
     * @param defaultOptionName
     *            the default option name
     * @param input
     *            the input
     *
     * @return the map
     */
    public static Map<String, String> extractSearchParameters(final String defaultOptionName, final String input) {
        final Map<String, String> paramsMap = new HashMap<>();
        if (input != null && !input.isEmpty()) {
            final var key = new StringBuilder();
            final var value = new StringBuilder();
            var isKey = true;
            var isEscaped = false;
            for (final char c : input.toCharArray()) {
                if (isEscaped) {
                    value.append(c);
                    isEscaped = false;
                } else if (c == '\\') {
                    isEscaped = true;
                } else if (isKey && c == '=') {
                    isKey = false;
                } else if (c == ' ' && !isKey) {
                    paramsMap.put(key.toString().trim().toLowerCase(), value.toString().trim());
                    key.setLength(0);
                    value.setLength(0);
                    isKey = true;
                } else if (isKey) {
                    key.append(c);
                } else {
                    value.append(c);
                }
            }
            if (paramsMap.isEmpty() && !key.isEmpty() && value.isEmpty()) {
                // No parameter found, so we set the default parameter with the content of the
                // input string
                paramsMap.put(defaultOptionName.toLowerCase(), input.trim());
            } else if (!key.isEmpty()) {
                paramsMap.put(key.toString().trim().toLowerCase(), value.toString().trim());
            }
        }
        return paramsMap;
    }

    /**
     * Matches glob pattern.
     *
     * This method uses the PathMatcher interface with the glob syntax to perform matching based on Unix-style wildcard
     * patterns ('*' and '?').
     *
     * @param pattern
     *            the pattern
     * @param input
     *            the input
     *
     * @return true, if successful
     */
    public static boolean matchesGlobPattern(final String pattern, final String input) {
        return FileSystems.getDefault().getPathMatcher("glob:" + pattern).matches(Paths.get(input));
    }
}
