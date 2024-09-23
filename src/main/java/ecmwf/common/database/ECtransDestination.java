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
public class ECtransDestination extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 6216538331510896076L;

    /** The ecd active. */
    protected boolean ECD_ACTIVE;

    /** The ecd comment. */
    protected String ECD_COMMENT;

    /** The ecd name. */
    protected String ECD_NAME;

    /** The ecd resolve. */
    protected boolean ECD_RESOLVE;

    /** The ecd restrict. */
    protected boolean ECD_RESTRICT;

    /** The ecd value. */
    protected String ECD_VALUE;

    /** The ecm name. */
    protected String ECM_NAME;

    /** The ectrans module. */
    protected ECtransModule ectransModule;

    /**
     * Instantiates a new ectrans destination.
     */
    public ECtransDestination() {
    }

    /**
     * Instantiates a new ectrans destination.
     *
     * @param name
     *            the name
     */
    public ECtransDestination(final String name) {
        setName(name);
    }

    /**
     * Gets the active.
     *
     * @return the active
     */
    public boolean getActive() {
        return ECD_ACTIVE;
    }

    /**
     * Sets the active.
     *
     * @param param
     *            the new active
     */
    public void setActive(final boolean param) {
        ECD_ACTIVE = param;
    }

    /**
     * Sets the active.
     *
     * @param param
     *            the new active
     */
    public void setActive(final String param) {
        ECD_ACTIVE = Boolean.parseBoolean(param);
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    public String getComment() {
        return ECD_COMMENT;
    }

    /**
     * Sets the comment.
     *
     * @param param
     *            the new comment
     */
    public void setComment(final String param) {
        ECD_COMMENT = param;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return ECD_NAME;
    }

    /**
     * Sets the name.
     *
     * @param param
     *            the new name
     */
    public void setName(final String param) {
        ECD_NAME = param;
    }

    /**
     * Gets the resolve.
     *
     * @return the resolve
     */
    public boolean getResolve() {
        return ECD_RESOLVE;
    }

    /**
     * Sets the resolve.
     *
     * @param param
     *            the new resolve
     */
    public void setResolve(final boolean param) {
        ECD_RESOLVE = param;
    }

    /**
     * Sets the resolve.
     *
     * @param param
     *            the new resolve
     */
    public void setResolve(final String param) {
        ECD_RESOLVE = Boolean.parseBoolean(param);
    }

    /**
     * Gets the restrict.
     *
     * @return the restrict
     */
    public boolean getRestrict() {
        return ECD_RESTRICT;
    }

    /**
     * Sets the restrict.
     *
     * @param param
     *            the new restrict
     */
    public void setRestrict(final boolean param) {
        ECD_RESTRICT = param;
    }

    /**
     * Sets the restrict.
     *
     * @param param
     *            the new restrict
     */
    public void setRestrict(final String param) {
        ECD_RESTRICT = Boolean.parseBoolean(param);
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue() {
        return ECD_VALUE;
    }

    /**
     * Sets the value.
     *
     * @param param
     *            the new value
     */
    public void setValue(final String param) {
        ECD_VALUE = param;
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
}
