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

package ecmwf.common.database;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.Serializable;
import java.util.Objects;

import ecmwf.common.text.Format;

/**
 * The Class IncomingConnection.
 */
public class IncomingConnection implements Serializable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -547617357243451889L;

    /** The login. */
    private String _id;

    /** The login. */
    private String _login;

    /** The data mover name. */
    private String _dataMoverName;

    /** The remote IP address. */
    private String _remoteIpAddress;

    /** The protocol. */
    private String _protocol;

    /** The subscriber email (populated for self-service portal sessions only). */
    private String _subscriberEmail;

    /** The start time. */
    private long _startTime;

    /** The bytes received from the client (upload). */
    private long _bytesIn;

    /** The bytes sent to the client (download). */
    private long _bytesOut;

    /** Currently active download streams. */
    private int _activeStreamsOut;

    /** Currently active upload streams. */
    private int _activeStreamsIn;

    /** Accumulated download stream count since session start. */
    private long _totalStreamsOut;

    /** Accumulated upload stream count since session start. */
    private long _totalStreamsIn;

    /**
     * Gets the session id.
     *
     * @return the session id
     */
    public String getId() {
        return _id;
    }

    /**
     * Sets the session id.
     *
     * @param id
     *            the session id
     */
    public void setId(final String id) {
        _id = id;
    }

    /**
     * Gets the login.
     *
     * @return the login
     */
    public String getLogin() {
        return _login;
    }

    /**
     * Sets the login.
     *
     * @param login
     *            the login
     */
    public void setLogin(final String login) {
        _login = login;
    }

    /**
     * Gets the data mover name.
     *
     * @return the data mover name
     */
    public String getDataMoverName() {
        return _dataMoverName;
    }

    /**
     * Sets the data mover name.
     *
     * @param dataMoverName
     *            the data mover name
     */
    public void setDataMoverName(final String dataMoverName) {
        _dataMoverName = dataMoverName;
    }

    /**
     * Gets the remote IP address.
     *
     * @return the remote IP address
     */
    public String getRemoteIpAddress() {
        return _remoteIpAddress;
    }

    /**
     * Sets the remote IP address.
     *
     * @param remoteIpAddress
     *            the remote IP address
     */
    public void setRemoteIpAddress(final String remoteIpAddress) {
        _remoteIpAddress = remoteIpAddress;
    }

    /**
     * Gets the protocol.
     *
     * @return the protocol
     */
    public String getProtocol() {
        return _protocol;
    }

    /**
     * Sets the protocol.
     *
     * @param protocol
     *            the protocol
     */
    public void setProtocol(final String protocol) {
        _protocol = protocol;
    }

    /**
     * Gets the subscriber email for self-service portal sessions.
     *
     * @return the subscriber email, or {@code null} if not a self-service session
     */
    public String getSubscriberEmail() {
        return _subscriberEmail;
    }

    /**
     * Sets the subscriber email.
     *
     * @param subscriberEmail
     *            the subscriber email
     */
    public void setSubscriberEmail(final String subscriberEmail) {
        _subscriberEmail = subscriberEmail;
    }

    /**
     * Gets the start time.
     *
     * @return the start time
     */
    public String getFormatedDuration() {
        return Format.formatDuration(System.currentTimeMillis() - _startTime);
    }

    /**
     * Gets the start time.
     *
     * @return the start time
     */
    public long getStartTime() {
        return _startTime;
    }

    /**
     * Sets the start time.
     *
     * @param startTime
     *            the start time
     */
    public void setStartTime(final long startTime) {
        _startTime = startTime;
    }

    /**
     * Gets the bytes received from the client.
     *
     * @return the bytes in
     */
    public long getBytesIn() {
        return _bytesIn;
    }

    /**
     * Sets the bytes received from the client.
     *
     * @param bytesIn
     *            the bytes in
     */
    public void setBytesIn(final long bytesIn) {
        _bytesIn = bytesIn;
    }

    /**
     * Gets the bytes sent to the client.
     *
     * @return the bytes out
     */
    public long getBytesOut() {
        return _bytesOut;
    }

    /**
     * Sets the bytes sent to the client.
     *
     * @param bytesOut
     *            the bytes out
     */
    public void setBytesOut(final long bytesOut) {
        _bytesOut = bytesOut;
    }

    /**
     * Gets the formatted bytes received from the client.
     *
     * @return the formatted bytes in
     */
    public String getFormatedBytesIn() {
        return Format.formatSize(_bytesIn);
    }

    /**
     * Gets the formatted bytes sent to the client.
     *
     * @return the formatted bytes out
     */
    public String getFormatedBytesOut() {
        return Format.formatSize(_bytesOut);
    }

    /**
     * Gets the currently active download streams.
     *
     * @return the active streams out
     */
    public int getActiveStreamsOut() {
        return _activeStreamsOut;
    }

    /**
     * Sets the currently active download streams.
     *
     * @param activeStreamsOut
     *            the active streams out
     */
    public void setActiveStreamsOut(final int activeStreamsOut) {
        _activeStreamsOut = activeStreamsOut;
    }

    /**
     * Gets the currently active upload streams.
     *
     * @return the active streams in
     */
    public int getActiveStreamsIn() {
        return _activeStreamsIn;
    }

    /**
     * Sets the currently active upload streams.
     *
     * @param activeStreamsIn
     *            the active streams in
     */
    public void setActiveStreamsIn(final int activeStreamsIn) {
        _activeStreamsIn = activeStreamsIn;
    }

    /**
     * Gets the accumulated download stream count.
     *
     * @return the total streams out
     */
    public long getTotalStreamsOut() {
        return _totalStreamsOut;
    }

    /**
     * Sets the accumulated download stream count.
     *
     * @param totalStreamsOut
     *            the total streams out
     */
    public void setTotalStreamsOut(final long totalStreamsOut) {
        _totalStreamsOut = totalStreamsOut;
    }

    /**
     * Gets the accumulated upload stream count.
     *
     * @return the total streams in
     */
    public long getTotalStreamsIn() {
        return _totalStreamsIn;
    }

    /**
     * Sets the accumulated upload stream count.
     *
     * @param totalStreamsIn
     *            the total streams in
     */
    public void setTotalStreamsIn(final long totalStreamsIn) {
        _totalStreamsIn = totalStreamsIn;
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(_activeStreamsIn, _activeStreamsOut, _bytesIn, _bytesOut, _dataMoverName, _id, _login,
                _protocol, _remoteIpAddress, _startTime, _totalStreamsIn, _totalStreamsOut);
    }

    /**
     * {@inheritDoc}
     *
     * Equals.
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final var other = (IncomingConnection) obj;
        if (!Objects.equals(_dataMoverName, other._dataMoverName)) {
            return false;
        }
        if (!Objects.equals(_id, other._id)) {
            return false;
        }
        if (!Objects.equals(_login, other._login)) {
            return false;
        }
        if (!Objects.equals(_protocol, other._protocol)) {
            return false;
        }
        if (!Objects.equals(_remoteIpAddress, other._remoteIpAddress)) {
            return false;
        }
        if (_bytesIn != other._bytesIn) {
            return false;
        }
        if (_bytesOut != other._bytesOut) {
            return false;
        }
        if (_activeStreamsOut != other._activeStreamsOut) {
            return false;
        }
        if (_activeStreamsIn != other._activeStreamsIn) {
            return false;
        }
        if (_totalStreamsOut != other._totalStreamsOut) {
            return false;
        }
        if (_totalStreamsIn != other._totalStreamsIn) {
            return false;
        }
        if (_startTime != other._startTime) {
            return false;
        }
        return true;
    }
}
