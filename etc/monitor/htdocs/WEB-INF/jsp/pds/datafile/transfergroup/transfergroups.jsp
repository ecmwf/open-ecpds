<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<div class="d-flex align-items-center mb-2 gap-2">
    <span class="text-muted small"><i class="bi bi-list-ul"></i> <strong>${fn:length(transfergroups)}</strong> transfer group(s)</span>
    <auth:link basePathKey="transfergroup.basepath" href="/edit/insert_form"
               styleClass="btn btn-sm btn-outline-success ms-auto"><i class="bi bi-plus-circle"></i> Create</auth:link>
</div>

<c:if test="${empty transfergroups}">
    <div class="alert">No Transfer Groups found.</div>
</c:if>

<c:if test="${not empty transfergroups}">
    <table id="transfergroupsTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
        <thead class="table-light">
            <tr>
                <th>Name</th>
                <th>Comment</th>
                <th>Cluster</th>
                <th class="text-center">Enabled</th>
                <th class="text-center">Replicate</th>
                <th class="text-center">Filter</th>
                <th class="text-center">Backup</th>
                <th>Servers</th>
                <th class="text-end">Actions</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="row" items="${transfergroups}">
                <tr>
                    <td class="fw-semibold">
                        <a href="/do/datafile/transfergroup/${row.id}" class="text-decoration-none dest-list-link">${row.name}</a>
                    </td>
                    <td class="text-muted small">${row.comment}</td>
                    <td class="small">
                        <c:if test="${not empty row.clusterName}">
                            <span class="badge bg-light text-secondary border me-1">${row.clusterName}</span>
                            <span class="text-muted" style="font-size:0.78rem;">w=${row.clusterWeight}</span>
                        </c:if>
                    </td>
                    <td class="text-center">
                        <c:choose>
                            <c:when test="${row.active}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:when>
                            <c:otherwise><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:otherwise>
                        </c:choose>
                    </td>
                    <td class="text-center">
                        <c:choose>
                            <c:when test="${row.replicate}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:when>
                            <c:otherwise><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:otherwise>
                        </c:choose>
                    </td>
                    <td class="text-center">
                        <c:choose>
                            <c:when test="${row.filter}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:when>
                            <c:otherwise><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:otherwise>
                        </c:choose>
                    </td>
                    <td class="text-center">
                        <c:choose>
                            <c:when test="${row.backup}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:when>
                            <c:otherwise><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:otherwise>
                        </c:choose>
                    </td>
                    <td class="small">
                        <c:set var="_servers" value="${row.transferServers}"/>
                        <c:choose>
                            <c:when test="${empty _servers}"><span class="text-muted">none</span></c:when>
                            <c:otherwise>
                                <c:forEach var="srv" items="${_servers}">
                                    <a href="/do/datafile/transferserver/${srv.name}"
                                       class="badge bg-light text-secondary border text-decoration-none me-1">${srv.name}</a>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td class="text-end" style="white-space:nowrap">
                        <auth:link basePathKey="transfergroup.basepath" href="/edit/update_form/${row.id}"
                                   imageKey="icon.small.update" styleClass="menuitem"/>
                        <auth:link basePathKey="transfergroup.basepath" href="/edit/delete_form/${row.id}"
                                   imageKey="icon.small.delete" styleClass="menuitem"/>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
    <script>
    $(function() {
        $('#transfergroupsTable').DataTable({
            paging:    false,
            searching: false,
            order:     [[0, 'asc']],
            language: { info: 'Showing _START_-_END_ of _TOTAL_' }
        });
    });
    </script>
</c:if>


