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

import static ecmwf.common.text.Util.isNotEmpty;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ecmwf.common.checksum.Checksum;
import ecmwf.common.database.ECUser;
import ecmwf.common.database.ECtransModule;
import ecmwf.common.database.Host;
import ecmwf.common.database.TransferMethod;
import ecmwf.common.technical.BoundedInputStream;
import ecmwf.common.technical.RandomInputStream;
import ecmwf.common.technical.StreamManagerImp;
import ecmwf.common.technical.StreamPlugThread;
import ecmwf.ecpds.mover.MoverServer.FileDescriptor;

/**
 * The Class MoverRepository.
 */
class MoverRepository implements GenericRepository {
    private final MoverServer _mover;

    MoverRepository(final MoverServer _mover) {
        this._mover = _mover;
        super();
    }

    /**
     * Gets the host.
     *
     * @param name
     *            the name
     *
     * @return the host
     */
    @Override
    public Host getHost(final String name) {
        return _mover.transferRepository.getHost(name);
    }

    /**
     * Gets the EC user.
     *
     * @param name
     *            the name
     *
     * @return the EC user
     */
    @Override
    public ECUser getECUser(final String name) {
        return _mover.transferRepository.getECUser(name);
    }

    /**
     * Gets the ectrans module.
     *
     * @param name
     *            the name
     *
     * @return the ectrans module
     */
    @Override
    public ECtransModule getECtransModule(final String name) {
        return _mover.transferRepository.getECtransModule(name);
    }

    /**
     * Gets the transfer method.
     *
     * @param name
     *            the name
     *
     * @return the transfer method
     */
    @Override
    public TransferMethod getTransferMethod(final String name) {
        return _mover.transferRepository.getTransferMethod(name);
    }

