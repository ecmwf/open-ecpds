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

package ecmwf.ecpds.mover;

import java.io.IOException;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.GenericFile;
import ecmwf.common.technical.GenericFileChecker;

/**
 * The Class FileChecker.
 */
final class FileChecker extends GenericFileChecker {
    /** The Constant _fs. */
    private static final Logger _fs = LogManager.getLogger("FileSystemLogs");

    /** The lastException. */
    private static boolean lastException = false;

    /** The lastExceptionTime. */
    private static long lastExceptionTime = -1;

    /** The lastExceptionUpdateTime. */
    private static long lastExceptionUpdateTime = -1;

    /** The path. */
    public static String path = null;

    /**
     * Checks for exception.
     *
     * @return true, if successful
     */
    protected static synchronized boolean hasException() {
        if (lastException && System.currentTimeMillis() - lastExceptionTime > 5 * Timer.ONE_MINUTE) {
            lastException = false;
        }
        return lastException && System.currentTimeMillis() - lastExceptionUpdateTime > 6 * Timer.ONE_MINUTE;
    }

    /**
     * Sets the exception.
     *
     * @param currentPath
     *            the current path
     */
    private static synchronized void setException(final String currentPath) {
        lastExceptionTime = System.currentTimeMillis();
        if (!lastException) {
            lastExceptionUpdateTime = System.currentTimeMillis();
            lastException = true;
        }
        path = currentPath;
    }

    /**
     * Instantiates a new file checker.
     *
     * @param file
     *            the file
     */
    FileChecker(final GenericFile file) {
        super(file);
    }

    /**
     * Catch exception.
     *
     * @param service
     *            the service
     * @param t
     *            the t
     */
    @Override
    public void catchException(final String service, final Throwable t) {
        if (t instanceof IOException && "Stream Closed".equals(t.getMessage())) {
            // This is because the stream was closed and does not indicate a
            // problem with the filesystem so we should ignore it (this can
            // happen if the transmission is interrupted).
            return;
        }
        final var currentPath = super.getGenericFile().getAbsolutePath();
        _fs.info("Executing {} on {}", service, currentPath, t);
        if (t instanceof IOException && "InputStream.read".equals(service)
                && "Input/output error".equals(t.getMessage())) {
            MoverServer._log.warn("Deleting file: {} (Input/output error)", currentPath);
            // TODO: don't delete the file before doing a better checking of
            // the file (it might be a temporary error)!
            try {
                delete();
            } catch (final Throwable e) {
                MoverServer._log.debug(e);
            }
        }
        setException(currentPath);
    }

    /**
     * Gets the generic file.
     *
     * @return the generic file
     */
    @Override
    public GenericFile getGenericFile() {
        return new FileChecker(super.getGenericFile());
    }

    /**
     * Gets the parent file.
     *
     * @return the parent file
     */
    @Override
    public GenericFile getParentFile() {
        return new FileChecker(super.getParentFile());
    }
}
