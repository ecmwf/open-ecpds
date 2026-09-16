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

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.Cnf;

/**
 * The Class GlobeImageryProvisioner.
 *
 * Best-effort, background provisioner for an optional, higher-resolution imagery layer for the "Live Earth" globe
 * visualisation, built from the public-domain Natural Earth "HYP_50M" (hypsometric tints + shaded relief + water, 50m
 * resolution) raster. The bundled low-resolution "NaturalEarthII" imagery shipped with Cesium (a handful of
 * whole-Earth-only zoom levels) looks blurred as soon as a user zooms into a country/region; this class fetches a
 * moderate-resolution source image once, tiles it locally (pure Java, no GDAL/native dependency required - the JDK's
 * bundled {@link ImageIO} TIFF reader is sufficient), and writes the result as a standard TMS tile pyramid (same
 * layout/format as the bundled one) into a local cache directory, so the same
 * {@code Cesium.TileMapServiceImageryProvider} used for the bundled imagery can serve it directly.
 * <p>
 * Deliberately NOT bundled/committed to the source tree (the tiled pyramid is tens of MB even after compression, and
 * the raw source archive is ~100MB): this only ever runs at runtime, on first use (the first "Live Earth" page opened
 * after the Monitor plugin starts), entirely in the background, and never blocks the requesting page (which simply
 * falls back to the bundled low-resolution imagery until - if ever - this completes). If no internet access is
 * available yet, it keeps retrying indefinitely at a fixed interval, so connectivity becoming available only later
 * (e.g. after a temporary network/proxy outage) is still picked up automatically, without a restart.
 * </p>
 */
final class GlobeImageryProvisioner {

    private static final Logger LOG = LogManager.getLogger(GlobeImageryProvisioner.class);

    /** Public, stable Natural Earth download URL for the 50m-resolution hypsometric/relief/water raster. */
    private static final String SOURCE_URL = "https://naturalearth.s3.amazonaws.com/50m_raster/HYP_50M_SR_W.zip";

    /** Name of the GeoTIFF entry inside {@link #SOURCE_URL}'s zip archive. */
    private static final String TIF_ENTRY_NAME = "HYP_50M_SR_W.tif";

    /**
     * Highest TMS zoom level generated. Level {@code n} is {@code 512*2^n} pixels wide, so level 4 (8192px) is the
     * highest level that does not upsample beyond the source raster's native ~10800px width - a good balance between
     * visible improvement over the bundled imagery and both generation time/memory and final size on disk.
     */
    private static final int MAX_ZOOM = 4;

    /** Standard TMS/Cesium imagery tile size, in pixels. */
    private static final int TILE_SIZE = 256;

    /** JPEG quality used when writing tiles (0-1). */
    private static final float JPEG_QUALITY = 0.85f;

    /** How long to wait between provisioning attempts after a failure (e.g. no internet access yet). */
    private static final Duration RETRY_INTERVAL = Duration.ofMinutes(15);

    /** Marker file written (last) once a cache directory is fully/successfully populated. */
    private static final String COMPLETE_MARKER = ".complete";

    /**
     * Minimum JVM max heap (bytes) required before attempting provisioning. Tiling holds the full source raster
     * (~10800x5400 {@code TYPE_INT_RGB}, ~233MB) and, concurrently, the largest resized zoom-level image (level
     * {@value #MAX_ZOOM}, ~134MB) at the same time, on top of whatever the rest of the Monitor plugin (Jetty,
     * WebSockets, RMI, etc.) already uses. On a small heap (e.g. the 512MB default used by the standalone
     * all-in-one image) this reliably triggers an {@link OutOfMemoryError} which, combined with
     * {@code -XX:+CrashOnOutOfMemoryError}, brings down the whole Monitor JVM in a crash loop - unacceptable for a
     * purely optional, best-effort enhancement. Configurable via {@code [Server] globeImageryMinHeapMB}.
     */
    private static final long MIN_HEAP_BYTES = Cnf.at("Server", "globeImageryMinHeapMB", 1024L) * 1024L * 1024L;

