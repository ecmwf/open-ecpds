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

package ecmwf.common.security;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.text.Util.isNotEmpty;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.Cnf;
import ecmwf.common.telnet.TelnetWrapper;
import ecmwf.common.text.Format;

/**
 * The Class Tools.
 */
public final class Tools {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(Tools.class);

    /** The Constant _header. */
    private static final String _header;

    /** The _current. */
    private static final AtomicLong _current;

    static {
        // We do use the current time at startup and increment it on each call
        // to get a unique key!
        _current = new AtomicLong(System.currentTimeMillis());
        // The header is used to create a unique key among systems and consist
        // in a random number between 100 and 999!
        _header = String.valueOf(ThreadLocalRandom.current().nextInt(900) + 100);
    }

    /**
     * _log ios.
     *
     * @param wrapper
     *            the wrapper
     */
    private static void _logIos(final TelnetWrapper wrapper) {
        if (wrapper != null && _log.isDebugEnabled()) {
            final var ios = wrapper.getIosLog();
            _log.debug("Session input/output: " + (isNotEmpty(ios) ? ios : "(empty)"));
        }
    }

    /**
     * Login.
     *
     * @param telnetd
     *            the telnetd
     * @param client
     *            the client
     * @param login
     *            the login
     * @param password
     *            the password
     * @param token
     *            the token
     * @param windowSize
     *            the window size
     * @param terminalType
     *            the terminal type
     * @param env
     *            the env
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void login(final Socket telnetd, final Socket client, final String login, final String password,
            final byte[] token, final Dimension windowSize, final String terminalType, final Map<String, String> env)
            throws IOException {
        login(telnetd, client.getOutputStream(), client.getInputStream(), login, password, token, windowSize,
                terminalType, env);
    }

    /**
     * Login.
     *
     * @param telnetd
     *            the telnetd
     * @param out
     *            the out
     * @param in
     *            the in
     * @param login
     *            the login
     * @param password
     *            the password
     * @param token
     *            the token
     * @param windowSize
     *            the window size
     * @param terminalType
     *            the terminal type
     * @param env
     *            the env
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void login(final Socket telnetd, final OutputStream out, final InputStream in, final String login,
            final String password, final byte[] token, final Dimension windowSize, final String terminalType,
            final Map<String, String> env) throws IOException {
        _log.debug("Login " + telnetd.getInetAddress().getHostAddress() + ":" + telnetd.getPort() + " (" + login + ":"
                + password + ")");
        final var telnetClient = new TelnetWrapper();
        final var telnetServer = new TelnetWrapper();
        telnetServer.setTerminalType(terminalType);
        if (windowSize != null) {
            telnetServer.setWindowSize(windowSize.width, windowSize.height);
        }
        final var soTimeout = telnetd.getSoTimeout();
        final var connectionTimeout = Cnf.durationAt("ECauthModule", "connectionTimeout", 10000);
        _log.debug("Connection timeout: " + Format.formatDuration(connectionTimeout));
        telnetd.setSoTimeout((int) connectionTimeout);
        var disconnected = false;
        String res = null;
        try {
            telnetServer.recordIos(true);
            telnetServer.connect(telnetd);
            var attempt = 0;
            while (res == null || res.indexOf("token:") == -1) {
                if (attempt++ == 3) {
                    throw new IOException("Not authenticated (login)");
                }
                if (attempt == 1) {
                    telnetServer.waitfor("ogin:");
                }
                telnetServer.send(login);
                telnetServer.waitfor("word:");
                telnetServer.send(password);
                res = telnetServer.waitfor("token:", "invalid");
            }
            _log.debug("Login attempts: " + attempt);
            telnetServer.send(new String(token));
            res = telnetServer.waitfor("env:", "Refused");
            if (res == null || res.indexOf("env:") == -1) {
                throw new IOException("Not authenticated");
            }
            env.put("ECAUTH_SERVICE", "session");
            for (final String key : env.keySet()) {
                final var environment = key + "=" + env.get(key);
                _log.debug("Exporting: " + environment);
                telnetServer.send(environment);
                res = telnetServer.waitfor("env:");
            }
            telnetServer.send(".");
            res = telnetServer.waitfor("Accepted");
            int i;
            if (res == null || (i = res.indexOf("Accepted")) == -1) {
                throw new IOException("Not authenticated");
            }
            telnetClient.connect(in, out);
            telnetClient.write(res.substring(i + 8).getBytes());
            _log.debug("Connected");
        } catch (final IOException ioe) {
            telnetServer.disconnect();
            telnetClient.disconnect();
            disconnected = true;
            _logIos(telnetServer);
            _log.warn("Not connected", ioe);
            throw ioe;
        } catch (final Throwable t) {
            telnetServer.disconnect();
            telnetClient.disconnect();
            disconnected = true;
            _logIos(telnetServer);
            _log.warn("Not connected", t);
            throw new IOException("Not connected");
        } finally {
            if (!disconnected) {
                telnetServer.recordIos(false);
                try {
                    telnetd.setSoTimeout(soTimeout);
                } catch (final Exception e) {
                    _log.warn("Setting soTimeout", e);
                }
            }
        }
    }

    /**
     * Login.
     *
     * @param socket
     *            the socket
     * @param login
     *            the login
     * @param password
     *            the password
     * @param token
     *            the token
     *
     * @return the telnet wrapper
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static TelnetWrapper login(final Socket socket, final String login, final String password,
            final byte[] token) throws IOException {
        final var telnetWrapper = new TelnetWrapper();
        final var soTimeout = socket.getSoTimeout();
        final var connectionTimeout = Cnf.durationAt("ECauthModule", "connectionTimeout", 10000);
        _log.debug("Connection timeout: " + Format.formatDuration(connectionTimeout));
        socket.setSoTimeout((int) connectionTimeout);
        var disconnected = false;
        String res = null;
        try {
            telnetWrapper.recordIos(true);
            telnetWrapper.connect(socket);
            var attempt = 0;
            while (res == null || res.indexOf("token:") == -1) {
                if (attempt++ == 3) {
                    throw new IOException("Not authenticated (login)");
                }
                if (attempt == 1) {
                    telnetWrapper.waitfor("ogin:");
                }
                telnetWrapper.send(login);
                telnetWrapper.waitfor("word:");
                telnetWrapper.send(password);
                res = telnetWrapper.waitfor("token:", "invalid");
            }
            _log.debug("Login attempts: " + attempt);
            telnetWrapper.send(new String(token));
            res = telnetWrapper.waitfor("env:", "Refused");
            if (res == null || res.indexOf("env:") == -1) {
                throw new IOException("Not authenticated (token)");
            }
            final var env = new HashMap<String, String>();
            env.put("ECAUTH_SERVICE", "data");
            for (final String key : env.keySet()) {
                telnetWrapper.send(key + "=" + env.get(key));
                res = telnetWrapper.waitfor("env:");
            }
            telnetWrapper.send(".");
            res = telnetWrapper.waitfor("Accepted");
            if (res == null || res.indexOf("Accepted") == -1) {
                throw new IOException("Not authenticated");
            }
            return telnetWrapper;
        } catch (final IOException ioe) {
            telnetWrapper.disconnect();
            disconnected = true;
            _logIos(telnetWrapper);
            _log.warn("Not connected", ioe);
            throw ioe;
        } catch (final Throwable t) {
            telnetWrapper.disconnect();
            disconnected = true;
            _logIos(telnetWrapper);
            _log.warn("Not connected", t);
            throw new IOException("Not connected");
        } finally {
            if (!disconnected) {
                telnetWrapper.recordIos(false);
                try {
                    socket.setSoTimeout(soTimeout);
                } catch (final Exception e) {
                    _log.warn("Setting soTimeout", e);
                }
            }
        }
    }

    /**
     * Ecauth.
     *
     * @param telnetd
     *            the telnetd
     * @param client
     *            the client
     * @param token
     *            the token
     * @param env
     *            the env
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void ecauth(final Socket telnetd, final Socket client, final byte[] token,
            final Map<String, String> env) throws IOException {
        ecauth(telnetd, client.getOutputStream(), client.getInputStream(), token, env);
    }

    /**
     * Ecauth.
     *
     * @param telnetd
     *            the telnetd
     * @param out
     *            the out
     * @param in
     *            the in
     * @param token
     *            the token
     * @param env
     *            the env
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void ecauth(final Socket telnetd, final OutputStream out, final InputStream in, final byte[] token,
            final Map<String, String> env) throws IOException {
        _log.debug("Ecauth " + telnetd.getInetAddress().getHostAddress() + ":" + telnetd.getPort());
        final var telnetClient = new TelnetWrapper();
        final var telnetServer = new TelnetWrapper();
        final var soTimeout = telnetd.getSoTimeout();
        final var connectionTimeout = Cnf.durationAt("ECauthModule", "connectionTimeout", 10000);
        _log.debug("Connection timeout: " + Format.formatDuration(connectionTimeout));
        telnetd.setSoTimeout((int) connectionTimeout);
        var disconnected = false;
        String res = null;
        try {
            telnetServer.recordIos(true);
            telnetServer.connect(telnetd);
            if (telnetServer.waitfor("token:") == null) {
                throw new IOException("NX not setup on this server");
            }
            telnetServer.send(new String(token));
            res = telnetServer.waitfor("env:", "Refused");
            if (res == null || res.indexOf("env:") == -1) {
                throw new IOException("Not authenticated");
            }
            env.put("ECAUTH_SERVICE", "session");
            for (final String key : env.keySet()) {
                final var environment = key + "=" + env.get(key);
                _log.debug("Exporting: " + environment);
                telnetServer.send(environment);
                res = telnetServer.waitfor("env:");
            }
            telnetServer.send(".");
            res = telnetServer.waitfor("Accepted");
            int i;
            if (res == null || (i = res.indexOf("Accepted")) == -1) {
                throw new IOException("Not authenticated");
            }
            telnetClient.connect(in, out);
            telnetClient.write(res.substring(i + 8).getBytes());
            _log.debug("Connected");
        } catch (final IOException ioe) {
            telnetServer.disconnect();
            telnetClient.disconnect();
            disconnected = true;
            _logIos(telnetServer);
            _log.warn("Not connected", ioe);
            throw ioe;
        } catch (final Throwable t) {
            telnetServer.disconnect();
            telnetClient.disconnect();
            disconnected = true;
            _logIos(telnetServer);
            _log.warn("Not connected", t);
            throw new IOException("Not connected");
        } finally {
            if (!disconnected) {
                telnetServer.recordIos(false);
                try {
                    telnetd.setSoTimeout(soTimeout);
                } catch (final Exception e) {
                    _log.warn("Setting soTimeout", e);
                }
            }
        }
    }

    /**
     * The main method.
     *
     * @param args
     *            the arguments
     */
    public static void main(final String[] args) {
        System.out.println(newPassword(args[0]));
    }

