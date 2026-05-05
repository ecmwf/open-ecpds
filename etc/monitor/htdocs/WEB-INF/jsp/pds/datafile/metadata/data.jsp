<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<div class="form-info-banner" style="margin-left:0">
	<i class="bi bi-tags text-primary flex-shrink-0"></i>
	Metadata: <strong><c:out value="${metadata.id}"/></strong>
</div>

<table class="fields">
<tr><th>Id</th><td>${metadata.id}</td></tr>
<tr><th>Name</th><td>${metadata.name}</td></tr>
<tr><th>Comment</th><td>${metadata.comment}</td></tr>
</table>
