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

package ecmwf.common.plugin;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.Serializable;

/**
 * The Class PluginInfo.
 */
public final class PluginInfo implements Serializable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1806381246046374650L;

    /** The _ref. */
    private final String _ref;

    /** The _name. */
    private final String _name;

    /** The _version. */
    private final String _version;

    /**
     * Instantiates a new plugin info.
     *
     * @param plugin
     *            the plugin
     */
    public PluginInfo(final PluginThread plugin) {
        _ref = plugin.getRef();
        _name = plugin.getPluginName();
        _version = plugin.getVersion();
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return _name;
    }

    /**
     * Gets the ref.
     *
     * @return the ref
     */
    public String getRef() {
        return _ref;
    }

    /**
     * Gets the version.
     *
     * @return the version
     */
    public String getVersion() {
        return _version;
    }
}
