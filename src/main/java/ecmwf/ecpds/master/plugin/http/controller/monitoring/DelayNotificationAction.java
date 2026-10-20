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

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.common.text.MarkdownUtil;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * The Delay Notification page: lets the operators send one consistent notice for all the products and cycles affected
 * by a delay, then inform of the (partial) resolution, update, cancel or delete it. Messages can be opened in Outlook
 * or queued through the Master mail service; this action works out the recipients and keeps the register of the notices
 * in the database. Outlook messages await operator confirmation, while messages queued directly are acknowledged
 * automatically. The recipients are always recomputed here, never taken from the browser.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 8.2.0
 *
 * @since 2026-10-12
 */
public class DelayNotificationAction extends PDSAction {

    private static final Logger _log = LogManager.getLogger(DelayNotificationAction.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final int MAX_TEXT = 8000;

    private static final int DEFAULT_LIMIT = 100;

    private static final int MAX_LIMIT = 5000;

    /** {@inheritDoc} */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFActionFormException {
        request.setAttribute("dnPreferenceUser", user.getUid());
        final var params = ECMWFActionForm.getPathParameters(mapping, request);
        final var action = params.isEmpty() ? "" : params.get(0).toString();
        if (action.isEmpty()) {
            return mapping.findForward("success");
        }
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        try {
            final Map<String, Object> out = new LinkedHashMap<>();
            final Map<String, Object> body = MAPPER.readValue(request.getInputStream(), new TypeReference<>() {
            });
            final var who = user.getName();
            switch (action) {
            case "data" -> data(body, out);
            case "report" -> report(body, out);
            case "markdown" -> {
                final var source = str(body.get("markdown"));
                out.put("html", MarkdownUtil.toMessageHtml(source));
                out.put("text", MarkdownUtil.toPlainText(source));
            }
            case "recipients" -> recipients(body, out);
            case "send" -> send(body, who, out);
            case "update" -> update(body, who, out);
            case "resolve" -> resolve(body, who, out);
            case "resend" -> resend(body, who, out);
            case "productemail" -> sendProductEmail(body, who, out);
            case "cancel" -> cancel(body, who, out);
            case "confirm" -> confirm(body, who);
            case "delete" -> {
                DelayNotices.delete(str(body.get("ref")));
                _log.info("Delay notice {} deleted by {}", body.get("ref"), who);
            }
            case "defaultcc" -> {
                requireOptionEnabled(ProductStatusMessages.NOTICE_ALLOW_CC_NAME, "Cc");
                validateAddresses(str(body.get("cc")));
                DelayNotices.setDefaultCc(str(body.get("cc")));
            }
            case "defaultbcc" -> {
                requireOptionEnabled(ProductStatusMessages.NOTICE_ALLOW_BCC_NAME, "Bcc");
                validateAddresses(str(body.get("bcc")));
                DelayNotices.setDefaultBcc(str(body.get("bcc")));
            }
            default -> throw new IllegalArgumentException("Unknown action: " + action);
            }
            out.put("success", true);
            response.getWriter().write(MAPPER.writeValueAsString(out));
        } catch (final Exception e) {
            _log.warn("DelayNotificationAction.{}", action, e);
            try {
                response.getWriter().write(MAPPER.writeValueAsString(
                        Map.of("success", false, "error", e.getMessage() == null ? e.toString() : e.getMessage())));
            } catch (final Exception ignored) {
                // Nothing more to do.
            }
        }
        return null;
    }

    private static String str(final Object o) {
        return o == null ? "" : String.valueOf(o).trim();
    }

    private static String text(final Map<String, Object> body, final String key) {
        final var v = body.get(key) == null ? "" : String.valueOf(body.get(key));
        if (v.length() > MAX_TEXT) {
            throw new IllegalArgumentException(key + " is too long");
        }
        return v;
    }

    private static void validateAddresses(final String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        for (final var address : value.split("[;,]", -1)) {
            if (!address.strip().matches(
                    "(?i)[A-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?(?:\\.[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?)+")) {
                throw new IllegalArgumentException("Enter valid email addresses separated by semicolons.");
            }
        }
    }

    private static String normalizeAddresses(final String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        validateAddresses(value);
        final var addresses = new ArrayList<String>();
        for (final var address : value.split("[;,]")) {
            if (!address.isBlank()) {
                addresses.add(address.trim());
            }
        }
        return String.join(",", addresses);
    }

    private static String productMessageSetting(final String name, final String defaultValue) throws Exception {
        final var value = MasterManager.getDB().getProductStatusMessage(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static void queueEmail(final String to, final String cc, final String bcc, final String subject,
            final String body, final boolean markdown) throws Exception {
        final var recipients = normalizeAddresses(to);
        if (recipients.isEmpty()) {
            throw new IllegalArgumentException("The To address is not configured");
        }
        MasterManager.getMI().sendNotificationEmail(recipients.split(",", 2)[0], recipients, normalizeAddresses(cc),
                normalizeAddresses(bcc), subject, markdown ? MarkdownUtil.toEmailHtml(body) : body);
    }

    /** Whether the Product Messages are written in Markdown (the default). */
    private static boolean markdownEnabled() {
        try {
            return optionEnabled(ProductStatusMessages.MARKDOWN_NAME);
        } catch (final Exception e) {
            return true;
        }
    }

    private void sendProductEmail(final Map<String, Object> body, final String who, final Map<String, Object> out)
            throws Exception {
        final var kind = str(body.get("kind"));
        if (!"delay".equals(kind) && !"resumed".equals(kind)) {
            throw new IllegalArgumentException("Unknown product email type");
        }
        final var product = str(body.get("product"));
        final var time = str(body.get("time"));
        final var allProducts = Boolean.TRUE.equals(body.get("allProducts"));
        final String label;
        if (allProducts) {
            label = "All Products";
        } else {
            if (!product.matches("[A-Za-z0-9._*-]{1,100}") || !time.isEmpty() && !time.matches("[A-Za-z0-9_-]{1,32}")) {
                throw new IllegalArgumentException("Invalid product or cycle");
            }
            label = time.isEmpty() ? product : time + "-" + product;
        }
        final var subject = ("delay".equals(kind) ? "ECMWF Products Delay (" : "ECMWF Products (") + label + ")";
        final var messageBody = text(body, "body");
        if (messageBody.isBlank()) {
            throw new IllegalArgumentException("The message body is required");
        }
        final var bcc = new ArrayList<String>();
        bcc.add(productMessageSetting(ProductStatusMessages.BCC_ADDRESS_NAME,
                ProductStatusMessages.DEFAULT_BCC_ADDRESS));
        if (allProducts) {
            for (final var entry : DelayRecipients.cyclesPerProduct().entrySet()) {
                for (final var cycle : entry.getValue()) {
                    bcc.add("ecpds-product-" + cycle + "-" + entry.getKey() + "@ecmwf.int");
                }
            }
        } else {
            bcc.add("ecpds-product-" + label + "@ecmwf.int");
        }
        queueEmail(
                productMessageSetting(ProductStatusMessages.TO_ADDRESS_NAME, ProductStatusMessages.DEFAULT_TO_ADDRESS),
                "", String.join(",", bcc), subject, messageBody, markdownEnabled());
        _log.info("Product {} email for {} queued directly by {}", kind, label, who);
        out.put("sentDirectly", true);
    }

    private static void markSent(final String ref, final int index, final String who) throws Exception {
        DelayNotices.modify(ref, notice -> {
            final var history = list(notice, "history");
            if (index < 0 || index >= history.size()) {
                throw new IllegalArgumentException("Unknown message");
            }
            final var entry = history.get(index);
            entry.put("confirmed", true);
            entry.put("confirmedAt", now());
            entry.put("confirmedBy", who);
            entry.put("sentVia", "direct");
        });
    }

    private static String noticeBcc(final Map<String, Object> entry) throws Exception {
        @SuppressWarnings("unchecked")
        final var emails = (Collection<String>) entry.get("emails");
        final var bcc = new ArrayList<String>();
        bcc.add(productMessageSetting(ProductStatusMessages.BCC_ADDRESS_NAME,
                ProductStatusMessages.DEFAULT_BCC_ADDRESS));
        if (emails != null) {
            bcc.addAll(emails);
        }
        bcc.add(str(entry.get("bcc")));
        return String.join(",", bcc);
    }

    private static void sendNoticeEmail(final String ref, final Map<String, Object> entry) throws Exception {
        final var kind = str(entry.get("kind"));
        final var updatePrefix = "update".equals(kind) ? "UPDATE - " : "";
        queueEmail(
                productMessageSetting(ProductStatusMessages.TO_ADDRESS_NAME, ProductStatusMessages.DEFAULT_TO_ADDRESS),
                str(entry.get("cc")), noticeBcc(entry), "[" + ref + "] " + updatePrefix + str(entry.get("subject")),
                str(entry.get("body")), !Boolean.FALSE.equals(entry.get("markdown")));
    }

    private void deliverDirectly(final String ref, final int index, final String who, final Map<String, Object> out) {
        try {
            final var notice = DelayNotices.get(ref);
            if (notice == null) {
                throw new IllegalArgumentException("Unknown notice: " + ref);
            }
            final var history = list(notice, "history");
            if (index < 0 || index >= history.size()) {
                throw new IllegalArgumentException("Unknown message");
            }
            sendNoticeEmail(ref, history.get(index));
            markSent(ref, index, who);
            _log.info("Message {} of delay notice {} queued directly by {}", index, ref, who);
            out.put("sentDirectly", true);
        } catch (final Exception e) {
            _log.warn("Direct email for delay notice {} could not be queued", ref, e);
            out.put("sentDirectly", false);
            out.put("deliveryError", e.getMessage() == null ? e.toString() : e.getMessage());
        }
    }

    private static boolean optionEnabled(final String name) throws Exception {
        return !"false".equals(MasterManager.getDB().getProductStatusMessage(name));
    }

    private static void requireOptionEnabled(final String name, final String label) throws Exception {
        if (!optionEnabled(name)) {
            throw new IllegalArgumentException("Additional " + label + " is disabled in Product Messages");
        }
    }

    private static String now() {
        final var f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return f.format(new Date());
    }

    private void data(final Map<String, Object> body, final Map<String, Object> out) throws Exception {
        var limit = DEFAULT_LIMIT;
        try {
            limit = Math.max(1, Math.min(MAX_LIMIT, Integer.parseInt(str(body.get("limit")))));
        } catch (final NumberFormatException e) {
            // Default limit.
        }
        out.put("cycles", DelayRecipients.cyclesPerProduct());
        out.put("minutesBeforeSchedule", DelayRecipients.minutesBeforeSchedulePerProduct());
        out.put("minutesBeforeSchedulePerCycle", DelayRecipients.minutesBeforeSchedulePerCycle());
        out.put("generationStatusPerProduct", DelayRecipients.generationStatusPerProduct());
        out.put("generationStatusPerCycle", DelayRecipients.generationStatusPerCycle());
        out.put("models", ProductModels.productsPerModel());
        out.put("defaultCc", DelayNotices.defaultCc());
        out.put("defaultBcc", DelayNotices.defaultBcc());
        out.put("allowNoticeCc", optionEnabled(ProductStatusMessages.NOTICE_ALLOW_CC_NAME));
        out.put("allowNoticeBcc", optionEnabled(ProductStatusMessages.NOTICE_ALLOW_BCC_NAME));
        out.put("markdownMessages", markdownEnabled());
        templates(out);
        final var notices = DelayNotices.list(limit);
        out.put("notices", notices);
        out.put("limit", limit);
        out.put("more", notices.size() >= limit);
    }

    private void report(final Map<String, Object> body, final Map<String, Object> out) throws Exception {
        final var fromDate = str(body.get("fromDate"));
        final var toDate = str(body.get("toDate"));
        final LocalDate from;
        final LocalDate to;
        try {
            from = LocalDate.parse(fromDate);
            to = LocalDate.parse(toDate);
        } catch (final DateTimeParseException e) {
            throw new IllegalArgumentException("Select a valid report date range");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("The report start date must not be after the end date");
        }
        out.put("notices", DelayNotices.list(from.toString(), to.toString()));
    }

    /**
     * Provides the messages managed in Product Messages (grouped variants, as a notice covers several cycles) and the
     * product descriptions, so that the notice body starts from the same text and placeholders.
     */
    private static void templates(final Map<String, Object> out) {
        final var md = markdownEnabled();
        var delay = ProductStatusMessages.defaultFor(ProductStatusMessages.DEFAULT_DELAY_MESSAGE_GROUPED, md);
        var resumed = ProductStatusMessages.defaultFor(ProductStatusMessages.DEFAULT_RESUMED_MESSAGE_GROUPED, md);
        final Map<String, String> descriptions = new LinkedHashMap<>();
        try {
            final var db = MasterManager.getDB();
            final var d = db.getProductStatusMessage(ProductStatusMessages.DELAY_MESSAGE_GROUPED_NAME);
            if (d != null) {
                delay = d;
            }
            final var r = db.getProductStatusMessage(ProductStatusMessages.RESUMED_MESSAGE_GROUPED_NAME);
            if (r != null) {
                resumed = r;
            }
            out.put("compactCycles",
                    !"false".equals(db.getProductStatusMessage(ProductStatusMessages.COMPACT_CYCLE_LISTS_NAME)));
            for (final var m : db.getProductMetadata()) {
                final var text = m.getDescription();
                if (m.isGeneric() && text != null && !text.isBlank()) {
                    descriptions.put(m.getProduct(), text.strip());
                }
            }
        } catch (final Exception e) {
            _log.warn("Cannot read the product messages, using the defaults", e);
        }
        var to = ProductStatusMessages.DEFAULT_TO_ADDRESS;
        var bcc = ProductStatusMessages.DEFAULT_BCC_ADDRESS;
        try {
            final var db = MasterManager.getDB();
            final var t = db.getProductStatusMessage(ProductStatusMessages.TO_ADDRESS_NAME);
            final var b = db.getProductStatusMessage(ProductStatusMessages.BCC_ADDRESS_NAME);
            to = t != null ? t : to;
            bcc = b != null ? b : bcc;
        } catch (final Exception e) {
            _log.warn("Cannot read the notification addresses, using the defaults", e);
        }
        out.put("toAddress", to);
        out.put("bccAddress", bcc);
        out.put("delayTemplate", delay);
        out.put("resumedTemplate", resumed);
        out.put("descriptions", descriptions);
    }

    /** Reads the cells (product, cycle and level). */
    private static List<Map<String, Object>> cells(final Map<String, Object> body) {
        final var result = new ArrayList<Map<String, Object>>();
        if (body.get("cells") instanceof final Collection<?> c) {
            for (final var o : c) {
                if (o instanceof final Map<?, ?> m) {
                    final var level = str(m.get("level"));
                    if (!level.isEmpty() && !"definite".equals(level) && !"possible".equals(level)) {
                        throw new IllegalArgumentException("Invalid level: " + level);
                    }
                    final var product = str(m.get("product"));
                    final var time = str(m.get("time"));
                    if (!product.matches("[A-Za-z0-9_.-]{1,64}") || !time.matches("[A-Za-z0-9_.-]{1,16}")) {
                        throw new IllegalArgumentException("Invalid product or cycle");
                    }
                    final var cell = new LinkedHashMap<String, Object>();
                    cell.put("product", product);
                    cell.put("time", time);
                    cell.put("level", level);
                    result.add(cell);
                }
            }
        }
        return result;
    }

    private static List<String> keys(final List<Map<String, Object>> cells) {
        final var keys = new ArrayList<String>();
        for (final var c : cells) {
            keys.add(c.get("product") + "@" + c.get("time"));
        }
        return keys;
    }

    private static void requireLevels(final List<Map<String, Object>> cells) {
        if (cells.isEmpty() || cells.stream().anyMatch(c -> str(c.get("level")).isEmpty())) {
            throw new IllegalArgumentException("Select at least one product and cycle (Definite or Possible)");
        }
    }

    private void recipients(final Map<String, Object> body, final Map<String, Object> out) throws Exception {
        final var r = DelayRecipients.resolve(keys(cells(body)), Boolean.TRUE.equals(body.get("includeNonMonitored")));
        out.put("destinations", r.destinations());
        out.put("emails", r.emails());
    }

    /** Copies the editable content of the request into the notice. */
    private static void fill(final Map<String, Object> notice, final Map<String, Object> body) {
        for (final var k : new String[] { "reason", "eta", "delay", "note", "cc", "bcc", "subject", "body" }) {
            notice.put(k, text(body, k));
        }
        notice.put("includeNonMonitored", Boolean.TRUE.equals(body.get("includeNonMonitored")));
        notice.put("includeCc", Boolean.TRUE.equals(body.get("includeCc")));
        notice.put("includeBcc", Boolean.TRUE.equals(body.get("includeBcc")));
    }

    /**
     * Creates a history entry: a message that is to be sent. It stays unconfirmed until the operator confirms having
     * sent it, and keeps its content so that it can be opened again in Outlook.
     */
    private static Map<String, Object> message(final String kind, final String by, final Map<String, Object> body,
            final Collection<String> emails) {
        final var h = new LinkedHashMap<String, Object>();
        h.put("kind", kind);
        h.put("at", now());
        h.put("by", by);
        h.put("subject", text(body, "subject"));
        h.put("body", text(body, "body"));
        h.put("markdown", markdownEnabled());
        h.put("cc", "true".equals(String.valueOf(body.get("includeCc"))) ? text(body, "cc") : "");
        h.put("bcc", "true".equals(String.valueOf(body.get("includeBcc"))) ? text(body, "bcc") : "");
        h.put("emails", new ArrayList<>(emails));
        h.put("confirmed", false);
        return h;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> list(final Map<String, Object> notice, final String key) {
        return (List<Map<String, Object>>) notice.computeIfAbsent(key, _ -> new ArrayList<>());
    }

    private void send(final Map<String, Object> body, final String who, final Map<String, Object> out)
            throws Exception {
        if (Boolean.TRUE.equals(body.get("includeCc"))) {
            requireOptionEnabled(ProductStatusMessages.NOTICE_ALLOW_CC_NAME, "Cc");
            validateAddresses(text(body, "cc"));
        }
        if (Boolean.TRUE.equals(body.get("includeBcc"))) {
            requireOptionEnabled(ProductStatusMessages.NOTICE_ALLOW_BCC_NAME, "Bcc");
            validateAddresses(text(body, "bcc"));
        }
        final var cells = cells(body);
        requireLevels(cells);
        final var r = DelayRecipients.resolve(keys(cells), Boolean.TRUE.equals(body.get("includeNonMonitored")));
        final var notice = new LinkedHashMap<String, Object>();
        notice.put("status", "active");
        notice.put("createdBy", who);
        fill(notice, body);
        notice.put("cells", cells);
        notice.put("destinations", new ArrayList<>(r.destinations().keySet()));
        notice.put("emails", new ArrayList<>(r.emails()));
        final var h = message("delay", who, body, r.emails());
        list(notice, "history").add(h);
        final var ref = DelayNotices.create(notice, "active", who);
        _log.info("Delay notice {} registered by {}", ref, who);
        out.put("ref", ref);
        out.put("index", 0);
        out.put("emails", r.emails());
        if (Boolean.TRUE.equals(body.get("direct"))) {
            deliverDirectly(ref, 0, who, out);
        }
    }

    private static void open(final Map<String, Object> notice, final String what) {
        final var status = str(notice.get("status"));
        if (!"active".equals(status) && !"partial".equals(status)) {
            throw new IllegalArgumentException("A " + status + " notice cannot be " + what);
        }
    }

    @SuppressWarnings("unchecked")
    private void update(final Map<String, Object> body, final String who, final Map<String, Object> out)
            throws Exception {
        if (Boolean.TRUE.equals(body.get("includeCc"))) {
            requireOptionEnabled(ProductStatusMessages.NOTICE_ALLOW_CC_NAME, "Cc");
            validateAddresses(text(body, "cc"));
        }
        if (Boolean.TRUE.equals(body.get("includeBcc"))) {
            requireOptionEnabled(ProductStatusMessages.NOTICE_ALLOW_BCC_NAME, "Bcc");
            validateAddresses(text(body, "bcc"));
        }
        final var cells = cells(body);
        requireLevels(cells);
        final var added = new ArrayList<String>();
        final var all = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        final var notice = DelayNotices.modify(str(body.get("ref")), n -> {
            open(n, "updated");
            // The resolved cells stay resolved unless they are selected again.
            final var selected = new ArrayList<>(cells);
            for (final var c : selected) {
                c.put("resolved", false);
            }
            for (final var p : (List<Map<String, Object>>) n.get("cells")) {
                if (Boolean.TRUE.equals(p.get("resolved")) && selected.stream().noneMatch(
                        c -> c.get("product").equals(p.get("product")) && c.get("time").equals(p.get("time")))) {
                    selected.add(p);
                }
            }
            final var r = DelayRecipients.resolve(keys(selected), Boolean.TRUE.equals(body.get("includeNonMonitored")));
            all.clear();
            added.clear();
            all.addAll((List<String>) n.get("emails"));
            for (final var e : r.emails()) {
                if (all.add(e)) {
                    added.add(e);
                }
            }
            final var destinations = new TreeSet<String>((List<String>) n.get("destinations"));
            destinations.addAll(r.destinations().keySet());
            fill(n, body);
            n.put("cells", selected);
            n.put("status", "active");
            n.put("destinations", new ArrayList<>(destinations));
            n.put("emails", new ArrayList<>(all));
            final var h = message("update", who, body, all);
            h.put("addedEmails", new ArrayList<>(added));
            list(n, "history").add(h);
        });
        _log.info("Delay notice {} updated by {}", notice.get("ref"), who);
        out.put("ref", notice.get("ref"));
        out.put("index", list(notice, "history").size() - 1);
        out.put("emails", all);
        out.put("addedEmails", added);
        if (Boolean.TRUE.equals(body.get("direct"))) {
            deliverDirectly(str(notice.get("ref")), (Integer) out.get("index"), who, out);
        }
    }

    @SuppressWarnings("unchecked")
    private void resolve(final Map<String, Object> body, final String who, final Map<String, Object> out)
            throws Exception {
        final var selected = new TreeSet<String>();
        if (body.get("cells") instanceof final Collection<?> c) {
            c.forEach(o -> selected.add(String.valueOf(o)));
        }
        final var allowCc = optionEnabled(ProductStatusMessages.NOTICE_ALLOW_CC_NAME);
        final var allowBcc = optionEnabled(ProductStatusMessages.NOTICE_ALLOW_BCC_NAME);
        final var emails = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        final var notice = DelayNotices.modify(str(body.get("ref")), n -> {
            open(n, "resolved");
            var stillOpen = 0;
            final var resolvedNow = new ArrayList<String>();
            for (final var cell : (List<Map<String, Object>>) n.get("cells")) {
                final var key = cell.get("product") + "@" + cell.get("time");
                if (!Boolean.TRUE.equals(cell.get("resolved")) && (selected.isEmpty() || selected.contains(key))) {
                    cell.put("resolved", true);
                    resolvedNow.add(key);
                } else if (!Boolean.TRUE.equals(cell.get("resolved"))) {
                    stillOpen++;
                }
            }
            if (resolvedNow.isEmpty()) {
                throw new IllegalArgumentException("Nothing to resolve");
            }
            // Same recipients as the notice, restricted to the resolved cells when the resolution is partial.
            emails.clear();
            if (stillOpen == 0) {
                emails.addAll((List<String>) n.get("emails"));
            } else {
                emails.addAll(DelayRecipients.resolve(resolvedNow, Boolean.TRUE.equals(n.get("includeNonMonitored")))
                        .emails());
                emails.retainAll((List<String>) n.get("emails"));
            }
            n.put("status", stillOpen == 0 ? "resolved" : "partial");
            if (stillOpen == 0) {
                n.put("resolvedAt", now());
                n.put("resolvedBy", who);
            }
            final var h = message("resolution", who, body, emails);
            h.put("cc", allowCc && Boolean.TRUE.equals(n.get("includeCc")) ? str(n.get("cc")) : "");
            h.put("bcc", allowBcc && Boolean.TRUE.equals(n.get("includeBcc")) ? str(n.get("bcc")) : "");
            h.put("cells", resolvedNow);
            list(n, "history").add(h);
        });
        _log.info("Delay notice {} resolved ({}) by {}", notice.get("ref"), notice.get("status"), who);
        out.put("ref", notice.get("ref"));
        out.put("index", list(notice, "history").size() - 1);
        out.put("emails", emails);
        out.put("status", notice.get("status"));
        out.put("allowNoticeCc", allowCc);
        out.put("allowNoticeBcc", allowBcc);
        if (Boolean.TRUE.equals(body.get("direct"))) {
            deliverDirectly(str(notice.get("ref")), (Integer) out.get("index"), who, out);
        }
    }

    private void cancel(final Map<String, Object> body, final String who, final Map<String, Object> out)
            throws Exception {
        final var notice = DelayNotices.modify(str(body.get("ref")), n -> {
            open(n, "cancelled");
            n.put("status", "cancelled");
            n.put("cancelledAt", now());
            n.put("cancelledBy", who);
            // No message is attached to a cancellation, so there is nothing to confirm.
            final var h = new LinkedHashMap<String, Object>();
            h.put("kind", "cancellation");
            h.put("at", now());
            h.put("by", who);
            h.put("confirmed", true);
            list(n, "history").add(h);
        });
        _log.info("Delay notice {} cancelled by {}", notice.get("ref"), who);
        out.put("emails", notice.get("emails"));
    }

    private void resend(final Map<String, Object> body, final String who, final Map<String, Object> out)
            throws Exception {
        final var ref = str(body.get("ref"));
        final int index;
        try {
            index = Integer.parseInt(str(body.get("index")));
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("Invalid message index");
        }
        final var notice = DelayNotices.get(ref);
        if (notice == null) {
            throw new IllegalArgumentException("Unknown notice: " + ref);
        }
        final var history = list(notice, "history");
        if (index < 0 || index >= history.size()) {
            throw new IllegalArgumentException("Unknown message");
        }
        if (Boolean.TRUE.equals(history.get(index).get("confirmed"))) {
            throw new IllegalArgumentException("This message is already confirmed as sent");
        }
        sendNoticeEmail(ref, history.get(index));
        markSent(ref, index, who);
        out.put("sentDirectly", true);
    }

    /** The operator confirms that a registered message was really sent. */
    private void confirm(final Map<String, Object> body, final String who) throws Exception {
        final int index;
        try {
            index = Integer.parseInt(str(body.get("index")));
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("Invalid message index");
        }
        DelayNotices.modify(str(body.get("ref")), n -> {
            final var history = list(n, "history");
            if (index < 0 || index >= history.size()) {
                throw new IllegalArgumentException("Unknown message");
            }
            final var h = history.get(index);
            h.put("confirmed", true);
            h.put("confirmedAt", now());
            h.put("confirmedBy", who);
            h.put("sentVia", "outlook");
        });
        _log.info("Message {} of delay notice {} confirmed as sent by {}", index, body.get("ref"), who);
    }
}
