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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.ecpds.master.weather.WeatherAdministration;
import ecmwf.ecpds.master.weather.WeatherLimits;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

public final class NetworkWeatherAction extends PDSAction {
    private static final Logger LOG = LogManager.getLogger(NetworkWeatherAction.class);
    private static final String TOKEN = NetworkWeatherAction.class.getName() + ".csrf";
    private static final long CHOICES_TTL = 300_000;
    private static final Object CHOICES_LOCK = new Object();
    private static long choicesAt;
    private static String choicesJson;
    private static int choicesMaximum;

    @Override
    protected boolean isAuthorized(User user, String currentPath, HttpServletRequest request) throws ECMWFException {
        return super.isAuthorized(user, currentPath, request) && user.hasAccess("/do/admin/");
    }

    @Override
    public ActionForward safeAuthorizedPerform(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response, User user) throws ECMWFException, ClassCastException {
        response.setHeader("Cache-Control", "no-store");
        final var httpSession = request.getSession();
        final String token;
        synchronized (httpSession) {
            if (!(httpSession.getAttribute(TOKEN) instanceof String)) {
                httpSession.setAttribute(TOKEN, UUID.randomUUID().toString());
            }
            token = (String) httpSession.getAttribute(TOKEN);
        }
        request.setAttribute("nwCsrf", token);
        boolean saveFailed = false;
        try {
            final var management = MasterManager.getMI();
            final var session = Util.getECpdsSessionFromObject(user);
            if ("POST".equalsIgnoreCase(request.getMethod())) {
                final var limits = management.getWeatherAdministration(session).limits();
                final var submitted = request.getParameter("csrf");
                if (submitted == null || submitted.length() > 128 || !MessageDigest
                        .isEqual(token.getBytes(StandardCharsets.UTF_8), submitted.getBytes(StandardCharsets.UTF_8))) {
                    throw new IllegalArgumentException("The form expired or is invalid; reload before saving.");
                }
                final var operation = request.getParameter("op");
                if ("findCandidates".equals(operation)) {
                    final var candidates = management.findWeatherCandidates(session, request.getParameter("source"),
                            Integer.parseInt(request.getParameter("days")), request.getParameter("protocol"));
                    final var json = new StringBuilder("{\"candidates\":[");
                    var first = true;
                    for (final var candidate : candidates) {
                        final var path = candidate.path();
                        json.append(first ? "" : ",").append("{\"source\":").append(quote(path.sourceId()))
                                .append(",\"id\":").append(quote(path.hostId())).append(",\"target\":")
                                .append(quote(path.configuredTarget())).append(",\"method\":")
                                .append(quote(path.protocol())).append(",\"nickname\":")
                                .append(quote(candidate.nickname())).append(",\"group\":")
                                .append(quote(candidate.group())).append(",\"type\":").append(quote(path.leg()))
                                .append('}');
                        first = false;
                    }
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(json.append("]}").toString());
                    return null;
                }
                final long revision = Long.parseLong(request.getParameter("revision"));
                if ("save".equals(operation)) {
                    final var text = request.getParameter("paths");
                    request.setAttribute("nwPaths", text);
                    final var paths = WeatherAdministration.parsePaths(text, limits.maxSelectedPaths(),
                            limits.maxConfigurationBytes());
                    management.saveWeatherSelection(session, revision, paths);
                    request.setAttribute("nwSuccess", "Manual scope saved. This does not enable collection or probes.");
                } else if ("automation".equals(operation)) {
                    final var approved = WeatherAdministration.parsePaths(request.getParameter("approved"),
                            limits.maxCandidates(), limits.maxConfigurationBytes());
                    final var pins = WeatherAdministration.parsePaths(request.getParameter("pins"),
                            limits.maxSelectedPaths(), limits.maxConfigurationBytes());
                    final var exclusions = WeatherAdministration.parsePaths(request.getParameter("exclusions"),
                            limits.maxCandidates(), limits.maxConfigurationBytes());
                    final var policies = parseProbePolicies(request.getParameter("probePolicies"), limits);
                    management.configureWeatherAutomation(session,
                            new ecmwf.ecpds.master.weather.WeatherAutomation.Configuration(
                                    Long.parseLong(request.getParameter("automationRevision")) + 1,
                                    "on".equals(request.getParameter("automatic")),
                                    "on".equals(request.getParameter("historical")), approved, pins, exclusions,
                                    "on".equals(request.getParameter("probesEnabled")), policies,
                                    parseSourceGroups(request.getParameter("sourceGroups"), limits)));
                    request.setAttribute("nwSuccess",
                            "Automation policy saved. Discovery and probe status below reflect asynchronous execution.");
                } else if ("reassess".equals(operation)) {
                    management.reassessWeather(session);
                    request.setAttribute("nwSuccess",
                            "Reassessment requested; inspect last-run status after the scheduler runs.");
                } else if ("activate".equals(operation) || "deactivate".equals(operation)) {
                    final boolean enabled = "activate".equals(operation);
                    final var changed = management.setWeatherCollectionEnabled(session, revision, enabled);
                    request.setAttribute("nwSuccess", enabled
                            ? changed.collector().enabled() ? "Network Weather collection activated."
                                    : "Activation saved; collection is suspended until writer readiness recovers."
                            : "Network Weather collection deactivated. Already admitted socket work may finish.");
                } else {
                    throw new IllegalArgumentException("Unknown Network Weather operation");
                }
            }
        } catch (Exception e) {
            if ("POST".equalsIgnoreCase(request.getMethod()) && "findCandidates".equals(request.getParameter("op"))) {
                LOG.warn("Network Weather history search failed", e);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json;charset=UTF-8");
                try {
                    response.getWriter().write("{\"error\":" + quote(e.getMessage()) + "}");
                } catch (java.io.IOException writeError) {
                    throw new java.io.UncheckedIOException(writeError);
                }
                return null;
            }
            saveFailed = true;
            LOG.warn("Network Weather administration request failed", e);
            request.setAttribute("nwError", e.getMessage());
        }
        try {
            final var status = MasterManager.getMI().getWeatherAdministration(Util.getECpdsSessionFromObject(user));
            final var limits = status.limits();
            request.setAttribute("nwMaxSources", limits.maxSources());
            request.setAttribute("nwMaxSelectedPaths", limits.maxSelectedPaths());
            request.setAttribute("nwMaxCandidates", limits.maxCandidates());
            request.setAttribute("nwMaxProbePolicies", limits.maxProbePolicies());
            request.setAttribute("nwMaxConfigurationBytes", limits.maxConfigurationBytes());
            request.setAttribute("nwRevision", status.configurationRevision());
            if (request.getAttribute("nwPaths") == null) {
                request.setAttribute("nwPaths", WeatherAdministration.formatPaths(status.paths()));
            }
            request.setAttribute("nwUpdatedBy", status.updatedBy());
            request.setAttribute("nwUpdatedAt", status.updatedAt() == 0 ? "Not saved" : time(status.updatedAt()));
            request.setAttribute("nwControlError", status.error());
            request.setAttribute("nwCanSave", status.restored() && !status.saving());
            request.setAttribute("nwEnabled", status.collector().enabled());
            request.setAttribute("nwRequestedEnabled", status.requestedEnabled());
            request.setAttribute("nwActivationBlocker", status.activationBlocker());
            request.setAttribute("nwCanActivate", status.restored() && !status.saving() && !status.collector().enabled()
                    && status.activationBlocker() == null);
            request.setAttribute("nwCount", status.paths().size());
            request.setAttribute("nwCapabilities",
                    ordered("Passive collection", status.collectionCapability(), "Socket persistence",
                            status.socketPersistenceCapability(), "Automatic selection",
                            status.automaticSelectionCapability(), "Scheduled probes", status.probeCapability()));
            final var snapshot = status.collector();
            request.setAttribute("nwHealth",
                    ordered("Retained events", snapshot.retainedEvents(), "Retained connections",
                            snapshot.retainedConnections(), "Invalid evidence", snapshot.invalid(), "Expired evidence",
                            snapshot.expired(), "Out of scope", snapshot.outOfScope(), "Capacity rejections",
                            snapshot.capacityRejected(), "Unsynchronized evidence", snapshot.unsynchronized(),
                            "Signature-verified batches", status.verifiedBatches(),
                            "Source/signature/timing rejected batches", status.rejectedBatches(),
                            "Source verification deferred batches", status.deferredBatches()));
            final var writer = status.persistence();
            request.setAttribute("nwWriter",
                    ordered("Ready", writer.ready(), "Pending events", writer.queue().pending(), "Reserved bytes",
                            writer.queue().reservedBytes(), "Written events", writer.writtenEvents(), "Retention gaps",
                            writer.retentionGaps(), "Transaction failures", writer.transactionFailures(),
                            "Dropped persistence events", writer.queue().dropped(), "Expired persistence events",
                            writer.queue().expired()));
            request.setAttribute("nwWriterError", writer.error());
            final var summaries = new ArrayList<Map<String, Object>>();
            for (final var summary : snapshot.summaries()) {
                final Map<String, Object> row = new LinkedHashMap<>();
                row.put("source", summary.path().sourceId());
                row.put("host", summary.path().hostId());
                row.put("target", summary.path().configuredTarget());
                row.put("protocol", summary.path().protocol());
                row.put("type", summary.path().leg());
                row.put("success", summary.successes());
                row.put("failure", summary.failures());
                row.put("cancelled", summary.cancellations());
                row.put("unknown", summary.unknown());
                row.put("sockets", summary.sendingsWithSockets());
                summaries.add(row);
            }
            request.setAttribute("nwSummaries", summaries);
            final var sources = new ArrayList<Map<String, Object>>();
            for (final var source : status.sources()) {
                final Map<String, Object> row = new LinkedHashMap<>();
                row.put("source", source.sourceId());
                row.put("state", source.state());
                row.put("epoch", source.sourceEpoch());
                row.put("verified", source.verifiedAt() == 0 ? "Not verified" : time(source.verifiedAt()));
                row.put("rtt", source.roundTripMs());
                row.put("detail", source.detail());
                sources.add(row);
            }
            request.setAttribute("nwSources", sources);
            final var automation = MasterManager.getMI().getWeatherAutomation(Util.getECpdsSessionFromObject(user));
            final var config = automation.configuration();
            request.setAttribute("nwAutomationRevision", config.revision());
            request.setAttribute("nwAutomatic", config.automatic());
            request.setAttribute("nwHistorical", config.historical());
            request.setAttribute("nwApproved", WeatherAdministration.formatPaths(config.approved().stream().toList()));
            request.setAttribute("nwPins", WeatherAdministration.formatPaths(config.pins().stream().toList()));
            request.setAttribute("nwExclusions",
                    WeatherAdministration.formatPaths(config.exclusions().stream().toList()));
            request.setAttribute("nwProbesEnabled", config.probesEnabled());
            request.setAttribute("nwSourceGroups",
                    config.sourceGroups().entrySet().stream().sorted(Map.Entry.comparingByKey())
                            .map(e -> e.getKey() + " | " + e.getValue())
                            .collect(java.util.stream.Collectors.joining("\n")));
            request.setAttribute("nwProbePolicies",
                    config.probePolicies().stream()
                            .map(p -> WeatherAdministration.formatPaths(java.util.List.of(p.path())).strip() + " | "
                                    + p.port() + " | " + p.mode() + " | " + p.intervalSeconds())
                            .sorted().collect(java.util.stream.Collectors.joining("\n")));
            final Map<String, Object> automationInfo = new LinkedHashMap<>();
            automationInfo.put("State",
                    !automation.loaded() ? "Not loaded" : automation.running() ? "Running" : "Idle");
            automationInfo.put("Last attempt", time(automation.lastAttempt()));
            automationInfo.put("Last success", time(automation.lastSuccess()));
            automationInfo.put("Next run", time(automation.nextRun()));
            automationInfo.put("Candidates", automation.candidates());
            automationInfo.put("Added / removed", automation.added() + " / " + automation.removed());
            automationInfo.put("History rows read", automation.rows() + (automation.partial() ? " (partial)" : ""));
            request.setAttribute("nwAutomationInfo", displayRows(automationInfo));
            request.setAttribute("nwAutomationError", automation.error());
            final var rejected = automation.rejectedHosts();
            request.setAttribute("nwRejectedHosts", rejected == null ? List.of()
                    : rejected.stream().map(host -> Map.of("id", host.hostId(), "reason", host.reason())).toList());
            request.setAttribute("nwSelectionReasons", automation.changes());
            final var probeStatus = MasterManager.getMI().getWeatherProbes(Util.getECpdsSessionFromObject(user));
            final Map<String, Object> probeInfo = new LinkedHashMap<>();
            probeInfo.put("Scheduled probes", probeStatus.enabled() ? "Enabled" : "Disabled");
            probeInfo.put("Pending jobs", probeStatus.pending());
            probeInfo.put("Expired or cancelled", probeStatus.expired());
            probeInfo.put("Rejected results", probeStatus.rejected());
            request.setAttribute("nwProbeInfo", displayRows(probeInfo));
            final var probeRows = new ArrayList<Map<String, Object>>();
            for (final var c : probeStatus.completed()) {
                final Map<String, Object> row = new LinkedHashMap<>();
                row.put("source", c.job().policy().path().sourceId());
                row.put("host", c.job().policy().path().hostId());
                row.put("type", c.job().policy().path().leg());
                row.put("mode", c.job().policy().mode());
                row.put("outcome", c.result().outcome());
                row.put("endpoint", c.result().endpoint() == null ? "" : c.result().endpoint());
                row.put("latency", c.result().latencyMs() == null ? "" : c.result().latencyMs());
                row.put("received", time(c.receivedAt()));
                probeRows.add(row);
            }
            for (final var c : probeStatus.unavailable()) {
                final Map<String, Object> row = new LinkedHashMap<>();
                row.put("source", c.job().policy().path().sourceId());
                row.put("host", c.job().policy().path().hostId());
                row.put("type", c.job().policy().path().leg());
                row.put("mode", c.job().policy().mode());
                row.put("outcome", "SOURCE_UNAVAILABLE");
                row.put("endpoint", "");
                row.put("latency", "");
                row.put("received", time(c.receivedAt()));
                probeRows.add(row);
            }
            request.setAttribute("nwProbeRows", probeRows);
            request.setAttribute("nwChoices",
                    choices("true".equals(request.getParameter("refreshChoices")), limits.maxHostChoices()));
            request.setAttribute("nwLoaded", true);
        } catch (Exception e) {
            LOG.warn("Unable to load Network Weather administration snapshot", e);
            request.setAttribute("nwError", "Unable to load the Master snapshot: " + e.getMessage());
        }
        if (saveFailed)
            retainSubmittedForm(request);
        return mapping.findForward("form");
    }

