<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<tiles:insert name="date.select"/>

<c:if test="${empty events}">
<br/>
<div class="alert">
  No Data Events found based on these criteria!
</div>
</c:if>

<c:if test="${!empty events}">
<p class="fw-bold mb-1 mt-2">Events for <auth:link basePathKey="user.basepath" href="/${user.id}">Web User ${user.uid}</auth:link> on ${selectedDate}</p>
<table id="userEventsTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-light">
        <tr>
            <th>Time</th>
            <th>Host</th>
            <th>Action</th>
            <th>Comment</th>
            <th></th>
        </tr>
    </thead>
    <tbody>
    <c:forEach var="event" items="${events}">
        <tr>
            <td>${event.time}</td>
            <td>${event.activity.host}</td>
            <td>${event.action}</td>
            <td>${event.comment}</td>
            <td>
                <c:if test="${event.type != '' and event.type != 'lost'}">
                    <c:set var="eventBasepath" value=""/>
                    <c:catch><c:set var="eventBasepath"><bean:message key="${event.type}.basepath"/></c:set></c:catch>
                    <c:if test="${not empty eventBasepath}">
                        <a href="${eventBasepath}/${event.linkId}"><content:icon altKey="ecpds.user.event.object" titleKey="ecpds.user.event.object" key="icon.small.arrow.right" writeFullTag="true"/></a>
                    </c:if>
                </c:if>
                <c:if test="${event.type == 'lost'}">
                    <content:icon altKey="ecpds.user.event.noObject" titleKey="ecpds.user.event.noObject" key="icon.small.square" writeFullTag="true"/>
                </c:if>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<script>
$(document).ready(function() {
    $('#userEventsTable').DataTable({
        paging:    true,
        pageLength: 25,
        searching: true,
        ordering:  true,
        info:      true,
        columnDefs: [{ orderable: false, targets: -1 }]
    });
});
</script>
</c:if>
