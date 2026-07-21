<%@ page session="true" import ="ecmwf.web.view.taglibs.util.TagUtils"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>

<tiles:useAttribute id="title" name="title" ignore="true" classname="java.lang.String"/>
<tiles:useAttribute id="titleKey" name="title.key" ignore="true" classname="java.lang.String"/>
<tiles:useAttribute id="titleBeanName" name="title.bean.name" ignore="true" classname="java.lang.String"/>
<tiles:useAttribute id="titleBeanProperty" name="title.bean.property" ignore="true" classname="java.lang.String"/>
<tiles:useAttribute id="titleExpression" name="title.expression" ignore="true" classname="java.lang.String"/>

<%
    String theTitle = null;
    if (titleExpression != null) theTitle = TagUtils.parseMultiExpressionText(pageContext, titleExpression);
    else if (titleBeanName != null && titleBeanProperty != null) theTitle = TagUtils.resolveQualifiedName(pageContext, titleBeanName + "." + titleBeanProperty).toString();
    else if (titleBeanName != null) theTitle = TagUtils.resolveQualifiedName(pageContext, titleBeanName).toString();
    else if (titleKey != null) theTitle = TagUtils.getResource(pageContext, titleKey, "Resource not found");
    else if (title != null) theTitle = title;
    else theTitle = "Title not set";
%>

<tiles:insert name="html.head">
    <tiles:put name="title"><%=theTitle%></tiles:put>
</tiles:insert>

<tiles:insert name="footer">
    <tiles:put name="helpKey"><tiles:getAsString name="helpKey" ignore="true"/></tiles:put>
</tiles:insert>

