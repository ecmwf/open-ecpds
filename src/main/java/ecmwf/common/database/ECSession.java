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

import ecmwf.common.text.Format;

/**
 * The Class ECSession.
 */
public class ECSession extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -5013815084594652944L;

    /** The ecs comment. */
    protected String ECS_COMMENT;

    /** The ecs date. */
    protected java.sql.Date ECS_DATE;

    /** The ecs id. */
    protected long ECS_ID;

    /** The ecs profile. */
    protected String ECS_PROFILE;

    /** The ecs root. */
    protected String ECS_ROOT;

    /** The ecs time. */
    protected java.sql.Time ECS_TIME;

    /** The ecu name. */
    protected String ECU_NAME;

    /** The ecuser. */
    protected ECUser ecuser;

    /**
     * Instantiates a new EC session.
     */
    public ECSession() {
    }

    /**
     * Instantiates a new EC session.
     *
     * @param id
     *            the id
     */
    public ECSession(final long id) {
        setId(id);
    }

    /**
     * Instantiates a new EC session.
     *
     * @param id
     *            the id
     */
    public ECSession(final String id) {
        setId(id);
    }

    /**
     * Gets the login.
     *
     * @return the login
     */
    public String getLogin() {
        return getECUserName();
    }

    /**
     * Gets the token.
     *
     * @return the token
     */
    public String getToken() {
        return Format.formatString(String.valueOf(ECS_ID), 10, '0', false);
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    public String getComment() {
        return ECS_COMMENT;
    }

    /**
     * Sets the comment.
     *
     * @param param
     *            the new comment
     */
    public void setComment(final String param) {
        ECS_COMMENT = Format.trimString(param, 1024);
    }

    /**
     * Gets the date.
     *
     * @return the date
     */
    public java.sql.Date getDate() {
        return ECS_DATE;
    }

    /**
     * Sets the date.
     *
     * @param param
     *            the new date
     */
    public void setDate(final java.sql.Date param) {
        ECS_DATE = param;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public long getId() {
        return ECS_ID;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final long param) {
        ECS_ID = param;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final String param) {
        ECS_ID = Long.parseLong(param);
    }

    /**
     * Gets the profile.
     *
     * @return the profile
     */
    public String getProfile() {
        return ECS_PROFILE;
    }

    /**
     * Sets the profile.
     *
     * @param param
     *            the new profile
     */
    public void setProfile(final String param) {
        ECS_PROFILE = param;
    }

    /**
     * Gets the root.
     *
     * @return the root
     */
    public String getRoot() {
        return ECS_ROOT;
    }

    /**
     * Sets the root.
     *
     * @param param
     *            the new root
     */
    public void setRoot(final String param) {
        ECS_ROOT = param;
    }

    /**
     * Gets the time.
     *
     * @return the time
     */
    public java.sql.Time getTime() {
        return ECS_TIME;
    }

    /**
     * Sets the time.
     *
     * @param param
     *            the new time
     */
    public void setTime(final java.sql.Time param) {
        ECS_TIME = param;
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
}
