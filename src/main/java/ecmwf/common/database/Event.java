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

import java.util.Objects;

/**
 * The Class Event.
 */
public class Event extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 8069080526398711614L;

    /** The act id. */
    protected long ACT_ID;

    /** The eve action. */
    protected String EVE_ACTION;

    /** The eve comment. */
    protected String EVE_COMMENT;

    /** The eve date. */
    protected java.sql.Date EVE_DATE;

    /** The eve error. */
    protected boolean EVE_ERROR;

    /** The eve id. */
    protected long EVE_ID;

    /** The eve time. */
    protected java.sql.Time EVE_TIME;

    /** The activity. */
    protected Activity activity;

    /**
     * Instantiates a new event.
     */
    public Event() {
    }

    /**
     * Instantiates a new event.
     *
     * @param id
     *            the id
     */
    public Event(final long id) {
        setId(id);
    }

    /**
     * Instantiates a new event.
     *
     * @param id
     *            the id
     */
    public Event(final String id) {
        setId(id);
    }

    /**
     * Gets the activity id.
     *
     * @return the activity id
     */
    public long getActivityId() {
        return ACT_ID;
    }

    /**
     * Sets the activity id.
     *
     * @param param
     *            the new activity id
     */
    public void setActivityId(final long param) {
        ACT_ID = param;
    }

    /**
     * Sets the activity id.
     *
     * @param param
     *            the new activity id
     */
    public void setActivityId(final String param) {
        ACT_ID = Long.parseLong(param);
    }

    /**
     * Gets the action.
     *
     * @return the action
     */
    public String getAction() {
        return EVE_ACTION;
    }

    /**
     * Sets the action.
     *
     * @param param
     *            the new action
     */
    public void setAction(final String param) {
        EVE_ACTION = param;
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    public String getComment() {
        return EVE_COMMENT;
    }

    /**
     * Sets the comment.
     *
     * @param param
     *            the new comment
     */
    public void setComment(final String param) {
        EVE_COMMENT = param;
    }

    /**
     * Gets the date.
     *
     * @return the date
     */
    public java.sql.Date getDate() {
        return EVE_DATE;
    }

    /**
     * Sets the date.
     *
     * @param param
     *            the new date
     */
    public void setDate(final java.sql.Date param) {
        EVE_DATE = param;
    }

    /**
     * Gets the error.
     *
     * @return the error
     */
    public boolean getError() {
        return EVE_ERROR;
    }

    /**
     * Sets the error.
     *
     * @param param
     *            the new error
     */
    public void setError(final boolean param) {
        EVE_ERROR = param;
    }

    /**
     * Sets the error.
     *
     * @param param
     *            the new error
     */
    public void setError(final String param) {
        EVE_ERROR = Boolean.parseBoolean(param);
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public long getId() {
        return EVE_ID;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final long param) {
        EVE_ID = param;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final String param) {
        EVE_ID = Long.parseLong(param);
    }

    /**
     * Gets the time.
     *
     * @return the time
     */
    public java.sql.Time getTime() {
        return EVE_TIME;
    }

    /**
     * Sets the time.
     *
     * @param param
     *            the new time
     */
    public void setTime(final java.sql.Time param) {
        EVE_TIME = param;
    }

    /**
     * Gets the activity.
     *
     * @return the activity
     */
    public Activity getActivity() {
        return activity;
    }

    /**
     * Sets the activity.
     *
     * @param param
     *            the new activity
     */
    public void setActivity(final Activity param) {
        activity = param;
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return Objects.hash(EVE_ID);
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
        final var other = (Event) obj;
        return EVE_ID == other.EVE_ID;
    }
}
