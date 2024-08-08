<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<c:set var="messages" value="${destinationDetailActionForm.messages}" />
<c:set var="message" value="${destinationDetailActionForm.message}" />

<c:if test="${not empty messages}">
	<display:table id="errormessage" name="${messages}" requestURI=""
		class="listing">
		<display:column title="Error(s)">${errormessage}</display:column>
	</display:table>
</c:if>

<c:if test="${not empty message}">
	<h4>${message}</h4>
</c:if>
