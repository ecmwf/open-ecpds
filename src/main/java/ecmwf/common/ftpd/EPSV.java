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
 * ECMWF Product Data Store (OpenECPDS) Project.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */

final class EPSV {
    /**
     * Instantiates a new epsv.
     *
     * @param currentContext
     *            the current context
     * @param parameter
     *            the parameter
     */
    public EPSV(final CurrentContext currentContext, final String parameter) {
        if (!currentContext.passiveMode) {
            currentContext.respond(502, "Use 'quote set passive=true' to allow PASV");
            return;
        }
        currentContext.dataSocket.createPasvSocket(currentContext, true);
    }
}
