<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<c:set var="messages" value="${destinationDetailActionForm.messages}" />
<c:set var="message" value="${destinationDetailActionForm.message}" />

<c:choose>
  <%-- Both errors and a success summary: show a single partial-success card --%>
  <c:when test="${not empty messages and not empty message}">
<div class="alert alert-warning mt-2 mb-2 p-2" role="alert">
    <div class="d-flex align-items-center gap-2 mb-1">
        <i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i>
        <strong class="small">Partial success &mdash; ${message}</strong>
    </div>
    <div style="max-height:10rem; overflow-y:auto; border-top:1px solid var(--bs-warning-border-subtle); padding-top:0.35rem; margin-top:0.1rem;">
        <ul id="errMsgList" class="mb-0 ps-1" style="font-size:0.8rem; line-height:1.6; list-style:none;">
            <c:forEach var="errormessage" items="${messages}">
                <li data-raw="${errormessage}">${errormessage}</li>
            </c:forEach>
        </ul>
    </div>
</div>
  </c:when>
  <%-- Errors only --%>
  <c:when test="${not empty messages}">
<div class="alert alert-warning mt-2 mb-2 p-2" role="alert">
    <div class="d-flex align-items-center gap-2 mb-1">
        <i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i>
        <strong class="small">Error(s)</strong>
    </div>
    <div style="max-height:10rem; overflow-y:auto; border-top:1px solid var(--bs-warning-border-subtle); padding-top:0.35rem; margin-top:0.1rem;">
        <ul id="errMsgList" class="mb-0 ps-1" style="font-size:0.8rem; line-height:1.6; list-style:none;">
            <c:forEach var="errormessage" items="${messages}">
                <li data-raw="${errormessage}">${errormessage}</li>
            </c:forEach>
        </ul>
    </div>
</div>
  </c:when>
  <%-- Success only --%>
  <c:when test="${not empty message}">
<div class="alert alert-success d-flex align-items-center gap-2 mt-2 mb-2 p-2" role="alert">
    <i class="bi bi-check-circle-fill flex-shrink-0"></i>
    <div>${message}</div>
</div>
  </c:when>
</c:choose>

