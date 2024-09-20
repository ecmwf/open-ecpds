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

package ecmwf.ecpds.master.transfer;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.ectrans.ECtransGroups.Module.HOST_ECTRANS;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_LOCATION;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.ConnectException;
import java.rmi.ConnectIOException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.UnmarshalException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.script.ScriptException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.DataFile;
import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.ECpdsBase;
import ecmwf.common.database.Host;
import ecmwf.common.database.MetadataValue;
import ecmwf.common.ecaccess.ECaccessServer;
import ecmwf.common.ecaccess.StarterServer;
import ecmwf.common.ectrans.ECtransException;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.ProxySocket;
import ecmwf.common.technical.UUIDUtils;
import ecmwf.common.text.Format;
import ecmwf.common.text.Format.DuplicatedChooseScore;
import ecmwf.ecpds.mover.MoverInterface;
import ecmwf.ecpds.mover.SourceNotAvailableException;

/**
 * The Class TransferManagement.
 */
public final class TransferManagement {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(TransferManagement.class);

    /** The Constant _dataBase. */
    private static final ECpdsBase dataBase = StarterServer.getInstance(ECaccessServer.class)
            .getDataBase(ECpdsBase.class);

    /**
     * Instantiates a new transfer management.
     */
    private TransferManagement() {
        // Hiding the implicit constructor
    }

    /**
     * _process.
     *
     * @param string
     *            the string
     *
     * @return the string
     *
     * @throws DirectoryException
     *             the directory exception
     */
    private static String process(final String string) throws DirectoryException {
        final String path;
        try {
            path = Format.choose(string);
        } catch (final DuplicatedChooseScore e) {
            throw new DirectoryException("Remote directory not set (duplicated score: " + e.getScore() + ")");
        } catch (final ScriptException e) {
            throw new DirectoryException("Remote directory not set (" + e.getMessage() + ")");
        }
        if (path == null) {
            throw new DirectoryException("Remote directory not set (no default)");
        }
        return path;
    }

    /**
     * Gets the unique key for a data transfer request.
     *
     * @param standBy
     *            is it a standby transfer
     * @param destination
     *            the destination name
     * @param target
     *            the target name
     * @param uniqueName
     *            the unique name
     *
     * @return the string
     */
    public static String getUniqueKey(final boolean standBy, final String destination, final String target,
            final String uniqueName) {
        return new StringBuilder().append(standBy ? "1" : "0").append(destination).append(target)
                .append(uniqueName != null ? uniqueName : "").toString();
    }

    /**
     * Converts into string.
     *
     * @param timestamp
     *            the timestamp
     *
     * @return the string
     */
    private static String toString(final Timestamp timestamp) {
        return timestamp != null ? String.valueOf(timestamp.getTime()) : null;
    }

    /**
     * Gets the target name.
     *
     * @param transfer
     *            the transfer
     * @param template
     *            the template
     *
     * @return the target name
     *
     * @throws DirectoryException
     *             the directory exception
     * @throws DataBaseException
     *             the data base exception
     */
    public static String getTargetName(final DataTransfer transfer, final String template)
            throws DirectoryException, DataBaseException {
        return getTargetName(transfer, template, "(none)");
    }

    /**
     * Gets the target name.
     *
     * @param transfer
     *            the transfer
     * @param template
     *            the template
     * @param message
     *            the message
     *
     * @return the target name
     *
     * @throws DirectoryException
     *             the directory exception
     * @throws DataBaseException
     *             the data base exception
     */
    public static String getTargetName(final DataTransfer transfer, final String template, final String message)
            throws DirectoryException, DataBaseException {
        return getTargetName(transfer, template, message, transfer.getMoverName(), new ArrayList<>());
    }

