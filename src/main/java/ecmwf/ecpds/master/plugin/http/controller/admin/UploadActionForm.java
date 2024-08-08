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

package ecmwf.ecpds.master.plugin.http.controller.admin;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.ecpds.master.plugin.http.home.transfer.HostHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.Host;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.ecpds.master.transfer.HostOption;
import ecmwf.web.controller.ECMWFActionForm;

/**
 * The Class UploadActionForm.
 */
public class UploadActionForm extends ECMWFActionForm {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 8714360388397772627L;

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(UploadActionForm.class);

    /** The host. */
    private String host;

    /** The target. */
    private String target;

    /** The from pos. */
    private String fromPos = "0";

    /** The text. */
    private String text;

    /**
     * Gets the host options.
     *
     * @return the host options
     */
    public Collection<Host> getHostOptions() {
        final List<Host> disseminationHosts = new ArrayList<>();
        try {
            final var list = HostHome.findAll();
            for (final Host host : list) {
                if (HostOption.DISSEMINATION.equals(host.getType())) {
                    disseminationHosts.add(host);
                }
            }
        } catch (final TransferException e) {
            log.error("Error getting Hosts", e);
        }
        return disseminationHosts;
    }

    /**
     * Gets the from pos.
     *
     * @return the from pos
     */
    public String getFromPos() {
        return fromPos;
    }

    /**
     * Gets the host.
     *
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * Gets the target.
     *
     * @return the target
     */
    public String getTarget() {
        return target;
    }

    /**
     * Gets the text.
     *
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the from pos.
     *
     * @param string
     *            the new from pos
     */
    public void setFromPos(final String string) {
        fromPos = string;
    }

    /**
     * Sets the host.
     *
     * @param string
     *            the new host
     */
    public void setHost(final String string) {
        host = string;
    }

    /**
     * Sets the target.
     *
     * @param string
     *            the new target
     */
    public void setTarget(final String string) {
        target = string;
    }

    /**
     * Sets the text.
     *
     * @param string
     *            the new text
     */
    public void setText(final String string) {
        text = string;
    }
}
