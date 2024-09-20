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

package ecmwf.ecpds.master;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.DataBaseObject;
import ecmwf.common.technical.Cnf;
import ecmwf.common.text.Format;

/**
 * The Class MonitorCall.
 */
final class MonitorCall {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(MonitorCall.class);

    /** The Constant _trace. */
    private static boolean _trace = Cnf.booleanAt("Other", "trace", false);

    /** The method. */
    private final String method;

    /** The start. */
    private final long start = System.currentTimeMillis();

    /**
     * Instantiates a new monitor call.
     *
     * @param method
     *            the method
     */
    public MonitorCall(final String method) {
        this.method = method;
    }

    /**
     * Set trace.
     *
     * @param trace
     *            the trace
     */
    public static void setTrace(final boolean trace) {
        _trace = trace;
    }

    /**
     * Get trace.
     *
     * @return the trace
     */
    public static boolean getTrace() {
        return _trace;
    }

    /**
     * Log call.
     *
     * @param method
     *            the method
     * @param call
     *            the call
     */
    private static void _logCall(final String method, final String call) {
        _log.debug(method + ": " + call);
        try {
            throw new Exception();
        } catch (final Exception e) {
            var i = 0;
            for (final StackTraceElement element : e.getStackTrace()) {
                final var className = element.getClassName();
                if (className != null && className.startsWith("ecmwf.")) {
                    _log.debug(method + " [" + i++ + "] -> " + element.toString());
                }
            }
        }
    }

    /**
     * Done.
     *
     * @param <T>
     *            the generic type
     * @param collection
     *            the collection
     *
     * @return the collection
     */
    <T> Collection<T> done(final Collection<T> collection) {
        if (_trace) {
            _logCall(method,
                    Format.formatDuration(start, System.currentTimeMillis()) + " -> ("
                            + (collection != null
                                    ? collection.getClass().getSimpleName() + "[" + collection.size() + "]" : "<null>")
                            + ")");
        }
        return collection;
    }

    /**
     * Done.
     *
     * @param <T>
     *            the generic type
     * @param list
     *            the collection
     *
     * @return the collection
     */
    <T> List<T> done(final List<T> list) {
        if (_trace) {
            _logCall(method, Format.formatDuration(start, System.currentTimeMillis()) + " -> ("
                    + (list != null ? list.getClass().getSimpleName() + "[" + list.size() + "]" : "<null>") + ")");
        }
        return list;
    }

    /**
     * Done.
     *
     * @param <T>
     *            the generic type
     * @param set
     *            the set
     *
     * @return the set
     */
    <T> Set<T> done(final Set<T> set) {
        if (_trace) {
            _logCall(method, Format.formatDuration(start, System.currentTimeMillis()) + " -> ("
                    + (set != null ? set.getClass().getSimpleName() + "[" + set.size() + "]" : "<null>") + ")");
        }
        return set;
    }

    /**
     * Done.
     *
     * @param <A>
     *            the generic type
     * @param <B>
     *            the generic type
     * @param map
     *            the hashtable
     *
     * @return the hashtable
     */
    <A, B> Map<A, B> done(final Map<A, B> map) {
        if (_trace) {
            _logCall(method, Format.formatDuration(start, System.currentTimeMillis()) + " -> ("
                    + (map != null ? map.getClass().getSimpleName() + "[" + map.size() + "]" : "<null>") + ")");
        }
        return map;
    }

    /**
     * Done.
     *
     * @param <T>
     *            the generic type
     * @param object
     *            the object
     *
     * @return the t[]
     */
    <T> T[] done(final T[] object) {
        if (_trace) {
            if (object == null) {
                _logCall(method, Format.formatDuration(start, System.currentTimeMillis()) + " -> (null)");

            } else {
                final Class<?> clazz = object.getClass();
                final var className = clazz.getSimpleName();
                _logCall(method, Format.formatDuration(start, System.currentTimeMillis()) + " -> ("
                        + className.substring(0, className.length() - 2) + "[" + Array.getLength(object) + "])");
            }
        }
        return object;
    }

    /**
     * Done.
     *
     * @param <T>
     *            the generic type
     * @param object
     *            the object
     *
     * @return the t
     */
    <T> T done(final T object) {
        if (_trace) {
            if (object == null) {
                _log.debug(method + ": " + Format.formatDuration(start, System.currentTimeMillis()) + " -> (null)");

            } else {
                final Class<?> clazz = object.getClass();
                final var className = clazz.getSimpleName();
                _logCall(method, Format.formatDuration(start, System.currentTimeMillis()) + " -> (" + className + ")"
                        + (object instanceof DataBaseObject ? " - " + object.toString() : ""));
            }
        }
        return object;
    }

    /**
     * Done.
     *
     * @param result
     *            the result
     *
     * @return the int
     */
    int done(final int result) {
        if (_trace) {
            _logCall(method, Format.formatDuration(start, System.currentTimeMillis()) + " -> (int) " + result);
        }
        return result;
    }

    /**
     * Done.
     *
     * @param result
     *            the result
     *
     * @return the long
     */
    long done(final long result) {
        if (_trace) {
            _logCall(method, Format.formatDuration(start, System.currentTimeMillis()) + " -> (long) " + result);
        }
        return result;
    }

    /**
     * Done.
     *
     * @param result
     *            the result
     *
     * @return true, if successful
     */
    boolean done(final boolean result) {
        if (_trace) {
            _logCall(method, Format.formatDuration(start, System.currentTimeMillis()) + " -> (boolean) " + result);
        }
        return result;
    }

    /**
     * Done.
     *
     * @param result
     *            the result
     *
     * @return the string
     */
    String done(final String result) {
        if (_trace) {
            _logCall(method, Format.formatDuration(start, System.currentTimeMillis()) + " -> (String) " + result);
        }
        return result;
    }

    /**
     * Done.
     *
     * @param bytes
     *            the bytes
     *
     * @return the byte[]
     */
    byte[] done(final byte[] bytes) {
        if (_trace) {
            _logCall(method, Format.formatDuration(start, System.currentTimeMillis()) + " -> ("
                    + (bytes != null ? "bytes[" + bytes.length + "]" : "null") + ")");
        }
        return bytes;
    }

    /**
     * Done.
     */
    void done() {
        if (_trace) {
            _logCall(method, Format.formatDuration(start, System.currentTimeMillis()) + " -> void");
        }
    }
}
