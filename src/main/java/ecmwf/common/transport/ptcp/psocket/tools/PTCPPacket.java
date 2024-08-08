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

package ecmwf.common.transport.ptcp.psocket.tools;

/**
 * ECMWF Product Data Store (OpenPDS) Project.
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */

public class PTCPPacket {

    /** The data. */
    private byte[] _data = null;

    /** The number. */
    private int _number = -1;

    /**
     * Instantiates a new PTCP packet.
     *
     * @param number
     *            the number
     * @param data
     *            the data
     */
    public PTCPPacket(final int number, final byte[] data) {
        _number = number;
        _data = data;
    }

    /**
     * Gets the data.
     *
     * @return the data
     */
    public byte[] getData() {
        return _data;
    }

    /**
     * Gets the number.
     *
     * @return the number
     */
    public int getNumber() {
        return _number;
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    public int getSize() {
        return _data.length;
    }

    /**
     * To string.
     *
     * @param number
     *            the number
     * @param size
     *            the size
     *
     * @return the string
     */
    public static String toString(final int number, final int size) {
        return "Packet[Number:" + number + ",Size:" + size + "]";
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return toString(getNumber(), getSize());
    }
}
