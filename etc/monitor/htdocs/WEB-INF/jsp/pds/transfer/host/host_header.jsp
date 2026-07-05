<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%-- Detect restricted user once here; stored in request scope so callers (data.jsp etc.) can also use it --%>
<c:set var="isRestrictedUser" value="true" scope="request" />
<auth:if basePathKey="transferhistory.basepath" paths="/">
  <auth:then><c:set var="isRestrictedUser" value="false" scope="request" /></auth:then>
</auth:if>
<c:if test="${not empty host.geoIpLocation}">
    <c:set var="_hGeoParts" value="${fn:split(host.geoIpLocation, '/')}"/>
    <c:set var="_hGeoPart0" value="${fn:trim(_hGeoParts[0])}"/>
    <c:set var="_hGeoPart1" value="${fn:trim(_hGeoParts[1])}"/>
    <c:set var="_hGeoIso" value="${fn:toLowerCase(fn:length(_hGeoPart0) == 2 ? _hGeoPart0 : _hGeoPart1)}"/>
</c:if>
<div class="dest-page-header mb-3">
        <div class="d-flex align-items-center gap-2 flex-wrap mb-1">
					<c:if test="${not host.active}"><i class="bi bi-slash-circle-fill text-danger" title="Host is disabled" style="font-size:0.9rem;"></i></c:if>
                    <a class="dest-page-name text-decoration-none" href="/do/transfer/host/${host.name}"<c:if test="${not host.active}"> style="text-decoration:line-through !important;color:var(--bs-secondary-color)"</c:if>>${host.nickName}</a>
        <div class="d-flex gap-2 align-items-center host-badge-group">
        <c:if test="${host.name != host.nickName}">
            <code class="dest-page-id" title="Host identifier">${host.name}</code>
        </c:if>
        <c:if test="${not empty host.geoIpLocation and fn:length(_hGeoIso) == 2}">
            <span class="fi fi-${_hGeoIso}" title="${host.geoIpLocation}" style="font-size:1.2em;border-radius:2px;"></span>
        </c:if>
        <c:choose>
            <c:when test="${host.type == 'Dissemination'}">
                <a href="/do/transfer/host?hostType=${host.type}&amp;hostSearch=" class="dest-type-link" title="Show all ${host.type} hosts"><span class="badge bg-primary fs-status"><i class="bi bi-send-fill"></i> ${host.type}</span></a>
            </c:when>
            <c:when test="${host.type == 'Acquisition'}">
                <a href="/do/transfer/host?hostType=${host.type}&amp;hostSearch=" class="dest-type-link" title="Show all ${host.type} hosts"><span class="badge bg-success fs-status"><i class="bi bi-cloud-download-fill"></i> ${host.type}</span></a>
            </c:when>
            <c:when test="${host.type == 'Source'}">
                <a href="/do/transfer/host?hostType=${host.type}&amp;hostSearch=" class="dest-type-link" title="Show all ${host.type} hosts"><span class="badge bg-secondary fs-status"><i class="bi bi-database-fill"></i> ${host.type}</span></a>
            </c:when>
            <c:when test="${host.type == 'Replication'}">
                <a href="/do/transfer/host?hostType=${host.type}&amp;hostSearch=" class="dest-type-link" title="Show all ${host.type} hosts"><span class="badge bg-secondary fs-status"><i class="bi bi-copy"></i> ${host.type}</span></a>
            </c:when>
            <c:when test="${host.type == 'Backup'}">
                <a href="/do/transfer/host?hostType=${host.type}&amp;hostSearch=" class="dest-type-link" title="Show all ${host.type} hosts"><span class="badge bg-secondary fs-status"><i class="bi bi-archive-fill"></i> ${host.type}</span></a>
            </c:when>
            <c:when test="${host.type == 'Proxy'}">
                <a href="/do/transfer/host?hostType=${host.type}&amp;hostSearch=" class="dest-type-link" title="Show all ${host.type} hosts"><span class="badge bg-secondary fs-status"><i class="bi bi-arrow-left-right"></i> ${host.type}</span></a>
            </c:when>
            <c:otherwise>
                <a href="/do/transfer/host?hostType=${host.type}&amp;hostSearch=" class="dest-type-link" title="Show all ${host.type} hosts"><span class="badge bg-secondary fs-status">${host.type}</span></a>
            </c:otherwise>
        </c:choose>
        <c:if test="${not empty host.transferMethodName}">
            <a href="/do/transfer/host?hostSearch=method%3D${host.transferMethodName}&amp;hostType=All" class="dest-type-link" title="Show all hosts using ${host.transferMethodName}"><span class="badge bg-info text-dark fs-status"><i class="bi bi-hdd-network me-1"></i>${host.transferMethodName}</span></a>
        </c:if>
        <c:if test="${not empty host.filterName and host.filterName ne 'none'}">
            <jsp:include page="/WEB-INF/jsp/pds/transfer/compression_icon.jsp"><jsp:param name="name" value="${host.filterName}"/></jsp:include>
        </c:if>
        </div>
        <%-- Desktop: full icon bar, hidden on mobile --%>
        <div id="_hostIconBar" class="d-none d-sm-flex gap-2 align-items-center ms-auto">
        <auth:if basePathKey="host.basepath" paths="">
        <auth:then>
        <a href='<bean:message key="host.basepath"/>' class="btn btn-sm btn-outline-secondary" title="All Transfer Hosts"><i class="bi bi-arrow-left"></i></a>
        <c:if test="${not empty host.name}">
        <a href='<bean:message key="host.basepath"/>/${host.name}' class="btn btn-sm btn-outline-secondary" title="Host Main Page"><i class="bi bi-house"></i></a>
        </c:if>
        <div style="border-left:1px solid var(--bs-border-color);height:1.5rem;"></div>
        </auth:then>
        </auth:if>
        <c:set var="_hostHasEditGroup" value="false"/>
        <auth:if basePathKey="host.basepath" paths="/edit/insert_form">
        <auth:then>
        <c:set var="_hostHasEditGroup" value="true"/>
        <div class="d-flex gap-1 align-items-center">
            <a href='<bean:message key="host.basepath"/>/edit/insert_form'
               class="btn btn-sm btn-outline-success" title="Create new host"><i class="bi bi-plus-circle"></i></a>
            <c:if test="${not empty host.id}">
            <a href='<bean:message key="host.basepath"/>/edit/update_form/${host.id}'
               class="btn btn-sm btn-outline-primary" title="Edit this host"><i class="bi bi-pencil"></i></a>
            <a href='<bean:message key="host.basepath"/>/edit/delete_form/${host.id}'
               class="btn btn-sm btn-outline-danger" title="Delete this host"><i class="bi bi-trash"></i></a>
            <c:if test="${not empty host.destinations}">
            <auth:if basePathKey="transferhistory.basepath" paths="/">
            <auth:then>
            <a href="#" class="btn btn-sm btn-outline-warning" title="Duplicate this host"
               onclick="ecpdsHostDuplicate('${host.id}','${host.nickName}');return false;"><i class="bi bi-copy"></i></a>
            </auth:then>
            </auth:if>
            </c:if>
            </c:if>
        </div>
        </auth:then>
        </auth:if>
        <c:if test="${not empty host.id}">
        <div class="d-flex gap-1 align-items-center"<c:if test="${_hostHasEditGroup}"> style="border-left:1px solid var(--bs-border-color);padding-left:0.5rem;"</c:if>>
            <c:if test="${!(isRestrictedUser == 'true' && host.type == 'Proxy')}">
            <a href='<bean:message key="host.basepath"/>/${host.id}?mode=changelog'
               class="btn btn-sm btn-outline-secondary" title="Changes Log"><i class="bi bi-clock-history"></i></a>
            </c:if>
            <auth:if basePathKey="host.basepath" paths="/edit/getOutput/">
            <auth:then>
            <c:choose>
            <c:when test="${host.type == 'Acquisition'}">
            <a href='<bean:message key="host.basepath"/>/edit/getOutput/view/${host.id}'
               id="_hostAcqConsoleBtn"
               class="btn btn-sm btn-outline-secondary position-relative" title="Acquisition Console"><i class="bi bi-terminal"></i></a>
            </c:when>
            <c:otherwise>
            <a href="#" class="btn btn-sm btn-outline-secondary disabled" title="Acquisition Console (Acquisition hosts only)"
               data-label="Acquisition Console" aria-disabled="true" tabindex="-1" onclick="return false;"><i class="bi bi-terminal"></i></a>
            </c:otherwise>
            </c:choose>
            </auth:then>
            </auth:if>
            <a href='<bean:message key="host.basepath"/>/edit/getReport/${host.id}'
               class="btn btn-sm btn-outline-secondary" title="Network Info"><i class="bi bi-wifi"></i></a>
            <c:choose>
              <c:when test="${not empty moduleGuide}">
                <a href="#" class="btn btn-sm btn-outline-info" title="Configuration Guide"
                   onclick="var el=document.getElementById('moduleGuideOffcanvas');if(el)bootstrap.Offcanvas.getOrCreateInstance(el).show();return false;"><i class="bi bi-book"></i></a>
              </c:when>
              <c:otherwise>
                <a href="#" class="btn btn-sm btn-outline-secondary disabled" title="No configuration guide available for this module"
                   data-label="Configuration Guide" aria-disabled="true" tabindex="-1" onclick="return false;"><i class="bi bi-book"></i></a>
              </c:otherwise>
            </c:choose>
            <auth:if basePathKey="transferhistory.basepath" paths="/">
            <auth:then>
            <div style="border-left:1px solid var(--bs-border-color);height:1.5rem;"></div>
            <a href="#" class="btn btn-sm btn-outline-warning" title="Clean Options"
               onclick="confirmationDialog({title:'Clean Options',message:'Clean the data window options for host <b>${host.nickName}</b>?<br/><br/>This will remove all options with default values from the option properties editor, simplifying the configuration. This action cannot be undone.',confirmText:'Clean',showLoading:true,onConfirm:function(){window.location.href='<bean:message key="host.basepath"/>/edit/cleanDataWindow/${host.id}'}}); return false;"><i class="bi bi-sliders"></i></a>
            <a href="#" class="btn btn-sm btn-outline-warning" title="Reset Stats"
               onclick="confirmationDialog({title:'Reset Stats',message:'Reset transfer statistics for host <b>${host.nickName}</b>?<br/><br/>This will permanently clear all accumulated transfer counters (bytes sent, transfer counts, error counts, etc.). This action cannot be undone.',confirmText:'Reset',showLoading:true,onConfirm:function(){window.location.href='<bean:message key="host.basepath"/>/edit/resetStats/${host.id}'}}); return false;"><i class="bi bi-arrow-counterclockwise"></i></a>
            </auth:then>
            </auth:if>
        </div>
        </c:if>
        </div><%-- end #_hostIconBar --%>

        <%-- Mobile: ⋯ dropdown, hidden on sm+ --%>
        <div class="d-sm-none ms-auto host-mobile-menu">
            <div class="dropdown">
                <button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button"
                        id="_hostActionsToggle" data-bs-toggle="dropdown" aria-expanded="false"
                        title="Actions">
                    <i class="bi bi-three-dots"></i>
                </button>
                <ul class="dropdown-menu dropdown-menu-end" id="_hostActionsMenu" aria-labelledby="_hostActionsToggle"></ul>
            </div>
        </div>
        <script>
        (function() {
            document.addEventListener('DOMContentLoaded', function() {
                var bar  = document.getElementById('_hostIconBar');
                var menu = document.getElementById('_hostActionsMenu');
                if (!bar || !menu) return;
                function addItem(a) {
                    var li   = document.createElement('li');
                    var isDisabled = a.classList.contains('disabled') || a.getAttribute('aria-disabled') === 'true';
                    var item;
                    if (isDisabled) {
                        item = document.createElement('span');
                        item.className = 'dropdown-item disabled';
                        item.setAttribute('aria-disabled', 'true');
                    } else {
                        item = document.createElement('a');
                        item.className = 'dropdown-item';
                        item.href = a.getAttribute('href');
                        if (a.getAttribute('onclick')) item.setAttribute('onclick', a.getAttribute('onclick'));
                    }
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
                        Array.from(child.children).forEach(function(gc) {
                            if (gc.tagName === 'A') {
                                addItem(gc);
                            } else if (gc.tagName === 'DIV' && gc.querySelector('a')) {
                                addDivider();
                                Array.from(gc.querySelectorAll('a')).forEach(addItem);
                            } else if (gc.tagName === 'DIV' && !gc.querySelector('a')) {
                                addDivider();
                            }
                        });
                    }
                });
                // Mark the active page icon on both the desktop bar and mobile menu
                var curPath   = window.location.pathname;
                var curSearch = window.location.search;
                Array.from(bar.querySelectorAll('a.btn')).forEach(function(a) {
                    if (a.getAttribute('href') === '#') return;
                    if (a.pathname === curPath && a.search === curSearch) {
                        a.classList.add('active');
                    }
                });
                Array.from(menu.querySelectorAll('a.dropdown-item')).forEach(function(item) {
                    if (!item.href || item.getAttribute('href') === '#') return;
                    var u = new URL(item.href, window.location.origin);
                    if (u.pathname === curPath && u.search === curSearch) {
                        item.classList.add('active');
                    }
                });
            });
        })();
        </script>
