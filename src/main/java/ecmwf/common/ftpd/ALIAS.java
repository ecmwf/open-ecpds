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

import java.util.Map;

import ecmwf.common.technical.Cnf;

/**
 * The Class ALIAS.
 */
final class ALIAS {
    /**
     * Instantiates a new alias.
     *
     * @param currentContext
     *            the current context
     * @param parameter
     *            the parameter
     */
    ALIAS(final CurrentContext currentContext, String parameter) {
        if ((parameter = Util.parseParameter(currentContext, ALIAS.class, parameter)) == null) {
            return;
        }
        final var equal = parameter.indexOf("=");
        if (equal == -1) {
            currentContext.respond(501, "ALIAS parse error");
            return;
        }
        final var alias = parameter.substring(0, equal).toUpperCase();
        final var target = parameter.substring(equal + 1).toUpperCase();
        try {
            setAliases(currentContext, alias, target);
        } catch (final ClassNotFoundException cnfe) {
            currentContext.respond(451, "Alias not found");
            return;
        }
        currentContext.respond(200, "ALIAS command successful");
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
        final var result = new StringBuilder();
        final Map<String, String> aliases;
        if ((aliases = currentContext.getAliases()) != null) {
            for (final String key : aliases.keySet()) {
                result.append("   ").append(key).append("=").append(currentContext.getAlias(key)).append("\n");
            }
        }
        Util.display(currentContext,
                "Syntax: ALIAS source=target\n" + (result.length() == 0 ? "no aliases\n" : result.toString())
                        + "Above aliases are set.\n" + "Current domain name is "
                        + currentContext.domainName.toUpperCase() + ".\n" + "Current domain user is "
                        + currentContext.domainUser + ".",
                214, Cnf.at("FtpPlugin", "tail", "Direct comments to ecaccess@ecmwf.int"));
    }

    /**
     * Sets the aliases.
     *
     * @param currentContext
     *            the current context
     * @param alias
     *            the alias
     * @param target
     *            the target
     *
     * @throws java.lang.ClassNotFoundException
     *             the class not found exception
     */
    public static void setAliases(final CurrentContext currentContext, final String alias, final String target)
            throws ClassNotFoundException {
        Util.getClass(currentContext, alias, true, false);
        if (alias.equals(target)) {
            currentContext.removeAlias(alias);
        } else {
            Util.getClass(currentContext, target, true, false);
            currentContext.putAlias(alias, target);
        }
    }
}
