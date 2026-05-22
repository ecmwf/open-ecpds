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
<div class="card border-0 shadow-sm mt-3">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-clock-history text-primary"></i>
    <span class="fw-semibold">Events for <auth:link basePathKey="user.basepath" href="/${user.id}">Web User ${user.uid}</auth:link></span>
    <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
        <div class="input-group input-group-sm" style="width:auto">
            <span class="input-group-text"><i class="bi bi-search"></i></span>
            <input type="text" id="userEventsSearch" class="form-control" placeholder="Search events..." style="min-width:180px">
        </div>
        <div class="input-group input-group-sm flex-nowrap" style="width:auto" title="Page size">
            <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
            <select id="userEventsPageLen" class="form-select form-select-sm" style="width:auto">
                <option value="10">10</option><option value="25">25</option><option value="50">50</option><option value="100">100</option><option value="250">250</option>
            </select>
        </div>
    </div>
</div>
<div class="card-body p-0">
<div class="table-responsive">
<table id="userEventsTable" class="table table-sm table-hover table-striped align-middle" style="width:100%">
    <thead class="table-light">
        <tr>
            <th title="Time (UTC)">Time</th>
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
</div>
</div>
</div>
<script>
$(document).ready(function() {
    var table = $('#userEventsTable').DataTable({
        paging:    true,
        pageLength: (function() { try { var v = parseInt(localStorage.getItem('userEventsPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })(),
        searching: true,
        ordering:  true,
        info:      true,
        columnDefs: [{ orderable: false, targets: -1 }],
        dom: 't<"d-flex align-items-start mt-2 px-3 pb-2"i<"ms-auto"p>>'
    });
    var _savedPageLen = (function() { try { var v = parseInt(localStorage.getItem('userEventsPageLen'), 10); return [10,25,50,100,250].indexOf(v) >= 0 ? v : 25; } catch(e) { return 25; } })();
    table.page.len(_savedPageLen).draw(false);
    $('#userEventsPageLen').val(_savedPageLen);
    $('#userEventsPageLen').on('change', function() {
        var len = +this.value;
        try { localStorage.setItem('userEventsPageLen', len); } catch(e) {}
        table.page.len(len).draw();
    });
    $('#userEventsSearch').on('keyup', function() { table.search(this.value).draw(); });
});
</script>
</c:if>
