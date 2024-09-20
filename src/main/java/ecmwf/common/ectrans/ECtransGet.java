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

package ecmwf.common.ectrans;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_BUFF_INPUT_SIZE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_BUFF_OUTPUT_SIZE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_FILTER_INPUT_STREAM;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_GET_HANDLER;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_GET_HANDLER_ACK;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_GET_HANDLER_CMD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_GET_HANDLER_EXIT_CODE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_INITIAL_INPUT_FILTER;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_PLUG_BUFF_SIZE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_PLUG_DO_FLUSH;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_PLUG_READ_FULLY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_RETRIEVAL_INTERRUPT_SLOW;
import static ecmwf.common.ectrans.ECtransOptions.HOST_RETRIEVAL_MAXIMUM_DURATION;
import static ecmwf.common.ectrans.ECtransOptions.HOST_RETRIEVAL_MINIMUM_DURATION;
import static ecmwf.common.ectrans.ECtransOptions.HOST_RETRIEVAL_MINIMUM_RATE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_RETRIEVAL_RATE_THROTTLING;
import static ecmwf.common.text.Util.isNotEmpty;

import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.rmi.interruptible.InterruptibleOutputStream;
import ecmwf.common.technical.MonitoredOutputStream;
import ecmwf.common.technical.StreamManagerImp;
import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.technical.ThrottledOutputStream;
import ecmwf.common.technical.TimedFilterOutputStream;
import ecmwf.common.technical.TransferManager;
import ecmwf.common.text.Format;

/**
 * The Class ECtransGet.
 */
