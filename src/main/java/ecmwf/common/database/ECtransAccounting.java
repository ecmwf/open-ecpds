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

public class ECtransAccounting extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -8607911332776732997L;

    /** The eca id. */
    protected int ECA_ID;

    /** The eca action. */
    protected String ECA_ACTION;

    /** The eca start date. */
    protected java.sql.Date ECA_START_DATE;

    /** The eca end date. */
    protected java.sql.Date ECA_END_DATE;

    /** The eca start time. */
    protected java.sql.Time ECA_START_TIME;

    /** The eca end time. */
    protected java.sql.Time ECA_END_TIME;

    /** The eca size. */
    protected long ECA_SIZE;

    /** The eca error. */
    protected boolean ECA_ERROR;

    /** The eca comment. */
    protected String ECA_COMMENT;

    /** The eca source host. */
    protected String ECA_SOURCE_HOST;

    /** The eca location. */
    protected String ECA_LOCATION;

    /** The eca remote. */
    protected String ECA_REMOTE;

    /** The eca source. */
    protected String ECA_SOURCE;

    /** The eca target. */
    protected String ECA_TARGET;

    /** The ecu name. */
    protected String ECU_NAME;

    /** The ecuser. */
    protected ECUser ecuser;

    /**
     * Instantiates a new ectrans accounting.
     */
    public ECtransAccounting() {
    }

    /**
     * Instantiates a new ectrans accounting.
     *
     * @param id
     *            the id
     */
    public ECtransAccounting(final int id) {
        setId(id);
    }

    /**
     * Instantiates a new ectrans accounting.
     *
     * @param id
     *            the id
     */
    public ECtransAccounting(final String id) {
        setId(id);
    }

    /**
     * Gets the action.
     *
     * @return the action
     */
    public String getAction() {
        return ECA_ACTION;
    }

    /**
     * Sets the action.
     *
     * @param param
     *            the new action
     */
    public void setAction(final String param) {
        ECA_ACTION = param;
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    public String getComment() {
        return ECA_COMMENT;
    }

    /**
     * Sets the comment.
     *
     * @param param
     *            the new comment
     */
    public void setComment(final String param) {
        ECA_COMMENT = param;
    }

    /**
     * Gets the start date.
     *
     * @return the start date
     */
    public java.sql.Date getStartDate() {
        return ECA_START_DATE;
    }

    /**
     * Sets the start date.
     *
     * @param param
     *            the new start date
     */
    public void setStartDate(final java.sql.Date param) {
        ECA_START_DATE = param;
    }

    /**
     * Gets the end date.
     *
     * @return the end date
     */
    public java.sql.Date getEndDate() {
        return ECA_END_DATE;
    }

    /**
     * Sets the end date.
     *
     * @param param
     *            the new end date
     */
    public void setEndDate(final java.sql.Date param) {
        ECA_END_DATE = param;
    }

    /**
     * Gets the error.
     *
     * @return the error
     */
    public boolean getError() {
        return ECA_ERROR;
    }

    /**
     * Sets the error.
     *
     * @param param
     *            the new error
     */
    public void setError(final boolean param) {
        ECA_ERROR = param;
    }

    /**
     * Sets the error.
     *
     * @param param
     *            the new error
     */
    public void setError(final String param) {
        ECA_ERROR = Boolean.parseBoolean(param);
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public int getId() {
        return ECA_ID;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final int param) {
        ECA_ID = param;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final String param) {
        ECA_ID = Integer.parseInt(param);
    }

    /**
     * Gets the location.
     *
     * @return the location
     */
    public String getLocation() {
        return ECA_LOCATION;
    }

    /**
     * Sets the location.
     *
     * @param param
     *            the new location
     */
    public void setLocation(final String param) {
        ECA_LOCATION = param;
    }

    /**
     * Gets the source host.
     *
     * @return the source host
     */
    public String getSourceHost() {
        return ECA_SOURCE_HOST;
    }

    /**
     * Sets the source host.
     *
     * @param param
     *            the new source host
     */
    public void setSourceHost(final String param) {
        ECA_SOURCE_HOST = param;
    }

    /**
     * Gets the remote.
     *
     * @return the remote
     */
    public String getRemote() {
        return ECA_REMOTE;
    }

    /**
     * Sets the remote.
     *
     * @param param
     *            the new remote
     */
    public void setRemote(final String param) {
        ECA_REMOTE = param;
    }

    /**
     * Gets the start time.
     *
     * @return the start time
     */
    public java.sql.Time getStartTime() {
        return ECA_START_TIME;
    }

    /**
     * Sets the start time.
     *
     * @param param
     *            the new start time
     */
    public void setStartTime(final java.sql.Time param) {
        ECA_START_TIME = param;
    }

    /**
     * Gets the end time.
     *
     * @return the end time
     */
    public java.sql.Time getEndTime() {
        return ECA_END_TIME;
    }

    /**
     * Sets the end time.
     *
     * @param param
     *            the new end time
     */
    public void setEndTime(final java.sql.Time param) {
        ECA_END_TIME = param;
    }

    /**
     * Gets the source.
     *
     * @return the source
     */
    public String getSource() {
        return ECA_SOURCE;
    }

    /**
     * Sets the source.
     *
     * @param param
     *            the new source
     */
    public void setSource(final String param) {
        ECA_SOURCE = param;
    }

    /**
     * Gets the target.
     *
     * @return the target
     */
    public String getTarget() {
        return ECA_TARGET;
    }

    /**
     * Sets the target.
     *
     * @param param
     *            the new target
     */
    public void setTarget(final String param) {
        ECA_TARGET = param;
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
     * Gets the size.
     *
     * @return the size
     */
    public long getSize() {
        return ECA_SIZE;
    }

    /**
     * Sets the size.
     *
     * @param param
     *            the new size
     */
    public void setSize(final long param) {
        ECA_SIZE = param;
    }

    /**
     * Sets the size.
     *
     * @param param
     *            the new size
     */
    public void setSize(final String param) {
        ECA_SIZE = Long.parseLong(param);
    }
}
