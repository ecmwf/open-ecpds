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

package ecmwf.common.technical;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.Serializable;

/**
 * The Class ProxyEvent.
 */
public final class ProxyEvent implements Serializable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -3116484995788964990L;

    /**
     * The Enum UserType.
     */
    public enum UserType {

        /** The data user. */
        DATA_USER,
        /** The web user. */
        WEB_USER
    }

    /** The _upload. */
    private boolean _upload = false;

    /** The _protocol. */
    private String _protocol;

    /** The _start time. */
    private long _startTime;

    /** The _duration. */
    private long _duration;

    /** The _sent. */
    private long _sent;

    /** The _remote host. */
    private String _remoteHost;

    /** The _local host. */
    private String _localHost;

    /** The _user name. */
    private String _userName;

    /** The user type. */
    private UserType _userType;

    /**
     * Instantiates a new proxy event.
     *
     * @param proxy
     *            the proxy
     */
    public ProxyEvent(final ProxySocket proxy) {
        proxy.addObject(ProxyEvent.class.getName(), this);
    }

    /**
     * Sets the upload.
     *
     * @param upload
     *            the new upload
     */
    public void setUpload(final boolean upload) {
        _upload = upload;
    }

    /**
     * Gets the upload.
     *
     * @return the upload
     */
    public boolean getUpload() {
        return _upload;
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
     *            the new protocol
     */
    public void setProtocol(final String protocol) {
        _protocol = protocol;
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
     *            the new start time
     */
    public void setStartTime(final long startTime) {
        _startTime = startTime;
    }

    /**
     * Gets the duration.
     *
     * @return the duration
     */
    public long getDuration() {
        return _duration;
    }

    /**
     * Sets the duration.
     *
     * @param duration
     *            the new duration
     */
    public void setDuration(final long duration) {
        _duration = duration;
    }

    /**
     * Gets the sent.
     *
     * @return the sent
     */
    public long getSent() {
        return _sent;
    }

    /**
     * Sets the sent.
     *
     * @param sent
     *            the new sent
     */
    public void setSent(final long sent) {
        _sent = sent;
    }

    /**
     * Gets the remote host.
     *
     * @return the remote host
     */
    public String getRemoteHost() {
        return _remoteHost;
    }

    /**
     * Sets the remote host.
     *
     * @param remoteHost
     *            the new remote host
     */
    public void setRemoteHost(final String remoteHost) {
        _remoteHost = remoteHost;
    }

    /**
     * Gets the local host.
     *
     * @return the local host
     */
    public String getLocalHost() {
        return _localHost;
    }

    /**
     * Sets the local host.
     *
     * @param localHost
     *            the new local host
     */
    public void setLocalHost(final String localHost) {
        _localHost = localHost;
    }

    /**
     * Gets the user name.
     *
     * @return the user name
     */
    public String getUserName() {
        return _userName;
    }

    /**
     * Sets the user name.
     *
     * @param userName
     *            the new user name
     */
    public void setUserName(final String userName) {
        _userName = userName;
    }

    /**
     * Gets the user type.
     *
     * @return the user type
     */
    public UserType getUserType() {
        return _userType;
    }

    /**
     * Sets the user type.
     *
     * @param userType
     *            the new user type
     */
    public void setUserType(final UserType userType) {
        _userType = userType;
    }
}
