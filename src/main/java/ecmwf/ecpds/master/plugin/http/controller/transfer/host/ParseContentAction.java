package ecmwf.ecpds.master.plugin.http.controller.transfer.host;

import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_PARSER;
import static ecmwf.common.ectrans.ECtransOptions.HOST_HTTP_PARSER_OPTIONS;

import java.time.Instant;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.common.ectrans.ECtransSetup;
import ecmwf.common.parser.core.ParserConfig;
import ecmwf.common.parser.core.RemoteListingEngine;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.model.users.User;

/** Parses already-loaded preview content without contacting remote file URLs. */
public class ParseContentAction extends PDSAction {
    private static final Logger LOG = LogManager.getLogger(ParseContentAction.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int MAX_CONTENT = 1048576;
    private static final int MAX_ROWS = 2000;

    @Override
    public ActionForward safeAuthorizedPerform(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response, User user) throws ECMWFException, ClassCastException {
        var result = MAPPER.createObjectNode();
        try {
            if (!"POST".equals(request.getMethod())) {
                response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                throw new IllegalArgumentException("Parse Preview requires POST");
            }
            var hostId = ECMWFActionForm.getPathParameter(mapping, request, 0);
            var host = MasterManager.getDB().getHost(hostId);
            if (host == null) {
                throw new IllegalArgumentException("Host not found");
            }
            var body = request.getInputStream().readNBytes(8 * MAX_CONTENT + 1);
            if (body.length > 8 * MAX_CONTENT) {
                throw new IllegalArgumentException("Parse Preview request exceeds 8 MB");
            }
            var input = MAPPER.readTree(body);
            if (input == null || !input.isObject() || !input.path("content").isTextual()
                    || (input.has("properties") && !input.path("properties").isTextual())) {
                throw new IllegalArgumentException("Expected JSON content and optional Properties text");
            }
            var content = input.path("content").asText();
            var properties = input.has("properties") ? input.path("properties").asText() : null;
            if (content == null || content.length() > MAX_CONTENT) {
                throw new IllegalArgumentException("Preview content is missing or exceeds 1 MB");
            }
            if (properties != null && properties.length() > 131072) {
                throw new IllegalArgumentException("Properties exceed 128 KB");
            }
            var setup = new ECtransSetup("http", properties != null ? properties : host.getData());
            result.put("parser", setup.getString(HOST_HTTP_PARSER));
            var config = ParserConfig.forHttpListing(setup.getString(HOST_HTTP_PARSER),
                    setup.getString(HOST_HTTP_PARSER_OPTIONS));
            result.put("parser", config.getType());
            result.set("options", MAPPER.valueToTree(config.getProperties()));
            var entries = new RemoteListingEngine().process(config, content);
            result.put("total", entries.length);
            result.put("limited", entries.length > MAX_ROWS);
            int missingNames = 0, missingUrls = 0, nonHttpUrls = 0, missingSizes = 0, missingTimes = 0;
            for (var entry : entries) {
                if (entry.name == null || entry.name.isBlank()) {
                    missingNames++;
                }
                if (entry.link == null || entry.link.isBlank()) {
                    missingUrls++;
                } else if (!entry.link.startsWith("http://") && !entry.link.startsWith("https://")
                        && entry.link.contains("://")) {
                    nonHttpUrls++;
                }
                if (entry.size < 0) {
                    missingSizes++;
                }
                if (entry.time < 0) {
                    missingTimes++;
                }
            }
            var checks = result.putObject("checks");
            checks.put("missingNames", missingNames);
            checks.put("missingUrls", missingUrls);
            checks.put("nonHttpUrls", nonHttpUrls);
            checks.put("missingSizes", missingSizes);
            checks.put("missingTimes", missingTimes);
            var rows = result.putArray("entries");
            for (int i = 0; i < Math.min(entries.length, MAX_ROWS); i++) {
                var entry = entries[i];
                var row = rows.addObject();
                row.put("record", i + 1);
                row.put("name", entry.name);
                row.put("url", entry.link);
                row.put("size", entry.size >= 0 ? Long.toString(entry.size) : null);
                row.put("timestamp", entry.time >= 0 ? Instant.ofEpochMilli(entry.time).toString() : null);
                row.put("raw", entry.line);
                var warnings = row.putArray("warnings");
                if (entry.name == null || entry.name.isBlank()) {
                    warnings.add("Name missing");
                }
                if (entry.link == null || entry.link.isBlank()) {
                    warnings.add("Missing URL: acquisition ignores this record");
                } else if (!entry.link.startsWith("http://") && !entry.link.startsWith("https://")
                        && entry.link.contains("://")) {
                    warnings.add("URL is not HTTP/HTTPS");
                }
                if (entry.size < 0) {
                    warnings.add("Size missing or invalid");
                }
                if (entry.time < 0) {
                    warnings.add("Timestamp missing or invalid");
                }
                row.put("metadataComplete", entry.size >= 0 && entry.time >= 0);
            }
        } catch (Exception e) {
            LOG.warn("Parse Preview failed: {}", Format.getMessage(e));
            result.put("error", Format.getMessage(e));
        }
        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            MAPPER.writeValue(response.getWriter(), result);
        } catch (Exception e) {
            LOG.error("Unable to write Parse Preview response", e);
        }
        return null;
    }
}
