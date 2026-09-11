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
package ecmwf.ecpds.master.plugin.http.controller.admin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.common.database.ProductMetadata;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.home.monitoring.ProductStatusHome;
import ecmwf.ecpds.master.plugin.http.home.monitoring.ProductStepStatusHome;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Admin action for managing per-product (optionally per-type) metadata, stored in the PRODUCT_METADATA database table:
 * a free-text description, exposed as the {@code {{DESCRIPTION}}} placeholder in the Product Status Messages (Admin
 * Tasks &rarr; Product Status Messages), and a "Tips" text shown on the product monitoring page. An entry with an empty
 * type applies to all types of that product (used as a fallback when no type-specific entry exists).
 *
 * GET /admin/productdescriptions &rarr; list all entries (HTML page) POST /admin/productdescriptions/save &rarr; add or
 * update an entry (JSON response) POST /admin/productdescriptions/delete &rarr; remove an entry (JSON response) POST
 * /admin/productdescriptions/deleteUnknown &rarr; remove all entries for products not seen in monitoring (JSON
 * response)
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2026-09-10
 */
public class ProductDescriptionsAction extends PDSAction {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(ProductDescriptionsAction.class);

    /** The Constant _mapper. */
    private static final ObjectMapper _mapper = new ObjectMapper();

    /** Maximum accepted length for a product name. */
    private static final int MAX_PRODUCT_LENGTH = 64;

    /** Maximum accepted length for a product type. */
    private static final int MAX_TYPE_LENGTH = 32;

    /** Maximum accepted length for a description or tips text. */
    private static final int MAX_TEXT_LENGTH = 4000;

    /**
     * Simple JavaBean wrapper around a {@link ProductMetadata} entry, plus a "known" flag, exposed to the JSP as
     * {@code ${entry.product}}/{@code ${entry.type}}/{@code ${entry.description}}/{@code ${entry.tips}} (rather than
     * iterating a raw {@link Map} with {@code entry.key}/{@code entry.value}, which is not reliably resolved by this
     * application's EL setup).
     */
    public static final class ProductMetadataEntry {

        /** The product name. */
        private final String product;

        /** The product type, or an empty string for the generic (all-types) entry. */
        private final String type;

        /** The description text. */
        private final String description;

        /** The tips text. */
        private final String tips;

        /** Whether this product is currently known to the monitoring interface ({@code /do/monitoring}). */
        private final boolean known;

        /**
         * Instantiates a new product metadata entry.
         *
         * @param product
         *            the product
         * @param type
         *            the type
         * @param description
         *            the description
         * @param tips
         *            the tips
         * @param known
         *            whether the product currently exists in the monitoring interface
         */
        public ProductMetadataEntry(final String product, final String type, final String description,
                final String tips, final boolean known) {
            this.product = product;
            this.type = type;
            this.description = description;
            this.tips = tips;
            this.known = known;
        }

        /**
         * Gets the product.
         *
         * @return the product
         */
        public String getProduct() {
            return product;
        }

        /**
         * Gets the type.
         *
         * @return the type
         */
        public String getType() {
            return type;
        }

        /**
         * Checks if is generic (applies to all types of the product).
         *
         * @return true, if is generic
         */
        public boolean isGeneric() {
            return type == null || type.isEmpty();
        }

        /**
         * Gets the description.
         *
         * @return the description
         */
        public String getDescription() {
            return description;
        }

        /**
         * Gets the tips.
         *
         * @return the tips
         */
        public String getTips() {
            return tips;
        }

