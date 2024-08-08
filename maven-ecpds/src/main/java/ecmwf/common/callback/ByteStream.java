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

package ecmwf.common.callback;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.Serializable;

/**
 * The Class ByteStream.
 */
public final class ByteStream implements Serializable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -2376394231712176967L;

    /** The _b. */
    private final byte[] _b;

    /** The _len. */
    private final int _len;

    /**
     * Instantiates a new byte stream.
     *
     * @param b
     *            the b
     * @param len
     *            the len
     */
    public ByteStream(final byte[] b, final int len) {
        if (len >= 0) {
            _len = len;
            _b = new byte[len];
            System.arraycopy(b, 0, _b, 0, len);
        } else {
            _len = -1;
            _b = null;
        }
    }

    /**
     * Gets the bytes.
     *
     * @return the bytes
     */
    public byte[] getBytes() {
        return _b;
    }

    /**
     * Gets the len.
     *
     * @return the len
     */
    public int getLen() {
        return _len;
    }
}