    /**
     * New password.
     *
     * @param size
     *            the size
     * @param param1
     *            the param1
     * @param param2
     *            the param2
     * @param param3
     *            the param3
     *
     * @return the string
     */
    public static String newPassword(final int size, final int param1, final int param2, final int param3) {
        var tmp2 = 55 - param1;
        var tmp1 = tmp2 * param1 * param1 * (param1 + param2 + param3);
        tmp2 = param1 * tmp2 * tmp2 * (tmp2 + param2 + param3);
        if (tmp2 < 0) {
            tmp2 = tmp2 * -1;
        }
        if (tmp1 < 0) {
            tmp1 = tmp1 * -1;
        }
        var tmp4 = tmp1 + tmp2;
        if (tmp4 < 0) {
            tmp4 = tmp4 * -1;
        }
        final var tmp3 = new StringBuilder().append(tmp1).append(tmp2).append(tmp4).toString();
        final var chars = "qwertyuiopasdfghjklzxcvbnm";
        final var password = new StringBuilder();
        var i = 0;
        while (password.length() < size && i + 2 < tmp3.length()) {
            final var value = Integer.parseInt(tmp3.substring(i, i + 2));
            i += 2;
            if (value < chars.length() && value >= 0) {
                password.append(chars.charAt(value));
            } else {
                password.append(tmp3.substring(i, i + 1));
                i++;
            }
        }
        return password.toString();
    }

