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

package ecmwf.ecpds.extension;

import java.util.Map;
import java.util.Objects;

/**
 * Startup configuration snapshot. Option values may contain secrets and must not be logged.
 */
public final class ExtensionContext {
    private final String root;
    private final Map<String, String> options;

    public ExtensionContext(final String root, final Map<String, String> options) {
        this.root = Objects.requireNonNull(root);
        this.options = Map.copyOf(options);
    }

    public String root() {
        return root;
    }

    public String option(final String name) {
        return options.get(name);
    }
}
