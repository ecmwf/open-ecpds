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

package ecmwf.common.ssh;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VirtualDirectoryStream implements DirectoryStream<Path> {

    private final List<Path> virtualDirectories;
    private boolean isClosed = false;

    public VirtualDirectoryStream(List<Path> virtualDirectories) {
        this.virtualDirectories = new ArrayList<>(virtualDirectories);
    }

    @Override
    public Iterator<Path> iterator() {
        if (isClosed) {
            throw new IllegalStateException("Stream is closed");
        }
        return virtualDirectories.iterator();
    }

    @Override
    public void close() throws IOException {
        isClosed = true; // Mark the stream as closed
    }
}
