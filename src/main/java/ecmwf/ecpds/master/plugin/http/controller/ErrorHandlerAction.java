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

package ecmwf.ecpds.master.plugin.http.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.Globals;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import ecmwf.ecpds.master.plugin.http.ECMWFException;

public class ErrorHandlerAction extends Action {

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) {
        String message = (String) request.getAttribute("javax.servlet.error.message");
        String uri = (String) request.getAttribute("javax.servlet.error.request_uri");
        Integer code = (Integer) request.getAttribute("javax.servlet.error.status_code");
        Object exception = request.getAttribute(ECMWFAction.EXCEPTION_KEY);

        String displayTitle = getResources(request, Globals.MESSAGES_KEY).getMessage("errors." + code + ".title", code);
        if (displayTitle == null)
            displayTitle = code + " " + message;

        ActionMessages actionMessages;
        if (exception == null) {
            actionMessages = ECMWFActionForm.newErrors("errors." + code + ".message", uri, message);
        } else if (exception instanceof ECMWFException) {
            actionMessages = ECMWFActionForm.newErrors("errors.item", (ECMWFException) exception);
        } else if (exception instanceof Exception) {
            actionMessages = ECMWFActionForm.newErrors("errors." + code + ".message", uri,
                    ((Exception) exception).getMessage());
        } else {
            actionMessages = ECMWFActionForm.newErrors("errors." + code + ".message", uri, exception.toString());
        }

        request.setAttribute(Globals.ERROR_KEY, actionMessages);
        request.setAttribute("errorTitle", displayTitle);
        return mapping.findForward("error");
    }
}
