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

package ecmwf.common.technical;

/**
 * ECMWF Product Data Store (OpenECPDS) Project.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */
public class ByteSize {

    /** The Constant ZERO. */
    public static final ByteSize ZERO = new ByteSize(0);

    /** The Constant MAX_VALUE. */
    public static final ByteSize MAX_VALUE = new ByteSize(Long.MAX_VALUE);

    /** The Constant KB. */
    public static final int KB = 1024;

    /** The Constant MB. */
    public static final int MB = 1024 * 1024;

    /** The Constant GB. */
    public static final int GB = 1024 * 1024 * 1024;

    /** The Constant TB. */
    public static final int TB = 1024 * 1024 * 1024 * 1024;

    /** The Constant PB. */
    public static final int PB = 1024 * 1024 * 1024 * 1024 * 1024;

    /** The Constant EB. */
    public static final int EB = 1024 * 1024 * 1024 * 1024 * 1024 * 1024;

    /** The bytes count. */
    private final long bytesCount;

    /**
     * Of KB.
     *
     * @param bytes
     *            the bytes
     *
     * @return the byte size
     */
    public static ByteSize ofKB(final long bytes) {
        return new ByteSize(Math.multiplyExact(bytes, KB));
    }

    /**
     * Of MB.
     *
     * @param bytes
     *            the bytes
     *
     * @return the byte size
     */
    public static ByteSize ofMB(final long bytes) {
        return new ByteSize(Math.multiplyExact(bytes, MB));
    }

    /**
     * Of GB.
     *
     * @param bytes
     *            the bytes
     *
     * @return the byte size
     */
    public static ByteSize ofGB(final long bytes) {
        return new ByteSize(Math.multiplyExact(bytes, GB));
    }

    /**
     * Of TB.
     *
     * @param bytes
     *            the bytes
     *
     * @return the byte size
     */
    public static ByteSize ofTB(final long bytes) {
        return new ByteSize(Math.multiplyExact(bytes, TB));
    }

    /**
     * Of PB.
     *
     * @param bytes
     *            the bytes
     *
     * @return the byte size
     */
    public static ByteSize ofPB(final long bytes) {
        return new ByteSize(Math.multiplyExact(bytes, PB));
    }

    /**
     * Of EB.
     *
     * @param bytes
     *            the bytes
     *
     * @return the byte size
     */
    public static ByteSize ofEB(final long bytes) {
        return new ByteSize(Math.multiplyExact(bytes, EB));
    }

    /**
     * Instantiates a new byte size.
     *
     * @param bytesCount
     *            the bytes count
     */
    private ByteSize(final long bytesCount) {
        this.bytesCount = bytesCount;
    }

    /**
     * Size.
     *
     * @return the long
     */
    public long size() {
        return bytesCount;
    }

    /**
     * Bits.
     *
     * @return the long
     */
    public long bits() {
        return bytesCount * 8;
    }

    /**
     * Of.
     *
     * @param bytesCount
     *            the bytes count
     *
     * @return the byte size
     */
    public static ByteSize of(final long bytesCount) {
        return new ByteSize(bytesCount);
    }

    /**
     * Parses the.
     *
     * @param bytesCount
     *            the bytes count
     *
     * @return the byte size
     */
    public static ByteSize parse(final String bytesCount) {
        return new ByteSize(parseByteSize(bytesCount));
    }

    /**
     * Checks if is zero.
     *
     * @return true, if is zero
     */
    public boolean isZero() {
        return bytesCount == 0;
    }

    /**
     * {@inheritDoc}
     *
     * To string.
     */
    @Override
    public String toString() {
        final String displaySize;
        if (bytesCount == Long.MAX_VALUE) {
            displaySize = "max-size";
        } else if (bytesCount % 1152921504606846976L == 0) {
            // It is a multiple of 1 EB (1 EB = 1073741824 GB = 1152921504606846976 bytes)
            displaySize = bytesCount / 1152921504606846976L + "EB";
        } else if (bytesCount % 1125899906842624L == 0) {
            // It is a multiple of 1 PB (1 PB = 1048576 GB = 1125899906842624 bytes)
            displaySize = bytesCount / 1125899906842624L + "PB";
        } else if (bytesCount % 1099511627776L == 0) {
            // It is a multiple of 1 TB (1 TB = 1024 GB = 1099511627776 bytes)
            displaySize = bytesCount / 1099511627776L + "TB";
        } else if (bytesCount % 1073741824L == 0) {
            // It is a multiple of 1 GB? (1 GB = 1024 MB = 1048576 KB = 1073741824 bytes)
            displaySize = bytesCount / 1073741824L + "GB";
        } else if (bytesCount % 1048576L == 0) {
            // It is a multiple of 1 MB (1 MB = 1024 KB = 1048576 bytes)
            displaySize = bytesCount / 1048576L + "MB";
        } else if (bytesCount % 1024L == 0) {
            // Is it a multiple of 1 KB (1 KB = 1024 bytes)
            displaySize = bytesCount / 1024L + "KB";
        } else {
            // In order to keep the exact precision we display it as a number of bytes!
            displaySize = bytesCount + "B";
        }
        return displaySize;
    }

    /**
     * Parses the byte size.
     *
     * @param byteSizeString
     *            the byte size string
     *
     * @return the long
     */
    private static long parseByteSize(final String byteSizeString) {
        // Remove any space and convert it to upper-case
        var value = byteSizeString.replaceAll("\\s", "").toUpperCase();
        final String unit;
        // Check if the string ends with a valid byte unit and extract it
        if (value.endsWith("KB")) {
            value = value.substring(0, value.length() - 2);
            unit = "KB";
        } else if (value.endsWith("MB")) {
            value = value.substring(0, value.length() - 2);
            unit = "MB";
        } else if (value.endsWith("GB")) {
            value = value.substring(0, value.length() - 2);
            unit = "GB";
        } else if (value.endsWith("TB")) {
            value = value.substring(0, value.length() - 2);
            unit = "TB";
        } else if (value.endsWith("PB")) {
            value = value.substring(0, value.length() - 2);
            unit = "PB";
        } else if (value.endsWith("EB")) {
            value = value.substring(0, value.length() - 2);
            unit = "EB";
        } else {
            if (value.endsWith("B")) {
                value = value.substring(0, value.length() - 1);
            } else if (value.endsWith("BYTES")) {
                value = value.substring(0, value.length() - 5);
            }
            unit = "B";
        }
        // The value should only contains digits
        if (!value.matches("\\d+")) {
            throw new IllegalArgumentException("Invalid byte size string: " + byteSizeString);
        }
        // Extract the numeric value
        final var numericValue = Double.parseDouble(value);
        // Calculate the byte count based on the unit
        if ("KB".equals(unit)) {
            return (long) (numericValue * 1024L);
        }
        if ("MB".equals(unit)) {
            return (long) (numericValue * 1048576L);
        } else if ("GB".equals(unit)) {
            return (long) (numericValue * 1073741824L);
        } else if ("TB".equals(unit)) {
            return (long) (numericValue * 1099511627776L);
        } else if ("PB".equals(unit)) {
            return (long) (numericValue * 1125899906842624L);
        } else if ("EB".equals(unit)) {
            return (long) (numericValue * 1152921504606846976L);
        } else {
            return (long) numericValue;
        }
    }
}
