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

package ecmwf.ecpds.master.plugin.http;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 7.4.0
 * @since 2026-09-12
 */

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ecmwf.common.technical.Cnf;

/**
 * The Class GlobeLabelsProvisioner.
 *
 * Best-effort, background provisioner for an optional country/city name label layer for the "Live Earth" globe
 * visualisation, built from the public-domain Natural Earth "110m cultural" vector datasets (country boundaries and
 * populated places). Only the small attribute tables (dBase III {@code .dbf} files bundled inside each dataset's
 * shapefile archive) are needed - both already carry ready-to-use label coordinates (country {@code LABEL_X}/
 * {@code LABEL_Y}, and populated-place {@code latitude}/{@code longitude}) alongside the name, so no shapefile geometry
 * parsing, projection maths, or third-party GIS library is required, just a small hand-rolled DBF reader.
 * <p>
 * Deliberately NOT bundled/committed to the source tree (kept consistent with {@link GlobeImageryProvisioner}): fetched
 * once at runtime, on first use, entirely in the background, retried indefinitely if it fails (e.g. no internet access
 * yet). The frontend fetches the resulting small JSON file directly and never blocks on it - country and place labels
 * simply appear once (if ever) it becomes available.
 * </p>
 */
final class GlobeLabelsProvisioner {

    private static final Logger LOG = LogManager.getLogger(GlobeLabelsProvisioner.class);

    /** Public, stable Natural Earth download URLs for the 110m-resolution cultural vector datasets. */
    private static final String COUNTRIES_URL = "https://naturalearth.s3.amazonaws.com/110m_cultural/ne_110m_admin_0_countries.zip";

    private static final String PLACES_URL = "https://naturalearth.s3.amazonaws.com/110m_cultural/ne_110m_populated_places_simple.zip";

    /** Name of the DBF (attribute table) entry inside each of the above zip archives. */
    private static final String COUNTRIES_DBF_ENTRY = "ne_110m_admin_0_countries.dbf";

    private static final String PLACES_DBF_ENTRY = "ne_110m_populated_places_simple.dbf";

    /** Name of the generated labels file, written into the cache directory. */
    private static final String LABELS_FILE = "labels.json";

    /** How long to wait between provisioning attempts after a failure (e.g. no internet access yet). */
    private static final Duration RETRY_INTERVAL = Duration.ofMinutes(15);

    /** Ensures the background provisioning loop is started at most once per JVM. */
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    private static final ObjectMapper JSON = new ObjectMapper();

    private GlobeLabelsProvisioner() {
        // Utility class.
    }

    /**
     * Gets the local cache directory the generated {@value #LABELS_FILE} is (or will be) written to, configurable via
     * the {@code [Server] globeLabelsCacheDir} option so it can be pointed at a persistent volume in production;
     * defaults to a subdirectory of the JVM's temp directory, which is fine for development/testing (just
     * re-provisioned on every restart).
     *
     * @return the cache directory
     */
    static File getCacheDirectory() {
        final var configured = Cnf.at("Server", "globeLabelsCacheDir",
                System.getProperty("java.io.tmpdir") + File.separator + "ecpds-globe-labels");
        return new File(configured);
    }

    /**
     * Whether the labels file has already been fully provisioned and is ready to be served.
     *
     * @return true, if successful
     */
    static boolean isReady() {
        return new File(getCacheDirectory(), LABELS_FILE).isFile();
    }

    /**
     * Starts the background provisioning loop if it is not already running and the labels are not already available.
     * Safe to call repeatedly (e.g. once per "Live Earth" WebSocket connection) - only the very first call actually
     * spawns the background thread.
     */
    static void ensureStarted() {
        if (isReady() || !STARTED.compareAndSet(false, true)) {
            return;
        }
        final var worker = new Thread(GlobeLabelsProvisioner::_runLoop, "globe-labels-provisioner");
        worker.setDaemon(true);
        worker.start();
    }

