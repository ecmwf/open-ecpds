<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:set var="buttonLabel" value="Permanently Delete" scope="request"/>
<c:set var="entityLabel" value="Data File" scope="request"/>
<c:set var="entityName" value="${datafile.id}" scope="request"/>

<!-- Warning Box -->
<div class="alert">
	<span class="closebtn" onclick="parent.history.back();">&times;</span>
	This operation will remove:
	<ul>
		<li>The Data File ${datafile.id} will be set to Deleted</li>
		<li>All related Data Transfers will be set to Deleted across all Destinations</li>
		<li>All physical files will be removed from all Data Movers</li>
	</ul>
	If you are completely sure this is what you want, click <span class="danger-action"><c:out value="${buttonLabel}"/></span> to proceed.
</div>
