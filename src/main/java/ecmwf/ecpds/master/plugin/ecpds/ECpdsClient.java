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

package ecmwf.ecpds.master.plugin.ecpds;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.ectrans.ECtransGroups.Module.DESTINATION_INCOMING;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_INCOMING_DATE_FORMAT;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_INCOMING_DELAY;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_INCOMING_EVENT;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_INCOMING_FAIL_ON_METADATA_PARSING_ERROR;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_INCOMING_LIFETIME;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_INCOMING_METADATA;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_INCOMING_PRIORITY;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_INCOMING_STANDBY;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_INCOMING_TMP;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_INCOMING_VERSION;
import static ecmwf.common.text.Util.isNotEmpty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.Destination;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.Pair;
import ecmwf.common.technical.StreamPlugThread;
import ecmwf.common.text.Format;
import ecmwf.common.version.Version;
import ecmwf.ecpds.master.plugin.ecpds.request.ECpdsCompleted;
import ecmwf.ecpds.master.plugin.ecpds.request.ECpdsExpected;
import ecmwf.ecpds.master.plugin.ecpds.request.ECpdsPut;
import ecmwf.ecpds.master.plugin.ecpds.request.ECpdsSelect;
import ecmwf.ecpds.master.plugin.ecpds.request.ECpdsStarted;
import ecmwf.ecpds.master.plugin.ecpds.request.ECpdsWaitForGroup;
import ecmwf.ecpds.master.transfer.DestinationOption;

/**
 * The Class ECpdsClient.
 */
