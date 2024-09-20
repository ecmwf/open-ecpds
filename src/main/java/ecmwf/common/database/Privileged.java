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

import java.util.Objects;

/**
 * ECMWF Product Data Store (OpenPDS) Project.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */

public class Privileged extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 6077304760023108975L;

    /** The ecu name. */
    protected String ECU_NAME;

    /** The not id. */
    protected int NOT_ID;

    /** The prv send. */
    protected boolean PRV_SEND;

    /** The prv subscribe. */
    protected boolean PRV_SUBSCRIBE;

    /** The ecuser. */
    protected ECUser ecuser;

    /** The notification. */
    protected Notification notification;

    /**
     * Instantiates a new privileged.
     */
    public Privileged() {
    }

    /**
     * Instantiates a new privileged.
     *
     * @param ecuserName
     *            the ecuser name
     * @param notificationId
     *            the notification id
     */
    public Privileged(final String ecuserName, final int notificationId) {
        setECUserName(ecuserName);
        setNotificationId(notificationId);
    }

    /**
     * Instantiates a new privileged.
     *
     * @param ecuserName
     *            the ecuser name
     * @param notificationId
     *            the notification id
     */
    public Privileged(final String ecuserName, final String notificationId) {
        setECUserName(ecuserName);
        setNotificationId(notificationId);
    }

    /**
     * Gets the EC user name.
     *
     * @return the EC user name
     */
    public String getECUserName() {
        return ECU_NAME;
    }

    /**
     * Sets the EC user name.
     *
     * @param param
     *            the new EC user name
     */
    public void setECUserName(final String param) {
        ECU_NAME = param;
    }

    /**
     * Gets the notification id.
     *
     * @return the notification id
     */
    public int getNotificationId() {
        return NOT_ID;
    }

    /**
     * Sets the notification id.
     *
     * @param param
     *            the new notification id
     */
    public void setNotificationId(final int param) {
        NOT_ID = param;
    }

    /**
     * Sets the notification id.
     *
     * @param param
     *            the new notification id
     */
    public void setNotificationId(final String param) {
        NOT_ID = Integer.parseInt(param);
    }

    /**
     * Gets the send.
     *
     * @return the send
     */
    public boolean getSend() {
        return PRV_SEND;
    }

    /**
     * Sets the send.
     *
     * @param param
     *            the new send
     */
    public void setSend(final boolean param) {
        PRV_SEND = param;
    }

    /**
     * Sets the send.
     *
     * @param param
     *            the new send
     */
    public void setSend(final String param) {
        PRV_SEND = Boolean.parseBoolean(param);
    }

    /**
     * Gets the subscribe.
     *
     * @return the subscribe
     */
    public boolean getSubscribe() {
        return PRV_SUBSCRIBE;
    }

    /**
     * Sets the subscribe.
     *
     * @param param
     *            the new subscribe
     */
    public void setSubscribe(final boolean param) {
        PRV_SUBSCRIBE = param;
    }

    /**
     * Sets the subscribe.
     *
     * @param param
     *            the new subscribe
     */
    public void setSubscribe(final String param) {
        PRV_SUBSCRIBE = Boolean.parseBoolean(param);
    }

    /**
     * Gets the EC user.
     *
     * @return the EC user
     */
    public ECUser getECUser() {
        return ecuser;
    }

    /**
     * Sets the EC user.
     *
     * @param param
     *            the new EC user
     */
    public void setECUser(final ECUser param) {
        ecuser = param;
    }

    /**
     * Gets the notification.
     *
     * @return the notification
     */
    public Notification getNotification() {
        return notification;
    }

    /**
     * Sets the notification.
     *
     * @param param
     *            the new notification
     */
    public void setNotification(final Notification param) {
        notification = param;
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return Objects.hash(ECU_NAME, NOT_ID);
    }

    /**
     * Equals.
     *
     * @param obj
     *            the obj
     *
     * @return true, if successful
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final var other = (Privileged) obj;
        if (!Objects.equals(ECU_NAME, other.ECU_NAME)) {
            return false;
        }
        if (NOT_ID != other.NOT_ID) {
            return false;
        }
        return true;
    }
}
