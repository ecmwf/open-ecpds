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

import ecmwf.common.text.Format;

/**
 * The Class ExistingStorageDirectory.
 */
public class ExistingStorageDirectory extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -547617357243451889L;

    /** The _file system. */
    private int _fileSystem;

    /** The _files count. */
    private int _filesCount;

    /** The _files count. */
    private long _filesSize;

    /** The _arrived time. */
    private long _arrivedTime;

    /** The _transfer group name. */
    private String _transferGroupName;

    /** The _proxy host name. */
    private String _proxyHostName;

    /**
     * Gets the file system.
     *
     * @return the file system
     */
    public int getFileSystem() {
        return _fileSystem;
    }

    /**
     * Sets the file system.
     *
     * @param fileSystem
     *            the new file system
     */
    public void setFileSystem(final int fileSystem) {
        _fileSystem = fileSystem;
    }

    /**
     * Gets the files count.
     *
     * @return the files count
     */
    public int getFilesCount() {
        return _filesCount;
    }

    /**
     * Sets the files count.
     *
     * @param filesCount
     *            the new files count
     */
    public void setFilesCount(final int filesCount) {
        _filesCount = filesCount;
    }

    /**
     * Gets the files size.
     *
     * @return the files size
     */
    public long getFilesSize() {
        return _filesSize;
    }

    /**
     * Sets the files size.
     *
     * @param filesSize
     *            the new files size
     */
    public void setFilesSize(final long filesSize) {
        _filesSize = filesSize;
    }

    /**
     * Gets the arrived time.
     *
     * @return the arrived time
     */
    public long getArrivedTime() {
        return _arrivedTime;
    }

    /**
     * Sets the arrived time.
     *
     * @param date
     *            the new arrived time
     */
    public void setArrivedTime(final long date) {
        _arrivedTime = date;
    }

    /**
     * Gets the transfer group name.
     *
     * @return the transfer group name
     */
    public String getTransferGroupName() {
        return _transferGroupName;
    }

    /**
     * Sets the transfer group name.
     *
     * @param transferGroupName
     *            the new transfer group name
     */
    public void setTransferGroupName(final String transferGroupName) {
        _transferGroupName = transferGroupName;
    }

    /**
     * Gets the proxy host name.
     *
     * @return the proxy host name
     */
    public String getProxyHostName() {
        return _proxyHostName;
    }

    /**
     * Sets the proxy host name.
     *
     * @param proxyHostName
     *            the new proxy host name
     */
    public void setProxyHostName(final String proxyHostName) {
        _proxyHostName = proxyHostName;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "volume" + _fileSystem + "/" + Format.formatTime("MMddyyyy", _arrivedTime);
    }
}
