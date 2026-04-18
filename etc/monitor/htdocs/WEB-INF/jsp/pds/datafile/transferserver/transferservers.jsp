<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<div class="d-flex align-items-center mb-2 gap-2">
    <span class="text-muted small"><i class="bi bi-list-ul"></i> <strong>${fn:length(transferservers)}</strong> transfer server(s)</span>
    <auth:link basePathKey="transferserver.basepath" href="/edit/insert_form"
               styleClass="btn btn-sm btn-outline-success ms-auto"><i class="bi bi-plus-circle"></i> Create</auth:link>
</div>

<c:if test="${empty transferservers}">
    <div class="alert">No Transfer Servers found.</div>
</c:if>

<c:if test="${not empty transferservers}">
    <table id="transferserversTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
        <thead class="table-light">
            <tr>
                <th>Name</th>
                <th>Host / Port</th>
                <th>Group</th>
                <th class="text-center">Enabled</th>
                <th class="text-center">Replicate</th>
                <th class="text-center">Check</th>
                <th>Max Transfers</th>
                <th>Last Update</th>
                <th class="text-end">Actions</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="server" items="${transferservers}">
                <tr>
                    <td class="fw-semibold">
                        <a href="/do/datafile/transferserver/${server.name}" class="text-decoration-none dest-list-link">${server.name}</a>
                    </td>
                    <td class="small text-muted" style="white-space:nowrap">
                        ${server.host}<c:if test="${server.port gt 0}">:<strong class="text-body">${server.port}</strong></c:if>
                    </td>
                    <td class="small">
                        <c:if test="${not empty server.transferGroupName}">
                            <a href="/do/datafile/transfergroup/${server.transferGroupName}"
                               class="badge bg-light text-secondary border text-decoration-none">${server.transferGroupName}</a>
                        </c:if>
                    </td>
                    <td class="text-center">
                        <c:choose>
                            <c:when test="${server.active}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:when>
                            <c:otherwise><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:otherwise>
                        </c:choose>
                    </td>
                    <td class="text-center">
                        <c:choose>
                            <c:when test="${server.replicate}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:when>
                            <c:otherwise><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:otherwise>
                        </c:choose>
                    </td>
                    <td class="text-center">
                        <c:choose>
                            <c:when test="${server.check}"><i class="bi bi-check-circle-fill text-success" title="Yes"></i></c:when>
                            <c:otherwise><i class="bi bi-x-circle-fill text-danger" title="No"></i></c:otherwise>
                        </c:choose>
                    </td>
                    <td class="small text-center">${server.maxTransfers}</td>
                    <td class="small text-muted" style="white-space:nowrap">
                        <c:if test="${not empty server.lastUpdateDate}">
                            <span title="${server.lastUpdateDate}">${server.lastUpdateDuration}</span>
                        </c:if>
                    </td>
                    <td class="text-end" style="white-space:nowrap">
                        <auth:link href="/do/datafile/transferserver/edit/update_form/${server.id}"
                                   imageKey="icon.small.update" styleClass="menuitem"/>
                        <auth:link href="/do/datafile/transferserver/edit/delete_form/${server.id}"
                                   imageKey="icon.small.delete" styleClass="menuitem"/>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
    <script>
    $(function() {
        $('#transferserversTable').DataTable({
            paging:    false,
            searching: false,
            order:     [[0, 'asc']],
            language: { info: 'Showing _START_-_END_ of _TOTAL_' }
        });
    });
    </script>
</c:if>
