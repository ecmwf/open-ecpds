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

import org.apache.logging.log4j.LogManager;

import org.apache.logging.log4j.Logger;

import ecmwf.common.text.Format;

/**
 * The Class Reflection.
 */
public final class Reflection {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(Reflection.class);

    /**
     * Execute.
     *
     * @param aObj
     *            the a obj
     * @param aMethodName
     *            the a method name
     * @param aArgTypes
     *            the a arg types
     * @param aArgs
     *            the a args
     *
     * @return the object
     *
     * @throws ReflectionExecutionException
     *             the reflection execution exception
     */
    public static Object execute(final Object aObj, final String aMethodName, final Class<?>[] aArgTypes,
            final Object[] aArgs) throws ReflectionExecutionException {
        try {
            return aObj.getClass().getMethod(aMethodName, aArgTypes).invoke(aObj, aArgs);
        } catch (final Throwable t) {
            _log.error("Cannot execute method " + aMethodName + " on " + Format.getClassName(aObj), t);
        }
        throw new ReflectionExecutionException(
                "Cannot execute method " + aMethodName + " on " + Format.getClassName(aObj));
    }
}
