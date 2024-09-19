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

package ecmwf.ecpds.mover.plugin.http;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.ecaccess.EccmdException;
import ecmwf.common.ecaccess.FileListElement;
import ecmwf.common.ecaccess.StarterServer;
import ecmwf.common.ecaccess.UserSession;
import ecmwf.common.ectrans.ECtransOptions;
import ecmwf.common.technical.MonitoredInputStream;
import ecmwf.common.technical.ProxyEvent;
import ecmwf.ecpds.mover.MoverServer;

/**
 * The Class Blob.
 */
public class Blob {

    /** The Constant logger. */
    private static final Logger logger = LogManager.getLogger(Blob.class);

    /** The Constant _mover. */
    private static final MoverServer _mover = StarterServer.getInstance(MoverServer.class);

    /** The element. */
    private final FileListElement _element;

    /** The file size. */
    private final long _fileSize;

    /** The remote address. */
    private final String _remoteAddress;

    /** The session. */
    private final UserSession _session;

    /** The metadata. */
    private final BlobMetadata _metadata;

    /** The content length. */
    private final long _contentLength;

    /** The offset. */
    private final long _offset;

    /**
     * Instantiates a new blob.
     *
     * @param remoteAddress
     *            the remote address
     * @param session
     *            the session
     * @param metadata
     *            the metadata
     * @param options
     *            the options
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    Blob(final String remoteAddress, final UserSession session, final BlobMetadata metadata, final GetOptions options)
            throws IOException {
        try {
            _element = session.getFileListElement(metadata.getPath());
            _fileSize = Long.parseLong(_element.getSize());
            _remoteAddress = remoteAddress;
            _session = session;
            _metadata = metadata;
            final var rangeFrom = options.getRangeFrom();
            final var rangeTo = options.getRangeTo();
            if (rangeTo != -1) {
                // This is a range
                _contentLength = rangeTo - rangeFrom + 1;
                _offset = rangeFrom;
            } else if (rangeFrom < 0) {
                // This is a tail
                _contentLength = -1 * rangeFrom;
                _offset = _fileSize + rangeFrom;
            } else {
                // This is a at
                _contentLength = _fileSize - rangeFrom;
                _offset = rangeFrom;
            }
        } catch (final EccmdException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Gets the metadata.
     *
     * @return the metadata
     */
    public BlobMetadata getMetadata() {
        return _metadata;
    }

    /**
     * Gets the content range.
     *
     * @return the content range
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public String getContentRange() throws IOException {
        return "bytes " + _offset + "-" + (_offset + _contentLength - 1) + "/" + _element.getSize();
    }

    /**
     * Gets the content length.
     *
     * @return the content length
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public long getContentLength() throws IOException {
        return _contentLength;
    }

    /**
     * Open stream.
     *
     * @return the input stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public InputStream openStream() throws IOException {
        logger.debug("OpenStream: " + _offset + "[" + _contentLength + "]");
        try {
            final var proxy = _session.getProxySocketInput(_metadata.getPath(), _offset, _contentLength);
            // Setting the event for the transfer history!
            return new MonitoredInputStream(proxy.getDataInputStream()) {
                @Override
                public void close() throws IOException {
                    try {
                        super.close();
                    } finally {
                        final var setup = _session.getECtransSetup();
                        if (setup == null || setup.getBoolean(ECtransOptions.USER_PORTAL_TRIGGER_EVENT)) {
                            // Populating with the transfer rate informations!
                            final var event = new ProxyEvent(proxy);
                            event.setProtocol("s3");
                            event.setLocalHost(_mover.getRoot());
                            event.setRemoteHost(_remoteAddress);
                            event.setUserType(ProxyEvent.UserType.DATA_USER);
                            event.setUserName(_session.getUser());
                            event.setDuration(getDuration());
                            event.setStartTime(getStartTime());
                            event.setSent(getByteSent());
                        }
                        logger.debug("Sent: " + getByteSent() + " (" + _offset + "[" + _contentLength + "])");
                        try {
                            _session.check(proxy);
                        } catch (final EccmdException e) {
                            throw new IOException(e.getMessage());
                        }
                    }
                }
            };
        } catch (final EccmdException e) {
            throw new IOException(e.getMessage());
        }
    }
}
