<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<c:set var="messages" value="${destinationDetailActionForm.messages}" />
<c:set var="message" value="${destinationDetailActionForm.message}" />

<c:if test="${not empty messages}">
<table id="messagesTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-light">
        <tr>
            <th>Error(s)</th>
        </tr>
    </thead>
    <tbody>
    <c:forEach var="errormessage" items="${messages}">
        <tr>
            <td>${errormessage}</td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<script>
$(document).ready(function() {
    $('#messagesTable').DataTable({
        paging:    false,
        searching: false,
        ordering:  false,
        info:      false
    });
});
</script>
</c:if>

<c:if test="${not empty message}">
    <h4>${message}</h4>
</c:if>
