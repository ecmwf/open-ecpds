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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Class DestinationBackup.
 */
public class DestinationBackup implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The destinations. */
    protected Set<DestinationList> destinations;

    /** The aliases. */
    protected List<Alias> aliases;

    /** The methods. */
    protected Set<TransferMethod> methods;

    /** The ecusers. */
    protected Set<ECUser> ecusers;

    /**
     * Gets the destinations.
     *
     * @return the destinations
     */
    public Set<DestinationList> getDestinations() {
        return destinations;
    }

    /**
     * Gets the aliases.
     *
     * @return the aliases
     */
    public List<Alias> getAliases() {
        return aliases;
    }

    /**
     * Gets the EC users.
     *
     * @return the EC users
     */
    public Set<ECUser> getECUsers() {
        return ecusers;
    }

    /**
     * Gets the methods.
     *
     * @return the methods
     */
    public Set<TransferMethod> getMethods() {
        return methods;
    }

    /**
     * Instantiates a new destination backup.
     */
    public DestinationBackup() {
        destinations = new HashSet<>();
        aliases = new ArrayList<>();
        methods = new HashSet<>();
        ecusers = new HashSet<>();
    }

    /**
     * Adds the.
     *
     * @param destination
     *            the destination
     * @param associations
     *            the associations
     * @param aliases
     *            the aliases
     */
    public void add(final Destination destination, final List<Association> associations, final List<Alias> aliases) {
        destinations.add(new DestinationList(destination, associations));
        this.aliases.addAll(aliases);
        final var ecuser = destination.getECUser();
        if (ecuser != null) {
            ecusers.add(ecuser);
        }
        for (final Association association : associations) {
            final var host = association.getHost();
            if (host != null) {
                methods.add(host.getTransferMethod());
                ecusers.add(host.getECUser());
            }
        }
    }
}
