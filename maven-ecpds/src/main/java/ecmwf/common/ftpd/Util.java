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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import ecmwf.common.technical.Cnf;
import ecmwf.common.text.Format;

/**
 * The Class Util.
 */
public final class Util {

    /** The _classes. */
    private static final Map<String, Class<?>> _classes = new HashMap<>();

    /** The _loader. */
    private static final ClassLoader _loader = FtpPlugin.class.getClassLoader();

    /** The Constant _CRLFc. */
    private static final char[] _CRLFc = { (char) 0x0d, (char) 0x0a };

    /** The Constant CRLFb. */
    static final byte[] CRLFb = { (byte) 0x0d, (byte) 0x0a };

    /** The Constant CRLF. */
    static final String CRLF = new String(_CRLFc); // CRLF for Telnet.

    /** The Constant LF. */
    static final String LF = new String(_CRLFc, 1, 1);

    /**
     * display.
     *
     * @param currentContext
     *            the current context
     * @param in
     *            the in
     * @param code
     *            the code
     * @param tail
     *            the tail
     */
    public static void display(final CurrentContext currentContext, final BufferedReader in, final int code,
            final String tail) {
        try {
            String line;
            try {
                while ((line = in.readLine()) != null) {
                    currentContext.respond(code + "-" + line);
                }
            } catch (final Exception e) {
            } finally {
                in.close();
            }
        } catch (final Exception ignored) {
        }
        currentContext.respond(code + " " + tail);
    }

    /**
     * Display.
     *
     * @param currentContext
     *            the current context
     * @param file
     *            the file
     * @param code
     *            the code
     * @param tail
     *            the tail
     */
    public static void display(final CurrentContext currentContext, final File file, final int code,
            final String tail) {
        try {
            display(currentContext, new BufferedReader(new InputStreamReader(new FileInputStream(file))), code, tail);
        } catch (final FileNotFoundException e) {
            display(currentContext, "", code, tail);
        }
    }

    /**
     * Display.
     *
     * @param currentContext
     *            the current context
     * @param message
     *            the message
     * @param code
     *            the code
     * @param tail
     *            the tail
     */
    public static void display(final CurrentContext currentContext, final String message, final int code,
            final String tail) {
        if (message == null || message.length() == 0) {
            currentContext.respond(code + " " + tail);
        } else {
            display(currentContext, new BufferedReader(new StringReader(message)), code, tail);
        }
    }

    /**
     * Exec.
     *
     * @param caller
     *            the caller
     * @param currentContext
     *            the current context
     * @param request
     *            the request
     * @param restricted
     *            the restricted
     *
     * @return the object
     *
     * @throws ClassNotFoundException
     *             the class not found exception
     * @throws NoSuchMethodException
     *             the no such method exception
     * @throws InstantiationException
     *             the instantiation exception
     * @throws IllegalAccessException
     *             the illegal access exception
     * @throws InvocationTargetException
     *             the invocation target exception
     */
    public static Object exec(final Class<?> caller, final CurrentContext currentContext, final String request,
            final boolean restricted) throws ClassNotFoundException, NoSuchMethodException, InstantiationException,
            IllegalAccessException, InvocationTargetException {
        final var ustr = request.toUpperCase();
        final var space = ustr.indexOf(' ');
        final var exec = space == -1 ? ustr : ustr.substring(0, space);
        final var parameters = space == -1 ? "" : request.substring(space).trim();
        var from = caller.getName();
        from = from.substring(from.lastIndexOf(".") + 1);
        final var alias = restricted ? null : currentContext.getAlias(exec);
        return Util.getClass(currentContext, exec, !from.equals(alias), restricted)
                .getConstructor(CurrentContext.class, String.class).newInstance(currentContext, parameters);
    }

    /**
     * Gets the class.
     *
     * @param currentContext
     *            the current context
     * @param command
     *            the command
     * @param alias
     *            the alias
     * @param restricted
     *            the restricted
     *
     * @return the class
     *
     * @throws ClassNotFoundException
     *             the class not found exception
     */
    public static Class<?> getClass(final CurrentContext currentContext, String command, final boolean alias,
            final boolean restricted) throws ClassNotFoundException {
        command = command.toUpperCase();
        if (alias) {
            final var found = currentContext.getAlias(command);
            command = found == null ? command : found;
        }
        var packageName = currentContext.getClass().getName();
        packageName = packageName.substring(0, packageName.lastIndexOf("."));
        // Ftp package?
        try {
            return _getClass(packageName + '.' + command);
        } catch (final ClassNotFoundException cnfe) {
        }
        if (!restricted) {
            // Declared module?
            final var modules = Cnf.at("FtpModuleList");
            if (modules != null && modules.containsKey(command)) {
                try {
                    return _getClass(modules.get(command));
                } catch (final ClassNotFoundException cnfe) {
                }
            }
            // Declared package?
            for (final String string : Cnf.listAt("FtpModuleList", "*")) {
                try {
                    return _getClass(string + '.' + command);
                } catch (final ClassNotFoundException cnfe) {
                }
            }
        }
        // Not found.
        throw new ClassNotFoundException(command);
    }

