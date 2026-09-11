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

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.common.database.ProductMetadata;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.home.monitoring.ProductStatusHome;
import ecmwf.ecpds.master.plugin.http.home.monitoring.ProductStepStatusHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.model.monitoring.MonitoringException;
import ecmwf.ecpds.master.plugin.http.model.monitoring.ProductStatus;
import ecmwf.ecpds.master.plugin.http.model.monitoring.ProductStepStatus;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * The Class GetSummaryDisplayAction.
 */
public class GetSummaryDisplayAction extends PDSAction {

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final var c = DestinationHome.findAll();
        final List<Destination> destinations = new ArrayList<>(c);
        Collections.sort(destinations, new DestinationComparator("name", true));
        request.setAttribute("destinations", destinations);
        if (!destinations.isEmpty()) {
            request.setAttribute("firstDestination", destinations.iterator().next());
        }
        final ArrayList<?> params = ECMWFActionForm.getPathParameters(mapping, request);
        final var ses = (MonitoringSessionActionForm) form;
        if (params.isEmpty()) {
            throw new ECMWFActionFormException("Unsupported Feature. Please contact the development team.");
        }
        if (params.size() == 1) {
            // Merged "all cycles/times" view: for products configured (Product Descriptions) to group all their
            // cycles onto a single page instead of one page per cycle. Gather every cycle currently known for
            // the product and combine their step statii into one table.
            final var product = params.get(0).toString();
            final var times = new TreeSet<String>();
            for (final var ps : ProductStatusHome.findFromMemory().values()) {
                if (product.equals(ps.getProduct())) {
                    times.add(ps.getTime());
                }
            }
            final List<ProductStepStatus> stepStatii = new ArrayList<>();
            for (final var time : times) {
                stepStatii.addAll(ProductStepStatusHome.findAll(product, time));
            }
            putDataForHeader(request, ses, product, "", stepStatii, false, null);
            request.setAttribute("productStepStatii", stepStatii);
            request.setAttribute("productTimes", times);
        } else if (params.size() == 2) {
            // Summary by product (tag). Take as tag name whatever they pass us.
            // If it is invalid then the page will be empty :-(. It is too
            // expensive to validate.
            final var product = params.get(0).toString();
            final var time = params.get(1).toString();
            final var stepStatii = ProductStepStatusHome.findAll(product, time);
            putDataForHeader(request, ses, product, time, stepStatii, false, null);
            request.setAttribute("productStepStatii", stepStatii);
        } else if (params.size() == 4) {
            final var product = params.get(0).toString();
            final var time = params.get(1).toString();
            var step = 0L;
            try {
                step = Long.parseLong(params.get(2).toString());
            } catch (final NumberFormatException e) {
                throw new ECMWFActionFormException("'step' has to be a number", e);
            }
            final var type = params.get(3).toString();
            final var history = ProductStepStatusHome.findHistory(product, time, step, type, -1);
            putDataForHeader(request, ses, product, time, history, true, type);
            request.setAttribute("productStepStatii", history);
            request.setAttribute("step", step);
            request.setAttribute("type", type);
        } else {
            throw new ECMWFActionFormException("Expected 0, 1, 2 or 4 parameters.");
        }
        request.setAttribute("updated", new Date());
        return mapping.findForward("success");
    }

    /**
     * Put data for header.
     *
     * @param request
     *            the request
     * @param ses
     *            the ses
     * @param product
     *            the product
     * @param time
     *            the time
     * @param stepStatii
     *            the product step statii currently shown in the table (used to compute the number of rows per column,
     *            and the distinct product types for the {@code {{DESCRIPTION}}} placeholder)
     * @param onecolumn
     *            the onecolumn
     * @param currentType
     *            the single product type currently being viewed (step/type history page), or {@code null} on the
     *            product/cycle overview page; used to look up the Tips text for the product monitoring page
     *
     * @throws MonitoringException
     *             the monitoring exception
     * @throws TransferException
     *             the transfer exception
     */
    private static final void putDataForHeader(final HttpServletRequest request, final MonitoringSessionActionForm ses,
            final String product, final String time, final Collection<ProductStepStatus> stepStatii,
            final boolean onecolumn, final String currentType) throws MonitoringException, TransferException {
        final List<ProductStatus> products = new ArrayList<>(ProductStatusHome.findFromMemory().values());
        Collections.sort(products, new ProductStatusComparator());
        if (time == null || time.isBlank()) {
            // Merged "all cycles" view: synthesize a single ProductStatus (worst status, earliest scheduled,
            // most recent update) from every cycle currently known for the product.
            final List<ProductStatus> cycles = new ArrayList<>();
            for (final var ps : products) {
                if (product.equals(ps.getProduct())) {
                    cycles.add(ps);
                }
            }
            request.setAttribute("productStatus",
                    cycles.isEmpty() ? null : MonitoringRequest.mergeProductStatuses(product, cycles));
            request.setAttribute("productNameAndTime", product);
        } else {
            request.setAttribute("productStatus", ProductStatusHome.findByProduct(product, time));
            request.setAttribute("productNameAndTime", time + "-" + product);
        }
        request.setAttribute("productName", product);
        request.setAttribute("products", products);
        request.setAttribute("reqData", new MonitoringRequest(request, ses));
        final var stepStatiiSize = stepStatii.size();
        if (onecolumn) {
            request.setAttribute("stepsPerColumn", stepStatiiSize);
        } else {
            request.setAttribute("stepsPerColumn", stepStatiiSize / 2 + 1);
        }
        request.setAttribute("nearestToScheduleIndex", MonitoringRequest.getNearestToScheduleIndex(products));
        putProductStatusMessages(request, product, time, stepStatii, currentType);
    }

    /**
     * Sets the "ECMWFProductsDelay" and "ECMWFProducts" request attributes consumed by product.jsp to pre-fill the
     * Outlook deeplink email bodies, as well as "productTips" (the Tips text for the current product/type, if any).
     * Fetches the current (possibly customized) messages from the database, falling back to the built-in defaults if
     * they have not been customized, or if the database cannot be reached. The {@code {{PRODUCT}}} and
     * {@code {{CYCLE}}} placeholders, if present, are replaced with the actual product name and cycle/time currently
     * being viewed (e.g. "GENFO" and "06"). The {@code {{DESCRIPTION}}} placeholder is replaced with a bullet list of
     * the descriptions configured (Admin Tasks &rarr; Product Descriptions) for each distinct product type currently
     * shown in the table (falling back to the generic, all-types description when no type-specific one is configured),
     * or with the plain generic description when no type-specific rows apply.
     *
     * @param request
     *            the request
     * @param product
     *            the product name (e.g. "GENFO")
     * @param time
     *            the cycle/time (e.g. "06")
     * @param stepStatii
     *            the product step statii currently shown in the table, used to determine the distinct product types
     * @param currentType
     *            the single product type currently being viewed, or {@code null} on the product/cycle overview page
     */
    private static final void putProductStatusMessages(final HttpServletRequest request, final String product,
            final String time, final Collection<ProductStepStatus> stepStatii, final String currentType) {
        var delayMessage = ProductStatusMessages.DEFAULT_DELAY_MESSAGE;
        var resumedMessage = ProductStatusMessages.DEFAULT_RESUMED_MESSAGE;
        String description = null;
        String tips = null;
        try {
            final var db = MasterManager.getDB();
            final var storedDelayMessage = db.getProductStatusMessage(ProductStatusMessages.DELAY_MESSAGE_NAME);
            if (storedDelayMessage != null) {
                delayMessage = storedDelayMessage;
            }
            final var storedResumedMessage = db.getProductStatusMessage(ProductStatusMessages.RESUMED_MESSAGE_NAME);
            if (storedResumedMessage != null) {
                resumedMessage = storedResumedMessage;
            }
            if (product != null) {
                final var metadata = db.getProductMetadata(product);
                description = buildBulletedField(metadata, stepStatii, ProductMetadata::getDescription);
                tips = currentType != null ? lookupField(metadata, currentType, ProductMetadata::getTips)
                        : buildBulletedField(metadata, stepStatii, ProductMetadata::getTips);
            }
        } catch (final Exception e) {
            // Database not reachable or an error occurred: silently fall back to the built-in defaults.
        }
        delayMessage = ProductStatusMessages.substitutePlaceholders(delayMessage, product, time, description);
        resumedMessage = ProductStatusMessages.substitutePlaceholders(resumedMessage, product, time, description);
        request.setAttribute("ECMWFProductsDelay", ProductStatusMessages.encodeForEmailBody(delayMessage));
        request.setAttribute("ECMWFProducts", ProductStatusMessages.encodeForEmailBody(resumedMessage));
        request.setAttribute("productTips", tips);
    }

    /**
     * Builds a bullet-list rendering of a {@link ProductMetadata} field (description or tips): one bullet line per
     * distinct product type currently shown in the table (falling back to the generic, all-types entry when no
     * type-specific value is configured), or the plain generic value when no type-specific rows apply/resolve. Used
     * both for the {@code {{DESCRIPTION}}} placeholder and for the aggregated Tips info panel shown on the
     * product/cycle overview page (which has no single "current type").
     *
     * @param metadata
     *            all configured metadata entries for the product
     * @param stepStatii
     *            the product step statii currently shown in the table
     * @param getter
     *            the field accessor ({@link ProductMetadata#getDescription()} or {@link ProductMetadata#getTips()})
     *
     * @return the text (bullet list, plain text, or {@code null} if nothing is configured)
     */
    private static final String buildBulletedField(final List<ProductMetadata> metadata,
            final Collection<ProductStepStatus> stepStatii,
            final java.util.function.Function<ProductMetadata, String> getter) {
        final var byType = new LinkedHashMap<String, ProductMetadata>();
        for (final var m : metadata) {
            byType.put(m.getType(), m);
        }
        final var generic = byType.containsKey("") ? stripValue(getter.apply(byType.get(""))) : null;
        final var types = new TreeSet<String>();
        for (final var s : stepStatii) {
            final var type = s.getType();
            if (type != null && !type.isBlank()) {
                types.add(type);
            }
        }
        final var bullets = new LinkedHashMap<String, String>();
        for (final var type : types) {
            final var specific = byType.get(type);
            final var specificValue = specific != null ? stripValue(getter.apply(specific)) : null;
            final var value = specificValue != null ? specificValue : generic;
            if (value != null && !value.isBlank()) {
                bullets.put(type, value);
            }
        }
        if (bullets.isEmpty()) {
            return generic;
        }
        final var distinctValues = new java.util.LinkedHashSet<>(bullets.values());
        if (distinctValues.size() == 1) {
            // Every type currently shown resolves to the exact same text (most commonly because only the generic,
            // all-types entry is configured, with no per-type overrides): showing the same line once per type would
            // just be noisy repetition, so collapse to a single, plain entry instead.
            return distinctValues.iterator().next();
        }
        final var sb = new StringBuilder();
        for (final var e : bullets.entrySet()) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            // Any embedded line breaks in the value (e.g. a multi-line Tips/Description entry) are re-indented so
            // continuation lines line up under the text rather than falling back flush against the left edge of the
            // card.
            final var indented = e.getValue().replace("\r\n", "\n").replace("\n", "\n  ");
            sb.append("- ").append(e.getKey()).append(": ").append(indented);
        }
        return sb.toString();
    }

    /**
     * Trims leading/trailing whitespace from a stored metadata value (description or tips), so stray spaces/tabs
     * accidentally saved around the text (e.g. via copy/paste) do not show up as visual misalignment in the bullet list
     * or info panel.
     *
     * @param value
     *            the raw value, possibly {@code null}
     *
     * @return the trimmed value, or {@code null} if it was {@code null} or blank
     */
    private static final String stripValue(final String value) {
        if (value == null) {
            return null;
        }
        final var stripped = value.strip();
        return stripped.isEmpty() ? null : stripped;
    }

    /**
     * Looks up a single field (description or tips) for a product type, falling back to the generic (all-types) entry
     * when no type-specific one is configured.
     *
     * @param metadata
     *            all configured metadata entries for the product
     * @param type
     *            the product type
     * @param getter
     *            the field accessor ({@link ProductMetadata#getDescription()} or {@link ProductMetadata#getTips()})
     *
     * @return the resolved field value, or {@code null} if nothing is configured
     */
    private static final String lookupField(final List<ProductMetadata> metadata, final String type,
            final java.util.function.Function<ProductMetadata, String> getter) {
        String specific = null;
        String generic = null;
        for (final var m : metadata) {
            if (type.equals(m.getType())) {
                specific = stripValue(getter.apply(m));
            } else if (m.isGeneric()) {
                generic = stripValue(getter.apply(m));
            }
        }
        return specific != null ? specific : generic;
    }
}