    /**
     * Gets the target name.
     *
     * @param transfer
     *            the transfer
     * @param template
     *            the template
     * @param message
     *            the message
     * @param moverName
     *            the mover name
     * @param metadataValues
     *            the metadata values
     *
     * @return the target name
     *
     * @throws DirectoryException
     *             the directory exception
     * @throws DataBaseException
     *             the data base exception
     */
    public static String getTargetName(final DataTransfer transfer, final String template, final String message,
            final String moverName, final Collection<MetadataValue> metadataValues)
            throws DirectoryException, DataBaseException {
        if (template == null || template.trim().length() == 0) {
            return transfer.getTarget();
        }
        final var original = transfer.getOriginalTransferServer();
        final var server = transfer.getTransferServer();
        final var group = server.getTransferGroup();
        final var destination = transfer.getDestination();
        final var country = destination.getCountry();
        final var ecuser = destination.getECUser();
        final var file = transfer.getDataFile();
        final var host = transfer.getHost();
        final var method = host.getTransferMethod();
        final var module = method.getECtransModule();
        final var status = StatusFactory.getDataTransferStatusName(false, transfer.getStatusCode());
        final var target = new File(transfer.getTarget());
        final var sb = new StringBuilder(Cnf.getValue(template));
        Format.replaceAll(sb, "$moverName", moverName);
        Format.replaceAll(sb, "$system[message]", message);
        Format.replaceAll(sb, "$originalTransferServer[name]", original != null ? original.getName() : null);
        Format.replaceAll(sb, "$originalTransferServer[host]", original != null ? original.getHost() : null);
        Format.replaceAll(sb, "$originalTransferServer[port]", original != null ? original.getPort() : null);
        Format.replaceAll(sb, "$transferServer[name]", server.getName());
        Format.replaceAll(sb, "$transferServer[host]", server.getHost());
        Format.replaceAll(sb, "$transferServer[port]", server.getPort());
        Format.replaceAll(sb, "$transferGroup[name]", group.getName());
        Format.replaceAll(sb, "$transferGroup[comment]", group.getComment());
        Format.replaceAll(sb, "$destination[name]", destination.getName());
        Format.replaceAll(sb, "$destination[comment]", destination.getComment());
        Format.replaceAll(sb, "$destination[userMail]", destination.getUserMail());
        Format.replaceAll(sb, "$country[name]", country.getName());
        Format.replaceAll(sb, "$country[iso]", country.getIso());
        Format.replaceAll(sb, "$ecuser[name]", ecuser.getName());
        Format.replaceAll(sb, "$ecuser[comment]", ecuser.getComment());
        Format.replaceAll(sb, "$ecuser[dir]", ecuser.getDir());
        Format.replaceAll(sb, "$ecuser[shell]", ecuser.getShell());
        Format.replaceAll(sb, "$ecuser[gid]", ecuser.getGid());
        Format.replaceAll(sb, "$ecuser[uid]", ecuser.getUid());
        Format.replaceAll(sb, "$dataFile[timeStep]", file.getTimeStep());
        Format.replaceAll(sb, "$dataFile[arrivedTime]", toString(file.getArrivedTime()));
        Format.replaceAll(sb, "$dataFile[id]", file.getId());
        Format.replaceAll(sb, "$dataFile[original]", file.getOriginal());
        Format.replaceAll(sb, "$dataFile[source]", file.getSource());
        Format.replaceAll(sb, "$dataFile[formatSize]", Format.formatSize(file.getSize()));
        Format.replaceAll(sb, "$dataFile[size]", file.getSize());
        Format.replaceAll(sb, "$dataFile[timeBase]", toString(file.getTimeBase()));
        Format.replaceAll(sb, "$dataFile[timeFile]", toString(file.getTimeFile()));
        Format.replaceAll(sb, "$dataFile[metaTime]", file.getMetaTime());
        Format.replaceAll(sb, "$dataFile[metaStream]", file.getMetaStream());
        Format.replaceAll(sb, "$dataFile[checksum]", getChecksum(file.getChecksum()));
        Format.replaceAll(sb, "$host[name]", host.getName());
        Format.replaceAll(sb, "$host[checkFilename]", host.getCheckFilename());
        Format.replaceAll(sb, "$host[comment]", host.getComment());
        Format.replaceAll(sb, "$host[data]", host.getData());
        Format.replaceAll(sb, "$host[host]", host.getHost());
        Format.replaceAll(sb, "$host[login]", host.getLogin());
        Format.replaceAll(sb, "$host[passwd]", host.getPasswd());
        Format.replaceAll(sb, "$host[userMail]", host.getUserMail());
        Format.replaceAll(sb, "$host[networkCode]", host.getNetworkCode());
        Format.replaceAll(sb, "$host[networkName]", host.getNetworkName());
        Format.replaceAll(sb, "$host[nickname]", host.getNickname());
        Format.replaceAll(sb, "$transferMethod[name]", method.getName());
        Format.replaceAll(sb, "$transferMethod[comment]", method.getComment());
        Format.replaceAll(sb, "$transferMethod[value]", method.getValue());
        Format.replaceAll(sb, "$ectransModule[name]", module.getName());
        Format.replaceAll(sb, "$ectransModule[classe]", module.getClasse());
        Format.replaceAll(sb, "$ectransModule[archive]", module.getArchive());
        Format.replaceAll(sb, "$dataTransfer[target]", transfer.getTarget());
        Format.replaceAll(sb, "$dataTransfer[id]", transfer.getId());
        Format.replaceAll(sb, "$dataTransfer[comment]", transfer.getComment());
        Format.replaceAll(sb, "$dataTransfer[identity]", transfer.getIdentity());
        Format.replaceAll(sb, "$dataTransfer[priority]", transfer.getPriority());
        Format.replaceAll(sb, "$dataTransfer[scheduled]", toString(transfer.getScheduledTime()));
        Format.replaceAll(sb, "$dataTransfer[statusCode]", status);
        Format.replaceAll(sb, "$dataTransfer[name]", target.getName());
        Format.replaceAll(sb, "$dataTransfer[path]", target.getPath());
        Format.replaceAll(sb, "$dataTransfer[parent]", target.getParent());
        // Do we have "$metadata[...]" parameters to process?
        if (sb.indexOf("$metadata[") != -1) {
            if (metadataValues.isEmpty()) {
                metadataValues.addAll(dataBase.getMetaDataByDataFileId(file.getId()));
            }
            // Replace all the known "$metadata[...]"!
            for (final MetadataValue value : metadataValues) {
                Format.replaceAll(sb, "$metadata[" + value.getMetadataAttributeName() + "]", value.getValue());
            }
            // Remove all the "$metadata[...]" which were not found!
            removeUknownMetadata(sb, transfer, host);
        }
        // Let's see if there is a choice to make?
        final var result = process(sb.toString());
        // If the path is empty or ends with a file separator then we add the
        // transfer target name!
        return result.length() == 0 || result.endsWith("/") || result.endsWith("\\") ? result + transfer.getTarget()
                : result;
    }