    /**
     * Retries {@link #_provisionOnce()} indefinitely (at {@link #RETRY_INTERVAL}) until it succeeds.
     */
    private static void _runLoop() {
        while (true) {
            try {
                if (_provisionOnce()) {
                    LOG.info("Globe country/city labels provisioned successfully into {}", getCacheDirectory());
                    return;
                }
            } catch (final Throwable t) {
                LOG.debug("Provisioning globe country/city labels failed, will retry later", t);
            }
            try {
                Thread.sleep(RETRY_INTERVAL.toMillis());
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /**
     * Performs a single end-to-end attempt: download both source archives, extract/parse their DBF attribute tables,
     * build the combined labels JSON, then atomically publish it (write to a temp file, then rename) so a client never
     * observes a partially-written file.
     *
     * @return true, if the labels were successfully downloaded and written
     *
     * @throws IOException
     *             if any I/O step fails (caught/retried by the caller)
     */
    private static boolean _provisionOnce() throws IOException {
        final var cacheDir = getCacheDirectory();
        if (!cacheDir.isDirectory() && !cacheDir.mkdirs()) {
            throw new IOException("Could not create cache directory: " + cacheDir);
        }
        File countriesZip = null;
        File placesZip = null;
        try {
            countriesZip = File.createTempFile("globe-labels-countries-", ".zip");
            _download(COUNTRIES_URL, countriesZip);
            placesZip = File.createTempFile("globe-labels-places-", ".zip");
            _download(PLACES_URL, placesZip);

            final var countries = _readDbf(countriesZip, COUNTRIES_DBF_ENTRY);
            final var places = _readDbf(placesZip, PLACES_DBF_ENTRY);

            final var root = JSON.createObjectNode();
            final var countriesNode = root.putArray("countries");
            for (final var record : countries) {
                final var name = record.get("NAME");
                final var lon = _parseDouble(record.get("LABEL_X"));
                final var lat = _parseDouble(record.get("LABEL_Y"));
                if (name == null || name.isBlank() || lon == null || lat == null) {
                    continue;
                }
                final var node = countriesNode.addObject();
                node.put("name", name.trim());
                node.put("lon", lon);
                node.put("lat", lat);
            }
            final var placesNode = root.putArray("places");
            for (final var record : places) {
                final var name = record.get("name");
                final var lon = _parseDouble(record.get("longitude"));
                final var lat = _parseDouble(record.get("latitude"));
                if (name == null || name.isBlank() || lon == null || lat == null) {
                    continue;
                }
                final var node = placesNode.addObject();
                node.put("name", name.trim());
                node.put("lon", lon);
                node.put("lat", lat);
                final var popMax = _parseDouble(record.get("pop_max"));
                node.put("pop", popMax == null ? 0 : popMax.longValue());
            }
            _writeLabelsFile(cacheDir, root);
            return true;
        } finally {
            if (countriesZip != null) {
                countriesZip.delete();
            }
            if (placesZip != null) {
                placesZip.delete();
            }
        }
    }

    /**
     * Downloads a URL to a local file.
     *
     * @param url
     *            the url
     * @param target
     *            the target
     *
     * @throws IOException
     *             if the download fails
     */
    private static void _download(final String url, final File target) throws IOException {
        final var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();
        final var request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofMinutes(2)).GET().build();
        try {
            final var response = client.send(request,
                    HttpResponse.BodyHandlers.ofFile(target.toPath(), java.nio.file.StandardOpenOption.CREATE,
                            java.nio.file.StandardOpenOption.WRITE,
                            java.nio.file.StandardOpenOption.TRUNCATE_EXISTING));
            if (response.statusCode() != 200) {
                throw new IOException("Unexpected HTTP status " + response.statusCode() + " downloading " + url);
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while downloading " + url, e);
        }
    }

    /**
     * Extracts a named DBF entry from a zip archive and parses it in full.
     *
     * @param zipFile
     *            the zip file
     * @param entryName
     *            the entry name
     *
     * @return the list of records, each a field-name to value map
     *
     * @throws IOException
     *             if the entry could not be found/parsed
     */
    private static List<java.util.Map<String, String>> _readDbf(final File zipFile, final String entryName)
            throws IOException {
        try (final var in = new ZipInputStream(Files.newInputStream(zipFile.toPath()))) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                if (entry.getName().equals(entryName)) {
                    return _parseDbf(in.readAllBytes());
                }
            }
        }
        throw new IOException("Entry " + entryName + " not found in " + zipFile);
    }

    /**
     * Parses a dBase III ({@code .dbf}) attribute table into a list of field-name to trimmed-string-value maps. Only
     * the small subset of the format actually produced by Natural Earth's shapefiles is supported: a 32-byte header,
     * followed by one 32-byte field descriptor per column (terminated by a {@code 0x0D} byte), followed by fixed-length
     * records (each starting with a 1-byte deletion flag, then the field values in the same order as the descriptors,
     * stored as plain ASCII/Latin-1 text - including numeric fields, which DBF always stores as text).
     *
     * @param data
     *            the raw file bytes
     *
     * @return the parsed records
     */
    private static List<java.util.Map<String, String>> _parseDbf(final byte[] data) {
        final var numRecords = _readLeInt(data, 4);
        final var headerSize = _readLeShort(data, 8);
        final var recordSize = _readLeShort(data, 10);
        final var fieldNames = new ArrayList<String>();
        final var fieldLengths = new ArrayList<Integer>();
        var pos = 32;
        while (pos < headerSize - 1 && data[pos] != 0x0D) {
            var nameEnd = pos;
            while (nameEnd < pos + 11 && data[nameEnd] != 0) {
                nameEnd++;
            }
            fieldNames.add(new String(data, pos, nameEnd - pos, StandardCharsets.ISO_8859_1));
            fieldLengths.add(data[pos + 16] & 0xFF);
            pos += 32;
        }
        final var records = new ArrayList<java.util.Map<String, String>>(numRecords);
        var recordStart = headerSize;
        for (var i = 0; i < numRecords && recordStart + recordSize <= data.length; i++) {
            var fieldStart = recordStart + 1; // Skip the 1-byte deletion flag.
            final var record = new java.util.HashMap<String, String>();
            for (var f = 0; f < fieldNames.size(); f++) {
                final var length = fieldLengths.get(f);
                final var value = new String(data, fieldStart, length, StandardCharsets.UTF_8).trim();
                record.put(fieldNames.get(f), value);
                fieldStart += length;
            }
            records.add(record);
            recordStart += recordSize;
        }
        return records;
    }

    private static int _readLeInt(final byte[] data, final int offset) {
        return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8) | ((data[offset + 2] & 0xFF) << 16)
                | ((data[offset + 3] & 0xFF) << 24);
    }

    private static int _readLeShort(final byte[] data, final int offset) {
        return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8);
    }

    /**
     * Parses a trimmed DBF numeric-field string value into a Double, tolerating blank/missing values.
     *
     * @param value
     *            the value
     *
     * @return the parsed value, or null if blank/unparsable
     */
    private static Double _parseDouble(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.valueOf(value.trim());
        } catch (final NumberFormatException e) {
            return null;
        }
    }

    /**
     * Writes the labels JSON to a temp file in the cache directory, then atomically renames it into place, so a
     * concurrent reader never observes a partially-written file.
     *
     * @param cacheDir
     *            the cache dir
     * @param root
     *            the root json node
     *
     * @throws IOException
     *             if the file could not be written
     */
    private static void _writeLabelsFile(final File cacheDir, final ObjectNode root) throws IOException {
        final var target = new File(cacheDir, LABELS_FILE);
        final var tmp = new File(cacheDir, LABELS_FILE + ".tmp");
        Files.writeString(tmp.toPath(), JSON.writeValueAsString(root), StandardCharsets.UTF_8);
        Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }
}
