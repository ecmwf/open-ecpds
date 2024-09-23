/**
 * A BASE64 Encoder/Decoder.
 *
 * <p>
 * This class is used to encode and decode data in BASE64 format as described in RFC 1521.
 *
 * <p>
 * This is "Open Source" software and released under the <a href="http://www.gnu.org/licenses/lgpl.html">GNU/LGPL</a> license.<br>
 * It is provided "as is" without warranty of any kind.<br>
 * Copyright 2003: Christian d'Heureuse, Inventec Informatik AG, Switzerland.<br>
 * Home page: <a href="http://www.source-code.biz">www.source-code.biz</a><br>
 *
 * <p>
 * Version history:<br>
 * 2003-07-22 Christian d'Heureuse (chdh): Module created.<br>
 * 2005-08-11 chdh: Lincense changed from GPL to LGPL.<br>
 * 2006-11-21 chdh:<br>
 *  &nbsp; Method encode(String) renamed to encodeString(String).<br>
 *  &nbsp; Method decode(String) renamed to decodeString(String).<br>
 *  &nbsp; New method encode(byte[],int) added.<br>
 *  &nbsp; New method decode(String) added.<br>
 */

package ecmwf.common.text;

/**
 * The Class BASE64Coder.
 *
 * @author root
 */
public class BASE64Coder {

    /** Mapping table from 6-bit nibbles to BASE64 characters. */
    private static char[] map1 = new char[64];
    static {
        var i = 0;
        for (var c = 'A'; c <= 'Z'; c++) {
            map1[i++] = c;
        }
        for (var c = 'a'; c <= 'z'; c++) {
            map1[i++] = c;
        }
        for (var c = '0'; c <= '9'; c++) {
            map1[i++] = c;
        }
        map1[i++] = '+';
        map1[i++] = '/';
    }

    /** Mapping table from BASE64 characters to 6-bit nibbles. */
    private static byte[] map2 = new byte[128];
    static {
        for (var i = 0; i < map2.length; i++) {
            map2[i] = -1;
        }
        for (var i = 0; i < 64; i++) {
            map2[map1[i]] = (byte) i;
        }
    }

    /**
     * Encodes a string into BASE64 format. No blanks or line breaks are inserted.
     *
     * @param s
     *            a String to be encoded.
     *
     * @return A String with the BASE64 encoded data.
     */
    public static String encodeString(final String s) {
        return new String(encode(s.getBytes()));
    }

    /**
     * Encodes a byte array into BASE64 format. No blanks or line breaks are inserted.
     *
     * @param in
     *            an array containing the data bytes to be encoded.
     *
     * @return A character array with the BASE64 encoded data.
     */
    public static char[] encode(final byte[] in) {
        return encode(in, in.length);
    }

    /**
     * Encodes a byte array into BASE64 format. No blanks or line breaks are inserted.
     *
     * @param in
     *            an array containing the data bytes to be encoded.
     * @param iLen
     *            number of bytes to process in in.
     *
     * @return A character array with the BASE64 encoded data.
     */
    public static char[] encode(final byte[] in, final int iLen) {
        final var oDataLen = (iLen * 4 + 2) / 3; // output length without padding
        final var oLen = (iLen + 2) / 3 * 4; // output length including padding
        final var out = new char[oLen];
        var ip = 0;
        var op = 0;
        while (ip < iLen) {
            final var i0 = in[ip++] & 0xff;
            final var i1 = ip < iLen ? in[ip++] & 0xff : 0;
            final var i2 = ip < iLen ? in[ip++] & 0xff : 0;
            final var o0 = i0 >>> 2;
            final var o1 = (i0 & 3) << 4 | i1 >>> 4;
            final var o2 = (i1 & 0xf) << 2 | i2 >>> 6;
            final var o3 = i2 & 0x3F;
            out[op++] = map1[o0];
            out[op++] = map1[o1];
            out[op] = op < oDataLen ? map1[o2] : '=';
            op++;
            out[op] = op < oDataLen ? map1[o3] : '=';
            op++;
        }
        return out;
    }

    /**
     * Decodes a string from BASE64 format.
     *
     * @param s
     *            a BASE64 String to be decoded.
     *
     * @return A String containing the decoded data.
     *
     * @throws java.lang.IllegalArgumentException
     *             if the input is not valid BASE64 encoded data.
     */
    public static String decodeString(final String s) {
        return new String(decode(s));
    }

    /**
     * Decodes a byte array from BASE64 format.
     *
     * @param s
     *            a BASE64 String to be decoded.
     *
     * @return An array containing the decoded data bytes.
     *
     * @throws java.lang.IllegalArgumentException
     *             if the input is not valid BASE64 encoded data.
     */
    public static byte[] decode(final String s) {
        return decode(s.toCharArray());
    }

    /**
     * Decodes a byte array from BASE64 format. No blanks or line breaks are allowed within the BASE64 encoded data.
     *
     * @param in
     *            a character array containing the BASE64 encoded data.
     *
     * @return An array containing the decoded data bytes.
     *
     * @throws java.lang.IllegalArgumentException
     *             if the input is not valid BASE64 encoded data.
     */
    public static byte[] decode(final char[] in) {
        var iLen = in.length;
        if (iLen % 4 != 0) {
            throw new IllegalArgumentException("Length of BASE64 encoded input string is not a multiple of 4.");
        }
        while (iLen > 0 && in[iLen - 1] == '=') {
            iLen--;
        }
        final var oLen = iLen * 3 / 4;
        final var out = new byte[oLen];
        var ip = 0;
        var op = 0;
        while (ip < iLen) {
            final int i0 = in[ip++];
            final int i1 = in[ip++];
            final int i2 = ip < iLen ? in[ip++] : 'A';
            final int i3 = ip < iLen ? in[ip++] : 'A';
            if (i0 > 127 || i1 > 127 || i2 > 127 || i3 > 127) {
                throw new IllegalArgumentException("Illegal character in BASE64 encoded data.");
            }
            final int b0 = map2[i0];
            final int b1 = map2[i1];
            final int b2 = map2[i2];
            final int b3 = map2[i3];
            if (b0 < 0 || b1 < 0 || b2 < 0 || b3 < 0) {
                throw new IllegalArgumentException("Illegal character in BASE64 encoded data.");
            }
            final var o0 = b0 << 2 | b1 >>> 4;
            final var o1 = (b1 & 0xf) << 4 | b2 >>> 2;
            final var o2 = (b2 & 3) << 6 | b3;
            out[op++] = (byte) o0;
            if (op < oLen) {
                out[op++] = (byte) o1;
            }
            if (op < oLen) {
                out[op++] = (byte) o2;
            }
        }
        return out;
    }

    /**
     * Instantiates a new BASE 64 coder. Dummy constructor.
     */
    private BASE64Coder() {
    }

}
