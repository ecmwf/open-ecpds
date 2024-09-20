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

package ecmwf.common.technical;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * Allow executing javascript or python code.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.script.ScriptException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.PolyglotException.StackFrame;
import org.graalvm.polyglot.ResourceLimits;
import org.graalvm.polyglot.Value;

/**
 * The Class ScriptManager.
 */
public final class ScriptManager implements AutoCloseable {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ScriptManager.class);

    /** The Constant LONG_RUNNING_TIME. */
    public static final long LONG_RUNNING_TIME = Cnf.at("ScriptManager", "longRunningTime", 1000L);

    /** The Constant JS. */
    public static final String JS = "js";

    /** The Constant PYTHON. */
    public static final String PYTHON = "python";

    /**
     * The Constant exposedClasses. Classes which are allowed to be used within the scripts.
     */
    private static final Class<?>[] exposedClasses = new Class[] { UUID.class, URL.class, StringBuffer.class,
            BufferedReader.class, InputStreamReader.class };

    /** The current language. */
    private final String currentLanguage;

    /** The resource limits. */
    private final ResourceLimits resourceLimits;

    /** The closed. */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * The Class Cache.
     */
    private static final class Cache {

        /** The context. */
        Context context;

        /** The bindings. */
        Value bindings;

        /**
         * Close.
         */
        void close() {
            if (context != null) {
                context.close();
            }
        }
    }

    /** The cache. */
    private Cache cache = null;

    /**
     * Exec. If no language is specified in the header of the script then the default language is used (e.g.
     * script=js:code or script=python:code).
     *
     * @param <T>
     *            the generic type
     * @param clazz
     *            the clazz
     * @param defaultLanguage
     *            the default language
     * @param script
     *            the script
     *
     * @return the t
     *
     * @throws ScriptException
     *             the script exception
     */
    public static <T> T exec(final Class<T> clazz, final String defaultLanguage, final String script)
            throws ScriptException {
        final var toLowerCase = script.toLowerCase();
        final String language;
        final int index;
        if (toLowerCase.startsWith(JS + ":")) {
            language = JS;
            index = JS.length() + 1;
        } else if (toLowerCase.startsWith(PYTHON + ":")) {
            language = PYTHON;
            index = PYTHON.length() + 1;
        } else {
            language = defaultLanguage;
            index = 0;
        }
        try (final var manager = new ScriptManager(language)) {
            return manager.eval(clazz, script.substring(index));
        }
    }

    /**
     * Exec. If no language is specified in the header of the script then the default language is used (e.g.
     * script=js:code or script=python:code).
     *
     * @param defaultLanguage
     *            the default language
     * @param script
     *            the script
     *
     * @throws ScriptException
     *             the script exception
     */
    public static void exec(final String defaultLanguage, final String script) throws ScriptException {
        exec(null, defaultLanguage, script);
    }

    /**
     * Instantiates a new script manager. Uses the requested script engine.
     *
     * @param language
     *            the language
     */
    public ScriptManager(final String language) {
        resourceLimits = null;
        currentLanguage = language;
    }

    /**
     * Instantiates a new script manager. Uses the requested script engine with a limit.
     *
     * @param limit
     *            the limit
     * @param language
     *            the language
     */
    public ScriptManager(final long limit, final String language) {
        // The limits are only supported with JavaScript
        resourceLimits = JS.equals(language) ? ResourceLimits.newBuilder().statementLimit(limit, null).build() : null;
        currentLanguage = language;
    }

    /**
     * Eval.
     *
     * @param <T>
     *            the generic type
     * @param clazz
     *            the clazz
     * @param script
     *            the script
     *
     * @return the t
     *
     * @throws ScriptException
     *             the script exception
     */
    public <T> T eval(final Class<T> clazz, final String script) throws ScriptException {
        final var start = System.currentTimeMillis();
        final Value value;
        try {
            value = getCache().context.eval(currentLanguage, script);
        } catch (final PolyglotException e) {
            throw getMessage("Eval " + currentLanguage + ": " + script, e);
        }
        if (_log.isDebugEnabled()) {
            final var duration = System.currentTimeMillis() - start;
            if (duration > LONG_RUNNING_TIME) {
                _log.debug("Time taken: {} ms", duration);
            }
        }
        return cast(clazz, value);
    }

    /**
     * Eval.
     *
     * @param script
     *            the script
     *
     * @throws ScriptException
     *             the script exception
     */
    public void eval(final String script) throws ScriptException {
        eval(null, script);
    }

    /**
     * Put.
     *
     * @param key
     *            the key
     * @param value
     *            the value
     *
     * @throws ScriptException
     *             the script exception
     */
    public void put(final String key, final Object value) throws ScriptException {
        try {
            getCache().bindings.putMember(key, value);
        } catch (final PolyglotException e) {
            throw getMessage("GetBindings/PutMember " + currentLanguage + ": " + key + "=" + value, e);
        }
    }

    /**
     * Put.
     *
     * @param context
     *            the context
     *
     * @throws ScriptException
     *             the script exception
     */
    public void put(final Map<String, Object> context) throws ScriptException {
        for (final Map.Entry<String, Object> entry : context.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Gets the value, execute it with the provided arguments if it is a function and then cast it to the desired class.
     *
     * @param <T>
     *            the generic type
     * @param clazz
     *            the clazz
     * @param name
     *            the name
     * @param args
     *            the args
     *
     * @return the t
     *
     * @throws ScriptException
     *             the script exception
     */
    public <T> T get(final Class<T> clazz, final String name, final Object... args) throws ScriptException {
        final var start = System.currentTimeMillis();
        final Value value;
        try {
            final var eval = getCache().context.eval(currentLanguage, name);
            value = eval.canExecute() ? eval.execute(args) : eval;
        } catch (final PolyglotException e) {
            throw getMessage("Eval/Execute " + currentLanguage + ": " + clazz + " <- " + name + "(" + args + ")", e);
        }
        if (_log.isDebugEnabled()) {
            final var duration = System.currentTimeMillis() - start;
            if (duration > LONG_RUNNING_TIME) {
                _log.debug("Time taken: {} ms", duration);
            }
        }
        return cast(clazz, value);
    }

    /**
     * Gets the cache. If the context does not exists then create it and add the exposed classes!
     *
     * @return the cache
     *
     * @throws ScriptException
     *             the script exception
     */
    private synchronized Cache getCache() throws ScriptException {
        if (cache == null) {
            cache = new Cache();
            final var builder = Context.newBuilder(currentLanguage);
            builder.useSystemExit(false);
            if (resourceLimits != null) {
                builder.resourceLimits(resourceLimits);
            }
            final var context = builder.allowHostAccess(HostAccess.ALL).allowHostClassLookup(ScriptManager::check)
                    .build();
            cache.context = context;
            cache.bindings = context.getBindings(currentLanguage);
            for (final Class<?> clazz : exposedClasses) {
                final var simpleName = clazz.getSimpleName();
                final var fullName = clazz.getCanonicalName();
                final String statement;
                if (JS.equals(currentLanguage)) {
                    statement = "Java.type('" + fullName + "')";
                } else if (PYTHON.equals(currentLanguage)) {
                    statement = "import " + fullName + " as " + simpleName;
                } else {
                    statement = "";
                }
                if (!statement.isBlank()) {
                    put(simpleName, context.eval(currentLanguage, statement));
                }
            }
            // Allow logging to the general log
            put("log", _log);
        }
        return cache;
    }

    /**
     * Gets the properties. Just for debugging (values are shorten with ...).
     *
     * @return the properties
     *
     * @throws ScriptException
     *             the script exception
     */
    public Map<String, String> getProperties() throws ScriptException {
        final var result = new HashMap<String, String>();
        try {
            final var value = getCache().bindings;
            for (final String property : value.getMemberKeys()) {
                result.put(property, value.getMember(property).toString());
            }
        } catch (final PolyglotException e) {
            throw getMessage("GetBindings/GetMember: " + currentLanguage, e);
        }
        return result;
    }

    /**
     * Called whenever the underlying script is trying to access an external class. It the external class is not listed
     * in the exposed classes then the permission is denied!
     *
     * @param className
     *            the class name
     *
     * @return true, if successful
     */
    private static boolean check(final String className) {
        for (final Class<?> clazz : exposedClasses) {
            if (clazz.getCanonicalName().equals(className)) {
                return true;
            }
        }
        // Not found
        _log.warn("Rejected class: {}", className);
        return false;
    }

    /**
     * Cast.
     *
     * @param <T>
     *            the generic type
     * @param clazz
     *            the clazz
     * @param value
     *            the value
     *
     * @return the t
     *
     * @throws ScriptException
     *             the script exception
     */
    private static <T> T cast(final Class<T> clazz, final Value value) throws ScriptException {
        // No return requested (void)
        if (clazz == null) {
            return null;
        }
        // No object defined?
        if (value.isNull()) {
            throw new ScriptException("No output from script");
        }
        try {
            return value.as(clazz);
        } catch (final ClassCastException e) {
            throw new ScriptException("Not a " + clazz.getSimpleName() + " output");
        }
    }

    /**
     * Checks if a variable exists in the underlying script (e.g. mqtt.message.qos).
     *
     * @param name
     *            the name
     *
     * @return true, if successful
     *
     * @throws ScriptException
     *             the script exception
     */
    public boolean exists(final String name) throws ScriptException {
        return !"undefined".equals(getCache().context.eval(currentLanguage, "typeof " + name).asString());
    }

    /**
     * Gets the message.
     *
     * @param message
     *            the message
     * @param e
     *            the e
     *
     * @return the message
     */
    private static ScriptException getMessage(final String message, final PolyglotException e) {
        _log.warn(message, e);
        final var trace = new StringBuilder();
        for (final StackFrame stack : e.getPolyglotStackTrace()) {
            final var language = stack.getLanguage();
            if (language != null && !"host".equals(language.getId())) {
                trace.append(trace.length() > 0 ? " <- " : "").append(stack.toString());
            }
        }
        return new ScriptException(e.getMessage() + (trace.length() > 0 ? " <- " + trace.toString() : ""));
    }

    /**
     * Close.
     */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true) && cache != null) {
            cache.close();
        }
    }
}
