<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<c:set var="messages" value="${destinationDetailActionForm.messages}" />
<c:set var="message" value="${destinationDetailActionForm.message}" />

<c:if test="${not empty messages}">
<div class="alert alert-warning mt-2 mb-2 p-2" role="alert">
    <div class="d-flex align-items-center gap-2 mb-1">
        <i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i>
        <strong class="small">Error(s)</strong>
    </div>
    <div style="max-height:10rem; overflow-y:auto; border-top:1px solid var(--bs-warning-border-subtle); padding-top:0.35rem; margin-top:0.1rem;">
        <ul id="errMsgList" class="mb-0 ps-3" style="font-size:0.8rem; line-height:1.6;">
            <c:forEach var="errormessage" items="${messages}">
                <li data-raw="${errormessage}">${errormessage}</li>
            </c:forEach>
        </ul>
    </div>
</div>
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

    ul.innerHTML = '';
    order.forEach(function (key) {
        var g = groups[key];
        var li = document.createElement('li');
        if (g.ids.length === 1 || g.ids[0] === null) {
            li.textContent = g.template;
        } else {
            /* Split on the first quoted token and inject a count badge */
            var parts = g.template.split(/'[^']*'/);
            li.appendChild(document.createTextNode(parts[0]));
            var badge = document.createElement('span');
            badge.className = 'badge rounded-pill bg-warning text-dark fw-normal';
            badge.style.cursor = 'help';
            badge.title = g.ids.join(', ');
            badge.textContent = g.ids.length + ' items';
            li.appendChild(badge);
            if (parts.length > 1) li.appendChild(document.createTextNode(parts.slice(1).join('')));
        }
        ul.appendChild(li);
    });
})();
</script>
</c:if>

<c:if test="${not empty message}">
<div class="alert alert-info d-flex align-items-center gap-2 mt-2 mb-2" role="alert">
    <i class="bi bi-info-circle-fill flex-shrink-0"></i>
    <div>${message}</div>
</div>
</c:if>