public final class ECpdsClient {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ECpdsClient.class);

    /** The Constant _host. */
    private static final String HOST = Cnf.at("ECpdsPlugin", "host", Cnf.at("Login", "hostName", "localhost"));

    /** The Constant _port. */
    private static final int PORT = Cnf.at("ECpdsPlugin", "port", Cnf.at("Ports", "ecpds", 6640));

    /**
     * Hide the default constructor!
     */
    private ECpdsClient() {
    }

    /**
     * _read.
     *
     * @param in
     *            the in
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static String read(final BufferedReader in) throws IOException {
        return read(in, null);
    }

    /**
     * _read.
     *
     * @param in
     *            the in
     * @param parameterName
     *            the parameter name
     *
     * @return the string
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static String read(final BufferedReader in, final String parameterName) throws IOException {
        final var result = in.readLine();
        if (result == null) {
            throw new IOException("Connection closed");
        }
        final var message = result.trim();
        if (message.startsWith("+")) {
            // The message is in the form: "+message"
            if (parameterName == null) {
                return message.substring(1);
            }
            // The message is in the form: "+PARAMETER-NAME message". Let's
            // check if this is the parameter name we are expecting and extract
            // the message
            var toReturn = message.substring(1);
            final var lowerCase = toReturn.toLowerCase();
            if (!lowerCase.startsWith(parameterName.toLowerCase())) {
                if (lowerCase.startsWith("message ")) {
                    throw new IOException(toReturn.substring(8));
                } else {
                    throw new IOException("Unexpected parameter");
                }
            }
            toReturn = toReturn.substring(parameterName.length()).trim();
            _log.debug("Received: {}", toReturn);
            return toReturn;
        }
        if (message.startsWith("-")) {
            throw new IOException(message.substring(1));
        } else {
            throw new IOException("Bad message format: " + message);
        }
    }

    /**
     * _write.
     *
     * @param _out
     *            the _out
     * @param message
     *            the message
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static void write(final PrintStream _out, final String message) {
        _log.debug("Sending: {}", message);
        _out.println(message);
        _out.flush();
    }

    /**
     * _write.
     *
     * @param out
     *            the _out
     * @param name
     *            the name
     * @param value
     *            the value
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static void write(final PrintStream out, final String name, final Object value) {
        if (value != null) {
            write(out, name + " " + value);
        }
    }

    /**
     * Gets the data file id.
     *
     * @param message
     *            the message
     *
     * @return the long
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static long getDataFileId(final String message) throws IOException {
        final int pos1;
        final int pos2;
        if ((pos1 = message.indexOf("(DatafileId=")) >= 0 && (pos2 = message.substring(pos1 + 12).indexOf(")")) > 0) {
            try {
                // Parse the DataFileId from the message
                return Long.parseLong(message.substring(pos1 + 12, pos1 + 12 + pos2));
            } catch (final Throwable ignored) {
                // Ignored!
            }
        }
        // This is not a DatafileId so it has to be an error message!
        throw new IOException(message);
    }

    /**
     * This method is used by the incoming ftp/sftp server through the DataFileAccessImpl class (mkdir and
     * getProxySocketOutput). This is were the metadata is built. This upload does not trigger an incoming history in
     * the ecpds plugin.
     *
     * @param from
     *            the from
     * @param destination
     *            the destination
     * @param filename
     *            the filename
     * @param in
     *            the in
     * @param timeFile
     *            the time file
     * @param timeBase
     *            the time base
     *
     * @return the long
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static long put(final String from, final Destination destination, final String filename,
            final InputStream in, final long timeFile, final long timeBase) throws IOException {
        Socket masterSocket = null;
        try {
            long fileSize = -1;
            masterSocket = new Socket(HOST, PORT);
            _log.debug("Connected to master {}:{}", HOST, PORT);
            final var destinationName = destination.getName();
            final var setup = DESTINATION_INCOMING.getECtransSetup(destination.getData());
            final var master = new PrintStream(masterSocket.getOutputStream());
            final var masterIn = new BufferedReader(new InputStreamReader(masterSocket.getInputStream()));
            // The order VERSION, FROM, USER, MESSAGE and REMOTEIP matters!
            write(master, "VERSION " + Version.getFullVersion() + " ("
                    + (DestinationOption.isAcquisition(destination.getType()) ? "acquisition" : "other") + ")");
            write(master, "FROM " + from);
            write(master, "USER -");
            read(masterIn, "MESSAGE"); // Welcome!
            write(master, "REMOTEIP -"); // Not possible to retrieve directly with source host!
            write(master, "DESTINATION " + destinationName);
            write(master, "TIMEFILE " + timeFile / 1000L);
            write(master, "ORIGINAL " + filename);
            write(master, "SOURCE " + filename);
            final var uniqueName = setup.getString(DESTINATION_INCOMING_VERSION);
            if (isNotEmpty(uniqueName)) {
                final var format = setup.getString(DESTINATION_INCOMING_DATE_FORMAT);
                final var sb = new StringBuilder(uniqueName);
                Format.replaceAll(sb, "$date", Format.formatTime(format, System.currentTimeMillis()));
                Format.replaceAll(sb, "$timestamp", System.currentTimeMillis());
                Format.replaceAll(sb, "$destination", destinationName);
                Format.replaceAll(sb, "$target", filename);
                Format.replaceAll(sb, "$original", filename);
                Format.replaceAll(sb, "$timefile", timeFile);
                write(master, "UNIQUENAME " + sb.toString());
            }
            write(master, "TARGET " + filename);
            write(master, "STANDBY " + (setup.matches(DESTINATION_INCOMING_TMP, filename)
                    || setup.getBoolean(DESTINATION_INCOMING_STANDBY)));
            write(master, "PRIORITY " + setup.getInteger(DESTINATION_INCOMING_PRIORITY));
            write(master, "EVENT " + setup.getBoolean(DESTINATION_INCOMING_EVENT));
            // Let's make sure the time base (product date) is set!
            write(master, "METADATA date=" + Format.formatTime("yyyyMMdd", timeBase) + "00");
            // Now process the metadata provided (note the product date can still be
            // overwritten here if required).
            final var currentTime = System.currentTimeMillis();
            final var metadata = setup.replace(
                    new Pair<>("$date",
                            Format.formatTime(setup.getString(DESTINATION_INCOMING_DATE_FORMAT), currentTime)),
                    new Pair<>("$timestamp", currentTime), new Pair<>("$destination[]", destinationName),
                    new Pair<>("$target[]", filename), new Pair<>("$original[]", filename),
                    new Pair<>("$timefile", timeFile / 1000L * 1000L)).getString(DESTINATION_INCOMING_METADATA);
            if (isNotEmpty(metadata)) {
                try {
                    write(master, "METADATA " + Format.choose(metadata));
                } catch (final Exception exception) {
                    if (setup.getBoolean(DESTINATION_INCOMING_FAIL_ON_METADATA_PARSING_ERROR)) {
                        throw new IOException(
                                Format.getLastMessage(exception, "filename does not match required pattern"));
                    }
                    _log.warn("Parsing metadata", exception);
                }
            }
            final var delay = setup.getDuration(DESTINATION_INCOMING_DELAY);
            if (delay != null && delay.isPositive()) {
                write(master, "DELAY " + delay);
            }
            final var lifeTime = setup.getDuration(DESTINATION_INCOMING_LIFETIME);
            if (lifeTime != null && lifeTime.isPositive()) {
                write(master, "LIFETIME " + lifeTime);
            }
            write(master, "FORCE " + true);
            write(master, "PUT");
            final var target = read(masterIn, "TARGET");
            final var ecproxyList = read(masterIn, "ECPROXY");
            String stat = null;
            if (!"Please continue".equals(read(masterIn, ""))) {
                final var message = "Unexpected reply from master";
                write(master, "-" + message);
                throw new IOException(message);
            }
            final var token = new StringTokenizer(ecproxyList, "|");
            while (token.hasMoreElements()) {
                final var currentEcproxy = token.nextToken();
                var ecproxyAddress = currentEcproxy;
                final var index = ecproxyAddress.indexOf(":");
                if (index != -1) {
                    Socket moverSocket = null;
                    try {
                        final var ecproxyPort = Short.parseShort(ecproxyAddress.substring(index + 1));
                        ecproxyAddress = ecproxyAddress.substring(0, index);
                        moverSocket = new Socket(ecproxyAddress, ecproxyPort);
                        _log.debug("Connected to mover {}:{}", ecproxyAddress, ecproxyPort);
                        final var mover = new PrintStream(moverSocket.getOutputStream());
                        final var moverIn = new BufferedReader(new InputStreamReader(moverSocket.getInputStream()));
                        write(mover, "ECPDS " + Version.getFullVersion());
                        write(mover, "TARGET " + target);
                        read(moverIn, "CONNECT");
                        write(mover, "SIZE -1");
                        _log.debug("Sending file content");
                        fileSize = StreamPlugThread.copy(moverSocket.getOutputStream(), in,
                                StreamPlugThread.DEFAULT_BUFF_SIZE);
                        if (_log.isDebugEnabled()) {
                            _log.debug("Sent {}", Format.formatSize(fileSize));
                        }
                        // Close down the output stream of this socket for
                        // the server to know there are no more data to be
                        // retrieved!
                        moverSocket.shutdownOutput();
                        // TODO: there are some side effects on some
                        // platforms and the all socket might be close as a
                        // result. I should send a packet to inform about
                        // the end of the data. This should be also
                        // implemented on the ECproxyPlugin class!
                        stat = read(moverIn, "STAT");
                        read(moverIn, "BYE");
                        write(mover, "BYE");
                        break;
                    } catch (final Throwable t) {
                        // try the next mover in the list
                        _log.debug("Communicating with ecproxy: {}", currentEcproxy, t);
                        throw new IOException("Communicating with ecproxy: " + currentEcproxy + ": " + t.getMessage());
                    } finally {
                        StreamPlugThread.closeQuietly(moverSocket);
                    }
                }
            }
            if (stat != null && fileSize != -1) {
                write(master, "HOST " + stat);
                write(master, "SIZE " + fileSize);
                write(master, "BYE");
                return getDataFileId(read(masterIn, "MESSAGE"));
            } else {
                final var message = "Transmission failed to each Data Mover";
                write(master, "-" + message);
                throw new IOException(message);
            }
        } finally {
            StreamPlugThread.closeQuietly(masterSocket);
        }
    }

    /**
     * This method is used from the AcquisitionThread on the MasterServer and the REST API through the monitoring
     * interface.
     *
     * @param from
     *            the from
     * @param user
     *            the user
     * @param destination
     *            the destination
     * @param hostForAcquisition
     *            the host for acquisition
     * @param metadata
     *            the metadata
     * @param original
     *            the original
     * @param uniqueName
     *            the unique name
     * @param target
     *            the target
     * @param timeFile
     *            the time file
     * @param fileSize
     *            the file size
     * @param event
     *            the event
     * @param priority
     *            the priority
     * @param lifeTime
     *            the life time
     * @param at
     *            the at
     * @param standby
     *            the standby
     * @param groupby
     *            the groupby
     * @param noretrieval
     *            the noretrieval
     * @param force
     *            the force
     * @param failedOnly
     *            the failed only
     * @param deleteOriginal
     *            the delete original
     * @param transferGroup
     *            the transfer group
     *
     * @return the long
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static long put(final String from, final String user, final Destination destination,
            final String hostForAcquisition, final String metadata, final String original, final String uniqueName,
            final String target, final long timeFile, final long fileSize, final boolean event, final int priority,
            final String lifeTime, final String at, final boolean standby, final String groupby,
            final boolean noretrieval, final boolean force, final boolean failedOnly, final boolean deleteOriginal,
            final String transferGroup) throws IOException {
        Socket masterSocket = null;
        try {
            masterSocket = new Socket(HOST, PORT);
            _log.debug("Connected to master {}:{}", HOST, PORT);
            final var master = new PrintStream(masterSocket.getOutputStream());
            final var masterIn = new BufferedReader(new InputStreamReader(masterSocket.getInputStream()));
            // The order VERSION, FROM, USER, MESSAGE and REMOTEIP matters!
            write(master, "VERSION " + Version.getFullVersion() + " (acquisition)");
            write(master, "FROM " + from);
            write(master, "USER " + (isNotEmpty(user) ? user : "-"));
            read(masterIn, "MESSAGE"); // Welcome!
            write(master, "REMOTEIP -"); // Not possible to retrieve directly with source host!
            write(master, "DESTINATION " + destination.getName());
            if (isNotEmpty(at)) {
                write(master, "AT " + at);
            }
            write(master, "TIMEFILE " + timeFile / 1000L);
            if (fileSize != -1) {
                write(master, "SIZE " + fileSize);
            }
            write(master, "ORIGINAL " + original);
            if (isNotEmpty(metadata)) {
                write(master, "METADATA " + metadata);
            }
            if (isNotEmpty(hostForAcquisition)) {
                write(master, "HOSTFORACQUISITION " + hostForAcquisition);
            }
            write(master, "SOURCE " + original);
            if (isNotEmpty(uniqueName)) {
                write(master, "UNIQUENAME " + uniqueName);
            }
            write(master, "TARGET " + target);
            write(master, "STANDBY " + standby);
            write(master, "EVENT " + event);
            write(master, "PRIORITY " + priority);
            if (isNotEmpty(lifeTime)) {
                write(master, "LIFETIME " + lifeTime);
            }
            write(master, "GROUPBY " + groupby);
            if (isNotEmpty(transferGroup)) {
                write(master, "GROUP " + transferGroup);
            }
            write(master, "NORETRIEVAL " + noretrieval);
            write(master, "FAILEDONLY " + failedOnly);
            write(master, "FORCE " + force);
            write(master, "REMOVE " + deleteOriginal);
            write(master, "PUT");
            return getDataFileId(read(masterIn, "MESSAGE"));
        } finally {
            StreamPlugThread.closeQuietly(masterSocket);
        }
    }

    /**
     * Process.
     *
     * @param req
     *            the req
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void process(final ECpdsExpected req) throws IOException {
        Socket masterSocket = null;
        try {
            masterSocket = new Socket(HOST, PORT);
            _log.debug("Connected to master {}:{}", HOST, PORT);
            final var master = new PrintStream(masterSocket.getOutputStream());
            final var masterIn = new BufferedReader(new InputStreamReader(masterSocket.getInputStream()));
            write(master, "VERSION", req.getVERSION());
            write(master, "USER", req.getUSER());
            write(master, "METADATA \"" + req.getMETADATA() + "\"");
            write(master, "AT", req.getAT());
            write(master, "EXPECTED");
            final var message = read(masterIn, "MESSAGE");
            _log.debug(message);
        } finally {
            StreamPlugThread.closeQuietly(masterSocket);
        }
    }

    /**
     * Process.
     *
     * @param req
     *            the req
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void process(final ECpdsStarted req) throws IOException {
        Socket masterSocket = null;
        try {
            masterSocket = new Socket(HOST, PORT);
            _log.debug("Connected to master {}:{}", HOST, PORT);
            final var master = new PrintStream(masterSocket.getOutputStream());
            final var masterIn = new BufferedReader(new InputStreamReader(masterSocket.getInputStream()));
            write(master, "VERSION", req.getVERSION());
            write(master, "USER", req.getUSER());
            write(master, "METADATA \"" + req.getMETADATA() + "\"");
            write(master, "STARTED");
            final var message = read(masterIn, "MESSAGE");
            _log.debug(message);
        } finally {
            StreamPlugThread.closeQuietly(masterSocket);
        }
    }

    /**
     * Process.
     *
     * @param req
     *            the req
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void process(final ECpdsCompleted req) throws IOException {
        Socket masterSocket = null;
        try {
            masterSocket = new Socket(HOST, PORT);
            _log.debug("Connected to master {}:{}", HOST, PORT);
            final var master = new PrintStream(masterSocket.getOutputStream());
            final var masterIn = new BufferedReader(new InputStreamReader(masterSocket.getInputStream()));
            write(master, "VERSION", req.getVERSION());
            write(master, "USER", req.getUSER());
            write(master, "METADATA \"" + req.getMETADATA() + "\"");
            write(master, "COMPLETED");
            final var message = read(masterIn, "MESSAGE");
            _log.debug(message);
        } finally {
            StreamPlugThread.closeQuietly(masterSocket);
        }
    }

    /**
     * Process.
     *
     * @param req
     *            the req
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void process(final ECpdsWaitForGroup req) throws IOException {
        Socket masterSocket = null;
        try {
            masterSocket = new Socket(HOST, PORT);
            _log.debug("Connected to master {}:{}", HOST, PORT);
            final var master = new PrintStream(masterSocket.getOutputStream());
            final var masterIn = new BufferedReader(new InputStreamReader(masterSocket.getInputStream()));
            write(master, "VERSION", req.getVERSION());
            write(master, "USER", req.getUSER());
            write(master, "WAITFORGROUP", req.getWAITFORGROUP());
            final var message = read(masterIn);
            while (!"QUIT".equals(message)) {
                _log.debug(message);
            }
        } finally {
            StreamPlugThread.closeQuietly(masterSocket);
        }
    }

    /**
     * Process.
     *
     * @param req
     *            the req
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void process(final ECpdsSelect req) throws IOException {
        Socket masterSocket = null;
        try {
            masterSocket = new Socket(HOST, PORT);
            _log.debug("Connected to master {}:{}", HOST, PORT);
            final var master = new PrintStream(masterSocket.getOutputStream());
            final var masterIn = new BufferedReader(new InputStreamReader(masterSocket.getInputStream()));
            final var select = req.getSELECT();
            write(master, "VERSION", req.getVERSION());
            write(master, "USER", req.getUSER());
            write(master, "SIZE", select.length());
            write(master, "SELECT");
            write(master, select);
            String message;
            try {
                while ((message = masterIn.readLine()) != null) {
                    _log.debug(message);
                }
            } catch (final SocketException e) {
                // End of the output (connection reset)!
            }
        } finally {
            StreamPlugThread.closeQuietly(masterSocket);
        }
    }

    /**
     * This method is called by the parser when processing the request logs. When possible the remote IP is extracted
     * from the FROM message. If the method is a PUSH and the fake write option is set to true then the target data
     * mover is instructed to simulate the writing on the disk, but no file is created. If the method is a PULL then the
     * file is retrieved as normal!
     *
     * @param req
     *            the req
     * @param in
     *            the in
     * @param fakeWrite
     *            the fake write
     *
     * @return the long
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public static long process(final ECpdsPut req, final InputStream in, final boolean fakeWrite) throws IOException {
        Socket masterSocket = null;
        try {
            masterSocket = new Socket(HOST, PORT);
            _log.debug("Connected to master {}:{}", HOST, PORT);
            final var master = new PrintStream(masterSocket.getOutputStream());
            final var masterIn = new BufferedReader(new InputStreamReader(masterSocket.getInputStream()));
            // The order VERSION, FROM, USER, MESSAGE and REMOTEIP matters!
            write(master, "VERSION", req.getVERSION());
            final var from = req.getFROM();
            write(master, "FROM", from);
            write(master, "USER", req.getUSER());
            read(masterIn, "MESSAGE"); // Welcome!
            final var string = "From the ecpds command at ";
            write(master, "REMOTEIP", from.startsWith(string) ? from.substring(string.length()) : "-");
            write(master, "DESTINATION", req.getDESTINATION());
            write(master, "TIMEFILE", req.getTIMEFILE());
            write(master, "SIZE", req.getSIZE());
            write(master, "AT", req.getAT());
            write(master, "ORIGINAL", req.getORIGINAL());
            write(master, "METADATA", req.getMETADATA());
            write(master, "HOSTFORACQUISITION", req.getHOSTFORACQUISITION());
            write(master, "SOURCE", req.getSOURCE());
            write(master, "UNIQUENAME", req.getUNIQUENAME());
            write(master, "TARGET", req.getTARGET());
            write(master, "STANDBY", req.isSTANDBY());
            write(master, "ASAP", req.isASAP());
            write(master, "EVENT", req.isEVENT());
            write(master, "PRIORITY", req.getPRIORITY());
            write(master, "IDENTITY", req.getIDENTITY());
            write(master, "LIFETIME", req.getLIFETIME());
            write(master, "GROUPBY", req.getGROUPBY());
            write(master, "GROUP", req.getGROUP());
            write(master, "CALLER", req.getCALLER());
            final var index = req.getINDEX();
            if (index != null && index > 0) {
                write(master, "INDEX", index);
            }
            write(master, "NORETRIEVAL", req.isNORETRIEVAL());
            write(master, "FORCE", req.isFORCE());
            write(master, "PUT");
            if (req.getGROUPBY() == null || req.getGROUPBY().length() == 0) {
                // The file must be directly transmitted to the data mover!
                long fileSize = -1;
                final var target = read(masterIn, "TARGET");
                final var ecproxyList = read(masterIn, "ECPROXY");
                String stat = null;
                if (!"Please continue".equals(read(masterIn, ""))) {
                    final var message = "Unexpected reply from master";
                    write(master, "-" + message);
                    throw new IOException(message);
                }
                final var token = new StringTokenizer(ecproxyList, "|");
                while (token.hasMoreElements()) {
                    final var currentEcproxy = token.nextToken();
                    var ecproxyAddress = currentEcproxy;
                    final var pos = ecproxyAddress.indexOf(":");
                    if (pos != -1) {
                        Socket moverSocket = null;
                        try {
                            final var ecproxyPort = Short.parseShort(ecproxyAddress.substring(pos + 1));
                            ecproxyAddress = ecproxyAddress.substring(0, pos);
                            moverSocket = new Socket(ecproxyAddress, ecproxyPort);
                            _log.debug("Connected to mover {}:{}", ecproxyAddress, ecproxyPort);
                            final var mover = new PrintStream(moverSocket.getOutputStream());
                            final var moverIn = new BufferedReader(new InputStreamReader(moverSocket.getInputStream()));
                            write(mover, "ECPDS " + Version.getFullVersion());
                            if (fakeWrite) {
                                write(mover, "OPTS", "ecproxy.fakeWrite=yes");
                            }
                            write(mover, "TARGET", target);
                            read(moverIn, "CONNECT");
                            write(mover, "SIZE", req.getSIZE());
                            _log.debug("Sending file content");
                            fileSize = StreamPlugThread.copy(moverSocket.getOutputStream(), in,
                                    StreamPlugThread.DEFAULT_BUFF_SIZE);
                            if (_log.isDebugEnabled()) {
                                _log.debug("Sent {}", Format.formatSize(fileSize));
                            }
                            // moverSocket.shutdownOutput();
                            stat = read(moverIn, "STAT");
                            read(moverIn, "BYE");
                            write(mover, "BYE");
                            break;
                        } catch (final Throwable t) {
                            // try the next mover in the list
                            _log.debug("Communicating with ecproxy: {}", currentEcproxy, t);
                            throw new IOException(
                                    "Communicating with ecproxy: " + currentEcproxy + ": " + t.getMessage());
                        } finally {
                            StreamPlugThread.closeQuietly(moverSocket);
                        }
                    }
                }
                if (stat != null && fileSize != -1) {
                    write(master, "HOST " + stat);
                    write(master, "SIZE " + fileSize);
                    write(master, "BYE");
                } else {
                    final var message = "Transmission failed to each Data Mover";
                    write(master, "-" + message);
                    throw new IOException(message);
                }
            }
            // Let's get the DataFileId!
            return getDataFileId(read(masterIn, "MESSAGE"));
        } finally {
            StreamPlugThread.closeQuietly(masterSocket);
        }
    }
}