    static void retainSubmittedForm(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod()))
            return;
        final var operation = request.getParameter("op");
        if ("save".equals(operation)) {
            request.setAttribute("nwPaths", request.getParameter("paths"));
        } else if ("automation".equals(operation)) {
            for (final var field : Map.of("approved", "nwApproved", "pins", "nwPins", "exclusions", "nwExclusions",
                    "sourceGroups", "nwSourceGroups", "probePolicies", "nwProbePolicies").entrySet())
                request.setAttribute(field.getValue(), request.getParameter(field.getKey()));
            request.setAttribute("nwAutomatic", "on".equals(request.getParameter("automatic")));
            request.setAttribute("nwHistorical", "on".equals(request.getParameter("historical")));
            request.setAttribute("nwProbesEnabled", "on".equals(request.getParameter("probesEnabled")));
        }
    }

    private static java.util.Set<ecmwf.ecpds.master.weather.WeatherProbe.Policy> parseProbePolicies(String text,
            WeatherLimits limits) {
        if (text == null || text.getBytes(StandardCharsets.UTF_8).length > limits.maxConfigurationBytes())
            throw new IllegalArgumentException("Probe policies are missing or exceed the configured "
                    + limits.maxConfigurationBytes() + "-byte limit");
        final var policies = new java.util.HashSet<ecmwf.ecpds.master.weather.WeatherProbe.Policy>();
        int lineNumber = 0;
        for (final var line : text.split("\\R")) {
            lineNumber++;
            if (line.isBlank())
                continue;
            final var fields = line.split("\\|", -1);
            if (fields.length != 8)
                throw new IllegalArgumentException("Probe policies, line " + lineNumber
                        + ": use source | host ID | target | method | type | service port | mode | seconds");
            try {
                if (java.util.Arrays.stream(ecmwf.ecpds.master.weather.WeatherProbe.Mode.values())
                        .noneMatch(mode -> mode.name().equals(fields[6].trim())))
                    throw new IllegalArgumentException("Choose mode TCP_CONNECT, MTR_ICMP or MTR_TCP");
                final var path = WeatherAdministration.parsePaths(String.join("|", java.util.Arrays.copyOf(fields, 5)),
                        1, limits.maxConfigurationBytes()).iterator().next();
                final var policy = new ecmwf.ecpds.master.weather.WeatherProbe.Policy(path,
                        Integer.parseInt(fields[5].trim()),
                        ecmwf.ecpds.master.weather.WeatherProbe.Mode.valueOf(fields[6].trim()),
                        Integer.parseInt(fields[7].trim()));
                if (!policies.add(policy))
                    throw new IllegalArgumentException("This probe policy is already listed");
                if (policies.size() > limits.maxProbePolicies())
                    throw new IllegalArgumentException(
                            "At most " + limits.maxProbePolicies() + " probe policies are allowed");
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Probe policies, line " + lineNumber + ", Host " + fields[1].trim()
                        + ": port and interval must be whole numbers", e);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Probe policies, line " + lineNumber + ", Host " + fields[1].trim() + ": " + e.getMessage(), e);
            }
        }
        return java.util.Set.copyOf(policies);
    }

    private static Map<String, String> parseSourceGroups(String text, WeatherLimits limits) {
        if (text == null || text.getBytes(StandardCharsets.UTF_8).length > limits.maxConfigurationBytes())
            throw new IllegalArgumentException(
                    "Source / transfer group candidates are missing or exceed the configured "
                            + limits.maxConfigurationBytes() + "-byte limit");
        final var groups = new java.util.HashMap<String, String>();
        int lineNumber = 0;
        for (final var line : text.split("\\R")) {
            lineNumber++;
            if (line.isBlank())
                continue;
            final var fields = line.split("\\|", -1);
            if (fields.length != 2 || fields[0].isBlank() || fields[1].isBlank())
                throw new IllegalArgumentException("Source / transfer group candidates, line " + lineNumber
                        + ": specify source | transfer group; neither value may be empty");
            if (groups.putIfAbsent(fields[0].trim(), fields[1].trim()) != null)
                throw new IllegalArgumentException("Source / transfer group candidates, line " + lineNumber
                        + ": source " + fields[0].trim() + " already has a transfer group");
            if (groups.size() > limits.maxSources())
                throw new IllegalArgumentException("Source/group scope exceeds the configured source limit");
        }
        return Map.copyOf(groups);
    }

    /**
     * Selector choices for the configuration builders. They only assist entry: the Master still validates every
     * submitted path. Shared by all administrators and refreshed at most every five minutes (or on explicit request) so
     * page loads do not repeatedly scan the Host table.
     */
    private static String choices(final boolean refresh, final int maximum) {
        synchronized (CHOICES_LOCK) {
            final var now = System.currentTimeMillis();
            if (choicesJson != null && !refresh && choicesMaximum == maximum && now - choicesAt < CHOICES_TTL) {
                return choicesJson;
            }
            final var json = new StringBuilder("{\"sources\":[");
            try {
                final var db = MasterManager.getDB();
                var first = true;
                for (final var mover : db.getTransferServerArray()) {
                    json.append(first ? "" : ",").append("{\"id\":").append(quote(mover.getName()))
                            .append(",\"group\":").append(quote(mover.getTransferGroupName())).append(",\"active\":")
                            .append(mover.getActive()).append('}');
                    first = false;
                }
                json.append("],\"protocols\":[");
                first = true;
                final var protocols = new java.util.TreeSet<String>();
                for (final var method : db.getTransferMethodArray())
                    protocols.add(method.getECtransModuleName());
                for (final var protocol : protocols) {
                    json.append(first ? "" : ",").append(quote(protocol));
                    first = false;
                }
                json.append("],\"groups\":[");
                first = true;
                for (final var group : db.getTransferGroupArray()) {
                    json.append(first ? "" : ",").append(quote(group.getName()));
                    first = false;
                }
                json.append("],\"hosts\":[");
                first = true;
                var count = 0;
                var truncated = false;
                for (final var host : db.getHostArray()) {
                    if (!host.getActive()
                            || !(ecmwf.ecpds.master.transfer.HostOption.DISSEMINATION.equals(host.getType())
                                    || ecmwf.ecpds.master.transfer.HostOption.ACQUISITION.equals(host.getType()))) {
                        continue;
                    }
                    if (++count > maximum) {
                        truncated = true;
                        break;
                    }
                    json.append(first ? "" : ",").append("{\"id\":").append(quote(host.getName()))
                            .append(",\"nickname\":").append(quote(host.getNickname())).append(",\"target\":")
                            .append(quote(host.getHost())).append(",\"method\":")
                            .append(quote(host.getTransferMethodName())).append(",\"group\":")
                            .append(quote(host.getTransferGroupName())).append(",\"port\":").append(probePort(host))
                            .append(",\"type\":").append(quote(host.getType().toUpperCase(java.util.Locale.ROOT)))
                            .append('}');
                    first = false;
                }
                json.append("],\"truncated\":").append(truncated).append(",\"loadedAt\":").append(now).append('}');
            } catch (final Exception e) {
                LOG.warn("Unable to load Network Weather selector choices", e);
                return "{\"sources\":[],\"groups\":[],\"hosts\":[],\"error\":"
                        + quote("Selector choices unavailable: " + e.getMessage()) + "}";
            }
            choicesAt = now;
            choicesMaximum = maximum;
            choicesJson = json.toString();
            return choicesJson;
        }
    }

    static Integer probePort(final ecmwf.common.database.Host host) {
        final var method = host.getTransferMethod();
        if (method == null || method.getECtransModuleName() == null) {
            return null;
        }
        final var module = method.getECtransModuleName().toLowerCase(java.util.Locale.ROOT);
        final var option = switch (module) {
        case "ftp" -> ecmwf.common.ectrans.ECtransOptions.HOST_FTP_PORT;
        case "ftps" -> ecmwf.common.ectrans.ECtransOptions.HOST_FTPS_PORT;
        case "sftp" -> ecmwf.common.ectrans.ECtransOptions.HOST_SFTP_PORT;
        case "http" -> ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_PORT;
        case "s3" -> ecmwf.common.ectrans.ECtransOptions.HOST_S3_PORT;
        case "gcs" -> ecmwf.common.ectrans.ECtransOptions.HOST_GCS_PORT;
        case "azure" -> ecmwf.common.ectrans.ECtransOptions.HOST_AZURE_PORT;
        case "ecauth" -> ecmwf.common.ectrans.ECtransOptions.HOST_ECAUTH_PORT;
        case "webdav" -> ecmwf.common.ectrans.ECtransOptions.HOST_WEBDAV_PORT;
        default -> null;
        };
        return option == null ? null : new ecmwf.common.ectrans.ECtransSetup(module, host.getData()).getInteger(option);
    }

    private static List<Map<String, Object>> ordered(final Object... keyValues) {
        final Map<String, Object> map = new LinkedHashMap<>();
        for (var i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return displayRows(map);
    }

    /** Map.Entry implementations are not readable by the Monitor's JSP EL resolver. */
    private static List<Map<String, Object>> displayRows(final Map<String, Object> values) {
        final List<Map<String, Object>> rows = new ArrayList<>();
        values.forEach((key, value) -> {
            final Map<String, Object> row = new LinkedHashMap<>();
            row.put("key", key);
            row.put("value", value);
            rows.add(row);
        });
        return rows;
    }

    private static String time(final long epochMillis) {
        return epochMillis <= 0 ? "Never" : java.time.Instant.ofEpochMilli(epochMillis)
                .truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString().replace('T', ' ').replace("Z", " UTC");
    }

    /** JSON string literal that is also safe inside an HTML script element. */
    public static String quote(final String value) {
        if (value == null) {
            return "null";
        }
        final var out = new StringBuilder(value.length() + 2).append('"');
        for (final var c : value.toCharArray()) {
            if (c == '"' || c == '\\') {
                out.append('\\').append(c);
            } else if (c < 0x20 || c == '<' || c == '>' || c == '&' || c == '\'' || c == 0x2028 || c == 0x2029) {
                out.append(String.format("\\u%04x", (int) c));
            } else {
                out.append(c);
            }
        }
        return out.append('"').toString();
    }
}
