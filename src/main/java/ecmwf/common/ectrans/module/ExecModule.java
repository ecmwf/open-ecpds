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

package ecmwf.common.ectrans.module;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.ectrans.ECtransOptions.HOST_EXEC_RETURN_CODE;
import static ecmwf.common.text.Util.isNotEmpty;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.StringReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.ectrans.TransferModule;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.text.Format;

/**
 * The Class ExecModule.
 */
public final class ExecModule extends TransferModule {
    /**
     * The Constant _DEFAULT_SCRIPT_TO_START. Default Script to start after transfer is completed with the file name in
     * first parameter.
     */
    private static final String _DEFAULT_SCRIPT_TO_START = System.getProperty("ecmwf.dir", "../..")
            + "/gateway/bin/ectrans";

    /** The Constant _log. To log messages to the gateway log file. */
    private static final Logger _log = LogManager.getLogger(ExecModule.class);

    /** The _synchronous. */
    private boolean _synchronous = true;

    /** The _use pipe. */
    private boolean _usePipe = false;

    /** The _return code. */
    private int _returnCode = 0;

    /** The _script. */
    private String _script = null;

    /** The _dir. */
    private File _dir = null;

    /** The _file. */
    private File _file = null;

    /** The _temp. */
    private File _temp = null;

    /** The _args. */
    private String _args = null;

    /** The _mkdirs. */
    private boolean _mkdirs = true;

    /** The _usetmp. */
    private boolean _usetmp = true;

    /** The _prefix. */
    private String _prefix = null;

    /** The _suffix. */
    private String _suffix = null;

    /** The _setup. */
    private ECtransSetup _setup = null;

    /**
     * {@inheritDoc}
     *
     * Connect.
     */
    @Override
    public void connect(final String dir, final ECtransSetup setup) throws IOException {
        _setup = setup;
        _script = Cnf.notEmptyStringAt("ExecModule", "script", _DEFAULT_SCRIPT_TO_START);
        final var file = new File(_script);
        if (!file.canRead()) {
            throw new IOException("Script " + _script + " not found/readable");
        }
        _dir = new File(Cnf.notEmptyStringAt("ExecModule", "dir", dir));
        _synchronous = Cnf.at("ExecModule", "synchronous", _synchronous);
        _usePipe = Cnf.at("ExecModule", "usePipe", _usePipe);
        _returnCode = setup.get(HOST_EXEC_RETURN_CODE, Cnf.at("ExecModule", "returnCode", _returnCode));
        _args = Cnf.at("ExecModule", "args", "");
        _mkdirs = Cnf.at("ExecModule", "mkdirs", _mkdirs);
        _usetmp = Cnf.at("ExecModule", "usetmp", _usetmp);
        _prefix = Cnf.at("ExecModule", "prefix", "");
        _suffix = Cnf.at("ExecModule", "suffix", "");
        if (_prefix.length() == 0 && _suffix.length() == 0) {
            _suffix = ".tmp";
        }
    }

    /**
     * {@inheritDoc}
     *
     * Close.
     */
    @Override
    public void close() throws IOException {
    }

