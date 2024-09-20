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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.text.Util.isNotEmpty;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.compression.bzip2a.CBZip2InputStream;
import ecmwf.common.compression.bzip2a.CBZip2OutputStream;
import ecmwf.common.text.Format;
import net.jpountz.lz4.LZ4FrameInputStream;
import net.jpountz.lz4.LZ4FrameOutputStream;

/**
 * The Class StreamManagerImp.
 */
public final class StreamManagerImp implements StreamManager {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(StreamManagerImp.class);

    /**
     * Instantiates a new stream manager imp.
     */
    private StreamManagerImp() {
        // Hiding constructor
    }

    /**
     * Get the compressor from its name *.
     *
     * @param name
     *            the name
     * @param out
     *            the out
     *
     * @return the compressor output stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static OutputStream getCompressorOutputStream(final String name, final OutputStream out)
            throws IOException {
        try {
            return new CompressorStreamFactory().createCompressorOutputStream(name, out);
        } catch (final CompressorException e) {
            throw new IOException("Compressor error for " + name, e);
        }
    }

    /**
     * Get the compressor input stream from its name *.
     *
     * @param name
     *            the name
     * @param in
     *            the in
     *
     * @return the compressor input stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static InputStream getCompressorInputStream(final String name, final InputStream in) throws IOException {
        try {
            return new CompressorStreamFactory().createCompressorInputStream(name, in);
        } catch (final CompressorException e) {
            throw new IOException("Compressor error for " + name, e);
        }
    }

    /**
     * Gets the filters.
     *
     * @param out
     *            the out
     * @param filter
     *            the filter
     *
     * @return the output stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static OutputStream getFiltersOutputStream(final OutputStream out, final String filter) throws IOException {
        if (NONE.equalsIgnoreCase(filter)) {
            return out;
        }
        if (LZMA.equalsIgnoreCase(filter)) {
            return getCompressorOutputStream(CompressorStreamFactory.LZMA, out);
        } else if (ZIP.equalsIgnoreCase(filter)) {
            // Using native Zip compression within Java!
            return new ZipOutputStream(out);
        } else if (GZIP.equalsIgnoreCase(filter)) {
            return getCompressorOutputStream(CompressorStreamFactory.GZIP, out);
        } else if (BZIP2a.equalsIgnoreCase(filter)) {
            // Using specific Bzip2 implementation which is faster that commons-compress!
            return new CBZip2OutputStream(out);
        } else if (LBZIP2.equalsIgnoreCase(filter)) {
            // Using binary Lbzip2 implementation!
            return new CommandOutputStream(out, Cnf.getCommand("Filter", "fly.out." + LBZIP2));
        } else if (LZ4.equalsIgnoreCase(filter)) {
            // Using specific LZ4 implementation which is faster that commons-compress!
            return new LZ4FrameOutputStream(out);
        } else if (SNAPPY.equalsIgnoreCase(filter)) {
            return getCompressorOutputStream(CompressorStreamFactory.SNAPPY_FRAMED, out);
        } else {
            _log.warn("Filter {} not supported", filter);
            throw new IOException("Filter " + filter + " not supported for output stream");
        }
    }

    /**
     * Gets the buffered.
     *
     * @param out
     *            the out
     * @param size
     *            the size
     *
     * @return the buffered
     */
    public static OutputStream getBuffered(final OutputStream out, final int size) {
        if (size > 0 && !(out instanceof BufferedOutputStream)) {
            _log.debug("Force BufferedOutputStream ({})", size);
            return new BufferedOutputStream(out, size);
        }
        return out;
    }

    /**
     * Gets the filters.
     *
     * @param out
     *            the out
     * @param filters
     *            the filters
     * @param size
     *            the size
     *
     * @return the filters
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static OutputStream getFilters(OutputStream out, final String filters, final int size) throws IOException {
        out = getBuffered(out, size);
        if (!NONE.equalsIgnoreCase(filters) && isNotEmpty(filters)) {
            final var token = new StringTokenizer(filters, ";,");
            while (token.hasMoreElements()) {
                out = getFiltersOutputStream(out, token.nextToken());
            }
        }
        return out;
    }

    /**
     * Checks if is filtered.
     *
     * @param out
     *            the out
     *
     * @return true, if is filtered
     */
    public static boolean isFiltered(final OutputStream out) {
        return out instanceof CompressorOutputStream || out instanceof ZipOutputStream
                || out instanceof CommandOutputStream || out instanceof LZ4FrameOutputStream
                || out instanceof CBZip2OutputStream;
    }

