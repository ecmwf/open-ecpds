<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<c:set var="buttonLabel" value="Permanently Delete" scope="request"/>
<c:set var="entityLabel" value="Web Category" scope="request"/>
<c:set var="entityName" value="${category.name}" scope="request"/>

<div class="alert">
	<span class="closebtn" onclick="parent.history.back();">&times;</span>
	This operation will remove:
	<ul>
		<li>The Web Category <c:out value="${category.name}" /></li>
	</ul>
	If you are completely sure this is what you want, click <span class="danger-action"><c:out value="${buttonLabel}"/></span> to proceed.
</div>
