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
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */
public class Notification extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -101197743401251727L;

    /** The ecu name. */
    protected String ECU_NAME;

    /** The not comment. */
    protected String NOT_COMMENT;

    /** The not id. */
    protected int NOT_ID;

    /** The not metadata. */
    protected String NOT_METADATA;

    /** The not name. */
    protected String NOT_NAME;

    /** The not public. */
    protected boolean NOT_PUBLIC;

    /** The not title. */
    protected String NOT_TITLE;

    /** The ecuser. */
    protected ECUser ecuser;

    /**
     * Instantiates a new notification.
     */
    public Notification() {
    }

    /**
     * Instantiates a new notification.
     *
     * @param id
     *            the id
     */
    public Notification(final int id) {
        setId(id);
    }

    /**
     * Instantiates a new notification.
     *
     * @param id
     *            the id
     */
    public Notification(final String id) {
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
        return NOT_COMMENT;
    }

    /**
     * Sets the comment.
     *
     * @param param
     *            the new comment
     */
    public void setComment(final String param) {
        NOT_COMMENT = param;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public int getId() {
        return NOT_ID;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final int param) {
        NOT_ID = param;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final String param) {
        NOT_ID = Integer.parseInt(param);
    }

    /**
     * Gets the metadata.
     *
     * @return the metadata
     */
    public String getMetadata() {
        return NOT_METADATA;
    }

    /**
     * Sets the metadata.
     *
     * @param param
     *            the new metadata
     */
    public void setMetadata(final String param) {
        NOT_METADATA = param;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return NOT_NAME;
    }

    /**
     * Sets the name.
     *
     * @param param
     *            the new name
     */
    public void setName(final String param) {
        NOT_NAME = param;
    }

    /**
     * Gets the public.
     *
     * @return the public
     */
    public boolean getPublic() {
        return NOT_PUBLIC;
    }

    /**
     * Sets the public.
     *
     * @param param
     *            the new public
     */
    public void setPublic(final boolean param) {
        NOT_PUBLIC = param;
    }

    /**
     * Sets the public.
     *
     * @param param
     *            the new public
     */
    public void setPublic(final String param) {
        NOT_PUBLIC = Boolean.parseBoolean(param);
    }

    /**
     * Gets the title.
     *
     * @return the title
     */
    public String getTitle() {
        return NOT_TITLE;
    }

    /**
     * Sets the title.
     *
     * @param param
     *            the new title
     */
    public void setTitle(final String param) {
        NOT_TITLE = param;
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