    /**
     * New password.
     *
     * @param string
     *            the string
     *
     * @return the string
     */
    public static String newPassword(final String string) {
        return newPassword(8, string.length(), string.hashCode(), Integer.MAX_VALUE);
    }

    /**
     * Converts into x509 certificate.
     *
     * @param certificate
     *            the certificate
     *
     * @return the x509 certificate
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws java.security.cert.CertificateException
     *             the certificate exception
     */
    public static X509Certificate toX509Certificate(final byte[] certificate) throws IOException, CertificateException {
        return toX509Certificate(new String(certificate, 0, certificate.length, Charset.forName("UTF-8")));
    }

    /**
     * Converts into x509 certificate.
     *
     * @param certificate
     *            the certificate
     *
     * @return the x509 certificate
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws java.security.cert.CertificateException
     *             the certificate exception
     */
    public static X509Certificate toX509Certificate(final String certificate) throws IOException, CertificateException {
        final var in = new ByteArrayInputStream(certificate.getBytes());
        final var cf = CertificateFactory.getInstance("X.509");
        final var cert = (X509Certificate) cf.generateCertificate(in);
        in.close();
        final var name = cert.getSubjectX500Principal().getName();
        try {
            cert.checkValidity();
        } catch (final CertificateExpiredException e) {
            _log.warn(name, e);
            throw e;
        } catch (final CertificateNotYetValidException e) {
            _log.warn(name, e);
        }
        return cert;
    }

