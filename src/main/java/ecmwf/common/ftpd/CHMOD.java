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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.FileNotFoundException;
import java.util.StringTokenizer;

import ecmwf.common.ecaccess.EccmdException;
import ecmwf.common.technical.Cnf;

/**
 * The Class CHMOD.
 */
final class CHMOD {
    /**
     * Instantiates a new chmod.
     *
     * @param currentContext
     *            the current context
     * @param parameter
     *            the parameter
     */
    public CHMOD(final CurrentContext currentContext, String parameter) {
        if ((parameter = Util.parseParameter(currentContext, CHMOD.class, parameter)) == null) {
            return;
        }
        final var args = new StringTokenizer(parameter, " ");
        final var smode = args.hasMoreElements() ? args.nextToken() : null;
        final var file = args.hasMoreElements() ? args.nextToken() : null;
        if (smode == null || file == null) {
            currentContext.respond(501, "Mode or file not specified");
            return;
        }
        final int mode;
        try {
            mode = Util.parseMode(smode);
        } catch (final NumberFormatException nfe) {
            currentContext.respond(451, "Illegal mode specification");
            return;
        }
        try {
            currentContext.session.chmod(mode, Util.getPath(currentContext, file, false, true));
        } catch (final EccmdException e) {
            currentContext.respond(451, e);
            return;
        } catch (final FileNotFoundException e) {
            currentContext.respond(550, file + ": No such file or directory");
            return;
        } catch (final Exception e) {
            currentContext.respond(451, "Requested action aborted", e);
            return;
        }
        currentContext.respond(250, "CHMOD command successful");
    }

    /**
     * Help.
     *
     * @param currentContext
     *            the current context
     * @param parameter
     *            the parameter
     */
    public static void HELP(final CurrentContext currentContext, final String parameter) {
        Util.display(currentContext, "Syntax: CHMOD mode path", 214,
                Cnf.at("FtpPlugin", "tail", "Direct comments to ecaccess@ecmwf.int"));
    }
}
