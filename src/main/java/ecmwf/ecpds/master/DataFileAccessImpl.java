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

import static ecmwf.common.ectrans.ECtransGroups.Module.DESTINATION_INCOMING;
import static ecmwf.common.ectrans.ECtransGroups.Module.DESTINATION_SCHEDULER;
import static ecmwf.common.ectrans.ECtransGroups.Module.USER_PORTAL;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_INCOMING_MAX_BYTES_PER_SEC_FOR_INPUT;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_INCOMING_MAX_BYTES_PER_SEC_FOR_OUTPUT;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_INCOMING_STANDBY;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_INCOMING_TMP;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_SCHEDULER_STANDBY;
import static ecmwf.common.ectrans.ECtransOptions.USER_PORTAL_RECORD_HISTORY;
import static ecmwf.common.ectrans.ECtransOptions.USER_PORTAL_RECORD_SPLUNK;

import static ecmwf.common.text.Util.nullToNone;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.callback.CallBackObject;
import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.Destination;
import ecmwf.common.database.ECpdsBase;
import ecmwf.common.database.IncomingHistory;
import ecmwf.common.database.IncomingUser;
import ecmwf.common.ecaccess.FileListElement;
import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.rmi.SocketConfig;
import ecmwf.common.technical.ProxyEvent;
import ecmwf.common.technical.ProxySocket;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.plugin.ecpds.ECpdsClient;
import ecmwf.ecpds.master.transfer.AliasesParser;
import ecmwf.ecpds.master.transfer.DestinationOption;
import ecmwf.ecpds.master.transfer.StatusFactory;
import ecmwf.ecpds.master.transfer.TransferScheduler;

/**
 * The Class DataFileAccessImpl.
 */
final class DataFileAccessImpl extends CallBackObject implements DataAccessInterface {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 2481520751862793055L;

    /** The Constant _log. */
    private static final transient Logger _log = LogManager.getLogger(DataFileAccessImpl.class);

    /** The Constant _splunk. */
    private static final Logger _splunk = LogManager.getLogger("SplunkLogs");

    /** The Constant ECPDS_MKDIR_FILE. */
    private static final transient String ECPDS_MKDIR_FILE = "/.ecpds_mkdir";

    /** The master. */
    private final transient MasterServer master;

    /**
     * Instantiates a new data file access impl.
     *
     * @param master
     *            the master
     *
     * @throws RemoteException
     *             the remote exception
     */
    DataFileAccessImpl(final MasterServer master) throws RemoteException {
        this.master = master;
    }

    /**
     * Make sure the file is available and that we are allowed to display it: is the scheduled time over or is it asap?.
     *
     * @param currentTransfer
     *            the current transfer
     *
     * @return true, if successful
     */
    private static boolean isAvailable(final DataTransfer currentTransfer) {
        final var file = currentTransfer.getDataFile();
        return (currentTransfer.getAsap() || currentTransfer.getScheduledTime().before(new Date()))
                && file.getDownloaded() && !currentTransfer.getDeleted() && !file.getDeleted()
                && !StatusFactory.INIT.equals(currentTransfer.getStatusCode());
    }

    /**
     * Gets the Destination object.
     *
     * @param destinationName
     *            the destination name
     *
     * @return destination, if successful
     *
     * @throws MasterException
     *             the master exception
     */
    private Destination getDestination(final String destinationName) throws MasterException {
        final var destination = master.getDestination(destinationName);
        if (destination == null) {
            throw new MasterException("Destination + " + destinationName + " does not exists");
        }
        return destination;
    }

    /**
     * Gets the data transfers.
     *
     * @param destinationName
     *            the destination name
     * @param target
     *            the target
     * @param specifiedTime
     *            the specified time
     * @param sort
     *            the sort
     * @param order
     *            the order
     *
     * @return the collection
     *
     * @throws MasterException
     *             the master exception
     */
    private Iterator<DataTransfer> getDataTransfers(final String destinationName, final String target,
            final long specifiedTime, final int sort, final int order) throws MasterException {
        final var from = new Date(specifiedTime);
        final var to = new Date(specifiedTime + Timer.ONE_DAY);
        final var base = master.getDataBase(ECpdsBase.class);
        try {
            return base.getDataTransfersByDestinationAndTargetOnDateIterator2(destinationName, target, from, to, sort,
                    order);
        } catch (IOException | SQLException e) {
            _log.warn(e);
            throw new MasterException("DataBase error");
        }
    }

    /**
     * Gets the data transfers.
     *
     * @param destinationName
     *            the destination name
     * @param target
     *            the target
     * @param sort
     *            the sort
     * @param order
     *            the order
     *
     * @return the iterator
     *
     * @throws MasterException
     *             the master exception
     */
    private Iterator<DataTransfer> getDataTransfers(final String destinationName, final String target, final int sort,
            final int order) throws MasterException {
        final var base = master.getDataBase(ECpdsBase.class);
        try {
            return base.getDataTransfersByDestinationAndTargetIterator2(destinationName,
                    "%".equals(target) ? "" : target, true, sort, order);
        } catch (IOException | SQLException e) {
            _log.warn(e);
            throw new MasterException("DataBase error");
        }
    }

