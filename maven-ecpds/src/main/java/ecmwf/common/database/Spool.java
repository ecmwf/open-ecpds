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
 * ECMWF Product Data Store (OpenPDS) Project.
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */

public class Spool extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 8889754242438720317L;

    /** The ecu name. */
    protected String ECU_NAME;

    /** The spo comment. */
    protected String SPO_COMMENT;

    /** The spo deleted. */
    protected boolean SPO_DELETED;

    /** The spo expiry date. */
    protected java.sql.Date SPO_EXPIRY_DATE;

    /** The spo expiry time. */
    protected java.sql.Time SPO_EXPIRY_TIME;

    /** The spo id. */
    protected int SPO_ID;

    /** The spo mail on end. */
    protected boolean SPO_MAIL_ON_END;

    /** The spo mail on error. */
    protected boolean SPO_MAIL_ON_ERROR;

    /** The spo mail on retry. */
    protected boolean SPO_MAIL_ON_RETRY;

    /** The spo mail on start. */
    protected boolean SPO_MAIL_ON_START;

    /** The spo priority. */
    protected int SPO_PRIORITY;

    /** The spo retry count. */
    protected int SPO_RETRY_COUNT;

    /** The spo retry done. */
    protected int SPO_RETRY_DONE;

    /** The spo retry frequency. */
    protected int SPO_RETRY_FREQUENCY;

    /** The spo maximum duration. */
    protected long SPO_MAXIMUM_DURATION;

    /** The spo start date. */
    protected java.sql.Date SPO_START_DATE;

    /** The spo start time. */
    protected java.sql.Time SPO_START_TIME;

    /** The spo status. */
    protected String SPO_STATUS;

    /** The spo user mail. */
    protected String SPO_USER_MAIL;

    /** The ecuser. */
    protected ECUser ecuser;

    /**
     * Instantiates a new spool.
     */
    public Spool() {
    }

    /**
     * Instantiates a new spool.
     *
     * @param id
     *            the id
     */
    public Spool(final int id) {
        setId(id);
    }

    /**
     * Instantiates a new spool.
     *
     * @param id
     *            the id
     */
    public Spool(final String id) {
        setId(id);
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
     * Gets the comment.
     *
     * @return the comment
     */
    public String getComment() {
        return SPO_COMMENT;
    }

    /**
     * Sets the comment.
     *
     * @param param
     *            the new comment
     */
    public void setComment(final String param) {
        SPO_COMMENT = strim(param, 4095);
    }

    /**
     * Gets the deleted.
     *
     * @return the deleted
     */
    public boolean getDeleted() {
        return SPO_DELETED;
    }

    /**
     * Sets the deleted.
     *
     * @param param
     *            the new deleted
     */
    public void setDeleted(final boolean param) {
        SPO_DELETED = param;
    }

    /**
     * Sets the deleted.
     *
     * @param param
     *            the new deleted
     */
    public void setDeleted(final String param) {
        SPO_DELETED = Boolean.parseBoolean(param);
    }

    /**
     * Gets the expiry date.
     *
     * @return the expiry date
     */
    public java.sql.Date getExpiryDate() {
        return SPO_EXPIRY_DATE;
    }

    /**
     * Sets the expiry date.
     *
     * @param param
     *            the new expiry date
     */
    public void setExpiryDate(final java.sql.Date param) {
        SPO_EXPIRY_DATE = param;
    }

    /**
     * Gets the expiry time.
     *
     * @return the expiry time
     */
    public java.sql.Time getExpiryTime() {
        return SPO_EXPIRY_TIME;
    }

    /**
     * Sets the expiry time.
     *
     * @param param
     *            the new expiry time
     */
    public void setExpiryTime(final java.sql.Time param) {
        SPO_EXPIRY_TIME = param;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public int getId() {
        return SPO_ID;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final int param) {
        SPO_ID = param;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final String param) {
        SPO_ID = Integer.parseInt(param);
    }

    /**
     * Gets the mail on end.
     *
     * @return the mail on end
     */
    public boolean getMailOnEnd() {
        return SPO_MAIL_ON_END;
    }

    /**
     * Sets the mail on end.
     *
     * @param param
     *            the new mail on end
     */
    public void setMailOnEnd(final boolean param) {
        SPO_MAIL_ON_END = param;
    }

    /**
     * Sets the mail on end.
     *
     * @param param
     *            the new mail on end
     */
    public void setMailOnEnd(final String param) {
        SPO_MAIL_ON_END = Boolean.parseBoolean(param);
    }

    /**
     * Gets the mail on error.
     *
     * @return the mail on error
     */
    public boolean getMailOnError() {
        return SPO_MAIL_ON_ERROR;
    }

    /**
     * Sets the mail on error.
     *
     * @param param
     *            the new mail on error
     */
    public void setMailOnError(final boolean param) {
        SPO_MAIL_ON_ERROR = param;
    }

    /**
     * Sets the mail on error.
     *
     * @param param
     *            the new mail on error
     */
    public void setMailOnError(final String param) {
        SPO_MAIL_ON_ERROR = Boolean.parseBoolean(param);
    }

    /**
     * Gets the mail on retry.
     *
     * @return the mail on retry
     */
    public boolean getMailOnRetry() {
        return SPO_MAIL_ON_RETRY;
    }

    /**
     * Sets the mail on retry.
     *
     * @param param
     *            the new mail on retry
     */
    public void setMailOnRetry(final boolean param) {
        SPO_MAIL_ON_RETRY = param;
    }

    /**
     * Sets the mail on retry.
     *
     * @param param
     *            the new mail on retry
     */
    public void setMailOnRetry(final String param) {
        SPO_MAIL_ON_RETRY = Boolean.parseBoolean(param);
    }

    /**
     * Gets the mail on start.
     *
     * @return the mail on start
     */
    public boolean getMailOnStart() {
        return SPO_MAIL_ON_START;
    }

    /**
     * Sets the mail on start.
     *
     * @param param
     *            the new mail on start
     */
    public void setMailOnStart(final boolean param) {
        SPO_MAIL_ON_START = param;
    }

    /**
     * Sets the mail on start.
     *
     * @param param
     *            the new mail on start
     */
    public void setMailOnStart(final String param) {
        SPO_MAIL_ON_START = Boolean.parseBoolean(param);
    }

    /**
     * Gets the priority.
     *
     * @return the priority
     */
    public int getPriority() {
        return SPO_PRIORITY;
    }

    /**
     * Sets the priority.
     *
     * @param param
     *            the new priority
     */
    public void setPriority(final int param) {
        SPO_PRIORITY = param;
    }

    /**
     * Sets the priority.
     *
     * @param param
     *            the new priority
     */
    public void setPriority(final String param) {
        SPO_PRIORITY = Integer.parseInt(param);
    }

    /**
     * Gets the retry count.
     *
     * @return the retry count
     */
    public int getRetryCount() {
        return SPO_RETRY_COUNT;
    }

    /**
     * Sets the retry count.
     *
     * @param param
     *            the new retry count
     */
    public void setRetryCount(final int param) {
        SPO_RETRY_COUNT = param;
    }

    /**
     * Sets the retry count.
     *
     * @param param
     *            the new retry count
     */
    public void setRetryCount(final String param) {
        SPO_RETRY_COUNT = Integer.parseInt(param);
    }

    /**
     * Gets the retry done.
     *
     * @return the retry done
     */
    public int getRetryDone() {
        return SPO_RETRY_DONE;
    }

    /**
     * Sets the retry done.
     *
     * @param param
     *            the new retry done
     */
    public void setRetryDone(final int param) {
        SPO_RETRY_DONE = param;
    }

    /**
     * Sets the retry done.
     *
     * @param param
     *            the new retry done
     */
    public void setRetryDone(final String param) {
        SPO_RETRY_DONE = Integer.parseInt(param);
    }

    /**
     * Gets the retry frequency.
     *
     * @return the retry frequency
     */
    public int getRetryFrequency() {
        return SPO_RETRY_FREQUENCY;
    }

    /**
     * Sets the retry frequency.
     *
     * @param param
     *            the new retry frequency
     */
    public void setRetryFrequency(final int param) {
        SPO_RETRY_FREQUENCY = param;
    }

    /**
     * Sets the retry frequency.
     *
     * @param param
     *            the new retry frequency
     */
    public void setRetryFrequency(final String param) {
        SPO_RETRY_FREQUENCY = Integer.parseInt(param);
    }

    /**
     * Gets the maximum duration.
     *
     * @return the maximum duration
     */
    public long getMaximumDuration() {
        return SPO_MAXIMUM_DURATION;
    }

    /**
     * Sets the maximum duration.
     *
     * @param param
     *            the new maximum duration
     */
    public void setMaximumDuration(final long param) {
        SPO_MAXIMUM_DURATION = param;
    }

    /**
     * Sets the maximum duration.
     *
     * @param param
     *            the new maximum duration
     */
    public void setMaximumDuration(final String param) {
        SPO_MAXIMUM_DURATION = Long.parseLong(param);
    }

    /**
     * Gets the start date.
     *
     * @return the start date
     */
    public java.sql.Date getStartDate() {
        return SPO_START_DATE;
    }

    /**
     * Sets the start date.
     *
     * @param param
     *            the new start date
     */
    public void setStartDate(final java.sql.Date param) {
        SPO_START_DATE = param;
    }

    /**
     * Gets the start time.
     *
     * @return the start time
     */
    public java.sql.Time getStartTime() {
        return SPO_START_TIME;
    }

    /**
     * Sets the start time.
     *
     * @param param
     *            the new start time
     */
    public void setStartTime(final java.sql.Time param) {
        SPO_START_TIME = param;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public String getStatus() {
        return SPO_STATUS;
    }

    /**
     * Sets the status.
     *
     * @param param
     *            the new status
     */
    public void setStatus(final String param) {
        SPO_STATUS = param;
    }

    /**
     * Gets the user mail.
     *
     * @return the user mail
     */
    public String getUserMail() {
        return SPO_USER_MAIL;
    }

    /**
     * Sets the user mail.
     *
     * @param param
     *            the new user mail
     */
    public void setUserMail(final String param) {
        SPO_USER_MAIL = param;
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
}
