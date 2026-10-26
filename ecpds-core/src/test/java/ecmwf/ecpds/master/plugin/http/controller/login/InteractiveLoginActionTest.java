package ecmwf.ecpds.master.plugin.http.controller.login;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionMapping;

import ecmwf.common.database.Activity;
import ecmwf.common.database.WebUser;
import ecmwf.ecpds.extension.InteractiveLogins;
import ecmwf.ecpds.master.ECpdsSession;
import ecmwf.ecpds.master.ManagementInterface;
import ecmwf.web.model.users.User;

public final class InteractiveLoginActionTest {
    private static final String START = InteractiveLoginAction.class.getName() + ".start";
    private static final String PENDING = InteractiveLoginAction.class.getName() + ".pending";

    public static void main(final String[] args) throws Exception {
        final var user = new WebUser("alice");
        user.setActive(true);
        final var constructor = ECpdsSession.class.getDeclaredConstructor(WebUser.class, String.class, Activity.class);
        constructor.setAccessible(true);
        final var authenticated = constructor.newInstance(user, "monitor", null);
        final var binding = new String[1];
        final var completed = new int[1];
        final var management = (ManagementInterface) Proxy.newProxyInstance(ManagementInterface.class.getClassLoader(),
                new Class<?>[] { ManagementInterface.class }, (proxy, method, values) -> {
                    return switch (method.getName()) {
                    case "beginInteractiveLogin" -> {
                        binding[0] = (String) values[0];
                        yield new InteractiveLogins.Challenge("state",
                                "https://identity.example/authorize?state=state");
                    }
                    case "completeInteractiveLogin" -> {
                        completed[0]++;
                        check("state".equals(values[0]) && binding[0].equals(values[1]) && "code".equals(values[2]),
                                "Callback must use the server-side browser binding and state");
                        yield authenticated;
                    }
                    default -> throw new AssertionError("Unexpected management operation " + method.getName());
                    };
                });
        final var action = new InteractiveLoginAction(management);
        final var browser = new Browser();
        final var mapping = new ActionMapping();
        mapping.setPath("/interactiveLogin");
        browser.session.attributes.put(START, "start-token");
        browser.parameters.put("loginToken", new String[] { "start-token" });
        browser.method = "GET";
        check(action.safePerform(mapping, null, browser.request, browser.response) != null,
                "Login initiation must reject GET");
        browser.method = "POST";
        check(action.safePerform(mapping, null, browser.request, browser.response) == null,
                "Valid initiation must redirect");
        check(browser.location.startsWith("https://identity.example/") && browser.rotations == 1,
                "Initiation must rotate the session id and redirect only to the provider");
        check(browser.session.attributes.containsKey(PENDING), "Initiation must retain a browser binding");
        mapping.setPath("/interactiveCallback");
        browser.method = "GET";
        browser.parameters.clear();
        browser.parameters.put("state", new String[] { "wrong" });
        browser.parameters.put("code", new String[] { "code" });
        action.safePerform(mapping, null, browser.request, browser.response);
        check(completed[0] == 0, "Wrong state must not reach the Master");
        check(!browser.session.attributes.containsKey(PENDING), "Invalid callback must discard the browser attempt");

        mapping.setPath("/interactiveLogin");
        browser.method = "POST";
        browser.session.attributes.put(START, "start-token");
        browser.parameters.put("loginToken", new String[] { "start-token" });
        action.safePerform(mapping, null, browser.request, browser.response);
        final var oldSession = browser.session;
        mapping.setPath("/interactiveCallback");
        browser.method = "GET";
        browser.parameters.clear();
        browser.parameters.put("state", new String[] { "state" });
        browser.parameters.put("code", new String[] { "code" });
        check(action.safePerform(mapping, null, browser.request, browser.response) == null,
                "Valid callback must complete");
        check(completed[0] == 1 && oldSession.invalid && browser.session != oldSession,
                "Successful login must replace the anonymous session");
        check("alice".equals(((User) browser.session.attributes.get(User.SESSION_KEY)).getUid()),
                "Login must store the authorized local user");
        check("/do/start".equals(browser.location), "Callback must not accept an arbitrary return URL");
        action.safePerform(mapping, null, browser.request, browser.response);
        check(completed[0] == 1, "Callback replay must not reach the Master");
        System.out
                .println("PASS: Monitor initiation, callback state binding, replay rejection and session replacement");
    }

    private static void check(final boolean value, final String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }

    private static final class Session {
        private final Map<String, Object> attributes = new HashMap<>();
        private boolean invalid;
        private final HttpSession proxy = (HttpSession) Proxy.newProxyInstance(HttpSession.class.getClassLoader(),
                new Class<?>[] { HttpSession.class }, (object, method, values) -> {
                    return switch (method.getName()) {
                    case "getAttribute" -> attributes.get(values[0]);
                    case "setAttribute" -> {
                        attributes.put((String) values[0], values[1]);
                        yield null;
                    }
                    case "removeAttribute" -> attributes.remove(values[0]);
                    case "invalidate" -> {
                        invalid = true;
                        attributes.clear();
                        yield null;
                    }
                    default -> null;
                    };
                });
    }

    private static final class Browser {
        private Session session = new Session();
        private final Map<String, String[]> parameters = new HashMap<>();
        private String method;
        private String location;
        private int rotations;
        private final HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(), new Class<?>[] { HttpServletRequest.class },
                (object, operation, values) -> {
                    return switch (operation.getName()) {
                    case "getSession" -> {
                        if (session.invalid) {
                            session = new Session();
                        }
                        yield session.proxy;
                    }
                    case "getMethod" -> method;
                    case "isSecure" -> true;
                    case "getParameterValues" -> parameters.get(values[0]);
                    case "getParameterMap" -> parameters;
                    case "changeSessionId" -> {
                        rotations++;
                        yield "rotated";
                    }
                    case "getRemoteHost" -> "browser";
                    case "getHeader" -> "test-agent";
                    case "getContextPath" -> "";
                    default -> null;
                    };
                });
        private final HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(), new Class<?>[] { HttpServletResponse.class },
                (object, operation, values) -> {
                    if ("sendRedirect".equals(operation.getName())) {
                        location = (String) values[0];
                    }
                    return null;
                });
    }
}
