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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.text.Util.isNotEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.StreamManager;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.model.users.User;
import ecmwf.web.util.bean.Pair;

/**
 * The Class FilterActionForm.
 */
public class FilterActionForm extends ECMWFActionForm {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 8714360388397772627L;

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(FilterActionForm.class);

    /** The Constant dataFormat. */
    protected static final String dateFormat = "yyyy-MM-dd";

    /** The destination. */
    private String destination;

    /** The email. */
    private String email;

    /** The filter. */
    private String filter;

    /** The filter. */
    private String pattern;

    /** The date (yesterday by default). */
    private String date = Format.formatTime(dateFormat, System.currentTimeMillis() - Timer.ONE_DAY);

    /** The include stdby (include stdby files by default). */
    private String includeStdby = "off";

    /**
     * Gets the filter name options.
     *
     * @return the filter name options
     */
    public Collection<Pair> getFilterNameOptions() {
        final Collection<Pair> options = new ArrayList<>();
        for (final String mode : StreamManager.modes) {
            // We don't want to allow checking NONE compression!
            if (!StreamManager.NONE.equals(mode)) {
                options.add(new Pair(mode, mode));
            }
        }
        return options;
    }

    /**
     * Gets the destination options.
     *
     * @return the destination options
     */
    public List<Pair> getDestinationOptions() {
        try {
            return Util.getDestinationPairList(DestinationHome.findAllNamesAndComments(), List.of());
        } catch (final TransferException e) {
            log.error("Problem getting Destinations", e);
            return new ArrayList<>(0);
        }
    }

    /**
     * Sets the default values.
     *
     * @param u
     *            the new default values
     */
    public void setDefaultValues(final User u) {
        final var ecUserName = u.getId();
        if (isNotEmpty(ecUserName)) {
            email = ecUserName + "@ecmwf.int";
        }
    }

    /**
     * Gets the destination.
     *
     * @return the destination
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Sets the destination.
     *
     * @param destination
     *            the new destination
     */
    public void setDestination(final String destination) {
        this.destination = destination;
    }

    /**
     * Gets the email.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email.
     *
     * @param email
     *            the new email
     */
    public void setEmail(final String email) {
        this.email = email;
    }

    /**
     * Gets the filter.
     *
     * @return the filter
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Sets the filter.
     *
     * @param filter
     *            the new filter
     */
    public void setFilter(final String filter) {
        this.filter = filter;
    }

    /**
     * Gets the pattern.
     *
     * @return the pattern
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Sets the pattern.
     *
     * @param pattern
     *            the new pattern
     */
    public void setPattern(final String pattern) {
        this.pattern = pattern;
    }

    /**
     * Gets the date.
     *
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * Sets the date.
     *
     * @param date
     *            the new date
     */
    public void setDate(final String date) {
        this.date = date;
    }

    /**
     * Gets the include stdby.
     *
     * @return the include stdby
     */
    public String getIncludeStdby() {
        return includeStdby;
    }

    /**
     * Gets the include stdby.
     *
     * @return the include stdby
     */
    public boolean getIncludeStdbyBoolean() {
        return convertToBoolean(includeStdby);
    }

    /**
     * Sets the include stdby.
     *
     * @param includeStdby
     *            the new include stdby
     */
    public void setIncludeStdby(final String includeStdby) {
        this.includeStdby = includeStdby;
    }
}
