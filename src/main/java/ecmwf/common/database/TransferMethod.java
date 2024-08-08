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
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.Objects;

/**
 * The Class TransferMethod.
 */
public class TransferMethod extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -1539400136760080062L;

    /** The ecm name. */
    protected String ECM_NAME;

    /** The tme active. */
    protected boolean TME_ACTIVE;

    /** The tme comment. */
    protected String TME_COMMENT;

    /** The tme name. */
    protected String TME_NAME;

    /** The tme resolve. */
    protected boolean TME_RESOLVE;

    /** The tme restrict. */
    protected boolean TME_RESTRICT;

    /** The tme value. */
    protected String TME_VALUE;

    /** The ectrans module. */
    protected ECtransModule ectransModule;

    /**
     * Instantiates a new transfer method.
     */
    public TransferMethod() {
    }

    /**
     * Instantiates a new transfer method.
     *
     * @param name
     *            the name
     */
    public TransferMethod(final String name) {
        setName(name);
    }

    /**
     * Gets the ectrans module name.
     *
     * @return the ectrans module name
     */
    public String getECtransModuleName() {
        return ECM_NAME;
    }

    /**
     * Sets the ectrans module name.
     *
     * @param param
     *            the new ectrans module name
     */
    public void setECtransModuleName(final String param) {
        ECM_NAME = param;
    }

    /**
     * Gets the active.
     *
     * @return the active
     */
    public boolean getActive() {
        return TME_ACTIVE;
    }

    /**
     * Sets the active.
     *
     * @param param
     *            the new active
     */
    public void setActive(final boolean param) {
        TME_ACTIVE = param;
    }

    /**
     * Sets the active.
     *
     * @param param
     *            the new active
     */
    public void setActive(final String param) {
        TME_ACTIVE = Boolean.parseBoolean(param);
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    public String getComment() {
        return TME_COMMENT;
    }

    /**
     * Sets the comment.
     *
     * @param param
     *            the new comment
     */
    public void setComment(final String param) {
        TME_COMMENT = param;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return TME_NAME;
    }

    /**
     * Sets the name.
     *
     * @param param
     *            the new name
     */
    public void setName(final String param) {
        TME_NAME = param;
    }

    /**
     * Gets the resolve.
     *
     * @return the resolve
     */
    public boolean getResolve() {
        return TME_RESOLVE;
    }

    /**
     * Sets the resolve.
     *
     * @param param
     *            the new resolve
     */
    public void setResolve(final boolean param) {
        TME_RESOLVE = param;
    }

    /**
     * Sets the resolve.
     *
     * @param param
     *            the new resolve
     */
    public void setResolve(final String param) {
        TME_RESOLVE = Boolean.parseBoolean(param);
    }

    /**
     * Gets the restrict.
     *
     * @return the restrict
     */
    public boolean getRestrict() {
        return TME_RESTRICT;
    }

    /**
     * Sets the restrict.
     *
     * @param param
     *            the new restrict
     */
    public void setRestrict(final boolean param) {
        TME_RESTRICT = param;
    }

    /**
     * Sets the restrict.
     *
     * @param param
     *            the new restrict
     */
    public void setRestrict(final String param) {
        TME_RESTRICT = Boolean.parseBoolean(param);
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue() {
        return TME_VALUE;
    }

    /**
     * Sets the value.
     *
     * @param param
     *            the new value
     */
    public void setValue(final String param) {
        TME_VALUE = param;
    }

    /**
     * Gets the ectrans module.
     *
     * @return the ectrans module
     */
    public ECtransModule getECtransModule() {
        return ectransModule;
    }

    /**
     * Sets the ectrans module.
     *
     * @param param
     *            the new ectrans module
     */
    public void setECtransModule(final ECtransModule param) {
        ectransModule = param;
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return Objects.hash(TME_NAME);
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
        final var other = (TransferMethod) obj;
        return Objects.equals(TME_NAME, other.TME_NAME);
    }
}
