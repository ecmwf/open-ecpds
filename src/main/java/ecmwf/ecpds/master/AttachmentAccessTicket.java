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

import java.io.File;

import ecmwf.common.ecaccess.AbstractTicket;
import ecmwf.common.text.Format;

/**
 * The Class AttachmentAccessTicket.
 */
public final class AttachmentAccessTicket extends AbstractTicket {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -1315878081444087217L;

    /** The Constant INPUT. */
    public static final int INPUT = 0;

    /** The Constant OUTPUT. */
    public static final int OUTPUT = 1;

    /** The _file. */
    private final File _file;

    /** The _mode. */
    private final int _mode;

    /** The _offset. */
    private final long _offset;

    /**
     * Instantiates a new attachment access ticket.
     *
     * @param file
     *            the file
     * @param mode
     *            the mode
     * @param offset
     *            the offset
     */
    public AttachmentAccessTicket(final File file, final int mode, final long offset) {
        this(file, mode, offset, -1);
    }

    /**
     * Instantiates a new attachment access ticket.
     *
     * @param file
     *            the file
     * @param mode
     *            the mode
     * @param offset
     *            the offset
     * @param umask
     *            the umask
     */
    public AttachmentAccessTicket(final File file, final int mode, final long offset, final int umask) {
        _file = file;
        _mode = mode;
        _offset = offset;
    }

    /**
     * Gets the file.
     *
     * @return the file
     */
    public File getFile() {
        return _file;
    }

    /**
     * Gets the mode.
     *
     * @return the mode
     */
    public int getMode() {
        return _mode;
    }

    /**
     * Gets the offset.
     *
     * @return the offset
     */
    public long getOffset() {
        return _offset;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the status.
     */
    @Override
    public String getStatus() {
        return "[" + hasError() + "][" + _mode + "][" + _file.getName() + "]["
                + Format.formatDuration(System.currentTimeMillis() - getTime()) + "]";
    }
}
