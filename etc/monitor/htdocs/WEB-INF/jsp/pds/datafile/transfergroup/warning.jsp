<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:set var="buttonLabel" value="Permanently Delete" scope="request"/>
<c:set var="entityLabel" value="Transfer Group" scope="request"/>
<c:set var="entityName" value="${transfergroup.name}" scope="request"/>

<!-- Warning Box -->
<div class="alert">
	<span class="closebtn" onclick="parent.history.back();">&times;</span>
	This operation will remove:
	<ul>
		<li>The Transfer Group ${transfergroup.name}</li>
		<c:if test="${fn:length(transfergroup.transferServers) > 0}">
			<li>Affected Transfer Servers: <c:forEach var="server"
					items="${transfergroup.transferServers}">
					<b>${server.name}</b>&nbsp;
				</c:forEach>
			</li>
		</c:if>
	</ul>
	If you are completely sure this is what you want, click <span class="danger-action"><c:out value="${buttonLabel}"/></span> to proceed.
</div>
