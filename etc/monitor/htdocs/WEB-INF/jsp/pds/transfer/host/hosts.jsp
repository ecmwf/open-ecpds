<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
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
                    <div class="row g-2 mb-2">
                        <div class="col-7">
                            <div class="input-group">
                                <span class="input-group-text text-muted bg-white"><i class="bi bi-search"></i></span>
                                <input class="form-control" name="hostSearch" id="hostSearch" type="text"
                                    placeholder="e.g. enabled=yes method=*Http hostname=*.test.fr id&gt;=100 options=*mqtt* nickname=Test_0? case=i"
                                    title="Default search is by nickname. Use id, hostname, login, password, nickname, comment, dir, enabled, method, email and options rules."
                                    value='<c:out value="${hostSearch}"/>'>
                            </div>
                        </div>
                        <div class="col-3">
                            <div class="input-group">
                                <span class="input-group-text text-muted bg-white"><i class="bi bi-tag"></i></span>
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
                                    data-bs-toggle="collapse" data-bs-target="#hostQueryBuilder"
                                    title="Build query" aria-expanded="false" aria-controls="hostQueryBuilder">
                                <i class="bi bi-sliders2"></i>
                            </button>
                        </div>
                    </div>
                    <%-- Row 2: secondary filters --%>
                    <div class="row g-2">
                        <div class="col-4">
                            <div class="input-group input-group-sm">
                                <span class="input-group-text text-muted bg-white"><i class="bi bi-diagram-3"></i></span>
                                <select class="form-select form-select-sm" name="network" onchange="form.submit()" title="Filter by Network">
                                    <c:forEach var="option" items="${networkOptions}">
                                        <option value="${option.name}" <c:if test="${network == option.name}">selected</c:if>>${option.value}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="col-4">
                            <div class="input-group input-group-sm">
                                <span class="input-group-text text-muted bg-white"><i class="bi bi-bookmark"></i></span>
                                <select class="form-select form-select-sm" name="label" onchange="form.submit()" title="Filter by Label">
                                    <c:forEach var="option" items="${labelOptions}">
                                        <option value="${option.name}" <c:if test="${label == option.name}">selected</c:if>>${option.value}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="col-4">
                            <div class="input-group input-group-sm">
                                <span class="input-group-text text-muted bg-white"><i class="bi bi-file-zip"></i></span>
                                <select class="form-select form-select-sm" name="hostFilter" onchange="form.submit()" title="Filter by Compression">
                                    <c:forEach var="option" items="${filterOptions}">
                                        <option value="${option.name}" <c:if test="${hostFilter == option.name}">selected</c:if>>${option.value}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                    </div>

                    <%-- Query Builder collapse panel --%>
                    <div class="collapse mt-2" id="hostQueryBuilder">
                        <div class="border rounded p-2 bg-white" style="font-size:0.85rem">
                            <div class="row g-2 mb-2">
                                <div class="col-md-3">
                                    <label class="form-label mb-1 fw-semibold"><code>nickname=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="hqb_nickname" placeholder="e.g. Test_0?" oninput="hqbPreview()">
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label mb-1 fw-semibold"><code>hostname=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="hqb_hostname" placeholder="e.g. *.test.fr" oninput="hqbPreview()">
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label mb-1 fw-semibold"><code>method=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="hqb_method" placeholder="e.g. *Http" oninput="hqbPreview()">
                                </div>
                                <div class="col-md-2">
                                    <label class="form-label mb-1 fw-semibold"><code>id</code> <span class="text-muted fw-normal">numeric</span></label>
                                    <div class="input-group input-group-sm">
                                        <select class="form-select form-select-sm" id="hqb_id_op" style="max-width:65px" onchange="hqbPreview()">
                                            <option value="=">=</option><option value=">=">&gt;=</option><option value=">">&gt;</option><option value="<=">&lt;=</option><option value="<">&lt;</option>
                                        </select>
                                        <input type="number" class="form-control form-control-sm" id="hqb_id_val" placeholder="e.g. 100" oninput="hqbPreview()">
                                    </div>
                                </div>
                            </div>
                            <div class="row g-2 mb-2">
                                <div class="col-md-4">
                                    <label class="form-label mb-1 fw-semibold"><code>comment=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="hqb_comment" placeholder="e.g. *test*" oninput="hqbPreview()">
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label mb-1 fw-semibold"><code>email=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="hqb_email" placeholder="e.g. *@domain.com" oninput="hqbPreview()">
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label mb-1 fw-semibold"><code>options=</code> <span class="text-muted fw-normal">Properties &amp; JS wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="hqb_options" placeholder="e.g. *mqtt*" oninput="hqbPreview()">
                                </div>
                            </div>
                            <div class="row g-2 mb-2">
                                <div class="col-md-3">
                                    <label class="form-label mb-1 fw-semibold"><code>login=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="hqb_login" oninput="hqbPreview()">
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label mb-1 fw-semibold"><code>dir=</code> <span class="text-muted fw-normal">wildcards * ?</span></label>
                                    <input type="text" class="form-control form-control-sm" id="hqb_dir" oninput="hqbPreview()">
                                </div>
                                <div class="col-md-2">
                                    <label class="form-label mb-1 fw-semibold"><code>enabled</code></label>
                                    <select class="form-select form-select-sm" id="hqb_enabled" onchange="hqbPreview()">
                                        <option value="">Any</option><option value="yes">Yes</option><option value="no">No</option>
                                    </select>
                                </div>
                                <div class="col-md-2">
                                    <label class="form-label mb-1 fw-semibold"><code>case=</code></label>
                                    <select class="form-select form-select-sm" id="hqb_case" onchange="hqbPreview()">
                                        <option value="s">Sensitive (default)</option>
                                        <option value="i">Case-insensitive</option>
                                    </select>
                                </div>
                            </div>
                            <%-- Live preview + action buttons --%>
                            <div class="d-flex align-items-center gap-2 pt-1 border-top mt-1">
                                <i class="bi bi-terminal text-muted flex-shrink-0"></i>
                                <code class="text-muted flex-grow-1 text-truncate" id="hqb_preview" style="font-size:0.8rem">-- fill in fields above --</code>
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
    <display:table name="${hosts}" id="host"
        requestURI="" sort="external" defaultsort="1" partialList="true"
        size="${hostsSize}" pagesize="${recordsPerPage}" class="listing">
        <display:column sortable="true" title="Nickname">
            <a href="<bean:message key="host.basepath"/>/${host.name}">${host.nickName}</a>
        </display:column>
        <display:column title="Hostname/IP" property="host" sortable="true" />
        <display:column title="Associated Destinations" sortable="false">
            <c:if test="${fn:length(host.destinations) le 3}">
                <c:forEach var="destination" items="${host.destinations}">
                    <b><a href="<bean:message key="destination.basepath"/>/${destination.name}">${destination.name}</a></b>
                </c:forEach>
            </c:if>
            <c:if test="${fn:length(host.destinations) eq 0}">
                <span class="text-muted">[none]</span>
            </c:if>
            <c:if test="${fn:length(host.destinations) gt 3}">
                <span class="text-muted">[${fn:length(host.destinations)}-destinations]</span>
            </c:if>
        </display:column>
        <display:column property="type" title="Type" sortable="true" />
        <display:column title="Method" sortable="true">
            <a href="<bean:message key="method.basepath"/>/${host.transferMethodName}">${host.transferMethodName}</a>
        </display:column>
        <display:column property="transferGroupName" title="Network" sortable="true" />
        <display:column property="networkName" title="Label" sortable="true" />
        <display:column sortable="true" title="Enabled">
            <c:if test="${host.active}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:if>
            <c:if test="${!host.active}"><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:if>
        </display:column>
    </display:table>
</c:if>
