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

package ecmwf.ecpds.master;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.Serializable;
import java.util.Map;

import ecmwf.common.database.Alias;
import ecmwf.common.database.Association;
import ecmwf.common.database.Destination;
import ecmwf.common.database.DestinationECUser;

/**
 * The Class DestinationDataBaseCache.
 */
class DestinationDataBaseCache implements Serializable, Cloneable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -4144300869386351560L;

    /** The _destination. */
    private Destination _destination = null;

    /** The _associations. */
    private Map<String, Association> _associations = null;

    /** The _aliases. */
    private Map<String, Alias> _aliases = null;

    /** The _destination ec users. */
    private Map<String, DestinationECUser> _destinationECUsers = null;

    /** The _bad data transfers count. */
    private Integer _badDataTransfersCount = null;

    /**
     * Gets the destination name.
     *
     * @return the destination name
     */
    public String getDestinationName() {
        return _destination.getName();
    }

    /**
     * Gets the associations.
     *
     * @return the associations
     */
    public Map<String, Association> getAssociations() {
        return _associations;
    }

    /**
     * Sets the associations.
     *
     * @param associations
     *            the associations
     */
    public void setAssociations(final Map<String, Association> associations) {
        _associations = associations;
    }

    /**
     * Gets the aliases.
     *
     * @return the aliases
     */
    public Map<String, Alias> getAliases() {
        return _aliases;
    }

    /**
     * Sets the aliases.
     *
     * @param aliases
     *            the aliases
     */
    public void setAliases(final Map<String, Alias> aliases) {
        _aliases = aliases;
    }

    /**
     * Gets the destination ec users.
     *
     * @return the destination ec users
     */
    public Map<String, DestinationECUser> getDestinationECUsers() {
        return _destinationECUsers;
    }

    /**
     * Sets the destination ec users.
     *
     * @param destinationECUsers
     *            the destination ec users
     */
    public void setDestinationECUsers(final Map<String, DestinationECUser> destinationECUsers) {
        _destinationECUsers = destinationECUsers;
    }

    /**
     * Gets the destination.
     *
     * @return the destination
     */
    public Destination getDestination() {
        return _destination;
    }

    /**
     * Sets the destination.
     *
     * @param destination
     *            the new destination
     */
    public void setDestination(final Destination destination) {
        _destination = destination;
    }

    /**
     * Gets the bad data transfers count.
     *
     * @return the bad data transfers count
     */
    public int getBadDataTransfersCount() {
        return _badDataTransfersCount == null ? 0 : _badDataTransfersCount;
    }

    /**
     * Sets the bad data transfers count.
     *
     * @param badDataTransfersCount
     *            the new bad data transfers count
     */
    public void setBadDataTransfersCount(final Integer badDataTransfersCount) {
        _badDataTransfersCount = badDataTransfersCount;
    }

    /**
     * Clone.
     *
     * @return the object
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (final CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }
}
