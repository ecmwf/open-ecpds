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

package ecmwf.ecpds.master.plugin.http.controller.monitoring;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.struts.action.ActionForm;

/**
 * The Class MonitoringSessionActionForm.
 */
public class MonitoringSessionActionForm extends ActionForm {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1851050987459488110L;

    /** The Constant DIV. */
    private static final String DIV = "\\|";

    /** The refresh period. */
    private String refreshPeriod = "60";

    /** The status. */
    private String status = "";

    /** The type. */
    private String type = "";

    /** The network. */
    private String network = "";

    /** The application. */
    private String application = "";

    /** The status list. */
    private List<String> statusList = null;

    /** The types list. */
    private List<String> typesList = null;

    /** The networks list. */
    private List<String> networksList = null;

    /**
     * Sets the refresh period.
     *
     * @param s
     *            the new refresh period
     */
    public void setRefreshPeriod(final String s) {
        this.refreshPeriod = s;
    }

    /**
     * Gets the refresh period.
     *
     * @return the refresh period
     */
    public String getRefreshPeriod() {
        return this.refreshPeriod;
    }

    /**
     * Sets the application.
     *
     * @param s
     *            the new application
     */
    public void setApplication(final String s) {
        this.application = s;
    }

    /**
     * Gets the application.
     *
     * @return the application
     */
    public String getApplication() {
        return this.application;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status.
     *
     * @param status
     *            the new status
     */
    public void setStatus(final String status) {
        this.status = status;
        this.statusList = !"".equals(status) ? Arrays.asList(this.status.split(DIV)) : new ArrayList<>();
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type
     *            the new type
     */
    public void setType(final String type) {
        this.type = type;
        this.typesList = !"".equals(type) ? Arrays.asList(this.type.split(DIV)) : new ArrayList<>();
    }

    /**
     * Gets the network.
     *
     * @return the network
     */
    public String getNetwork() {
        return network;
    }

    /**
     * Sets the network.
     *
     * @param network
     *            the new network
     */
    public void setNetwork(final String network) {
        this.network = network;
        this.networksList = !"".equals(network) ? Arrays.asList(this.network.split(DIV)) : new ArrayList<>();
    }

    /**
     * Gets the status list.
     *
     * @return the status list
     */
    public List<String> getStatusList() {
        return statusList;
    }

    /**
     * Gets the types list.
     *
     * @return the types list
     */
    public List<String> getTypesList() {
        return typesList;
    }

    /**
     * Gets the networks list.
     *
     * @return the networks list
     */
    public List<String> getNetworksList() {
        return networksList;
    }

    /**
     * Gets the updated.
     *
     * @return the updated
     */
    public Date getUpdated() {
        return new Date();
    }
}
