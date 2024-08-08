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

package ecmwf.common.ftpd;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.ectrans.ECtransOptions.USER_PORTAL_TRIGGER_EVENT;
import static ecmwf.common.text.Util.isNotEmpty;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.ecaccess.NativeAuthenticationProvider;
import ecmwf.common.technical.ProxyEvent;
import ecmwf.common.technical.ProxySocket;
import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.text.Format;
import ecmwf.ecbatch.eis.rmi.client.EccmdException;

/**
 * The Class STOR.
 */
final class STOR {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(STOR.class);

    /**
     * Instantiates a new stor.
     *
     * @param currentContext
     *            the current context
     * @param parameter
     *            the parameter
     */
    public STOR(final CurrentContext currentContext, String parameter) {
        final var append = currentContext.parametersList.remove("STOR.append") != null;
        final var unique = currentContext.parametersList.remove("STOR.unique") != null;
        final var execute = currentContext.parametersList.remove("STOR.execute") != null;
        // If the file name is to be generated create a temporary file.
        final var req = parameter;
        if (unique || execute) {
            try {
                parameter = currentContext.session.getTempFile();
            } catch (final EccmdException e) {
                currentContext.respond(451, e);
                return;
            } catch (final Exception e) {
                currentContext.respond(451, "Requested action aborted", e);
                return;
            }
        }
        // Get the stor file name after unique.
        if ((parameter = Util.parseParameter(currentContext, STOR.class, parameter)) == null) {
            return;
        }
        ProxySocket proxy = null;
        OutputStream os = null;
        try {
            try {
                proxy = currentContext.session.getProxySocketOutput(
                        Util.getPath(currentContext, parameter, false, true), currentContext.rest,
                        currentContext.umask);
                os = proxy.getDataSocket().getOutputStream();
            } catch (final EccmdException e) {
                currentContext.respond(451, e);
                return;
            } catch (final FileNotFoundException e) {
                currentContext.respond(550, parameter + ": No such file or directory");
                return;
            } catch (final Exception e) {
                currentContext.respond(451, "Requested action aborted", e);
                return;
            }
            final Socket dataSocket;
            if ((dataSocket = currentContext.dataSocket.getDataSocket(currentContext)) == null) {
                currentContext.respond(425, "Can't open data connection. Try using passive (PASV) transfers");
                return;
            }
            try {
                currentContext.respond(150,
                        "File status okay; about to open " + currentContext.transferText() + " connection");
                // If not restoring session, append to the file.
                currentContext.rest = 0;
                if (append && currentContext.session
                        .getFileSize(Util.getPath(currentContext, parameter, false, true)) == -1) {
                    throw new FileNotFoundException();
                }
                dataSocket.setSendBufferSize(currentContext.buffer);
                dataSocket.setReceiveBufferSize(currentContext.buffer);
                final var in = dataSocket.getInputStream();
                var byteCount = 0L;
                final var startTime = System.currentTimeMillis();
                final var setup = currentContext.session.getECtransSetup();
                final ProxyEvent event;
                // Populating with the transfer rate informations!
                if (setup == null || setup.getBoolean(USER_PORTAL_TRIGGER_EVENT)) {
                    event = new ProxyEvent(proxy);
                    event.setProtocol("ftp");
                    event.setLocalHost(NativeAuthenticationProvider.getInstance().getRoot());
                    event.setRemoteHost(currentContext.remoteIP.getHostAddress());
                    event.setUserType(ProxyEvent.UserType.DATA_USER);
                    event.setUserName(currentContext.authName);
                    event.setStartTime(startTime);
                    event.setUpload(true);
                } else {
                    event = null;
                }
                if (currentContext.transferType == CurrentContext.ATYPE) {
                    // ASCII file transfers are going to be a bit slow 'cause we
                    // have to read
                    // them a byte at a time to convert EOL's to our native
                    // ways.
                    _log.debug("ASCII file transfer");
                    final var ins = new BufferedInputStream(in);
                    var ci = 0;
                    final var baos = new ByteArrayOutputStream();
                    final var out = new DataOutputStream(baos);
                    while (ci != -1) {
                        while ((ci = ins.read()) != -1 && baos.size() < currentContext.buffer) {
                            final var c = (byte) ci;
                            if (c == Util.CRLFb[0]) {
                                continue; // Toss away CR's
                            }
                            if (c == Util.CRLFb[1]) {
                                out.writeBytes("\n"); // Write whatever our
                                // platform uses for a
                                // newline.
                            } else {
                                out.writeByte(c); // Write the byte.
                            }
                        }
                        try {
                            os.write(baos.toByteArray());
                        } catch (final IOException ioe) {
                            if (event != null) {
                                event.setDuration(System.currentTimeMillis() - startTime);
                                event.setSent(byteCount);
                            }
                            currentContext.session.check(proxy);
                            throw ioe;
                        }
                        byteCount += baos.size();
                        baos.reset();
                    }
                } else {
                    // Binary transfer - much faster.
                    _log.debug("Binary file transfer");
                    final var ins = new BufferedInputStream(in);
                    final var buffer = new byte[currentContext.buffer];
                    int amount;
                    while ((amount = StreamPlugThread.readFully(ins, buffer, 0, currentContext.buffer)) > 0) {
                        byteCount += amount;
                        try {
                            os.write(buffer, 0, amount);
                        } catch (final IOException ioe) {
                            if (event != null) {
                                event.setDuration(System.currentTimeMillis() - startTime);
                                event.setSent(byteCount);
                            }
                            currentContext.session.check(proxy);
                            throw ioe;
                        }
                    }
                }
                in.close();
                os.close();
                final var stopTime = System.currentTimeMillis();
                _log.info("Transfer completed at " + Format.formatRate(byteCount, stopTime - startTime));
                if (byteCount == 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (final InterruptedException e) {
                        _log.debug(e);
                    }
                }
                if (event != null) {
                    event.setDuration(System.currentTimeMillis() - startTime);
                    event.setSent(byteCount);
                }
                currentContext.session.check(proxy);
                if (unique) {
                    // Report the file name if we're generating the name.
                    currentContext.respond(250, parameter);
                } else if (execute) {
                    // Export the file name and execute the request.
                    currentContext.parametersList.put("STOR.unique", parameter);
                } else {
                    // Normal transfer.
                    currentContext.respond(226, "Transfer complete");
                }
                currentContext.newEvent("put " + currentContext.domainName, new File(parameter).getName() + " ("
                        + Format.getMBitsPerSeconds(byteCount, stopTime - startTime) + " Mbits/s)", false);
            } catch (final EccmdException e) {
                currentContext.newEvent("put " + currentContext.domainName, e.getMessage(), true);
                currentContext.respond(426, e);
                return;
            } catch (final Exception e) {
                final var exception = e.getMessage();
                final var message = "Connection closed; transfer aborted";
                currentContext.newEvent("get " + currentContext.domainName, isNotEmpty(exception) ? exception : message,
                        true);
                currentContext.respond(426, message, e);
                return;
            } finally {
                if (dataSocket != null) {
                    StreamPlugThread.closeQuietly(dataSocket);
                    currentContext.dataSocket.close();
                }
            }
        } finally {
            StreamPlugThread.closeQuietly(os);
            try {
                if (proxy != null) {
                    proxy.close();
                }
            } catch (final Exception ignored) {
            }
        }
        if (execute) {
            currentContext.parametersList.put("STOR.unique", parameter);
            try {
                Util.exec(getClass(), currentContext, req, false);
            } catch (final Exception e) {
                currentContext.parametersList.remove("STOR.unique");
                currentContext.respond(426, "Connection closed; transfer aborted", e);
                return;
            }
        }
    }
}
