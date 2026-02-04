<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<c:set var="entityLabel" value="Web User" scope="request"/>
<c:set var="entityName" value="${user.uid}" scope="request"/>

<div class="alert">
	<span class="closebtn" onclick="parent.history.back();">&times;</span>
	This operation will remove:
	<ul>
		<li>The Web User <c:out value="${user.uid}" /></li>
	</ul>
	If you are completely sure this is what you want, click Process to
	proceed.
</div>
