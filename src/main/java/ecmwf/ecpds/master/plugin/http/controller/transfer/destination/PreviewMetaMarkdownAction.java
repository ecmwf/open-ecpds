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

package ecmwf.ecpds.master.plugin.http.controller.transfer.destination;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * Renders Markdown to the same restricted, allow-listed HTML used for the Opsview notes export
 * ({@link ExportDestinationMetaNotesAction}, via {@link ecmwf.common.text.MarkdownUtil#toSafeHtml}), so the live
 * preview shown next to the Ace editor on the Destination Metadata page for a {@code markdown} field always matches
 * what actually gets exported. Pure text transform - runs locally in this plugin, no RMI call to the Master Server
 * needed. Returns JSON: {@code {"success":true,"html":"..."}} or {@code {"success":false,"error":"..."}}.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2026-09-30
 */

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ecmwf.common.text.MarkdownUtil;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.users.User;

/**
 * The Class PreviewMetaMarkdownAction.
 */
public class PreviewMetaMarkdownAction extends PDSAction {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(PreviewMetaMarkdownAction.class);

    /** The Constant _mapper. */
    private static final ObjectMapper _mapper = new ObjectMapper();

    /**
     * {@inheritDoc}
     *
     * Safe authorized perform.
     */
    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFActionFormException {
        response.setContentType("application/json;charset=UTF-8");
        try {
            if (!GetDestinationMetaDataAction.canEditMeta(user)) {
                throw new IllegalStateException("Not authorized to edit destination metadata");
            }
            final var body = _mapper.readValue(request.getInputStream(), Map.class);
            final var markdown = (String) body.get("markdown");
            final var html = MarkdownUtil.toSafeHtml(markdown);
            final var result = _mapper.createObjectNode();
            result.put("success", true);
            result.put("html", html);
            response.getWriter().write(_mapper.writeValueAsString(result));
        } catch (final Exception e) {
            _log.warn("PreviewMetaMarkdownAction", e);
            try {
                final ObjectNode result = _mapper.createObjectNode();
                result.put("success", false);
                result.put("error", e.getMessage() != null ? e.getMessage() : "error");
                response.getWriter().write(_mapper.writeValueAsString(result));
            } catch (final Exception ignored) {
            }
        }
        return null; // already wrote response
    }
}
