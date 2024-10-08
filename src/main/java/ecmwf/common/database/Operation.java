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

import java.util.Objects;

/**
 * The Class Operation.
 */
public class Operation extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 7215453492398706908L;

    /** The ope comment. */
    protected String OPE_COMMENT;

    /** The ope name. */
    protected String OPE_NAME;

    /** The ope validity. */
    protected int OPE_VALIDITY;

    /**
     * Instantiates a new operation.
     */
    public Operation() {
    }

    /**
     * Instantiates a new operation.
     *
     * @param name
     *            the name
     */
    public Operation(final String name) {
        setName(name);
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    public String getComment() {
        return OPE_COMMENT;
    }

    /**
     * Sets the comment.
     *
     * @param param
     *            the new comment
     */
    public void setComment(final String param) {
        OPE_COMMENT = param;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return OPE_NAME;
    }

    /**
     * Sets the name.
     *
     * @param param
     *            the new name
     */
    public void setName(final String param) {
        OPE_NAME = param;
    }

    /**
     * Gets the validity.
     *
     * @return the validity
     */
    public int getValidity() {
        return OPE_VALIDITY;
    }

    /**
     * Sets the validity.
     *
     * @param param
     *            the new validity
     */
    public void setValidity(final int param) {
        OPE_VALIDITY = param;
    }

    /**
     * Sets the validity.
     *
     * @param param
     *            the new validity
     */
    public void setValidity(final String param) {
        OPE_VALIDITY = Integer.parseInt(param);
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(OPE_NAME);
    }

    /**
     * {@inheritDoc}
     *
     * Equals.
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final var other = (Operation) obj;
        return Objects.equals(OPE_NAME, other.OPE_NAME);
    }
}
