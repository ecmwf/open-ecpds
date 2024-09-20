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

package ecmwf.common.text;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * Provides utilities methods.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.Collection;
import java.util.List;

/**
 * The Class Util.
 */
public final class Util {

    /**
     * Instantiates a new util.
     */
    private Util() {
        // Hiding public constructor!
    }

    /**
     * Checks if the message is not empty (not null and contains at least one character).
     *
     * @param message
     *            the message
     *
     * @return true, if is not empty
     */
    public static boolean isNotEmpty(final String message) {
        return message != null && !message.isBlank();
    }

    /**
     * Checks if the options are not empty (not null and contains at least one option).
     *
     * @param options
     *            the options
     *
     * @return true, if is not empty
     */
    public static boolean isNotEmpty(final Options options) {
        return options != null && !options.isEmpty();
    }

    /**
     * Checks if the list is not empty (not null and contains at least one element).
     *
     * @param list
     *            the list
     *
     * @return true, if is not empty
     */
    public static boolean isNotEmpty(final List<?> list) {
        return list != null && !list.isEmpty();
    }

    /**
     * Checks if the collection is not empty (not null and contains at least one element).
     *
     * @param collection
     *            the collection
     *
     * @return true, if is not empty
     */
    public static boolean isNotEmpty(final Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    /**
     * Checks if the message is empty (null or contains no character).
     *
     * @param message
     *            the message
     *
     * @return true, if is empty
     */
    public static boolean isEmpty(final String message) {
        return message == null || message.isBlank();
    }

    /**
     * Checks if the options are empty (null or contains no option).
     *
     * @param options
     *            the options
     *
     * @return true, if is empty
     */
    public static boolean isEmpty(final Options options) {
        return options == null || options.isEmpty();
    }

    /**
     * Checks if the list is empty (null or contains no element).
     *
     * @param list
     *            the list
     *
     * @return true, if is empty
     */
    public static boolean isEmpty(final List<?> list) {
        return list == null || list.isEmpty();
    }

    /**
     * Checks if the collection is empty (null or contains no element).
     *
     * @param collection
     *            the collection
     *
     * @return true, if is empty
     */
    public static boolean isEmpty(final Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * If the message is null then translate it to none.
     *
     * @param object
     *            the object
     *
     * @return the string
     */
    public static Object nullToNone(final Object object) {
        if (object != null) {
            return "none".equalsIgnoreCase(String.valueOf(object)) ? "-" : object;
        }
        return "-";
    }
}