    /**
     * Gets the data transfer.
     *
     * @param destination
     *            the destination
     * @param source
     *            the source
     *
     * @return the data transfer
     *
     * @throws MasterException
     *             the master exception
     */
    private DataTransfer getDataTransfer(final Destination destination, final String source) throws MasterException {
        if (source.startsWith("DataTransferId=")) {
            // The DataTransfer id is provided directly for better performance!
            return getDataTransfer(source.substring(15));
        }
        final var tokenizer = new StringTokenizer(source, "/");
        if (destination.getGroupByDate()) {
            // The files are grouped in directories named by date!
            if (tokenizer.countTokens() == 2) {
                // The first token is supposed to be the date!
                final var date = tokenizer.nextToken();
                // Is it a valid date (check against the date format)?
                final var specifiedTime = DestinationOption.parseDate(destination, date);
                if (specifiedTime == -1) {
                    // The date was not recognized!
                    throw new MasterException("Invalid date: " + date);
                }
                // The next token is supposed to be the name of the data
                // file!
                final var transfers = getDataTransfers(destination.getName(), tokenizer.nextToken(), specifiedTime, 3,
                        2); // sort=DAT_SCHEDULED_TIME & order=DESC
                try {
                    if (transfers.hasNext()) {
                        try {
                            return master.getDataBase(ECpdsBase.class).getDataTransfer(transfers.next().getId());
                        } catch (final DataBaseException e) {
                            _log.warn(e);
                            throw new MasterException("DataBase error");
                        }
                    }
                } finally {
                    transfers.remove();
                }
                // File not found!
            } else if (tokenizer.countTokens() == 1) {
                // This is supposed to be the DataTransfer id!
                return getDataTransfer(source);
            } else {
                // This is not a valid path!
                throw new MasterException("Invalid path: " + source);
            }
        } else // The files are grouped by names and sub-directories as defined in
        // the path of the source!
        if (!source.endsWith("/")) {
            final var transfers = getDataTransfers(destination.getName(), source, -1, -1);
            try {
                if (transfers.hasNext()) {
                    try {
                        return master.getDataBase(ECpdsBase.class).getDataTransfer(transfers.next().getId());
                    } catch (final DataBaseException e) {
                        _log.warn(e);
                        throw new MasterException("DataBase error");
                    }
                }
            } finally {
                transfers.remove();
            }
        }
        // We couldn't find the file!
        throw new MasterException("File not found: " + source);
    }

