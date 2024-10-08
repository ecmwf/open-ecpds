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

package ecmwf.common.plugin;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.Comparator;

/**
 * The Class EventComparator.
 */
public final class EventComparator implements Comparator<PluginEvent<?>> {

    /**
     * {@inheritDoc}
     *
     * Compare.
     */
    @Override
    public int compare(final PluginEvent<?> event1, final PluginEvent<?> event2) {
        return comparePluginEvents(event1, event2);
    }

    /**
     * Compare plugin events.
     *
     * @param event1
     *            the event1
     * @param event2
     *            the event2
     *
     * @return the int
     */
    static int comparePluginEvents(final PluginEvent<?> event1, final PluginEvent<?> event2) {
        return Long.compare(event1.getCreationTime(), event2.getCreationTime());
    }
}
