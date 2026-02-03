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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_BUFF_INPUT_SIZE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_BUFF_OUTPUT_SIZE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_CREATE_CHECKSUM;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_INITIAL_INPUT_FILTER;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_INITIAL_INPUT_MD5;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_INITIAL_INPUT_SIZE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_NOTIFY_AUTH;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_NOTIFY_POST;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_NOTIFY_PRE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_NOTIFY_PUBLISH;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_PLUG_BUFF_SIZE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_PLUG_DO_FLUSH;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_PLUG_READ_FULLY;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_PUT_HANDLER;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_PUT_HANDLER_ACK;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_PUT_HANDLER_CMD;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_PUT_HANDLER_EXIT_CODE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_PUT_MONITORED_INPUT_DELTA;
import static ecmwf.common.ectrans.ECtransOptions.HOST_ECTRANS_SUPPORT_FILTER;
import static ecmwf.common.ectrans.ECtransOptions.HOST_UPLOAD_INTERRUPT_SLOW;
import static ecmwf.common.ectrans.ECtransOptions.HOST_UPLOAD_MAXIMUM_DURATION;
import static ecmwf.common.ectrans.ECtransOptions.HOST_UPLOAD_MINIMUM_DURATION;
import static ecmwf.common.ectrans.ECtransOptions.HOST_UPLOAD_MINIMUM_RATE;
import static ecmwf.common.ectrans.ECtransOptions.HOST_UPLOAD_RATE_THROTTLING;
import static ecmwf.common.text.Util.isNotEmpty;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.checksum.Checksum;
import ecmwf.common.rmi.interruptible.InterruptibleInputStream;
import ecmwf.common.technical.MonitoredInputStream;
import ecmwf.common.technical.MonitoredOutputStream;
import ecmwf.common.technical.ProgressHandler;
import ecmwf.common.technical.ScriptManager;
import ecmwf.common.technical.StreamManagerImp;
import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.technical.ThrottledInputStream;
import ecmwf.common.technical.TimedFilterInputStream;
import ecmwf.common.technical.TransferManager;
import ecmwf.common.text.Format;
import ecmwf.common.text.Options;

/**
 * The Class ECtransPut.
 */
