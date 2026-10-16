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

import static ecmwf.common.ectrans.ECtransGroups.Module.DESTINATION_SCHEDULER;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_SCHEDULER_LIFETIME;
import static ecmwf.common.ectrans.ECtransOptions.DESTINATION_SCHEDULER_STORAGEPOLICY;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.common.database.TransferGroup;
import ecmwf.ecpds.master.DataBaseInterface;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.storage.StoragePolicy;
import ecmwf.ecpds.master.transfer.DestinationOption;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

/**
 * Creates, updates, orders and deletes the Storage Lifecycle Policies (Stage 1: the policies are only defined and
 * evaluated, no file is removed).
 */
public class StoragePoliciesAction extends PDSAction {

    private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_.-]{1,32}");

    private static final long HOUR = 3600L * 1000;

    private static final long DAY = 24 * HOUR;

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final DataBaseInterface db;
        try {
            db = MasterManager.getDB();
        } catch (final Exception e) {
            request.setAttribute("spError", "Unable to connect to the database: " + e.getMessage());
            return mapping.findForward("form");
        }
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            try {
                post(db, request, user.getName());
            } catch (final Exception e) {
                request.setAttribute("spError", e.getMessage());
            }
        }
        try {
            load(db, request);
        } catch (final Exception e) {
            request.setAttribute("spError", "Unable to load the policies (has docker/ecpds/database/storage-policy.sql"
                    + " been applied to the database?): " + e.getMessage());
        }
        return mapping.findForward("form");
    }

    private static void post(final DataBaseInterface db, final HttpServletRequest request, final String user)
            throws Exception {
        final var op = param(request, "op");
        switch (op) {
        case "save" -> save(db, request, user);
        case "delete" -> {
            final var name = param(request, "name");
            db.deleteStoragePolicy(name);
            request.setAttribute("spSuccess", "Policy \"" + name + "\" deleted.");
        }
        case "up", "down" -> move(db, param(request, "name"), "up".equals(op));
        case "test" -> test(db, request);
        default -> throw new IllegalArgumentException("Unknown operation");
        }
    }

    private static void save(final DataBaseInterface db, final HttpServletRequest request, final String user)
            throws Exception {
        final Map<String, String> row = new LinkedHashMap<>();
        final var original = param(request, "original");
        final var name = param(request, "name");
        row.put("name", name);
        row.put("comment", param(request, "comment"));
        row.put("active", request.getParameter("active") != null ? "1" : "0");
        row.put("mode", param(request, "mode"));
        row.put("destinations", param(request, "destinations"));
        row.put("types", String.join(",", values(request, "types")));
        row.put("groups", String.join(",", values(request, "groups")));
        row.put("pattern", param(request, "pattern"));
        row.put("window", String.valueOf(duration(request, "window", 7, DAY)));
        row.put("restart", request.getParameter("restart") != null ? "1" : "0");
        row.put("drop", request.getParameter("drop") != null ? "1" : "0");
        row.put("dropAfter", String.valueOf(duration(request, "dropAfter", 1, HOUR)));
        request.setAttribute("spEdit", row);
        request.setAttribute("spEditOriginal", original);
        if (!NAME.matcher(name).matches() || StoragePolicy.DEFAULT_NAME.equalsIgnoreCase(name)) {
            throw new IllegalArgumentException(
                    "Invalid name (1 to 32 letters, digits, '.', '_' or '-'; \"default\" is reserved).");
        }
        try {
            new StoragePolicy(row);
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        if (original.isEmpty()) {
            for (final var existing : db.getStoragePolicies()) {
                if (name.equals(existing.get("name"))) {
                    throw new IllegalArgumentException("A policy named \"" + name + "\" already exists.");
                }
            }
            db.insertStoragePolicy(row, user);
        } else if (!db.updateStoragePolicy(row, user)) {
            throw new IllegalArgumentException("Policy \"" + name + "\" does not exist anymore.");
        }
        request.removeAttribute("spEdit");
        request.removeAttribute("spEditOriginal");
        request.setAttribute("spSuccess", "Policy \"" + name + "\" saved.");
    }

    private static void move(final DataBaseInterface db, final String name, final boolean up) throws Exception {
        final List<String> names = new ArrayList<>();
        for (final var row : db.getStoragePolicies()) {
            names.add(row.get("name"));
        }
        final var index = names.indexOf(name);
        final var target = up ? index - 1 : index + 1;
        if (index >= 0 && target >= 0 && target < names.size()) {
            names.set(index, names.get(target));
            names.set(target, name);
            db.setStoragePolicyOrder(names);
        }
    }

    private static void test(final DataBaseInterface db, final HttpServletRequest request) throws Exception {
        final var destinationName = param(request, "testDestination");
        final var fileName = param(request, "testFile");
        request.setAttribute("spTestDestination", destinationName);
        request.setAttribute("spTestFile", fileName);
        final var destination = db.getDestination(destinationName, false);
        final var setup = DESTINATION_SCHEDULER.getECtransSetup(destination.getData());
        final var subject = new StoragePolicy.Subject(destination.getName(), destination.getType(),
                destination.getTransferGroupName(), fileName.isEmpty() ? null : fileName,
                setup.getString(DESTINATION_SCHEDULER_STORAGEPOLICY));
        final var match = StoragePolicy.select(StoragePolicy.fromRows(db.getStoragePolicies()), subject);
        final var policy = match.policy();
        final var lifetime = setup.getDuration(DESTINATION_SCHEDULER_LIFETIME);
        final var window = Duration.ofMillis(policy.getRequeueWindowMs());
        final var result = new StringBuilder("Policy \"" + policy.getName() + "\" (" + match.reason() + "). Requeue"
                + " window: " + describe(policy.getRequeueWindowMs()) + ".");
        if (lifetime != null && lifetime.isPositive() && lifetime.compareTo(window) < 0) {
            result.append(" The destination lifetime (").append(lifetime)
                    .append(") is shorter than the window: the lifetime wins, a policy never extends the retention.");
        }
        request.setAttribute("spTestResult", result.toString());
    }

    private static void load(final DataBaseInterface db, final HttpServletRequest request) throws Exception {
        final var rows = db.getStoragePolicies();
        final List<Map<String, String>> display = new ArrayList<>();
        for (final var row : rows) {
            final Map<String, String> copy = new LinkedHashMap<>(row);
            copy.put("windowText", describe(Long.parseLong(row.get("window"))));
            copy.put("dropAfterText", describe(Long.parseLong(row.get("dropAfter"))));
            final var types = new ArrayList<String>();
            for (final var type : StoragePolicy.splitList(row.get("types"))) {
                types.add(DestinationOption.getLabel(type));
            }
            copy.put("typesText", String.join(", ", types));
            copy.put("typesCount", String.valueOf(types.size()));
            copy.put("destinationsCount", String.valueOf(StoragePolicy.splitList(row.get("destinations")).size()));
            copy.put("groupsCount", String.valueOf(StoragePolicy.splitList(row.get("groups")).size()));
            display.add(copy);
        }
        request.setAttribute("spList", display);
        // Map.entry() values are not public classes, which EL cannot read: copy them into plain maps
        final var typeList = new ArrayList<Map<String, String>>();
        for (final var type : DestinationOption.getTypes(false)) {
            typeList.add(Map.of("key", String.valueOf(type.getKey()), "value", type.getValue()));
        }
        request.setAttribute("spTypes", typeList);
        final var groups = new ArrayList<String>();
        for (final TransferGroup group : db.getTransferGroupArray()) {
            groups.add(group.getName());
        }
        request.setAttribute("spGroups", groups);
        request.setAttribute("spDestinationOptions",
                Util.getDestinationPairList(DestinationHome.findAllNamesAndComments(), List.of()));
        final var edit = param(request, "edit");
        if (request.getAttribute("spEdit") == null) {
            if (!edit.isEmpty()) {
                for (final var row : rows) {
                    if (edit.equals(row.get("name"))) {
                        request.setAttribute("spEdit", row);
                        request.setAttribute("spEditOriginal", edit);
                    }
                }
            } else if (request.getParameter("new") != null) {
                request.setAttribute("spEdit",
                        Map.of("name", "", "active", "1", "mode", "DRY_RUN", "window",
                                String.valueOf(StoragePolicy.DEFAULT_REQUEUE_WINDOW_MS), "dropAfter",
                                String.valueOf(StoragePolicy.DEFAULT_DROP_REPLICAS_AFTER_MS)));
                request.setAttribute("spEditOriginal", "");
            }
        }
        final var editRow = (Map<?, ?>) request.getAttribute("spEdit");
        if (editRow != null) {
            final var window = Long.parseLong(String.valueOf(editRow.get("window")));
            final var dropAfter = Long.parseLong(String.valueOf(editRow.get("dropAfter")));
            request.setAttribute("spWindowValue", window % DAY == 0 ? window / DAY : window / HOUR);
            request.setAttribute("spWindowUnit", window % DAY == 0 ? "days" : "hours");
            request.setAttribute("spDropAfterValue", dropAfter % DAY == 0 ? dropAfter / DAY : dropAfter / HOUR);
            request.setAttribute("spDropAfterUnit", dropAfter % DAY == 0 ? "days" : "hours");
            request.setAttribute("spEditTypes", StoragePolicy.splitList((String) editRow.get("types")));
            request.setAttribute("spEditGroups", StoragePolicy.splitList((String) editRow.get("groups")));
        }
    }

    private static String describe(final long millis) {
        if (millis % DAY == 0) {
            return millis / DAY + (millis == DAY ? " day" : " days");
        }
        if (millis % HOUR == 0) {
            return millis / HOUR + (millis == HOUR ? " hour" : " hours");
        }
        return millis / 60000 + " minutes";
    }

    private static long duration(final HttpServletRequest request, final String name, final long defaultValue,
            final long defaultUnit) {
        final var text = param(request, name);
        final long amount;
        try {
            amount = text.isEmpty() ? defaultValue : Long.parseLong(text);
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number: " + text);
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Durations cannot be negative.");
        }
        final var unit = "hours".equals(param(request, name + "Unit")) ? HOUR : DAY;
        return amount * (text.isEmpty() ? defaultUnit : unit);
    }

    private static String param(final HttpServletRequest request, final String name) {
        final var value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    private static List<String> values(final HttpServletRequest request, final String name) {
        final var values = request.getParameterValues(name);
        return values == null ? List.of() : List.of(values);
    }
}
