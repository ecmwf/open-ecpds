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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.management.timer.Timer;

import ecmwf.common.callback.CallBackObject;
import ecmwf.common.ecaccess.FileListElement;
import ecmwf.common.rmi.SocketConfig;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.ProxySocket;
import ecmwf.ecpds.master.transfer.DestinationOption;

/**
 * The Class AttachmentAccessImpl.
 */
final class AttachmentAccessImpl extends CallBackObject implements DataAccessInterface {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -2180018837664516453L;

    /** The master. */
    private final transient MasterServer master;

    /** The Constant ATTACHMENTS_DIR. */
    private static final transient String ATTACHMENTS_DIR = Cnf.at("Server", "attachments", "/tmp/ecpds-attachments")
            + File.separator;

    /**
     * Instantiates a new attachment access impl.
     *
     * @param master
     *            the master
     *
     * @throws RemoteException
     *             the remote exception
     */
    AttachmentAccessImpl(final MasterServer master) throws RemoteException {
        this.master = master;
    }

    /**
     * Gets the attachment file.
     *
     * @param destinationNameOrType
     *            the destination name or type
     * @param source
     *            the source
     * @param checkIfExists
     *            the check if exists
     *
     * @return the file
     *
     * @throws MasterException
     *             the master exception
     */
    private static File getAttachmentFile(final String destinationNameOrType, final String source,
            final boolean checkIfExists) throws MasterException {
        final var file = new File(ATTACHMENTS_DIR + destinationNameOrType + File.separator + source);
        if (checkIfExists && !file.exists()) {
            throw new MasterException("File or directory " + source + " not found");
        }
        return file;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the file last modified.
     */
    @Override
    public long getFileLastModified(final String destinationNameOrType, final String source)
            throws MasterException, IOException {
        final var monitor = new MonitorCall("getFileLastModified(" + destinationNameOrType + "," + source + ")");
        return monitor.done(getAttachmentFile(destinationNameOrType, source, true).lastModified());
    }

    /**
     * {@inheritDoc}
     *
     * Size.
     */
    @Override
    public long size(final String destinationNameOrType, final String source) throws MasterException, IOException {
        final var monitor = new MonitorCall("size(" + destinationNameOrType + "," + source + ")");
        return monitor.done(getAttachmentFile(destinationNameOrType, source, true).length());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the proxy socket input.
     */
    @Override
    public ProxySocket getProxySocketInput(final String destinationNameOrType, final String source, final long offset)
            throws MasterException, IOException {
        final var monitor = new MonitorCall(
                "getProxySocketInput(" + destinationNameOrType + "," + source + "," + offset + ")");
        final var file = getAttachmentFile(destinationNameOrType, source, true);
        if (!file.exists()) {
            throw new MasterException("File " + file.getName() + " not found");
        }

        final var ticket = master.getTicketRepository()
                .add(new AttachmentAccessTicket(file, AttachmentAccessTicket.INPUT, offset));
        final var socketConfig = new SocketConfig("ECpdsPlugin");
        return monitor
                .done(new ProxySocket(ticket.getId(), socketConfig.getPublicAddress(), socketConfig.getPort(), true));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the proxy socket input.
     */
    @Override
    public ProxySocket getProxySocketInput(final String destinationNameOrType, final String source, final long offset,
            final long length) throws MasterException, IOException {
        throw new MasterException("Range not supported");
    }

    /**
     * {@inheritDoc}
     *
     * Gets the proxy socket output.
     */
    @Override
    public ProxySocket getProxySocketOutput(final String destinationNameOrType, final String target, final long offset,
            final int umask) throws MasterException, IOException {
        final var monitor = new MonitorCall(
                "getProxySocketOutput(" + destinationNameOrType + "," + target + "," + offset + "," + umask + ")");
        final var ticket = master.getTicketRepository().add(new AttachmentAccessTicket(
                getAttachmentFile(destinationNameOrType, target, false), AttachmentAccessTicket.OUTPUT, offset, umask));
        final var socketConfig = new SocketConfig("ECpdsPlugin");
        return monitor
                .done(new ProxySocket(ticket.getId(), socketConfig.getPublicAddress(), socketConfig.getPort(), true));
    }

    /**
     * {@inheritDoc}
     *
     * Delete.
     */
    @Override
    public void delete(final String destinationNameOrType, final String source, final boolean force)
            throws MasterException, IOException {
        final var monitor = new MonitorCall("delete(" + destinationNameOrType + "," + source + "," + force + ")");
        final var file = getAttachmentFile(destinationNameOrType, source, false);
        if (!file.exists() && !force) {
            throw new MasterException("File " + file.getName() + " not found");
        }
        if (!file.delete() && !force) {
            throw new MasterException("File " + file.getName() + " NOT deleted");
        }
        monitor.done();
    }

    /**
     * {@inheritDoc}
     *
     * Mkdir.
     */
    @Override
    public void mkdir(final String destinationNameOrType, final String path) throws MasterException, IOException {
        throw new MasterException("Permission denied");
    }

    /**
     * {@inheritDoc}
     *
     * Rmdir.
     */
    @Override
    public void rmdir(final String destinationNameOrType, final String path) throws MasterException, IOException {
        throw new MasterException("Permission denied");
    }

    /**
     * {@inheritDoc}
     *
     * List.
     */
    @Override
    public FileListElement[] list(final String destinationNameOrType, final String path)
            throws MasterException, IOException {
        return list(destinationNameOrType, path, -1, -1, "");
    }

    /**
     * {@inheritDoc}
     *
     * List.
     */
    @Override
    public FileListElement[] list(final String destinationNameOrType, final String path, final int sort,
            final int order) throws MasterException, IOException {
        return list(destinationNameOrType, path, sort, order, "");
    }

    /**
     * List.
     *
     * @param destinationNameOrType
     *            the destination name or type
     * @param path
     *            the path
     * @param sort
     *            the sort
     * @param order
     *            the order
     * @param options
     *            the options
     *
     * @return the file list element[]
     *
     * @throws ecmwf.ecpds.master.MasterException
     *             the master exception
     */
    public FileListElement[] list(final String destinationNameOrType, final String path, final int sort,
            final int order, final String options) throws MasterException {
        final var monitor = new MonitorCall("list(" + destinationNameOrType + "," + path + ")");
        final var file = getAttachmentFile(destinationNameOrType, path, false);
        final var files = file.exists() && file.isDirectory() ? file.listFiles() : new File[] {};
        if (files == null || files.length == 0) {
            return monitor.done(new FileListElement[] {});
        }
        Arrays.sort(files);
        final String comment;
        final String group;
        final String user;
        final var destination = master.getDestination(destinationNameOrType);
        if (destination != null) {
            comment = destination.getComment();
            group = destination.getCountryIso();
            user = destination.getECUserName();
        } else {
            final var entryType = DestinationOption.getTypeEntry(destinationNameOrType);
            if (entryType == null) {
                // Not a destination or type?
                return monitor.done(new FileListElement[] {});
            }
            comment = DestinationOption.getLabel(entryType.getLabel() + " (id=" + entryType.getId() + ")");
            group = DestinationOption.TYPE_USER_AND_GROUP;
            user = DestinationOption.TYPE_USER_AND_GROUP;
        }
        final List<FileListElement> elements = new ArrayList<>();
        for (final File fileEntry : files) {
            final var filename = fileEntry.getName();
            final var element = new FileListElement();
            element.setComment(comment);
            element.setGroup(group);
            element.setUser(user);
            element.setName(filename);
            element.setTime(fileEntry.lastModified());
            element.setRight(fileEntry.isDirectory() ? "drwxr-x---" : "-rw-r--r--");
            element.setSize(String.valueOf(fileEntry.length()));
            elements.add(element);
        }
        return monitor.done(elements.toArray(new FileListElement[elements.size()]));
    }

    /**
     * {@inheritDoc}
     *
     * Gets the.
     */
    @Override
    public FileListElement get(final String destinationNameOrType, final String path)
            throws MasterException, IOException {
        final var monitor = new MonitorCall("get(" + destinationNameOrType + "," + path + ")");
        // We have to list all the files! (could be improved)
        final var list = list(destinationNameOrType, new File(path).getParent());
        FileListElement element = null;
        final var file = new File(path);
        if (list != null) {
            for (final FileListElement fle : list) {
                if (fle.getName().equals(file.getName())) {
                    element = fle;
                    break;
                }
            }
        }
        return monitor.done(element);
    }

    /**
     * {@inheritDoc}
     *
     * Move.
     */
    @Override
    public void move(final String destinationNameOrType, final String source, final String target)
            throws MasterException, IOException {
        final var monitor = new MonitorCall("move(" + destinationNameOrType + "," + source + "," + target + ")");
        final var file = getAttachmentFile(destinationNameOrType, source, false);
        if (!file.exists()) {
            throw new FileNotFoundException(file.getName());
        }
        if (!file.renameTo(getAttachmentFile(destinationNameOrType, target, false))) {
            throw new MasterException("File " + file.getName() + " NOT renamed");
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
        master.getTicketRepository().check(proxy.getTicket(),
                Cnf.at("Other", "ticketWaitDuration", 20 * Timer.ONE_MINUTE));
        monitor.done();
    }
}
