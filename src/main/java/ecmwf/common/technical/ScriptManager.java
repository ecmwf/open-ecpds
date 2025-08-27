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
import java.time.Duration;
import java.time.Period;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.script.ScriptException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Context.Builder;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.ResourceLimits;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

/**
 * The Class ScriptManager.
 */
public final class ScriptManager implements AutoCloseable {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ScriptManager.class);

    /** The Constant RUNNING_TIMEOUT. */
    private static final long RUNNING_TIMEOUT = Cnf.at("ScriptManager", "runningTimeout", 10_000);

    /** The Constant LONG_RUNNING_TIME. */
    private static final long LONG_RUNNING_TIME = Cnf.at("ScriptManager", "longRunningTime", 1_000);

    /** The Constant JS. */
    public static final String JS = "js";

    /** The Constant PYTHON. */
    public static final String PYTHON = "python";

    /**
     * The Constant EXPOSED_CLASSES. Classes which are allowed to be used within the scripts.
     */
    private static final Class<?>[] EXPOSED_CLASSES = new Class[] { UUID.class, URL.class, StringBuffer.class,
            BufferedReader.class, InputStreamReader.class };

    /** The Constant CONTEXT_PROVIDER. */
    private static final ContextProvider CONTEXT_PROVIDER = new ContextProvider(
            Cnf.at("ScriptManager", "resourceLimits", 0), Cnf.at("ScriptManager", "useSharedEngine", true),
            Cnf.at("ScriptManager", "allowExperimentalOptions", false),
            Cnf.stringAt("ScriptManager", "warnVirtualThreadSupport", false),
            Cnf.stringAt("ScriptManager", "engineCompilation", true));

    /** The Constant ALLOW_VIRTUAL_THREAD. */
    private static final boolean ALLOW_VIRTUAL_THREAD = Cnf.at("ScriptManager", "allowVirtualThread", false);

    /** The Constant CASTERS. */
    private static final Map<Class<?>, Function<Value, ?>> CASTERS = new HashMap<>();

    static {
        CASTERS.put(String.class, Value::asString);
        CASTERS.put(Integer.class, Value::asInt);
        CASTERS.put(Double.class, Value::asDouble);
        CASTERS.put(Long.class, Value::asLong);
        CASTERS.put(Boolean.class, Value::asBoolean);
        CASTERS.put(ByteSize.class, v -> ByteSize.of(v.asLong()));
        CASTERS.put(Duration.class, v -> Duration.ofMillis(v.asLong()));
        CASTERS.put(Period.class, v -> Period.parse(v.asString()));
        CASTERS.put(TimeRange.class, v -> TimeRange.parse(v.asString()));
    }

    /** The current language. */
    private final String currentLanguage;

    /** The closed. */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * The Class Cache.
     */
    private final class Cache implements AutoCloseable {

        /** The Constant TRACKER. */
        private static final ResourceTracker TRACKER = new ResourceTracker(Cache.class);

        /** The released flag. */
        private final AtomicBoolean released = new AtomicBoolean(false);

        /** The context. */
        Context context;

        /** The bindings. */
        Value bindings;

        /**
         * Instantiates a new cache.
         *
         * @throws ScriptException
         *             the script exception
         */
        Cache() throws ScriptException {
            try {
                context = CONTEXT_PROVIDER.acquire();
                bindings = context.getBindings(currentLanguage);
                for (final Class<?> clazz : EXPOSED_CLASSES)
                    bindings.putMember(clazz.getSimpleName(), clazz);
                // Allow logging to the general log
                bindings.putMember("log", ProxyObject.fromMap(Map.of("debug", (ProxyExecutable) args -> {
                    if (args.length > 0)
                        _log.debug(args[0].asString());
                    return null;
                }, "info", (ProxyExecutable) args -> {
                    if (args.length > 0)
                        _log.info(args[0].asString());
                    return null;
                }, "warn", (ProxyExecutable) args -> {
                    if (args.length > 0)
                        _log.warn(args[0].asString());
                    return null;
                }, "error", (ProxyExecutable) args -> {
                    if (args.length > 0)
                        _log.error(args[0].asString());
                    return null;
                })));
                TRACKER.onOpen();
            } catch (final Throwable e) {
                final var message = "Failed to initialize script context";
                _log.warn(message, e);
                release(); // Prevent memory leak
                throw e instanceof final ScriptException scriptException ? scriptException
                        : new ScriptException(message);
            }
        }

        /**
         * Release.
         */
        void release() {
            if (released.compareAndSet(false, true) && context != null) {
                CONTEXT_PROVIDER.release(context, currentLanguage);
                context = null;
                bindings = null;
            }
        }

        /**
         * Close.
         */
        public void close() {
            var closedSuccessfully = false;
            try {
                release();
                closedSuccessfully = true;
            } catch (Exception e) {
                _log.warn("Failed to close context cleanly", e);
            } finally {
                TRACKER.onClose(closedSuccessfully);
            }
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
     * Wraps the given JavaScript code in an Immediately Invoked Function Expression (IIFE), ensuring that a predefined
     * set of Java classes (listed in {@code EXPOSED_CLASSES}) are made available in the script via {@code Java.type()}
     * bindings.
     * <p>
     * If the provided script is already wrapped in an IIFE (i.e. starts with {@code (() => {})(})}), the method injects
     * the preamble inside the existing wrapper instead of wrapping it again.
     *
     * @param scriptBody
     *            The JavaScript code to be wrapped and prefixed with type bindings.
     *
     * @return The script wrapped in an IIFE with exposed Java types, unless already wrapped.
     */
    private static String wrapJavaScript(final String scriptBody) {
        final var trimmed = scriptBody.trim();
        final var preamble = Arrays.stream(EXPOSED_CLASSES)
                .map(clazz -> "var " + clazz.getSimpleName() + " = Java.type('" + clazz.getName() + "');")
                .collect(Collectors.joining("\n"));
        if (trimmed.startsWith("(() => {") && trimmed.endsWith("})()")) {
            // Already wrapped, inject preamble after the opening {
            final var innerBody = trimmed.substring("(() => {".length(), trimmed.length() - "})()".length()).trim();
            return "(() => {\n" + indent(preamble, 1) + "\n\n" + indent(innerBody, 1) + "\n" + "})()";
        } else {
            return "(() => {\n" + indent(preamble, 1) + "\n\n" + indent(scriptBody, 1) + "\n" + "})()";
        }
    }

    /**
     * Wraps a Python script body in an isolated function context, and injects import statements for Java classes
     * exposed via {@code exposedClasses}. The Java classes are imported using the GraalVM-specific syntax:
     * {@code import fully.qualified.ClassName as ClassName}.
     * <p>
     * The script is wrapped in a uniquely named function to avoid name collisions and is executed immediately. The
     * result is stored in a uniquely named variable, and the function is deleted afterward to minimize global scope
     * pollution.
     *
     * @param scriptBody
     *            the body of the Python script to wrap and execute
     *
     * @return a complete Python script string with injected Java imports and execution wrapper
     */
    private static String wrapPythonScript(final String scriptBody) {
        final var wrapperName = "__wrapper_" + UUID.randomUUID().toString().replace("-", "");
        final var wrapperFunction = "_" + wrapperName + "__";
        final var resultVar = wrapperName + "_result__";
        final var preamble = Arrays.stream(EXPOSED_CLASSES)
                .map(clazz -> "import " + clazz.getName() + " as " + clazz.getSimpleName())
                .collect(Collectors.joining("\n"));
        return preamble + "\n\n" + "def " + wrapperFunction + "():\n" + indent(scriptBody, 1) + "\n" + resultVar + " = "
                + wrapperFunction + "()\n" + "del " + wrapperFunction + "\n" + resultVar;
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
     * Ensures that any single line in a JavaScript include a return.
     *
     * @param scriptBody
     *            The script content to process.
     *
     * @return script body with return added if required.
     */
    private String addMissingReturnToSingleLineJSExpression(final String scriptBody) {
        // Return as-is if language is not JavaScript
        if (!JS.equals(currentLanguage))
            return scriptBody;
        final var trimmed = scriptBody.replaceAll("\\r\\n?", "\n").trim();
        // Check if it's a single line (no newline chars)
        if (!trimmed.contains("\n")) {
            // Add "return " if not already present
            return trimmed.startsWith("return ") ? trimmed : "return " + trimmed;
        }
        return trimmed;
    }

    /**
     * The Interface ScriptAction.
     *
     * @param <T>
     *            the generic type
     */
    @FunctionalInterface
    public interface ScriptAction<T> {

        /**
         * Run.
         *
         * @param value
         *            the value
         *
         * @return the t
         *
         * @throws ScriptException
         *             the script exception
         */
        T run(Value value) throws ScriptException;
    }

    /**
     * Exec. Use a cleaning executor service: a virtual-thread executor if experimental options are allowed, otherwise a
     * platform-thread executor.
     *
     * @param <T>
     *            the generic type
     * @param language
     *            the language
     * @param bindings
     *            the bindings
     * @param scriptContent
     *            the script content
     * @param action
     *            the action
     *
     * @return the t
     *
     * @throws ScriptException
     *             the script exception
     */
    public static <T> T exec(final String language, final Map<String, Object> bindings, final String scriptContent,
            final ScriptAction<T> action) throws ScriptException {
        return exec(language, bindings, scriptContent, action, RUNNING_TIMEOUT);
    }

    /**
     * Exec. Use a cleaning executor service: a virtual-thread executor if experimental options are allowed, otherwise a
     * platform-thread executor.
     *
     * @param <T>
     *            the generic type
     * @param language
     *            the language
     * @param bindings
     *            the bindings
     * @param scriptContent
     *            the script content
     * @param action
     *            the action
     * @param timeoutMs
     *            the timeout ms
     *
     * @return the result of type T
     *
     * @throws ScriptException
     *             the script exception
     */
    public static <T> T exec(final String language, final Map<String, Object> bindings, final String scriptContent,
            final ScriptAction<T> action, long timeoutMs) throws ScriptException {
        Future<T> future = null;
        try (final var executor = ThreadService.getSingleCleaningThreadLocalExecutorService(ALLOW_VIRTUAL_THREAD,
                true)) {
            future = executor.submit(() -> {
                final var currentThread = Thread.currentThread();
                final var originalCL = currentThread.getContextClassLoader();
                try (final var manager = new ScriptManager(language)) {
                    return action.run(manager.put(bindings).eval(scriptContent));
                } catch (PolyglotException e) {
                    throw getMessage("Exec " + language, e);
                } finally {
                    currentThread.setContextClassLoader(originalCL);
                    ThreadContext.clearAll();
                }
            });
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException _) {
            if (future != null)
                future.cancel(true);
            throw new ScriptException("Script execution timed out after " + timeoutMs + " ms");
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            throw new ScriptException("Script execution interrupted");
        } catch (ExecutionException e) {
            var cause = e.getCause();
            if (cause instanceof ScriptException scriptException) {
                throw scriptException;
            } else {
                var msg = new StringBuilder("Unexpected error during script execution");
                if (cause != null && cause.getMessage() != null) {
                    msg.append(": ").append(cause.getMessage());
                } else if (cause != null) {
                    msg.append(": ").append(cause.getClass().getName());
                }
                var se = new ScriptException(msg.toString());
                if (cause != null)
                    se.initCause(cause);
                throw se;
            }
        }
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
     * @param bindings
     *            the bindings
     * @param script
     *            the script
     *
     * @return the t
     *
     * @throws ScriptException
     *             the script exception
     */
    public static <T> T exec(final Class<T> clazz, final String defaultLanguage, final Map<String, Object> bindings,
            final String script) throws ScriptException {
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
        return exec(language, bindings, script.substring(index), value -> cast(clazz, value));
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
        return exec(clazz, defaultLanguage, Collections.emptyMap(), script);
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
        exec(null, defaultLanguage, Collections.emptyMap(), script);
    }

    /**
     * Instantiates a new script manager. Uses the requested script engine.
     *
     * @param language
     *            the language
     */
    private ScriptManager(final String language) {
        currentLanguage = language;
    }

    /**
     * Eval. Must be called on the same thread where the context was created, and the thread should support cleanup of
     * thread-local variables.
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
     * @param scriptBody
     *            the script
     *
     * @return the the Value
     *
     * @throws ScriptException
     *             the script exception
     */
    private Value eval(final String scriptBody) throws ScriptException {
        if (scriptBody == null || scriptBody.isBlank())
            return null; // Nothing to evaluate!
        final var currentThread = Thread.currentThread();
        final var originalCL = currentThread.getContextClassLoader();
        // Save and clear ThreadContext
        final var originalThreadContextMap = ThreadContext.getImmutableContext();
        ThreadContext.clearAll();
        final var start = System.currentTimeMillis();
        try {
            currentThread.setContextClassLoader(ScriptManager.class.getClassLoader());
            var value = getCache().context.eval(
                    Source.create(currentLanguage, wrapScript(addMissingReturnToSingleLineJSExpression(scriptBody))));
            var duration = System.currentTimeMillis() - start;
            if (_log.isDebugEnabled() && duration > LONG_RUNNING_TIME) {
                _log.debug("Time taken: {} ms", duration);
            }
            return value;
        } catch (PolyglotException polyglotException) {
            throw getMessage("Eval " + currentLanguage, polyglotException);
        } catch (Exception e) {
            final var message = "Eval " + currentLanguage;
            _log.warn(message, e);
            throw new ScriptException(message + " (" + e.getMessage() + ")");
        } finally {
            currentThread.setContextClassLoader(originalCL); // Restore CL
            // Restore ThreadContext
            ThreadContext.clearAll(); // ensure clean slate before restore
            if (originalThreadContextMap != null) {
                ThreadContext.putAll(originalThreadContextMap);
            }
        }
    }

    /**
     * Put.
     *
     * @param key
     *            the key
     * @param value
     *            the value
     *
     * @return the script manager
     *
     * @throws ScriptException
     *             the script exception
     */
    private ScriptManager put(final String key, final Object value) throws ScriptException {
        try {
            getCache().bindings.putMember(key, value);
        } catch (final PolyglotException e) {
            throw getMessage("Exposing to " + currentLanguage + ": " + key + "=" + value, e);
        }
        return this;
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
    private ScriptManager put(final Map<String, Object> context) throws ScriptException {
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
    private static Value getNestedValue(final Value root, final String path) {
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
            cache = new Cache();
        }
        return cache;
    }

    /**
     * Casts a GraalVM {@code Value} to a specified Java type.
     *
     * @param <T>
     *            The target type to which the value should be cast.
     * @param clazz
     *            The {@code Class} representing the target type {@code T}.
     * @param value
     *            The GraalVM {@code Value} object to convert.
     *
     * @return The converted object of type {@code T}.
     *
     * @throws ClassCastException
     *             if the value cannot be converted to the target type.
     */
    private static <T> T cast(final Class<T> clazz, final Value value) throws ScriptException {
        // No return requested (void)
        if (clazz == null)
            return null;
        // No return from the script?
        if (value == null || value.isNull() || "undefined".equals(value.toString()))
            throw new ScriptException("No return from script or null/undefined object");
        try {
            return cast(value, clazz);
        } catch (final ClassCastException _) {
            throw new ScriptException("Not a " + clazz.getSimpleName() + " output (" + value.toString() + ")");
        }
    }

    /**
     * Casts a GraalVM {@code Value} to a specified Java type.
     * <p>
     * This method uses a map of optimized converters for common scalar types and falls back to GraalVM's default
     * casting for others.
     *
     * @param <T>
     *            The target type to which the value should be cast.
     * @param value
     *            The GraalVM {@code Value} object to convert.
     * @param clazz
     *            The {@code Class} representing the target type {@code T}.
     *
     * @return The converted object of type {@code T}.
     *
     * @throws ClassCastException
     *             if the value cannot be converted to the target type.
     */
    @SuppressWarnings("unchecked")
    private static <T> T cast(final Value value, final Class<T> clazz) {
        // Look up the specific caster function, or use a default
        Function<Value, ?> caster = CASTERS.get(clazz);
        if (caster != null) {
            return (T) caster.apply(value);
        }
        // Default behavior for unsupported or complex types
        _log.debug("Using default GraalVM cast for type: {}", clazz);
        return value.as(clazz);
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
        final var sourceSection = e.getSourceLocation();
        if (_log.isDebugEnabled() && sourceSection != null) {
            final var snippet = sourceSection.getCharacters() != null
                    ? sourceSection.getCharacters().toString().replaceAll("\\s+", " ").trim() : "<unknown code>";
            _log.debug("{} at line {}: {}", message, sourceSection.getStartLine(), snippet, e);
        } else if (sourceSection != null) {
            _log.warn("{} at line {}", message, sourceSection.getStartLine(), e);
        } else {
            _log.warn(message, e);
        }
        final var trace = new StringBuilder();
        for (final var stack : e.getPolyglotStackTrace()) {
            final var language = stack.getLanguage();
            if (language != null && !"host".equals(language.getId())) {
                if (!trace.isEmpty()) {
                    trace.append(" <- ");
                }
                trace.append(stack);
            }
        }
        return new ScriptException(e.getMessage() + (trace.isEmpty() ? "" : " <- " + trace));
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

    /**
     * The Class ContextProvider.
     */
    private static class ContextProvider {

        /** The Constant TRACKER. */
        private static final ResourceTracker TRACKER = new ResourceTracker(ContextProvider.class);

        /** The use shared engine. */
        private final boolean useSharedEngine;

        /** The resource limits. */
        private final Builder contextBuilder;

        /**
         * Instantiates a new context provider.
         *
         * @param limits
         *            the limits
         */
        ContextProvider(final int limits, final boolean useSharedEngine, final boolean allowExperimentalOptions,
                final String warnVirtualThreadSupport, final String engineCompilation) {
            this.contextBuilder = Context.newBuilder(JS, PYTHON);
            this.useSharedEngine = useSharedEngine;
            if (useSharedEngine)
                contextBuilder.engine(
                        getConfiguredEngine(allowExperimentalOptions, warnVirtualThreadSupport, engineCompilation));
            final var nullOutputStream = OutputStream.nullOutputStream();
            contextBuilder.allowHostAccess(HostAccess.ALL).allowHostClassLookup(ContextProvider::check)
                    .useSystemExit(false).allowCreateProcess(false).allowCreateThread(false)
                    .in(InputStream.nullInputStream()).out(nullOutputStream).err(nullOutputStream);
            if (limits > 0)
                contextBuilder.resourceLimits(ResourceLimits.newBuilder().statementLimit(limits, null).build());
        }

        /**
         * Gets the configured engine.
         *
         * @return the configured engine
         */
        static Engine getConfiguredEngine(final boolean allowExperimentalOptions, final String warnVirtualThreadSupport,
                final String engineCompilation) {
            final var engineBuilder = Engine.newBuilder(JS, PYTHON);
            if (allowExperimentalOptions) {
                engineBuilder.allowExperimentalOptions(true)
                        .option("engine.WarnVirtualThreadSupport", warnVirtualThreadSupport)
                        .option("engine.Compilation", engineCompilation);
            }
            return engineBuilder.build();
        }

        /**
         * Called whenever the underlying script is trying to access an external class. It the external class is not
         * listed in the exposed classes then the permission is denied!
         *
         * @param className
         *            the class name
         *
         * @return true, if successful
         */
        static boolean check(final String className) {
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
         * Creates the new context.
         *
         * @return the context
         */
        Context acquire() {
            final var context = contextBuilder.build();
            TRACKER.onOpen();
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
            var closedSuccessfully = false;
            try {
                context.close(true);
                if (!useSharedEngine)
                    context.getEngine().close(true);
                closedSuccessfully = true;
            } catch (Exception e) {
                _log.warn("Failed to close context cleanly", e);
            } finally {
                TRACKER.onClose(closedSuccessfully);
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
        static void resetBindings(final Context context, final String currentLanguage) {
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
    }
}
