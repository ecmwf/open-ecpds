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

package ecmwf.common.ectrans.notification;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;

import ecmwf.common.ectrans.notification.wis2.Wis2NotificationProfile;

/** Explicit selection; an unavailable profile never falls back to script interpretation. */
public final class NotificationProfiles {
    private NotificationProfiles() {
    }

    public static NotificationProfile resolve(final String name) throws IOException {
        return resolve(name, NotificationProfile.class.getClassLoader());
    }

    public static NotificationProfile resolve(final String name, final ClassLoader loader) throws IOException {
        if ("script".equals(name))
            return null;
        final var profiles = new LinkedHashMap<String, NotificationProfile>();
        add(profiles, new Wis2NotificationProfile());
        for (final var profile : ServiceLoader.load(NotificationProfile.class, loader))
            add(profiles, profile);
        final var profile = profiles.get(name);
        if (profile == null)
            throw new IOException("Unknown MQTT notification profile: " + name);
        return profile;
    }

    private static void add(final Map<String, NotificationProfile> profiles, final NotificationProfile profile)
            throws IOException {
        final var name = profile.name();
        if (profile.apiVersion() != NotificationProfile.API_VERSION || name == null
                || !name.matches("[a-z][a-z0-9_-]{0,63}") || "script".equals(name))
            throw new IOException("Invalid or incompatible MQTT notification profile");
        if (profiles.putIfAbsent(name, profile) != null)
            throw new IOException("Duplicate MQTT notification profile: " + name);
    }
}