    /**
     * Converts into pem.
     *
     * @param certificate
     *            the certificate
     *
     * @return the string
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws java.security.cert.CertificateEncodingException
     *             the certificate encoding exception
     */
    public static String toPEM(final X509Certificate certificate) throws IOException, CertificateEncodingException {
        final var pem = new ByteArrayOutputStream();
        final Writer wr = new OutputStreamWriter(pem, Charset.forName("UTF-8"));
        wr.write("-----BEGIN CERTIFICATE-----\n");
        wr.write(new String(Base64.getEncoder().encode(certificate.getEncoded()), "UTF-8"));
        wr.write("\n-----END CERTIFICATE-----\n");
        wr.flush();
        return pem.toString("UTF-8");
    }

    /**
     * From pem.
     *
     * @param pem
     *            the pem
     *
     * @return the x509 certificate
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws java.security.cert.CertificateEncodingException
     *             the certificate encoding exception
     * @throws java.security.cert.CertificateException
     *             the certificate exception
     */
    public static X509Certificate fromPEM(final String pem)
            throws IOException, CertificateEncodingException, CertificateException {
        final var cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf
                .generateCertificate(new ByteArrayInputStream(pem.getBytes(Charset.forName("UTF-8"))));
    }

    /**
     * Gets the unique long id.
     *
     * @return the unique long id
     */
    public static long getUniqueLongId() {
        return getUniqueIntId();
    }

    /**
     * Gets the unique int id.
     *
     * @return the unique int id
     */
    public static int getUniqueIntId() {
        final var res = String.valueOf(_current.incrementAndGet());
        // The unique key is made of a first random number between 10 and 20, the header
        // which is fixed and the last 5 numbers of the incremented current time!
        return Integer
                .parseInt(ThreadLocalRandom.current().nextInt(11) + 10 + _header + res.substring(res.length() - 5));
    }
}
