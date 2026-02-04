<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<c:set var="entityLabel" value="Data User" scope="request"/>
<c:set var="entityName" value="${incoming.id}" scope="request"/>

<div class="alert">
	<span class="closebtn" onclick="parent.history.back();">&times;</span>
	This operation will remove:
	<ul>
		<li>The Data User <c:out value="${incoming.id}" /></li>
	</ul>
	If you are completely sure this is what you want, click Process to
	proceed.
</div>
