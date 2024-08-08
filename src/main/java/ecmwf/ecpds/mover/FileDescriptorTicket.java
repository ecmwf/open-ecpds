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

package ecmwf.ecpds.mover;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import ecmwf.common.ecaccess.AbstractTicket;
import ecmwf.common.text.Format;
import ecmwf.ecpds.mover.MoverServer.FileDescriptor;

/**
 * The Class FileDescriptorTicket.
 */
public final class FileDescriptorTicket extends AbstractTicket {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -1060691526399176391L;

    /** The _file. */
    private final FileDescriptor _file;

    /**
     * Instantiates a new file descriptor ticket.
     *
     * @param file
     *            the file
     */
    public FileDescriptorTicket(final FileDescriptor file) {
        _file = file;
    }

    /**
     * Gets the file descriptor.
     *
     * @return the file descriptor
     */
    public FileDescriptor getFileDescriptor() {
        return _file;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    @Override
    public String getStatus() {
        return "[" + hasError() + "][" + _file.getDataFile().getId() + "]["
                + Format.formatDuration(System.currentTimeMillis() - getTime()) + "]";
    }
}
