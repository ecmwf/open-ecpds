<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<c:set var="entityLabel" value="Transfer Module" scope="request"/>
<c:set var="entityName" value="${module.name}" scope="request"/>

<div class="alert">
	<span class="closebtn" onclick="parent.history.back();">&times;</span>
	This operation will remove:
	<ul>
		<li>The Transfer Module <c:out value="${module.name}" /></li>
	</ul>
	If you are completely sure this is what you want, click Process to
	proceed.
</div>
