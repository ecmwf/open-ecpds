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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Allow executing javascript or python code.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Period;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.script.ScriptException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.PolyglotException.StackFrame;
import org.graalvm.polyglot.ResourceLimits;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

/**
 * The Class ScriptManager.
 */
public final class ScriptManager implements AutoCloseable {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ScriptManager.class);

    /** The Constant LONG_RUNNING_TIME. */
    private static final long LONG_RUNNING_TIME = Cnf.at("ScriptManager", "longRunningTime", 1000L);

    /** The Constant JS. */
    public static final String JS = "js";

    /** The Constant PYTHON. */
    public static final String PYTHON = "python";

    /** The Constant SHARED_ENGINE. */
    private static final Engine SHARED_ENGINE = Engine.newBuilder(JS, PYTHON)
            .allowExperimentalOptions(Cnf.at("ScriptManager", "allowExperimentalOptions", true))
            .option("engine.WarnVirtualThreadSupport", Cnf.stringAt("ScriptManager", "warnVirtualThreadSupport", false))
            .option("engine.Compilation", Cnf.stringAt("ScriptManager", "engineCompilation", false)).build();

    /** The Constant CONTEXT_PROVIDER. */
    private static final ContextProvider CONTEXT_PROVIDER = new ContextProvider(
            Cnf.at("ScriptManager", "enableContextSpool", true), Cnf.at("ScriptManager", "resourceLimits", 0));

    /** The Constant DEBUG_POOL. */
    private static final boolean DEBUG_POOL = Cnf.at("ScriptManager", "debugPool", false);

    /** The Constant DEBUG_FREQUENCY. */
    private static final int DEBUG_FREQUENCY = Cnf.at("ScriptManager", "debugFrequency", 1_000_000);

    /**
     * The Constant EXPOSED_CLASSES. Classes which are allowed to be used within the scripts.
     */
    private static final Class<?>[] EXPOSED_CLASSES = new Class[] { UUID.class, URL.class, StringBuffer.class,
            BufferedReader.class, InputStreamReader.class };

    /** The current language. */
    private final String currentLanguage;

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
         *
         * @param currentLanguage
         *            the current language
         */
        void close(final String currentLanguage) {
            releaseContext(context, currentLanguage);
        }
    }

    /** The cache. */
    private Cache cache = null;

    /**
     * Wraps the given script content to avoid polluting the global scope. Supports encapsulation for JavaScript and
     * Python.
     *
     * @param scriptBody
     *            The script content to wrap.
     *
     * @return Wrapped script that runs in an isolated scope.
     */
    private String wrapScript(final String scriptBody) {
        // Return as-is if script or language is not defined
        if (currentLanguage == null || scriptBody == null)
            return scriptBody;
        // Normalize line endings to Unix format to avoid syntax issues (e.g., with
        // Python or JavaScript)
        final var normalizedScript = scriptBody.replaceAll("\\r\\n?", "\n");
        // Choose wrapper based on language
        return switch (currentLanguage) {
        case JS -> wrapJavaScript(normalizedScript); // JavaScript: wrap in IIFE
        case PYTHON -> wrapPythonScript(normalizedScript); // Python: wrap in function
        default -> normalizedScript; // Other: return cleaned script
        };
    }

    /**
     * Wraps the provided JavaScript code in an immediately-invoked function expression (IIFE) to prevent polluting the
     * global context and to capture the script's final expression as the result.
     *
     * This wrapper ensures that:
     * <ul>
     * <li>Temporary variables or function declarations do not leak into the global scope.</li>
     * <li>The result of the last evaluated expression in the script is returned to the caller.</li>
     * </ul>
     *
     * Note: The scriptBody should end with a meaningful expression if a result is expected.
     *
     * @param scriptBody
     *            the JavaScript code to wrap
     *
     * @return a wrapped version of the script, ready for evaluation in a GraalVM context
     */
    private static String wrapJavaScript(final String scriptBody) {
        return "(() => {\n" + indent(scriptBody, 1) + "\n" + "})()";
    }

    /**
     * Wraps the provided Python code in a uniquely named function to isolate its scope and avoid polluting the global
     * namespace. The function is then called immediately, its result captured in a variable, and the function
     * definition is deleted to clean up.
     *
     * This wrapper ensures that:
     * <ul>
     * <li>Temporary variables or function definitions inside the script do not remain in the global scope.</li>
     * <li>The script's final expression (or an explicit return) is captured and returned as the result.</li>
     * </ul>
     *
     * Note: The scriptBody should use a return statement to provide a result from the function.
     *
     * @param scriptBody
     *            the Python code to wrap
     *
     * @return a wrapped version of the script, ready for evaluation in a GraalVM Python context
     */
    private static String wrapPythonScript(final String scriptBody) {
        final var wrapperName = "__wrapper_" + UUID.randomUUID().toString().replace("-", "");
        final var wrapperFunction = "_" + wrapperName + "__";
        final var resultVar = wrapperName + "_result__";
        // Use newline-separated string concatenation instead of text blocks
        return "def " + wrapperFunction + "():\n" + indent(scriptBody, 1) + "\n" + resultVar + " = " + wrapperFunction
                + "()\n" + "del " + wrapperFunction + "\n" + resultVar;
    }

    /**
     * Indents each line of a script with 4 spaces so it can be placed inside a function definition.
     *
     * @param body
     *            the body
     * @param levels
     *            the levels
     *
     * @return Indented Python code for use inside a function
     */
    private static String indent(final String body, final int levels) {
        final var prefix = "    ".repeat(levels); // 4 spaces per level
        return Arrays.stream(body.split("\n")).map(line -> prefix + line).collect(Collectors.joining("\n"));
    }

    /**
     * Ensures that the last meaningful line of a script is returned. Supports both JavaScript and Python.
     *
     * @param scriptBody
     *            The script content to wrap.
     *
     * @return Wrapped script that runs in an isolated scope.
     */
    private String addReturnToLastExpression(final String scriptBody) {
        // Return as-is if script or language is not defined
        if (currentLanguage == null || scriptBody == null)
            return scriptBody;
        // Normalize line endings to Unix format to avoid syntax issues (e.g., with
        // Python or JavaScript)
        final var normalizedScript = scriptBody.replaceAll("\\r\\n?", "\n");
        // Choose method based on language
        return switch (currentLanguage) {
        case JS -> addReturnToLastExpressionJS(normalizedScript);
        case PYTHON -> addReturnToLastExpressionPython(normalizedScript);
        default -> normalizedScript; // Other: return cleaned script
        };
    }

    /**
     * Ensures that the last meaningful line of a JavaScript code snippet is returned when the script is wrapped inside
     * a function.
     * <p>
     * GraalVM automatically evaluates the last expression of a script, but this behavior is lost when the script is
     * wrapped in a function. This method rewrites the last non-comment, non-blank line as a return statement if it is a
     * standalone expression.
     *
     * @param jsCode
     *            the original JavaScript code snippet
     *
     * @return the modified code with a return statement on the last expression if needed
     */
    private static String addReturnToLastExpressionJS(final String jsCode) {
        final var lines = jsCode.split("\\r?\\n");
        final var result = new StringBuilder();
        // Find the index of the last meaningful line
        var lastExprIndex = -1;
        for (var i = lines.length - 1; i >= 0; i--) {
            final var line = lines[i].trim();
            if (!line.isEmpty() && !line.startsWith("//")) {
                lastExprIndex = i;
                break;
            }
        }
        for (var i = 0; i < lines.length; i++) {
            if (i == lastExprIndex) {
                final var trimmed = lines[i].trim();

                // Already a return?
                if (trimmed.startsWith("return ")) {
                    result.append(lines[i]);
                } else {
                    result.append("return ").append(trimmed.replaceAll(";+\\s*$", "")).append(";");
                }
            } else {
                result.append(lines[i]);
            }
            if (i < lines.length - 1) {
                result.append("\n");
            }
        }
        return result.toString();
    }

    /**
     * Ensures that the last meaningful line of a Python code snippet is returned when wrapped in a function. If the
     * last non-comment, non-blank line is a bare expression (e.g., a string or variable), it is replaced with a return
     * statement.
     *
     * Assumes the code is top-level (not indented blocks).
     *
     * @param pyCode
     *            the original Python code snippet
     *
     * @return the modified code with a return statement on the last expression if needed
     */
    private static String addReturnToLastExpressionPython(final String pyCode) {
        final var lines = pyCode.split("\\r?\\n");
        final var result = new StringBuilder();
        var lastExprIndex = -1;
        for (var i = lines.length - 1; i >= 0; i--) {
            final var line = lines[i].trim();
            if (!line.isEmpty() && !line.startsWith("#")) {
                lastExprIndex = i;
                break;
            }
        }
        for (var i = 0; i < lines.length; i++) {
            if (i == lastExprIndex) {
                final var trimmed = lines[i].trim();

                if (trimmed.startsWith("return ") || trimmed.equals("return")) {
                    result.append(lines[i]);
                } else {
                    result.append("return ").append(trimmed);
                }
            } else {
                result.append(lines[i]);
            }
            if (i < lines.length - 1) {
                result.append("\n");
            }
        }
        return result.toString();
    }

    /**
     * Exec. If no language is specified in the header of the script then the default language is used (e.g.
     * script=js:code or script=python:code). Also add a return is missing from the last expression of the script.
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
            return manager.eval(clazz, manager.addReturnToLastExpression(script.substring(index)), null);
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
     * @param parameter
     *            the parameter
     *
     * @return the t
     *
     * @throws ScriptException
     *             the script exception
     */
    public <T> T eval(final Class<T> clazz, final String script, final String parameter) throws ScriptException {
        return eval(clazz, eval(script), parameter);
    }

    /**
     * Eval.
     *
     * @param <T>
     *            the generic type
     * @param clazz
     *            the clazz
     * @param value
     *            the value
     * @param parameter
     *            the parameter
     *
     * @return the t
     *
     * @throws ScriptException
     *             the script exception
     */
    public static <T> T eval(final Class<T> clazz, final Value value, final String parameter) throws ScriptException {
        return cast(clazz, parameter != null ? getNestedValue(value, parameter) : value);
    }

    /**
     * Eval.
     *
     * @param script
     *            the script
     *
     * @return the t
     *
     * @throws ScriptException
     *             the script exception
     */
    public Value eval(final String script) throws ScriptException {
        final var start = System.currentTimeMillis();
        final Value value;
        try {
            value = getCache().context.eval(Source.create(currentLanguage, wrapScript(script)));
        } catch (final PolyglotException e) {
            throw getMessage("Eval " + currentLanguage + ": " + script, e);
        }
        if (_log.isDebugEnabled()) {
            final var duration = System.currentTimeMillis() - start;
            if (duration > LONG_RUNNING_TIME) {
                _log.debug("Time taken: {} ms", duration);
            }
        }
        return value;
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
     * @return the script manager
     *
     * @throws ScriptException
     *             the script exception
     */
    public ScriptManager put(final Map<String, Object> context) throws ScriptException {
        for (final Map.Entry<String, Object> entry : context.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * Gets the nested value.
     *
     * @param root
     *            the root
     * @param path
     *            the path
     *
     * @return the nested value
     */
    public static Value getNestedValue(final Value root, final String path) {
        if (root == null || !root.hasMembers())
            return null;
        final var keys = path.split("\\.");
        var current = root;
        for (final String key : keys) {
            if (!current.hasMember(key)) {
                return null;
            }
            current = current.getMember(key);
        }
        return current;
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
            final var tmp = new Cache();
            try {
                tmp.context = CONTEXT_PROVIDER.acquire();
                tmp.bindings = tmp.context.getBindings(currentLanguage);
                for (final Class<?> clazz : EXPOSED_CLASSES)
                    tmp.bindings.putMember(clazz.getSimpleName(), clazz);
                // Allow logging to the general log
                tmp.bindings.putMember("log", _log);
                this.cache = tmp;
            } catch (final Throwable e) {
                final var message = "Failed to initialize script context";
                _log.warn(message, e);
                releaseContext(tmp.context, currentLanguage); // Prevent memory leak
                throw e instanceof final ScriptException scriptException ? scriptException
                        : new ScriptException(message);
            }
        }
        return cache;
    }

    /**
     * Close context.
     *
     * @param context
     *            the context
     * @param currentLanguage
     *            the current language
     */
    private static void releaseContext(final Context context, final String currentLanguage) {
        if (context != null)
            CONTEXT_PROVIDER.release(context, currentLanguage);
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
        for (final Class<?> clazz : EXPOSED_CLASSES) {
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
        if (clazz == null)
            return null;
        // No return from the script?
        if (value.isNull() || "undefined".equals(value.toString()))
            throw new ScriptException("No return from script or null/undefined object");
        try {
            return cast(value, clazz);
        } catch (final ClassCastException _) {
            throw new ScriptException("Not a " + clazz.getSimpleName() + " output (" + value.toString() + ")");
        }
    }

    /**
     * Cast the value in the requested type.
     *
     * @param <T>
     *            the generic type
     * @param value
     *            the value
     * @param clazz
     *            the clazz
     *
     * @return the object of type class
     */
    @SuppressWarnings({ "unchecked" })
    private static <T> T cast(final Value value, final Class<T> clazz) {
        if (clazz == String.class) {
            return (T) value.asString();
        } else if (clazz == Integer.class) {
            return (T) Integer.valueOf(value.asInt());
        } else if (clazz == Double.class) {
            return (T) Double.valueOf(value.asDouble());
        } else if (clazz == Long.class) {
            return (T) Long.valueOf(value.asLong());
        } else if (clazz == Boolean.class) {
            return (T) Boolean.valueOf(value.asBoolean());
        } else if (clazz == ByteSize.class) {
            return (T) ByteSize.of(value.asLong());
        } else if (clazz == Duration.class) {
            return (T) Duration.ofMillis(value.asLong());
        } else if (clazz == Period.class) {
            return (T) Period.parse(value.asString());
        } else if (clazz == TimeRange.class) {
            return (T) TimeRange.parse(value.asString());
        } else {
            // No matching conversion is found, return default casting!
            _log.warn("Unsuported option type: {}", clazz);
            return value.as(clazz);
        }
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
            cache.close(currentLanguage);
        }
    }

    /**
     * Shutdown.
     */
    public static void shutdown() {
        CONTEXT_PROVIDER.shutdown();
    }

    /**
     * The Class ContextProvider.
     */
    private static class ContextProvider {

        /** The null output stream. */
        private static final OutputStream nullOutputStream = OutputStream.nullOutputStream();

        /** The null input stream. */
        private static final InputStream nullInputStream = InputStream.nullInputStream();

        /** The pool. */
        private final ConcurrentLinkedQueue<Context> pool = new ConcurrentLinkedQueue<>();

        /** The created count. */
        private final AtomicInteger createdCount = new AtomicInteger(0);

        /** The Constant format. */
        static final DecimalFormat format = new DecimalFormat("0.00");

        /** The startup. */
        final long startup = System.currentTimeMillis();

        /** The acquired. */
        final AtomicLong acquired = new AtomicLong(0);

        /** The released. */
        final AtomicLong released = new AtomicLong(0);

        /** The resource limits. */
        private final ResourceLimits resourceLimits;

        /** The use context spool. */
        private final boolean useContextSpool;

        /**
         * Instantiates a new context provider.
         *
         * @param useContextSpool
         *            the use context spool
         * @param limits
         *            the limits
         */
        ContextProvider(final boolean useContextSpool, final int limits) {
            this.useContextSpool = useContextSpool;
            this.resourceLimits = limits > 0 ? ResourceLimits.newBuilder().statementLimit(limits, null).build() : null;
        }

        /**
         * Creates the new context.
         *
         * @return the context
         */
        private Context createNewContext() {
            final var builder = Context.newBuilder(JS, PYTHON).engine(SHARED_ENGINE).allowHostAccess(HostAccess.ALL)
                    .allowHostClassLookup(ScriptManager::check).useSystemExit(false).allowCreateProcess(false)
                    .allowCreateThread(false).in(nullInputStream).out(nullOutputStream).err(nullOutputStream);
            if (resourceLimits != null)
                builder.resourceLimits(resourceLimits);
            return builder.build();
        }

        /**
         * Gets the avg per sec.
         *
         * @param count
         *            the count
         *
         * @return the avg per sec
         */
        private String getAvgPerSec(final AtomicLong count) {
            return format.format(count.get() / ((System.currentTimeMillis() - startup) / 1000d));
        }

        /**
         * Acquire.
         *
         * @return the context
         */
        Context acquire() {
            var context = pool.poll();
            if (context == null) {
                _log.debug("Context created: {}", createdCount.incrementAndGet());
                context = createNewContext();
            } else {
                if (DEBUG_POOL && acquired.updateAndGet(c -> c == Long.MAX_VALUE ? 1 : c + 1) % DEBUG_FREQUENCY == 0
                        && _log.isDebugEnabled()) {
                    _log.debug("Context pool: Acquired: {} ({}/sec avg) Released: {} ({}/sec avg)", acquired,
                            getAvgPerSec(acquired), released, getAvgPerSec(released));
                }
            }
            return context;
        }

        /**
         * Release.
         *
         * @param context
         *            the context
         * @param currentLanguage
         *            the current language
         */
        void release(final Context context, final String currentLanguage) {
            resetBindings(context, currentLanguage);
            if (!(useContextSpool && pool.offer(context))) {
                _log.debug("Context closed: {}", createdCount.decrementAndGet());
                context.close();
            } else {
                if (DEBUG_POOL && released.updateAndGet(c -> c == Long.MAX_VALUE ? 1 : c + 1) % DEBUG_FREQUENCY == 0
                        && _log.isDebugEnabled()) {
                    _log.debug("Context pool: Acquired: {} ({}/sec avg) Released: {} ({}/sec avg)", acquired,
                            getAvgPerSec(acquired), released, getAvgPerSec(released));
                }
            }
        }

        /**
         * Reset bindings.
         *
         * @param context
         *            the context
         * @param currentLanguage
         *            the current language
         */
        private void resetBindings(final Context context, final String currentLanguage) {
            // Clear current bindings
            final var bindings = context.getBindings(currentLanguage);
            for (final String key : bindings.getMemberKeys()) {
                try {
                    bindings.removeMember(key);
                } catch (final UnsupportedOperationException _) {
                    // skip non-removable keys, like built-ins
                }
            }
        }

        /**
         * Shutdown.
         */
        void shutdown() {
            pool.forEach(Context::close);
            pool.clear();
        }
    }
}
