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
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */

public class MSUser extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 845086690856940617L;

    /** The ecd name. */
    protected String ECD_NAME;

    /** The ecu name. */
    protected String ECU_NAME;

    /** The msu active. */
    protected boolean MSU_ACTIVE;

    /** The msu comment. */
    protected String MSU_COMMENT;

    /** The msu data. */
    protected String MSU_DATA;

    /** The msu dir. */
    protected String MSU_DIR;

    /** The msu host. */
    protected String MSU_HOST;

    /** The msu login. */
    protected String MSU_LOGIN;

    /** The msu name. */
    protected String MSU_NAME;

    /** The msu passwd. */
    protected String MSU_PASSWD;

    /** The ecuser. */
    protected ECUser ecuser;

    /** The ectrans destination. */
    protected ECtransDestination ectransDestination;

    /**
     * Instantiates a new MS user.
     */
    public MSUser() {
    }

    /**
     * Instantiates a new MS user.
     *
     * @param name
     *            the name
     */
    public MSUser(final String name) {
        setName(name);
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
     * Gets the active.
     *
     * @return the active
     */
    public boolean getActive() {
        return MSU_ACTIVE;
    }

    /**
     * Sets the active.
     *
     * @param param
     *            the new active
     */
    public void setActive(final boolean param) {
        MSU_ACTIVE = param;
    }

    /**
     * Sets the active.
     *
     * @param param
     *            the new active
     */
    public void setActive(final String param) {
        MSU_ACTIVE = Boolean.parseBoolean(param);
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    public String getComment() {
        return MSU_COMMENT;
    }

    /**
     * Sets the comment.
     *
     * @param param
     *            the new comment
     */
    public void setComment(final String param) {
        MSU_COMMENT = param;
    }

    /**
     * Gets the data.
     *
     * @return the data
     */
    public String getData() {
        return MSU_DATA;
    }

    /**
     * Sets the data.
     *
     * @param param
     *            the new data
     */
    public void setData(final String param) {
        MSU_DATA = param;
    }

    /**
     * Gets the dir.
     *
     * @return the dir
     */
    public String getDir() {
        return MSU_DIR;
    }

    /**
     * Sets the dir.
     *
     * @param param
     *            the new dir
     */
    public void setDir(final String param) {
        MSU_DIR = param;
    }

    /**
     * Gets the host.
     *
     * @return the host
     */
    public String getHost() {
        return MSU_HOST;
    }

    /**
     * Sets the host.
     *
     * @param param
     *            the new host
     */
    public void setHost(final String param) {
        MSU_HOST = param;
    }

    /**
     * Gets the login.
     *
     * @return the login
     */
    public String getLogin() {
        return MSU_LOGIN;
    }

    /**
     * Sets the login.
     *
     * @param param
     *            the new login
     */
    public void setLogin(final String param) {
        MSU_LOGIN = param;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return MSU_NAME;
    }

    /**
     * Sets the name.
     *
     * @param param
     *            the new name
     */
    public void setName(final String param) {
        MSU_NAME = param;
    }

    /**
     * Gets the passwd.
     *
     * @return the passwd
     */
    public String getPasswd() {
        return MSU_PASSWD;
    }

    /**
     * Sets the passwd.
     *
     * @param param
     *            the new passwd
     */
    public void setPasswd(final String param) {
        MSU_PASSWD = param;
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
