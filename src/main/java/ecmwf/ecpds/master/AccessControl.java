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

package ecmwf.ecpds.master;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.text.Util.isNotEmpty;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.CatUrl;
import ecmwf.common.database.Category;
import ecmwf.common.database.DataBaseException;
import ecmwf.common.database.Destination;
import ecmwf.common.database.ECpdsBase;
import ecmwf.common.database.Url;
import ecmwf.common.database.WeuCat;

/**
 * The Class AccessControl.
 */
final class AccessControl {
    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(AccessControl.class);

    /**
     * Insert access control.
     *
     * @param base
     *            the base
     * @param destinationName
     *            the destination name
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public static void insertAccessControl(final ECpdsBase base, final String destinationName)
            throws DataBaseException {
        // Deal with the user access control for this Destination!
        // Create the URLs
        final var url1 = new Url("/do/transfer/destination/" + destinationName);
        final var url2 = new Url("/do/transfer/destination/operations/" + destinationName + "/");
        final var url3 = new Url("/do/transfer/destination/metadata/" + destinationName);
        base.insert(url1, false);
        base.insert(url2, false);
        base.insert(url3, false);
        // Create the main category!
        final var category = new Category();
        category.setActive(true);
        category.setDescription("Destination Operations & Monitoring");
        category.setName(destinationName + " operations");
        base.insert(category, true);
        // Associate the URLs with the main category!
        base.insert(new CatUrl(category.getId(), url1.getName()), false);
        base.insert(new CatUrl(category.getId(), url2.getName()), false);
        base.insert(new CatUrl(category.getId(), url3.getName()), false);
        // Associate the URLs with the "operations" category!
        for (final Category c : base.getCategoryArray()) {
            if ("operations".equals(c.getName())) {
                base.insert(new CatUrl(c.getId(), url1.getName()), false);
                base.insert(new CatUrl(c.getId(), url2.getName()), false);
                base.insert(new CatUrl(c.getId(), url3.getName()), false);
                break;
            }
        }
    }

    /**
     * Removes the access control.
     *
     * @param base
     *            the base
     * @param destinationName
     *            the Destination name
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public static void removeAccessControl(final ECpdsBase base, final String destinationName)
            throws DataBaseException {
        // Deal with the user access control for this Destination!
        // The URLs
        final var url1 = new Url("/do/transfer/destination/" + destinationName);
        final var url2 = new Url("/do/transfer/destination/operations/" + destinationName + "/");
        final var url3 = new Url("/do/transfer/destination/metadata/" + destinationName);
        // Remove all the associations with the Categories!
        for (final CatUrl cu : base.getCatUrlArray()) {
            if (url1.getName().equals(cu.getUrlName()) || url2.getName().equals(cu.getUrlName())
                    || url3.getName().equals(cu.getUrlName())) {
                base.remove(cu);
            }
        }
        // Find the main category!
        Category main = null;
        for (final Category c : base.getCategoryArray()) {
            if ((destinationName + " operations").equalsIgnoreCase(c.getName())) {
                main = c;
                break;
            }
        }
        if (main != null) {
            // Remove all the associations with the users!
            for (final WeuCat wc : base.getWeuCatArray()) {
                if (wc.getCategoryId() == main.getId()) {
                    base.remove(wc);
                }
            }
            // Remove the main category
            base.remove(main);
        }
        // Now we can remove the URL!
        base.remove(url1);
        base.remove(url2);
        base.remove(url3);
    }

    /**
     * Clean access control.
     *
     * @param base
     *            the base
     *
     * @return the int
     *
     * @throws ecmwf.common.database.DataBaseException
     *             the data base exception
     */
    public static int cleanAccessControl(final ECpdsBase base) throws DataBaseException {
        var processed = 0;
        final List<String> destinations = new ArrayList<>();
        // Build the destination list from the Url found
        final var url1 = "/do/transfer/destination/";
        final var url2 = "/do/transfer/destination/operations/";
        final var url3 = "/do/transfer/destination/metadata/";
        for (final Url url : base.getUrlArray()) {
            final var name = url.getName();
            String destinationName = null;
            if (name.startsWith(url2)) {
                destinationName = name.substring(url2.length(), name.length() - 1);
            } else if (name.startsWith(url3)) {
                destinationName = name.substring(url3.length());
            } else {
                if (name.startsWith(url1)) {
                    destinationName = name.substring(url1.length());
                }
            }
            if (isNotEmpty(destinationName) && !destinationName.endsWith("/")
                    && !destinations.contains(destinationName)) {
                _log.debug("Destination found: " + destinationName);
                destinations.add(destinationName);
            }
        }
        // For each destination found check if it still exists?
        for (final String destinationName : destinations.toArray(new String[destinations.size()])) {
            if (base.getDestinationObject(destinationName) == null) {
                // the destination does not exists, so let's remove the access
                // control!
                _log.debug("Removing access control for destination: " + destinationName);
                // Remove it from the list!
                destinations.remove(destinationName);
                try {
                    removeAccessControl(base, destinationName);
                    processed++;
                } catch (final Throwable t) {
                    _log.debug("could not clean destination access control for: " + destinationName, t);
                }
            } else {
                // The destination exists, let's check if the main category is
                // not missing?
                Category category = null;
                for (final Category c : base.getCategoryArray()) {
                    if ((destinationName + " operations").equalsIgnoreCase(c.getName())) {
                        category = c;
                        break;
                    }
                }
                if (category == null) {
                    // Create the main category!
                    _log.debug("Main category is missing for destination: " + destinationName);
                    processed++;
                    category = new Category();
                    category.setActive(true);
                    category.setDescription("Destination Operations & Monitoring");
                    category.setName(destinationName + " operations");
                    base.insert(category, true);
                }
                // Define both URL
                final var urla = url1 + destinationName;
                final var urlb = url2 + destinationName + "/";
                final var urlc = url3 + destinationName;
                // Associate the URLs with the main category if not yet done!
                for (final String urlName : new String[] { urla, urlb, urlc }) {
                    final var url = new Url(urlName);
                    if (base.getUrlObject(urlName) == null) {
                        base.insert(url, false);
                    }
                    final var catUrl = new CatUrl(category.getId(), urlName);
                    if (base.getCatUrlObject(category.getId(), urlName) == null) {
                        base.insert(catUrl, false);
                    }
                }
                // Associate the URLs with the "operations" category is not
                // yet done!
                for (final Category c : base.getCategoryArray()) {
                    if ("operations".equals(c.getName())) {
                        for (final String url : new String[] { urla, urlb, urlc }) {
                            final var catUrl = new CatUrl(c.getId(), url);
                            if (base.getCatUrlObject(c.getId(), catUrl.getUrlName()) == null) {
                                base.insert(catUrl, false);
                            }
                        }
                        break;
                    }
                }
            }
        }
        // For every destination in the database not found in the access
        // control, add it!
        for (final Destination destination : base.getDestinationArray()) {
            final var destinationName = destination.getName();
            if (!destinations.contains(destinationName)) {
                // the destination does not exists, so let's remove the access
                // control!
                _log.debug("Adding access control for destination: " + destinationName);
                try {
                    insertAccessControl(base, destinationName);
                    processed++;
                } catch (final Throwable t) {
                    _log.debug("could not insert destination access control for: " + destinationName, t);
                }
            }
        }
        return processed;
    }
}
