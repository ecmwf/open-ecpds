<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%-- Filter form --%>
<auth:if basePathKey="transferhistory.basepath" paths="/">
    <auth:then>
        <form class="mb-3" id="hostSearchForm">
            <div class="card border-0 shadow-sm">
                <div class="card-body py-2 px-3">
                    <%-- Row 1: search + type + button --%>
                    <div class="row g-1 mb-1">
                        <div class="col-7">
                            <div class="input-group">
                                <span class="input-group-text text-muted"><i class="bi bi-search"></i></span>
                                <input class="form-control" name="hostSearch" id="hostSearch" type="text"
                                    placeholder="e.g. enabled=yes method=*Http hostname=*.test.fr id&gt;=100 options=*mqtt* nickname=Test_0? case=i"
                                    title="Default search is by nickname. Use id, hostname, login, password, nickname, comment, dir, enabled, method, email and options rules."
                                    value='<c:out value="${hostSearch}"/>'>
                            </div>
                        </div>
                        <div class="col-3">
                            <div class="input-group">
                                <span class="input-group-text text-muted"><i class="bi bi-tag"></i></span>
                                <select class="form-select" name="hostType" id="hostType" onchange="form.submit()" title="Filter by Type">
                                    <c:forEach var="option" items="${typeOptions}">
                                        <option value="${option.name}" <c:if test="${hostType == option.name}">selected</c:if>>${option.value}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="col-2 d-flex gap-1">
                            <button type="submit" class="btn btn-primary flex-grow-1"><i class="bi bi-search"></i> Search</button>
                            <button type="button" class="btn btn-outline-secondary px-2"
                                    id="btnHostQB"
                                    onclick="toggleQBPanel('hostQueryBuilder','btnHostQB')"
                                    title="Build query">
                                <i class="bi bi-sliders2"></i>
                            </button>
                        </div>
                    </div>
                    <%-- Row 2: secondary filters --%>
                    <div class="row g-2">
                        <div class="col-4">
                            <div class="input-group input-group-sm">
                                <span class="input-group-text text-muted"><i class="bi bi-diagram-3"></i></span>
                                <select class="form-select form-select-sm" name="network" onchange="form.submit()" title="Filter by Network">
                                    <c:forEach var="option" items="${networkOptions}">
                                        <option value="${option.name}" <c:if test="${network == option.name}">selected</c:if>>${option.value}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="col-4">
                            <div class="input-group input-group-sm">
                                <span class="input-group-text text-muted"><i class="bi bi-bookmark"></i></span>
                                <select class="form-select form-select-sm" name="label" onchange="form.submit()" title="Filter by Label">
                                    <c:forEach var="option" items="${labelOptions}">
                                        <option value="${option.name}" <c:if test="${label == option.name}">selected</c:if>>${option.value}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="col-4">
                            <div class="input-group input-group-sm">
                                <span class="input-group-text text-muted"><i class="bi bi-file-zip"></i></span>
                                <select class="form-select form-select-sm" name="hostFilter" onchange="form.submit()" title="Filter by Compression">
                                    <c:forEach var="option" items="${filterOptions}">
                                        <option value="${option.name}" <c:if test="${hostFilter == option.name}">selected</c:if>>${option.value}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                    </div>

                    <%-- Query Builder panel --%>
                    <div id="hostQueryBuilder" class="border rounded p-2"
                         style="display:none; position:absolute; z-index:9999; background:var(--bs-body-bg); box-shadow:0 4px 16px rgba(0,0,0,0.15); font-size:0.85rem">
                        <div class="row g-1 mb-1">
                                <div class="col-4">
                                    <label class="form-label mb-0 fw-semibold"><code>nickname=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="hqb_nickname" placeholder="e.g. Test_0?" oninput="hqbPreview()" list="hqb_nickname_list" autocomplete="off">
                                    <datalist id="hqb_nickname_list">
                                        <c:forEach var="n" items="${hostNickNames}">
                                            <option value="${n}">
                                        </c:forEach>
                                    </datalist>
                                </div>
                                <div class="col-4">
                                    <label class="form-label mb-0 fw-semibold"><code>hostname=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="hqb_hostname" placeholder="e.g. *.test.fr" oninput="hqbPreview()" list="hqb_hostname_list" autocomplete="off">
                                    <datalist id="hqb_hostname_list">
                                        <c:forEach var="h" items="${hostHostNames}">
                                            <option value="${h}">
                                        </c:forEach>
                                    </datalist>
                                </div>
                                <div class="col-4">
                                    <label class="form-label mb-0 fw-semibold"><code>method=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="hqb_method" placeholder="e.g. *Http" oninput="hqbPreview()" list="hqb_method_list" autocomplete="off">
                                    <datalist id="hqb_method_list">
                                        <c:forEach var="m" items="${transferMethodOptions}">
                                            <option value="${m.name}">
                                        </c:forEach>
                                    </datalist>
                                </div>
                            </div>
                            <div class="row g-1 mb-1">
                                <div class="col-4">
                                    <label class="form-label mb-0 fw-semibold"><code>id</code> <span class="text-muted fw-normal">numeric</span></label>
                                    <div class="input-group input-group-sm">
                                        <select class="form-select form-select-sm" id="hqb_id_op" style="max-width:65px" onchange="hqbPreview()">
                                            <option value="=">=</option><option value=">=">&gt;=</option><option value=">">&gt;</option><option value="<=">&lt;=</option><option value="<">&lt;</option>
                                        </select>
                                        <input type="number" class="form-control form-control-sm" id="hqb_id_val" placeholder="e.g. 100" oninput="hqbPreview()">
                                    </div>
                                </div>
                                <div class="col-4">
                                    <label class="form-label mb-0 fw-semibold"><code>login=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="hqb_login" oninput="hqbPreview()">
                                </div>
                                <div class="col-4">
                                    <label class="form-label mb-0 fw-semibold"><code>dir=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="hqb_dir" oninput="hqbPreview()">
                                </div>
                            </div>
                            <div class="row g-1 mb-1">
                                <div class="col-4">
                                    <label class="form-label mb-0 fw-semibold"><code>comment=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="hqb_comment" placeholder="e.g. *test*" oninput="hqbPreview()">
                                </div>
                                <div class="col-4">
                                    <label class="form-label mb-0 fw-semibold"><code>email=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="hqb_email" placeholder="e.g. *@domain.com" oninput="hqbPreview()">
                                </div>
                                <div class="col-4">
                                    <label class="form-label mb-0 fw-semibold"><code>options=</code> <span class="text-muted fw-normal">Properties &amp; JS wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="hqb_options" placeholder="e.g. *mqtt*" oninput="hqbPreview()">
                                </div>
                            </div>
                            <div class="row g-1 mb-1">
                                <div class="col-6">
                                    <label class="form-label mb-0 fw-semibold"><code>enabled</code></label>
                                    <select class="form-select form-select-sm" id="hqb_enabled" onchange="hqbPreview()">
                                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                                    </select>
                                </div>
                                <div class="col-6">
                                    <label class="form-label mb-0 fw-semibold"><code>case=</code></label>
                                    <select class="form-select form-select-sm" id="hqb_case" onchange="hqbPreview()">
                                        <option value="s">Sensitive (default)</option>
                                        <option value="i">Case-insensitive</option>
                                    </select>
                                </div>
                            </div>
                            <%-- Live preview + action buttons --%>
                            <div class="d-flex align-items-start gap-1 pt-1 border-top mt-1">
                                <i class="bi bi-terminal text-muted flex-shrink-0"></i>
                                <code class="text-muted flex-grow-1" style="font-size:0.8rem;word-break:break-all" id="hqb_preview">-- fill in fields above --</code>
                                <button type="button" class="btn btn-sm btn-outline-secondary" onclick="hqbClear()">
                                    <i class="bi bi-x-circle me-1"></i>Clear
                                </button>
                                <button type="button" class="btn btn-sm btn-primary" onclick="hqbApply()">
                                    <i class="bi bi-check-lg me-1"></i>Apply &amp; Search
                                </button>
                            </div>
                        </div>
                </div>
            </div>
        </form>
        <script>
        function hqbVal(id) { return document.getElementById(id) ? document.getElementById(id).value.trim() : ''; }
        function hqbQuote(v) { var q=v.indexOf(' ')>=0||v.indexOf('=')>=0||v.indexOf('"')>=0; return q?'"'+v.replace(/"/g,'\\"')+'"':v; }
        function hqbBuild() {
            var p = [];
            var idVal = hqbVal('hqb_id_val');
            if (idVal) p.push('id' + hqbVal('hqb_id_op') + idVal);
            ['nickname','hostname','method','comment','email','options','login','dir'].forEach(function(f) {
                var v = hqbVal('hqb_' + f); if (v) p.push(f + '=' + hqbQuote(v));
            });
            var en = hqbVal('hqb_enabled'); if (en) p.push('enabled=' + en);
            if (hqbVal('hqb_case') === 'i') p.push('case=i');
            return p.join(' ');
        }
        function hqbPreview() {
            var q = hqbBuild();
            document.getElementById('hqb_preview').textContent = q || '-- fill in fields above --';
        }
        function hqbApply() {
            document.getElementById('hostSearch').value = hqbBuild();
            document.getElementById('hostSearchForm').submit();
        }
        function hqbClear() {
            ['nickname','hostname','method','comment','email','options','login','dir','id_val'].forEach(function(f) {
                document.getElementById('hqb_' + f).value = '';
            });
            document.getElementById('hqb_enabled').value = '';
            document.getElementById('hqb_id_op').value = '=';
            document.getElementById('hqb_case').value = 's';
            hqbPreview();
        }
        function toggleQBPanel(panelId, btnId) {
            var panel = document.getElementById(panelId);
            var btn = document.getElementById(btnId);
            if (panel.style.display === 'block') { panel.style.display = 'none'; return; }
            if (panel.parentElement !== document.body) { document.body.appendChild(panel); }
            var r = btn.getBoundingClientRect();
            var sy = window.pageYOffset || document.documentElement.scrollTop;
            var sx = window.pageXOffset || document.documentElement.scrollLeft;
            var pw = 740;
            panel.style.top = (r.bottom + sy + 4) + 'px';
            panel.style.left = Math.max(sx, r.right + sx - pw) + 'px';
            panel.style.width = pw + 'px';
            panel.style.right = 'auto';
            panel.style.display = 'block';
        }
        document.addEventListener('click', function(e) {
            var panel = document.getElementById('hostQueryBuilder');
            var btn = document.getElementById('btnHostQB');
            if (panel && panel.style.display === 'block' && !panel.contains(e.target) && btn && !btn.contains(e.target))
                panel.style.display = 'none';
        });
        </script>
    </auth:then>
</auth:if>

<%-- No results --%>
<c:if test="${empty hosts}">
    <div class="alert">
        <c:if test="${!hasHostSearch}">
            No Hosts found based on these criteria.
        </c:if>
        <c:if test="${hasHostSearch}">
            <c:if test="${!empty getHostsError}">
                <strong>Error in your query:</strong> ${getHostsError}
            </c:if>
            <c:if test="${empty getHostsError}">
                No Hosts found. Default search is by nickname.
            </c:if>
            <p class="mb-1 mt-2">You can conduct an extended search using the following rules:</p>
            <ul class="mb-0">
                <li><code>id</code>, <code>hostname=</code>, <code>login=</code>, <code>password=</code>, <code>nickname=</code>, <code>comment=</code>, <code>dir=</code>, <code>enabled=yes/no</code>, <code>method=</code>, <code>email=</code>, <code>options=</code></li>
                <li>Example: <code>enabled=yes method=*Http hostname=*.test.fr id&gt;=100 options=*mqtt* nickname=Test_0? case=i</code></li>
                <li><code>case=i</code> for case-insensitive, <code>case=s</code> for case-sensitive (default)</li>
                <li>Enclose values with spaces or equals signs in double quotes, e.g. <code>"United States"</code> or <code>"a=b"</code></li>
                <li>Wildcards: <code>*</code> (zero or more chars), <code>?</code> (exactly one char)</li>
            </ul>
        </c:if>
    </div>
</c:if>

<%-- Results table --%>
<c:if test="${not empty hosts}">
    <div class="d-flex align-items-center mb-2 gap-2">
        <span class="text-muted small"><i class="bi bi-list-ul"></i> <strong>${hostsSize}</strong> host(s) found</span>
    </div>
    <table id="hostsTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
        <thead class="table-light">
            <tr>
                <th style="width:28px;"></th>
                <th>Host</th>
                <th>Hostname/IP</th>
                <th>Network</th>
                <th>Label</th>
                <th>Destinations</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="host" items="${hosts}">
                <c:if test="${not empty host.geoIpLocation}">
                    <c:set var="_hGeoParts" value="${fn:split(host.geoIpLocation, '/')}"/>
                    <c:set var="_hGeoPart0" value="${fn:trim(_hGeoParts[0])}"/>
                    <c:set var="_hGeoPart1" value="${fn:trim(_hGeoParts[1])}"/>
                    <c:set var="_hGeoIso" value="${fn:toLowerCase(fn:length(_hGeoPart0) == 2 ? _hGeoPart0 : _hGeoPart1)}"/>
                </c:if>
                <tr>
                    <td>
                        <c:if test="${not empty host.geoIpLocation}">
                            <span class="fi fi-${_hGeoIso}"
                                  title="${host.geoIpLocation}"
                                  style="font-size:1.1em;display:block"></span>
                        </c:if>
                    </td>
                    <td>
                        <span style="white-space:nowrap"><c:if test="${not host.active}"><i class="bi bi-slash-circle-fill text-danger me-1" title="Disabled" style="font-size:0.78rem;"></i></c:if><c:choose>
                            <c:when test="${not host.active}"><a href="<bean:message key="host.basepath"/>/${host.name}"
                               class="fw-semibold dest-list-link"
                               style="text-decoration:line-through;color:var(--bs-secondary-color)"
                               >${host.nickName}</a></c:when>
                            <c:otherwise><a href="<bean:message key="host.basepath"/>/${host.name}"
                               class="fw-semibold text-decoration-none dest-list-link"
                               >${host.nickName}</a></c:otherwise>
                        </c:choose><c:if test="${host.name != host.nickName}">
                            <code class="dest-page-id ms-1" style="font-size:0.75rem;" title="Host identifier">${host.name}</code></c:if>
                            <c:choose>
                                <c:when test="${host.type == 'Dissemination'}">
                                    <span class="badge bg-secondary ms-1" style="font-size:0.7rem;" title="Dissemination"><i class="bi bi-send-fill"></i></span>
                                </c:when>
                                <c:when test="${host.type == 'Acquisition'}">
                                    <span class="badge bg-secondary ms-1" style="font-size:0.7rem;" title="Acquisition"><i class="bi bi-cloud-download-fill"></i></span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-secondary ms-1" style="font-size:0.7rem;">${host.type}</span>
                                </c:otherwise>
                            </c:choose>
                            <c:if test="${not empty host.transferMethodName}">
                                <span class="badge bg-info text-dark ms-1" style="font-size:0.7rem;" title="${host.transferMethod.comment}"><i class="bi bi-hdd-network me-1"></i>${host.transferMethodName}</span>
                            </c:if>
                            <c:if test="${not empty host.filterName and host.filterName ne 'none'}">
                                <jsp:include page="/WEB-INF/jsp/pds/transfer/compression_icon.jsp"><jsp:param name="name" value="${host.filterName}"/></jsp:include>
                            </c:if></span>
                        <c:if test="${not empty host.comment}">
                            <div class="text-muted" style="font-size:0.78rem; line-height:1.3; margin-top:1px;">${host.comment}</div>
                        </c:if>
                    </td>
                    <td class="text-muted small">${host.host}</td>
                    <td class="small">${host.transferGroupName}</td>
                    <td class="small">${host.networkName}</td>
                    <td class="small">
                        <c:choose>
                            <c:when test="${fn:length(host.destinations) == 0}">
                                <span class="text-muted fst-italic">none</span>
                            </c:when>
                            <c:when test="${fn:length(host.destinations) le 3}">
                                <c:forEach var="destination" items="${host.destinations}">
                                    <a href="<bean:message key="destination.basepath"/>/${destination.name}"
                                       class="badge bg-light text-secondary border text-decoration-none me-1">${destination.name}</a>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <span class="badge bg-light text-secondary border">${fn:length(host.destinations)} destinations</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
    <script>
    $(function() {
        $('#hostsTable').DataTable({
            paging:    true,
            pageLength: 25,
            searching: false,
            order:     [],
            columnDefs: [{ orderable: false, targets: [0] }],
            language: { lengthMenu: 'Show _MENU_ per page', info: 'Showing _START_-_END_ of _TOTAL_' }
        });
    });
    </script>
</c:if>
