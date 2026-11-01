package ecmwf.ecpds.master.plugin.http.controller.admin;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ecmwf.common.text.LoginPageContent;
import ecmwf.ecpds.master.DataBaseInterface;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.PDSAction;
import ecmwf.web.ECMWFException;
import ecmwf.web.model.users.User;

/**
 * Administration of public login phrases and documentation links.
 */
public class LoginPageContentAction extends PDSAction {
    private static final String TOKEN = LoginPageContentAction.class.getName() + ".csrf";
    private final DataBaseInterface database;

    public LoginPageContentAction() {
        this(null);
    }

    LoginPageContentAction(final DataBaseInterface database) {
        this.database = database;
    }

    @Override
    protected boolean isAuthorized(final User user, final String path, final HttpServletRequest request)
            throws ECMWFException {
        return super.isAuthorized(user, path, request) && user.hasAccess("/do/admin/");
    }

    @Override
    public ActionForward safeAuthorizedPerform(final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final User user)
            throws ECMWFException {
        response.setHeader("Cache-Control", "no-store");
        final String token;
        synchronized (request.getSession()) {
            if (!(request.getSession().getAttribute(TOKEN) instanceof String)) {
                request.getSession().setAttribute(TOKEN, UUID.randomUUID().toString());
            }
            token = (String) request.getSession().getAttribute(TOKEN);
        }
        request.setAttribute("loginContentCsrf", token);
        var config = LoginPageContent.defaults();
        try {
            final var db = database == null ? MasterManager.getDB() : database;
            config = LoginPageContent.parse(db.getMonitorConfigGroup(LoginPageContent.GROUP).get(LoginPageContent.KEY));
            if ("POST".equalsIgnoreCase(request.getMethod())) {
                final var submitted = request.getParameter("csrf");
                if (submitted == null || submitted.length() > 128 || !MessageDigest
                        .isEqual(token.getBytes(StandardCharsets.UTF_8), submitted.getBytes(StandardCharsets.UTF_8))) {
                    throw new IllegalArgumentException("The form expired; reload before saving.");
                }
                final var texts = request.getParameterValues("phrase");
                if (texts != null && texts.length > 50) {
                    throw new IllegalArgumentException("Provide at most 50 messages");
                }
                final var messages = new ArrayList<LoginPageContent.Message>();
                for (var i = 0; texts != null && i < texts.length; i++) {
                    messages.add(new LoginPageContent.Message(texts[i].trim(), value(request, "article", i),
                            value(request, "label", i), value(request, "audience", i),
                            Boolean.parseBoolean(value(request, "visible", i)),
                            Boolean.parseBoolean(optionalValue(request, "markdown", i))));
                }
                config = new LoginPageContent.Configuration("true".equals(request.getParameter("enabled")), messages);
                LoginPageContent.validate(config);
                final var operation = request.getParameter("op");
                if ("preview".equals(operation)) {
                    final var audience = request.getParameter("previewAudience");
                    if (!"monitor".equals(audience) && !"portal".equals(audience)) {
                        throw new IllegalArgumentException("Choose Monitor or Data Portal for preview");
                    }
                    response.setContentType("text/html;charset=UTF-8");
                    response.getWriter().write("<!doctype html><html lang=\"en\"><head><meta charset=\"UTF-8\">" + """
                            <meta name="viewport" content="width=device-width, initial-scale=1">
                            <title>Login content preview</title>
                            <script>
                            (function(){
                              var source=parent.document.documentElement;
                              function syncTheme(){
                                document.documentElement.setAttribute('data-bs-theme',
                                  source.getAttribute('data-bs-theme')==='dark'?'dark':'light');
                              }
                              syncTheme();
                              new MutationObserver(syncTheme).observe(source,
                                {attributes:true,attributeFilter:['data-bs-theme']});
                            }());
                            </script>
                            <link rel="stylesheet" href="/bootstrap/css/bootstrap.min.css">
                            <style>body{padding:1rem;background:var(--bs-body-bg);color:var(--bs-body-color)}</style>
                            </head><body>
                            """ + LoginPageContent.render(config, audience) + "</body></html>");
                    return null;
                }
                if (!"save".equals(operation)) {
                    throw new IllegalArgumentException("Unknown operation");
                }
                db.setMonitorConfigValue(LoginPageContent.GROUP, LoginPageContent.KEY,
                        LoginPageContent.serialize(config));
                request.setAttribute("loginContentSuccess", "Login page content saved.");
            }
        } catch (final Exception e) {
            request.setAttribute("loginContentError", e.getMessage());
        }
        request.setAttribute("loginContent", config);
        request.setAttribute("loginContentMessages", config.messages());
        return mapping.findForward("success");
    }

    private static String value(final HttpServletRequest request, final String name, final int index) {
        final var values = request.getParameterValues(name);
        if (values == null || index >= values.length) {
            throw new IllegalArgumentException("Incomplete message row");
        }
        return values[index].trim();
    }

    private static String optionalValue(final HttpServletRequest request, final String name, final int index) {
        final var values = request.getParameterValues(name);
        return values == null || index >= values.length ? "false" : values[index];
    }
}
