<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:if test="${empty attributes}">
    <div class="alert">No Meta Data found.</div>
</c:if>

<c:if test="${not empty attributes}">
    <div class="card border-0 shadow-sm mt-3">
    <div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
        <i class="bi bi-database text-primary"></i>
        <span class="fw-semibold">
            <c:choose>
                <c:when test="${not empty attributeName}">All Meta Data Values for parameter <i>${attributeName}</i></c:when>
                <c:otherwise>All Meta Data Parameters</c:otherwise>
            </c:choose>
        </span>
        <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
            <div class="input-group input-group-sm" style="width:auto">
                <span class="input-group-text"><i class="bi bi-search"></i></span>
                <input type="text" id="metaAttrSearch" class="form-control" placeholder="Filter..." style="min-width:160px">
            </div>
            <div class="input-group input-group-sm flex-nowrap" style="width:auto" title="Page size">
                <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
                <select id="metaAttrPageLen" class="form-select form-select-sm" style="width:auto">
                    <option value="10">10</option>
                    <option value="25">25</option>
                    <option value="50">50</option>
                    <option value="100">100</option>
                    <option value="250">250</option>
                </select>
            </div>
        </div>
    </div>
    <div class="card-body p-0">
    <div class="table-responsive">
    <table id="metadatasTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
        <thead class="table-primary">
            <tr>
                <th>Name</th>
                <c:if test="${not empty attributeName}">
                    <th>Value</th>
                </c:if>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="row" items="${attributes}">
                <tr>
                    <td>
                        <a href="/do/datafile/metadata/attribute/${row.name}"
                           class="text-decoration-none dest-list-link">${row.name}</a>
                    </td>
                    <c:if test="${not empty attributeName}">
                        <td class="text-muted small">${row.value}</td>
                    </c:if>
                </tr>
            </c:forEach>
        </tbody>
    </table>
    </div>
    </div>
    </div>
    <script>
    $(function() {
        var table = $('#metadatasTable').DataTable({
            paging:    true,
            pageLength: (function() { try { var v = parseInt(localStorage.getItem('metaAttrPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 50; } catch(e) { return 50; } })(),
            searching: true,
            order:     [[0, 'asc']],
            dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>',
            language: { info: 'Showing _START_-_END_ of _TOTAL_' }
        });
        $('#metaAttrSearch').on('keyup', function() { table.search(this.value).draw(); });
        var _savedPageLen = (function() { try { var v = parseInt(localStorage.getItem('metaAttrPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 50; } catch(e) { return 50; } })();
        $('#metaAttrPageLen').val(_savedPageLen);
        $('#metaAttrPageLen').on('change', function() {
            var len = +this.value;
            try { localStorage.setItem('metaAttrPageLen', len); } catch(e) {}
            table.page.len(len).draw();
        });
    });
    </script>
</c:if>