    /**
     * Removes the unknown metadata. Remove all the unknown metadata tags (e.g. replace $metadata[...] with '').
     *
     * @param sb
     *            the sb
     * @param transfer
     *            the transfer
     * @param host
     *            the host
     *
     * @throws DirectoryException
     *             the directory exception
     */
    private static void removeUknownMetadata(final StringBuilder sb, final DataTransfer transfer, final Host host)
            throws DirectoryException {
        int index;
        while ((index = sb.indexOf("$metadata[")) != -1) {
            final var last = sb.indexOf("]", index + 10);
            if (last == -1) {
                throw new DirectoryException("Malformed metadata in directory: \"" + sb + "\"");
            }
            if (_log.isWarnEnabled()) {
                _log.warn("Metadata \"{}\" not found for DataTransfer-{} on Host-{}", sb.substring(index, last + 1),
                        transfer.getId(), host.getName());
            }
            sb.delete(index, last + 1);
        }
    }

    /**
     * Gets the connect options. Allow preparing the connect options for the notification and/or allocate system.
     *
     * @param transfer
     *            the transfer
     * @param targetName
     *            the target name
     * @param moverName
     *            the mover name
     * @param metadataValues
     *            the metadata values
     *
     * @return the connect options
     *
     * @throws DataBaseException
     *             the data base exception
     */
    public static String getConnectOptions(final DataTransfer transfer, final String targetName, final String moverName,
            final Collection<MetadataValue> metadataValues) throws DataBaseException {
        final var dataFile = transfer.getDataFile();
        final var timeStep = dataFile.getTimeStep();
        final var metaStream = dataFile.getMetaStream();
        final var metaTime = dataFile.getMetaTime();
        final var host = transfer.getHost();
        final var setup = HOST_ECTRANS.getECtransSetup(host != null ? host.getData() : "");
        final var transferUuid = new UUID(0, transfer.getId()).toString();
        final var fileUuid = new UUID(0, dataFile.getId()).toString();
        // This is only for forecast products with the proper fields!
        final var sb = new StringBuilder();
        append(sb, "filename", targetName); // Required to allocate a file for ECcharts (sftp module)
        append(sb, "filesize", dataFile.getSize()); // Required to allocate a file for ECcharts (sftp module)
        append(sb, "timefile", dataFile.getTimeFile().getTime()); // With filesize used to check duplicates on MQTT
        append(sb, "movername", moverName);
        append(sb, "uuid", UUIDUtils.get(dataFile.getId(), transfer.getId()));
        append(sb, "datafileid", dataFile.getId());
        append(sb, "datafileuuid", fileUuid);
        append(sb, "datatransferid", transfer.getId());
        append(sb, "datatransferuuid", transferUuid);
        append(sb, "metastream", metaStream);
        append(sb, "metatarget", dataFile.getMetaTarget());
        append(sb, "metatime", metaTime);
        append(sb, "metatype", dataFile.getMetaType());
        append(sb, "timestep", timeStep);
        append(sb, "groupby", dataFile.getGroupBy());
        append(sb, "checksum", getChecksum(dataFile.getChecksum()));
        append(sb, "etag", getETag(dataFile));
        final var expiredDate = transfer.getExpiryTime().getTime();
        append(sb, "lifetime", expiredDate - System.currentTimeMillis());
        final var productDate = dataFile.getTimeBase().getTime();
        append(sb, "productdate", productDate);
        append(sb, "date", Format.formatTime("yyyyMMdd", productDate));
        append(sb, "time", Format.formatTime("HHmm", productDate));
        append(sb, "step", timeStep != -1 ? Format.formatLong(timeStep, 3, true) : "");
        append(sb, "stream", metaStream);
        append(sb, "destination", transfer.getDestinationName());
        final var location = new StringBuilder(setup.getString(HOST_ECTRANS_LOCATION));
        Format.replaceAll(location, "$filename", targetName);
        Format.replaceAll(location, "$movername", moverName);
        Format.replaceAll(location, "$datafileid", dataFile.getId());
        Format.replaceAll(location, "$datafileuuid", fileUuid);
        Format.replaceAll(location, "$datatransferid", transfer.getId());
        Format.replaceAll(location, "$datatransferuuid", transferUuid);
        append(sb, "location", location);
        // Only provide metadata if required!
        if (metadataValues.isEmpty()) {
            metadataValues.addAll(dataBase.getMetaDataByDataFileId(dataFile.getId()));
        }
        for (final MetadataValue value : metadataValues) {
            append(sb, "metadata[" + value.getMetadataAttributeName() + "]", value.getValue());
        }
        return sb.toString();
    }

