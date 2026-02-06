<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:set var="buttonLabel" value="Permanently Delete" scope="request"/>
<c:set var="entityLabel" value="Transfer Server" scope="request"/>
<c:set var="entityName" value="${transferserver.name}" scope="request"/>

<!-- Warning Box -->
<div class="alert">
	<span class="closebtn" onclick="parent.history.back();">&times;</span>
	This operation will remove:
	<ul>
		<li>The Transfer Server ${transferserver.name}</li>
		<li>Affected Transfer Group: <b>${transferserver.transferGroup.name}</b></li>
	</ul>
	If you are completely sure this is what you want, click <span class="danger-action"><c:out value="${buttonLabel}"/></span> to proceed.
</div>
