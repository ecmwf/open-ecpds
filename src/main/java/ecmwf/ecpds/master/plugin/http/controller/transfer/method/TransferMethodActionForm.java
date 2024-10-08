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

package ecmwf.ecpds.master.plugin.http.controller.transfer.method;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.ecpds.master.plugin.http.home.transfer.EcTransModuleHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.EcTransModule;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferMethod;
import ecmwf.web.controller.ECMWFActionForm;

/**
 * The Class TransferMethodActionForm.
 */
public class TransferMethodActionForm extends ECMWFActionForm {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -748252966842514322L;

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(TransferMethodActionForm.class);

    /** The id. */
    private String id = "";

    /** The name. */
    private String name = "";

    /** The value. */
    private String value = "";

    /** The restrict. */
    private String restrict = "";

    /** The resolve. */
    private String resolve = "";

    /** The comment. */
    private String comment = "";

    /** The active. */
    private String active = "";

    /** The ec trans module name. */
    private String ecTransModuleName = "";

    /**
     * Gets the active.
     *
     * @return the active
     */
    public String getActive() {
        return active;
    }

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the restrict.
     *
     * @return the restrict
     */
    public String getRestrict() {
        return restrict;
    }

    /**
     * Gets the resolve.
     *
     * @return the resolve
     */
    public String getResolve() {
        return resolve;
    }

    /**
     * Gets the ec trans module name.
     *
     * @return the ec trans module name
     */
    public String getEcTransModuleName() {
        return ecTransModuleName;
    }

    /**
     * Gets the ec trans module options.
     *
     * @return the ec trans module options
     */
    public Collection<EcTransModule> getEcTransModuleOptions() {
        try {
            return EcTransModuleHome.findAll();
        } catch (final TransferException e) {
            log.error("Problem looking for EcTransModules", e);
            return new ArrayList<>(0);
        }
    }

    /**
     * Sets the active.
     *
     * @param string
     *            the new active
     */
    public void setActive(final String string) {
        active = string;
    }

    /**
     * Sets the comment.
     *
     * @param string
     *            the new comment
     */
    public void setComment(final String string) {
        comment = string;
    }

    /**
     * Sets the id.
     *
     * @param string
     *            the new id
     */
    public void setId(final String string) {
        id = string;
    }

    /**
     * Sets the name.
     *
     * @param string
     *            the new name
     */
    public void setName(final String string) {
        name = string;
    }

    /**
     * Sets the resolve.
     *
     * @param string
     *            the new resolve
     */
    public void setResolve(final String string) {
        resolve = string;
    }

    /**
     * Sets the restrict.
     *
     * @param string
     *            the new restrict
     */
    public void setRestrict(final String string) {
        restrict = string;
    }

    /**
     * Sets the value.
     *
     * @param string
     *            the new value
     */
    public void setValue(final String string) {
        value = string;
    }

    /**
     * Sets the ec trans module name.
     *
     * @param name
     *            the new ec trans module name
     */
    public void setEcTransModuleName(final String name) {
        ecTransModuleName = name;
    }

    /**
     * Populate transfer method.
     *
     * @param h
     *            the h
     */
    protected void populateTransferMethod(final TransferMethod h) {
        h.setName(name);
        h.setValue(value);
        h.setRestrict(convertToBoolean(restrict));
        h.setResolve(convertToBoolean(resolve));
        h.setComment(comment);
        h.setActive(convertToBoolean(active));
        h.setEcTransModuleName(ecTransModuleName);
    }

    /**
     * Populate from transfer method.
     *
     * @param h
     *            the h
     */
    protected void populateFromTransferMethod(final TransferMethod h) {
        this.setId(h.getId());
        this.setName(h.getName());
        this.setValue(h.getValue());
        this.setRestrict(h.getRestrict() ? "on" : "off");
        this.setResolve(h.getResolve() ? "on" : "off");
        this.setComment(h.getComment());
        this.setActive(h.getActive() ? "on" : "off");
        this.setEcTransModuleName(h.getEcTransModuleName());
    }
}