        /**
         * Checks if is known.
         *
         * @return true, if the product currently exists in the monitoring interface
         */
        public boolean isKnown() {
            return known;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFActionFormException {

        final var params = ECMWFActionForm.getPathParameters(mapping, request);
        final var action = params.isEmpty() ? "" : params.get(0).toString();

        if ("save".equals(action)) {
            return handleSave(request, response);
        }
        if ("delete".equals(action)) {
            return handleDelete(request, response);
        }
        if ("deleteUnknown".equals(action)) {
            return handleDeleteUnknown(request, response);
        }
        if ("import".equals(action)) {
            return handleImport(request, response);
        }

        // Known product names (from live monitoring memory), used to power a suggestions datalist and to flag,
        // in the table below, any configured product that does not (or no longer) exist in the monitoring
        // interface (/do/monitoring).
        final var knownProducts = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        try {
            // findFromMemory() is keyed by "product@time", not by product name, so the product names must be
            // collected from the cached values rather than from the map's key set.
            for (final var status : ProductStatusHome.findFromMemory().values()) {
                knownProducts.add(status.getProduct());
            }
            request.setAttribute("knownProductNames", new ArrayList<>(knownProducts));
        } catch (final Exception e) {
            request.setAttribute("knownProductNames", java.util.Collections.emptyList());
        }

        // Default: list page
        try {
            final var db = MasterManager.getDB();
            final var metadata = db.getProductMetadata();
            final var sorted = new ArrayList<>(metadata);
            sorted.sort(Comparator.comparing(ProductMetadata::getProduct, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(ProductMetadata::getType, String.CASE_INSENSITIVE_ORDER));
            final var entries = new ArrayList<ProductMetadataEntry>(sorted.size());
            for (final var m : sorted) {
                entries.add(new ProductMetadataEntry(m.getProduct(), m.getType(), m.getDescription(), m.getTips(),
                        knownProducts.contains(m.getProduct())));
            }
            request.setAttribute("productDescriptions", entries);
        } catch (final Exception e) {
            _log.warn("ProductDescriptionsAction: failed to load product metadata", e);
            request.setAttribute("productDescriptions", java.util.Collections.emptyList());
            request.setAttribute("pdError", "Unable to load product metadata: " + e.getMessage());
        }

        return mapping.findForward("success");
    }

    /**
     * Handle save.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     *
     * @return the action forward
     */
    private ActionForward handleSave(final HttpServletRequest request, final HttpServletResponse response) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            final var body = _mapper.readValue(request.getInputStream(), Map.class);

            final var product = trimOrNull(body.get("product"));
            if (product == null || product.isBlank())
                throw new IllegalArgumentException("Product name is required");
            if (product.length() > MAX_PRODUCT_LENGTH)
                throw new IllegalArgumentException("Product name too long (max " + MAX_PRODUCT_LENGTH + ")");
            if (!product.matches("[A-Za-z0-9_.-]+"))
                throw new IllegalArgumentException(
                        "Product name must contain only letters, digits, underscores, dots or hyphens");

            var type = trimOrNull(body.get("type"));
            type = type != null ? type : "";
            if (type.length() > MAX_TYPE_LENGTH)
                throw new IllegalArgumentException("Product type too long (max " + MAX_TYPE_LENGTH + ")");
            if (!type.isEmpty() && !type.matches("[A-Za-z0-9_.-]+"))
                throw new IllegalArgumentException(
                        "Product type must contain only letters, digits, underscores, dots or hyphens");

            var description = body.get("description") != null ? String.valueOf(body.get("description")) : "";
            if (description.length() > MAX_TEXT_LENGTH)
                throw new IllegalArgumentException("Description too long (max " + MAX_TEXT_LENGTH + ")");

            var tips = body.get("tips") != null ? String.valueOf(body.get("tips")) : "";
            if (tips.length() > MAX_TEXT_LENGTH)
                throw new IllegalArgumentException("Tips too long (max " + MAX_TEXT_LENGTH + ")");

            MasterManager.getDB().setProductMetadata(product, type, description, tips);

            response.getWriter().write("{\"success\":true}");
        } catch (final Exception e) {
            _log.warn("ProductDescriptionsAction.handleSave", e);
            writeError(response, e.getMessage());
        }
        return null;
    }

