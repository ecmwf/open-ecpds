<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:insert name="subcontent" />

<c:if test="${not empty requeuedSize}">
	<h4>${action} ${requeuedSize} Data Transfer(s).</h4>
	<br />
</c:if>
