package ecmwf.common.text;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Public login messaging shared by the Monitor and Data Portal.
 */
public final class LoginPageContent {
    public static final String GROUP = "LoginPageContent";
    public static final String KEY = "configuration";
    public static final String DOCUMENTATION = "https://ecmwf.github.io/open-ecpds/";
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    private static final Set<String> AUDIENCES = Set.of("both", "monitor", "portal");

    public record Message(String text, String article, String label, String audience, boolean enabled,
            boolean markdown) {
        public Message(final String text, final String article, final String label, final String audience,
                final boolean enabled) {
            this(text, article, label, audience, enabled, false);
        }

        public boolean isMarkdown() {
            return markdown;
        }

        public String getText() {
            return text;
        }

        public String getArticle() {
            return article;
        }

        public String getLabel() {
            return label;
        }

        public String getAudience() {
            return audience;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }

    public record Configuration(boolean enabled, List<Message> messages) {
        public boolean isEnabled() {
            return enabled;
        }
    }

    private LoginPageContent() {
    }

    public static Configuration defaults() {
        return new Configuration(true, List.of(
                new Message("Your data. Your protocol. Your choice.", "concepts/protocols/",
                        "Explore supported protocols", "both", true),
                new Message("Built at ECMWF. Built for data across domains.", "why-openecpds/", "Why OpenECPDS?",
                        "both", true),
                new Message("Acquire, distribute and share from one platform.", "", "Explore OpenECPDS", "both", true),
                new Message("Many protocols. One platform.", "use-cases/data-portal/", "Explore the Data Portal",
                        "both", true),
                new Message("Open source. No proprietary platform lock-in.", "why-openecpds/", "Discover the benefits",
                        "both", true),
                new Message("Connect on-premises systems and cloud storage.", "transfer-modules/",
                        "Explore transfer modules", "both", true)));
    }

    public static Configuration parse(final String json) {
        if (json == null) {
            return defaults();
        }
        if (json.length() > 30000) {
            throw new IllegalArgumentException("Login content is too large");
        }
        try {
            return validate(MAPPER.readValue(json, Configuration.class));
        } catch (final java.io.IOException e) {
            throw new IllegalArgumentException("Invalid login content configuration", e);
        }
    }

    public static Configuration validate(final Configuration config) {
        if (config == null || config.messages() == null || config.messages().size() > 50) {
            throw new IllegalArgumentException("Provide at most 50 messages");
        }
        for (final var message : config.messages()) {
            if (message == null || message.text() == null || message.text().isBlank() || message.text().length() > 160
                    || message.text().chars().anyMatch(c -> Character.isISOControl(c)
                            && !(message.markdown() && (c == '\n' || c == '\r' || c == '\t')))) {
                throw new IllegalArgumentException(
                        "Each message needs a phrase of at most 160 characters; line breaks require Markdown");
            }
            if (!AUDIENCES.contains(message.audience() == null ? "" : message.audience())) {
                throw new IllegalArgumentException("Select Monitor, Data Portal or both");
            }
            if (message.article() == null || !message.article().matches("(?:[a-z0-9]+(?:-[a-z0-9]+)*/)*")
                    || message.article().length() > 200) {
                throw new IllegalArgumentException("Articles must be documentation-relative paths ending in /");
            }
            if (message.label() == null || message.label().length() > 80
                    || message.label().chars().anyMatch(Character::isISOControl)
                    || (!message.article().isEmpty() && message.label().isBlank())) {
                throw new IllegalArgumentException("Article links need a single-line label of at most 80 characters");
            }
        }
        return new Configuration(config.enabled(), List.copyOf(config.messages()));
    }

    public static String serialize(final Configuration config) {
        try {
            return MAPPER.writeValueAsString(validate(config));
        } catch (final java.io.IOException e) {
            throw new IllegalArgumentException("Cannot serialize login content", e);
        }
    }

    public static String render(final Configuration config, final String audience) {
        validate(config);
        if (!config.enabled()) {
            return "";
        }
        final var messages = config.messages().stream().filter(message -> message.enabled()
                && ("both".equals(message.audience()) || audience.equals(message.audience()))).toList();
        final var html = new StringBuilder(
                """
                        <section class="ecpds-login-content" aria-label="Discover OpenECPDS">
                        <style>
                        #login-card:has(>.ecpds-login-content) #login-form{padding-bottom:.35rem}
                        .ecpds-login-content{padding:.25rem 2rem .65rem;display:grid;grid-template-columns:minmax(0,1fr);gap:.25rem;text-align:center;font-size:.8rem;line-height:1.45;color:var(--bs-secondary-color,#59636e)}
                        .ecpds-login-content .login-message{margin:0;transition:opacity .2s ease}
                        .ecpds-login-content .login-message[hidden]{display:none}
                        .ecpds-login-content .login-message p{margin:0 0 .2rem;overflow-wrap:anywhere}
                        .ecpds-login-content .login-message-text{margin:0 0 .2rem;overflow-wrap:anywhere}
                        .ecpds-login-content a{color:var(--bs-link-color,#0d6efd);text-decoration:none}
                        .ecpds-login-content .login-message a{font-size:.75rem}
                        .ecpds-login-content .login-message a:hover{text-decoration:underline}
                        .ecpds-login-content .login-content-controls{display:flex;gap:.15rem;align-items:center;justify-content:center}
                        .ecpds-login-content .login-content-icon{display:inline-flex;align-items:center;justify-content:center;width:30px;height:30px;padding:0;border:0;border-radius:50%;background:transparent;color:inherit;cursor:pointer;transition:background .15s,color .15s}
                        .ecpds-login-content .login-content-icon:hover{background:rgba(13,110,253,.09);color:var(--bs-link-color,#0d6efd)}
                        .ecpds-login-content .login-content-icon:focus-visible{outline:2px solid var(--bs-link-color,#0d6efd);outline-offset:2px}
                        .ecpds-login-content .login-content-icon svg{width:16px;height:16px}
                        .ecpds-login-content .login-content-icon svg[hidden]{display:none}
                        @media(prefers-reduced-motion:reduce){.ecpds-login-content .login-message{transition:none}}
                        </style>
                        <div class="login-message-area" aria-live="off">
                        """);
        for (var i = 0; i < messages.size(); i++) {
            final var message = messages.get(i);
            html.append("<div class=\"login-message\"").append(i == 0 ? "" : " hidden")
                    .append("><div class=\"login-message-text\">").append(message.markdown()
                            ? MarkdownUtil.toLoginNoticeHtml(message.text()) : escape(message.text()))
                    .append("</div>");
            if (!message.article().isEmpty()) {
                html.append("<a target=\"_blank\" rel=\"noopener noreferrer\" href=\"").append(DOCUMENTATION)
                        .append(message.article()).append("\">").append(escape(message.label())).append("</a>");
            }
            html.append("</div>");
        }
        html.append(
                """
                        </div><div class="login-content-controls">
                        <a class="login-content-icon" href="https://ecmwf.github.io/open-ecpds/why-openecpds/" target="_blank" rel="noopener noreferrer" aria-label="About OpenECPDS" title="About OpenECPDS">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" aria-hidden="true"><circle cx="12" cy="12" r="9"/><path d="M12 11v6"/><circle cx="12" cy="7" r=".8" fill="currentColor" stroke="none"/></svg>
                        </a>
                        """);
        if (messages.size() > 1) {
            html.append(
                    """
                            <button type="button" class="login-content-pause login-content-icon btn" aria-pressed="false" aria-label="Pause messages" title="Pause messages">
                            <svg class="login-pause-symbol" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true"><path d="M9 6v12M15 6v12"/></svg>
                            <svg class="login-play-symbol" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round" aria-hidden="true" hidden><path d="m9 5 11 7-11 7z"/></svg>
                            </button>
                            """);
        }
        return html
                .append("""
                        </div></section>
                        <script>
                        (function(){
                          var script=document.currentScript,root=script.previousElementSibling;
                          var items=Array.from(root.querySelectorAll('.login-message'));
                          var button=root.querySelector('.login-content-pause');
                          if(items.length<2)return;
                          var motion=window.matchMedia('(prefers-reduced-motion: reduce)');
                          var index=0,paused=motion.matches,timer=null,fade=null;
                          function reserve(){
                            var height=0;
                            items.forEach(function(item){var hidden=item.hidden;item.hidden=false;height=Math.max(height,item.offsetHeight);item.hidden=hidden;});
                            root.querySelector('.login-message-area').style.minHeight=height+'px';
                          }
                          function clear(){clearTimeout(timer);clearTimeout(fade);items[index].style.opacity='';}
                          function schedule(){
                            clear();
                            if(paused||document.hidden||root.contains(document.activeElement)||root.matches(':hover'))return;
                            timer=setTimeout(function(){
                              items[index].style.opacity='0';
                              fade=setTimeout(function(){
                                items[index].hidden=true;items[index].style.opacity='';
                                index=(index+1)%items.length;items[index].hidden=false;schedule();
                              },motion.matches?0:200);
                            },8000);
                          }
                          function state(){
                            var label=paused?'Resume messages':'Pause messages';
                            button.setAttribute('aria-label',label);button.title=label;
                            button.setAttribute('aria-pressed',String(paused));
                            button.querySelector('.login-pause-symbol').toggleAttribute('hidden',paused);
                            button.querySelector('.login-play-symbol').toggleAttribute('hidden',!paused);
                            schedule();
                          }
                          button.addEventListener('click',function(){paused=!paused;state();});
                          root.addEventListener('focusin',clear);
                          root.addEventListener('focusout',function(){setTimeout(schedule,0);});
                          root.addEventListener('mouseenter',clear);
                          root.addEventListener('mouseleave',schedule);
                          document.addEventListener('visibilitychange',schedule);
                          motion.addEventListener('change',function(){if(motion.matches)paused=true;state();});
                          window.addEventListener('resize',reserve);
                          reserve();state();
                        }());
                        </script>
                        """)
                .toString();
    }

    public static String escape(final String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
