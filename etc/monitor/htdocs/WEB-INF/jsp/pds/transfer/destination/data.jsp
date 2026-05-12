<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/fn.tld" prefix="fn"%>

<auth:if basePathKey="destination.basepath"
	paths="/operations/${destinationDetailActionForm.id}/requeue/">
	<auth:then>
		<c:set var="ecpdsCanHandleQueue" value="true" scope="request" />
		<c:set var="numberOfColumns" value="9" scope="request" />
	</auth:then>
	<auth:else>
		<c:set var="numberOfColumns" value="8" scope="request" />
	</auth:else>
</auth:if>

<tiles:insert page="./pds/transfer/destination/data/javascript.jsp" />

<tiles:importAttribute name="isDelete" ignore="true" />
<c:if test="${empty isDelete}">
	<jsp:include page="/WEB-INF/jsp/pds/transfer/destination/destination_header.jsp"/>
</c:if>

<c:choose>
<c:when test="${not empty isDelete}">
	<tiles:insert page="./pds/transfer/destination/data/warning.jsp" />
</c:when>
<c:otherwise>
<form name="destinationDetailActionForm" method="GET"
	action="<bean:message key="destination.basepath"/>/${destinationDetailActionForm.id}">

	<html:form action="/transfer/destination/*">
		<html:hidden property="status" />
		<html:hidden property="dataStream" />
		<html:hidden property="dataTime" />
		<html:hidden property="disseminationStream" />
		<html:hidden property="date" />

		<c:forEach var="param"
			items="${destinationDetailActionForm.displayTagsParamCollection}">
			<input type="hidden" name="${param.name}" value="${param.value}" />
		</c:forEach>

<tiles:insert page="./pds/transfer/destination/data/info_table_1.jsp" /><tiles:insert page="./pds/transfer/destination/data/info_table_2.jsp" />
<tiles:insert page="./pds/transfer/destination/data/messages.jsp" />
<tiles:insert page="./pds/transfer/destination/data/filter_table.jsp" />
<table border=0 width="100%">
<tr><td valign="top" colspan="3"><tiles:insert page="./pds/transfer/destination/data/transfer_table.jsp" /></td></tr>
</table>
<tiles:insert page="./pds/transfer/destination/data/hosts_and_users_tr.jsp" />

		<script>
			setAll();
		</script>

	</html:form>
</form>
</c:otherwise>
</c:choose>