    /**
     * Append. Add a new value to the string builder in the form "key=value;".
     *
     * @param sb
     *            the sb
     * @param key
     *            the key
     * @param value
     *            the value
     */
    private static void append(final StringBuilder sb, final String key, final Object value) {
        sb.append(sb.length() > 0 ? ";" : "").append(key).append("=").append(value != null ? value : "");
    }

    /**
     * Gets the checksum.
     *
     * @param checksum
     *            the checksum
     *
     * @return the checksum
     */
    private static String getChecksum(final String checksum) {
        // Let's check if the Checksum is in the form xxxxxxxx/lbzip2=yyyyyyyy?
        int index;
        if (checksum != null && (index = checksum.indexOf("/")) != -1) {
            return checksum.substring(0, index);
        }
        return checksum;
    }

    /**
     * Gets the E tag.
     *
     * @param dataFile
     *            the data file
     *
     * @return the string
     */
    public static String getETag(final DataFile dataFile) {
        final var buffer = ByteBuffer.allocate(12);
        buffer.putLong(dataFile.getId());
        final var fileInstance = dataFile.getFileInstance();
        buffer.putInt(fileInstance != null ? fileInstance : 0);
        buffer.position(0);
        final var mostSignificantBits = buffer.getLong();
        final var leastSignificantBits = buffer.getInt();
        return new UUID(mostSignificantBits, leastSignificantBits).toString();
    }

