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

import ecmwf.common.plugin.PluginEvent;

/**
 * The Class ResetProductEvent.
 */
public class ResetProductEvent extends PluginEvent<Object> {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 500275739755464953L;

    /** The Constant NAME. */
    public static final String NAME = "ResetProductEvent";

    /** The _meta stream. */
    private final String _metaStream;

    /** The _meta time. */
    private final String _metaTime;

    /**
     * Instantiates a new reset product event.
     *
     * @param metaStream
     *            the meta stream
     * @param metaTime
     *            the meta time
     */
    public ResetProductEvent(final String metaStream, final String metaTime) {
        super(NAME, null);
        _metaStream = metaStream;
        _metaTime = metaTime;
    }

    /**
     * Instantiates a new reset product event.
     *
     * @param name
     *            the name
     * @param metaStream
     *            the meta stream
     * @param metaTime
     *            the meta time
     */
    public ResetProductEvent(final String name, final String metaStream, final String metaTime) {
        super(name, null);
        _metaStream = metaStream;
        _metaTime = metaTime;
    }

    /**
     * Gets the meta stream.
     *
     * @return the meta stream
     */
    public String getMetaStream() {
        return _metaStream;
    }

    /**
     * Gets the meta time.
     *
     * @return the meta time
     */
    public String getMetaTime() {
        return _metaTime;
    }
}
