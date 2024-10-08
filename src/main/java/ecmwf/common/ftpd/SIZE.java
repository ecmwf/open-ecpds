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

import ecmwf.common.ecaccess.EccmdException;

/**
 * The Class SIZE.
 */
final class SIZE {

    /**
     * Instantiates a new size.
     *
     * @param currentContext
     *            the current context
     * @param parameter
     *            the parameter
     */
    public SIZE(final CurrentContext currentContext, String parameter) {
        if ((parameter = Util.parseParameter(currentContext, SIZE.class, parameter)) == null) {
            return;
        }
        final long dataLength;
        try {
            dataLength = currentContext.session.getFileSize(Util.getPath(currentContext, parameter, false, true));
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
        currentContext.respond(213, String.valueOf(dataLength));
    }
}
