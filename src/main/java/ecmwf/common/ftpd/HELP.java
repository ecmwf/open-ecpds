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

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.Cnf;

/**
 * The Class HELP.
 */
final class HELP {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(HELP.class);

    /**
     * Instantiates a new help.
     *
     * @param currentContext
     *            the current context
     * @param parameter
     *            the parameter
     */
    public HELP(final CurrentContext currentContext, final String parameter) {
        final var st = new StringTokenizer(parameter, " ");
        if (st.countTokens() == 0) {
            Util.display(currentContext, new File(Cnf.at("FtpPlugin", "help", "help.ftp")), 214,
                    "Syntax: HELP command");
        } else {
            final var command = st.nextToken();
            if ("SITE".equalsIgnoreCase(command)) {
                // Respond to a QUOTE HELP SITE command (e.g. with NcFTP
                // software)!
                Util.display(currentContext, new BufferedReader(new StringReader("""
                        The following commands are recognized.
                         ACCT ALLO APPE CDUP CLNT CWD DELE EPRT EPSV HELP LIST MDTM MKD
                         NLST NOOP PASS PASV PORT PWF QUIT REST RETR RMD  RNFR RNTO SITE
                         SIZE STAT STOR STOU SYST TYPE USER XCUP XCWD XMKD XPWD XRMD
                        """)), 214, "Help Ok.");
            } else {
                try {
                    final Class<?> clazz = Util.getClass(currentContext, command, true, false);
                    clazz.getDeclaredMethod("HELP", CurrentContext.class, String.class).invoke(clazz, currentContext,
                            command);
                } catch (final Exception e) {
                    _log.debug(e);
                    currentContext.respond(501, "HELP " + command + " not found");
                }
            }
        }
    }
}
