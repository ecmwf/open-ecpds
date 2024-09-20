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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.math.BigDecimal;

/**
 * The Class Reception.
 */
public class Reception extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -258372606051444631L;

    /** The not id. */
    protected int NOT_ID;

    /** The rec environment. */
    protected String REC_ENVIRONMENT;

    /** The rec expiration date. */
    protected BigDecimal REC_EXPIRATION_DATE;

    /** The rec id. */
    protected int REC_ID;

    /** The rec monitor. */
    protected boolean REC_MONITOR;

    /** The rec sequence. */
    protected int REC_SEQUENCE;

    /** The rec time. */
    protected BigDecimal REC_TIME;

    /** The notification. */
    protected Notification notification;

    /**
     * Instantiates a new reception.
     */
    public Reception() {
    }

    /**
     * Instantiates a new reception.
     *
     * @param id
     *            the id
     */
    public Reception(final int id) {
        setId(id);
    }

    /**
     * Instantiates a new reception.
     *
     * @param id
     *            the id
     */
    public Reception(final String id) {
        setId(id);
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
     * Gets the environment.
     *
     * @return the environment
     */
    public String getEnvironment() {
        return REC_ENVIRONMENT;
    }

    /**
     * Sets the environment.
     *
     * @param param
     *            the new environment
     */
    public void setEnvironment(final String param) {
        REC_ENVIRONMENT = param;
    }

    /**
     * Gets the expiration date.
     *
     * @return the expiration date
     */
    public java.sql.Timestamp getExpirationDate() {
        return bigDecimalToTimestamp(REC_EXPIRATION_DATE);
    }

    /**
     * Sets the expiration date.
     *
     * @param param
     *            the new expiration date
     */
    public void setExpirationDate(final java.sql.Timestamp param) {
        REC_EXPIRATION_DATE = timestampToBigDecimal(param);
    }

    /**
     * Sets the expiration date.
     *
     * @param param
     *            the new expiration date
     */
    public void setExpirationDate(final BigDecimal param) {
        REC_EXPIRATION_DATE = param;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public int getId() {
        return REC_ID;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final int param) {
        REC_ID = param;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final String param) {
        REC_ID = Integer.parseInt(param);
    }

    /**
     * Gets the monitor.
     *
     * @return the monitor
     */
    public boolean getMonitor() {
        return REC_MONITOR;
    }

    /**
     * Sets the monitor.
     *
     * @param param
     *            the new monitor
     */
    public void setMonitor(final boolean param) {
        REC_MONITOR = param;
    }

    /**
     * Sets the monitor.
     *
     * @param param
     *            the new monitor
     */
    public void setMonitor(final String param) {
        REC_MONITOR = Boolean.parseBoolean(param);
    }

    /**
     * Gets the sequence.
     *
     * @return the sequence
     */
    public int getSequence() {
        return REC_SEQUENCE;
    }

    /**
     * Sets the sequence.
     *
     * @param param
     *            the new sequence
     */
    public void setSequence(final int param) {
        REC_SEQUENCE = param;
    }

    /**
     * Sets the sequence.
     *
     * @param param
     *            the new sequence
     */
    public void setSequence(final String param) {
        REC_SEQUENCE = Integer.parseInt(param);
    }

    /**
     * Gets the time.
     *
     * @return the time
     */
    public java.sql.Timestamp getTime() {
        return bigDecimalToTimestamp(REC_TIME);
    }

    /**
     * Sets the time.
     *
     * @param param
     *            the new time
     */
    public void setTime(final java.sql.Timestamp param) {
        REC_TIME = timestampToBigDecimal(param);
    }

    /**
     * Sets the time.
     *
     * @param param
     *            the new time
     */
    public void setTime(final BigDecimal param) {
        REC_TIME = param;
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
}
