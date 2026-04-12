<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/fn.tld" prefix="fn"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<script>window._validIso=new Set(["AC","AD","AE","AF","AG","AI","AL","AM","AO","AQ","AR","AS","AT","AU","AW","AX","AZ","BA","BB","BD","BE","BF","BG","BH","BI","BJ","BL","BM","BN","BO","BQ","BR","BS","BT","BV","BW","BY","BZ","CA","CC","CD","CF","CG","CH","CI","CK","CL","CM","CN","CO","CP","CR","CU","CV","CW","CX","CY","CZ","DE","DG","DJ","DK","DM","DO","DZ","EA","EE","EG","EH","ER","ES","ET","EU","FI","FJ","FK","FM","FO","FR","GA","GB","GD","GE","GF","GG","GH","GI","GL","GM","GN","GP","GQ","GR","GS","GT","GU","GW","GY","HK","HM","HN","HR","HT","HU","IC","ID","IE","IL","IM","IN","IO","IQ","IR","IS","IT","JE","JM","JO","JP","KE","KG","KH","KI","KM","KN","KP","KR","KW","KY","KZ","LA","LB","LC","LI","LK","LR","LS","LT","LU","LV","LY","MA","MC","MD","ME","MF","MG","MH","MK","ML","MM","MN","MO","MP","MQ","MR","MS","MT","MU","MV","MW","MX","MY","MZ","NA","NC","NE","NF","NG","NI","NL","NO","NP","NR","NU","NZ","OM","PA","PE","PF","PG","PH","PK","PL","PM","PN","PR","PS","PT","PW","PY","QA","RE","RO","RS","RU","RW","SA","SB","SC","SD","SE","SG","SH","SI","SJ","SK","SL","SM","SN","SO","SR","SS","ST","SV","SX","SY","SZ","TA","TC","TD","TF","TG","TH","TJ","TK","TL","TM","TN","TO","TR","TT","TV","TW","TZ","UA","UG","UM","UN","US","UY","UZ","VA","VC","VE","VG","VI","VN","VU","WF","WS","XK","YE","YT","ZA","ZM","ZW"]);</script>

