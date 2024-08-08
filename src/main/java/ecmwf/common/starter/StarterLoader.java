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

package ecmwf.common.starter;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The Class StarterLoader.
 */
public final class StarterLoader extends ClassLoader {

    /**
     * Instantiates a new starter loader.
     *
     * @param paths
     *            the paths
     * @param parent
     *            the parent
     *
     * @return the class loader
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static ClassLoader getClassLoader(final String paths, final ClassLoader parent) throws IOException {
        return Starter.get("urlClassLoader", true) ? new StarterLoader(paths, parent) : parent;
    }

    /**
     * Instantiates a new starter loader.
     *
     * @param paths
     *            the paths
     * @param parent
     *            the parent
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public StarterLoader(final String paths, final ClassLoader parent) throws IOException {
        super(new URLClassLoader(pathsToURLs(paths), parent));
    }

    /**
     * Paths to URLs.
     *
     * @param paths
     *            the paths
     *
     * @return the UR l[]
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static URL[] pathsToURLs(final String paths) throws IOException {
        Starter.debug(StarterLoader.class, "Processing ClassPath: " + paths);
        final List<URL> urls = new ArrayList<>();
        for (final String path : paths.split(":")) {
            final var file = new File(path).getCanonicalFile();
            if (!file.isDirectory()) {
                addURL(urls, file);
            } else {
                final var fileList = file.list();
                if (fileList != null) {
                    final List<String> list = Arrays.asList(fileList);
                    Collections.sort(list);
                    for (final String name : list) {
                        final var current = new File(path, name).getCanonicalFile();
                        if (!current.isDirectory()) {
                            addURL(urls, current);
                        }
                    }
                }
            }
        }
        return urls.toArray(new URL[0]);
    }

    /**
     * Adds URLs.
     *
     * @param urls
     *            the urls
     * @param file
     *            the file
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static void addURL(final List<URL> urls, final File file) throws IOException {
        Starter.debug(StarterLoader.class, "Adding URL: \"" + file + "\"");
        urls.add(file.toURI().toURL());
    }

    /**
     * Allow tracing class loading.
     *
     * @param name
     *            The binary name of the class
     *
     * @return The resulting {@code Class} object
     *
     * @throws ClassNotFoundException
     *             If the class was not found
     */
    @Override
    public Class<?> findClass(final String name) throws ClassNotFoundException {
        Starter.debug(StarterLoader.class, "FindClass: " + name);
        return super.findClass(name);
    }
}
