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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.ecaccess.NativeAuthenticationProvider;
import ecmwf.common.technical.Cnf;

/**
 * The Class DOMAIN.
 */
final class DOMAIN {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(DOMAIN.class);

    /** The Constant _defaultDomain. */
    private static final String _defaultDomain = Cnf.at("FtpDomainList", "*");

    /** The Constant _domainsList. */
    private static final Map<String, String> _domainsList = Cnf.at("FtpDomainList", new HashMap<>());

    /** The Constant _hiddenDomainsList. */
    private static final List<String> _hiddenDomainsList = Cnf.listAt("FtpPlugin", "hidden");

    /**
     * Instantiates a new domain.
     *
     * @param currentContext
     *            the current context
     * @param parameter
     *            the parameter
     */
    public DOMAIN(final CurrentContext currentContext, String parameter) {
        if ((parameter = Util.parseParameter(currentContext, DOMAIN.class, parameter)) == null) {
            return;
        }
        final var set = setDomain(currentContext, parameter);
        if (set && !"*".equals(currentContext.domainName)) {
            currentContext.browser = false;
        }
        currentContext.respond(200,
                set ? "DOMAIN set to " + currentContext.domainName.replace('*', '/') : "Invalid domain name");
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
        Util.display(currentContext,
                "Syntax: DOMAIN target-domain\n" + getDomainsList() + "\n" + "Above domains are recognized.\n"
                        + "Current domain is " + currentContext.domainName.toUpperCase().replace('*', '/') + "["
                        + currentContext.domainUser + "].\n" + "Default domain is "
                        + _defaultDomain.toUpperCase().replace('*', '/') + "[" + currentContext.user + "].",
                214, Cnf.at("FtpPlugin", "tail", "Direct comments to ecaccess@ecmwf.int"));
    }

    /**
     * Gets the domains list.
     *
     * @return the domains list
     */
    public static String getDomainsList() {
        final var result = new StringBuilder();
        for (String key : _domainsList.keySet()) {
            key = key.toUpperCase();
            if (!"*".equals(key) && !_hiddenDomainsList.contains(key)) {
                result.append("   ").append(key);
            }
        }
        return result.toString();
    }

    /**
     * Gets the domain value.
     *
     * @param domain
     *            the domain
     *
     * @return the domain value
     */
    public static String getDomainValue(final String domain) {
        if (!"*".equals(domain)) {
            for (final String key : _domainsList.keySet()) {
                if (!"*".equals(key) && !_hiddenDomainsList.contains(key) && key.equalsIgnoreCase(domain)) {
                    return _domainsList.get(key);
                }
            }
        }
        return null;
    }

    /**
     * Sets the domain.
     *
     * @param currentContext
     *            the current context
     * @param domain
     *            the domain
     *
     * @return true, if successful
     */
    public static boolean setDomain(final CurrentContext currentContext, String domain) {
        domain = domain == null ? _defaultDomain == null ? "*" : _defaultDomain : domain;
        String user = null;
        var pos = domain.indexOf("[");
        if (pos > 0 && domain.endsWith("]")) {
            var found = false;
            user = domain.substring(pos + 1, domain.length() - 1);
            domain = domain.substring(0, pos);
            try {
                found = user.equals(currentContext.user)
                        || NativeAuthenticationProvider.getInstance().isRegistredUser(user);
            } catch (final Exception e) {
                _log.warn("Setting domain for ecuser " + user, e);
                found = false;
            }
            if (!found) {
                return false;
            }
        }
        if ("*".equals(domain = domain.replace('.', '*').replace('/', '*'))) {
            currentContext.domainValue = "";
            currentContext.domainName = "*";
            currentContext.domainUser = user != null ? user
                    : currentContext.domainUser != null ? currentContext.domainUser : currentContext.user;
            return currentContext.browser = true;
        }
        for (final String key : _domainsList.keySet()) {
            if (!"*".equals(key) && key.equalsIgnoreCase(domain)) {
                currentContext.domainValue = _domainsList.get(key);
                currentContext.domainName = key;
                currentContext.domainUser = user != null ? user : currentContext.user;
                if (!currentContext.containsAliases()) {
                    // First login to this domain.
                    for (final String alias : Cnf.listAt("FtpAliases", currentContext.domainName)) {
                        try {
                            pos = alias.indexOf("=");
                            ALIAS.setAliases(currentContext, alias.substring(0, pos), alias.substring(pos + 1));
                        } catch (final Exception e) {
                            _log.warn("Setting alias " + alias, e);
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }
}