public final class ECtransPut extends ECtransAction {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ECtransPut.class);

    /** The Constant UNKNOWN_SIZE. */
    private static final long UNKNOWN_SIZE = -1;

    /** The ticket. */
    private final Object ticket;

    /** The posn. */
    private final long posn;

    /** The size. */
    private final long size;

    /** The handler. */
    private final ProgressHandler handler;

    /** The target. */
    private String target = null;

    /** The toFilter. */
    private boolean toFilter = false;

    /**
     * Instantiates a new ectrans put.
     *
     * @param target
     *            the target
     * @param ticket
     *            the ticket
     * @param posn
     *            the posn
     * @param size
     *            the size
     */
    public ECtransPut(final String target, final Object ticket, final long posn, final long size) {
        this(target, ticket, posn, size, false, null);
    }

    /**
     * Instantiates a new ectrans put.
     *
     * @param target
     *            the target
     * @param ticket
     *            the ticket
     * @param posn
     *            the posn
     * @param size
     *            the size
     * @param toFilter
     *            the to filter
     * @param handler
     *            the handler
     */
    public ECtransPut(final String target, final Object ticket, final long posn, final long size,
            final boolean toFilter, final ProgressHandler handler) {
        this.target = target;
        this.ticket = ticket;
        this.posn = posn;
        this.size = size;
        this.toFilter = toFilter;
        this.handler = handler;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    @Override
    protected String getName() {
        return "put";
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
        _log.debug("Start ECtransPut for {} (size={})", target, size);
        final var setup = getECtransCallback().getECtransSetup();
        // Do we have a notification request?
        NotificationInterface notificationInterface = null;
        try {
            final var connectOptions = (Options) module.getAttribute("connectOptions");
            // First version of the notification mechanism (based on a script)
            final var notifyPre = new StringBuilder(setup.getString(HOST_ECTRANS_NOTIFY_PRE));
            if (notifyPre.length() > 0 && isNotEmpty(connectOptions)) {
                connectOptions.replace(notifyPre);
                ScriptManager.exec(ScriptManager.JS, Format.removeUnknownMetadata(notifyPre.toString()));
            }
            // Second version of the notification mechanism (e.g. mqtt)
            final var notifyAuth = setup.getOptions(HOST_ECTRANS_NOTIFY_AUTH);
            if (!notifyAuth.isEmpty() && isNotEmpty(connectOptions)) {
                notifyAuth.inject(connectOptions);
                // The format of the notification request is the following:
                // test.notifyAuth="url=mqtt://localhost:1883;name=ecpds;password=ecpds2020"
                final var url = Format.removeUnknownMetadata(notifyAuth.get("url", null));
                final var name = Format.removeUnknownMetadata(notifyAuth.get("name", null));
                final var passwd = Format.removeUnknownMetadata(notifyAuth.get("password", null));
                _log.debug("Notification requested ({},{},{})", url, name, passwd);
                if (!isNotEmpty(url) || !isNotEmpty(name) || !isNotEmpty(passwd)) {
                    throw new IOException("Missing parameters for notification");
                }
                _log.debug("Notification requested ({})", url);
                try {
                    notificationInterface = getRemoteProvider().getNotificationInterface(url, name, passwd);
                } catch (final Throwable t) {
                    throw new IOException("Initializing notification", t);
                }
            }
            final var history = getECtransHistory();
            history.setComment(target);
            var initialInputFilter = setup.getString(HOST_ECTRANS_INITIAL_INPUT_FILTER);
            initialInputFilter = StreamManagerImp.isFiltered(initialInputFilter) ? initialInputFilter : null;
            toFilter = toFilter && initialInputFilter != null;
            final long initialInputSize = setup.getLong(HOST_ECTRANS_INITIAL_INPUT_SIZE);
            var checksum = setup.getString(HOST_ECTRANS_INITIAL_INPUT_MD5);
            var createChecksum = setup.getBoolean(HOST_ECTRANS_CREATE_CHECKSUM);
            final var decrompressedOnTheFly = setup.getBoolean(HOST_ECTRANS_SUPPORT_FILTER);
            if (!createChecksum) {
                checksum = null;
            }
            if (!setup.getBoolean(HOST_ECTRANS_PUT_HANDLER)) {
                // The data transmission is processed here, no delegation!
                if (checksum != null) {
                    module.setInputMd5(checksum);
                }
                if (initialInputFilter != null) {
                    if (decrompressedOnTheFly) {
                        _log.debug("File will be decompressed on-the-fly on remote site");
                        module.setInputFilter(initialInputFilter);
                        if (initialInputSize != UNKNOWN_SIZE) {
                            module.setInputSize(initialInputSize);
                        }
                    } else {
                        _log.debug("File will NOT be decompressed on-the-fly on remote site (done remotely afterward)");
                        history.setComment(target += StreamManagerImp.getExtension(initialInputFilter));
                    }
                    if (createChecksum && checksum == null) {
                        try {
                            module.delegateChecksum();
                        } catch (final IOException e) {
                            // Not a DissFTP server? Let's ignore it.
                            _log.debug("Checksum not available", e);
                        }
                        createChecksum = false;
                    }
                }
                var in = getRemoteProvider().getDataInputStream(ticket);
                // Any transfer rate checking?
                if (setup.getBoolean(HOST_UPLOAD_INTERRUPT_SLOW)) {
                    _log.debug("Setting a timed input stream");
                    in = new TimedFilterInputStream(in, setup.getDuration(HOST_UPLOAD_MINIMUM_DURATION).toMillis(),
                            setup.getByteSize(HOST_UPLOAD_MINIMUM_RATE).size(),
                            setup.getDuration(HOST_UPLOAD_MAXIMUM_DURATION).toMillis());
                }
                // Any transfer rate throttling?
                final var rate = setup.getByteSize(HOST_UPLOAD_RATE_THROTTLING);
                if (rate != null && rate.size() > 0) {
                    _log.debug("Setting a throttled input stream");
                    in = new ThrottledInputStream(in, rate.size());
                }
                if (interruptible) {
                    _log.debug("Setting an interruptible input stream");
                    in = new InterruptibleInputStream(in);
                }
                OutputStream out = null;
                Checksum md5 = null;
                final var algorithm = Checksum.Algorithm.MD5;
                if (createChecksum && checksum == null) {
                    try {
                        md5 = Checksum.getChecksum(algorithm, in);
                        in = md5.getInputStream();
                    } catch (final Throwable t) {
                        _log.warn("Cannot init {}", algorithm.getName(), t);
                    }
                }
                final var mIn = new MonitoredInputStream(in,
                        setup.get(HOST_ECTRANS_PUT_MONITORED_INPUT_DELTA, Duration.ofMillis(0)).toMillis(), handler);
                final var bufferedInputSize = setup.getByteSize(HOST_ECTRANS_BUFF_INPUT_SIZE);
                in = StreamManagerImp.getBuffered(mIn, bufferedInputSize != null ? (int) bufferedInputSize.size() : 0);
                String message = null;
                MonitoredOutputStream mOut = null;
                var success = false;
                var done = false;
                try {
                    if (!toFilter) {
                        _log.debug("Trying optimized put (providing input stream to transfer module)");
                        try {
                            done = module.put(in, target, posn, size);
                        } catch (final Throwable t) {
                            _log.warn("Optimized put failed, falling back to standard put", t);
                            message = t.getMessage();
                            done = true;
                        }
                    }
                    if (!done) {
                        _log.debug("Performing standard put (getting output stream from transfer module)");
                        out = module.put(target, posn, size);
                        if (!decrompressedOnTheFly) {
                            // We have to know the size of the compressed file when we will do the checking!
                            mOut = new MonitoredOutputStream(out);
                        }
                        final var bufferedSize = setup.getByteSize(HOST_ECTRANS_BUFF_OUTPUT_SIZE);
                        out = StreamManagerImp.getFilters(mOut != null ? mOut : out,
                                toFilter ? initialInputFilter : null,
                                bufferedSize != null ? (int) bufferedSize.size() : 0);
                        final var plug = new StreamPlugThread(in, out);
                        plug.setBuffSize((int) setup.getByteSize(HOST_ECTRANS_PLUG_BUFF_SIZE).size());
                        plug.setFlush(StreamManagerImp.isFiltered(out) || setup.getBoolean(HOST_ECTRANS_PLUG_DO_FLUSH));
                        plug.setReadFully(setup.getBoolean(HOST_ECTRANS_PLUG_READ_FULLY));
                        plug.configurableRun();
                        message = plug.getMessage();
                        plug.close();
                        out.close();
                    }
                    if (message == null) {
                        in.close();
                        success = true;
                    }
                } finally {
                    module.updateSocketStatistics();
                    if (!success) {
                        StreamPlugThread.closeQuietly(in);
                        StreamPlugThread.closeQuietly(out);
                    }
                }
                final var hasMessage = isNotEmpty(message);
                // This is the size of the data sent and rate (plain or compressed)
                final long sent;
                final String streamRate;
                if (mOut != null) {
                    sent = mOut.getByteSent();
                    streamRate = mOut.getRate();
                    _log.debug("OutputRate={}, InputRateOnClose={}", mOut.getRate(), mIn.getRateOnClose());
                } else {
                    sent = mIn.getByteSent();
                    streamRate = mIn.getRate();
                    _log.debug("InputRate={}, InputRateOnClose={}", mIn.getRate(), mIn.getRateOnClose());
                }
                final var contentSize = !decrompressedOnTheFly || initialInputSize == UNKNOWN_SIZE ? posn + sent
                        : initialInputSize;
                final var progress = size == UNKNOWN_SIZE ? Format.formatSize(sent)
                        : Format.formatPercentage(size - posn, sent);
                final var infoOrWarnMessage = "Sent " + progress + " - " + streamRate + " (bytes=" + sent + ",posn="
                        + posn + ")" + (hasMessage ? " - " + message : "");
                if (hasMessage) {
                    _log.warn(infoOrWarnMessage);
                    // We had an error, let's check if we can also get some information from the
                    // transfer module?
                    try {
                        module.check(contentSize, checksum, true);
                    } catch (final IOException e) {
                        _log.warn("Transfer module check after transfer error", e);
                        final var eMessage = e.getMessage();
                        if (isNotEmpty(eMessage) && !eMessage.equals(message)) {
                            message = eMessage + " -> " + message;
                        }
                    }
                    throw new IOException(message);
                }
                _log.info(infoOrWarnMessage);
                if (md5 != null) {
                    checksum = md5.getValue();
                    _log.debug("{}: {}", algorithm.getName(), checksum);
                }
                // We had no error on this side, let's check if it also looked good from the
                // transfer module point of view?
                try {
                    module.check(contentSize, checksum, false);
                } catch (final IOException e) {
                    _log.warn("Transfer module check after transfer success", e);
                    throw e;
                }
                if (size != UNKNOWN_SIZE && size != contentSize) {
                    _log.debug("Different sizes: {}!={} (posn={})", size, contentSize, posn);
                    if (initialInputFilter == null) {
                        throw new IOException("Transfer aborted at " + progress);
                    }
                    if (_log.isInfoEnabled()) {
                        _log.info("Filter improvement ({}): {}", initialInputFilter,
                                Format.formatPercentage(size, contentSize));
                    }
                    module.setAttribute("compression.fileSize", contentSize);
                } else {
                    history.setComment(target + " (" + mIn.getSimplifiedRate() + ")");
                }
                // Let's set the nature of the operation for the transfer history!
                module.setAttribute("remote.operation", "sent");
            } else {
                // The transfer is not processed here, we are delegating the data transmission
                // to an external service!
                if (initialInputFilter != null) { // In this case, no filtering is possible!
                    throw new IOException("Filter not supported with putHandler: " + initialInputFilter);
                }
                final var putHandlerCmd = setup.getString(HOST_ECTRANS_PUT_HANDLER_CMD);
                if (putHandlerCmd != null && !putHandlerCmd.isBlank()) {
                    // This is a shell command to start on the underlying OS!
                    final var sourceFile = getRemoteProvider().getDataOutputFile(ticket); // The source on the local
                                                                                          // host!
                    final var targetFile = new File(module.prePut(target, sourceFile.getName(), posn));
                    final var contentSize = size == UNKNOWN_SIZE ? sourceFile.length() : size;
                    final var manager = new TransferManager(getCommand(putHandlerCmd, sourceFile, targetFile),
                            contentSize);
                    manager.setDebug(module.getDebug());
                    final var result = manager.waitFor(setup.getString(HOST_ECTRANS_PUT_HANDLER_ACK), null);
                    if (setup.getInteger(HOST_ECTRANS_PUT_HANDLER_EXIT_CODE) != result) {
                        throw new IOException("Transfer aborted (exitCode: " + result + ")");
                    }
                    module.check(contentSize, checksum, false); // Check and rename if needed (e.g. when a temporary
                                                                // filename is used)
                    history.setComment(target + " (" + manager.getSimplifiedRate() + ")");
                    // Let's set the nature of the operation for the transfer history!
                    module.setAttribute("remote.operation", "exec");
                } else {
                    // The delegation is provided by the transfer module, now we try to copy the
                    // file from its original place to the target place!
                    try {
                        module.copy(getRemoteProvider().getOriginalFilename(ticket), target, posn, size);
                    } catch (final IOException e) {
                        _log.warn("copy", e);
                        throw e;
                    }
                    // Let's set the nature of the operation for the transfer history!
                    module.setAttribute("remote.operation", "copy");
                }
            }
            // Do we have a notification to execute?
            final var notifyPost = new StringBuilder(setup.getString(HOST_ECTRANS_NOTIFY_POST));
            if (notifyPost.length() > 0 && isNotEmpty(connectOptions)) {
                connectOptions.replace(notifyPost);
                ScriptManager.exec(ScriptManager.JS, Format.removeUnknownMetadata(notifyPost.toString()));
            }
            // Do we have a notification interface. If it is the case then we have to
            // send a notification
            if (notificationInterface != null) {
                // The format of the parameter is the following:
                // test.notifyPublish="topic=ecpds/gts/0000;payload=https://ecpds.ecmwf.int/data/gts/FGTER.bin;metadata=filename=FGTER.bin,metatime=0000;lifetime=4505"
                final var notifyPublish = setup.getOptions(HOST_ECTRANS_NOTIFY_PUBLISH, ";\n");
                notifyPublish.inject(connectOptions);
                final var url = Format
                        .removeUnknownMetadata(notifyPublish.get("url", notifyPublish.get("payload", null)));
                final var key = Format
                        .removeUnknownMetadata(notifyPublish.get("key", notifyPublish.get("topic", null)));
                final var value = Format
                        .removeUnknownMetadata(notifyPublish.get("value", notifyPublish.get("metadata", null)));
                if (isNotEmpty(url) && isNotEmpty(key) && isNotEmpty(value)) {
                    final var lifetime = notifyPublish.get("lifetime", -1L);
                    _log.debug("Notification requested ({},{},{},{})", url, key, value, lifetime);
                    try {
                        notificationInterface.notify(url, key, value, lifetime);
                    } catch (final Throwable t) {
                        throw new IOException("Publishing notification", t);
                    }
                }
            }
        } finally {
            StreamPlugThread.closeQuietly(notificationInterface);
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
    private static String getCommand(final String command, final File source, final File target) {
        final var sb = new StringBuilder(command);
        Format.replaceAll(sb, "$source", source);
        Format.replaceAll(sb, "$target", target);
        return sb.toString();
    }
}
