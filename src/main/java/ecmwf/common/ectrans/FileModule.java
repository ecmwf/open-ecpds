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

package ecmwf.common.ectrans;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Class FileModule.
 */
public class FileModule extends TransferModule {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(FileModule.class);

    /** The _dir. */
    private File _dir = null;

    /**
     * Connect.
     *
     * @param location
     *            the location
     * @param setup
     *            the setup
     */
    @Override
    public void connect(final String location, final ECtransSetup setup) {
        _dir = new File(location);
    }

    /**
     * Close.
     */
    @Override
    public void close() {
    }

    /**
     * Del.
     *
     * @param name
     *            the name
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public void del(final String name) throws IOException {
        final var file = new File(_dir, name);
        if (file.exists() && !file.delete()) {
            throw new IOException("Couldn't delete file: " + file.getAbsolutePath());
        }
    }

    /**
     * Gets the.
     *
     * @param name
     *            the name
     * @param posn
     *            the posn
     *
     * @return the input stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public InputStream get(final String name, final long posn) throws IOException {
        final var in = new FileInputStream(new File(_dir, name));
        final long result;
        if (posn > 0 && (result = in.skip(posn)) != posn) {
            _log.warn("Couldn't skip by: " + posn + " (" + result + ")");
        }
        return in;
    }

    /**
     * Put.
     *
     * @param name
     *            the name
     * @param posn
     *            the posn
     * @param size
     *            the size
     *
     * @return the output stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public OutputStream put(final String name, final long posn, final long size) throws IOException {
        if ((!_dir.exists() && !_dir.mkdirs()) && !_dir.exists()) {
            throw new IOException("Couldn't mkdirs: " + _dir.getAbsolutePath());
        }
        final var file = new File(_dir, name);
        if (posn == 0 && file.exists() && !file.delete()) {
            _log.warn("Couldn't delete file: " + file.getAbsolutePath());
        }
        final var raf = new RandomAccessFile(file, "rw");
        if (posn > 0) {
            raf.seek(posn);
        }
        return new RandomOutputStream(raf);
    }

    /**
     * Size.
     *
     * @param name
     *            the name
     *
     * @return the long
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public long size(final String name) throws IOException {
        final var file = new File(_dir, name);
        if (!file.exists()) {
            throw new IOException("File " + file.getAbsolutePath() + " not found");
        }
        return file.length();
    }

    /**
     * The Class RandomOutputStream.
     */
    private static final class RandomOutputStream extends OutputStream {
        /** The _file. */
        private final RandomAccessFile _file;

        /**
         * Instantiates a new random output stream.
         *
         * @param file
         *            the file
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public RandomOutputStream(final RandomAccessFile file) throws IOException {
            _file = file;
        }

        /**
         * Close.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void close() throws IOException {
            _file.close();
        }

        /**
         * Flush.
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void flush() throws IOException {
        }

        /**
         * Write.
         *
         * @param b
         *            the b
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void write(final byte[] b) throws IOException {
            _file.write(b);
        }

        /**
         * Write.
         *
         * @param b
         *            the b
         * @param off
         *            the off
         * @param len
         *            the len
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            _file.write(b, off, len);
        }

        /**
         * Write.
         *
         * @param b
         *            the b
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        @Override
        public void write(final int b) throws IOException {
            _file.write(b);
        }
    }
}
