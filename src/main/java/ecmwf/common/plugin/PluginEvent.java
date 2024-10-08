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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.Serializable;

import ecmwf.common.text.Format;

/**
 * The Class PluginEvent.
 *
 * @param <T>
 *            the generic type
 */
public class PluginEvent<T> implements Serializable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 500275739755464953L;

    /** The _name. */
    private final String _name;

    /** The _object. */
    private final T _object;

    /** The _time. */
    private final long _time;

    /** The _target. */
    private String _target = null;

    /** The _source. */
    private String _source = null;

    /**
     * Instantiates a new plugin event.
     *
     * @param name
     *            the name
     * @param object
     *            the object
     */
    public PluginEvent(final String name, final T object) {
        _time = System.currentTimeMillis();
        _name = name;
        _object = object;
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
     * Gets the object.
     *
     * @return the object
     */
    public T getObject() {
        return _object;
    }

    /**
     * Sets the target.
     *
     * @param target
     *            the new target
     */
    public void setTarget(final String target) {
        _target = target;
    }

    /**
     * Gets the target.
     *
     * @return the target
     */
    public String getTarget() {
        return _target;
    }

    /**
     * Gets the creation time.
     *
     * @return the creation time
     */
    public long getCreationTime() {
        return _time;
    }

    /**
     * Sets the source.
     *
     * @param comment
     *            the new source
     */
    public void setSource(final String comment) {
        _source = comment;
    }

    /**
     * Gets the source.
     *
     * @return the source
     */
    public String getSource() {
        return _source;
    }

    /**
     * {@inheritDoc}
     *
     * To string.
     */
    @Override
    public String toString() {
        return Format.formatTime("HH:mm:ss,SSS", _time) + " " + _name + (_source != null ? " " + _source : "");
    }
}