    /**
     * Extract the dataFileId from the etag.
     *
     * @param etag
     *            the etag
     *
     * @return the string
     */
    public static long parseETag(final String etag) {
        // Parse the ETag and retrieve the DataFile identifier
        final var uuid = UUID.fromString(etag);
        final var buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        buffer.position(0);
        // To retrieve the file instance -> buffer.getInt() after the getLong()
        return buffer.getLong();
    }

    /**
     * Puts the.
     *
     * @param moverName
     *            the name of the mover
     * @param mover
     *            the mover
     * @param hostsForSource
     *            the hosts for source
     * @param transfer
     *            the transfer
     *
     * @return the data transfer
     *
     * @throws DirectoryException
     *             the directory exception
     * @throws DataBaseException
     *             the data base exception
     * @throws SourceNotAvailableException
     *             the source not available exception
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static DataTransfer put(final String moverName, final MoverInterface mover, final Host[] hostsForSource,
            final DataTransfer transfer)
            throws DirectoryException, DataBaseException, SourceNotAvailableException, ECtransException, IOException {
        final List<MetadataValue> metadataValues = new ArrayList<>();
        final var targetName = getTargetName(transfer, transfer.getHost().getDir(), "(none)", moverName,
                metadataValues);
        final var opt = new DestinationOption(transfer.getDestination());
        // The string in the host data and event script is parsed to find out if we
        // should resolve metadata or not?
        transfer.setConnectOptions(getConnectOptions(transfer, targetName, moverName, metadataValues));
        var posn = 0L;
        try {
            if (opt.ifTargetExistResume()) {
                try {
                    posn = mover.size(transfer, targetName);
                } catch (final IOException e) {
                }
                final var originalSize = transfer.getDataFile().getSize();
                if (posn > originalSize) {
                    posn = originalSize;
                }
            }
            return mover.put(hostsForSource, transfer, targetName, posn, posn);
        } catch (final RemoteException e) {
            _log.warn("Connection issue with {}", moverName, e);
            if (e.getCause() instanceof NoSuchObjectException || e.getCause() instanceof ConnectException
                    || e.getCause() instanceof ConnectIOException || e.getCause() instanceof UnmarshalException) {
                throw new ECtransException("DataMover " + moverName + " connection issue");
            }
            throw e;
        }
    }

    /**
     * Puts the.
     *
     * @param moverName
     *            the name of the mover
     * @param mover
     *            the mover
     * @param host
     *            the host
     * @param target
     *            the target
     * @param remotePosn
     *            the remote posn
     * @param size
     *            the size
     *
     * @return the proxy socket
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static ProxySocket put(final String moverName, final MoverInterface mover, final Host host,
            final String target, final long remotePosn, final long size) throws ECtransException, IOException {
        try {
            return mover.put(host, target, remotePosn, size);
        } catch (final RemoteException e) {
            _log.warn("Connection issue with {}", moverName, e);
            if (e.getCause() instanceof NoSuchObjectException || e.getCause() instanceof ConnectException
                    || e.getCause() instanceof ConnectIOException || e.getCause() instanceof UnmarshalException) {
                throw new ECtransException("DataMover " + moverName + " connection issue");
            }
            throw e;
        }
    }

    /**
     * Gets the.
     *
     * @param moverName
     *            the name of the mover
     * @param mover
     *            the mover
     * @param host
     *            the host
     * @param source
     *            the source
     * @param remotePosn
     *            the remote posn
     * @param removeOriginal
     *            the remove original
     *
     * @return the proxy socket
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static ProxySocket get(final String moverName, final MoverInterface mover, final Host host,
            final String source, final long remotePosn, final boolean removeOriginal)
            throws ECtransException, IOException {
        try {
            return mover.get(host, source, remotePosn, removeOriginal);
        } catch (final RemoteException e) {
            _log.warn("Connection issue with {}", moverName, e);
            if (e.getCause() instanceof NoSuchObjectException || e.getCause() instanceof ConnectException
                    || e.getCause() instanceof ConnectIOException || e.getCause() instanceof UnmarshalException) {
                throw new ECtransException("DataMover " + moverName + " connection issue");
            }
            throw e;
        }
    }

    /**
     * Gets the.
     *
     * @param moverName
     *            the name of the mover
     * @param mover
     *            the mover
     * @param hostsForSource
     *            the hosts for source
     * @param dataFile
     *            the data file
     * @param remotePosn
     *            the remote posn
     * @param length
     *            the length
     *
     * @return the proxy socket
     *
     * @throws SourceNotAvailableException
     *             the source not available exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static ProxySocket get(final String moverName, final MoverInterface mover, final Host[] hostsForSource,
            final DataFile dataFile, final long remotePosn, final long length)
            throws SourceNotAvailableException, IOException {
        try {
            return mover.get(dataFile, hostsForSource, remotePosn, length);
        } catch (final RemoteException e) {
            _log.warn("Connection issue with {}", moverName, e);
            if (e.getCause() instanceof NoSuchObjectException || e.getCause() instanceof ConnectException
                    || e.getCause() instanceof ConnectIOException || e.getCause() instanceof UnmarshalException) {
                throw new IOException("DataMover " + moverName + " connection issue");
            }
            throw e;
        }
    }

    /**
     * Size.
     *
     * @param moverName
     *            the name of the mover
     * @param mover
     *            the mover
     * @param host
     *            the host
     * @param source
     *            the source
     *
     * @return the long
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static long size(final String moverName, final MoverInterface mover, final Host host, final String source)
            throws ECtransException, IOException {
        try {
            return mover.size(host, source);
        } catch (final RemoteException e) {
            _log.warn("Connection issue with {}", moverName, e);
            if (e.getCause() instanceof NoSuchObjectException || e.getCause() instanceof ConnectException
                    || e.getCause() instanceof ConnectIOException || e.getCause() instanceof UnmarshalException) {
                throw new ECtransException("DataMover " + moverName + " connection issue");
            }
            throw e;
        }
    }

    /**
     * List.
     *
     * @param moverName
     *            the name of the mover
     * @param mover
     *            the mover
     * @param host
     *            the host
     * @param source
     *            the source
     * @param pattern
     *            the pattern
     *
     * @return the string[]
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static String[] list(final String moverName, final MoverInterface mover, final Host host,
            final String source, final String pattern) throws ECtransException, IOException {
        try {
            return mover.listAsStringArray(host, source, pattern);
        } catch (final RemoteException e) {
            _log.warn("Connection issue with {}", moverName, e);
            if (e.getCause() instanceof NoSuchObjectException || e.getCause() instanceof ConnectException
                    || e.getCause() instanceof ConnectIOException || e.getCause() instanceof UnmarshalException) {
                throw new ECtransException("DataMover " + moverName + " connection issue");
            }
            throw e;
        }
    }

    /**
     * Del.
     *
     * @param moverName
     *            the name of the mover
     * @param mover
     *            the mover
     * @param host
     *            the host
     * @param source
     *            the source
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void del(final String moverName, final MoverInterface mover, final Host host, final String source)
            throws ECtransException, IOException {
        try {
            mover.del(host, source);
        } catch (final RemoteException e) {
            _log.warn("Connection issue with {}", moverName, e);
            if (e.getCause() instanceof NoSuchObjectException || e.getCause() instanceof ConnectException
                    || e.getCause() instanceof ConnectIOException || e.getCause() instanceof UnmarshalException) {
                throw new ECtransException("DataMover " + moverName + " connection issue");
            }
            throw e;
        }
    }

    /**
     * Mkdir.
     *
     * @param moverName
     *            the name of the mover
     * @param mover
     *            the mover
     * @param host
     *            the host
     * @param dir
     *            the dir
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void mkdir(final String moverName, final MoverInterface mover, final Host host, final String dir)
            throws ECtransException, IOException {
        try {
            mover.mkdir(host, dir);
        } catch (final RemoteException e) {
            _log.warn("Connection issue with {}", moverName, e);
            if (e.getCause() instanceof NoSuchObjectException || e.getCause() instanceof ConnectException
                    || e.getCause() instanceof ConnectIOException || e.getCause() instanceof UnmarshalException) {
                throw new ECtransException("DataMover " + moverName + " connection issue");
            }
            throw e;
        }
    }

    /**
     * Rmdir.
     *
     * @param moverName
     *            the name of the mover
     * @param mover
     *            the mover
     * @param host
     *            the host
     * @param dir
     *            the dir
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void rmdir(final String moverName, final MoverInterface mover, final Host host, final String dir)
            throws ECtransException, IOException {
        try {
            mover.rmdir(host, dir);
        } catch (final RemoteException e) {
            _log.warn("Connection issue with {}", moverName, e);
            if (e.getCause() instanceof NoSuchObjectException || e.getCause() instanceof ConnectException
                    || e.getCause() instanceof ConnectIOException || e.getCause() instanceof UnmarshalException) {
                throw new ECtransException("DataMover " + moverName + " connection issue");
            }
            throw e;
        }
    }

    /**
     * Move.
     *
     * @param moverName
     *            the name of the mover
     * @param mover
     *            the mover
     * @param host
     *            the host
     * @param source
     *            the source
     * @param target
     *            the target
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void move(final String moverName, final MoverInterface mover, final Host host, final String source,
            final String target) throws ECtransException, IOException {
        try {
            mover.move(host, source, target);
        } catch (final RemoteException e) {
            _log.warn("Connection issue with {}", moverName, e);
            if (e.getCause() instanceof NoSuchObjectException || e.getCause() instanceof ConnectException
                    || e.getCause() instanceof ConnectIOException || e.getCause() instanceof UnmarshalException) {
                throw new ECtransException("DataMover " + moverName + " connection issue");
            }
            throw e;
        }
    }

    /**
     * Check.
     *
     * @param moverName
     *            the name of the mover
     * @param mover
     *            the mover
     * @param ticket
     *            the ticket
     *
     * @throws ECtransException
     *             the ectrans exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void check(final String moverName, final MoverInterface mover, final long ticket)
            throws ECtransException, IOException {
        try {
            mover.check(ticket);
        } catch (final RemoteException e) {
            _log.warn("Connection issue with {}", moverName, e);
            if (e.getCause() instanceof NoSuchObjectException || e.getCause() instanceof ConnectException
                    || e.getCause() instanceof ConnectIOException || e.getCause() instanceof UnmarshalException) {
                throw new ECtransException("DataMover " + moverName + " connection issue");
            }
            throw e;
        }
    }
}
