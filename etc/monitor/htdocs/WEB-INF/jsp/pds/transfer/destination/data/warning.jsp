<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:set var="buttonLabel" value="Permanently Delete" scope="request"/>
<c:set var="entityLabel" value="Destination" scope="request"/>
<c:set var="entityName" value="${destination.name}" scope="request"/>

<!-- Warning Box -->
<div class="alert">
  <span class="closebtn" onclick="parent.history.back();">&times;</span>
This operation will remove:
<ul>
<li>The Destination ${destination.name}</li>
<li>All its Associations to Hosts</li>
<li>All its Hosts (unless shared with at least one other Destination)</li>
<li>All its Data Transfers, including monitoring information. Transfers currently in progress will be stopped and removed.</li>
<li>All physical files from all Data Movers.</li>
</ul>
If you are completely sure this is what you want, click <span class="danger-action"><c:out value="${buttonLabel}"/></span> to proceed.
</div>
