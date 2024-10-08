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

final class SET {
    /**
     * Instantiates a new sets the.
     *
     * @param currentContext
     *            the current context
     * @param parameter
     *            the parameter
     */
    public SET(final CurrentContext currentContext, String parameter) {
        // Get the set command.
        if ((parameter = Util.parseParameter(currentContext, SET.class, parameter)) == null) {
            return;
        }
        final var equal = parameter.indexOf("=");
        if (equal == -1) {
            currentContext.respond(501, "SET parse error");
            return;
        }
        final var name = parameter.substring(0, equal);
        final var value = parameter.substring(equal + 1);
        if ("buffer".equals(name.toLowerCase())) {
            try {
                currentContext.buffer = Integer.parseInt(value);
                currentContext.respond(200, "SET buffer command successful (" + currentContext.buffer + " bytes)");
                return;
            } catch (final NumberFormatException e) {
                currentContext.respond(451, "SET error", e);
                return;
            }
        }
        if ("passive".equals(name.toLowerCase())) {
            currentContext.passiveMode = "true".equalsIgnoreCase(value);
            currentContext.respond(200, "SET passive command successful (" + currentContext.passiveMode + ")");
            return;
        }
        currentContext.respond(504, "SET unknown parameter");
    }
}
