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

import java.io.FileNotFoundException;

import ecmwf.common.ecaccess.EccmdException;
import ecmwf.common.text.Format;

/**
 * The Class CWD.
 */
final class CWD {
    /**
     * Instantiates a new cwd.
     *
     * @param currentContext
     *            the current context
     * @param parameter
     *            the parameter
     */
    public CWD(final CurrentContext currentContext, String parameter) {
        if ((parameter = Util.parseParameter(currentContext, CWD.class, parameter)) == null) {
            return;
        }
        if (parameter.startsWith("~")) {
            if (DOMAIN.setDomain(currentContext, currentContext.domainName + "[" + parameter.substring(1) + "]")) {
                currentContext.respond(250, "CWD command successful");
            } else {
                currentContext.respond(550, parameter.substring(1) + ": Invalid user name");
            }
            return;
        }
        if (parameter.indexOf('*') != -1 || parameter.indexOf('?') != -1) {
            currentContext.respond(451, "Wildcards are not allowed in path");
            return;
        }
        var domainName = currentContext.domainName;
        final var domainValue = currentContext.domainValue;
        try {
            if (currentContext.browser && "/".equals(Format.normalizePath(
                    (!currentContext.domainName.equals("*") ? "/" + currentContext.domainName.toUpperCase() + "/" : "")
                            + (parameter.startsWith("/") ? "" : currentContext.getPath() + '/') + parameter))) {
                currentContext.respond(250, "CWD command successful");
                DOMAIN.setDomain(currentContext, "*[" + currentContext.domainUser + "]");
                domainName = null;
                return;
            }
            final var path = Util.getPath(currentContext, parameter, true, false);
            final var elem = currentContext.session.getFileListElement(path);
            if (elem.isDirectory()) {
                currentContext.path.put(currentContext.domainName + '.' + currentContext.domainUser,
                        path.substring(currentContext.domainUser.length() + 2 + currentContext.domainValue.length()));
                Util.display(currentContext, elem.getComment(), 250, "CWD command successful");
                domainName = null;
                return;
            }
            currentContext.respond(550, parameter + ": No such file or directory");
        } catch (final FileNotFoundException e) {
            final var message = e.getMessage();
            if (message != null && "*".equals(message)) {
                currentContext.respond(250, "CWD command successful");
                DOMAIN.setDomain(currentContext, "*[" + currentContext.domainUser + "]");
                domainName = null;
                return;
            }
            currentContext.respond(550, parameter + ": No such file or directory");
        } catch (final EccmdException e) {
            currentContext.respond(451, e);
        } catch (final Exception e) {
            currentContext.respond(451, "Requested action aborted", e);
        } finally {
            if (domainName != null) {
                currentContext.domainName = domainName;
                currentContext.domainValue = domainValue;
            }
        }
    }
}
