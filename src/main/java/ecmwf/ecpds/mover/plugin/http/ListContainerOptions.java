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

package ecmwf.ecpds.mover.plugin.http;

/**
 * ECMWF Product Data Store (OpenPDS) Project.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */
public class ListContainerOptions {

    /** The delimiter. */
    private String _delimiter = "";

    /** The prefix. */
    private String _prefix = "";

    /** The marker. */
    private String _marker;

    /** The max keys. */
    private int _maxKeys = 1000;

    /** The recursive. */
    private boolean _recursive = false;

    /**
     * Delimiter.
     *
     * @param delimiter
     *            the delimiter
     */
    public void delimiter(final String delimiter) {
        _delimiter = delimiter;
    }

    /**
     * Recursive.
     */
    public void recursive() {
        _recursive = true;
    }

    /**
     * Prefix.
     *
     * @param prefix
     *            the prefix
     */
    public void prefix(final String prefix) {
        _prefix = prefix;
    }

    /**
     * After marker.
     *
     * @param marker
     *            the marker
     */
    public void afterMarker(final String marker) {
        _marker = marker;
    }

    /**
     * Max results.
     *
     * @param maxKeys
     *            the max keys
     */
    public void maxResults(final int maxKeys) {
        _maxKeys = maxKeys;
    }

    /**
     * Gets the delimiter.
     *
     * @return the delimiter
     */
    public String getDelimiter() {
        return _delimiter;
    }

    /**
     * Gets the prefix.
     *
     * @return the prefix
     */
    public String getPrefix() {
        return _prefix;
    }

    /**
     * Gets the marker.
     *
     * @return the marker
     */
    public String getMarker() {
        return _marker;
    }

    /**
     * Gets the max keys.
     *
     * @return the max keys
     */
    public int getMaxKeys() {
        return _maxKeys;
    }

    /**
     * Checks if is recursive.
     *
     * @return true, if is recursive
     */
    public boolean isRecursive() {
        return _recursive;
    }

    /**
     * {@inheritDoc}
     *
     * To string.
     */
    @Override
    public String toString() {
        return "ListContainerOptions [_delimiter=" + _delimiter + ", _prefix=" + _prefix + ", _marker=" + _marker
                + ", _maxKeys=" + _maxKeys + ", _recursive=" + _recursive + "]";
    }
}
