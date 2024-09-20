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

import static ecmwf.common.text.Util.isNotEmpty;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.ECtransDestination;
import ecmwf.common.database.ECtransHistory;
import ecmwf.common.database.ECtransModule;
import ecmwf.common.database.MSUser;
import ecmwf.common.starter.StarterLoader;
import ecmwf.common.technical.Cnf;

/**
 * The Class RemoteProvider.
 */
public abstract class RemoteProvider {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(RemoteProvider.class);

    /** The Constant _loaders. */
    private static final Map<String, LoaderEntry> _loaders = new HashMap<>();

    /** The Constant _defaultLoader. */
    private static final LoaderEntry _defaultLoader = new LoaderEntry();

    /**
     * Gets the root.
     *
     * @return the root
     */
    public abstract String getRoot();

    /**
     * Update ms user.
     *
     * @param msuser
     *            the msuser
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract void updateMSUser(MSUser msuser) throws IOException;

    /**
     * Gets the ectrans destination.
     *
     * @param name
     *            the name
     *
     * @return the ectrans destination
     */
    public abstract ECtransDestination getECtransDestination(String name);

    /**
     * On close.
     *
     * @param history
     *            the history
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void onClose(final ECtransHistory history) throws IOException {
    }

    /**
     * Encrypt.
     *
     * @param data
     *            the data
     *
     * @return the string
     */
    public String encrypt(final String data) {
        return data;
    }

    /**
     * Decrypt.
     *
     * @param data
     *            the data
     *
     * @return the string
     */
    public String decrypt(final String data) {
        return data;
    }

    /**
     * Gets the allocate interface.
     *
     * @param url
     *            the url
     * @param properties
     *            the properties
     *
     * @return the allocate interface
     *
     * @throws Exception
     *             the exception
     */
    public AllocateInterface getAllocateInterface(final String url, final Properties properties) throws Exception {
        throw new IOException("Not implemented");
    }

    /**
     * Gets the notification interface.
     *
     * @param url
     *            the url
     * @param name
     *            the name
     * @param password
     *            the password
     *
     * @return the notification interface
     *
     * @throws Exception
     *             the exception
     */
    public NotificationInterface getNotificationInterface(final String url, final String name, final String password)
            throws Exception {
        throw new IOException("Not implemented");
    }

    /**
     * Load transfer module.
     *
     * @param module
     *            the module
     *
     * @return the transfer module
     *
     * @throws Exception
     *             the exception
     */
    public TransferModule loadTransferModule(final ECtransModule module) throws Exception {
        final var archive = Cnf.getValue(module.getArchive());
        final LoaderEntry entry;
        if (isNotEmpty(archive)) {
            synchronized (_loaders) {
                final var existingEntry = _loaders.get(archive);
                if (existingEntry == null || existingEntry.isExpired()) {
                    _loaders.put(archive, entry = new LoaderEntry(archive));
                    _log.debug("Loading module " + module.getName() + " from archive " + archive);
                } else {
                    entry = existingEntry;
                    _log.debug("Loading module " + module.getName() + " from cache");
                }
            }
        } else {
            // No specific class loader!
            entry = _defaultLoader;
        }
        return entry.getTransferModule(module.getClasse());
    }

    /**
     * Unload transfer module.
     *
     * @param module
     *            the module
     */
    public void unloadTransferModule(final TransferModule module) {
        _log.debug("Unloading module " + module.getECtransModule().getName());
    }

    /**
     * Gets the data input stream.
     *
     * @param ticket
     *            the ticket
     *
     * @return the data input stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract InputStream getDataInputStream(Object ticket) throws IOException;

    /**
     * Gets the data output stream.
     *
     * @param ticket
     *            the ticket
     *
     * @return the data output stream
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public abstract OutputStream getDataOutputStream(Object ticket) throws IOException;

    /**
     * Gets the data input file.
     *
     * @param ticket
     *            the ticket
     *
     * @return the data input file
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public File getDataInputFile(final Object ticket) throws IOException {
        throw new IOException("Not implemented");
    }

    /**
     * Gets the data output file.
     *
     * @param ticket
     *            the ticket
     *
     * @return the data output file
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public File getDataOutputFile(final Object ticket) throws IOException {
        throw new IOException("Not implemented");
    }

    /**
     * Gets the object.
     *
     * @param key
     *            the key
     *
     * @return the object
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public Object getObject(final Object key) throws IOException {
        throw new IOException("Not implemented");
    }

    /**
     * Gets the data output file.
     *
     * @param ticket
     *            the ticket
     *
     * @return the data output file
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public String getOriginalFilename(final Object ticket) throws IOException {
        throw new IOException("Not implemented");
    }

    /**
     * The Class LoaderEntry.
     */
    private static final class LoaderEntry {

        /** The _classes. */
        private final Map<String, Class<?>> _classes = new HashMap<>();

        /** The _loader. */
        private final ClassLoader _loader;

        /** The _file. */
        private final File _file;

        /** The _last modified. */
        private final long _lastModified;

        /**
         * Instantiates a new loader entry.
         *
         * @param archive
         *            the archive
         *
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        LoaderEntry(final String archive) throws IOException {
            _file = new File(archive);
            _lastModified = _file.lastModified();
            _loader = StarterLoader.getClassLoader(archive, Thread.currentThread().getContextClassLoader());
        }

        /**
         * Instantiates the default loader entry.
         */
        LoaderEntry() {
            _file = null;
            _lastModified = -1;
            _loader = Thread.currentThread().getContextClassLoader();
        }

        /**
         * Gets the transfer module class.
         *
         * @param className
         *            the class name
         *
         * @return the transfer module class
         *
         * @throws InstantiationException
         *             the instantiation exception
         * @throws IllegalAccessException
         *             the illegal access exception
         * @throws IllegalArgumentException
         *             the illegal argument exception
         * @throws InvocationTargetException
         *             the invocation target exception
         * @throws NoSuchMethodException
         *             the no such method exception
         * @throws SecurityException
         *             the security exception
         * @throws ClassNotFoundException
         *             the class not found exception
         */
        TransferModule getTransferModule(final String className)
                throws InstantiationException, IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
            Class<?> clazz;
            synchronized (_classes) {
                if ((clazz = _classes.get(className)) == null) {
                    _classes.put(className, clazz = _loader.loadClass(className));
                }
            }
            final Object object = clazz.getDeclaredConstructor().newInstance();
            if (object instanceof TransferModule transferModule) {
                return transferModule;
            }
            throw new IllegalArgumentException("Not a transfer module class (" + className + "): " + object);
        }

        /**
         * Checks if is expired. The default loader never expires.
         *
         * @return true, if is expired
         */
        boolean isExpired() {
            return _file != null && _file.lastModified() != _lastModified;
        }
    }
}
