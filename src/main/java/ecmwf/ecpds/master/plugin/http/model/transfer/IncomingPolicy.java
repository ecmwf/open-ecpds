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

package ecmwf.ecpds.master.plugin.http.model.transfer;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * An IncomingPolicy, as it is going to be seen from Controller and View for the
 * web application

 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Collection;

import ecmwf.web.model.ModelBean;

/**
 * The Interface IncomingPolicy.
 */
public interface IncomingPolicy extends ModelBean {

    /**
     * {@inheritDoc}
     *
     * Gets the id.
     */
    @Override
    String getId();

    /**
     * Gets the comment.
     *
     * @return the comment
     */
    String getComment();

    /**
     * Gets the active.
     *
     * @return the active
     */
    boolean getActive();

    /**
     * Gets the data.
     *
     * @return the data
     */
    String getData();

    /**
     * Gets the properties.
     *
     * @return the properties
     */
    String getProperties();

    /**
     * Gets the completions.
     *
     * @return the completions
     */
    String getCompletions();

    /**
     * {@inheritDoc}
     *
     * Sets the id.
     */
    @Override
    void setId(String id);

    /**
     * Sets the comment.
     *
     * @param comment
     *            the new comment
     */
    void setComment(String comment);

    /**
     * Sets the active.
     *
     * @param active
     *            the new active
     */
    void setActive(boolean active);

    /**
     * Sets the data.
     *
     * @param data
     *            the new data
     */
    void setData(String data);

    /**
     * Adds the destination.
     *
     * @param d
     *            the d
     */
    void addDestination(Destination d);

    /**
     * Delete destination.
     *
     * @param d
     *            the d
     */
    void deleteDestination(Destination d);

    /**
     * Gets the associated destinations.
     *
     * @return the associated destinations
     *
     * @throws ecmwf.ecpds.master.plugin.http.model.transfer.TransferException
     *             the transfer exception
     */
    Collection<Destination> getAssociatedDestinations() throws TransferException;
}
