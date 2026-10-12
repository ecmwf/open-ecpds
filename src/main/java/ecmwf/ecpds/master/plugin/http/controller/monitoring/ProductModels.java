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

package ecmwf.ecpds.master.plugin.http.controller.monitoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import ecmwf.common.database.DataBaseException;
import ecmwf.ecpds.master.MasterManager;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Product models: a model (e.g. "IFS" or "AIFS") is only identified by its unique name, and a product belongs to at
 * most one model. Both are kept in the SYS_CONFIG table (groups "ProductModelName" for the model names and
 * "ProductModel" for the product to model assignments), so no schema change is needed. Models are maintained from Admin
 * Tasks &rarr; Product Descriptions and used to group and filter the products of the Delay Notification page.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 8.2.0
 *
 * @since 2026-10-12
 */
public final class ProductModels {

    /** SYS_CONFIG group holding the model names (one parameter per model, case-insensitive primary key). */
    private static final String NAMES_GROUP = "ProductModelName";

    /** SYS_CONFIG group holding the product to model assignments (parameter = product, value = model). */
    private static final String ASSIGNMENTS_GROUP = "ProductModel";

    /** The accepted model names. */
    private static final Pattern MODEL_NAME_PATTERN = Pattern.compile("[A-Za-z0-9][A-Za-z0-9 _.+-]{0,63}");

    /** The accepted product names (same rule as the product descriptions). */
    private static final Pattern PRODUCT_NAME_PATTERN = Pattern.compile("[A-Za-z0-9_.-]{1,64}");

    private ProductModels() {
        // Hiding constructor!
    }

    /**
     * Gets the names of all the models, sorted alphabetically (ignoring the case).
     *
     * @return the model names
     *
     * @throws java.rmi.RemoteException
     *             on remote failure
     * @throws DataBaseException
     *             if the models cannot be read
     */
    public static List<String> names() throws DataBaseException, java.rmi.RemoteException {
        final var result = new ArrayList<>(MasterManager.getDB().getMonitorConfigGroup(NAMES_GROUP).keySet());
        result.sort(String.CASE_INSENSITIVE_ORDER);
        return result;
    }

    /**
     * Gets the model of each product that has one.
     *
     * @return the models, keyed by product name (sorted ignoring the case)
     *
     * @throws java.rmi.RemoteException
     *             on remote failure
     * @throws DataBaseException
     *             if the assignments cannot be read
     */
    public static Map<String, String> assignments() throws DataBaseException, java.rmi.RemoteException {
        final Map<String, String> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        result.putAll(MasterManager.getDB().getMonitorConfigGroup(ASSIGNMENTS_GROUP));
        return result;
    }

    /**
     * Gets the products of each model, including the models without any product.
     *
     * @return the product names of each model, keyed by model name (sorted ignoring the case)
     *
     * @throws java.rmi.RemoteException
     *             on remote failure
     * @throws DataBaseException
     *             if the models cannot be read
     */
    public static Map<String, List<String>> productsPerModel() throws DataBaseException, java.rmi.RemoteException {
        final Map<String, List<String>> result = new LinkedHashMap<>();
        for (final var model : names()) {
            result.put(model, new ArrayList<>());
        }
        for (final var e : assignments().entrySet()) {
            // A model name is unique ignoring the case so we match it that way.
            for (final var model : result.keySet()) {
                if (model.equalsIgnoreCase(e.getValue())) {
                    result.get(model).add(e.getKey());
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Creates a model, or renames and updates the products of an existing one. The list of products is authoritative:
     * the products of the model that are not in the list are removed from it, and the listed products that belong to
     * another model are moved to this one.
     *
     * @param oldName
     *            the current name of the model to update, or {@code null} to create a new model
     * @param name
     *            the (new) unique name of the model
     * @param products
     *            the names of the products of the model
     *
     * @throws java.rmi.RemoteException
     *             on remote failure
     * @throws DataBaseException
     *             if the name or a product is invalid, if the name is already used, or if the update fails
     */
    public static synchronized void save(final String oldName, final String name, final Collection<String> products)
            throws DataBaseException, java.rmi.RemoteException {
        if (name == null || !MODEL_NAME_PATTERN.matcher(name).matches()) {
            throw new DataBaseException(
                    "Invalid model name (use 1 to 64 letters, digits, spaces, underscores, dots, plus or hyphens)");
        }
        for (final var product : products) {
            if (product == null || !PRODUCT_NAME_PATTERN.matcher(product).matches()) {
                throw new DataBaseException("Invalid product name: " + product);
            }
        }
        final var db = MasterManager.getDB();
        final var existing = names();
        String current = null;
        for (final var model : existing) {
            if (oldName != null && model.equalsIgnoreCase(oldName)) {
                current = model;
            } else if (model.equalsIgnoreCase(name)) {
                throw new DataBaseException("A model named '" + model + "' already exists");
            }
        }
        if (oldName != null && current == null) {
            throw new DataBaseException("Unknown model: " + oldName);
        }
        final var previous = assignments();
        // Move the model to its new name (the case may change).
        if (current != null && !current.equals(name)) {
            db.deleteMonitorConfigValue(NAMES_GROUP, current);
        }
        db.setMonitorConfigValue(NAMES_GROUP, name, "");
        final var selected = new java.util.TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        selected.addAll(products);
        for (final var e : previous.entrySet()) {
            if (current != null && current.equalsIgnoreCase(e.getValue()) && !selected.contains(e.getKey())) {
                db.deleteMonitorConfigValue(ASSIGNMENTS_GROUP, e.getKey());
            }
        }
        for (final var product : selected) {
            final var assigned = previous.get(product);
            if (!name.equals(assigned)) {
                db.setMonitorConfigValue(ASSIGNMENTS_GROUP, product, name);
            }
        }
    }

    /**
     * Deletes a model; its products are left without model.
     *
     * @param name
     *            the name of the model
     *
     * @throws java.rmi.RemoteException
     *             on remote failure
     * @throws DataBaseException
     *             if the model does not exist or cannot be deleted
     */
    public static synchronized void delete(final String name) throws DataBaseException, java.rmi.RemoteException {
        final var db = MasterManager.getDB();
        String current = null;
        for (final var model : names()) {
            if (model.equalsIgnoreCase(name)) {
                current = model;
            }
        }
        if (current == null) {
            throw new DataBaseException("Unknown model: " + name);
        }
        for (final var e : assignments().entrySet()) {
            if (current.equalsIgnoreCase(e.getValue())) {
                db.deleteMonitorConfigValue(ASSIGNMENTS_GROUP, e.getKey());
            }
        }
        db.deleteMonitorConfigValue(NAMES_GROUP, current);
    }

    /**
     * Removes from their model the products which are not in the specified collection (e.g. products which no longer
     * have a product description).
     *
     * @param valid
     *            the product names which can stay assigned to a model
     *
     * @throws java.rmi.RemoteException
     *             on remote failure
     * @throws DataBaseException
     *             if the update fails
     */
    public static synchronized void retainProducts(final Collection<String> valid)
            throws DataBaseException, java.rmi.RemoteException {
        final var keep = new java.util.TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        keep.addAll(valid);
        final var db = MasterManager.getDB();
        for (final var product : assignments().keySet()) {
            if (!keep.contains(product)) {
                db.deleteMonitorConfigValue(ASSIGNMENTS_GROUP, product);
            }
        }
    }
}