<c:if test="${host.type == 'Acquisition'}">
<script>
(function() {
    var _hostId = '<c:out value="${host.id}"/>';

    function _acqBadgeHtml(state) {
        if (state === 'live') {
            return '<span class="spinner-grow" style="width:0.5rem;height:0.5rem;vertical-align:middle;"></span>';
        }
        return '<span class="spinner-border" style="width:0.5rem;height:0.5rem;border-width:2px;vertical-align:middle;"></span>';
    }

    // state: 'live', 'watching', or falsy to clear
    function applyAcqBadge(state) {
        var colour = state === 'live' ? 'bg-success' : 'bg-primary';
        // Icon bar button
        var btn = document.getElementById('_hostAcqConsoleBtn');
        if (btn) {
            var existing = btn.querySelector('.acq-run-badge');
            if (existing) existing.remove();
            if (state) {
                var b = document.createElement('span');
                b.className = 'position-absolute top-0 start-100 translate-middle badge rounded-pill ' + colour + ' acq-run-badge';
                b.style.cssText = 'font-size:0.55rem;padding:3px 4px;line-height:1;';
                b.innerHTML = _acqBadgeHtml(state);
                btn.appendChild(b);
            }
        }
        // Mobile dropdown
        var menu = document.getElementById('_hostActionsMenu');
        if (menu) {
            menu.querySelectorAll('a.dropdown-item').forEach(function(item) {
                if (!item.href || item.href.indexOf('getOutput/view') === -1) return;
                var eb = item.querySelector('.acq-run-badge-mob');
                if (eb) eb.remove();
                if (state) {
                    var b2 = document.createElement('span');
                    b2.className = 'badge rounded-pill ' + colour + ' ms-auto acq-run-badge-mob';
                    b2.style.cssText = 'font-size:0.55rem;padding:3px 4px;line-height:1;';
                    b2.innerHTML = _acqBadgeHtml(state);
                    item.style.display = 'flex';
                    item.style.alignItems = 'center';
                    item.appendChild(b2);
                }
            });
        }
    }

    function checkAndApply() {
        fetch('/do/transfer/host/' + encodeURIComponent(_hostId) + '?json=acquisitionRunning')
            .then(function(r) { return r.ok ? r.json() : null; })
            .then(function(data) { applyAcqBadge((data && data.running) ? 'watching' : ''); })
            .catch(function() {});
    }

    // Run after DOM is ready (mobile menu is built in DOMContentLoaded)
    document.addEventListener('DOMContentLoaded', checkAndApply);
    // Expose so output.jsp Progress JS can sync it without an extra fetch
    window._applyAcqBadge = applyAcqBadge;

    // Poll every 10 s on all pages except the Acquisition Console itself
    // (which drives the badge directly from its own Progress polling via _applyAcqBadge).
    // Stop polling when the tab is hidden to save resources.
    if (window.location.href.indexOf('getOutput/view') === -1) {
        var _pollTimer = null;
        function _schedulePoll() {
            _pollTimer = setTimeout(function() {
                if (document.visibilityState !== 'hidden') checkAndApply();
                _schedulePoll();
            }, 4000);
        }
        document.addEventListener('DOMContentLoaded', _schedulePoll);
        document.addEventListener('visibilitychange', function() {
            if (document.visibilityState === 'visible') {
                checkAndApply(); // immediate refresh when tab becomes visible again
            }
        });
    }
}());
</script>
</c:if>
    </div>
