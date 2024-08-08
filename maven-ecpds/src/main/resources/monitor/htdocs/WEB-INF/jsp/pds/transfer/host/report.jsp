<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<c:if test="${empty proxy}">
	<h3>
		Network Information for <c:out value="${host.nickName}" />
	</h3>
</c:if>
<c:if test="${not empty proxy}">
	<h3>
		Network Information for
		<c:out value="${host.nickName}" />
		through
		<c:out value="${proxy.nickName}" />
	</h3>
</c:if>

<div class="info">
	<pre>${message}</pre>
</div>