    /** Ensures the background provisioning loop is started at most once per JVM. */
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    private GlobeImageryProvisioner() {
        // Utility class.
    }

    /**
     * Gets the local cache directory the tiled pyramid is (or will be) written to, configurable via the
     * {@code [Server] globeImageryCacheDir} option so it can be pointed at a persistent volume (e.g.
     * {@code /var/lib/ecpds/monitor/globe-imagery}) in production; defaults to a subdirectory of the JVM's temp
     * directory, which is fine for development/testing (just re-provisioned on every restart).
     *
     * @return the cache directory
     */
    static File getCacheDirectory() {
        final var configured = Cnf.at("Server", "globeImageryCacheDir",
                System.getProperty("java.io.tmpdir") + File.separator + "ecpds-globe-imagery");
        return new File(configured);
    }

    /**
     * Whether the higher-resolution imagery has already been fully provisioned and is ready to be served.
     *
     * @return true, if successful
     */
    static boolean isReady() {
        return new File(getCacheDirectory(), COMPLETE_MARKER).isFile();
    }

    /**
     * Starts the background provisioning loop if it is not already running and the imagery is not already available.
     * Safe to call repeatedly (e.g. once per "Live Earth" WebSocket connection) - only the very first call actually
     * spawns the background thread. No-op (with a one-off log message) if the JVM's max heap is below
     * {@link #MIN_HEAP_BYTES}, since the bundled low-resolution imagery is always used as a fallback anyway.
     */
    static void ensureStarted() {
        if (isReady() || !STARTED.compareAndSet(false, true)) {
            return;
        }
        final var maxHeap = Runtime.getRuntime().maxMemory();
        if (maxHeap < MIN_HEAP_BYTES) {
            LOG.info(
                    "Skipping higher-resolution globe imagery provisioning: JVM max heap ({}MB) is below the "
                            + "required minimum ({}MB); the bundled low-resolution imagery will be used instead. "
                            + "Increase the heap (e.g. ALLOCATED_MEMORY/MAX_MEMORY) or lower "
                            + "[Server] globeImageryMinHeapMB to enable it.",
                    maxHeap / (1024 * 1024), MIN_HEAP_BYTES / (1024 * 1024));
            return;
        }
        final var worker = new Thread(GlobeImageryProvisioner::_runLoop, "globe-imagery-provisioner");
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
                    LOG.info("Higher-resolution globe imagery provisioned successfully into {}", getCacheDirectory());
                    return;
                }
            } catch (final Throwable t) {
                LOG.debug("Provisioning higher-resolution globe imagery failed, will retry later", t);
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
     * Performs a single end-to-end attempt: download the source archive, extract the raster, tile it into a TMS pyramid
     * under a staging directory, then atomically publish it (rename) so a client never observes a partially-written
     * cache.
     *
     * @return true, if the imagery was successfully downloaded and tiled
     *
     * @throws IOException
     *             if any I/O step fails (caught/retried by the caller)
     */
    private static boolean _provisionOnce() throws IOException {
        final var cacheDir = getCacheDirectory();
        final var stagingDir = new File(cacheDir.getParentFile(), cacheDir.getName() + ".staging");
        _deleteRecursively(stagingDir);
        if (!stagingDir.mkdirs()) {
            throw new IOException("Could not create staging directory: " + stagingDir);
        }
        File zipFile = null;
        File tifFile = null;
        try {
            zipFile = File.createTempFile("globe-imagery-", ".zip");
            _download(SOURCE_URL, zipFile);
            tifFile = File.createTempFile("globe-imagery-", ".tif");
            _extractEntry(zipFile, TIF_ENTRY_NAME, tifFile);
            final var source = ImageIO.read(tifFile);
            if (source == null) {
                throw new IOException("No suitable ImageIO reader found for " + TIF_ENTRY_NAME);
            }
            try {
                for (var zoom = 0; zoom <= MAX_ZOOM; zoom++) {
                    _writeZoomLevel(source, zoom, stagingDir);
                }
            } finally {
                source.flush();
            }
            _writeTileMapResource(stagingDir);
            if (!new File(stagingDir, COMPLETE_MARKER).createNewFile()) {
                throw new IOException("Could not write completion marker in " + stagingDir);
            }
            _deleteRecursively(cacheDir);
            if (!stagingDir.renameTo(cacheDir)) {
                // Cross-filesystem staging/cache dirs (unusual, but possible with a custom configuration): fall back
                // to a plain copy-then-delete.
                _copyRecursively(stagingDir.toPath(), cacheDir.toPath());
                _deleteRecursively(stagingDir);
            }
            return true;
        } finally {
            _deleteRecursively(stagingDir);
            if (zipFile != null) {
                zipFile.delete();
            }
            if (tifFile != null) {
                tifFile.delete();
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
        final var request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofMinutes(5)).GET().build();
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
     * Extracts a single named entry from a zip archive to a target file.
     *
     * @param zipFile
     *            the zip file
     * @param entryName
     *            the entry name
     * @param target
     *            the target
     *
     * @throws IOException
     *             if the entry could not be found/extracted
     */
    private static void _extractEntry(final File zipFile, final String entryName, final File target)
            throws IOException {
        try (final var in = new ZipInputStream(Files.newInputStream(zipFile.toPath()))) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                if (entry.getName().equals(entryName)) {
                    Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    return;
                }
            }
        }
        throw new IOException("Entry " + entryName + " not found in " + zipFile);
    }

    /**
     * Renders one full TMS zoom level from the original source image (always resampled directly from the original,
     * never from a previously-resampled level, to avoid compounding blur), slices it into {@value #TILE_SIZE}x
     * {@value #TILE_SIZE} JPEG tiles, and writes them under {@code <stagingDir>/<zoom>/<x>/<y>.jpg} using the TMS
     * (south-to-north) tile-y convention expected by Cesium's {@code TileMapServiceImageryProvider}.
     *
     * @param source
     *            the full-resolution source image
     * @param zoom
     *            the zoom level
     * @param stagingDir
     *            the staging dir
     *
     * @throws IOException
     *             if a tile could not be written
     */
    private static void _writeZoomLevel(final BufferedImage source, final int zoom, final File stagingDir)
            throws IOException {
        final var tilesWide = 2 << zoom; // 2 * 2^zoom
        final var tilesHigh = 1 << zoom; // 2^zoom
        final var levelWidth = tilesWide * TILE_SIZE;
        final var levelHeight = tilesHigh * TILE_SIZE;
        final var resized = new BufferedImage(levelWidth, levelHeight, BufferedImage.TYPE_INT_RGB);
        final var g = resized.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(source, 0, 0, levelWidth, levelHeight, null);
        } finally {
            g.dispose();
        }
        try {
            final var writer = _newJpegWriter();
            try {
                for (var tx = 0; tx < tilesWide; tx++) {
                    for (var rowFromTop = 0; rowFromTop < tilesHigh; rowFromTop++) {
                        final var tmsY = tilesHigh - 1 - rowFromTop; // TMS: y=0 is the southern-most row.
                        final var tile = resized.getSubimage(tx * TILE_SIZE, rowFromTop * TILE_SIZE, TILE_SIZE,
                                TILE_SIZE);
                        final var dir = new File(stagingDir, zoom + File.separator + tx);
                        if (!dir.isDirectory() && !dir.mkdirs()) {
                            throw new IOException("Could not create tile directory: " + dir);
                        }
                        _writeJpeg(writer, tile, new File(dir, tmsY + ".jpg"));
                    }
                }
            } finally {
                writer.dispose();
            }
        } finally {
            resized.flush();
        }
    }

    /**
     * Creates a reusable JPEG {@link ImageWriter} configured for {@link #JPEG_QUALITY}.
     *
     * @return the image writer
     */
    private static ImageWriter _newJpegWriter() {
        final Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No JPEG ImageWriter available");
        }
        return writers.next();
    }

    /**
     * Writes a single tile image as JPEG with {@link #JPEG_QUALITY} compression.
     *
     * @param writer
     *            the writer
     * @param tile
     *            the tile
     * @param target
     *            the target
     *
     * @throws IOException
     *             if the tile could not be written
     */
    private static void _writeJpeg(final ImageWriter writer, final BufferedImage tile, final File target)
            throws IOException {
        final var params = writer.getDefaultWriteParam();
        if (params.canWriteCompressed()) {
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionQuality(JPEG_QUALITY);
        }
        try (final var ios = new FileImageOutputStream(target)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(tile, null, null), params);
        } finally {
            writer.reset();
        }
    }

    /**
     * Writes the {@code tilemapresource.xml} descriptor, in the same TMS "geodetic" profile format Cesium's
     * {@code TileMapServiceImageryProvider} already parses for the bundled low-resolution imagery.
     *
     * @param stagingDir
     *            the staging dir
     *
     * @throws IOException
     *             if the descriptor could not be written
     */
    private static void _writeTileMapResource(final File stagingDir) throws IOException {
        final var sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        sb.append("<TileMap version=\"1.0.0\" tilemapservice=\"http://tms.osgeo.org/1.0.0\">\n");
        sb.append("  <Title>HYP_50M_SR_W (Natural Earth, 50m)</Title>\n");
        sb.append("  <Abstract></Abstract>\n");
        sb.append("  <SRS>EPSG:4326</SRS>\n");
        sb.append(
                "  <BoundingBox miny=\"-90.00000000000000\" minx=\"-180.00000000000000\" maxy=\"90.00000000000000\" maxx=\"180.00000000000000\"/>\n");
        sb.append("  <Origin y=\"-90.00000000000000\" x=\"-180.00000000000000\"/>\n");
        sb.append("  <TileFormat width=\"256\" height=\"256\" mime-type=\"image/jpg\" extension=\"jpg\"/>\n");
        sb.append("  <TileSets profile=\"geodetic\">\n");
        for (var zoom = 0; zoom <= MAX_ZOOM; zoom++) {
            final var unitsPerPixel = 180.0 / (TILE_SIZE * (1 << zoom));
            sb.append("    <TileSet href=\"").append(zoom).append("\" units-per-pixel=\"")
                    .append(String.format("%.14f", unitsPerPixel)).append("\" order=\"").append(zoom).append("\"/>\n");
        }
        sb.append("  </TileSets>\n");
        sb.append("</TileMap>\n");
        Files.writeString(new File(stagingDir, "tilemapresource.xml").toPath(), sb.toString());
    }

    /**
     * Recursively deletes a file/directory tree, if it exists. Best-effort: I/O errors are swallowed since this is only
     * ever used to clean up temporary/staging content.
     *
     * @param file
     *            the file
     */
    private static void _deleteRecursively(final File file) {
        if (file == null || !file.exists()) {
            return;
        }
        final var children = file.listFiles();
        if (children != null) {
            for (final File child : children) {
                _deleteRecursively(child);
            }
        }
        file.delete();
    }

    /**
     * Recursively copies a directory tree (fallback for when the staging and final cache directories are not on the
     * same filesystem, so a plain rename is not possible).
     *
     * @param source
     *            the source
     * @param target
     *            the target
     *
     * @throws IOException
     *             if the copy fails
     */
    private static void _copyRecursively(final Path source, final Path target) throws IOException {
        try (final var stream = Files.walk(source)) {
            for (final Path path : (Iterable<Path>) stream::iterator) {
                final var destination = target.resolve(source.relativize(path));
                if (Files.isDirectory(path)) {
                    Files.createDirectories(destination);
                } else {
                    Files.createDirectories(destination.getParent());
                    Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
}
