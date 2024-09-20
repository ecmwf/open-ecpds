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
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Class Singletons.
 */
public final class Singletons {
    /** The _instances. */
    private static final Map<String, Object> instances = new ConcurrentHashMap<>();

    /**
     * Instantiates a new singletons. Utility classes should not have public constructors!
     */
    private Singletons() {
    }

    /**
     * Save.
     *
     * @param object
     *            the object
     */
    public static void save(final Object object) {
        save(object.getClass(), object);
    }

    /**
     * Save.
     *
     * @param clazz
     *            the clazz
     * @param object
     *            the object
     */
    public static void save(final Class<?> clazz, final Object object) {
        instances.put(clazz.getName(), object);
    }

    /**
     * Gets the.
     *
     * @param <T>
     *            the generic type
     * @param clazz
     *            the clazz
     *
     * @return the t
     */
    public static <T> T get(final Class<T> clazz) {
        final var object = instances.get(clazz.getName());
        return object != null ? clazz.cast(object) : null;
    }

    /**
     * Gets the.
     *
     * @param <T>
     *            the generic type
     * @param name
     *            the name
     * @param clazz
     *            the clazz
     *
     * @return the t
     */
    public static <T> T get(final Class<?> name, final Class<T> clazz) {
        final var object = instances.get(name.getName());
        return object != null ? clazz.cast(object) : null;
    }
}
