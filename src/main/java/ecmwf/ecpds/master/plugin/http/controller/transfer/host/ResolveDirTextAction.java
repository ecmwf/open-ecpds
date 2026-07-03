package ecmwf.ecpds.master.plugin.http.controller.transfer.host;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Resolves all static placeholder tokens in a plain-text Directory field and returns the resolved
 * text as JSON. Substitutes $host[...], $transferMethod[...], $ectransModule[...], and $date /
 * $dirdate tokens using the current date — without running a script on a DataMover.
 *
 * Accepts POST to /do/transfer/host/edit/resolveDirText/{hostId} with parameters:
 *   text — the plain-text directory content
 *
 * Returns JSON: {"output": "..."} or {"error": "..."}
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
 * The Class ResolveDirTextAction.
 */
public class ResolveDirTextAction extends PDSAction {

    private static final Logger _log = LogManager.getLogger(ResolveDirTextAction.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Maximum plain-text size accepted (128 KB). */
    private static final int MAX_TEXT_BYTES = 131072;

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
            final var text = request.getParameter("text");

            if (hostId == null || hostId.isBlank()) {
                writeError(response, result, "Missing host ID");
                return null;
            }
            if (text == null || text.isBlank()) {
                writeError(response, result, "Text is empty");
                return null;
            }
            if (text.length() > MAX_TEXT_BYTES) {
                writeError(response, result, "Text exceeds maximum allowed size");
                return null;
            }

            final var host = MasterManager.getDB().getHost(hostId);
            if (host == null) {
                writeError(response, result, "Host not found: " + hostId);
                return null;
            }

            _log.debug("ResolveDirText: host={}", hostId);

            final var session = Util.getECpdsSessionFromObject(user);
            result.put("output", MasterManager.getMI().resolveDirText(session, host, text));

        } catch (final Exception e) {
            _log.warn("ResolveDirText error", e);
            writeError(response, result, Format.getMessage(e));
        }

        try {
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            MAPPER.writeValue(response.getWriter(), result);
        } catch (final Exception e) {
            _log.error("Failed to write ResolveDirText JSON response", e);
        }
        return null;
    }

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
