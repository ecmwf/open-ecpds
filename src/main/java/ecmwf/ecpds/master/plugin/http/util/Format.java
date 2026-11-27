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

/*
 * File: Format.java
 * Package: ecmwf.common.text
 * Project: ECaccess
 *
 * copyright (c) ECMWF - 2003
 */

package ecmwf.ecpds.master.plugin.http.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import javax.management.timer.Timer;

/**
 * DOCUMENT ME!
 *
 * @author ECMWF (syi@ecmwf.int)
 *
 * @version v2.0.3 Modified by sy8 to include into dynamic web
 */
public class Format {
    // ~ Static fields/initializers
    // ---------------------------------------------

    /** DOCUMENT ME! */
    // private static Log _log = (Log) Log.getLogger(Format.class.getName());
    private static Log log = LogFactory.getLog(Format.class);

    /** DOCUMENT ME! */
    private static int _thisYear = new GregorianCalendar().get(GregorianCalendar.YEAR);

    /** DOCUMENT ME! */
    private static final byte[] CRLFb = { (byte) 0x0d, (byte) 0x0a };

    /** DOCUMENT ME! */
    private static String _address = null;

    /** DOCUMENT ME! */
    private static BitSet _dontNeedEncoding;

    /** DOCUMENT ME! */
    private static final int _caseDiff = ('a' - 'A');

    /** DOCUMENT ME! */
    private static String _dfltEncName = null;

    /** DOCUMENT ME! */
    private static final char[] _hexaLookup = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f' };

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