<%-- Filter form --%>
<auth:if basePathKey="transferhistory.basepath" paths="/">
    <auth:then>
        <form class="mb-3" id="destinationSearchForm">
            <div class="card border-0 shadow-sm">
                <div class="card-body py-2 px-3">
                    <%-- Row 1: main search + type + button --%>
                    <div class="row g-2 mb-2">
                        <div class="col-7">
                            <div class="input-group">
                                <span class="input-group-text text-muted bg-white"><i class="bi bi-search"></i></span>
                                <input class="form-control" name="destinationSearch" id="destinationSearch" type="text"
                                    placeholder="e.g. enabled=yes name=AB? email=*@meteo.ms comment=*test* country=fr options=*mqtt* case=i"
                                    title="Default search is by name. Use name, comment, country, email, enabled, monitor, backup, forceproxy and options rules."
                                    value='<c:out value="${destinationSearch}"/>'>
                            </div>
                        </div>
                        <div class="col-3">
                            <div class="input-group">
                                <span class="input-group-text text-muted bg-white"><i class="bi bi-tag"></i></span>
                                <select class="form-select" name="destinationType" id="destinationType" onchange="form.submit()" title="Filter by Type">
                                    <c:forEach var="option" items="${typeOptions}">
                                        <option value="${option.name}" <c:if test="${destinationType == option.name}">selected</c:if>>${option.value}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="col-2 d-flex gap-1">
                            <button type="submit" class="btn btn-primary flex-grow-1"><i class="bi bi-search"></i> Search</button>
                            <button type="button" class="btn btn-outline-secondary px-2"
                                    data-bs-toggle="collapse" data-bs-target="#destQueryBuilder"
                                    title="Build query" aria-expanded="false" aria-controls="destQueryBuilder">
                                <i class="bi bi-sliders2"></i>
                            </button>
                        </div>
                    </div>
                    <%-- Row 2: secondary filters --%>
                    <div class="row g-2">
                        <div class="col-3">
                            <div class="input-group input-group-sm">
                                <span class="input-group-text text-muted bg-white"><i class="bi bi-activity"></i></span>
                                <select class="form-select form-select-sm" name="destinationStatus" onchange="form.submit()" title="Filter by Status">
                                    <c:forEach var="option" items="${statusOptions}">
                                        <option value="${option}" <c:if test="${destinationStatus == option}">selected</c:if>>${option}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="col-3">
                            <div class="input-group input-group-sm">
                                <span class="input-group-text text-muted bg-white"><i class="bi bi-file-zip"></i></span>
                                <select class="form-select form-select-sm" name="destinationFilter" onchange="form.submit()" title="Filter by Compression">
                                    <c:forEach var="option" items="${filterOptions}">
                                        <option value="${option.name}" <c:if test="${destinationFilter == option.name}">selected</c:if>>${option.value}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="col-3">
                            <div class="input-group input-group-sm">
                                <span class="input-group-text text-muted bg-white"><i class="bi bi-diagram-2"></i></span>
                                <select class="form-select form-select-sm" name="aliases" onchange="form.submit()" title="Aliased From/To">
                                    <option value="all" <c:if test="${aliases == 'all'}">selected</c:if>>All Destinations</option>
                                    <option value="to"  <c:if test="${aliases == 'to'}">selected</c:if>>Aliased From ...</option>
                                    <option value="from" <c:if test="${aliases == 'from'}">selected</c:if>>Aliases To ...</option>
                                </select>
                            </div>
                        </div>
                        <div class="col-3">
                            <div class="input-group input-group-sm">
                                <span class="input-group-text text-muted bg-white"><i class="bi bi-sort-alpha-down"></i></span>
                                <select class="form-select form-select-sm" name="sortDirection" onchange="form.submit()" title="Sort Direction">
                                    <option value="asc"  <c:if test="${sortDirection == 'asc'}">selected</c:if>>Ascending</option>
                                    <option value="desc" <c:if test="${sortDirection == 'desc'}">selected</c:if>>Descending</option>
                                </select>
                            </div>
                        </div>
                    </div>

                    <%-- Query Builder collapse panel --%>
                    <div class="collapse mt-2" id="destQueryBuilder">
                        <div class="border rounded p-2 bg-white" style="font-size:0.85rem">
                            <div class="row g-2 mb-2">
                                <div class="col-md-4">
                                    <label class="form-label mb-1 fw-semibold"><code>name=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="dqb_name" placeholder="e.g. dest_*" oninput="dqbPreview()" list="dqb_name_list" autocomplete="off">
                                    <datalist id="dqb_name_list">
                                        <c:forEach var="d" items="${destinationNames}">
                                            <option value="${d.name}">
                                        </c:forEach>
                                    </datalist>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label mb-1 fw-semibold"><code>comment=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="dqb_comment" placeholder="e.g. *test*" oninput="dqbPreview()">
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label mb-1 fw-semibold"><code>email=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="dqb_email" placeholder="e.g. *@meteo.ms" oninput="dqbPreview()">
                                </div>
                            </div>
                            <div class="row g-2 mb-2">
                                <div class="col-md-3">
                                    <label class="form-label mb-1 fw-semibold"><code>country=</code></label>
                                    <div class="d-flex align-items-center gap-2">
                                        <select class="form-select form-select-sm" id="dqb_country" onchange="dqbPreview()">
                                            <option value="">Any</option>
                                        </select>
                        <span id="dqb_country_flag" class="fi" style="font-size:1.4em;display:none;flex-shrink:0"></span>
                                    </div>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label mb-1 fw-semibold"><code>options=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="dqb_options" placeholder="e.g. *mqtt*" oninput="dqbPreview()">
                                </div>
                                <div class="col-md-2">
                                    <label class="form-label mb-1 fw-semibold"><code>case=</code></label>
                                    <select class="form-select form-select-sm" id="dqb_case" onchange="dqbPreview()">
                                        <option value="s">Sensitive (default)</option>
                                        <option value="i">Case-insensitive</option>
                                    </select>
                                </div>
                            </div>
                            <div class="row g-2 mb-2">
                                <div class="col">
                                    <label class="form-label mb-1 fw-semibold"><code>enabled</code></label>
                                    <select class="form-select form-select-sm" id="dqb_enabled" onchange="dqbPreview()">
                                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                                    </select>
                                </div>
                                <div class="col">
                                    <label class="form-label mb-1 fw-semibold"><code>monitor</code></label>
                                    <select class="form-select form-select-sm" id="dqb_monitor" onchange="dqbPreview()">
                                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                                    </select>
                                </div>
                                <div class="col">
                                    <label class="form-label mb-1 fw-semibold"><code>backup</code></label>
                                    <select class="form-select form-select-sm" id="dqb_backup" onchange="dqbPreview()">
                                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                                    </select>
                                </div>
                                <div class="col">
                                    <label class="form-label mb-1 fw-semibold"><code>forceproxy</code></label>
                                    <select class="form-select form-select-sm" id="dqb_forceproxy" onchange="dqbPreview()">
                                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                                    </select>
                                </div>
                                <div class="col-md-5"></div>
                            </div>
                            <%-- Live preview + action buttons --%>
                            <div class="d-flex align-items-center gap-2 pt-1 border-top mt-1">
                                <i class="bi bi-terminal text-muted flex-shrink-0"></i>
                                <code class="text-muted flex-grow-1 text-truncate" id="dqb_preview" style="font-size:0.8rem">-- fill in fields above --</code>
                                <button type="button" class="btn btn-sm btn-outline-secondary" onclick="dqbClear()">
                                    <i class="bi bi-x-circle me-1"></i>Clear
                                </button>
                                <button type="button" class="btn btn-sm btn-primary" onclick="dqbApply()">
                                    <i class="bi bi-check-lg me-1"></i>Apply &amp; Search
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </form>
        <script>
        function dqbVal(id) { return document.getElementById(id) ? document.getElementById(id).value.trim() : ''; }
        function dqbQuote(v) { var q=v.indexOf(' ')>=0||v.indexOf('=')>=0||v.indexOf('"')>=0; return q?'"'+v.replace(/"/g,'\\"')+'"':v; }
        function dqbBuild() {
            var p = [];
            ['name','comment','email','country','options'].forEach(function(f) {
                var v = dqbVal('dqb_' + f); if (v) p.push(f + '=' + dqbQuote(v));
            });
            ['enabled','monitor','backup','forceproxy'].forEach(function(f) {
                var v = dqbVal('dqb_' + f); if (v) p.push(f + '=' + v);
            });
            if (dqbVal('dqb_case') === 'i') p.push('case=i');
            return p.join(' ');
        }
        function dqbPreview() {
            var q = dqbBuild();
            document.getElementById('dqb_preview').textContent = q || '-- fill in fields above --';
        }
        function dqbApply() {
            document.getElementById('destinationSearch').value = dqbBuild();
            document.getElementById('destinationSearchForm').submit();
        }
        function dqbClear() {
            ['name','comment','email','country','options'].forEach(function(f) {
                document.getElementById('dqb_' + f).value = '';
            });
            ['enabled','monitor','backup','forceproxy'].forEach(function(f) {
                document.getElementById('dqb_' + f).value = '';
            });
            document.getElementById('dqb_case').value = 's';
            dqbPreview();
        }
        // Populate country selector from rendered destination rows
        document.addEventListener('DOMContentLoaded', function() {
            var seen = {}, opts = [];
            document.querySelectorAll('[data-iso][data-name]').forEach(function(el) {
                var iso = el.getAttribute('data-iso');
                var name = el.getAttribute('data-name');
                if (iso && name && !seen[iso]) {
                    seen[iso] = true;
                    opts.push({iso: iso, name: name});
                }
            });
            opts.sort(function(a, b) { return a.name.localeCompare(b.name); });
            var sel = document.getElementById('dqb_country');
            opts.forEach(function(c) {
                var o = document.createElement('option');
                o.value = c.iso;
                o.textContent = c.name;
                sel.appendChild(o);
            });
            // Flag preview inline with select
            var flag = document.getElementById('dqb_country_flag');
            function updateFlag() {
                var iso = sel.value;
                if (!iso) { flag.style.display = 'none'; return; }
                flag.className = 'fi fi-' + iso.toLowerCase();
                flag.style.display = 'inline-block';
            }
            sel.addEventListener('change', updateFlag);
        });
        </script>
    </auth:then>
</auth:if>

<%-- No results --%>
<c:if test="${empty columns}">
    <div class="alert">
        <c:if test="${!hasDestinationSearch}">
            No Destinations found matching these criteria.
        </c:if>
        <c:if test="${hasDestinationSearch}">
            <c:if test="${!empty getDestinationsError}">
                <strong>Error in your query:</strong> ${getDestinationsError}
            </c:if>
            <c:if test="${empty getDestinationsError}">
                No Destinations found. The default search is by name or email address.
            </c:if>
            <p class="mb-1 mt-2">You can conduct an extended search using the following rules:</p>
            <ul class="mb-0">
                <li><code>name=</code>, <code>comment=</code>, <code>country=</code>, <code>email=</code>, <code>enabled=yes/no</code>, <code>monitor=</code>, <code>backup=</code>, <code>forceproxy=</code>, <code>options=</code></li>
                <li>Example: <code>enabled=yes name=des0?_a* email=*@meteo.ms comment=*test* country=fr options=*mqtt* case=i</code></li>
                <li><code>case=i</code> for case-insensitive, <code>case=s</code> for case-sensitive (default)</li>
                <li>Enclose values with spaces or equals signs in double quotes, e.g. <code>"United States"</code></li>
                <li>Wildcards: <code>*</code> (zero or more chars), <code>?</code> (exactly one char)</li>
            </ul>
        </c:if>
    </div>
</c:if>

<%-- Results table --%>
<c:if test="${not empty columns}">
    <div class="d-flex align-items-center mb-2 gap-2">
        <span class="text-muted small"><i class="bi bi-list-ul"></i> <strong>${fn:length(destinations)}</strong> destination(s) found</span>
    </div>
    <table id="destinationsTable" class="table table-sm table-hover align-middle" style="width:100%">
        <thead class="table-light">
            <tr>
                <th style="width:28px;"></th>
                <th>Destination</th>
                <th style="width:110px;">Status</th>
                <c:if test="${fn:length(destinations) < 200}">
                    <th>Aliases</th>
                </c:if>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="d" items="${destinations}">
                <tr>
                    <td>
                        <span class="fi fi-${fn:toLowerCase(d.countryIso)}"
                             title="${d.country.name}"
                             data-iso="${fn:toLowerCase(d.countryIso)}" data-name="${d.country.name}"
                             style="font-size:1.1em;display:block"></span>
                    </td>
                    <td>
                        <span style="white-space:nowrap"><a href="/do/transfer/destination/${d.id}"
                           class="fw-semibold text-decoration-none dest-list-link"
                           >${d.id}</a><c:if test="${not empty d.typeText}"><c:choose
><c:when test="${d.typeText == 'Gold'}"><span class="dest-page-type dest-type-gold ms-1"><i class="bi bi-trophy-fill"></i> Gold</span
></c:when><c:when test="${d.typeText == 'Silver'}"><span class="dest-page-type dest-type-silver ms-1"><i class="bi bi-award-fill"></i> Silver</span
></c:when><c:when test="${d.typeText == 'Bronze'}"><span class="dest-page-type dest-type-bronze ms-1"><i class="bi bi-award"></i> Bronze</span
></c:when><c:when test="${d.typeText == 'Basic'}"><span class="dest-page-type dest-type-basic ms-1"><i class="bi bi-patch-check"></i> Basic</span
></c:when><c:otherwise><span class="dest-page-type ms-1">${d.typeText}</span
></c:otherwise></c:choose></c:if><c:if test="${not d.showInMonitors}">
                            <i class="bi bi-eye-slash text-muted ms-1" title="Not shown in Monitor Display" style="font-size:0.78rem;"></i>
                        </c:if><c:if test="${not empty d.filterName and d.filterName ne 'none'}">
                            <i class="bi bi-file-zip text-muted ms-1" title="Data compression enabled (${d.filterName})" style="font-size:0.78rem;"></i>
                        </c:if></span>
                        <c:if test="${not empty d.comment}">
                            <div class="text-muted" style="font-size:0.78rem; line-height:1.3; margin-top:1px;">${d.comment}</div>
                        </c:if>
                    </td>
                    <td>
                        <c:set var="statusBase" value="${fn:contains(d.formattedStatus, '-') ? fn:substringBefore(d.formattedStatus, '-') : d.formattedStatus}"/>
                        <c:choose>
                            <c:when test="${statusBase == 'Running'}">
                                <span class="badge bg-success" title="${d.formattedStatus}">${d.formattedStatus}</span>
                            </c:when>
                            <c:when test="${statusBase == 'Waiting' or statusBase == 'Retrying' or statusBase == 'Interrupted'}">
                                <span class="badge bg-warning text-dark" title="${d.formattedStatus}">${d.formattedStatus}</span>
                            </c:when>
                            <c:when test="${statusBase == 'Restarting' or statusBase == 'Resending'}">
                                <span class="badge bg-info text-dark" title="${d.formattedStatus}">${d.formattedStatus}</span>
                            </c:when>
                            <c:when test="${statusBase == 'Idle'}">
                                <span class="badge bg-secondary" title="${d.formattedStatus}">${d.formattedStatus}</span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge bg-danger" title="${d.formattedStatus}">${d.formattedStatus}</span>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <c:if test="${fn:length(destinations) < 200}">
                        <td>
                            <c:set var="destAliases" value="${d.aliases}"/>
                            <c:choose>
                                <c:when test="${fn:length(destAliases) == 0}">
                                    <span class="text-muted fst-italic" style="font-size:0.8rem;">none</span>
                                </c:when>
                                <c:when test="${fn:length(destAliases) < 3}">
                                    <c:forEach var="alias" items="${destAliases}">
                                        <a href="/do/transfer/destination/${alias.id}"
                                           class="badge bg-light text-secondary border text-decoration-none me-1"
                                           title="${alias.id} is an alias for ${d.id}">${alias.id}</a>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-light text-secondary border">${fn:length(destAliases)} aliases</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </c:if>
                </tr>
            </c:forEach>
        </tbody>
    </table>
    <script>
    $(function() {
        $('#destinationsTable').DataTable({
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