    /**
     * Gets the transfer module class.
     *
     * @param className
     *            the class name
     *
     * @return the transfer module class
     *
     * @throws ClassNotFoundException
     *             the class not found exception
     */
    private static Class<?> _getClass(final String className) throws ClassNotFoundException {
        Class<?> clazz;
        synchronized (_classes) {
            if ((clazz = _classes.get(className)) == null) {
                _classes.put(className, clazz = _loader.loadClass(className));
            }
        }
        return clazz;
    }

    /**
     * Parses the mode.
     *
     * @param mode
     *            the mode
     *
     * @return the int
     *
     * @throws NumberFormatException
     *             the number format exception
     */
    public static int parseMode(String mode) throws NumberFormatException {
        var imode = Integer.parseInt(mode);
        if (imode < 0) {
            imode = imode * -1;
        }
        mode = String.valueOf(imode);
        for (var i = 0; i < mode.length(); i++) {
            final var digit = Integer.parseInt(mode.substring(i, i + 1));
            if (digit > 7) {
                throw new NumberFormatException(mode);
            }
        }
        return imode;
    }

    /**
     * Parses the parameter.
     *
     * @param curCon
     *            the cur con
     * @param command
     *            the command
     * @param request
     *            the request
     *
     * @return the string
     */
    public static String parseParameter(final CurrentContext curCon, final Class<?> command, String request) {
        var name = command.getName();
        name = name.substring(name.lastIndexOf(".") + 1);
        if ((request = request.trim()).length() == 0) {
            curCon.respond(501, name + " parse error");
            return null;
        }
        return request;
    }

    /**
     * Gets the path.
     *
     * @param currentContext
     *            the current context
     * @param path
     *            the path
     * @param update
     *            the update
     * @param file
     *            the file
     *
     * @return the path
     *
     * @throws FileNotFoundException
     *             the file not found exception
     */
    public static String getPath(final CurrentContext currentContext, String path, final boolean update,
            final boolean file) throws FileNotFoundException {
        final var upath = path.toUpperCase();
        for (final String ext : Cnf.listAt("FtpPlugin", "ext")) {
            if (upath.startsWith(ext.toUpperCase() + ":")) {
                return path;
            }
        }
        final var root = path.startsWith("/");
        path = (root ? "" : currentContext.getPath() + '/') + path;
        if (currentContext.browser && !"*".equals(currentContext.domainName)) {
            path = (root ? "" : "/" + currentContext.domainName.toUpperCase() + "/") + path;
        }
        path = Format.normalizePath(path);
        var domainValue = currentContext.domainValue;
        if (currentContext.browser) {
            if ("/".equals(path)) {
                throw new FileNotFoundException("*");
            }
            final var token = new StringTokenizer(path, "/");
            final var domainName = token.nextToken();
            if (file && ("/" + domainName).equals(path)) {
                throw new FileNotFoundException("*");
            }
            domainValue = DOMAIN.getDomainValue(domainName);
            if (domainValue == null) {
                throw new FileNotFoundException(domainName);
            }
            path = path.substring(domainName.length() + 1);
            if (update) {
                currentContext.domainName = domainName;
                currentContext.domainValue = domainValue;
            }
        }
        return "[" + currentContext.domainUser + "]" + domainValue + path;
    }

    /**
     * Gets the message with eol.
     *
     * @param currentContext
     *            the current context
     * @param message
     *            the message
     *
     * @return the message with eol
     */
    public static String getMessageWithEOL(final CurrentContext currentContext, final String message) {
        final var s = new StringBuilder();
        try {
            final var dis = new BufferedReader(new StringReader(message));
            String line;
            try {
                while ((line = dis.readLine()) != null) {
                    s.append(line).append(currentContext.transferEOL());
                }
            } catch (final Exception e) {
            }
        } catch (final Exception ignored) {
        }
        return s.toString();
    }
}