        _dfltEncName = System.getProperty("file.encoding");
    }

    // ~ Constructors
    // -----------------------------------------------------------

    /**
     * Creates a new Format object.
     */
    public Format() {
        // Default constructor
    }

    // ~ Methods
    // ----------------------------------------------------------------

    /**
     * Formats a decimal number with the specified number of decimals.
     *
     * @param number
     *            The number to be formatted
     * @param decimals
     *            The number of decimals to display.
     *
     * @return A string with the formatted number
     */
    private static final String _formatNumber(double number, int decimals) {
        if (decimals == 0) {
            return String.valueOf((long) number);
        } else {
            return (long) number + "." + ((long) (number * (10 ^ decimals)) - ((long) number) * (10 ^ decimals));
        }
    }

    /**
     * Formats a value using units of bytes, kilobytes, megabytes or gigabytes.
     *
     * @param bytes
     *            The value to format
     *
     * @return A string with a decimal value follwed by bytes, Kb, Mb or Gb.
     */
    public static String formatSize(long bytes) {
        if (bytes >= 1073741824) // = 1024*1024*1024
        {
            double gb = (double) bytes / 1073741824;
            return _formatNumber(gb, 1) + " GB";
        } else if (bytes >= 1048576) // = 1024*1024
        {
            double mb = (double) bytes / 1048576;
            return _formatNumber(mb, 1) + " MB";
        } else if (bytes >= 1024) {
            double kb = (double) bytes / 1024;
            return _formatNumber(kb, 1) + " KB";
        } else {
            return bytes + " bytes";
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param date
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String formatTime(long date) {
        if (date <= 0) {
            return "-";
        }

        Date df = new Date(date);
        GregorianCalendar dt = new GregorianCalendar();
        dt.setTime(df);

        return (new SimpleDateFormat((_thisYear != dt.get(GregorianCalendar.YEAR)) ? "MMM dd  yyyy" : "MMM dd HH:mm"))
                .format(df);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String formatCurrentTime() {
        return formatTime(System.currentTimeMillis());
    }

    /**
     * DOCUMENT ME!
     *
     * @param date
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String formatTime(String format, long date) {
        if (date <= 0) {
            return "-";
        }

        Date df = new Date(date);
        GregorianCalendar dt = new GregorianCalendar();
        dt.setTime(df);

        return (new SimpleDateFormat(format).format(df));
    }

    /**
     * DOCUMENT ME!
     *
     * @param start
     *            DOCUMENT ME!
     * @param stop
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String formatDuration(long start, long stop) {
        return formatDuration(stop - start);
    }

    /**
     * DOCUMENT ME!
     *
     * @param duration
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String formatDuration(long duration) {
        if (duration < 1000) {
            return (duration < 0 ? 0 : duration) + "ms";
        }

        double seconds = ((double) duration) / ((double) 1000);
        long remaining = 0;
        String ext;

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

        String result = String.valueOf(seconds);
        int pos = result.indexOf('.');

        return (result.substring(0, (pos > -1) ? pos : result.length()) + ext
                + (remaining > 0 ? ' ' + formatDuration(remaining) : "")).trim();
    }

    /**
     * DOCUMENT ME!
     *
     * @param bool
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String formatBoolean(boolean bool) {
        return bool ? "yes" : "no";
    }

    /**
     * DOCUMENT ME!
     *
     * @param n
     *            DOCUMENT ME!
     * @param width
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String formatLong(long n, int width) {
        return formatLong(n, width, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param n
     *            DOCUMENT ME!
     * @param width
     *            DOCUMENT ME!
     * @param zero
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String formatLong(long n, int width, boolean zero) {
        StringBuffer num = new StringBuffer(Long.toString(n));
        int spaces = width - num.length();

        for (int i = 0; i < spaces; i++)
            num.insert(0, zero ? '0' : ' ');

        return num.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param size
     *            DOCUMENT ME!
     * @param start
     *            DOCUMENT ME!
     * @param stop
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String formatRate(long size, long start, long stop) {
        return formatRate(size, stop - start);
    }

    /**
     * DOCUMENT ME!
     *
     * @param size
     *            DOCUMENT ME!
     * @param duration
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String formatRate(long size, long duration) {
        String result = formatSize(size) + " in " + formatDuration(duration);

        if (duration == 0) {
            return result;
        }

        double rate = ((double) size) / (((double) duration) / ((double) 1000));

        return result + " (" + formatSize((long) rate) + "/sec)";
    }

    /**
     * DOCUMENT ME!
     *
     * @param size
     *            DOCUMENT ME!
     * @param duration
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String formatSimplifiedRate(long size, long duration) {
        if (duration <= 0) {
            return "-";
        }

        return formatSize((long) (((double) size) / (((double) duration) / ((double) 1000)))) + "/sec";
    }

    /**
     * DOCUMENT ME!
     *
     * @param size
     *            DOCUMENT ME!
     * @param start
     *            DOCUMENT ME!
     * @param stop
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String formatSimplifiedRate(long size, long start, long stop) {
        double duration = ((double) stop) - ((double) start);
        if (duration <= 0) {
            return "-";
        }

        return formatSize((long) (((double) size) / (duration / ((double) 1000)))) + "/sec";
    }

    /**
     * DOCUMENT ME!
     *
     * @param size
     *            DOCUMENT ME!
     * @param sent
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String formatProgress(long size, long sent) {
        return (sent >= size || size == 0 ? "100%"
                : ((int) ((((double) 100) * ((double) sent)) / ((double) size))) + "%");
    }

    /**
     * DOCUMENT ME!
     *
     * @param text
     *            DOCUMENT ME!
     * @param width
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String formatString(String text, int width) {
        return formatString(text, width, ' ');
    }

    /**
     * DOCUMENT ME!
     *
     * @param text
     *            DOCUMENT ME!
     * @param width
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String formatString(Object text, int width) {
        return formatString(String.valueOf(text), width, ' ', true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param text
     *            DOCUMENT ME!
     * @param width
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String formatString(Object text, int width, boolean onRight) {
        return formatString(String.valueOf(text), width, ' ', onRight);
    }

    /**
     * DOCUMENT ME!
     *
     * @param text
     *            DOCUMENT ME!
     * @param width
     *            DOCUMENT ME!
     * @param empty
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String formatString(String text, int width, char empty) {
        return formatString(text, width, empty, true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param text
     *            DOCUMENT ME!
     * @param width
     *            DOCUMENT ME!
     * @param empty
     *            DOCUMENT ME!
     * @param onRight
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String formatString(String text, int width, char empty, boolean onRight) {
        int spaces = width - text.length();

        for (int i = 0; i < spaces; i++)
            text = onRight ? (text + empty) : (empty + text);

        return text;
    }

    /**
     * DOCUMENT ME!
     *
     * @param parameters
     *            DOCUMENT ME!
     */
    /*
     * public static long getDuration(String duration) throws NumberFormatException { try { int length =
     * duration.length(); long value = Long.parseLong(duration.substring(0, length - 1));
     *
     * switch (duration.charAt(length - 1)) { case 'w' : return value * Timer.ONE_WEEK; case 'd' : return value *
     * Timer.ONE_DAY; case 'h' : return value * Timer.ONE_HOUR; case 'm' : return value * Timer.ONE_MINUTE; case 's' :
     * return value * Timer.ONE_SECOND; } } catch (Exception e) { }
     *
     * if (!duration.endsWith("h")) { try { return getDuration(duration + "h"); } catch (NumberFormatException e) { } }
     *
     * throw new NumberFormatException("Duration format error: " + duration); }
     */

    /**
     * DOCUMENT ME!
     *
     * @param text
     *            DOCUMENT ME!
     * @param width
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String formatValue(long value, int width) {
        return Format.formatString(String.valueOf(value), width, '0', false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param text
     *            DOCUMENT ME!
     * @param maxSize
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String trimString(String text, int maxSize) {
        return ((maxSize > 0) && (text != null) && (text.length() > maxSize)) ? text.substring(0, maxSize) : text;
    }

    /**
     * DOCUMENT ME!
     *
     * @param lastDate
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String formatUntil(long lastDate) {
        double newValue;
        String ext;

        if ((newValue = (double) lastDate) > -1) {
            newValue = (System.currentTimeMillis() - newValue) / 1000;

            if (newValue > 60) {
                newValue = newValue / 60;

                if (newValue > 60) {
                    return Format.formatTime(lastDate);
                } else {
                    ext = "minute" + ((newValue > 1) ? "s" : "");
                }
            } else {
                ext = "second" + ((newValue > 1) ? "s" : "");
            }
        } else {
            return null;
        }

        String result = "" + newValue;
        int pos = result.indexOf('.');

        return result.substring(0, (pos > -1) ? pos : result.length()) + ' ' + ext;
    }

    /**
     * DOCUMENT ME!
     *
     * @param target
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String fromXMLValue(String target) {
        int ind1;
        int ind2;
        String result = new String("");

        while (((ind1 = target.indexOf("%")) != -1) && ((ind2 = target.substring(ind1).indexOf(";")) != -1)) {
            int c = Integer.parseInt(target.substring(ind1 + 1, ind1 + ind2));
            result += (target.substring(0, ind1) + ((char) c));
            target = target.substring(ind1 + ind2 + 1);
        }

        return result + target;
    }

    /**
     * DOCUMENT ME!
     *
     * @param value
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String hide(Object value) {
        String newValue = new String();

        for (int i = 0; i < String.valueOf(value).length(); i++)
            newValue += "*";

        return newValue;
    }

    /**
     * DOCUMENT ME!
     *
     * @param data
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String windowsToUnix(String text) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] data = text.getBytes();

        for (int i = 0; i < data.length; i++) {
            byte c = (byte) data[i];

            if ((c == CRLFb[0]) && ((i + 1) < data.length) && ((byte) data[i + 1] == CRLFb[1])) {
                continue;
            } else {
                out.write((int) c);
            }
        }

        return new String(out.toByteArray());
    }

    /**
     * DOCUMENT ME!
     *
     * @param text
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String unixToWindows(String text) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(text.getBytes());
        BufferedReader dis = new BufferedReader(new InputStreamReader(in));
        String line;

        try {
            while ((line = dis.readLine()) != null) {
                out.write((line + "\r\n").getBytes());
            }
        } catch (IOException e) {
            log.debug(e);

            return text;
        }

        return new String(out.toByteArray());
    }

    /**
     * DOCUMENT ME!
     *
     * @param target
     *            DOCUMENT ME!
     * @param name
     *            DOCUMENT ME!
     * @param value
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String replace(String target, String name, Object value) {
        return replace(target, name, value, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param target
     *            DOCUMENT ME!
     * @param name
     *            DOCUMENT ME!
     * @param value
     *            DOCUMENT ME!
     * @param hide
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String replace(String target, String name, Object value, boolean hide) {
        int ind;

        value = value != null ? value : new String();

        if (hide) {
            value = hide(value);
        }

        while ((ind = target.toLowerCase().indexOf(name.toLowerCase())) != -1)
            target = target.substring(0, ind) + String.valueOf(value) + target.substring(ind + name.length());

        return target;
    }

    /**
     * DOCUMENT ME!
     *
     * @param time
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static long toTime(String time) {
        SimpleDateFormat format1 = new SimpleDateFormat("MMM dd yyyy");
        SimpleDateFormat format2 = new SimpleDateFormat("MMM dd HH:mm yyyy");
        ParsePosition pos = new ParsePosition(0);

        Date date;

        if ((time != null) && ((date = format2.parse(time + " " + _thisYear, pos)) != null)) {
            return date.getTime();
        } else {
            if ((time != null) && ((date = format1.parse(time, pos)) != null)) {
                return date.getTime();
            }
        }

        return -1;
    }

    /**
     * DOCUMENT ME!
     *
     * @param format
     *            DOCUMENT ME!
     * @param time
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static long toTime(String format, String time) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        ParsePosition pos = new ParsePosition(0);
        Date date;

        if ((time != null) && ((date = sdf.parse(time, pos)) != null)) {
            return date.getTime();
        }

        return -1;
    }

    /**
     * DOCUMENT ME!
     *
     * @param target
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String toXMLValue(String target) {
        String result = new String();

        for (int i = 0; i < target.length(); i++) {
            char c = target.charAt(i);

            result += ((Character.isLetterOrDigit(c) || (c == ' ') || (c == ',') || (c == '(') || (c == ')')
                    || (c == '#') || (c == '=') || (c == '*')) ? ("" + c) : ("%" + ((int) c) + ";"));
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param message
     *            DOCUMENT ME!
     * @param empty
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String trimString(String message, String empty) {
        return ((message == null) || (message.length() == 0)) ? empty
                : (((message.replace('\t', ' ')).replace('\n', ' ')).replace('\r', ' ')).trim();
    }

    /**
     * DOCUMENT ME!
     *
     * @param bytes
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String toHexa(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        StringBuffer s = new StringBuffer(bytes.length * 2);

        for (int i = 0; i < bytes.length; i++) {
            s.append(_hexaLookup[(bytes[i] >>> 4) & 0x0f]);
            s.append(_hexaLookup[bytes[i] & 0x0f]);
        }

        return s.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param hexa
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws java.lang.NumberFormatException
     *             DOCUMENT ME!
     */
    public static byte[] toBytes(String hexa) throws NumberFormatException {
        if (hexa == null) {
            return null;
        }

        byte[] bytes;

        bytes = new byte[hexa.length() / 2];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hexa.substring(2 * i, (2 * i) + 2), 16);
        }

        return bytes;
    }

    /**
     * DOCUMENT ME!
     *
     * @param object
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException
     *             DOCUMENT ME!
     * @throws ClassNotFoundException
     *             DOCUMENT ME!
     */
    public static byte[] toBytes(Object object) throws IOException, ClassNotFoundException {
        if (object == null) {
            return null;
        }

        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        ObjectOutputStream p = new ObjectOutputStream(ostream);
        p.writeObject(object);
        p.flush();

        return ostream.toByteArray();
    }

    /**
     * DOCUMENT ME!
     *
     * @param loader
     *            DOCUMENT ME!
     * @param bytes
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException
     *             DOCUMENT ME!
     * @throws ClassNotFoundException
     *             DOCUMENT ME!
     */
    public static Object toObject(ClassLoader loader, byte[] bytes) throws IOException, ClassNotFoundException {
        if (bytes == null) {
            return null;
        }

        ByteArrayInputStream istream = new ByteArrayInputStream(bytes);
        ObjectInputStream p = new _ObjectInputStream(loader, istream);
        Object obj = p.readObject();
        istream.close();

        return obj;
    }

    /**
     * DOCUMENT ME!
     *
     * @param bytes
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException
     *             DOCUMENT ME!
     * @throws ClassNotFoundException
     *             DOCUMENT ME!
     */
    public static Object toObject(byte[] bytes) throws IOException, ClassNotFoundException {
        if (bytes == null) {
            return null;
        }

        ByteArrayInputStream istream = new ByteArrayInputStream(bytes);
        ObjectInputStream p = new ObjectInputStream(istream);
        Object obj = p.readObject();
        istream.close();

        return obj;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String getLocalAddress() {
        if (_address == null) {
            try {
                _address = InetAddress.getLocalHost().getHostAddress();
            } catch (IOException e) {
                _address = "127.0.0.1";
            }
        }

        return _address;
    }

    /**
     * DOCUMENT ME!
     *
     * @param host
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String getHostAddress(String host) {
        try {
            return InetAddress.getByName(host).getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param message
     *            DOCUMENT ME!
     * @param token
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String[] getParameters(String message, String token) {
        StringTokenizer tokenizer = new StringTokenizer(message, token);
        int size = tokenizer.countTokens();
        String[] result = new String[size];

        for (int i = 0; i < size; i++) {
            result[i] = (String) tokenizer.nextElement();
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param message
     *            DOCUMENT ME!
     * @param token
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String getMessage(Exception e) {
        String message = e.getMessage();

        return getClassName(e) + (message != null ? ": " + message : "");
    }

    /**
     * DOCUMENT ME!
     *
     * @param s
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws UnsupportedEncodingException
     *             DOCUMENT ME!
     * @throws IllegalArgumentException
     *             DOCUMENT ME!
     */
    public static String decode(String s) throws UnsupportedEncodingException {
        boolean needToChange = false;
        StringBuffer sb = new StringBuffer();
        int numChars = s.length();
        int i = 0;

        while (i < numChars) {
            char c = s.charAt(i);

            switch (c) {
            case '+':
                sb.append(' ');
                i++;
                needToChange = true;

                break;

            case '%':

                try {
                    byte[] bytes = new byte[(numChars - i) / 3];
                    int pos = 0;

                    while (((i + 2) < numChars) && (c == '%')) {
                        bytes[pos++] = (byte) Integer.parseInt(s.substring(i + 1, i + 3), 16);
                        i += 3;

                        if (i < numChars) {
                            c = s.charAt(i);
                        }
                    }

                    if ((i < numChars) && (c == '%')) {
                        throw new IllegalArgumentException("URLDecoder: Incomplete trailing escape (%) pattern");
                    }

                    sb.append(new String(bytes, 0, pos, _dfltEncName));
                } catch (NumberFormatException e) {
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

        return (needToChange ? sb.toString() : s);
    }

    /**
     * DOCUMENT ME!
     *
     * @param object
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String getClassName(Object object) {
        if (object == null) {
            return null;
        }

        String className = (object instanceof Class) ? ((Class) object).getName() : object.getClass().getName();

        return className.substring(className.lastIndexOf('.') + 1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param object
     *            DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String objectToString(Object object) {
        if (object instanceof Boolean || object instanceof Long || object instanceof Integer || object instanceof Double
                || object instanceof Float || object instanceof String || object instanceof Date
                || !(object instanceof Serializable)) {
            return String.valueOf(object);
        }

        Class c = object.getClass();
        String result = new String();
        Method[] theMethods = c.getMethods();

        for (int i = 0; i < theMethods.length; i++) {
            String method = theMethods[i].getName();

            if (!(method.startsWith("get") && (method.length() > 3) && (theMethods[i].getParameterTypes() != null)
                    && (theMethods[i].getParameterTypes().length == 0))) {
                continue;
            }

            try {
                result += (((i == 0) ? "" : " ") + (Character.toLowerCase(method.charAt(3)) + method.substring(4) + "=["
                        + (String.valueOf(theMethods[i].invoke(object)) + "]")));
            } catch (Exception e) {
                log.debug(method, e);
            }
        }

        return result;
    }

    // ~ Inner Classes
    // ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @author ECMWF (syi@ecmwf.int)
     *
     * @version v2.0.2
     */
    private static class _ObjectInputStream extends ObjectInputStream {
        // ~ Instance fields
        // ----------------------------------------------------

        /** DOCUMENT ME! */
        private ClassLoader _loader;

        // ~ Constructors
        // -------------------------------------------------------

        /**
         * Creates a new _ObjectInputStream object.
         *
         * @param loader
         *            DOCUMENT ME!
         * @param in
         *            DOCUMENT ME!
         *
         * @throws IOException
         *             DOCUMENT ME!
         */
        _ObjectInputStream(ClassLoader loader, InputStream in) throws IOException {
            super(in);
            _loader = loader;
        }

        // ~ Methods
        // ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param desc
         *            DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         *
         * @throws IOException
         *             DOCUMENT ME!
         * @throws ClassNotFoundException
         *             DOCUMENT ME!
         */
        protected Class resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            return Class.forName(desc.getName(), false, _loader);
        }
    }
}