    /**
     * Gets the data input stream.
     *
     * @param ticket
     *            the ticket
     *
     * @return the data input stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public InputStream getDataInputStream(final Object ticket) throws IOException {
        final var descriptor = (FileDescriptor) ticket;
        return new java.io.FilterInputStream(openDataInputStream(ticket)) {
            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    java.lang.ref.Reference.reachabilityFence(descriptor);
                }
            }
        };
    }

    private InputStream openDataInputStream(final Object ticket) throws IOException {
        final var desc = (FileDescriptor) ticket;
        final var file = desc.getFile(false);
        final var dataFile = desc.getDataFile();
        final var size = desc.getSize();
        var posn = desc.getPosn();
        var length = desc.getLength();
        var banChecksum = posn > 0 || length != -1; // No checksum possible on a range request!
        InputStream in = null;
        if (desc.isLocal() || !MoverServer.OPERATIONAL) {
            if (!MoverServer.OPERATIONAL && (file.length() != size || !file.canRead())) {
                MoverServer._log.warn("RandomInputStream for: {}", file.getAbsolutePath());
                in = new RandomInputStream(length == -1 ? size - posn : length);
                banChecksum = true; // No checksum should be processed (irrelevant on random data)
                length = 0; // No length required here, the random input stream has the exact size
                posn = 0; // Same as for the length
            } else {
                MoverServer._log.debug("Source found locally: {} ({} bytes)", file.getAbsolutePath(), size);
                final var inputFilter = desc.getInputFilter();
                if (banChecksum && StreamManagerImp.isFiltered(inputFilter)) {
                    // When using a range there is no filter allowed!
                    throw new IOException("Range not supported with filters");
                }
                // If required the skip will be done at the end of this method
                in = StreamManagerImp.getFilters(file.getInputStream(), inputFilter, 0);
            }
        } else {
            MoverServer._log.warn("Source not found locally");
            String message = null;
            Throwable throwable = null;
            try {
                final var hostsForSource = desc.getHostsForSource();
                if (hostsForSource != null && hostsForSource.length > 0) {
                    in = new ECtransInputStream(hostsForSource, dataFile, posn);
                    if (in instanceof final ECtransInputStream ectransIn)
                        desc.setSelectedSourceHost(ectransIn.getHost());
                    posn = 0; // No skip required here, will be processed in the ECtransInputStream
                }
            } catch (final SourceNotAvailableException s) {
                throwable = s.getCause();
                message = s.getMessage();
            } catch (final Throwable t) {
                throwable = t;
                MoverServer._log.debug("Looking for a remote source", t);
            } finally {
                if (in == null) {
                    MoverServer._log.warn("Source not found remotely");
                    // The source is not available anywhere, locally or remotely!
                    final var fnfe = new FileNotFoundException(isNotEmpty(message) ? message : "Source not available");
                    fnfe.initCause(throwable);
                    throw fnfe;
                }
            }
        }
        // Are we supposed to skip data?
        if (posn > 0) {
            // If we skip data then obviously we can't do the checksum on
            // the fly!
            final long result;
            if ((result = in.skip(posn)) != posn) {
                StreamPlugThread.closeQuietly(in);
                throw new IOException("Error skipping bytes (" + result + ")");
            }
        } else if (!banChecksum) {
            // We don't skip data and we get all the bytes (no range), so we can process the
            // Checksum!
            final var checksumValue = dataFile.getChecksum();
            if (isNotEmpty(checksumValue)) {
                // There is a Checksum defined!
                final var filter = desc.getFilter();
                if (isNotEmpty(filter)) {
                    // The transmission is based on a filtered file so do we
                    // have the Checksum for this filtering method?
                    final var searchString = "/" + filter + "=";
                    final var index = checksumValue.indexOf(searchString);
                    if (index != -1) {
                        final var checksumForFilter = checksumValue.substring(index + searchString.length());
                        final var fileSize = dataFile.getFilterSize();
                        if (size == fileSize) {
                            MoverServer._log.debug("Checking against {} hash ({}={})", MoverServer._algorithm.getName(),
                                    filter, checksumForFilter);
                            try {
                                return Checksum.getChecksum(checksumForFilter, MoverServer._algorithm, in)
                                        .getInputStream();
                            } catch (final Throwable t) {
                                MoverServer._log.warn("Cannot init {}", MoverServer._algorithm.getName(), t);
                            }
                        } else {
                            // Should NEVER happen but it is better to check
                            // just in case!
                            MoverServer._log.warn(
                                    "Checksum control disabled for DataFile-{}: file size inconsistency (filter={},length={},size={})",
                                    dataFile.getId(), filter, size, fileSize);
                        }
                    } else {
                        // We don't have any Checksum for this filter!
                        MoverServer._log.warn("Checksum control disabled for DataFile-{}: no Checksum found for {}",
                                dataFile.getId(), filter);
                    }
                } else {
                    // The transmission is based on a standard file (the
                    // filtering might occur but on the fly)!
                    final var fileSize = dataFile.getSize();
                    if (size == fileSize) {
                        // We could have to remove the filtered Checksum
                        // which might have been appended!
                        final var index = checksumValue.indexOf("/");
                        final var checksumForPlain = index != -1 ? checksumValue.substring(0, index) : checksumValue;
                        MoverServer._log.debug("Checking against {} hash ({})", MoverServer._algorithm.getName(),
                                checksumForPlain);
                        try {
                            return Checksum.getChecksum(checksumForPlain, MoverServer._algorithm, in).getInputStream();
                        } catch (final Throwable t) {
                            MoverServer._log.warn("Cannot init {}", MoverServer._algorithm.getName(), t);
                        }
                    } else {
                        // Should NEVER happen but it is better to check
                        // just in case!
                        MoverServer._log.warn(
                                "Checksum control disabled for DataFile-{}: file size inconsistency (length={},size={})",
                                dataFile.getId(), size, fileSize);
                    }
                }
            } else {
                // We have no Checksum set on the DataFile!
                MoverServer._log.warn("No Checksum available for DataFile-{}", dataFile.getId());
            }
        }
        if (length > 0) {
            // We only send the length requested (this is a range request)!
            return new BoundedInputStream(in, length);
        }
        return in;
    }

    /**
     * Gets the data output stream.
     *
     * @param ticket
     *            the ticket
     *
     * @return the data output stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public OutputStream getDataOutputStream(final Object ticket) throws IOException {
        final var desc = (FileDescriptor) ticket;
        final var file = desc.getFile(true);
        final var posn = desc.getPosn();
        if (posn > 0 && file.length() != posn) {
            throw new IOException("Incorrect posn for append (posn!=size)");
        }
        return file.getOutputStream(posn > 0);
    }

    /**
     * Gets the data input file.
     *
     * @param ticket
     *            the ticket
     *
     * @return the data input file
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public File getDataInputFile(final Object ticket) throws IOException {
        final var desc = (FileDescriptor) ticket;
        return desc.getFile(true).getFile();
    }

    /**
     * Gets the data output file.
     *
     * @param ticket
     *            the ticket
     *
     * @return the data output file
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public File getDataOutputFile(final Object ticket) throws IOException {
        final var desc = (FileDescriptor) ticket;
        return desc.getFile(true).getFile();
    }

    /**
     * Gets the original filename.
     *
     * @param ticket
     *            the ticket
     *
     * @return the original filename
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public String getOriginalFilename(final Object ticket) throws IOException {
        final var desc = (FileDescriptor) ticket;
        return desc.getDataFile().getOriginal();
    }
}