public final class ECtransGet extends ECtransAction {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ECtransGet.class);

    /** The ticket. */
    private final Object ticket;

    /** The posn. */
    private final long posn;

    /** The source. */
    private final String source;

    /** The _size. */
    private long size = -1;

    /** The remove original. */
    private boolean removeOriginal = false;

    /**
     * Instantiates a new ectrans get.
     *
     * @param source
     *            the source
     * @param ticket
     *            the ticket
     * @param posn
     *            the posn
     * @param removeOriginal
     *            the remove original
     */
    public ECtransGet(final String source, final Object ticket, final long posn, final boolean removeOriginal) {
        this.source = source;
        this.ticket = ticket;
        this.posn = posn;
        this.size = -1;
        this.removeOriginal = removeOriginal;
    }

    /**
     * Instantiates a new ectrans get.
     *
     * @param source
     *            the source
     * @param ticket
     *            the ticket
     * @param posn
     *            the posn
     */
    public ECtransGet(final String source, final Object ticket, final long posn) {
        this(source, ticket, posn, false);
    }

    /**
     * Sets the removes the original.
     *
     * @param removeOriginal
     *            the new removes the original
     */
    public void setRemoveOriginal(final boolean removeOriginal) {
        this.removeOriginal = removeOriginal;
    }

    /**
     * Sets the size.
     *
     * @param size
     *            the new size
     */
    public void setSize(final long size) {
        this.size = size;
    }

    /**
     * Gets the ticket.
     *
     * @return the ticket
     */
    public Object getTicket() {
        return ticket;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    @Override
    protected String getName() {
        return "get";
    }

    /**
     * Exec.
     *
     * @param module
     *            the module
     * @param interruptible
     *            the interruptible
     *
     * @throws Exception
     *             the exception
     */
    @Override
    protected void exec(final TransferModule module, final boolean interruptible) throws Exception {
        _log.debug("Start ECtransGet for {} (size={})", source, size);
        final var setup = getECtransCallback().getECtransSetup();
        final var history = getECtransHistory();
        history.setComment(source);
        if (!setup.getBoolean(HOST_ECTRANS_GET_HANDLER)) {
            var out = getRemoteProvider().getDataOutputStream(ticket);
            // Any transfer rate checking?
            if (setup.getBoolean(HOST_RETRIEVAL_INTERRUPT_SLOW)) {
                _log.debug("Setting a timed output stream");
                out = new TimedFilterOutputStream(out, setup.getDuration(HOST_RETRIEVAL_MINIMUM_DURATION).toMillis(),
                        setup.getByteSize(HOST_RETRIEVAL_MINIMUM_RATE).size(),
                        setup.getDuration(HOST_RETRIEVAL_MAXIMUM_DURATION).toMillis());
            }
            // Any transfer rate throttling?
            final var rate = setup.getByteSize(HOST_RETRIEVAL_RATE_THROTTLING);
            if (rate != null && rate.size() > 0) {
                _log.debug("Setting a throttled output stream");
                out = new ThrottledOutputStream(out, rate.size());
            }
            if (interruptible) {
                _log.debug("Setting an interruptible output stream");
                out = new InterruptibleOutputStream(out);
            }
            InputStream in = null;
            final var initialFilter = setup.getString(HOST_ECTRANS_INITIAL_INPUT_FILTER); // to hide?
            final var filter = setup.getString(HOST_ECTRANS_FILTER_INPUT_STREAM); // to hide?
            final var filters = initialFilter + "," + filter;
            if (StreamManagerImp.isFiltered(filters)) {
                module.setOutputFilter(filters);
            }
            final var monitor = new MonitoredOutputStream(out);
            final var bufferedOutputSize = setup.getByteSize(HOST_ECTRANS_BUFF_OUTPUT_SIZE);
            out = StreamManagerImp.getBuffered(monitor,
                    bufferedOutputSize != null ? (int) bufferedOutputSize.size() : 0);
            String message = null;
            var success = false;
            var done = false;
            try {
                if (!StreamManagerImp.isFiltered(filter)) {
                    _log.debug("Trying optimized get (providing output stream to transfer module)");
                    try {
                        done = module.get(out, source, posn);
                    } catch (final Throwable t) {
                        _log.warn("put", t);
                        message = t.getMessage();
                        done = true;
                    }
                }
                if (!done) {
                    _log.debug("Performing standard get (getting input stream from transfer module)");
                    in = module.get(source, posn);
                    final var bufferedSize = setup.getByteSize(HOST_ECTRANS_BUFF_INPUT_SIZE);
                    in = StreamManagerImp.getFilters(in, filter, bufferedSize != null ? (int) bufferedSize.size() : 0);
                    final var plug = new StreamPlugThread(in, out);
                    plug.setBuffSize((int) setup.getByteSize(HOST_ECTRANS_PLUG_BUFF_SIZE).size());
                    plug.setFlush(setup.getBoolean(HOST_ECTRANS_PLUG_DO_FLUSH));
                    plug.setReadFully(setup.getBoolean(HOST_ECTRANS_PLUG_READ_FULLY));
                    plug.configurableRun();
                    plug.close();
                    message = plug.getMessage();
                    in.close();
                }
                if (message == null) {
                    out.close();
                    success = true;
                }
            } finally {
                if (!success) {
                    StreamPlugThread.closeQuietly(in);
                    StreamPlugThread.closeQuietly(out);
                }
            }
            final var hasMessage = isNotEmpty(message);
            final var sent = monitor.getByteSent();
            final var sentSize = posn + sent;
            final var hostName = (String) module.getAttribute("remote.hostName");
            final var progress = size == -1 ? Format.formatSize(sent) : Format.formatPercentage(size - posn, sent);
            final var infoOrWarnMessage = "Received " + progress + (isNotEmpty(hostName) ? " from " + hostName : "")
                    + " - " + monitor.getRate() + " (bytes=" + sent + ",posn=" + posn + ")"
                    + (hasMessage ? " - " + message : "");
            if (!hasMessage) {
                _log.info(infoOrWarnMessage);
            } else {
                _log.warn(infoOrWarnMessage);
            }
            try {
                module.check(size == -1 ? sentSize : size, null);
            } catch (final IOException e) {
                _log.warn("check", e);
                if (hasMessage) {
                    final var exception = new IOException(message);
                    exception.initCause(e);
                    throw exception;
                }
                throw e;
            }
            if (hasMessage) {
                throw new IOException(message);
            }
            if (size != -1 && size != sentSize) {
                if (!StreamManagerImp.isFiltered(initialFilter)) {
                    _log.warn("Size mismatch: {}!={} (posn={})", size, sentSize, posn);
                    throw new IOException("Transfer aborted at " + progress + " (size mismatch)");
                }
                _log.info("Filtering effect of {}: {}>{} (only sent {})", initialFilter, size, sentSize, progress);
                history.setComment(
                        source + " (network rate: " + monitor.getSimplifiedRate() + ", virtual " + initialFilter
                                + " rate: " + Format.getMBitsPerSeconds(size, monitor.getDuration()) + " Mbits/s)");
            } else {
                history.setComment(source + " (" + monitor.getSimplifiedRate() + ")");
            }
        } else {
            final var getHandlerCmd = setup.getString(HOST_ECTRANS_GET_HANDLER_CMD);
            if ((getHandlerCmd == null) || getHandlerCmd.isBlank()) {
                throw new IOException("No valid handler command found (please check option \""
                        + HOST_ECTRANS_GET_HANDLER_CMD.getFullName() + "\")");
            }
            module.preGet(source, posn);
            final var target = getRemoteProvider().getDataOutputFile(ticket);
            final var manager = new TransferManager(getCommand(getHandlerCmd, source, target.getAbsolutePath()), size);
            final var result = manager.waitFor(setup.getString(HOST_ECTRANS_GET_HANDLER_ACK), null);
            if (setup.getInteger(HOST_ECTRANS_GET_HANDLER_EXIT_CODE) != result) {
                throw new IOException("Transfer aborted (exitCode: " + result + ")");
            }
            module.check(size == -1 ? target.length() : size, null);
            history.setComment(source + " (" + manager.getSimplifiedRate() + ")");
        }
        if (removeOriginal) {
            try {
                module.del(source);
            } catch (final Throwable t) {
                _log.warn("Deleting original file: {}", source, t);
                history.setComment(history.getComment() + " - source file NOT deleted");
            }
        }
    }

    /**
     * Gets the command.
     *
     * @param command
     *            the command
     * @param source
     *            the source
     * @param target
     *            the target
     *
     * @return the string
     */
    private static String getCommand(final String command, final String source, final String target) {
        final var sb = new StringBuilder(command);
        Format.replaceAll(sb, "$source", source);
        Format.replaceAll(sb, "$target", target);
        return sb.toString();
    }
}
