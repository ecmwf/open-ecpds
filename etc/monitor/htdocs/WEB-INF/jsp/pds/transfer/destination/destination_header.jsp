<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<c:set var="_ds" value="${destination.formattedStatus}"/>
<c:set var="_dsb" value="${fn:contains(_ds, '-') ? fn:substringBefore(_ds, '-') : _ds}"/>
<c:if test="${not empty destinationDetailActionForm}">
    <c:set var="_destDate" value="${destinationDetailActionForm.date}" />
</c:if>
<c:if test="${empty destinationDetailActionForm}">
    <c:set var="_destDate" value="${param['date']}" />
</c:if>
<div class="dest-page-header mb-3">
    <div class="d-flex align-items-center gap-2 flex-wrap mb-1">
        <c:if test="${not destination.active}"><i class="bi bi-slash-circle-fill text-danger" title="Destination is disabled" style="font-size:0.9rem;align-self:center;"></i></c:if>
        <a class="dest-page-name text-decoration-none" href="/do/transfer/destination/${destination.name}"<c:if test="${not destination.active}"> style="text-decoration:line-through !important;color:var(--bs-secondary-color)"</c:if>>${destination.name}</a>
        <div class="d-flex gap-2 align-items-center flex-wrap dest-badge-group">
        <jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_flag.jsp"/>
        <jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_type_badge.jsp"/>
        <c:choose>
            <c:when test="${_dsb == 'Idle'}">
                <a href="/do/transfer/destination?destinationStatus=${_dsb}&amp;destinationSearch=" class="dest-type-link" title="Show all ${_dsb} destinations"><span class="badge bg-secondary fs-status">${_ds}</span></a>
            </c:when>
            <c:when test="${_dsb == 'Running'}">
                <a href="/do/transfer/destination?destinationStatus=${_dsb}&amp;destinationSearch=" class="dest-type-link" title="Show all ${_dsb} destinations"><span class="badge bg-success fs-status">${_ds}</span></a>
            </c:when>
            <c:when test="${_dsb == 'Restarting' or _dsb == 'Resending'}">
                <a href="/do/transfer/destination?destinationStatus=${_dsb}&amp;destinationSearch=" class="dest-type-link" title="Show all ${_dsb} destinations"><span class="badge bg-info text-dark fs-status">${_ds}</span></a>
            </c:when>
            <c:when test="${_dsb == 'Waiting' or _dsb == 'Retrying' or _dsb == 'Interrupted'}">
                <a href="/do/transfer/destination?destinationStatus=${_dsb}&amp;destinationSearch=" class="dest-type-link" title="Show all ${_dsb} destinations"><span class="badge bg-warning text-dark fs-status">${_ds}</span></a>
            </c:when>
            <c:when test="${_dsb == 'Initialized' or _dsb == 'Stopped' or _dsb == 'NoHosts' or _dsb == 'Failed'}">
                <a href="/do/transfer/destination?destinationStatus=${_dsb}&amp;destinationSearch=" class="dest-type-link" title="Show all ${_dsb} destinations"><span class="badge bg-danger fs-status">${_ds}</span></a>
            </c:when>
            <c:otherwise>
                <a href="/do/transfer/destination?destinationStatus=${_dsb}&amp;destinationSearch=" class="dest-type-link" title="Show all ${_dsb} destinations"><span class="badge bg-secondary fs-status">${_ds}</span></a>
            </c:otherwise>
        </c:choose>
        <c:if test="${not destination.showInMonitors}">
            <i class="bi bi-eye-slash text-muted" title="Not shown in Monitor Display" style="font-size:0.85rem;"></i>
        </c:if>
        <c:if test="${not empty destination.filterName and destination.filterName ne 'none'}">
            <jsp:include page="/WEB-INF/jsp/pds/transfer/compression_icon.jsp"><jsp:param name="name" value="${destination.filterName}"/></jsp:include>
        </c:if>
        <c:if test="${not empty destination.proxyHostsAndPriorities}">
            <i class="bi bi-hdd-network text-secondary" title="Uses a Proxy Host" style="font-size:0.9rem;"></i>
        </c:if>
        </div>
        <%-- Desktop: full icon bar, hidden on mobile --%>
        <div id="_destIconBar" class="d-none d-sm-flex gap-2 align-items-center ms-auto">
        <auth:if basePathKey="destination.basepath" paths="">
        <auth:then>
        <a href='<bean:message key="destination.basepath"/>' class="btn btn-sm btn-outline-secondary" title="All Destinations"><i class="bi bi-arrow-left"></i></a>
        <c:if test="${not empty destination.id}">
        <a href='<bean:message key="destination.basepath"/>/${destination.id}'
           class="btn btn-sm btn-outline-secondary" title="Destination Main Page"><i class="bi bi-house"></i></a>
        </c:if>
        <div style="border-left:1px solid var(--bs-border-color);height:1.5rem;"></div>
        </auth:then>
        </auth:if>
        <c:set var="_destHasEditGroup" value="false"/>
        <auth:if basePathKey="destination.basepath" paths="/edit/insert_form">
        <auth:then>
        <c:set var="_destHasEditGroup" value="true"/>
        <div class="d-flex gap-1 align-items-center">
            <a href='<bean:message key="destination.basepath"/>/edit/insert_form'
               class="btn btn-sm btn-outline-success" title="Create new destination"><i class="bi bi-plus-circle"></i></a>
            <c:if test="${not empty destination.id}">
            <a href='<bean:message key="destination.basepath"/>/edit/update_form/${destination.id}'
               class="btn btn-sm btn-outline-primary" title="Edit this destination"><i class="bi bi-pencil"></i></a>
            <a href='<bean:message key="destination.basepath"/>/edit/delete_form/${destination.id}'
               class="btn btn-sm btn-outline-danger" title="Delete this destination"><i class="bi bi-trash"></i></a>
            <a href='<bean:message key="destination.basepath"/>/edit/insert_form?fromDestination=${destination.name}'
               class="btn btn-sm btn-outline-warning" title="Duplicate this destination"><i class="bi bi-copy"></i></a>
            </c:if>
        </div>
        </auth:then>
        </auth:if>
        <c:if test="${not empty destination.id}">
        <div class="d-flex gap-1 align-items-center"<c:if test="${_destHasEditGroup}"> style="border-left:1px solid var(--bs-border-color);padding-left:0.5rem;"</c:if>>
            <auth:if basePathKey="transferhistory.basepath" paths="/">
            <auth:then>
            <a href='<bean:message key="destination.basepath"/>/${destination.id}?mode=parameters'
               class="btn btn-sm btn-outline-secondary" title="Parameters"><i class="bi bi-sliders"></i></a>
            </auth:then>
            </auth:if>
            <a href='<bean:message key="destination.basepath"/>/${destination.id}?mode=datausers'
               id="_destDataUsersBtn"
               class="btn btn-sm btn-outline-secondary position-relative" title="Data Users"><i class="bi bi-people"></i></a>
            <a href='<bean:message key="destination.basepath"/>/${destination.id}?mode=traffic'
               class="btn btn-sm btn-outline-secondary" title="Data Rates"><i class="bi bi-graph-up"></i></a>
            <a href='<bean:message key="destination.basepath"/>/${destination.id}?mode=changelog'
               class="btn btn-sm btn-outline-secondary" title="Changes Log"><i class="bi bi-clock-history"></i></a>
            <a href='<bean:message key="monitoring.timeline.basepath"/>/${destination.id}<c:if test="${not empty _destDate}">?date=${_destDate}</c:if>'
               class="btn btn-sm btn-outline-secondary" title="Transfer Timeline"><i class="bi bi-calendar3"></i></a>
            <a href='/do/monitoring/unsuccessful/${destination.id}'
               class="btn btn-sm btn-outline-secondary" title="Outstanding"><i class="bi bi-hourglass-split"></i></a>
            <auth:if basePathKey="transferhistory.basepath" paths="/">
            <auth:then>
            <a href='<bean:message key="transferhistory.basepath"/>?destinationName=${destination.id}<c:if test="${not empty _destDate}">&amp;date=${_destDate}</c:if>&amp;fromDestination=true'
               class="btn btn-sm btn-outline-secondary" title="Transfer History"><i class="bi bi-archive"></i></a>
            </auth:then>
            </auth:if>
            <auth:if basePathKey="destination.basepath" paths="/metadata/${destination.id}">
            <auth:then>
            <a href='<bean:message key="destination.basepath"/>/metadata/${destination.id}'
               class="btn btn-sm btn-outline-secondary" title="Metadata"><i class="bi bi-paperclip"></i></a>
            </auth:then>
            </auth:if>
        </div>
        </c:if>
        </div><%-- end #_destIconBar --%>

        <%-- Mobile: ⋯ dropdown, hidden on sm+ --%>
        <div class="d-sm-none ms-auto dest-mobile-menu">
            <div class="dropdown">
                <button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button"
                        id="_destActionsToggle" data-bs-toggle="dropdown" aria-expanded="false"
                        title="Actions">
                    <i class="bi bi-three-dots"></i>
                </button>
                <ul class="dropdown-menu dropdown-menu-end" id="_destActionsMenu" aria-labelledby="_destActionsToggle"></ul>
            </div>
        </div>
        <script>
        (function() {
            document.addEventListener('DOMContentLoaded', function() {
                var bar  = document.getElementById('_destIconBar');
                var menu = document.getElementById('_destActionsMenu');
                if (!bar || !menu) return;
                function addItem(a) {
                    var li   = document.createElement('li');
                    var item = document.createElement('a');
                    item.className = 'dropdown-item';
                    item.href = a.getAttribute('href');
                    var ic = a.querySelector('i[class]');
                    if (ic) {
                        var icon = document.createElement('i');
                        icon.className = ic.className + ' me-2';
                        item.appendChild(icon);
                    }
                    item.appendChild(document.createTextNode(a.getAttribute('data-label') || a.title || a.textContent.trim()));
                    li.appendChild(item);
                    menu.appendChild(li);
                }
                function addDivider() {
                    if (menu.children.length === 0) return;
                    var li = document.createElement('li');
                    li.innerHTML = '<hr class="dropdown-divider m-1">';
                    menu.appendChild(li);
                }
                Array.from(bar.children).forEach(function(child) {
                    if (child.tagName === 'A') {
                        addItem(child);
                    } else if (child.tagName === 'DIV' && child.querySelector('a')) {
                        addDivider();
                        Array.from(child.querySelectorAll('a')).forEach(addItem);
                    }
                    /* <div style="border-left:..."> separators: skip, handled by addDivider() */
                });
                // Mark the active page icon on both the desktop bar and mobile menu.
                // Matching rules:
                //  - Icons with ?mode=X  → active only when current URL also has ?mode=X
                //  - Icons without mode= → active when pathname matches AND current URL
                //    has no mode= (covers status/date filter params on the main page)
                var curPath   = window.location.pathname;
                var curParams = new URLSearchParams(window.location.search);
                var curMode   = curParams.get('mode') || '';
                function isActive(a) {
                    if (a.pathname !== curPath) return false;
                    var aMode = new URLSearchParams(a.search).get('mode') || '';
                    return aMode === curMode;
                }
                Array.from(bar.querySelectorAll('a.btn')).forEach(function(a) {
                    if (isActive(a)) a.classList.add('active');
                });
                Array.from(menu.querySelectorAll('a.dropdown-item')).forEach(function(item) {
                    var u = new URL(item.href, window.location.origin);
                    var aMode = new URLSearchParams(u.search).get('mode') || '';
                    if (u.pathname === curPath && aMode === curMode) item.classList.add('active');
                });
            });
        })();
        (function() {
            var destName = '${destination.name}';
            if (!destName) return;
            // Shared state so whichever runs last (fetch or DOMContentLoaded) can apply the badge.
            var _duCount = 0;
            function applyMobileBadge() {
                if (!_duCount) return;
                var menu = document.getElementById('_destActionsMenu');
                if (!menu) return;
                Array.from(menu.querySelectorAll('a.dropdown-item')).forEach(function(item) {
                    if (item.href && item.href.indexOf('mode=datausers') !== -1
                            && !item.querySelector('.du-mobile-badge')) {
                        var b = document.createElement('span');
                        b.className = 'badge rounded-pill bg-danger ms-auto du-mobile-badge';
                        b.style.cssText = 'font-size:0.6rem;padding:2px 4px;line-height:1;';
                        b.textContent = _duCount;
                        item.style.display = 'flex';
                        item.style.alignItems = 'center';
                        item.appendChild(b);
                    }
                });
            }
            document.addEventListener('DOMContentLoaded', applyMobileBadge);
            fetch('/do/transfer/destination/' + encodeURIComponent(destName) + '?json=dataUsersCount')
                .then(function(r) { return r.ok ? r.json() : null; })
                .then(function(data) {
                    if (!data || !data.count) return;
                    _duCount = data.count;
                    var btn = document.getElementById('_destDataUsersBtn');
                    if (btn) {
                        var badge = document.createElement('span');
                        badge.className = 'position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger';
                        badge.style.cssText = 'font-size:0.6rem;padding:2px 4px;line-height:1;';
                        badge.textContent = _duCount;
                        btn.appendChild(badge);
                    }
                    var sidebar = document.getElementById('_destDataUsersSidebarBadge');
                    if (sidebar) {
                        sidebar.className = 'badge rounded-pill bg-danger ms-1';
                        sidebar.style.cssText = 'font-size:0.6rem;padding:2px 4px;line-height:1;vertical-align:middle;';
                        sidebar.textContent = _duCount;
                    }
                    applyMobileBadge();
                })
                .catch(function() {});
        })();
        </script>
    </div>
    <c:if test="${not empty destination.comment}">
        <p class="dest-page-comment">${destination.comment}</p>
    </c:if>
    <c:if test="${not empty destination.ecUserName}">
        <p class="mb-0 small text-muted">
            <i class="bi bi-person-fill me-1"></i><c:choose>
                <c:when test="${not empty destination.userMail}"><a href="mailto:${destination.userMail}" class="text-muted">${destination.ecUserName}</a></c:when>
                <c:otherwise>${destination.ecUserName}</c:otherwise>
            </c:choose>
        </p>
    </c:if>
</div>
