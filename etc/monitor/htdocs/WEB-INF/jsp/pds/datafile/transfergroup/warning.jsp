<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:choose>
  <c:when test="${fn:length(transfergroup.transferServers) > 0}">

    <%-- Blocked: movers must be removed first --%>
    <c:set var="hideSubmit" value="true" scope="request"/>

    <div class="alert alert-warning d-flex gap-3 align-items-start mb-3">
      <i class="bi bi-exclamation-triangle-fill fs-4 flex-shrink-0 mt-1"></i>
      <div>
        <strong>Transfer Group cannot be deleted yet.</strong>
        <p class="mb-2 mt-1">
          This Transfer Group still has <strong>${fn:length(transfergroup.transferServers)}</strong>
          Data Mover<c:if test="${fn:length(transfergroup.transferServers) > 1}">s</c:if> assigned to it.
          You must delete all Data Movers belonging to this group before the group itself can be removed.
        </p>
        <p class="mb-2">
          Please delete the following Data Mover<c:if test="${fn:length(transfergroup.transferServers) > 1}">s</c:if> first:
        </p>
        <ul class="mb-0">
          <c:forEach var="server" items="${transfergroup.transferServers}">
            <li>
              <a href="/do/datafile/transferserver/${server.name}" class="fw-semibold">
                <i class="bi bi-server me-1"></i><c:out value="${server.name}"/>
              </a>
            </li>
          </c:forEach>
        </ul>
      </div>
    </div>

  </c:when>
  <c:otherwise>

    <%-- No movers — allow deletion --%>
    <c:set var="buttonLabel" value="Permanently Delete" scope="request"/>
    <c:set var="entityLabel" value="Transfer Group" scope="request"/>
    <c:set var="entityName" value="${transfergroup.name}" scope="request"/>

    <div class="alert alert-danger d-flex gap-3 align-items-start mb-3">
      <i class="bi bi-trash-fill fs-4 flex-shrink-0 mt-1"></i>
      <div>
        <strong>This action is permanent and cannot be undone.</strong>
        <p class="mb-2 mt-1">
          Deleting Transfer Group <strong><c:out value="${transfergroup.name}"/></strong> will:
        </p>
        <ul class="mb-2">
          <li>Remove the Transfer Group record permanently.</li>
          <li>
            Unassign any <strong>Hosts</strong> and <strong>Destinations</strong> currently linked to this
            group — they will fall back to the system default Transfer Group for new transfers.
          </li>
          <li>
            <strong class="text-danger">Any data files stored exclusively on this group's Data Movers
            and not retrievable from a Source Host will become permanently unavailable.</strong>
          </li>
        </ul>
        <p class="mb-0">
          If you are completely sure, click <span class="badge bg-danger">Permanently Delete</span> to proceed.
        </p>
      </div>
    </div>

  </c:otherwise>
</c:choose>
