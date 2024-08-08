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

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * Base Action for all PDS actions. If performs authorization before handling
 * the request to a subclass.
 *
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.common.text.Format;
import ecmwf.web.ECMWFException;
import ecmwf.web.controller.ECMWFAction;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.controller.ECMWFActionFormException;
import ecmwf.web.model.ModelBean;
import ecmwf.web.model.ModelException;
import ecmwf.web.model.users.User;

/**
 * The Class PDSAction.
 */
public abstract class PDSAction extends ECMWFAction {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(PDSAction.class);

    /** The Constant RETURN_AFTER_LOGIN_KEY. */
    public static final String RETURN_AFTER_LOGIN_KEY = "ecmwf.ecpds.RETURN_AFTER_LOGIN";

    /**
     * Do authorization check and delegate to child class implementing the abstract method.
     *
     * @param mapping
     *            the mapping
     * @param form
     *            the form
     * @param request
     *            the request
     * @param response
     *            the response
     *
     * @return the action forward
     *
     * @throws ECMWFException
     *             the ECMWF exception
     * @throws ClassCastException
     *             the class cast exception
     */
    @Override
    public ActionForward safePerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response)
            throws ECMWFException, ClassCastException {
        Date preUser = null;
        Date preExec = null;
        Date postExec = null;
        if (log.isDebugEnabled()) {
            preUser = new Date();
        }
        ActionForward forward = null;
        final var user = getUser(request);
        final var currentPath = request.getContextPath() + request.getServletPath() + mapping.getPath();
        if (user == null) {
            // Access Denied. No User.
            request.getSession().setAttribute(RETURN_AFTER_LOGIN_KEY, currentPath);
            forward = mapping.findForward("login");
        } else if (!user.hasAccess(currentPath)) {
            // Access Denied to THIS user!!
            request.getSession().setAttribute(RETURN_AFTER_LOGIN_KEY, currentPath);
            if (user.getCategories().isEmpty()) {
                request.setAttribute(Globals.ERROR_KEY, ECMWFActionForm.newErrors("errors.noAccess", user.getUid()));
            } else {
                request.setAttribute(Globals.ERROR_KEY,
                        ECMWFActionForm.newErrors("errors.accessDenied", user.getUid(), currentPath));
            }
            // In this case don't try certificate because we're not getting
            // anything better.
            forward = mapping.findForward("login_noauto");
        } else {
            // Access Granted
            if (log.isDebugEnabled()) {
                preExec = new Date();
            }
            try {
                forward = this.safeAuthorizedPerform(mapping, form, request, response, user);
            } catch (final ModelException e) {
                final var t = e.getCause();
                throw new ECMWFActionFormException(Format.getMessage(t != null ? t : e, "Server Error", 0));
            }
            if (log.isDebugEnabled()) {
                postExec = new Date();
            }
            if (log.isDebugEnabled()) {
                log.debug("TIME: " + request.getServletPath() + request.getPathInfo() + " => User info: "
                        + (preExec.getTime() - preUser.getTime()) / 1000.0 + " seconds. Data get (pre-forward): "
                        + (postExec.getTime() - preExec.getTime()) / 1000.0 + " seconds.");
            }

        }
        return forward;
    }

    /**
     * Safe authorized perform.
     *
     * @param mapping
     *            the mapping
     * @param form
     *            the form
     * @param request
     *            the request
     * @param response
     *            the response
     * @param user
     *            the user
     *
     * @return the action forward
     *
     * @throws ECMWFException
     *             the ECMWF exception
     * @throws ClassCastException
     *             the class cast exception
     */
    public abstract ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException, ClassCastException;

    /**
     * Gets the date options.
     *
     * @param daysBack
     *            the days back
     * @param all
     *            the all
     *
     * @return the date options
     */
    public static final Collection<String> getDateOptions(final int daysBack, final boolean all) {
        final var iso = getISOFormat();
        final var N = daysBack + 1;
        final List<String> l = new ArrayList<>(N + 1);
        final var c = Calendar.getInstance();
        c.setTime(new Date());
        l.add(iso.format(c.getTime()));
        for (var i = 0; i < N; i++) {
            c.add(Calendar.DATE, -1);
            l.add(iso.format(c.getTime()));
        }
        if (all) {
            l.add("All");
        }
        return l;
    }

    /**
     * Gets the ISO format.
     *
     * @return the ISO format
     */
    public static final SimpleDateFormat getISOFormat() {
        return new SimpleDateFormat("yyyy-MM-dd");
    }

    /**
     * Search.
     *
     * @param c
     *            the c
     * @param search
     *            the search
     *
     * @return the collection
     */
    public Collection<ModelBean> search(final Collection<? extends ModelBean> c, final String search) {
        final List<ModelBean> filtered = new ArrayList<>();
        final var bits = search.toLowerCase().split(" ");
        var match = false;
        for (final ModelBean b : c) {
            match = false;
            try {
                for (final String bit : bits) {
                    if (!(match = match(b, bit))) {
                        break;
                    }
                }
            } catch (final Exception e) {
                log.error("Problem filtering bean '" + b + "'", e);
            }
            if (match) {
                filtered.add(b);
            }
        }
        return filtered;
    }

    /**
     * Match.
     *
     * @param b
     *            the b
     * @param what
     *            the what
     *
     * @return true, if successful
     */
    public boolean match(final ModelBean b, final String what) {
        return b.getId().toLowerCase().contains(what);
    }
}
