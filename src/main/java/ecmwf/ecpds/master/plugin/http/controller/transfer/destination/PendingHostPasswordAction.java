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
 */

package ecmwf.ecpds.master.plugin.http.controller.transfer.destination;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import ecmwf.ecpds.extension.InteractiveLogins;
import ecmwf.ecpds.master.HostPasswordRotationStatus.Operation;
import ecmwf.ecpds.master.HostPasswordActivation;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.ecpds.master.plugin.http.dao.Util;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

public final class PendingHostPasswordAction extends PDSAction {
    private static final String CSRF = PendingHostPasswordAction.class.getName() + ".csrf";
    static final String ACTIVATION = PendingHostPasswordAction.class.getName() + ".activation";
    private static final int MAX_BODY = 32 * 1024;
    private static final ObjectMapper JSON = new ObjectMapper().enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS,
            DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);

    record PendingActivation(String destination, long host, UUID reference, HostPasswordActivation challenge,
            String code, long expires) implements java.io.Serializable {
        boolean matches(final String destination, final long host, final UUID reference) {
            return this.destination.equals(destination) && this.host == host && this.reference.equals(reference)
                    && expires > System.currentTimeMillis();
        }

        @Override
        public String toString() {
            return "Pending password activation [redacted]";
        }
    }

    @Override
    protected boolean isAuthorized(final User user, final String path, final HttpServletRequest request) {
        // The authorized destination comes from the resolved mapping and is checked by the Master.
        return true;
    }

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");
        if (!request.isSecure()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        response.setContentType("application/json; charset=UTF-8");
        final boolean status = "GET".equals(request.getMethod());
        if (!status && !"POST".equals(request.getMethod())) {
            response.setHeader("Allow", "GET, POST");
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return null;
        }
        final var session = request.getSession(false);
        final var supplied = request.getHeader("X-ECPDS-Rotation-CSRF");
        if (session == null || !status && (!(session.getAttribute(CSRF) instanceof String expected) || supplied == null
                || supplied.length() != expected.length()
                || !MessageDigest.isEqual(supplied.getBytes(StandardCharsets.UTF_8),
                        expected.getBytes(StandardCharsets.UTF_8)))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        try {
            final var path = mapping.getParameter().split("\\|", -1);
            if (path.length != 2 || !user.hasAccess("/do/transfer/destination/operations/" + path[0] + "/")
                    || request.getQueryString() != null) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return null;
            }
            final var host = Long.parseLong(path[1]);
            Operation operation = Operation.STATUS;
            UUID reference = null;
            String password = null;
            String activationOperation = null;
            String activationState = null;
            long retirementGraceSeconds = 86400;
            if (!status) {
                if (!"application/json".equalsIgnoreCase(request.getContentType())) {
                    response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
                    return null;
                }
                final var bytes = request.getInputStream().readNBytes(MAX_BODY + 1);
                try {
                    if (bytes.length > MAX_BODY) {
                        response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
                        return null;
                    }
                    final var json = JSON.readTree(bytes);
                    final var name = json.path("operation").textValue();
                    if ("BEGIN_ACTIVATION".equals(name) || "ACTIVATE".equals(name)) {
                        activationOperation = name;
                        activationState = json.path("state").textValue();
                    } else {
                        operation = Operation.valueOf(name);
                    }
                    if (activationOperation == null && operation == Operation.STATUS) {
                        throw new IllegalArgumentException();
                    }
                    if (json.has("reference")) {
                        reference = UUID.fromString(json.path("reference").textValue());
                    }
                    password = json.path("password").textValue();
                    if (json.has("retirementGraceHours")) {
                        final var hours = json.path("retirementGraceHours");
                        if (!hours.isIntegralNumber() || !hours.canConvertToLong() || hours.longValue() < 1
                                || hours.longValue() > 720) {
                            throw new IllegalArgumentException();
                        }
                        retirementGraceSeconds = hours.longValue() * 3600;
                    }
                } finally {
                    java.util.Arrays.fill(bytes, (byte) 0);
                }
            }
            if (activationOperation != null) {
                if (reference == null) {
                    throw new IllegalArgumentException();
                }
                final HostPasswordActivation result;
                if ("BEGIN_ACTIVATION".equals(activationOperation)) {
                    final var existing = session.getAttribute(ACTIVATION);
                    if (password != null || activationState != null) {
                        throw new IllegalArgumentException();
                    }
                    if (existing instanceof PendingActivation pending && pending.matches(path[0], host, reference)) {
                        final var challenge = pending.challenge();
                        result = new HostPasswordActivation(challenge.state(),
                                pending.code() == null ? challenge.authorizationUri() : null,
                                challenge.localPasswordRequired(), false);
                    } else {
                        result = MasterManager.getPasswordRotationMI().activateHostPassword(
                                Util.getECpdsSessionFromObject(user), path[0], host,
                                new HostPasswordActivation.Request(reference, null, null, null));
                        session.setAttribute(ACTIVATION, new PendingActivation(path[0], host, reference, result, null,
                                System.currentTimeMillis() + 5 * 60_000));
                    }
                } else {
                    final PendingActivation pending;
                    synchronized (session) {
                        final var candidate = session.getAttribute(ACTIVATION);
                        session.removeAttribute(ACTIVATION);
                        if (!(candidate instanceof PendingActivation found) || !found.matches(path[0], host, reference)
                                || activationState == null || !activationState.equals(found.challenge().state())) {
                            throw new IllegalArgumentException();
                        }
                        pending = found;
                    }
                    result = MasterManager.getPasswordRotationMI().activateHostPassword(
                            Util.getECpdsSessionFromObject(user), path[0], host,
                            new HostPasswordActivation.Request(reference, activationState, pending.code(), password));
                }
                JSON.writeValue(response.getWriter(), result);
                return null;
            }
            final var result = MasterManager.getPasswordRotationMI().pendingHostPassword(
                    Util.getECpdsSessionFromObject(user), path[0], host, operation, reference, password,
                    retirementGraceSeconds);
            if (status) {
                synchronized (session) {
                    if (!(session.getAttribute(CSRF) instanceof String)) {
                        session.setAttribute(CSRF, InteractiveLogins.random());
                    }
                }
                response.setHeader("X-ECPDS-Rotation-CSRF", (String) session.getAttribute(CSRF));
            }
            JSON.writeValue(response.getWriter(), result);
        } catch (final Exception _) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try {
                JSON.writeValue(response.getWriter(), Map.of("error", "Pending password operation failed"));
            } catch (final IOException _) {
                throw new ECMWFException("Cannot report pending password operation failure");
            }
        }
        return null;
    }
}
