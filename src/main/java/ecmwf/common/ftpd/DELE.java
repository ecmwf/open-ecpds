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

import ecmwf.common.ecaccess.EccmdException;

/**
 * The Class DELE.
 */
final class DELE {
    /**
     * Instantiates a new dele.
     *
     * @param currentContext
     *            the current context
     * @param parameter
     *            the parameter
     */
    public DELE(final CurrentContext currentContext, String parameter) {
        if ((parameter = Util.parseParameter(currentContext, DELE.class, parameter)) == null) {
            return;
        }
        try {
            currentContext.session.deleteFile(Util.getPath(currentContext, parameter, false, true), false);
        } catch (final EccmdException ee) {
            currentContext.respond(451, ee);
            return;
        } catch (final FileNotFoundException e) {
            currentContext.respond(550, parameter + ": No such file or directory");
            return;
        } catch (final Exception e) {
            currentContext.respond(451, "Requested action aborted", e);
            return;
        }
        currentContext.newEvent("delete", parameter, false);
        currentContext.respond(250, "DELE command successful");
    }
}
