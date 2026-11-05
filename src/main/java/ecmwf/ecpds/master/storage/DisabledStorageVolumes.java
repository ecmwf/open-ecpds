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
 * In applying this license, ECMWF does not waive the privileges and immunities
 * granted to it by virtue of its status as an inter-governmental organization
 * nor does it submit to any jurisdiction.
 */

package ecmwf.ecpds.master.storage;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.ECpdsBase;

/** Persistent Master-side disable state for individual Data Mover storage volumes. */
public final class DisabledStorageVolumes {
    private static final String CONFIG_GROUP = "DisabledStorageVolume";
    private static final Pattern MOVER_NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9_.-]{0,127}");

    private final ECpdsBase database;
    private final Set<String> disabled = ConcurrentHashMap.newKeySet();

    public DisabledStorageVolumes(final ECpdsBase database) throws DataBaseException {
        this.database = database;
        for (final Map.Entry<String, String> entry : database.getSysConfigValuesByGroup(CONFIG_GROUP).entrySet()) {
            if ("true".equalsIgnoreCase(entry.getValue()) && validKey(entry.getKey())) {
                disabled.add(entry.getKey());
            }
        }
    }

    public boolean isDisabled(final String mover, final int volume) {
        return disabled.contains(key(mover, volume));
    }

    public boolean isDisabledInGroup(final String[] movers, final int volume) {
        for (final var mover : movers) {
            if (isDisabled(mover, volume)) {
                return true;
            }
        }
        return false;
    }

    public Set<String> snapshot() {
        return Set.copyOf(disabled);
    }

    public synchronized void setDisabled(final String mover, final int volume, final boolean value)
            throws DataBaseException {
        final var key = key(mover, volume);
        if (value) {
            database.setSysConfigValue(CONFIG_GROUP, key, "true");
            disabled.add(key);
        } else {
            database.deleteSysConfigValue(CONFIG_GROUP, key);
            disabled.remove(key);
        }
    }

    public static String key(final String mover, final int volume) {
        if (mover == null || !MOVER_NAME.matcher(mover).matches() || volume < 0) {
            throw new IllegalArgumentException("Invalid Data Mover or volume");
        }
        return mover + ":" + volume;
    }

    private static boolean validKey(final String key) {
        final var separator = key.lastIndexOf(':');
        if (separator <= 0 || separator == key.length() - 1) {
            return false;
        }
        try {
            key(key.substring(0, separator), Integer.parseInt(key.substring(separator + 1)));
            return true;
        } catch (final IllegalArgumentException e) {
            return false;
        }
    }
}