    /**
     * Gets the filters.
     *
     * @param in
     *            the in
     * @param filter
     *            the filter
     *
     * @return the input stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static InputStream getFiltersInputStream(final InputStream in, final String filter) throws IOException {
        if (NONE.equalsIgnoreCase(filter)) {
            return in;
        }
        if (LZMA.equalsIgnoreCase(filter)) {
            return getCompressorInputStream(CompressorStreamFactory.LZMA, in);
        } else if (ZIP.equalsIgnoreCase(filter)) {
            // Using native Zip compression within Java!
            return new ZipInputStream(in);
        } else if (GZIP.equalsIgnoreCase(filter)) {
            return getCompressorInputStream(CompressorStreamFactory.GZIP, in);
        } else if (BZIP2a.equalsIgnoreCase(filter)) {
            // Using specific Bzip2 implementation which is faster that commons-compress!
            return new CBZip2InputStream(in);
        } else if (LBZIP2.equalsIgnoreCase(filter)) {
            // Using binary Lbzip2 implementation!
            return new CommandInputStream(in, Cnf.getCommand("Filter", "fly.in." + LBZIP2));
        } else if (LZ4.equalsIgnoreCase(filter)) {
            // Using specific LZ4 implementation which is faster that commons-compress!
            return new LZ4FrameInputStream(in);
        } else if (SNAPPY.equalsIgnoreCase(filter)) {
            return getCompressorInputStream(CompressorStreamFactory.SNAPPY_FRAMED, in);
        } else {
            _log.warn("Filter {} not supported", filter);
            throw new IOException("Filter " + filter + " not supported for input stream");
        }
    }

    /**
     * Gets the buffered.
     *
     * @param in
     *            the in
     * @param size
     *            the size
     *
     * @return the buffered
     */
    public static InputStream getBuffered(final InputStream in, final int size) {
        if (size > 0 && !(in instanceof BufferedInputStream)) {
            _log.debug("Force BufferedInputStream ({})", size);
            return new BufferedInputStream(in, size);
        }
        return in;
    }

    /**
     * Gets the filters.
     *
     * @param in
     *            the in
     * @param filters
     *            the filters
     * @param size
     *            the size
     *
     * @return the filters
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static InputStream getFilters(InputStream in, final String filters, final int size) throws IOException {
        in = getBuffered(in, size);
        if (!NONE.equalsIgnoreCase(filters) && isNotEmpty(filters)) {
            final var token = new StringTokenizer(filters, ";,");
            final List<String> list = new ArrayList<>();
            while (token.hasMoreElements()) {
                list.add(0, token.nextToken());
            }
            for (final String filter : list) {
                in = getFiltersInputStream(in, filter);
            }
        }
        return in;
    }

    /**
     * Gets the extension.
     *
     * @param filter
     *            the filter
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static String getExtensionFor(final String filter) throws IOException {
        if (NONE.equalsIgnoreCase(filter)) {
            return "";
        }
        if (LZMA.equalsIgnoreCase(filter)) {
            return "lzma";
        }
        if (ZIP.equalsIgnoreCase(filter)) {
            return "zip";
        } else if (GZIP.equalsIgnoreCase(filter)) {
            return "gz";
        } else if (BZIP2a.equalsIgnoreCase(filter) || LBZIP2.equalsIgnoreCase(filter)) {
            return "bz2";
        } else if (LZ4.equalsIgnoreCase(filter)) {
            return "lz4";
        } else if (SNAPPY.equalsIgnoreCase(filter)) {
            return "sz";
        } else {
            _log.warn("Filter {} not supported", filter);
            throw new IOException("Filter " + filter + " not supported");
        }
    }

    /**
     * Gets the extension.
     *
     * @param filters
     *            the filters
     *
     * @return the extension
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static String getExtension(final String filters) throws IOException {
        final var extension = new StringBuilder();
        if (!NONE.equalsIgnoreCase(filters) && isNotEmpty(filters)) {
            final var token = new StringTokenizer(filters, ";,");
            final List<String> list = new ArrayList<>();
            while (token.hasMoreElements()) {
                list.add(0, token.nextToken());
            }
            for (final String filter : list) {
                extension.append(".").append(getExtensionFor(filter));
            }
        }
        return extension.toString();
    }

    /**
     * Checks if is filtered.
     *
     * @param in
     *            the in
     *
     * @return true, if is filtered
     */
    public static boolean isFiltered(final InputStream in) {
        return in instanceof CompressorInputStream || in instanceof ZipInputStream || in instanceof CommandInputStream
                || in instanceof LZ4FrameInputStream || in instanceof CBZip2InputStream;
    }

    /**
     * Checks if is filtered.
     *
     * @param filters
     *            the filters
     *
     * @return true, if is filtered
     */
    public static boolean isFiltered(final String filters) {
        var result = false;
        if (!NONE.equalsIgnoreCase(filters) && isNotEmpty(filters)) {
            final var token = new StringTokenizer(filters, ";,");
            while (!result && token.hasMoreElements()) {
                final var filter = token.nextToken();
                result = LZMA.equalsIgnoreCase(filter) || ZIP.equalsIgnoreCase(filter) || GZIP.equalsIgnoreCase(filter)
                        || BZIP2a.equalsIgnoreCase(filter) || LBZIP2.equalsIgnoreCase(filter)
                        || LZ4.equalsIgnoreCase(filter) || SNAPPY.equalsIgnoreCase(filter);
            }
        }
        return result;
    }

    /**
     * Gets the filters.
     *
     * @param filters
     *            the filters
     * @param size
     *            the size
     *
     * @return the filters
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static String getFilters(final String filters, final long size) throws IOException {
        var result = NONE;
        if (!NONE.equalsIgnoreCase(filters) && isNotEmpty(filters)) {
            final var token = new StringTokenizer(filters, ":");
            while (token.hasMoreElements()) {
                result = token.nextToken();
                if (token.hasMoreElements()) {
                    try {
                        final var limit = Long.parseLong(token.nextToken());
                        if (size <= limit) {
                            if (_log.isDebugEnabled()) {
                                _log.debug("Choose filter {} ({}<={}) - {}", result, Format.formatSize(size),
                                        Format.formatSize(limit), filters);
                            }
                            return result;
                        }
                        result = NONE;
                    } catch (final Throwable t) {
                        throw new IOException("Bad syntax for filter: " + filters);
                    }
                }
            }
        }
        _log.debug("Choose filter {}{}", result, !result.equals(filters) ? " (" + filters + ")" : "");
        return result;
    }
}