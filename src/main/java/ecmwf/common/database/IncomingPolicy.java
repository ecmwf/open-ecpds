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
 * The Class IncomingPolicy.
 */
public class IncomingPolicy extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -2865241853138289995L;

    /** The inp id. */
    protected String INP_ID;

    /** The inp comment. */
    protected String INP_COMMENT;

    /** The inp active. */
    protected boolean INP_ACTIVE;

    /** The inp data. */
    protected String INP_DATA;

    /**
     * Instantiates a new incoming policy.
     */
    public IncomingPolicy() {
    }

    /**
     * Instantiates a new incoming policy.
     *
     * @param id
     *            the id
     */
    public IncomingPolicy(final String id) {
        INP_ID = id;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId() {
        return INP_ID;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final String param) {
        INP_ID = param;
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    public String getComment() {
        return INP_COMMENT;
    }

    /**
     * Sets the comment.
     *
     * @param param
     *            the new comment
     */
    public void setComment(final String param) {
        INP_COMMENT = param;
    }

    /**
     * Gets the active.
     *
     * @return the active
     */
    public boolean getActive() {
        return INP_ACTIVE;
    }

    /**
     * Sets the active.
     *
     * @param param
     *            the new active
     */
    public void setActive(final boolean param) {
        INP_ACTIVE = param;
    }

    /**
     * Sets the active.
     *
     * @param param
     *            the new active
     */
    public void setActive(final String param) {
        INP_ACTIVE = Boolean.parseBoolean(param);
    }

    /**
     * Gets the data.
     *
     * @return the data
     */
    public String getData() {
        return INP_DATA;
    }

    /**
     * Sets the data.
     *
     * @param param
     *            the new data
     */
    public void setData(final String param) {
        INP_DATA = param;
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return Objects.hash(INP_ID);
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
        final var other = (IncomingPolicy) obj;
        return Objects.equals(INP_ID, other.INP_ID);
    }
}