<script>
function ecpdsHostDuplicate(hostId, nickName) {
    var destinations = [<c:forEach var="_d" items="${host.destinations}" varStatus="_s">'<c:out value="${_d.name}"/>'<c:if test="${!_s.last}">,</c:if></c:forEach>];
    var extra = '';
    if (destinations.length > 1) {
        var opts = destinations.map(function(n){return '<option value="'+n+'">'+n+'</option>';}).join('');
        extra = '<br><br>Select destination:<br><select id="ecpds-dup-dest" class="form-select form-select-sm mt-1">'+opts+'</select>';
    }
    confirmationDialog({
        title: 'Confirm Host Duplication',
        message: destinations.length === 1
            ? 'Are you sure you want to duplicate host <strong>'+nickName+'</strong> in destination <strong>'+destinations[0]+'</strong>?'
            : 'Are you sure you want to duplicate host <strong>'+nickName+'</strong>?'+extra,
        showLoading: true,
        onConfirm: function() {
            var dest = destinations.length === 1 ? destinations[0] : document.getElementById('ecpds-dup-dest').value;
            window.location.href = '/do/transfer/destination/operations/'+dest+'/duplicateHost/'+hostId;
        }
    });
}
</script>
    <c:if test="${not empty host.comment}">
        <p class="dest-page-comment">${host.comment}</p>
    </c:if>
    <c:if test="${not empty host.ECUserName}">
        <p class="mb-0 small text-muted">
            <i class="bi bi-person-fill me-1"></i><c:choose>
                <c:when test="${not empty host.userMail}"><a href="mailto:${host.userMail}" class="text-muted">${host.ECUserName}</a></c:when>
                <c:otherwise>${host.ECUserName}</c:otherwise>
            </c:choose>
        </p>
    </c:if>
</div>
