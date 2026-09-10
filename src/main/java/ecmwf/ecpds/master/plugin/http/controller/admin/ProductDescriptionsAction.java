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
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.home.monitoring.ProductStatusHome;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Admin action for managing per-product text descriptions, stored in the SYS_CONFIG database table (group
 * "ProductDescription"). These descriptions are exposed as the {@code {{DESCRIPTION}}} placeholder in the Product
 * Status Messages (Admin Tasks &rarr; Product Status Messages).
 *
 * GET /admin/productdescriptions &rarr; list all descriptions (HTML page) POST /admin/productdescriptions/save &rarr;
 * add or update a description (JSON response) POST /admin/productdescriptions/delete &rarr; remove a description (JSON
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

    /** Maximum accepted length for a description. */
    private static final int MAX_DESCRIPTION_LENGTH = 4000;

    /**
     * Simple JavaBean wrapper around a product/description pair, exposed to the JSP as
     * {@code ${entry.product}}/{@code ${entry.description}} (rather than iterating a raw {@link Map} with
     * {@code entry.key}/{@code entry.value}, which is not reliably resolved by this application's EL setup).
     */
    public static final class ProductDescriptionEntry {

        /** The product name. */
        private final String product;

        /** The description text. */
        private final String description;

        /** Whether this product is currently known to the monitoring interface ({@code /do/monitoring}). */
        private final boolean known;

        /**
         * Instantiates a new product description entry.
         *
         * @param product
         *            the product
         * @param description
         *            the description
         * @param known
         *            whether the product currently exists in the monitoring interface
         */
        public ProductDescriptionEntry(final String product, final String description, final boolean known) {
            this.product = product;
            this.description = description;
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
         * Gets the description.
         *
         * @return the description
         */
        public String getDescription() {
            return description;
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

        // Known product names (from live monitoring memory), used to power a suggestions datalist and to flag,
        // in the table below, any configured product that does not (or no longer) exist in the monitoring
        // interface (/do/monitoring).
        final var knownProducts = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        try {
            knownProducts.addAll(ProductStatusHome.findFromMemory().keySet());
            request.setAttribute("knownProductNames", new ArrayList<>(knownProducts));
        } catch (final Exception e) {
            request.setAttribute("knownProductNames", java.util.Collections.emptyList());
        }

        // Default: list page
        try {
            final var db = MasterManager.getDB();
            final var descriptions = db.getProductDescriptions();
            final var sorted = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
            sorted.putAll(descriptions);
            final var entries = new ArrayList<ProductDescriptionEntry>(sorted.size());
            for (final var e : sorted.entrySet()) {
                entries.add(new ProductDescriptionEntry(e.getKey(), e.getValue(), knownProducts.contains(e.getKey())));
            }
            request.setAttribute("productDescriptions", entries);
            request.setAttribute("productDescriptionsJson", _mapper.writeValueAsString(descriptions));
        } catch (final Exception e) {
            _log.warn("ProductDescriptionsAction: failed to load descriptions", e);
            request.setAttribute("productDescriptions", java.util.Collections.emptyList());
            request.setAttribute("productDescriptionsJson", "{}");
            request.setAttribute("pdError", "Unable to load product descriptions: " + e.getMessage());
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

            var description = body.get("description") != null ? String.valueOf(body.get("description")) : "";
            if (description.length() > MAX_DESCRIPTION_LENGTH)
                throw new IllegalArgumentException("Description too long (max " + MAX_DESCRIPTION_LENGTH + ")");

            MasterManager.getDB().setProductDescription(product, description);

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
            MasterManager.getDB().deleteProductDescription(product);
            response.getWriter().write("{\"success\":true}");
        } catch (final Exception e) {
            _log.warn("ProductDescriptionsAction.handleDelete", e);
            writeError(response, e.getMessage());
        }
        return null;
    }

    /**
     * Handle delete unknown. Deletes all configured product descriptions whose product name does not currently exist in
     * the monitoring interface (/do/monitoring).
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
                knownProducts.addAll(ProductStatusHome.findFromMemory().keySet());
            } catch (final Exception e) {
                _log.warn("ProductDescriptionsAction.handleDeleteUnknown: failed to load known products", e);
            }
            final var descriptions = db.getProductDescriptions();
            var deleted = 0;
            var errors = 0;
            for (final var product : descriptions.keySet()) {
                if (!knownProducts.contains(product)) {
                    try {
                        db.deleteProductDescription(product);
                        deleted++;
                    } catch (final Exception e) {
                        _log.warn("handleDeleteUnknown: failed to delete product {}", product, e);
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