    /**
     * Handle delete.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     *
     * @return the action forward
     */
    private ActionForward handleDelete(final HttpServletRequest request, final HttpServletResponse response) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            final var body = _mapper.readValue(request.getInputStream(), Map.class);
            final var product = trimOrNull(body.get("product"));
            if (product == null || product.isBlank())
                throw new IllegalArgumentException("Product name is required");
            final var type = trimOrNull(body.get("type"));
            MasterManager.getDB().deleteProductMetadata(product, type != null ? type : "");
            response.getWriter().write("{\"success\":true}");
        } catch (final Exception e) {
            _log.warn("ProductDescriptionsAction.handleDelete", e);
            writeError(response, e.getMessage());
        }
        return null;
    }

    /**
     * Handle delete unknown. Deletes all configured product metadata entries whose product name does not currently
     * exist in the monitoring interface (/do/monitoring).
     *
     * @param request
     *            the request
     * @param response
     *            the response
     *
     * @return the action forward
     */
    private ActionForward handleDeleteUnknown(final HttpServletRequest request, final HttpServletResponse response) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            final var db = MasterManager.getDB();
            final var knownProducts = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
            try {
                // findFromMemory() is keyed by "product@time", not by product name, so the product names must be
                // collected from the cached values rather than from the map's key set.
                for (final var status : ProductStatusHome.findFromMemory().values()) {
                    knownProducts.add(status.getProduct());
                }
            } catch (final Exception e) {
                _log.warn("ProductDescriptionsAction.handleDeleteUnknown: failed to load known products", e);
            }
            final var metadata = db.getProductMetadata();
            var deleted = 0;
            var errors = 0;
            for (final var m : metadata) {
                if (!knownProducts.contains(m.getProduct())) {
                    try {
                        db.deleteProductMetadata(m.getProduct(), m.getType());
                        deleted++;
                    } catch (final Exception e) {
                        _log.warn("handleDeleteUnknown: failed to delete product {}/{}", m.getProduct(), m.getType(),
                                e);
                        errors++;
                    }
                }
            }
            response.getWriter().write("{\"success\":true,\"deleted\":" + deleted + ",\"errors\":" + errors + "}");
        } catch (final Exception e) {
            _log.warn("ProductDescriptionsAction.handleDeleteUnknown", e);
            writeError(response, e.getMessage());
        }
        return null;
    }

    /**
     * Handle import. Scans every product/type pair currently present in the monitoring interface (the "Type" column
     * shown on e.g. {@code /do/monitoring/summary/GOPER/06}) and creates an empty (blank description/tips) metadata
     * entry for any pair not already configured, so it can be filled in afterwards via Edit.
     *
     * @param request
     *            the request
     * @param response
     *            the response
     *
     * @return the action forward
     */
    private ActionForward handleImport(final HttpServletRequest request, final HttpServletResponse response) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            final var db = MasterManager.getDB();
            final var existing = new java.util.HashSet<String>();
            for (final var m : db.getProductMetadata()) {
                existing.add(pairKey(m.getProduct(), m.getType()));
            }
            final var seen = new java.util.HashSet<String>();
            final var imported = new ArrayList<Map<String, String>>();
            for (final var s : ProductStepStatusHome.findAll()) {
                if (!s.isPresent()) {
                    continue;
                }
                final var product = s.getProduct();
                if (product == null || product.isBlank()) {
                    continue;
                }
                final var type = s.getType() != null ? s.getType() : "";
                final var key = pairKey(product, type);
                if (!seen.add(key) || existing.contains(key)) {
                    continue;
                }
                db.setProductMetadata(product, type, "", "");
                final var entry = new java.util.LinkedHashMap<String, String>();
                entry.put("product", product);
                entry.put("type", type);
                imported.add(entry);
            }
            final var result = new java.util.LinkedHashMap<String, Object>();
            result.put("success", true);
            result.put("imported", imported.size());
            result.put("entries", imported);
            response.getWriter().write(_mapper.writeValueAsString(result));
        } catch (final Exception e) {
            _log.warn("ProductDescriptionsAction.handleImport", e);
            writeError(response, e.getMessage());
        }
        return null;
    }

    /**
     * Builds a case-insensitive lookup key for a (product, type) pair.
     *
     * @param product
     *            the product
     * @param type
     *            the type
     *
     * @return the pair key
     */
    private static String pairKey(final String product, final String type) {
        return product.toLowerCase() + "\u0001" + (type != null ? type.toLowerCase() : "");
    }

    /**
     * Trim or null.
     *
     * @param v
     *            the v
     *
     * @return the string
     */
    private static String trimOrNull(final Object v) {
        return v != null ? String.valueOf(v).trim() : null;
    }

    /**
     * Write error.
     *
     * @param response
     *            the response
     * @param msg
     *            the msg
     */
    private static void writeError(final HttpServletResponse response, final String msg) {
        try {
            final var safe = (msg != null ? msg : "error").replace("\"", "'");
            response.getWriter().write("{\"success\":false,\"error\":\"" + safe + "\"}");
        } catch (final Exception ignored) {
        }
    }
}
