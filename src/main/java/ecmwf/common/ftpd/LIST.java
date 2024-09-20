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
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

import ecmwf.common.ecaccess.EccmdException;
import ecmwf.common.ecaccess.FileListElement;
import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.text.Format;

/**
 * The Class LIST.
 */
final class LIST {
    /** The Constant _time. */
    private static final long _time = System.currentTimeMillis();

    /**
     * Instantiates a new list.
     *
     * @param currentContext
     *            the current context
     * @param parameter
     *            the parameter
     */
    public LIST(final CurrentContext currentContext, String parameter) {
        // Remove options
        parameter = parameter.trim();
        while (parameter.startsWith("-")) {
            final var pos = parameter.indexOf(" ");
            if (pos > 0) {
                parameter = parameter.substring(pos + 1);
            } else {
                parameter = "";
            }
        }
        final var nlst = currentContext.parametersList.remove("LIST.nlst") != null;
        final Socket dataSocket;
        if ((dataSocket = currentContext.dataSocket.getDataSocket(currentContext)) == null) {
            currentContext.respond(425, "Can't open data connection");
            return;
        }
        try {
            currentContext.respond(150, "Opening " + currentContext.transferText() + " data connection");
            final var out = new PrintWriter(dataSocket.getOutputStream(), true);
            FileListElement[] fileList = null;
            try {
                fileList = currentContext.session
                        .getFileList(Util.getPath(currentContext, parameter.trim(), false, false));
            } catch (final FileNotFoundException e) {
                final var message = e.getMessage();
                if (message == null || !"*".equals(message)) {
                    throw e;
                }
                final var token = new StringTokenizer(DOMAIN.getDomainsList());
                fileList = new FileListElement[token.countTokens()];
                var i = 0;
                while (token.hasMoreElements()) {
                    final var element = new FileListElement();
                    element.setRight("drwxr-x---");
                    element.setUser(currentContext.domainUser);
                    element.setGroup(currentContext.session.getDefaultGroup());
                    element.setSize("2048");
                    element.setTime(_time);
                    element.setName(token.nextToken());
                    fileList[i++] = element;
                }
            }
            if (!nlst && fileList.length > 0 && fileList[0].getRight() != null) {
                // This is a proper file list element so we can use the standard
                // format for this one!
                for (final FileListElement element : fileList) {
                    out.print(_toFormattedString(element));
                    out.print(currentContext.transferEOL());
                }
            } else {
                // Either we just display the names or the provider did return a
                // file list element without permissions set (e.g. a list of
                // files returned from a remote site) so we don't try to format
                // the string!
                for (final FileListElement element : fileList) {
                    out.print(element.getName());
                    out.print(currentContext.transferEOL());
                }
            }
            out.flush();
            currentContext.respond(226, "Transfer complete");
        } catch (final EccmdException e) {
            currentContext.respond(451, e);
            return;
        } catch (final FileNotFoundException e) {
            currentContext.respond(550, parameter + ": No such file or directory");
            return;
        } catch (final Exception e) {
            currentContext.respond(451, "Requested action aborted", e);
            return;
        } finally {
            if (dataSocket != null) {
                StreamPlugThread.closeQuietly(dataSocket);
                currentContext.dataSocket.close();
            }
        }
    }

    /**
     * Converts into formatted string.
     *
     * @param element
     *            the element
     *
     * @return the string
     */
    private static String _toFormattedString(final FileListElement element) {
        final var permission = element.getRight();
        final var link = permission.startsWith("l") ? element.getLink() : null;
        return Format.getFtpList(permission, element.getUser(), element.getGroup(), element.getSize(),
                element.getTime(), element.getName() + (link != null ? " -> " + link : ""));
    }
}
