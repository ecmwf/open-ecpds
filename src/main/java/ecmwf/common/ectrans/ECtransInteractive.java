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

import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_DEBUG;
import static ecmwf.common.text.Util.isNotEmpty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.text.Format;
import ecmwf.common.version.Version;

/**
 * The Class ECtransInteractive.
 */
public class ECtransInteractive {
    /** The Constant _br. */
    private static final BufferedReader _br = new BufferedReader(new InputStreamReader(System.in));

    /** The _module. */
    private static TransferModule _module = null;

    /**
     * _close.
     *
     * @return true, if successful
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static boolean _close() throws IOException {
        if (_module != null) {
            try {
                _module.close();
            } catch (final Exception e) {
            }
            _module = null;
            return true;
        }
        return false;
    }

    /**
     * _prompt.
     *
     * @param message
     *            the message
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static String _prompt(final String message) throws IOException {
        return _prompt(message, true);
    }

    /**
     * _prompt.
     *
     * @param message
     *            the message
     * @param connected
     *            the connected
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static String _prompt(final String message, final boolean connected) throws IOException {
        if (connected && _module == null) {
            throw new IOException("Not connected!");
        }
        System.out.print(message + ": ");
        return _br.readLine();
    }

    /**
     * _plug.
     *
     * @param in
     *            the in
     * @param out
     *            the out
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static String _plug(final InputStream in, final OutputStream out) throws IOException {
        try {
            final var b = new byte[1024];
            final var time = System.currentTimeMillis();
            var total = 0L;
            int size;
            while ((size = StreamPlugThread.readFully(in, b, 0, 1024)) > 0) {
                out.write(b, 0, size);
                total += size;
            }
            return Format.formatRate(total, System.currentTimeMillis() - time);
        } finally {
            in.close();
            out.close();
        }
    }

    /**
     * Puts the file.
     *
     * @param source
     *            the source
     * @param target
     *            the target
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static String _putFile(final String source, final String target) throws IOException {
        if (_module == null) {
            throw new IOException("Not connected!");
        }
        final var file = new File(source);
        if (file.canRead()) {
            final var transfer = "File " + source + " uploaded - "
                    + _plug(new FileInputStream(file), _module.put(target, 0, file.length()));
            _module.check(file.length(), null);
            return transfer;
        } else {
            throw new IOException("File not found/readable!");
        }
    }

    /**
     * Gets the file.
     *
     * @param source
     *            the source
     * @param target
     *            the target
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static String _getFile(final String source, final String target) throws IOException {
        if (_module == null) {
            throw new IOException("Not connected!");
        }
        final var file = new File(target);
        if (!file.exists()) {
            final var transfer = "File " + source + " downloaded - "
                    + _plug(_module.get(source, 0), new FileOutputStream(file));
            _module.check(file.length(), null);
            return transfer;
        } else {
            throw new IOException("File already exists!");
        }
    }

    /**
     * The main method.
     *
     * @param args
     *            the arguments
     */
    public static void main(final String[] args) {
        System.out.println("ECtrans Interactive (v" + Version.getFullVersion() + ")");
        System.out.print("#> ");
        System.out.flush();
        String cmd;
        try {
            while ((cmd = _br.readLine()) != null) {
                try {
                    if (cmd.startsWith("size")) {
                        System.out.println(_module.size(_prompt("filename")));
                    } else if (cmd.startsWith("rmdir")) {
                        _module.rmdir(_prompt("directory"));
                    } else if (cmd.startsWith("put")) {
                        final var source = _prompt("source");
                        final var target = _prompt("target");
                        System.out.println(_putFile(source, target));
                    } else if (cmd.startsWith("move")) {
                        final var source = _prompt("source");
                        final var target = _prompt("target");
                        _module.move(source, target);
                    } else if (cmd.startsWith("mkdir")) {
                        _module.mkdir(_prompt("directory"));
                    } else if (cmd.startsWith("list")) {
                        final var list = _module.listAsStringArray(_prompt("directory"), null);
                        for (final String element : list) {
                            System.out.println(element);
                        }
                    } else if (cmd.startsWith("get")) {
                        final var source = _prompt("source");
                        final var target = _prompt("target");
                        System.out.println(_getFile(source, target));
                    } else if (cmd.startsWith("del")) {
                        _module.del(_prompt("filename"));
                    } else if (cmd.startsWith("connect")) {
                        final var location = _prompt("location", false);
                        final var module = _prompt("module", false);
                        final var clazz = _prompt("class", false);
                        final var data = _prompt("data", false);
                        _close();
                        _module = (TransferModule) ECtransInteractive.class.getClassLoader().loadClass(clazz)
                                .getDeclaredConstructor().newInstance();
                        final var setup = new ECtransSetup(module, data);
                        _module.setDebug(setup.getBoolean(HOST_ECTRANS_DEBUG));
                        _module.connect(location, setup);
                    } else if (cmd.startsWith("close")) {
                        if (!_close()) {
                            System.err.println("Connection already closed!");
                        }
                    } else if (cmd.startsWith("quit")) {
                        System.out.println("Bye!");
                        _close();
                        return;
                    } else if (cmd.startsWith("help")) {
                        System.out.println("Commands available:");
                        System.out.println(" - connect, close, list, del, mkdir, rmdir, move, get, put, size");
                    } else if (isNotEmpty(cmd)) {
                        System.err.println("Command '" + cmd + "' not found! (please try -help)");
                    }
                } catch (final Exception e) {
                    System.err.println("===================================================");
                    e.printStackTrace(System.err);
                    System.err.println("===================================================");
                    System.err.flush();
                }
                System.out.print("#> ");
                System.out.flush();
            }
        } catch (final IOException ioe) {
        }
        System.out.println("Bye!");
    }
}
