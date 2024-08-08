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

package ecmwf.common.ftp;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.parser.DefaultFTPFileEntryParserFactory;
import org.apache.commons.net.ftp.parser.ParserInitializationException;
import org.apache.commons.net.ftp.parser.RegexFTPFileEntryParserImpl;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import ecmwf.common.text.Format;

/**
 * The Class FtpParser.
 */
public final class FtpParser {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(FtpParser.class);

    /** The Constant REMOTE_OS. */
    public static final String[] REMOTE_OS = { "UNIX", "VMS", "WINDOWS", "OS/2", "OS/400", "AS/400", "MVS", "NETWARE",
            "MACOS PETER" };

    /** The Constant FILE_TYPE. */
    public static final int FILE_TYPE = 0;

    /** The Constant DIRECTORY_TYPE. */
    public static final int DIRECTORY_TYPE = 1;

    /** The Constant SYMBOLIC_LINK_TYPE. */
    public static final int SYMBOLIC_LINK_TYPE = 2;

    /** The Constant UNKNOWN_TYPE. */
    public static final int UNKNOWN_TYPE = 3;

    /**
     * The Class FileEntry.
     */
    public static final class FileEntry {
        /** The exception. */
        public Throwable exception = null;

        /** The type. */
        public int type = -1;

        /** The permissions. */
        public String permissions = null;

        /** The line. */
        public String line = null;

        /** The name. */
        public String name = null;

        /** The user. */
        public String user = null;

        /** The group. */
        public String group = null;

        /** The link. */
        public String link = null;

        /** The size. */
        public long size = -1;

        /** The time. */
        public long time = -1;

        /**
         * To string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            return "[line=" + line + ",permissions=" + permissions + ",type=" + type + ",name=" + name + ",user=" + user
                    + ",group=" + group + ",link=" + link + ",size=" + size + ",time=" + time + "]";
        }
    }

    /**
     * Parses the dir.
     *
     * @param regexFormat
     *            the regex format
     * @param systemKey
     *            the system key
     * @param defaultDateFormat
     *            the default date format
     * @param recentDateFormat
     *            the recent date format
     * @param serverLanguageCode
     *            the server language code
     * @param shortMonthNames
     *            the short month names
     * @param serverTimeZoneId
     *            the server time zone id
     * @param filenames
     *            the filenames
     *
     * @return the file entry[]
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static FileEntry[] parseDir(final String regexFormat, final String systemKey, final String defaultDateFormat,
            final String recentDateFormat, final String serverLanguageCode, final String shortMonthNames,
            final String serverTimeZoneId, final String[] filenames) throws IOException {
        final FTPFileEntryParser parser;
        try {
            parser = new DefaultFTPFileEntryParserFactory().createFileEntryParser(new FTPClientConfig(systemKey,
                    defaultDateFormat, recentDateFormat, serverLanguageCode, shortMonthNames, serverTimeZoneId));
            if (regexFormat != null && parser instanceof final RegexFTPFileEntryParserImpl regexParser) {
                regexParser.setRegex(regexFormat);
            }
        } catch (final ParserInitializationException e) {
            _log.warn("Parser not initialized", e);
            throw new IOException("Parser not initialized");
        }
        final List<FileEntry> result = new ArrayList<>();
        if (filenames != null) {
            final List<String> mutableList = new ArrayList<>(Arrays.asList(filenames));
            for (final String readLine : parser.preParse(mutableList)) {
                final var entry = new FileEntry();
                entry.line = Format.cleanTextContent(readLine);
                try {
                    // Let's remove the "." after the permission if does exists,
                    // otherwise the parsing of the ftp line would not work!
                    if (entry.line.length() > 10 && entry.line.charAt(10) == '.') {
                        entry.line = entry.line.substring(0, 10) + entry.line.substring(11);
                    }
                    final var file = parser.parseFTPEntry(entry.line);
                    // Just try to set the permissions, if we can't then we
                    // continue without it!
                    try {
                        entry.permissions = file.toFormattedString().substring(0, 10);
                    } catch (final Throwable t) {
                    }
                    entry.type = file.getType();
                    entry.user = file.getUser();
                    entry.group = file.getGroup();
                    entry.size = file.getSize();
                    entry.link = file.getLink();
                    entry.name = file.getName();
                    final var cal = file.getTimestamp();
                    if (cal != null) {
                        entry.time = cal.getTimeInMillis();
                    }
                } catch (final NullPointerException e) {
                    _log.warn("Could not parse: {}", entry);
                    entry.exception = new IOException("error parsing " + systemKey + " entry");
                } catch (final Throwable t) {
                    _log.warn("Could not parse: {}", entry, t);
                    entry.exception = t;
                }
                result.add(entry);
            }
        }
        // Only sort if we have more than one entry!
        if (result.size() > 1) {
            Collections.sort(result, new FileEntryComparator());
        }
        return result.toArray(new FileEntry[result.size()]);
    }

    /**
     * The Class FileEntryComparator.
     */
    private static final class FileEntryComparator implements Comparator<FileEntry> {

        /**
         * Compare.
         *
         * @param f1
         *            the f 1
         * @param f2
         *            the f 2
         *
         * @return the int
         */
        @Override
        public int compare(final FileEntry f1, final FileEntry f2) {
            return Long.compare(f1.time, f2.time);
        }
    }

    /**
     * The main method.
     *
     * @param args
     *            the arguments
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void main(final String[] args) throws IOException {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.ALL);
        final var lines = new String[] {
                "-rw-r--r--. 1 sapp root 394710 Sep  4 00:05 /sapp/outbox/wmo/out/SYNA0001_202309032100_180.DAT",
                "-rw-r-----    1 14467    10080    1435913256 Sept 03 19:01 wave_NIWA_2023090312_prod_fc.grib2",
                "-rw-r-----    1 14467    10080    1443895109 Sep 02 06:19 wave_NIWA_2023090200_prod_fc.grib2" };
        final var entries = parseDir(null, "UNIX", null, null, "en", null, null, lines);
        if (_log.isInfoEnabled()) {
            for (final FileEntry entry : entries) {
                _log.info("{}", entry);
            }
            for (final FileEntry entry : entries) {
                _log.info("{}", Format.getFtpList(entry.permissions, entry.user, entry.group, "" + entry.size,
                        entry.time, entry.name));
            }
        }
    }
}
