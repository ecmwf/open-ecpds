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
#loadingDiv{top:50%;left:50%;width:160px;height:160px;margin:-80px 0 0 -80px;background:#fff;border-radius:12px;box-shadow:0 8px 20px rgba(0,0,0,.35);display:flex;align-items:center;justify-content:center;}
.loader{border-radius:50%;width:80px;height:80px;border:10px solid rgba(0,0,0,.15);border-left-color:#333;animation:load8 1.1s linear infinite;}
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

<div id="downloadResultModal" style="display:none;">
  <h3>Download Status</h3>
  <div id="downloadResultContent"></div>
</div>

<logic:present name="<%=ecmwf.web.model.users.User.SESSION_KEY%>">
<button id="ai-chat-open">Ask OpenECPDS</button>
<div id="ai-chat-widget">
    <div id="ai-chat-header">
        OpenECPDS Assistant
        <span id="ai-chat-close">&times;</span>
    </div>
    <div id="ai-chat-messages"></div>
    <div id="ai-chat-input">
        <input type="text" id="ai-chat-text" placeholder="Ask about OpenECPDS..." aria-label="Ask OpenECPDS" />
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

<div id="loadingBackdrop"></div>
<div id="loadingDiv"><div class="loader"></div></div>
<div id="confirmationDialog"><p id="confirmationDialogMessage" style="margin-top:10px;"></p></div>

<div id="contentDiv">
    <table id="outerTable" width="<tiles:getAsString name="page_width"/>" border="0" cellspacing="0" cellpadding="0" bgcolor="#ffffff">
        <tr><td colspan=2 height=100>&nbsp;</td></tr>
        <tr>
            <td valign="top" width="<tiles:getAsString name="submenu_width"/>">
                <tiles:get name="submenu" />
                <tiles:get name="spare" />
                <tiles:get name="spare2" ignore="true"/>
                <tiles:get name="spare3" ignore="true"/>
            </td>
            <td valign="top" class="content">
                <tiles:insert name="content">
                    <tiles:put name="subcontent"><tiles:getAsString name="subcontent" ignore="true"/></tiles:put>
                    <tiles:put name="date.select"><tiles:getAsString name="date.select" ignore="true"/></tiles:put>
                    <tiles:put name="destination.select"><tiles:getAsString name="destination.select" ignore="true"/></tiles:put>
                    <tiles:put name="metadata.select"><tiles:getAsString name="metadata.select" ignore="true"/></tiles:put>
                </tiles:insert>
            </td>
        </tr>
        <tr><td colspan=2 height=40>&nbsp;</td></tr>
    </table>
</div>

<tiles:get name="html.bottom" />

<script>
$(document).tooltip();
$(window).on('load',function(){$("#loadingBackdrop,#loadingDiv").fadeOut(150);$("#contentDiv").fadeIn("fast");});
function confirmationDialog(arg1, arg2, arg3) {
    // Normalize args to options object
    var opts = {};
    if (typeof arg1 === "object") {
        opts = arg1 || {};
    } else {
        opts.title     = arg1;
        opts.message   = arg2;
        opts.onConfirm = arg3;
    }
    var title        = opts.title || "Please Confirm";
    var message      = opts.message || "";
    var onConfirm    = typeof opts.onConfirm === "function" ? opts.onConfirm : function(){};
    var onCancel     = typeof opts.onCancel === "function"  ? opts.onCancel  : function(){};
    var width        = opts.width || 500;
    var confirmText  = opts.confirmText || "Confirm";
    var cancelText   = opts.cancelText  || "Cancel";
    var showLoading  = (opts.showLoading !== false); // default true
    var allowHtml    = (opts.allowHtml !== false);   // default true
    // Inject message
    if (allowHtml) {
        $("#confirmationDialogMessage").html(message);
    } else {
        $("#confirmationDialogMessage").text(message);
    }
    // Open dialog
    $("#confirmationDialog").dialog({
        modal: true,
        width: width,
        title: title,
        // Ensure ESC can't accidentally close during confirm flow (optional)
        closeOnEscape: true,
        buttons: [
            {
                text: confirmText,
                click: function () {
                    var dlg = $(this);
                    // Prevent double clicks
                    var $buttons = dlg.parent().find(".ui-dialog-buttonpane button");
                    $buttons.prop("disabled", true);

                    // Show global loading overlay if desired
                    if (showLoading) {
                        $("#loadingBackdrop").show();
                        $("#loadingDiv").show();
                    }
                    // Close the dialog to clear the screen
                    dlg.dialog("close");
                    // Allow repaint so overlay is visible before heavy work/navigation
                    setTimeout(function () {
                        try {
                            onConfirm();
                        } finally {
                            // Typically you do NOT hide overlay here because a navigation
                            // to a new page will occur; the new page can hide/remove it
                            // on $(window).on('load', ...) as you already do.
                            //
                            // If your onConfirm does not navigate and you want to
                            // hide the overlay after async work, do it there.
                        }
                    }, 50);
                },
                "class": "confirm-button" // optional to style as danger
            },
            {
                text: cancelText,
                click: function () {
                    // Ensure overlay is not shown if user cancels
                    $("#loadingBackdrop").hide();
                    $("#loadingDiv").hide();
                    $(this).dialog("close");
                    onCancel();
                }
            }
        ],
        // Optional: ensure focus is managed
        open: function () {
            // Focus the confirm button by default (or cancel per your preference)
            var $dlg = $(this).parent();
            $dlg.find(".ui-dialog-buttonpane button:eq(0)").trigger("focus");
        }
    });
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
	    // Detect any unexpected HTML response
	    if (text.startsWith("<!DOCTYPE") || text.startsWith("<html")) {
	        text = "##DOWNLOAD_ERROR## Server returned an HTML page instead of the file.";
	    }
	    const isError = text.includes("##DOWNLOAD_ERROR##");
	    if (isError) {
	        // Strip the token so it does not appear in the popup
	        const cleanMsg = text.replace("##DOWNLOAD_ERROR##", "").trim();
	        $("#downloadResultModal").dialog({
	            modal: true,
	            width: 480,
	            title: "Download Error",
	            buttons: [
	                {
	                    text: "Close",
	                    click: function () { $(this).dialog("close"); }
	                }
	            ],
	            open: function () {
	                $("#downloadResultContent")
	                    .html("<span style='color:#b30000'>" + cleanMsg + "</span>");
	            }
	        });
	    } else {
	        $("#downloadResultModal").dialog({
	            modal: true,
	            width: 480,
	            title: "Download Started",
	            buttons: [
	                {
	                    text: "OK",
	                    click: function () { $(this).dialog("close"); }
	                }
	            ],
	            open: function () {
	                $("#downloadResultContent")
	                    .html("<span style='color:#007700'>&#10004; Download started successfully.</span>");
	            }
	        });
	    }
	});
    $("#downloadFrame").attr("src", url);
  });
});
</script>
