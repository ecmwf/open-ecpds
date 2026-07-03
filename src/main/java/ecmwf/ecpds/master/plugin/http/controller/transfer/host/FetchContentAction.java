package ecmwf.ecpds.master.plugin.http.controller.transfer.host;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Retrieves the raw content of one or more remote paths/URIs via the host's configured ECtrans
 * module on the DataMover. Supports any protocol handled by ECtrans (HTTP, FTP, SFTP, S3, …).
 *
 * Accepts POST to /do/transfer/host/edit/fetchContent/{hostId} with parameters:
 *   sources  — newline-separated list of remote paths/URIs (max 5)
 *   maxBytes — maximum bytes to return per file (default 65536)
 *
 * Returns JSON: {"output": "...", "mover": "..."} or {"error": "..."}
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ecmwf.common.text.Format;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.model.users.User;

/**
 * The Class FetchContentAction.
 */
public class FetchContentAction extends PDSAction {

    private static final Logger _log = LogManager.getLogger(FetchContentAction.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Maximum number of bytes returned per source file (256 KB — generous since line limit kicks in first). */
    private static final int DEFAULT_MAX_BYTES = 262144;

    /** Maximum number of lines returned per source file. */
    private static final int DEFAULT_MAX_LINES = 100;

    /** Maximum number of source files accepted per request. */
    private static final int MAX_SOURCES = 5;

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException {
        final var result = MAPPER.createObjectNode();
        try {
            final var hostId = ECMWFActionForm.getPathParameter(mapping, request, 0);
            final var sourcesParam = request.getParameter("sources");
            final var maxBytesParam = request.getParameter("maxBytes");
            final var maxLinesParam = request.getParameter("maxLines");

            if (hostId == null || hostId.isBlank()) {
                writeError(response, result, "Missing host ID");
                return null;
            }
            if (sourcesParam == null || sourcesParam.isBlank()) {
                writeError(response, result, "Missing sources parameter");
                return null;
            }

            final var host = MasterManager.getDB().getHost(hostId);
            if (host == null) {
                writeError(response, result, "Host not found: " + hostId);
                return null;
            }

            int maxBytes;
            try {
                maxBytes = maxBytesParam != null ? Integer.parseInt(maxBytesParam) : DEFAULT_MAX_BYTES;
                if (maxBytes <= 0 || maxBytes > 1048576)
                    maxBytes = DEFAULT_MAX_BYTES;
            } catch (final NumberFormatException ignored) {
                maxBytes = DEFAULT_MAX_BYTES;
            }

            int maxLines;
            try {
                maxLines = maxLinesParam != null ? Integer.parseInt(maxLinesParam) : DEFAULT_MAX_LINES;
                if (maxLines <= 0 || maxLines > 10000)
                    maxLines = DEFAULT_MAX_LINES;
            } catch (final NumberFormatException ignored) {
                maxLines = DEFAULT_MAX_LINES;
            }

            final var sources = sourcesParam.lines().map(String::trim).filter(s -> !s.isBlank()).limit(MAX_SOURCES)
                    .toArray(String[]::new);

            final var session = Util.getECpdsSessionFromObject(user);
            final var filesArray = MAPPER.createArrayNode();
            for (final var source : sources) {
                final var fileNode = MAPPER.createObjectNode();
                fileNode.put("source", source);
                try {
                    final var content = MasterManager.getMI().fetchUrlContent(session, host, source, maxBytes);
                    fileNode.put("content", truncateLines(content, maxLines));
                } catch (final Exception e) {
                    fileNode.put("error", Format.getMessage(e));
                }
                filesArray.add(fileNode);
            }
            result.set("files", filesArray);

        } catch (final Exception e) {
            _log.warn("FetchContent error", e);
            writeError(response, result, Format.getMessage(e));
        }

        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            MAPPER.writeValue(response.getWriter(), result);
        } catch (final Exception e) {
            _log.error("Failed to write FetchContent JSON response", e);
        }
        return null;
    }

    /**
     * Truncate lines.
     */
    private static String truncateLines(final String content, final int maxLines) {
        if (content == null || content.isEmpty())
            return content;
        final var lines = content.split("\n", -1);
        if (lines.length <= maxLines)
            return content;
        final var sb = new StringBuilder();
        for (var i = 0; i < maxLines; i++) {
            sb.append(lines[i]).append('\n');
        }
        sb.append("[... ").append(lines.length - maxLines).append(" more line(s) truncated ...]");
        return sb.toString();
    }

    /**
     * Write error.
     */
    private static void writeError(final HttpServletResponse response, final ObjectNode result, final String msg) {
        result.put("error", msg);
        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            MAPPER.writeValue(response.getWriter(), result);
        } catch (final Exception ignored) {
        }
    }
}