<style>
body{background:#fff;color:#000;font-family:Arial,sans-serif;margin:0;padding:0;}
#loadingBackdrop,#loadingDiv{display:none;position:fixed;inset:0;z-index:99999;}
#loadingBackdrop{background:rgba(0,0,0,.35);}
#loadingDiv{top:50%;left:50%;width:120px;height:120px;margin:-60px 0 0 -60px;background:var(--bs-body-bg,#fff);border-radius:16px;box-shadow:0 8px 24px rgba(0,0,0,.25);display:flex;align-items:center;justify-content:center;}
.loader{border-radius:50%;width:56px;height:56px;border:6px solid var(--bs-border-color,rgba(0,0,0,.1));border-left-color:var(--bs-primary,#0d6efd);animation:load8 .9s linear infinite;}
@keyframes load8{0%{transform:rotate(0deg);}100%{transform:rotate(360deg);}}

#ai-chat-open{position:fixed;bottom:50px;right:20px;background:#2a6db0;color:#fff;border:none;border-radius:20px;padding:10px 18px;cursor:pointer;z-index:2147483647;}
#ai-chat-widget{position:fixed;bottom:70px;right:20px;width:380px;height:480px;background:#fff;border:1px solid #bbb;border-radius:10px;display:none;flex-direction:column;box-shadow:0 6px 18px rgba(0,0,0,.35);z-index:2147483647;}
#ai-chat-header{background:#2a6db0;color:#fff;padding:10px;font-weight:bold;position:relative;cursor:move;user-select:none;}
#ai-chat-close{float:right;cursor:pointer;}
#ai-chat-messages{height:360px;overflow-y:auto;padding:10px;font-size:13px;}
#ai-chat-input{border-top:1px solid #ddd;padding:8px;display:flex;gap:4px;}
#ai-chat-text{flex:1;}
#ai-chat-send{width:80px;}

.ai-user{text-align:right;margin:6px 0;background:#e0f0ff;color:#000;display:inline-block;padding:6px 12px;border-radius:12px;max-width:75%;word-wrap:break-word;}
.ai-bot pre.ai-text{background:#f0f0f0;color:#000;padding:6px 12px;border-radius:12px;display:inline-block;max-width:75%;white-space:pre-wrap;word-wrap:break-word;font-family:monospace;margin:4px 0;transition:opacity .2s ease-in-out;}
.ai-bot.ai-error pre.ai-text{background:#ffe6e6;color:#b30000;border:1px solid #ff0000;}
.ai-copy{margin-left:8px;font-size:11px;padding:2px 6px;cursor:pointer;}
.ai-bot.ai-cancelled pre.ai-text{color:#666;font-style:italic;opacity:0;transition:opacity .3s ease-in-out;}
.ai-bot.ai-cancelled.show pre.ai-text{opacity:1;}

.typing-indicator{display:inline-block;font-style:italic;color:#666;margin-left:4px;}
.typing-indicator .dot{display:inline-block;width:6px;height:6px;margin:0 2px;background:#666;border-radius:50%;opacity:.3;animation:blink 1.4s infinite;}
.typing-indicator .dot:nth-child(1){animation-delay:0s;}
.typing-indicator .dot:nth-child(2){animation-delay:.2s;}
.typing-indicator .dot:nth-child(3){animation-delay:.4s;}
@keyframes blink{0%,80%,100%{opacity:.3;}40%{opacity:1;}}
[data-bs-theme=dark] .input-group-text{background-color:var(--bs-tertiary-bg)!important;color:var(--bs-secondary-color)!important;border-color:var(--bs-border-color)!important;}
[data-bs-theme=dark] input[type="text"].search,[data-bs-theme=dark] .search{background-color:var(--bs-body-bg)!important;color:var(--bs-body-color)!important;border-color:var(--bs-border-color)!important;}
</style>

<body>
<iframe title="sandboxFrame" id="sandboxFrame" style="display:none;"></iframe>

<tiles:insert name="header">
    <tiles:put name="title"><%=theTitle%></tiles:put>
    <tiles:put name="submenu_width"><tiles:getAsString name="submenu_width"/></tiles:put>
    <tiles:put name="location"><tiles:getAsString name="location"/></tiles:put>
    <tiles:put name="submenu_top"><tiles:getAsString name="submenu_top"/></tiles:put>
</tiles:insert>

<iframe id="downloadFrame" style="display:none;"></iframe>

<div class="modal fade" id="downloadResultModal" tabindex="-1"
     aria-labelledby="downloadResultModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="downloadResultModalLabel">Download Status</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <div class="modal-body">
        <div id="downloadResultContent"></div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
      </div>
    </div>
  </div>
</div>

<logic:present name="<%=ecmwf.web.model.users.User.SESSION_KEY%>">
<button id="ai-chat-open">Ask <%=System.getProperty("monitor.nickName")%></button>
<div id="ai-chat-widget">
    <div id="ai-chat-header">
        <%=System.getProperty("monitor.nickName")%> Assistant
        <span id="ai-chat-close">&times;</span>
    </div>
    <div id="ai-chat-messages"></div>
    <div id="ai-chat-input">
        <input type="text" id="ai-chat-text" placeholder="Ask about <%=System.getProperty("monitor.nickName")%>..." aria-label="Ask <%=System.getProperty("monitor.nickName")%>" />
        <button id="ai-chat-send">Send</button>
    </div>
</div>

<script>
$(document).ready(function() {
    let aiContext = null;
    try {
        const raw = document.getElementById("ai-context")?.dataset?.json;
        aiContext = raw ? JSON.parse(raw) : null;
    } catch(e) { console.error("Invalid AI context JSON:", e); }

    if(!aiContext || Object.keys(aiContext).length === 0) $("#ai-chat-open,#ai-chat-widget").hide();

    const $messages = $("#ai-chat-messages");
    let aiSocket = null, reconnectAttempts = 0, assistantOpen = false;
    let pendingQueue = [], sentQueue = [];
    let promptHistory = [], historyIndex = -1;
    const MAX_HISTORY = 5, STREAM_FLUSH_INTERVAL = 40, autoScrollEnabled = true;

    // Smooth scroll helper
    function smoothScroll(force=false) {
        const msgBox = $messages[0]; 
        if(!msgBox) return;
        if(force || autoScrollEnabled) msgBox.scrollTop = msgBox.scrollHeight;
    }

    // Typing indicator
    function createTypingIndicator() {
        const container = $("<span class='typing-indicator'></span>");
        for(let i=0;i<3;i++) container.append($("<span class='dot'></span>"));
        return container;
    }

    // Flush token buffer incrementally
    function flushTokenBuffer(msgObj, force=false) {
        if(!msgObj || !msgObj.botMessage) return;
        if(!msgObj.tokenBuffer && !force) return;
        if(msgObj.cancel) return;

        const container = msgObj.botMessage.find(".ai-text")[0];
        const text = msgObj.tokenBuffer; msgObj.tokenBuffer = "";
        let i = 0;

        function typeChunk() {
            if(msgObj.cancel) return;
            const chunkSize = 4;
            container.append(text.slice(i, i + chunkSize));
            i += chunkSize;
            smoothScroll(true);
            if(i < text.length) msgObj.bufferTimeout = setTimeout(typeChunk, 1);
            else msgObj.bufferTimeout = null;
        }
        typeChunk();
    }

    // Cancel all in-flight AI messages
	function aiCancel(reason="[Cancelled]") {
	    if(aiSocket && aiSocket.readyState === WebSocket.OPEN) {
	        aiSocket.send(JSON.stringify({ type: "cancel" }));
	    }

	    if(!sentQueue.length && !pendingQueue.length) return;

	    sentQueue.forEach(msgObj => {
	        msgObj.cancel = true;
	        if(msgObj.bufferTimeout){
	            clearTimeout(msgObj.bufferTimeout);
	            msgObj.bufferTimeout = null;
	        }
	        msgObj.tokenBuffer = "";
	        if(msgObj.typingIndicator){
	            msgObj.typingIndicator.stop(true,true).remove();
	        }
	        msgObj.botMessage.find(".ai-cursor").remove();
	        msgObj.botMessage
	            .removeClass("ai-error")
	            .addClass("ai-cancelled");
	        msgObj.botMessage
	            .find(".ai-text")
	            .text(reason);
	        msgObj.botMessage.addClass("show");
	    });

	    sentQueue = [];
	    pendingQueue = [];
	    $("#ai-chat-cancel").hide();
	}

    // Send a new AI message
    function aiSend() {
        const text = $("#ai-chat-text").val().trim();
        if(!text) return;

        promptHistory.unshift(text);
        if(promptHistory.length > MAX_HISTORY) promptHistory.pop();
        historyIndex = -1;
        $("#ai-chat-text").val("");

        $messages.append($("<div class='ai-user'></div>").text(text));

        const botMessage = $("<div class='ai-bot'><pre class='ai-text'></pre><span class='ai-cursor'></span></div>");
        const typingIndicator = createTypingIndicator();
        botMessage.append(typingIndicator);
        $messages.append(botMessage);

        const msgId = Date.now()+"-"+Math.random().toString(36).substring(2,8);

        const msgPayload = {
            question: text,
            context: window.aiContext || null,
            botMessage, typingIndicator,
            tokenBuffer: "", bufferTimeout: null, cancel: false,
            id: msgId
        };

        pendingQueue.push(msgPayload);
        processQueue();
    }

    // Enable/disable send button and handle Enter / history keys
    const $sendBtn = $("#ai-chat-send");
    const $textBox = $("#ai-chat-text");

    $sendBtn.prop("disabled", !$textBox.val().trim());

    $textBox.on("input", function() {
        $sendBtn.prop("disabled", !$(this).val().trim());
    });

    $textBox.keydown((e) => {
        if(e.key === "Enter") {
            if(!$textBox.val().trim()) { e.preventDefault(); return; }
            aiSend(); e.preventDefault();
        } else if(e.key === "ArrowUp") {
            if(historyIndex < promptHistory.length-1){ historyIndex++; $textBox.val(promptHistory[historyIndex]); e.preventDefault(); }
        } else if(e.key === "ArrowDown") {
            if(historyIndex>0){ historyIndex--; $textBox.val(promptHistory[historyIndex]); }
            else { historyIndex=-1; $textBox.val(""); }
            e.preventDefault();
        }
    });

    $sendBtn.click(aiSend);

    // Process pending messages
    function processQueue() {
        if(!aiSocket || aiSocket.readyState !== WebSocket.OPEN) {
            if(!aiSocket) aiConnect();
            return;
        }

        if(pendingQueue.length || sentQueue.length) $("#ai-chat-cancel").show();

        while(pendingQueue.length) {
            const msg = pendingQueue.shift();
            sentQueue.push(msg);
            aiSocket.send(JSON.stringify({
                type: "question",
                question: msg.question,
                context: msg.context,
                id: msg.id
            }));
        }
    }

    // Connect to AI WebSocket
    function aiConnect() {
        const protocol = window.location.protocol === "https:" ? "wss://" : "ws://";
        aiSocket = new WebSocket(protocol + window.location.host + "/ws/ai");

        aiSocket.onopen = () => { reconnectAttempts = 0; processQueue(); };

        aiSocket.onmessage = (event) => {
            let data;
            try { data = JSON.parse(event.data); } 
            catch(e) { console.warn("Invalid AI JSON:", event.data); return; }

            if(!sentQueue.length) return;
            const msgObj = sentQueue.find(m => m.id === data.id) || sentQueue[0];
            if(msgObj.cancel) return;

            switch(data.type) {
                case "token":
                    msgObj.tokenBuffer += data.text || "";
                    if(!msgObj.bufferTimeout) {
                        msgObj.bufferTimeout = setTimeout(() => { flushTokenBuffer(msgObj); msgObj.bufferTimeout = null; }, STREAM_FLUSH_INTERVAL);
                    }
                    break;
                case "done":
                    flushTokenBuffer(msgObj, true);
                    msgObj.typingIndicator?.fadeOut(200);
                    msgObj.botMessage.find(".ai-cursor").remove();
                    if(!msgObj.botMessage.find(".ai-copy").length){
                        $("<button class='ai-copy' aria-label='Copy AI response'>Copy</button>")
                            .click(() => navigator.clipboard.writeText(msgObj.botMessage.find(".ai-text").text()))
                            .appendTo(msgObj.botMessage);
                    }
                    sentQueue = sentQueue.filter(m => m.id !== msgObj.id);
                    if(sentQueue.length === 0) $("#ai-chat-cancel").hide();
                    break;
                case "error":
                    msgObj.botMessage.addClass("ai-error");
                    msgObj.botMessage.find(".ai-text").text(data.message || "Unknown error");
                    msgObj.typingIndicator?.fadeOut(200);
                    sentQueue = sentQueue.filter(m => m.id !== msgObj.id);
                    if(sentQueue.length === 0) $("#ai-chat-cancel").hide();
                    break;
                case "ping":
                    break;
                default:
                    console.warn("Unknown AI message type:", data.type);
            }
        };

        aiSocket.onclose = () => {
            if(sentQueue.length || pendingQueue.length) aiCancel("[Cancelled: connection lost]");
            aiSocket = null;
            if(assistantOpen && reconnectAttempts < 5) {
                reconnectAttempts++;
                setTimeout(aiConnect, 2000);
            }
        };

        aiSocket.onerror = (err) => console.error("AI WebSocket error", err);
    }

    // UI bindings
    $("#ai-chat-open").click(() => { 
        $("#ai-chat-widget").fadeIn(); 
        $("#ai-chat-open").fadeOut(); 
        assistantOpen = true;
        if(!aiSocket) aiConnect();
    });

    $("#ai-chat-close").click(() => {
        $("#ai-chat-widget").fadeOut();
        $("#ai-chat-open").fadeIn();
        assistantOpen = false;
        if(aiSocket && aiSocket.readyState === 1) aiSocket.close(1000, "User closed");
    });

    // Cancel button
    const cancelBtn = $("<button id='ai-chat-cancel'>Cancel</button>")
        .css({position:"absolute",right:"60px",top:"10px",padding:"4px 8px","font-size":"12px",cursor:"pointer",display:"none"})
        .click(() => aiCancel());
    $("#ai-chat-header").append(cancelBtn);

    window.addEventListener('beforeunload', () => { if(aiSocket && aiSocket.readyState===1) aiSocket.close(1000,'User closed'); });
});
</script>
</logic:present>

<div id="loadingBackdrop" style="display:none"></div>
<div id="loadingDiv" style="display:none"><div class="loader"></div></div>

<div class="modal fade" id="testResultModal" tabindex="-1"
     aria-labelledby="testResultModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header py-2">
        <h5 class="modal-title fw-semibold" id="testResultModalLabel"><i class="bi bi-terminal me-2"></i>Test Result</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <div class="modal-body p-0" id="testResultBody">
        <div class="px-3 pt-2 pb-0" style="border-bottom:1px solid var(--bs-border-color);">
          <span class="text-muted" style="font-size:0.7rem;font-weight:600;letter-spacing:0.06em;text-transform:uppercase;">
            <i class="bi bi-terminal me-1"></i>Output
          </span>
        </div>
        <pre id="testResultContent" class="m-0 p-3"
             style="max-height:50vh;overflow:auto;white-space:pre-wrap;word-break:break-word;font-size:0.85rem;border-radius:0;"></pre>
        <div id="testResultFileContent" style="display:none;max-height:50vh;overflow:auto;font-size:0.85rem;"></div>
      </div>
      <div id="testResultUrlArea" style="display:none;background:var(--bs-tertiary-bg);" class="px-3 pt-2 pb-2 border-top">
        <label for="testResultUrlEdit" class="form-label mb-1" style="font-size:0.7rem;font-weight:600;letter-spacing:0.06em;text-transform:uppercase;color:var(--bs-info);">
          <i class="bi bi-pencil-square me-1"></i>URLs to fetch <span style="font-weight:400;text-transform:none;letter-spacing:0;" class="text-muted">(one per line &mdash; edit before fetching)</span>
        </label>
        <textarea id="testResultUrlEdit" class="form-control form-control-sm font-monospace"
                  rows="3" spellcheck="false"
                  style="font-size:0.8rem;resize:vertical;border-color:var(--bs-info-border-subtle);"></textarea>
      </div>
      <div class="modal-footer py-2">
        <div class="d-flex align-items-center gap-2 me-auto">
          <button type="button" class="btn btn-outline-info btn-sm" id="testResultFetchBtn" style="display:none;">
            <i class="bi bi-file-text me-1"></i>Preview File Content
          </button>
        </div>
        <div id="testResultCopyControls" class="d-flex align-items-center gap-1" style="display:none!important;">
          <div class="btn-group btn-group-sm" role="group" aria-label="Copy mode">
            <input type="radio" class="btn-check" name="fcCopyMode" id="fcCopyRaw" value="raw" autocomplete="off" checked>
            <label class="btn btn-outline-secondary" for="fcCopyRaw" style="font-size:0.75rem;">Raw</label>
            <input type="radio" class="btn-check" name="fcCopyMode" id="fcCopyPretty" value="pretty" autocomplete="off">
            <label class="btn btn-outline-secondary" for="fcCopyPretty" style="font-size:0.75rem;">Pretty</label>
          </div>
          <button type="button" class="btn btn-outline-secondary btn-sm" id="testResultCopyBtn"
                  onclick="_fcGlobalCopy(document.querySelector('input[name=fcCopyMode]:checked').value)">
            <i class="bi bi-clipboard me-1"></i>Copy All
          </button>
        </div>
        <button type="button" class="btn btn-outline-secondary btn-sm" id="testResultCopySimpleBtn"
                onclick="(function(){var pre=document.getElementById('testResultContent');var t=pre?pre.dataset.rawText||pre.textContent:'';navigator.clipboard.writeText(t).then(function(){var b=document.getElementById('testResultCopySimpleBtn');var o=b.innerHTML;b.innerHTML='<i class=\'bi bi-check2 me-1\'></i>Copied!';setTimeout(function(){b.innerHTML=o;},1500);});})()">
          <i class="bi bi-clipboard me-1"></i>Copy
        </button>
        <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Close</button>
      </div>
    </div>
  </div>
</div>

<div class="modal fade" id="confirmationModal" tabindex="-1"
     aria-labelledby="confirmationModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="confirmationModalLabel">Please Confirm</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <div class="modal-body">
        <p id="confirmationDialogMessage"></p>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" id="confirmationModalCancelBtn"
                data-bs-dismiss="modal">Cancel</button>
        <button type="button" class="btn btn-primary"
                id="confirmationModalConfirmBtn">Confirm</button>
      </div>
    </div>
  </div>
</div>

<div class="offcanvas offcanvas-start" tabindex="-1" id="sidebarMenu" aria-labelledby="sidebarMenuLabel">
    <div class="offcanvas-header border-bottom py-2 px-3">
        <span class="fw-semibold small" id="sidebarMenuLabel"><i class="bi bi-layout-sidebar me-2"></i>Navigation</span>
        <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
    </div>
    <div class="offcanvas-body p-0 d-flex flex-column">
        <div class="flex-grow-1 overflow-auto">
            <table class="spareBox2">
                <tr><td><a href="/do/start"><i class="bi bi-house-fill"></i> Home</a></td></tr>
            </table>
            <jsp:include page="/WEB-INF/jsp/pds/submenu.jsp" />
            <tiles:get name="submenu" />
            <tiles:get name="spare" />
            <tiles:get name="spare2" ignore="true"/>
            <tiles:get name="spare3" ignore="true"/>
        </div>
        <div class="px-3 py-2" style="border-top:1px solid var(--bs-border-color); flex-shrink:0;">
            <a href="https://github.com/ecmwf/open-ecpds" target="_blank" rel="noopener"
               style="display:flex; align-items:center; gap:6px; font-size:0.78rem; color:var(--bs-secondary-color); text-decoration:none;">
                <i class="bi bi-github"></i> GitHub Repository
                <i class="bi bi-box-arrow-up-right" style="font-size:0.65rem; opacity:0.6;"></i>
            </a>
        </div>
    </div>
</div>
<script>
document.addEventListener('DOMContentLoaded', function() {
    var body = document.querySelector('#sidebarMenu .offcanvas-body');
    var btn = document.querySelector('[data-bs-target="#sidebarMenu"]');
    if (btn && body && !body.querySelector('a, table, button')) btn.style.display = 'none';
});
// When browser restores this page from bfcache (e.g. Cancel -> history.back()),
// close the sidebar offcanvas if it was open at the time of navigation.
window.addEventListener('pageshow', function(event) {
    if (event.persisted) {
        var offcanvasEl = document.getElementById('sidebarMenu');
        if (offcanvasEl && offcanvasEl.classList.contains('show')) {
            var instance = bootstrap.Offcanvas.getInstance(offcanvasEl);
            if (instance) instance.hide();
            else bootstrap.Offcanvas.getOrCreateInstance(offcanvasEl).hide();
        }
    }
});
</script>

<!-- UI Help offcanvas -->
<div class="offcanvas offcanvas-end" tabindex="-1" id="uiHelpOffcanvas" aria-labelledby="uiHelpOffcanvasLabel" style="width:360px;max-width:95vw;">
  <div class="offcanvas-header border-bottom py-2 px-3">
    <span class="fw-semibold small" id="uiHelpOffcanvasLabel"><i class="bi bi-question-circle me-2 text-primary"></i>Interface Guide</span>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>
  <div class="offcanvas-body px-3 py-2" style="font-size:0.83rem; overflow-y:auto;">

    <div class="mb-3">
      <div class="fw-semibold mb-1 d-flex align-items-center gap-2"><i class="bi bi-list text-secondary"></i> Navigation Menu</div>
      <p class="mb-1 text-muted">The <strong>&#9776; hamburger button</strong> (top-left) opens the side navigation panel, providing access to all sections and subsections available on the current page.</p>
    </div>

    <hr class="my-2">

    <div class="mb-3">
      <div class="fw-semibold mb-1 d-flex align-items-center gap-2"><i class="bi bi-moon-fill text-secondary"></i> Light / Dark Theme</div>
      <p class="mb-1 text-muted">The <strong>moon / sun button</strong> (top-right) toggles between light and dark display mode. Your preference is remembered across sessions.</p>
    </div>

    <hr class="my-2">

    <div class="mb-3">
      <div class="fw-semibold mb-1 d-flex align-items-center gap-2"><i class="bi bi-info-circle text-primary"></i> Tooltips &amp; Info Icons</div>
      <p class="mb-1 text-muted"><strong>Hover</strong> over any <i class="bi bi-question-circle text-muted"></i> or <i class="bi bi-info-circle text-muted"></i> icon next to a field to read a description. Some icons open a full reference panel when clicked.</p>
    </div>

    <hr class="my-2">

    <div class="mb-3">
      <div class="fw-semibold mb-1 d-flex align-items-center gap-2"><i class="bi bi-list-ol text-secondary"></i> Page Size Selector</div>
      <p class="mb-1 text-muted">The <i class="bi bi-list-ol text-muted"></i> dropdown in table headers controls how many rows are shown per page. Your choice is remembered separately for each table.</p>
    </div>

    <hr class="my-2">

    <div class="mb-3">
      <div class="fw-semibold mb-1 d-flex align-items-center gap-2"><i class="bi bi-arrow-down-up text-secondary"></i> Column Sorting</div>
      <p class="mb-1 text-muted">Click any <strong>column header</strong> to sort. Columns cycle through three states:</p>
      <ul class="mb-1 ps-3 text-muted">
        <li><i class="bi bi-arrow-down-up"></i> <strong>Unsorted</strong> &mdash; natural order</li>
        <li><i class="bi bi-sort-up"></i> <strong>Ascending</strong> &mdash; A &rarr; Z / oldest first</li>
        <li><i class="bi bi-sort-down"></i> <strong>Descending</strong> &mdash; Z &rarr; A / newest first</li>
      </ul>
    </div>

    <hr class="my-2">

    <div class="mb-3">
      <div class="fw-semibold mb-1 d-flex align-items-center gap-2"><i class="bi bi-search text-secondary"></i> Search &amp; Filter Boxes</div>
      <p class="mb-1 text-muted">The <i class="bi bi-search text-muted"></i> search box above a table filters rows instantly as you type. It matches any column. Use it together with the page size selector for faster navigation on large tables.</p>
    </div>

    <hr class="my-2">

    <div class="mb-3">
      <div class="fw-semibold mb-1 d-flex align-items-center gap-2"><i class="bi bi-funnel text-secondary"></i> Query Builder</div>
      <p class="mb-1 text-muted">Some pages (e.g. Transfer History, Data Files) include an advanced <strong>query builder</strong>. Click the <i class="bi bi-sliders2"></i> <strong>Filter</strong> button next to the search box to open the condition panel. Fill in the available condition rows (field, operator, value), then click <strong>Apply &amp; Search</strong> to run the query. You can also type filter expressions directly in the search box.</p>
    </div>

    <hr class="my-2">

    <div class="mb-3">
      <div class="fw-semibold mb-1 d-flex align-items-center gap-2"><i class="bi bi-layout-three-columns text-secondary"></i> Column Visibility</div>
      <p class="mb-1 text-muted">Tables with many columns have a <strong>Columns</strong> dropdown button in the header. Choose a preset (<em>Auto</em>, <em>All</em>, <em>Compact</em>) or <em>Custom</em> to pick individual columns. <em>Auto</em> mode hides less important columns on small screens automatically.</p>
    </div>

    <hr class="my-2">

    <div class="mb-3">
      <div class="fw-semibold mb-1 d-flex align-items-center gap-2"><i class="bi bi-code-square text-secondary"></i> Properties &amp; Script Editors</div>
      <p class="mb-1 text-muted">The code editors for <strong>Properties</strong> and <strong>JavaScript</strong> support syntax highlighting and auto-completion. Press <kbd>Ctrl+Space</kbd> for suggestions. Use the <strong>Format</strong> button to pretty-print the content and <strong>Clear</strong> to empty it. The <i class="bi bi-question-circle text-muted"></i> icon in the accordion header opens the full properties reference &mdash; it automatically highlights the option matching the word at the current cursor position.</p>
    </div>

    <hr class="my-2">

    <div class="mb-3">
      <div class="fw-semibold mb-1 d-flex align-items-center gap-2"><i class="bi bi-plus-circle text-secondary"></i> Association Panels</div>
      <p class="mb-1 text-muted">On edit pages, panels like <em>Dissemination Hosts</em> or <em>Data Policies</em> show current associations as chips. Click <strong>+ Add</strong> to expand a searchable chooser and add new entries. Click the <i class="bi bi-x-lg text-muted"></i> on a chip to remove it.</p>
    </div>

    <hr class="my-2">

    <div class="mb-3">
      <div class="fw-semibold mb-1 d-flex align-items-center gap-2"><i class="bi bi-graph-up text-secondary"></i> Charts &amp; Tables</div>
      <p class="mb-1 text-muted">Pages with both a <strong>chart</strong> and a <strong>table</strong> view have a toggle button to switch between them. The search box and row count selector apply to the table view only.</p>
      <p class="mb-1 text-muted">On timeline pages, a <i class="bi bi-sort-up"></i>/<i class="bi bi-sort-down"></i> <strong>Order</strong> button lets you toggle between showing the <em>earliest</em> or <em>latest</em> entries first. Your preference is saved per page.</p>
    </div>

  </div>
</div>

<div id="contentDiv">
    <div id="outerTable">
        <div class="content">
            <tiles:insert name="content">
                <tiles:put name="subcontent"><tiles:getAsString name="subcontent" ignore="true"/></tiles:put>
                <tiles:put name="date.select"><tiles:getAsString name="date.select" ignore="true"/></tiles:put>
                <tiles:put name="destination.select"><tiles:getAsString name="destination.select" ignore="true"/></tiles:put>
                <tiles:put name="metadata.select"><tiles:getAsString name="metadata.select" ignore="true"/></tiles:put>
            </tiles:insert>
        </div>
    </div>
</div>

<tiles:get name="html.bottom" />

<script>
if (typeof bootstrap !== 'undefined' && typeof bootstrap.Tooltip === 'function') {
        document.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(function(el) { bootstrap.Tooltip.getOrCreateInstance(el); });
    } else {
        // Bootstrap tooltip not available; skip
    }
$(window).on('load',function(){$("#loadingBackdrop,#loadingDiv").fadeOut(150);$("#contentDiv").fadeIn("fast");});
function confirmationDialog(arg1, arg2, arg3) {
    var opts = {};
    if (typeof arg1 === "object") {
        opts = arg1 || {};
    } else {
        opts.title     = arg1;
        opts.message   = arg2;
        opts.onConfirm = arg3;
    }
    var title       = opts.title       || "Please Confirm";
    var message     = opts.message     || "";
    var onConfirm   = typeof opts.onConfirm === "function" ? opts.onConfirm : function(){};
    var onCancel    = typeof opts.onCancel  === "function" ? opts.onCancel  : function(){};
    var confirmText = opts.confirmText || "Confirm";
    var cancelText  = opts.cancelText  || "Cancel";
    var showLoading = (opts.showLoading !== false);
    var allowHtml   = (opts.allowHtml  !== false);

    $("#confirmationModalLabel").text(title);
    if (allowHtml) {
        $("#confirmationDialogMessage").html(message);
    } else {
        $("#confirmationDialogMessage").text(message);
    }
    $("#confirmationModalConfirmBtn").text(confirmText);
    $("#confirmationModalCancelBtn").text(cancelText);

    var $modal = $("#confirmationModal");
    var bsModal = bootstrap.Modal.getOrCreateInstance($modal[0]);

    $modal.off("click.confirmDlg").on("click.confirmDlg", "#confirmationModalConfirmBtn", function () {
        var $btn = $(this);
        $btn.prop("disabled", true);
        bsModal.hide();
        if (showLoading) {
            $("#loadingBackdrop").show();
            $("#loadingDiv").show();
        }
        setTimeout(function () {
            try { onConfirm(); } finally { $btn.prop("disabled", false); }
        }, 50);
    });

    $modal.off("click.cancelDlg").on("click.cancelDlg", "#confirmationModalCancelBtn, [data-bs-dismiss='modal']", function () {
        $("#loadingBackdrop").hide();
        $("#loadingDiv").hide();
        onCancel();
    });

    bsModal.show();
}
$(function() {
  $('a[href*="/download/"]').on('click', function(e) {
    e.preventDefault();
    let url = $(this).attr('href');
    url += (url.includes('?') ? '&' : '?') + '_ts=' + Date.now();
	// url += "&debug=1"; // Uncomment for debug probe mode (no streaming, just metadata)
	$("#downloadFrame").off("load").on("load", function () {
	    const iframe = document.getElementById("downloadFrame");
	    const doc = iframe.contentDocument || iframe.contentWindow.document;
	    let text = "";
	    if (doc && doc.body && doc.body.innerText) {
	        text = doc.body.innerText.trim();
	    }
	    if (text.startsWith("<!DOCTYPE") || text.startsWith("<html")) {
	        text = "##DOWNLOAD_ERROR## Server returned an HTML page instead of the file.";
	    }
	    const isError = text.includes("##DOWNLOAD_ERROR##");
	    const cleanMsg = text.replace("##DOWNLOAD_ERROR##", "").trim();
	    $("#downloadResultModalLabel").text(isError ? "Download Error" : "Download Started");
	    if (isError) {
	        $("#downloadResultContent").html("<span class='text-danger'>" + cleanMsg + "</span>");
	    } else {
	        $("#downloadResultContent").html("<span class='text-success'>&#10004; Download started successfully.</span>");
	    }
	    bootstrap.Modal.getOrCreateInstance(document.getElementById("downloadResultModal")).show();
	});
    $("#downloadFrame").attr("src", url);
  });
});
</script>
