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
 * ECMWF Product Data Store (OpenPDS) Project.
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */

final class REST {
    /**
     * Instantiates a new rest.
     *
     * @param currentContext
     *            the current context
     * @param parameter
     *            the parameter
     */
    public REST(final CurrentContext currentContext, String parameter) {
        if ((parameter = Util.parseParameter(currentContext, REST.class, parameter)) == null) {
            return;
        }
        // Set the file position for the RESTore
        try {
            currentContext.rest = Long.parseLong(parameter);
        } catch (final Exception e) {
            currentContext.respond(501, "REST parse error", e);
            return;
        }
        currentContext.respond(350, "Requested file action pending further information");
    }
}