    /**
     * {@inheritDoc}
     *
     * Check.
     */
    @Override
    public void check(final long sent, final String checksum) throws IOException {
        // The _temp and _file variables are not null if the put()
        // method has been called before.
        if (_temp != null && _file != null) {
            // Rename the file using its original name.
            if (!_temp.renameTo(_file)) {
                throw new IOException(
                        "Rename operation unsuccessful for " + _temp.getPath() + " (" + _file.getPath() + ")");
            }
            // Process parameters
            final var args = _args.length() > 0 ? _processParameters() : _file.getAbsolutePath();
            // Start script with the file name in first parameter.
            final Process process;
            if (_usePipe) {
                if (getDebug()) {
                    _log.debug("Exec: " + _script + " (using pipe)");
                }
                process = Runtime.getRuntime().exec(_script);
                final var out = process.getOutputStream();
                final var dis = new BufferedReader(new StringReader(_args));
                String line;
                try {
                    while ((line = dis.readLine()) != null) {
                        out.write(line.concat("\r\n").getBytes());
                    }
                } finally {
                    out.close();
                }
            } else {
                final var command = _script + (args.length() > 0 ? " " + args : "");
                if (getDebug()) {
                    _log.debug("Exec: " + command);
                }
                process = Runtime.getRuntime().exec(command);
            }
            final int returnCode;
            if (_synchronous) {
                try {
                    returnCode = process.waitFor();
                    if (getDebug()) {
                        _log.debug("ReturnCode: " + returnCode);
                    }
                } catch (final InterruptedException e) {
                    throw new IOException("Interrupted while waiting");
                } finally {
                    StreamPlugThread.closeQuietly(process);
                }
                if (_returnCode != returnCode) {
                    throw new IOException("Unexpected return code from " + _script + " (" + returnCode + ")");
                }
            }
            setAttribute("remote.fileName", _file);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Del.
     */
    @Override
    public void del(final String name) throws IOException {
        final var file = new File(_dir, name);
        if (file.exists() && !file.delete()) {
            throw new IOException("Couldn't delete file: " + file.getAbsolutePath());
        }
    }

    /**
     * {@inheritDoc}
     *
     * Put.
     */
    @Override
    public OutputStream put(final String name, final long posn, final long size) throws IOException {
        if (posn > 0) {
            throw new IOException("Resume/Append not supported by the " + _setup.getModuleName() + " module");
        }
        // Create all subdirectories (if necessary).
        if ((_mkdirs && !_dir.exists() && !_dir.mkdirs()) && !_dir.exists()) {
            throw new IOException("Couldn't mkdirs: " + _dir.getAbsolutePath());
        }
        // Set the _file and _temp variables (delete previous copy of _temp).
        _file = new File(_dir, name);
        _temp = new File(_dir, _getTemporaryName(name));
        if (_temp.exists() && !_temp.delete()) {
            _log.warn("Couldn't delete existing file: " + _temp.getAbsolutePath());
        }
        if (getDebug()) {
            _log.debug("Put file: " + name + " (" + _temp.getAbsolutePath() + ")");
        }
        // Return the OutputStream to the ECtransContainer.
        return new RandomOutputStream(new RandomAccessFile(_temp, "rw"));
    }

    /**
     * {@inheritDoc}
     *
     * Size.
     */
    @Override
    public long size(final String name) {
        final var file = new File(_dir, name);
        if (file.exists()) {
            return file.length();
        }
        return 0;
    }

    /**
     * Gets the temporary name.
     *
     * @param name
     *            the name
     *
     * @return the string
     */
    private String _getTemporaryName(final String name) {
        final var file = new File(name);
        final var pathName = file.getParent();
        return (isNotEmpty(pathName) ? pathName + File.separator : "")
                + (_usetmp ? _prefix + file.getName() + _suffix : file.getName());
    }

    /**
     * _process parameters.
     *
     * @return the string
     */
    private String _processParameters() {
        final var ecuser = getECUser();
        final var msuser = getMSUser();
        final var sb = new StringBuilder(_args);
        Format.replaceAll(sb, "$msuser[name]", msuser.getName());
        Format.replaceAll(sb, "$msuser[comment]", msuser.getComment());
        Format.replaceAll(sb, "$msuser[dir]", msuser.getDir());
        Format.replaceAll(sb, "$msuser[host]", msuser.getHost());
        Format.replaceAll(sb, "$msuser[login]", msuser.getLogin());
        Format.replaceAll(sb, "$msuser[passwd]", getClearPassword());
        Format.replaceAll(sb, "$msuser[password]", getClearPassword());
        Format.replaceAll(sb, "$ecuser[name]", ecuser.getName());
        Format.replaceAll(sb, "$ecuser[uid]", ecuser.getUid());
        Format.replaceAll(sb, "$ecuser[gid]", ecuser.getGid());
        Format.replaceAll(sb, "$ecuser[dir]", ecuser.getDir());
        Format.replaceAll(sb, "$ecuser[shell]", ecuser.getShell());
        Format.replaceAll(sb, "$ecuser[comment]", ecuser.getComment());
        Format.replaceAll(sb, "$ecuser", ecuser.getName());
        Format.replaceAll(sb, "$target", _file.getAbsolutePath());
        Format.replaceAll(sb, "$location", _dir);
        Format.replaceAll(sb, "$dir", _dir);
        Format.replaceAll(sb, "$password", getClearPassword());
        Format.replaceAll(sb, "$passwd", getClearPassword());
        final var args = sb.toString();
        if (getDebug()) {
            _log.debug("Process parameters: " + _args + " (" + args + ")");
        }
        return args;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the.
     */
    @Override
    public InputStream get(final String name, final long posn) throws IOException {
        // This method is called by ECaccess to get a file from
        // the gateway. This service is not implemented in this
        // module.
        throw new NotImplementedException("Method get is not implemented in " + ExecModule.class.getName());
    }

    /**
     * The Class RandomOutputStream. Local class used to return a standard OutputStream to the gateway in the put()
     * method.
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

    /**
     * The Class NotImplementedException. Local class used to signal not implemented services/options.
     */
    private static final class NotImplementedException extends IOException {
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = -8116699783282235202L;

        /**
         * Instantiates a new not implemented exception.
         *
         * @param message
         *            the message
         */
        NotImplementedException(final String message) {
            super(message);
        }
    }
}
