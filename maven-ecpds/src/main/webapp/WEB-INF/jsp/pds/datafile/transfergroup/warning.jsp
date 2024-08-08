<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

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
	If you are completely sure this is what you want, click Process to
	proceed.
</div>
