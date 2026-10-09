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

import java.io.Serializable;
import java.util.List;

/** Detached, read-only view of current Master scheduler workers. */
public record SchedulerTrafficSnapshot(String type, boolean enabled, String activity, List<Transfer> transfers)
        implements Serializable {

    private static final long serialVersionUID = 1L;

    public SchedulerTrafficSnapshot {
        transfers = List.copyOf(transfers);
    }

    public record Transfer(long transferId, long dataFileId, String destination, String target, String transferStatus,
            String phase, String sourceMover, String targetHost, long size, long startedAt) implements Serializable {
        private static final long serialVersionUID = 1L;
    }
}
