<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<p class="fw-bold mb-2 mt-2">
    <c:choose>
        <c:when test="${not empty attributeName}">All Meta Data Values for parameter <i>${attributeName}</i></c:when>
        <c:otherwise>All Meta Data Parameters</c:otherwise>
    </c:choose>
</p>

<c:if test="${empty attributes}">
    <div class="alert">No Meta Data found.</div>
</c:if>

<c:if test="${not empty attributes}">
    <div class="d-flex align-items-center mb-2 gap-2">
        <span class="text-muted small"><i class="bi bi-list-ul"></i> <strong>${fn:length(attributes)}</strong> result(s)</span>
    </div>
    <table id="metadatasTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
        <thead class="table-light">
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
    <script>
    $(function() {
        $('#metadatasTable').DataTable({
            paging:    true,
            pageLength: 50,
            searching: true,
            order:     [[0, 'asc']],
            language: { lengthMenu: 'Show _MENU_ per page', info: 'Showing _START_-_END_ of _TOTAL_',
                        search: '<i class="bi bi-search"></i>', searchPlaceholder: 'Filter...' }
        });
    });
    </script>
</c:if>
