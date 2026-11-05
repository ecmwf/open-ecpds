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
 * granted to it by virtue of its status as an inter-governmental organization.
 */

package ecmwf.common.database;

import java.io.Serializable;

/** Mount and backing-device details reported by a Data Mover for one logical storage volume. */
public record StorageVolumeInfo(int volume, String volumePath, String device, String mountPoint, String fileSystem)
        implements Serializable {
    private static final long serialVersionUID = 1L;
}