    /**
     * Gets the data transfer.
     *
     * @param dataTransferId
     *            the data transfer id
     *
     * @return the data transfer
     *
     * @throws MasterException
     *             the master exception
     */
    private DataTransfer getDataTransfer(final String dataTransferId) throws MasterException {
        final long id;
        try {
            id = Long.parseLong(dataTransferId);
        } catch (final NumberFormatException e) {
            throw new MasterException("Invalid id supplied for DataTransfer: " + dataTransferId);
        }
        final var transfer = master.getDataTransfer(id);
        if (transfer == null) {
            throw new MasterException("DataTransfer not found: " + id);
        }
        if (!isAvailable(transfer)) {
            throw new MasterException("DataTransfer not available: " + id);
        }
        return transfer;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the file last modified.
     */
    @Override
    public long getFileLastModified(final String destinationName, final String source)
            throws MasterException, IOException {
        final var monitor = new MonitorCall("getFileLastModified(" + destinationName + "," + source + ")");
        return monitor.done(
                getDataTransfer(getDestination(destinationName), source).getDataFile().getArrivedTime().getTime());
    }

    /**
     * {@inheritDoc}
     *
     * Size.
     */
    @Override
    public long size(final String destinationName, final String source) throws MasterException, IOException {
        final var monitor = new MonitorCall("size(" + destinationName + "," + source + ")");
        return monitor.done(getDataTransfer(getDestination(destinationName), source).getSize());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the proxy socket input.
     */
    @Override
    public ProxySocket getProxySocketInput(final String destinationName, final String source, final long offset)
            throws MasterException, IOException {
        return getProxySocketInput(destinationName, source, offset, -1);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the proxy socket input.
     */
    @Override
    public ProxySocket getProxySocketInput(final String destinationName, final String source, final long offset,
            final long length) throws MasterException, IOException {
        final var monitor = new MonitorCall(
                "getProxySocketInput(" + destinationName + "," + source + "," + offset + "," + length + ")");
        final var destination = getDestination(destinationName);
        final var transfer = getDataTransfer(destination, source);
        if (transfer == null) {
            throw new MasterException("DataTransfer not found");
        }
        try {
            final var proxy = TransferScheduler.get(transfer, offset, length);
            final var setup = DESTINATION_INCOMING.getECtransSetup(destination.getData());
            proxy.addObject(DataTransfer.class.getName(), transfer);
            setup.getOptionalByteSize(DESTINATION_INCOMING_MAX_BYTES_PER_SEC_FOR_INPUT)
                    .ifPresent(maxBytesPerSec -> proxy.setMaxBytesPerSec(maxBytesPerSec.size()));
            return monitor.done(proxy);
        } catch (final DataBaseException e) {
            _log.warn(source, e);
            throw new MasterException("DataBase error");
        } catch (final MasterException e) {
            _log.warn(source, e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the proxy socket output.
     */
    @Override
    public ProxySocket getProxySocketOutput(final String destinationName, String target, final long offset,
            final int umask) throws MasterException, IOException {
        final var monitor = new MonitorCall(
                "getProxySocketOutput(" + destinationName + "," + target + "," + offset + "," + umask + ")");
        if (offset != 0) {
            throw new MasterException("Offset not supported");
        }
        final var destination = getDestination(destinationName);
        final var tokenizer = new StringTokenizer(target, "/");
        final var timeFile = System.currentTimeMillis();
        long timeBase = -1;
        if (destination.getGroupByDate()) {
            // The files are grouped in directories named by date!
            if (tokenizer.countTokens() == 1) {
                // We are in the root directory of the Destination so we take
                // the current date!
                target = tokenizer.nextToken();
                timeBase = timeFile;
            } else if (tokenizer.countTokens() == 2) {
                // We are in a date directory so let's scan the date from
                // the directory name!
                final var date = tokenizer.nextToken();
                target = tokenizer.nextToken();
                timeBase = DestinationOption.parseDate(destination, date);
            } else {
                // Where are we :-)
                throw new MasterException("Permission denied");
            }
        } else {
            // The files are grouped by names and sub-directories as defined in
            // the path of the source!
            timeBase = timeFile;
        }
        if (timeBase == -1) {
            throw new MasterException("Invalid date specified");
        }
        if (_log.isDebugEnabled()) {
            _log.debug("Time: {}", DestinationOption.formatDate(destination, timeBase));
        }
        final var ticket = master.getTicketRepository()
                .add(new MoverAccessTicket(destination, target, timeFile, timeBase));
        final var socketConfig = new SocketConfig("ECpdsPlugin");
        final var proxy = new ProxySocket(ticket.getId(), socketConfig.getPublicAddress(), socketConfig.getPort(),
                true);
        final var setup = DESTINATION_INCOMING.getECtransSetup(destination.getData());
        proxy.addObject(MoverAccessTicket.class.getName(), Boolean.TRUE);
        setup.getOptionalByteSize(DESTINATION_INCOMING_MAX_BYTES_PER_SEC_FOR_OUTPUT)
                .ifPresent(maxBytesPerSec -> proxy.setMaxBytesPerSec(maxBytesPerSec.size()));
        return monitor.done(proxy);
    }

    /**
     * {@inheritDoc}
     *
     * Delete.
     */
    @Override
    public void delete(final String destinationName, final String source, final boolean force)
            throws MasterException, IOException {
        final var monitor = new MonitorCall("delete(" + destinationName + "," + source + "," + force + ")");
        try {
            master.removeDataFileAndDataTransfers(
                    getDataTransfer(getDestination(destinationName), source).getDataFile(), null,
                    "from the incoming connection");
        } catch (final DataBaseException e) {
            throw new MasterException("DataBase error");
        }
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Mkdir.
     */
    @Override
    public void mkdir(final String destinationName, final String path) throws MasterException, IOException {
        final var monitor = new MonitorCall("mkdir(" + destinationName + "," + path + ")");
        final var destination = getDestination(destinationName);
        // If it is group by date then we don't allow the creation of directories!
        if (destination.getGroupByDate()) {
            throw new MasterException("Permission denied");
        }
        // Let's make sure the directory does not already exists or there is no existing
        // file with this name?
        if (list(destinationName, path).length > 0) {
            throw new MasterException("Create directory operation failed");
        }
        _log.debug("Creating directory in Destination {}: {}", destinationName, path);
        // Let's create a mkdir file to register this new directory!
        final var currentTime = System.currentTimeMillis();
        ECpdsClient.put("From data portal", destination, path + ECPDS_MKDIR_FILE,
                new ByteArrayInputStream(("Directory created on: " + Format.formatTime(currentTime) + "\n").getBytes()),
                currentTime, currentTime);
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Rmdir.
     */
    @Override
    public void rmdir(final String destinationName, final String path) throws MasterException, IOException {
        final var monitor = new MonitorCall("rmdir(" + destinationName + "," + path + ")");
        final var destination = getDestination(destinationName);
        // If it is group by date then we don't allow the deletion of directories to be
        // consistent with the directory creation!
        if (destination.getGroupByDate()) {
            throw new MasterException("Permission denied");
        }
        _log.debug("Deleting directory in Destination {}: {}", destinationName, path);
        // Let's remove the files/directories recursively from this directory!
        for (final FileListElement fle : list(destinationName, path)) {
            final var fullName = path + "/" + fle.getName();
            if (fle.isDirectory()) {
                rmdir(destinationName, fullName);
            } else {
                delete(destinationName, fullName, true);
            }
        }
        monitor.done();
    }

    /**
     * Gets the file list element.
     *
     * @param destination
     *            the destination
     * @param transfer
     *            the transfer
     * @param target
     *            the target
     *
     * @return the file list element
     */
    private static FileListElement getFileListElement(final Destination destination, final DataTransfer transfer,
            final String target) {
        final var element = new FileListElement();
        element.setComment(String.valueOf(transfer.getId()));
        element.setGroup(destination.getCountryIso());
        element.setName(target);
        final var time = transfer.getQueueTime();
        element.setTime((time != null ? time : transfer.getScheduledTime()).getTime());
        element.setSize(String.valueOf(transfer.getSize()));
        element.setUser(destination.getECUserName());
        element.setRight("-rw-r--r--");
        return element;
    }

    /**
     * {@inheritDoc}
     *
     * List.
     */
    @Override
    public FileListElement[] list(final String destinationName, final String path) throws MasterException, IOException {
        return list(destinationName, path, -1, -1);
    }

    /**
     * {@inheritDoc}
     *
     * List.
     */
    @Override
    public FileListElement[] list(final String destinationName, String path, final int sort, final int order)
            throws MasterException, IOException {
        final var monitor = new MonitorCall("list(" + destinationName + "," + path + ")");
        final var destination = getDestination(destinationName);
        final List<FileListElement> elements = new ArrayList<>();
        final List<String> keys = new ArrayList<>();
        final var base = master.getDataBase(ECpdsBase.class);
        if (destination.getGroupByDate()) {
            // The files are grouped in directories named by date!
            if (path == null || path.length() == 0) {
                // This is the root directory of the Destination. Let's list the
                // dates with data available for download!
                final Long[] dates;
                try {
                    dates = base.getDatesByDestinationAndTargetOnDate(destinationName, order);
                } catch (final DataBaseException e) {
                    _log.warn(e);
                    throw new MasterException("DataBase error");
                }
                for (final Long date : dates) {
                    final var element = new FileListElement();
                    element.setComment(destination.getComment());
                    element.setGroup(destination.getCountryIso());
                    element.setName(DestinationOption.formatDate(destination, date));
                    element.setPath(element.getName());
                    element.setTime(date);
                    element.setUser(destination.getECUserName());
                    element.setRight("drwxr-x---");
                    element.setSize("2048");
                    elements.add(element);
                }
            } else {
                // This is a sub-directory of the Destination so let's get the
                // date!
                final var tokenizer = new StringTokenizer(path, "/");
                final var date = tokenizer.nextToken();
                final var specifiedTime = DestinationOption.parseDate(destination, date);
                if (specifiedTime == -1) {
                    throw new MasterException("Invalid date: " + date);
                }
                // We have at least one directory specified so let's check if
                // there are wild-cards or not?
                var target = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;
                if (target != null) {
                    // Converting Unix wild-cards into SQL wild-cards!
                    target = Format.unix2sqlWildcards(target);
                }
                final var transfers = getDataTransfers(destinationName, target, specifiedTime, sort, order);
                try {
                    while (transfers.hasNext()) {
                        final var currentTransfer = transfers.next();
                        final var name = new File(currentTransfer.getTarget()).getName();
                        if (!keys.contains(name)) {
                            final var element = getFileListElement(destination, currentTransfer, name);
                            element.setPath(date + "/" + name);
                            elements.add(element);
                            keys.add(name);
                        }
                    }
                } finally {
                    transfers.remove();
                }
            }
        } else {
            // The files are grouped by names and sub-directories as defined in
            // the path of the source!
            if (path == null) {
                path = "";
            }
            final var level = new StringTokenizer(path, "/").countTokens();
            final var transfers = getDataTransfers(destinationName, Format.unix2sqlWildcards(path) + "%", sort, order);
            try {
                while (transfers.hasNext()) {
                    final var currentTransfer = transfers.next();
                    var target = Format.normalizePath(currentTransfer.getTarget());
                    if (target.startsWith("/")) {
                        target = target.substring(1);
                    }
                    final var token = new StringTokenizer(target, "/");
                    if (token.countTokens() > level) {
                        // Let's remove the unused path!
                        for (var i = 0; i < level; i++) {
                            token.nextToken();
                        }
                        // Now we have the relative name!
                        var relative = token.nextToken("\0");
                        if (relative.startsWith("/")) {
                            relative = relative.substring(1);
                        }
                        final var index = relative.indexOf("/");
                        if (index == -1) {
                            // This file is in the current directory so let's
                            // add it to the list!
                            final var length = path.length();
                            if (length == 0 || length > 0 && target.startsWith(path + "/")) {
                                final var name = new File(relative).getName();
                                if (!keys.contains(name)) {
                                    final var element = getFileListElement(destination, currentTransfer, name);
                                    element.setPath(target);
                                    elements.add(element);
                                    keys.add(name);
                                }
                            }
                        } else {
                            // This file is in a sub-directory so let's add a
                            // new directory!
                            final var name = relative.substring(0, index);
                            if (!keys.contains(name)) {
                                // This directory does not exists yet!
                                final var element = new FileListElement();
                                element.setComment(destination.getComment());
                                element.setGroup(destination.getCountryIso());
                                element.setName(name);
                                element.setPath(name);
                                element.setTime(currentTransfer.getScheduledTime().getTime());
                                element.setUser(destination.getECUserName());
                                element.setRight("drwxr-x---");
                                element.setSize("2048");
                                elements.add(element);
                                keys.add(name);
                            }
                        }
                    } else if (Format.matches(path, target)) {
                        // This file is in the current directory so let's
                        // add it to the list!
                        final var name = new File(target).getName();
                        if (!keys.contains(name)) {
                            final var element = getFileListElement(destination, currentTransfer, name);
                            element.setPath(target);
                            elements.add(element);
                            keys.add(name);
                        }
                    }
                }
            } finally {
                transfers.remove();
            }
        }
        final var size = elements.size();
        _log.debug("List({}|{}|{})", destinationName, path, size);
        return monitor.done(elements.toArray(new FileListElement[size]));
    }

    /**
     * Find a filename. The files are grouped by names and sub-directories as defined in the path of the source (no
     * groupByDates).
     *
     * @param destination
     *            the destination
     * @param path
     *            the path
     * @param filename
     *            the filename
     *
     * @return the file list element
     *
     * @throws MasterException
     *             the master exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private FileListElement find(final Destination destination, final String path, final String filename)
            throws MasterException, IOException {
        final var currentTime = System.currentTimeMillis();
        final var currentPath = path == null ? "" : path;
        final var level = new StringTokenizer(currentPath, "/").countTokens();
        final var transfers = getDataTransfers(destination.getName(), Format.unix2sqlWildcards(currentPath) + "%", -1,
                -1);
        try {
            while (transfers.hasNext()) {
                final var currentTransfer = transfers.next();
                var target = Format.normalizePath(currentTransfer.getTarget());
                if (target.startsWith("/")) {
                    target = target.substring(1);
                }
                final var token = new StringTokenizer(target, "/");
                if (token.countTokens() > level) {
                    // Let's remove the unused path!
                    for (var i = 0; i < level; i++) {
                        token.nextToken();
                    }
                    // Now we have the relative name!
                    var relative = token.nextToken("\0");
                    if (relative.startsWith("/")) {
                        relative = relative.substring(1);
                    }
                    final var index = relative.indexOf("/");
                    if (index == -1) {
                        // This file is in the current directory!
                        final var length = currentPath.length();
                        if (length == 0 || length > 0 && target.startsWith(currentPath + "/")) {
                            final var name = new File(relative).getName();
                            if (filename.equals(name)) {
                                return getFileListElement(destination, currentTransfer, name);
                            }
                        }
                    } else {
                        // This file is in a sub-directory!
                        final var name = relative.substring(0, index);
                        if (filename.equals(name)) {
                            final var element = new FileListElement();
                            element.setComment(destination.getComment());
                            element.setGroup(destination.getCountryIso());
                            element.setName(name);
                            element.setTime(currentTime);
                            element.setUser(destination.getECUserName());
                            element.setRight("drwxr-x---");
                            element.setSize("2048");
                            return element;
                        }
                    }
                } else if (Format.matches(currentPath, target)) {
                    // This file is in the current directory!
                    final var name = new File(target).getName();
                    if (filename.equals(name)) {
                        return getFileListElement(destination, currentTransfer, name);
                    }
                }
            }
        } finally {
            transfers.remove();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the.
     */
    @SuppressWarnings("null")
    @Override
    public FileListElement get(final String destinationName, final String path) throws MasterException, IOException {
        final var monitor = new MonitorCall("get(" + destinationName + "," + path + ")");
        final var destination = getDestination(destinationName);
        final var size = path != null ? path.length() : 0;
        // The DataTransfer id is provided directly for better performance!
        if (size > 0 && path.startsWith("DataTransferId=")) {
            final var transfer = getDataTransfer(path.substring(15));
            return getFileListElement(destination, transfer, new File(transfer.getTarget()).getName());
        }
        final var currentTime = System.currentTimeMillis();
        FileListElement element = null;
        if (destination.getGroupByDate()) {
            // The files are grouped in directories named by date!
            if (size == 0) {
                // This is the root directory of the Destination!
                element = new FileListElement();
                element.setComment(destination.getComment());
                element.setGroup(destination.getCountryIso());
                element.setName(destination.getName());
                element.setTime(currentTime);
                element.setUser(destination.getECUserName());
                element.setRight("drwxr-x---");
                element.setSize("2048");
            } else {
                // This is a sub-directory of the Destination so let's get the
                // date!
                final var tokenizer = new StringTokenizer(path, "/");
                final var date = tokenizer.nextToken();
                final var specifiedTime = DestinationOption.parseDate(destination, date);
                if (specifiedTime == -1) {
                    // It's not a date so it should be a filename and it does
                    // not exists as the files in the destination directory are
                    // automatically sent to the current date directory!
                    if (tokenizer.hasMoreTokens()) {
                        // We don't allow sub-directories!
                        throw new MasterException("Permission denied");
                    }
                } else {
                    // Do we have a file name specified as well?
                    final var target = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;
                    if (target == null) {
                        // This is a date directory (no file name specified)!
                        element = new FileListElement();
                        element.setComment(destination.getComment());
                        element.setGroup(destination.getCountryIso());
                        element.setName(date);
                        element.setTime(currentTime);
                        element.setUser(destination.getECUserName());
                        element.setRight("drwxr-x---");
                        element.setSize("2048");
                    } else {
                        // This is a data transfer!
                        try {
                            element = getFileListElement(destination, getDataTransfer(destination, path),
                                    new File(target).getName());
                        } catch (final MasterException e) {
                            // File not found!
                        }
                    }
                }
            }
        } else {
            // We have to list all the files! (could be improved)
            final var file = new File(path);
            element = find(destination, file.getParent(), file.getName());
        }
        return monitor.done(element);
    }

    /**
     * {@inheritDoc}
     *
     * Move.
     */
    @Override
    public void move(final String destinationName, final String source, final String target)
            throws MasterException, IOException {
        final var monitor = new MonitorCall("move(" + destinationName + "," + source + "," + target + ")");
        final var destination = getDestination(destinationName);
        try {
            final var file = getDataTransfer(destination, source).getDataFile();
            final var original = file.getOriginal();
            final var setup = DESTINATION_INCOMING.getECtransSetup(destination.getData());
            // We check if it is a rename from a temporary name to a definitive
            // name?
            final var tmpToDefinitive = setup.matches(DESTINATION_INCOMING_TMP, original)
                    && !setup.matches(DESTINATION_INCOMING_TMP, target);
            final var aliases = new AliasesParser(destination, target, null, 0, -1, false, false);
            final var dataBase = master.getDataBase();
            for (final DataTransfer transfer : master.getDataTransfers(file.getId())) {
                final var currentDestination = transfer.getDestination();
                final var currentDestinationName = currentDestination.getName();
                final var options = aliases.getAliasOptions(currentDestinationName);
                if (options == null) {
                    // It might be that the Aliasing configuration has
                    // changed since the initial submission of the DataFile?
                    _log.warn("No options for Destination {} (leave DataTransfer {} unchanged)", currentDestinationName,
                            transfer.getId());
                    continue;
                }
                // We have to deal with the target name!
                final var oldTarget = transfer.getTarget();
                var newTarget = options.getTarget();
                if (destination.getGroupByDate()) {
                    // We group by date so we remove the directory name from the
                    // target name!
                    final var tokenizer = new StringTokenizer(newTarget, "/");
                    final var date = tokenizer.nextToken();
                    final var specifiedTime = DestinationOption.parseDate(destination, date);
                    if (specifiedTime == -1) {
                        throw new MasterException("Invalid date: " + date);
                    }
                    final var transferDate = DestinationOption.formatDate(destination,
                            transfer.getTimeBase().getTime());
                    if (!date.equals(transferDate)) {
                        // We cannot change the date of the product!
                        throw new MasterException("Cannot change product date to " + date);
                    }
                    // Let's set the new target name!
                    if (tokenizer.hasMoreTokens()) {
                        newTarget = tokenizer.nextToken();
                        if (tokenizer.hasMoreTokens()) {
                            // We don't do with sub-directories!
                            throw new MasterException("Sub-directory not allowed here");
                        }
                    } else {
                        // We couldn't find a target name!
                        newTarget = null;
                    }
                }
                if (newTarget == null || newTarget.length() == 0) {
                    // We should have a proper target!
                    throw new MasterException("No target name specified");
                }
                transfer.setTarget(newTarget);
                final var data = currentDestination.getData();
                // What else to do?
                if (tmpToDefinitive
                        && !DESTINATION_INCOMING.getECtransSetup(data).getBoolean(DESTINATION_INCOMING_STANDBY)
                        && !DESTINATION_SCHEDULER.getECtransSetup(data).getBoolean(DESTINATION_SCHEDULER_STANDBY)
                        && StatusFactory.HOLD.equals(transfer.getStatusCode())) {
                    // The file was a temporary file and the new name is not a
                    // temporary file so we have to process the Aliases and
                    // queue it!
                    final var status = StatusFactory.WAIT;
                    transfer.setStatusCode(status);
                    transfer.setComment(
                            "Renamed from " + oldTarget + " to " + newTarget + " and scheduled for no sooner than "
                                    + Format.formatTime("MMM dd HH:mm:ss", transfer.getScheduledTime().getTime()));
                    _log.debug("DataTransfer {} renamed from {} to {} and queued", transfer.getId(), oldTarget,
                            newTarget);
                } else {
                    // It was not a temporary file or the new name is still a
                    // temporary file or the DataTransfer is not in Standby so
                    // there is nothing to do!
                    transfer.setComment("Renamed from " + oldTarget + " to " + newTarget);
                    _log.debug("DataTransfer {} renamed from {} to {}", transfer.getId(), oldTarget, newTarget);
                }
                dataBase.update(transfer);
                master.addTransferHistory(transfer);
                final var currentStatus = transfer.getStatusCode();
                // If the file is now queued then we might want to reload the
                // Destination to flush the cache!
                if (StatusFactory.WAIT.equals(currentStatus) || StatusFactory.RETR.equals(currentStatus)) {
                    master.reloadDestination(transfer);
                }
            }
        } catch (final DataBaseException e) {
            throw new MasterException("DataBase error");
        }
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Check.
     */
    @Override
    public void check(final ProxySocket proxy) throws MasterException, IOException {
        final var monitor = new MonitorCall(
                "check(" + proxy.getDataHost() + ":" + proxy.getDataPort() + "->" + proxy.getTicket() + ")");
        final List<DataTransfer> transfers = new ArrayList<>();
        var success = false;
        try {
            if (proxy.getObject(MoverAccessTicket.class.getName()) != null) {
                // There is a MoverAccessTicket ticket so the operation is an
                // upload operation and we have to wait for the ECpdsClient to
                // complete!
                final var repository = master.getTicketRepository();
                final var moverTicket = (MoverAccessTicket) repository.get(proxy.getTicket());
                repository.check(proxy.getTicket(), true);
                try {
                    // Let's try to find the DataTransfer(s) from the
                    // MoverTicket!
                    transfers.addAll(master.getECpdsBase().getDataTransfersByDataFileId(master.dataCache,
                            moverTicket.getDataFileId(), false));
                } catch (final Throwable t) {
                    _log.warn(t);
                    // We forget about the history!
                }
            } else {
                // This is a download operation so we have to check on the
                // remote data mover!
                TransferScheduler.check(proxy);
                // Now we get the DataTransfer for the history!
                final var transfer = (DataTransfer) proxy.getObject(DataTransfer.class.getName());
                if (transfer != null) {
                    transfers.add(transfer);
                }
            }
            success = true;
        } finally {
            final var event = (ProxyEvent) proxy.getObject(ProxyEvent.class.getName());
            if (event != null) {
                final var base = master.getECpdsBase();
                final IncomingUser incomingUser;
                final ECtransSetup setup;
                if (event.getUserType() == ProxyEvent.UserType.DATA_USER) {
                    incomingUser = base.getIncomingUserObject(event.getUserName());
                    setup = incomingUser != null
                            ? USER_PORTAL.getECtransSetup(base.getDataFromUserPolicies(incomingUser)) : null;
                } else {
                    incomingUser = null;
                    setup = null;
                }
                if (incomingUser != null) {
                    final var transferRate = Format.getMBitsPerSeconds(event.getSent(), event.getDuration());
                    final var mover = master.getDataBase(ECpdsBase.class).getTransferServerObject(event.getLocalHost());
                    final var message = event.getSent() + " byte(s) " + (event.getUpload() ? "uploaded" : "downloaded")
                            + " by DataUser=" + event.getUserName() + " from " + event.getRemoteHost() + " using "
                            + event.getProtocol()
                            + (mover != null ? " on DataMover=" + mover.getName() : " through " + event.getLocalHost())
                            + " (" + transferRate + " Mbits/s)";
                    for (final DataTransfer transfer : transfers) {
                        // We found also a profile, so let's add something in the
                        // transfer history!
                        if (setup != null && setup.getBoolean(USER_PORTAL_RECORD_HISTORY)) {
                            master.addTransferHistory(transfer, null,
                                    success ? event.getUpload() ? transfer.getStatusCode() : StatusFactory.DONE
                                            : StatusFactory.STOP,
                                    message, !success);
                        }
                        // If this is a download and the DataTransfer is in Standby
                        // mode then we should set it to Done!
                        if (success && !event.getUpload() && StatusFactory.HOLD.equals(transfer.getStatusCode())) {
                            if (event.getDuration() == 0) {
                                _log.debug("0ms duration detected for DataTransfer-{} download (forcing 1ms)",
                                        transfer.getId());
                                event.setDuration(1);
                            }
                            transfer.setComment(message);
                            transfer.setStatusCode(StatusFactory.DONE);
                            transfer.setDuration(event.getDuration());
                            transfer.setStartTime(new Timestamp(event.getStartTime()));
                            transfer.setSent(event.getSent());
                            transfer.setStartCount(transfer.getStartCount() + 1);
                            final var finishTime = new Timestamp(event.getStartTime() + event.getDuration());
                            transfer.setFinishTime(finishTime);
                            if (transfer.getFirstFinishTime() == null) {
                                transfer.setFirstFinishTime(finishTime);
                            }
                            try {
                                final var destination = base.getDestination(transfer.getDestinationName());
                                final var options = new DestinationOption(destination);
                                if (destination.getMailOnEnd()) {
                                    master.sendECpdsMessage(transfer);
                                }
                                if (options.deleteFromSpoolOnSuccess()) {
                                    transfer.setDeleted(true);
                                }
                                final var value = destination.getSchedulerValue();
                                value.setLastTransferOk(transfer.getId());
                                base.update(value);
                                base.update(transfer);
                            } catch (final Throwable t) {
                                _log.error("Updating DataTransfer-{} status", transfer.getId(), t);
                            }
                        }
                        try {
                            final var file = transfer.getDataFile();
                            final var history = new IncomingHistory();
                            history.setDataTransfer(transfer);
                            history.setDataTransferId(transfer.getId());
                            history.setDestination(transfer.getDestinationName());
                            history.setFileName(transfer.getTarget());
                            history.setFileSize(file.getSize());
                            history.setScheduledTime(transfer.getScheduledTime());
                            history.setStartTime(new Timestamp(event.getStartTime()));
                            history.setMetaStream(file.getMetaStream());
                            history.setMetaType(file.getMetaType());
                            history.setMetaTime(file.getMetaTime());
                            history.setTimeBase(file.getTimeBase());
                            history.setTimeStep(file.getTimeStep());
                            history.setDuration(event.getDuration());
                            history.setUserName(event.getUserName());
                            history.setSent(event.getSent());
                            history.setProtocol(event.getProtocol());
                            history.setTransferServer(event.getLocalHost());
                            history.setHostAddress(event.getRemoteHost());
                            history.setUpload(event.getUpload());
                            // Create a new IncomingHistory?
                            if (setup != null && setup.getBoolean(USER_PORTAL_RECORD_HISTORY)) {
                                _log.debug("IncomingHistory created for DataTransfer {}", transfer.getId());
                                base.insert(history, true);
                            }
                            // Create a new Splunk entry?
                            if (_splunk.isInfoEnabled() && setup != null
                                    && setup.getBoolean(USER_PORTAL_RECORD_SPLUNK)) {
                                final var destination = transfer.getDestination();
                                _splunk.info("INH;{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{};{}",
                                        "Monitored=" + destination.getMonitor(),
                                        "DataTransferId=" + history.getDataTransferId(),
                                        "DestinationName=" + destination.getName(),
                                        "DestinationType=" + DestinationOption.getLabel(destination.getType()),
                                        "FileName=" + history.getFileName(), "FileSize=" + history.getFileSize(),
                                        "ScheduledTime=" + history.getScheduledTime(),
                                        "StartTime=" + history.getStartTime(),
                                        "MetaStream=" + nullToNone(history.getMetaStream()),
                                        "MetaType=" + nullToNone(history.getMetaType()),
                                        "MetaTime=" + history.getMetaTime(), "TimeBase=" + history.getTimeBase(),
                                        "TimeStep=" + history.getTimeStep(), "Duration=" + history.getDuration(),
                                        "UserId=" + incomingUser.getId(), "CountryCode=" + incomingUser.getIso(),
                                        "UserDescription=" + incomingUser.getComment(),
                                        "BytesSent=" + history.getSent(), "TransferProtocol=" + history.getProtocol(),
                                        "TransferServer=" + history.getTransferServer(),
                                        "HostAddress=" + history.getHostAddress(),
                                        "ExpiryTime=" + transfer.getExpiryTime(),
                                        "FileSystem=" + file.getFileSystem(),
                                        "Action=" + (history.getUpload() ? "upload" : "download"));
                            }
                        } catch (final Throwable t) {
                            _log.error("Creating IncomingHistory", t);
                        }
                    }
                }
            }
        }
        monitor.done();
    }
}
