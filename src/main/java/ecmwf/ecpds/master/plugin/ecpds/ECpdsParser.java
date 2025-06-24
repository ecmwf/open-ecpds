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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.ecaccess.StorageRepository;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.ThreadService.ConfigurableLoopRunnable;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.plugin.ecpds.request.ECpdsRequest;

/**
 * The Class ECpdsParser.
 */
final class ECpdsParser extends ConfigurableLoopRunnable {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ECpdsParser.class);

    /** Pause between directory scanning. */
    private static final long PAUSE = 5 * Timer.ONE_SECOND;

    /** Repository to store the ecpds requests in a queue. */
    private final ParserRepository repository;

    /** Last modified time of the last file processed. */
    private Long lastRecordedModified = null;

    /** Last time stamp of the last file entry processed. */
    private Long lastRecorderTimeStamp = null;

    /** Directory where to look for the files. */
    private final String repositoryDir;

    /** Shall we delete a file once it is successfully processed?. */
    private final boolean cleanOnSuccess;

    /**
     * Delay the initial startup to give time to the ECpdsPlugin to start fully.
     */
    private long initialDelay = 30 * Timer.ONE_SECOND;

    /**
     * Define how faster we want to go! (1 is similar, 2 is 2 times faster, 0.5 is 2 times slower ...).
     */
    private final long speedRatio;

    /** Time gained after every new request if the ratio is not 1. */
    private long timeGained = 0;

    /**
     * Instantiates a new ecpds parser.
     *
     * @param lastTimeStamp
     *            the last time stamp
     * @param ratio
     *            the ratio
     * @param dir
     *            the dir
     * @param clean
     *            the clean
     */
    public ECpdsParser(final long lastTimeStamp, final long ratio, final String dir, final boolean clean) {
        // Initialise the repository to store and process the ecpds request
        _log.debug("Starting the RequestParser");
        repository = new ParserRepository("ParserRepository");
        setPriority(Thread.MIN_PRIORITY);
        if (lastTimeStamp > 0) {
            lastRecorderTimeStamp = lastTimeStamp;
        }
        speedRatio = ratio;
        repositoryDir = dir;
        cleanOnSuccess = clean;
        // Start the Thread to parse the target directory on a regular basis
        setPause(PAUSE);
        execute();
    }

    /**
     * Configurable loop run.
     */
    @Override
    public void configurableLoopRun() {
        // Make sure the ECpdsPlugin is ready to receive a request if this is the first
        // run!
        if (initialDelay != -1) {
            try {
                Thread.sleep(initialDelay);
            } catch (final InterruptedException _) {
                Thread.currentThread().interrupt();
            } finally {
                initialDelay = -1;
            }
        }
        final var dirPath = Paths.get(repositoryDir);
        final var filesToProcess = new ArrayList<File>();
        // Use DirectoryStream to avoid native memory leaks
        try (var stream = Files.newDirectoryStream(dirPath, path -> {
            final var name = path.getFileName().toString().toLowerCase();
            return !(name.endsWith(".tmp") || name.endsWith(".temp") || name.startsWith("."));
        })) {
            for (final Path path : stream) {
                final var file = path.toFile();
                if (file.isFile()) {
                    filesToProcess.add(file);
                }
            }
        } catch (final IOException e) {
            _log.warn("Failed to list files in directory: {}", repositoryDir, e);
        }
        // Sort files using the same comparator
        Collections.sort(filesToProcess, new FileComparator());
        for (final File file : filesToProcess) {
            try {
                lastRecordedModified = processFile(lastRecordedModified, file);
            } catch (final Throwable t) {
                _log.warn("Couldn't process file: {}", file.getAbsolutePath(), t);
            }
        }
    }

    /**
     * Process.
     *
     * @param lastModified
     *            the last modified
     * @param file
     *            the file
     *
     * @return the long
     *
     * @throws Exception
     *             the exception
     */
    public long processFile(final Long lastModified, final File file) throws Exception {
        var result = lastModified;
        var index = 0;
        if (lastModified == null || file.lastModified() > lastModified) {
            // First file to process or file older than the previous file processed!
            _log.debug("Processing file: {}", file.getAbsolutePath());
            try (final var br = new BufferedReader(new FileReader(file))) {
                String currentLine;
                StringBuilder previousLine = null;
                result = file.lastModified();
                while ((currentLine = br.readLine()) != null) {
                    if (currentLine.contains("[TYPE]") && currentLine.contains("[TIMESTAMP]")) {
                        // Let's process the previous line
                        if (previousLine != null) {
                            lastRecorderTimeStamp = processEntry(lastRecorderTimeStamp, previousLine.toString());
                            index++;
                        }
                        // Start a new line
                        previousLine = new StringBuilder(currentLine);
                    } else if (previousLine != null) {
                        previousLine.append(" ").append(currentLine.trim());
                    }
                }
                // Last line!
                if (previousLine != null) {
                    lastRecorderTimeStamp = processEntry(lastRecorderTimeStamp, previousLine.toString());
                    index++;
                }
            }
            final var cleaned = cleanOnSuccess && file.delete();
            _log.debug("File {} completed ({} line(s),{})", file.getAbsolutePath(), index,
                    cleaned ? "deleted" : "not-deleted");
        }
        return result;
    }

    /**
     * Process new entry.
     *
     * @param lastTimeStamp
     *            the last time stamp
     * @param line
     *            the line
     *
     * @return the long
     */
    private long processEntry(final Long lastTimeStamp, final String line) {
        // Let's convert the line into an ECpdsRequest object and push it in the
        // queue of the repository for later processing
        final var types = new String[] { "Completed", "Expected", "Put", "Select", "Started", "WaitForGroup" };
        var result = lastTimeStamp;
        for (final String type : types) {
            if (line.indexOf("[TYPE] " + type.toUpperCase()) != -1) {
                try {
                    if (Class.forName("ecmwf.ecpds.master.plugin.ecpds.request.ECpds" + type)
                            .getConstructor(String.class).newInstance(line) instanceof final ECpdsRequest req) {
                        var currentTimestamp = req.getTIMESTAMP() - timeGained;
                        if (lastTimeStamp != null) {
                            final var elapsedTime = currentTimestamp - lastTimeStamp;
                            final var delay = elapsedTime / speedRatio;
                            if (delay > 0) {
                                if (_log.isDebugEnabled())
                                    _log.debug("Next request queued in {} ({})", Format.formatDuration(delay), type);
                                // Let's wait to have the same delay between the
                                // previous request and the current one as on the
                                // operational system
                                timeGained += elapsedTime - delay;
                                currentTimestamp = lastTimeStamp + delay;
                                req.setTIMESTAMP(currentTimestamp);
                                Thread.sleep(delay);
                            } else if (delay < 0) {
                                if (_log.isDebugEnabled())
                                    _log.debug("Request NOT queued (expired by {})", Format.formatDuration(-1 * delay));
                                return lastTimeStamp;
                            }
                        }
                        result = currentTimestamp;
                        // Store it in the queue of the repository!
                        repository.put(req);
                    }
                } catch (final Throwable t) {
                    _log.warn("Could not process line: {}", line, t);
                }
                break;
            }
        }
        return result;
    }

    /**
     * The Class FileComparator.
     */
    private static final class FileComparator implements Comparator<File> {

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
        public int compare(final File f1, final File f2) {
            return Long.compare(f1.lastModified(), f2.lastModified());
        }
    }

    /**
     * The Class ECpdsRequestComparator.
     */
    private static final class ECpdsRequestComparator implements Comparator<ECpdsRequest> {

        /**
         * Compare.
         *
         * @param req1
         *            the req 1
         * @param req2
         *            the req 2
         *
         * @return the int
         */
        @Override
        public int compare(final ECpdsRequest req1, final ECpdsRequest req2) {
            return Long.compare(req1.getTIMESTAMP(), req2.getTIMESTAMP());
        }
    }

    /**
     * The Class ParserRepository.
     */
    private final class ParserRepository extends StorageRepository<ECpdsRequest> {

        /**
         * Put.
         *
         * @param object
         *            the object
         */
        @Override
        public void put(final ECpdsRequest object) {
            // Make sure the repository is already started!
            synchronized (ParserRepository.this) {
                if (getStartDate() == null) {
                    _log.debug("Starting the ParserRepository");
                    start();
                }
            }
            // Put the request in the queue
            super.put(object);
        }

        /**
         * Instantiates a new parser repository.
         *
         * @param name
         *            the name
         */
        private ParserRepository(final String name) {
            super(name, Cnf.at("RequestParser", "requestSize", 25),
                    Cnf.at("RequestParser", "requestDelay", Timer.ONE_SECOND));
            setComparator(new ECpdsRequestComparator());
            setMaxAuthorisedSize(Cnf.at("RequestParser", "maxAuthorisedSize", 1000));
        }

        /**
         * Update.
         *
         * @param request
         *            the request
         *
         * @throws Exception
         *             the exception
         */
        @Override
        public void update(final ECpdsRequest request) throws Exception {
            _log.debug("Processing request (lastTimeStamp={}): {}", request.getTIMESTAMP(), request);
            try {
                request.process(speedRatio);
            } catch (final Throwable t) {
                _log.warn("Couldn't process request", t);
            }
        }
    }
}