/*
 * Copyright 2014-2020 Andrew Gaul <andrew@gaul.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ecmwf.ecpds.mover.plugin.http;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.common.io.ByteStreams;

/**
 * Parse an AWS v4 signature chunked stream. Reference:
 * https://docs.aws.amazon.com/AmazonS3/latest/API/sigv4-streaming.html
 */
final class ChunkedInputStream extends FilterInputStream {

    /** The chunk. */
    private byte[] chunk;

    /** The current index. */
    private int currentIndex;

    /** The current length. */
    private int currentLength;

    /** The current signature. */
    @SuppressWarnings("unused")
    private String currentSignature;

    /**
     * Instantiates a new chunked input stream.
     *
     * @param is
     *            the is
     */
    ChunkedInputStream(final InputStream is) {
        super(is);
    }

    /**
     * Read.
     *
     * @return the int
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public int read() throws IOException {
        while (currentIndex == currentLength) {
            final var line = readLine(in);
            if ("".equals(line)) {
                return -1;
            }
            final var parts = line.split(";", 2);
            currentLength = Integer.parseInt(parts[0], 16);
            currentSignature = parts[1];
            chunk = new byte[currentLength];
            currentIndex = 0;
            ByteStreams.readFully(in, chunk);
            // TODO: check currentSignature
            if (currentLength == 0) {
                return -1;
            }
            readLine(in);
        }
        return chunk[currentIndex++] & 0xFF;
    }

    /**
     * Read.
     *
     * @param b
     *            the b
     * @param off
     *            the off
     * @param len
     *            the len
     *
     * @return the int
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        int i;
        for (i = 0; i < len; ++i) {
            final var ch = read();
            if (ch == -1) {
                break;
            }
            b[off + i] = (byte) ch;
        }
        if (i == 0) {
            return -1;
        }
        return i;
    }

    /**
     * Read a \r\n terminated line from an InputStream.
     *
     * @param is
     *            the is
     *
     * @return line without the newline or empty String if InputStream is empty
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static String readLine(final InputStream is) throws IOException {
        final var builder = new StringBuilder();
        while (true) {
            var ch = is.read();
            if (ch == '\r') {
                ch = is.read();
                if (ch == '\n') {
                    break;
                } else {
                    throw new IOException("unexpected char after \\r: " + ch);
                }
            }
            if (ch == -1) {
                if (builder.length() > 0) {
                    throw new IOException("unexpected end of stream");
                }
                break;
            }
            builder.append((char) ch);
        }
        return builder.toString();
    }
}
