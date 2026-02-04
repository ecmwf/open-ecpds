<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<c:set var="entityLabel" value="Host" scope="request"/>
<c:set var="entityName" value="${host.nickName}" scope="request"/>

<div class="alert">
	<span class="closebtn" onclick="parent.history.back();">&times;</span>
	This operation will remove:
	<ul>
		<li>The Host <c:out value="${host.nickName}" /></li>
		<c:if test="${fn:length(host.destinations) > 0}">
			<li>Affected Destinations: <c:forEach var="destination"
					items="${host.destinations}">
					<b><a title="${destination.comment}">${destination.name}</a></b>&nbsp;
			</c:forEach>
			</li>
		</c:if>
	</ul>
	If you are completely sure this is what you want, click Process to
	proceed.
</div>