<c:if test="${not empty messages}">
<script>
(function () {
    var ul = document.getElementById('errMsgList');
    if (!ul) return;
    var msgs = [];
    ul.querySelectorAll('li').forEach(function (li) {
        msgs.push(li.getAttribute('data-raw') || li.textContent.trim());
    });

    /* Group by template: replace first single-quoted value with a placeholder */
    var groups = {}, order = [];
    msgs.forEach(function (msg) {
        var key = msg.replace(/'[^']*'/, "'§'");
        if (!groups[key]) { groups[key] = { template: msg, ids: [] }; order.push(key); }
        var m = msg.match(/'([^']*)'/);
        groups[key].ids.push(m ? m[1] : null);
    });

    var ICON_HTML = '<i class="bi bi-x-circle-fill text-danger flex-shrink-0"></i>';

    /* Returns parts joined — the <li> itself is the flex container (icon + text-span only). */
    function flexLine(parts) {
        return parts.join('');
    }

    /* Custom pill: font-size inherits from parent so there is no size mismatch → natural alignment.
       Embedded inside the text span so it cannot wrap to a second flex line. */
    function pill(text) {
        return '<span style="border:1px solid rgba(108,117,125,0.5);border-radius:0.4em;'
             + 'padding:0.05em 0.35em;background:rgba(108,117,125,0.1);white-space:nowrap">'
             + text + '</span>';
    }

    /* Convert a raw status code (e.g. "DONE", "STOP") to a coloured badge span.
       vertical-align:0.1em corrects for the badge font-size (0.75em) being smaller than parent. */
    function statusBadge(code) {
        var nameMap = {
            INIT: 'Arriving', SCHE: 'Preset', FETC: 'Fetching', HOLD: 'StandBy',
            WAIT: 'Queued',   EXEC: 'Transferring', DONE: 'Done', RETR: 'ReQueued',
            STOP: 'Stopped',  FAIL: 'Failed',        INTR: 'Interrupted'
        };
        var clsMap = {
            DONE: 'bg-success',
            EXEC: 'bg-primary', FETC: 'bg-primary', INIT: 'bg-primary',
            WAIT: 'bg-warning text-dark', SCHE: 'bg-warning text-dark',
            HOLD: 'bg-warning text-dark', RETR: 'bg-warning text-dark',
            FAIL: 'bg-danger'
        };
        var name = nameMap[code] || code;
        var cls  = clsMap[code]  || 'bg-secondary';
        return '<span class="badge ' + cls + '" title="' + code + '" style="vertical-align:0.1em">' + name + '</span>';
    }

    /* Escapes a plain string for safe insertion via innerHTML. */
    function escHtml(s) {
        return String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
                        .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
    }

    function friendlyText(key, ids) {
        var count = ids.length;
        var n = '<strong>' + count + '</strong>\u00a0' + (count === 1 ? 'transfer' : 'transfers');
        var m;
        /* pill/badge embedded inside the text span — cannot wrap to a second flex line */
        if (/^REQUEUE operation unavailable for transfer '§'\. Source file not available/i.test(key))
            return { html: flexLine([ICON_HTML, '<span>Unable to requeue ' + n + ': ' + pill('source file not available') + '</span>']) };
        if ((m = key.match(/^REQUEUE operation unavailable for transfer '§'\. Current Status\s+(.+)/i)))
            return { html: flexLine([ICON_HTML, '<span>Unable to requeue ' + n + ': status is ' + statusBadge(m[1].trim()) + '</span>']) };
        if ((m = key.match(/^STOP operation unavailable for transfer '§'\. Current Status\s+(.+)/i)))
            return { html: flexLine([ICON_HTML, '<span>Unable to stop ' + n + ': status is ' + statusBadge(m[1].trim()) + '</span>']) };
        if ((m = key.match(/^INTERRUPT operation unavailable for transfer '§'\. Current Status\s+(.+)/i)))
            return { html: flexLine([ICON_HTML, '<span>Unable to interrupt ' + n + ': status is ' + statusBadge(m[1].trim()) + '</span>']) };
        if (/^Error REQUEUING transfer '§'/i.test(key))
            return { html: flexLine([ICON_HTML, '<span>Error requeuing ' + n + '</span>']) };
        if (/^Error setting to STOP transfer '§'/i.test(key))
            return { html: flexLine([ICON_HTML, '<span>Error stopping ' + n + '</span>']) };
        if (/^Error interrupting retrieval for Transfer '§'/i.test(key))
            return { html: flexLine([ICON_HTML, '<span>Error interrupting retrieval for ' + n + '</span>']) };
        if ((m = key.match(/^Impossible to change priority of transfer '§' to '([^']+)'/i)))
            return { html: flexLine([ICON_HTML, '<span>Unable to set priority to ' + pill(m[1]) + ' for ' + n + '</span>']) };
        if (/^Impossible to change expiry date of transfer '§'/i.test(key))
            return { html: flexLine([ICON_HTML, '<span>Unable to change expiry date for ' + n + '</span>']) };
        if (/^No host assigned yet for transfer/i.test(key))
            return { html: flexLine([ICON_HTML, '<span>No host assigned for ' + n + '</span>']) };
        return null;
    }

    ul.innerHTML = '';
    order.forEach(function (key) {
        var g = groups[key];
        var li = document.createElement('li');
        li.style.display = 'flex';
        li.style.alignItems = 'center';
        li.style.gap = '0.3rem';
        li.style.marginBottom = '0.15rem';
        var friendly = friendlyText(key, g.ids);
        if (friendly) {
            li.innerHTML = friendly.html;
        } else if (g.ids.length === 1 || g.ids[0] === null) {
            li.innerHTML = flexLine([ICON_HTML, '<span>' + escHtml(g.template) + '</span>']);
        } else {
            /* Unrecognised pattern: replace quoted ID with a count pill (inline in text span) */
            var parts = g.template.split(/'[^']*'/);
            var text = escHtml(parts[0])
                     + '<span style="border:1px solid rgba(255,193,7,0.6);border-radius:0.4em;'
                     + 'padding:0.05em 0.35em;background:rgba(255,193,7,0.15);white-space:nowrap">'
                     + g.ids.length + ' items</span>'
                     + (parts.length > 1 ? escHtml(parts.slice(1).join('')) : '');
            li.innerHTML = flexLine([ICON_HTML, '<span>' + text + '</span>']);
        }
        ul.appendChild(li);
    });
})();
</script>
</c:if>
