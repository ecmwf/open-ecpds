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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;

import ecmwf.common.ecaccess.FileListElement;
import ecmwf.common.technical.ProxySocket;

/**
 * The Class AttachmentAccessProxy.
 */
final class AttachmentAccessProxy implements DataAccessInterface {
    /** The _attachment interface. */
    private final DataAccessInterface _attachmentInterface;

    /**
     * Instantiates a new attachment access proxy.
     *
     * @param attachmentInterface
     *            the attachment interface
     */
    protected AttachmentAccessProxy(final DataAccessInterface attachmentInterface) {
        _attachmentInterface = attachmentInterface;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the file last modified.
     */
    @Override
    public long getFileLastModified(final String destinationName, final String source)
            throws MasterException, IOException {
        if (destinationName == null || source == null || source.isEmpty()) {
            throw new MasterException("Invalid parameter(s) for getFileLastModified");
        }
        return _attachmentInterface.getFileLastModified(destinationName, source);
    }

    /**
     * {@inheritDoc}
     *
     * Size.
     */
    @Override
    public long size(final String destinationName, final String source) throws MasterException, IOException {
        if (destinationName == null || source == null || source.isEmpty()) {
            throw new MasterException("Invalid parameter(s) for size");
        }
        return _attachmentInterface.size(destinationName, source);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the proxy socket input.
     */
    @Override
    public ProxySocket getProxySocketInput(final String destinationName, final String source, final long offset)
            throws MasterException, IOException {
        if (destinationName == null || source == null || source.isEmpty()) {
            throw new MasterException("Invalid parameter(s) for getProxySocketInput");
        }
        return _attachmentInterface.getProxySocketInput(destinationName, source, offset);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the proxy socket input.
     */
    @Override
    public ProxySocket getProxySocketInput(final String destinationName, final String source, final long offset,
            final long length) throws MasterException, IOException {
        throw new MasterException("Range not supported");
    }

    /**
     * {@inheritDoc}
     *
     * Gets the proxy socket output.
     */
    @Override
    public ProxySocket getProxySocketOutput(final String destinationName, final String target, final long offset,
            final int umask) throws MasterException, IOException {
        if (destinationName == null || target == null || target.isEmpty()) {
            throw new MasterException("Invalid parameter(s) for getProxySocketOutput");
        }
        return _attachmentInterface.getProxySocketOutput(destinationName, target, offset, umask);
    }

    /**
     * {@inheritDoc}
     *
     * Delete.
     */
    @Override
    public void delete(final String destinationName, final String source, final boolean force)
            throws MasterException, IOException {
        if (destinationName == null || source == null || source.isEmpty()) {
            throw new MasterException("Invalid parameter(s) for delete");
        }
        _attachmentInterface.delete(destinationName, source, force);
    }

    /**
     * {@inheritDoc}
     *
     * Mkdir.
     */
    @Override
    public void mkdir(final String destinationName, final String path) throws MasterException, IOException {
        if (destinationName == null || path == null || path.isEmpty()) {
            throw new MasterException("Invalid parameter(s) for mkdir");
        }
        _attachmentInterface.mkdir(destinationName, path);
    }

    /**
     * {@inheritDoc}
     *
     * Rmdir.
     */
    @Override
    public void rmdir(final String destinationName, final String path) throws MasterException, IOException {
        if (destinationName == null || path == null || path.isEmpty()) {
            throw new MasterException("Invalid parameter(s) for rmdir");
        }
        _attachmentInterface.rmdir(destinationName, path);
    }

    /**
     * {@inheritDoc}
     *
     * List.
     */
    @Override
    public FileListElement[] list(final String destinationName, final String path) throws MasterException, IOException {
        if (destinationName == null || path == null) {
            throw new MasterException("Invalid parameter(s) for list");
        }
        return _attachmentInterface.list(destinationName, path);
    }

    /**
     * {@inheritDoc}
     *
     * List.
     */
    @Override
    public FileListElement[] list(final String destinationName, final String path, final int sort, final int order)
            throws MasterException, IOException {
        if (destinationName == null || path == null) {
            throw new MasterException("Invalid parameter(s) for list");
        }
        return _attachmentInterface.list(destinationName, path, sort, order);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the.
     */
    @Override
    public FileListElement get(final String destinationName, final String path) throws MasterException, IOException {
        if (destinationName == null || path == null) {
            throw new MasterException("Invalid parameter(s) for list");
        }
        return _attachmentInterface.get(destinationName, path);
    }

    /**
     * {@inheritDoc}
     *
     * Move.
     */
    @Override
    public void move(final String destinationName, final String source, final String target)
            throws MasterException, IOException {
        if (destinationName == null || source == null || source.isEmpty() || target == null || target.isEmpty()) {
            throw new MasterException("Invalid parameter(s) for move");
        }
        _attachmentInterface.move(destinationName, source, target);
    }

    /**
     * {@inheritDoc}
     *
     * Check.
     */
    @Override
    public void check(final ProxySocket proxy) throws MasterException, IOException {
        if (proxy == null) {
            throw new MasterException("Invalid parameter(s) for check");
        }
        _attachmentInterface.check(proxy);
    }
}
