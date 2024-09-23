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
 * ECMWF Product Data Store (ECPDS) Project.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */
public class ECtransHistory extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -8607911332776732997L;

    /** The ecd name. */
    protected String ECD_NAME;

    /** The ech action. */
    protected String ECH_ACTION;

    /** The ech comment. */
    protected String ECH_COMMENT;

    /** The ech date. */
    protected java.sql.Date ECH_DATE;

    /** The ech error. */
    protected boolean ECH_ERROR;

    /** The ech id. */
    protected int ECH_ID;

    /** The ech location. */
    protected String ECH_LOCATION;

    /** The ech remote. */
    protected String ECH_REMOTE;

    /** The ech time. */
    protected java.sql.Time ECH_TIME;

    /** The ech url. */
    protected String ECH_URL;

    /** The msu name. */
    protected String MSU_NAME;

    /** The msuser. */
    protected MSUser msuser;

    /** The ectrans destination. */
    protected ECtransDestination ectransDestination;

    /**
     * Instantiates a new ectrans history.
     */
    public ECtransHistory() {
    }

    /**
     * Instantiates a new ectrans history.
     *
     * @param id
     *            the id
     */
    public ECtransHistory(final int id) {
        setId(id);
    }

    /**
     * Instantiates a new ectrans history.
     *
     * @param id
     *            the id
     */
    public ECtransHistory(final String id) {
        setId(id);
    }

    /**
     * Gets the ectrans destination name.
     *
     * @return the ectrans destination name
     */
    public String getECtransDestinationName() {
        return ECD_NAME;
    }

    /**
     * Sets the ectrans destination name.
     *
     * @param param
     *            the new ectrans destination name
     */
    public void setECtransDestinationName(final String param) {
        ECD_NAME = param;
    }

    /**
     * Gets the action.
     *
     * @return the action
     */
    public String getAction() {
        return ECH_ACTION;
    }

    /**
     * Sets the action.
     *
     * @param param
     *            the new action
     */
    public void setAction(final String param) {
        ECH_ACTION = param;
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    public String getComment() {
        return ECH_COMMENT;
    }

    /**
     * Sets the comment.
     *
     * @param param
     *            the new comment
     */
    public void setComment(final String param) {
        ECH_COMMENT = param;
    }

    /**
     * Gets the date.
     *
     * @return the date
     */
    public java.sql.Date getDate() {
        return ECH_DATE;
    }

    /**
     * Sets the date.
     *
     * @param param
     *            the new date
     */
    public void setDate(final java.sql.Date param) {
        ECH_DATE = param;
    }

    /**
     * Gets the error.
     *
     * @return the error
     */
    public boolean getError() {
        return ECH_ERROR;
    }

    /**
     * Sets the error.
     *
     * @param param
     *            the new error
     */
    public void setError(final boolean param) {
        ECH_ERROR = param;
    }

    /**
     * Sets the error.
     *
     * @param param
     *            the new error
     */
    public void setError(final String param) {
        ECH_ERROR = Boolean.parseBoolean(param);
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public int getId() {
        return ECH_ID;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final int param) {
        ECH_ID = param;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final String param) {
        ECH_ID = Integer.parseInt(param);
    }

    /**
     * Gets the location.
     *
     * @return the location
     */
    public String getLocation() {
        return ECH_LOCATION;
    }

    /**
     * Sets the location.
     *
     * @param param
     *            the new location
     */
    public void setLocation(final String param) {
        ECH_LOCATION = param;
    }

    /**
     * Gets the remote.
     *
     * @return the remote
     */
    public String getRemote() {
        return ECH_REMOTE;
    }

    /**
     * Sets the remote.
     *
     * @param param
     *            the new remote
     */
    public void setRemote(final String param) {
        ECH_REMOTE = param;
    }

    /**
     * Gets the time.
     *
     * @return the time
     */
    public java.sql.Time getTime() {
        return ECH_TIME;
    }

    /**
     * Sets the time.
     *
     * @param param
     *            the new time
     */
    public void setTime(final java.sql.Time param) {
        ECH_TIME = param;
    }

    /**
     * Gets the url.
     *
     * @return the url
     */
    public String getUrl() {
        return ECH_URL;
    }

    /**
     * Sets the url.
     *
     * @param param
     *            the new url
     */
    public void setUrl(final String param) {
        ECH_URL = param;
    }

    /**
     * Gets the MS user name.
     *
     * @return the MS user name
     */
    public String getMSUserName() {
        return MSU_NAME;
    }

    /**
     * Sets the MS user name.
     *
     * @param param
     *            the new MS user name
     */
    public void setMSUserName(final String param) {
        MSU_NAME = param;
    }

    /**
     * Gets the MS user.
     *
     * @return the MS user
     */
    public MSUser getMSUser() {
        return msuser;
    }

    /**
     * Sets the MS user.
     *
     * @param param
     *            the new MS user
     */
    public void setMSUser(final MSUser param) {
        msuser = param;
    }

    /**
     * Gets the ectrans destination.
     *
     * @return the ectrans destination
     */
    public ECtransDestination getECtransDestination() {
        return ectransDestination;
    }

    /**
     * Sets the ectrans destination.
     *
     * @param param
     *            the new ectrans destination
     */
    public void setECtransDestination(final ECtransDestination param) {
        ectransDestination = param;
    }
}
