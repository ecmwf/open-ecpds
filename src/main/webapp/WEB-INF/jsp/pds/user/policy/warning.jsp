<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<div class="alert">
	<span class="closebtn" onclick="parent.history.back();">&times;</span>
	This operation will remove:
	<ul>
		<li>The Data Policy <c:out value="${policy.id}" /></li>
	</ul>
	If you are completely sure this is what you want, click Process to
	proceed.
</div>
