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

import java.util.List;

/**
 * Common option contract for built-in enum constants and extension-owned definitions.
 */
public interface ECtransOptionDefinition {
    ECtransOption<?> getOption();

    default String getParameter() {
        return getOption().getParameter();
    }

    default String getModule() {
        return getOption().getModule();
    }

    default String getName() {
        return getOption().getName();
    }

    default String getFullName() {
        return getParameter();
    }

    default ECtransGroups getGroup() {
        return getOption().getGroup();
    }

    default Class<?> getClazz() {
        return getOption().getClazz();
    }

    default boolean isVisible() {
        return getOption().isVisible();
    }

    default boolean isSecured() {
        return getOption().isSecured();
    }

    default List<String> getChoicesAsStrings() {
        return getOption().getChoices().stream().map(Object::toString).toList();
    }

    default List<SubOption> getSubOptions() {
        return getOption().getSubOptions();
    }

    default boolean hasFreeFormSubOptions() {
        return getOption().hasFreeFormSubOptions();
    }

    default <T> T getDefaultValue(final Class<T> clazz) {
        return ECtransOption.convertDefault(clazz, getOption().getDefaultValue());
    }

    default <T> List<T> getDefaultValues(final Class<T> clazz) {
        return getOption().getDefaultValues().stream().map(value -> ECtransOption.convertDefault(clazz, value))
                .toList();
    }

    default String getDefaultString() {
        return getDefaultValue(String.class);
    }
}
