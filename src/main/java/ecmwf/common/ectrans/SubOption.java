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

package ecmwf.common.ectrans;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.List;

/**
 * Describes one of the {@code key=value} entries that can appear inside the string returned by
 * {@link ecmwf.common.text.Options} when parsing the value of an {@link ECtransOptions} constant whose type is a nested
 * options string (e.g. {@code scheduler.force="pattern=.*;standby=yes"}).
 *
 * <p>
 * This is purely descriptive metadata used for documentation and validation purposes (e.g. by the Properties editor in
 * the web UI, or by future documentation generators). It has no effect on the actual runtime parsing, which is entirely
 * handled by {@link ecmwf.common.text.Options}.
 *
 * @param name
 *            the key as expected inside the options string (e.g. "pattern", "standby")
 * @param clazz
 *            the expected type of the value (Boolean, Integer, Long, String, Duration, etc.)
 * @param choices
 *            the list of accepted values if the value is restricted to a fixed set (empty if free-form)
 */
public record SubOption(String name, Class<?> clazz, List<String> choices) {

    /**
     * Instantiates a new sub option with no restricted set of choices.
     *
     * @param name
     *            the key as expected inside the options string
     * @param clazz
     *            the expected type of the value
     */
    public SubOption(final String name, final Class<?> clazz) {
        this(name, clazz, List.of());
    }
}
