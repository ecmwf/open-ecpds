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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.ecaccess.EccmdException;
import ecmwf.common.ecaccess.NativeAuthenticationProvider;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.ProxyEvent;
import ecmwf.common.technical.ProxySocket;
import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.text.Format;

/**
 * The Class RETR.
 */
final class RETR {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(RETR.class);

    /**
     * Instantiates a new retr.
     *
     * @param currentContext
     *            the current context
     * @param parameter
     *            the parameter
     */
    public RETR(final CurrentContext currentContext, String parameter) {
        if ((parameter = Util.parseParameter(currentContext, RETR.class, parameter)) == null) {
            return;
        }
        InputStream ios = null;
        ProxySocket proxy = null;
        final String realPath;
        try {
            long dataLength;
            try {
                realPath = Util.getPath(currentContext, parameter, false, true);
                dataLength = currentContext.session.getFileSize(realPath);
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
            if (dataLength == -1) {
                currentContext.respond(451, parameter + ": Not a plain file");
                return;
            }
            final Socket dataSocket;
            if ((dataSocket = currentContext.dataSocket.getDataSocket(currentContext)) == null) {
                currentContext.respond(425, "Can't open data connection. Try using passive (PASV) transfers");
                return;
            }
            try {
                currentContext.respond(150,
                        currentContext.transferText() + " connection for " + parameter + " (" + dataLength + " bytes)");
                // See if a REST was left behind.
                var posn = currentContext.rest;
                dataLength -= posn;
                currentContext.rest = 0;
                dataSocket.setSendBufferSize(currentContext.buffer);
                dataSocket.setReceiveBufferSize(currentContext.buffer);
                proxy = currentContext.session.getProxySocketInput(realPath, posn);
                ios = proxy.getDataSocket().getInputStream();
                final var out = dataSocket.getOutputStream();
                int count;
                final var size = posn;
                final var startTime = System.currentTimeMillis();
                final var data = new byte[currentContext.buffer];
                if (currentContext.transferType == CurrentContext.ATYPE) {
                    // ASCII file transfers are going to be a bit slow 'cause we
                    // have to read them a byte at a time to convert possible
                    // bare NL's or CRLF's to CRLF. This could be a binary file
                    // so don't try to read lines.
                    final var outs = new BufferedOutputStream(out);
                    while ((count = ios.read(data, 0, currentContext.buffer)) > 0) {
                        posn += count;
                        for (var i = 0; i < count; i++) {
                            final var c = data[i];
                            if (c == Util.CRLFb[0]) {
                                continue; // Ignore CR's
                            }
                            if (c == Util.CRLFb[1]) {
                                outs.write(Util.CRLFb, 0, 2);
                            } else {
                                outs.write(c); // Write the byte.
                            }
                        }
                    }
                    outs.flush();
                } else {
                    // Binary transfer - quite fast.
                    while ((count = ios.read(data, 0, currentContext.buffer)) > 0) {
                        out.write(data, 0, count);
                        posn += count;
                    }
                }
                out.close();
                ios.close();
                final var realSize = posn - size;
                final var duration = System.currentTimeMillis() - startTime;
                final var success = realSize == dataLength;
                final var setup = currentContext.session.getECtransSetup();
                // Populating with the transfer rate informations!
                if (setup == null || setup.getBoolean(USER_PORTAL_TRIGGER_EVENT)) {
                    final var event = new ProxyEvent(proxy);
                    event.setDuration(duration);
                    event.setProtocol("ftp");
                    event.setLocalHost(NativeAuthenticationProvider.getInstance().getRoot());
                    event.setRemoteHost(currentContext.remoteIP.getHostAddress());
                    event.setUserType(ProxyEvent.UserType.DATA_USER);
                    event.setUserName(currentContext.authName);
                    event.setStartTime(startTime);
                    event.setSent(realSize);
                }
                if (!success) {
                    currentContext.session.check(proxy);
                    throw new IOException("Connection closed; incorrect file size");
                }
                if (Cnf.at("FtpPlugin", "alwaysCheckOnRETR", false)) {
                    currentContext.session.check(proxy);
                }
                _log.info("Transfer completed at " + Format.formatRate(realSize, duration));
                currentContext.respond(226, "Transfer complete");
                currentContext.newEvent("get " + currentContext.domainName, new File(parameter).getName() + " ("
                        + Format.getMBitsPerSeconds(realSize, duration) + " Mbits/s)", false);
            } catch (final EccmdException e) {
                currentContext.newEvent("get " + currentContext.domainName, e.getMessage(), true);
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
            try {
                if (ios != null) {
                    ios.close();
                }
            } catch (final Exception ignored) {
            }
        }
    }
}
