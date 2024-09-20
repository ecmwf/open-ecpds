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

package ecmwf.common.technical;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Class ObjectRepository.
 */
public abstract class ObjectRepository {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ObjectRepository.class);

    /**
     * Instantiates a new object repository.
     */
    public ObjectRepository() {
        // Default constructor
    }

    /**
     * Gets the new key.
     *
     * @return the new key
     */
    public static String getNewKey() {
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * Gets the file.
     *
     * @param key
     *            the key
     *
     * @return the file
     */
    private File _getFile(final String key) {
        return new File(getDir(), key + '.' + getExt());
    }

    /**
     * Clear.
     */
    public void clear() {
        final var files = getDir().listFiles();
        for (var i = 0; files != null && i < files.length; i++) {
            if (files[i].getName().endsWith('.' + getExt()) && (files[i].exists() && !files[i].delete())) {
                _log.warn("Couldn't delete file: " + files[i].getAbsolutePath());
            }
        }
    }

    /**
     * Elements.
     *
     * @return the object[]
     */
    public Object[] elements() {
        final var files = getDir().listFiles();
        final List<Object> list = new ArrayList<>();
        if (files != null) {
            for (final File file : files) {
                if (file.getName().endsWith('.' + getExt())) {
                    try {
                        list.add(getElement(file));
                    } catch (final Exception e) {
                        if (file.exists() && !file.delete()) {
                            _log.warn("Couldn't delete file: " + file.getAbsolutePath());
                        }
                    }
                }
            }
        }
        return list.toArray(new Object[list.size()]);
    }

    /**
     * Gets the.
     *
     * @param key
     *            the key
     *
     * @return the object
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException
     *             the class not found exception
     */
    public Object get(final String key) throws IOException, ClassNotFoundException {
        return getElement(_getFile(key));
    }

    /**
     * Gets the dir.
     *
     * @return the dir
     */
    public abstract File getDir();

    /**
     * Gets the element.
     *
     * @param file
     *            the file
     *
     * @return the element
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException
     *             the class not found exception
     */
    public Object getElement(final File file) throws IOException, ClassNotFoundException {
        final var istream = new FileInputStream(file);
        final var p = new ObjectInputStream(istream);
        final var obj = p.readObject();
        istream.close();
        return obj;
    }

    /**
     * Gets the ext.
     *
     * @return the ext
     */
    public abstract String getExt();

    /**
     * Keys.
     *
     * @return the string[]
     */
    public String[] keys() {
        final var files = getDir().listFiles();
        final List<Object> list = new ArrayList<>();
        if (files != null) {
            for (final File file : files) {
                final var name = file.getName();
                if (name.endsWith('.' + getExt())) {
                    list.add(name.substring(0, name.length() - (getExt().length() + 1)));
                }
            }
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * Last modified.
     *
     * @param key
     *            the key
     *
     * @return the long
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public long lastModified(final String key) throws IOException {
        return _getFile(key).lastModified();
    }

    /**
     * Puts the.
     *
     * @param key
     *            the key
     * @param obj
     *            the obj
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void put(final String key, final Object obj) throws IOException {
        final var ostream = new FileOutputStream(_getFile(key));
        final var p = new ObjectOutputStream(ostream);
        p.writeObject(obj);
        p.flush();
        ostream.close();
    }

    /**
     * Removes the.
     *
     * @param key
     *            the key
     *
     * @return true, if successful
     */
    public boolean remove(final String key) {
        return _getFile(key).delete();
    }
}
