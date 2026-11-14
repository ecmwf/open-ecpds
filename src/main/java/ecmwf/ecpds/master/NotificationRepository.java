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

import javax.management.timer.Timer;
import ecmwf.common.ecaccess.StorageRepository;
import ecmwf.common.plugin.EventComparator;
import ecmwf.common.plugin.PluginEvent;
import ecmwf.common.technical.Cnf;

/**
 * The Class NotificationRepository.
 */
final class NotificationRepository extends StorageRepository<PluginEvent<?>> {
    private final MasterServer _master;

    /**
     * Instantiates a new notification repository.
     *
     * @param name
     *            the name
     */
    NotificationRepository(final MasterServer _master, final String name) {
        // Bind the Master before superclass construction and field initialization.
        this._master = _master;
        super(name, Cnf.at("StorageRepository", "notificationSize", 5),
                Cnf.at("StorageRepository", "notificationDelay", Timer.ONE_SECOND));
        setComparator(new EventComparator());
    }

    /**
     * Update.
     *
     * @param object
     *            the object
     *
     * @throws Exception
     *             the exception
     */
    @Override
    public void update(final PluginEvent<?> object) throws Exception {
        _master.updatePluginEvents(new PluginEvent<?>[] { object });
    }
}
