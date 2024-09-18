<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<c:if test="${metaDataSize == 0}">
	<br />
	<div class="alert">
		No Metadata available for Destination
		<c:out value="${destination.name}" />
	</div>
</c:if>

<c:if test="${metaDataSize != 0}">
	<table class="listing">
		<tr>
			<td><a
				href="<bean:message key="destination.basepath"/>?destinationSearch=country=${destination.country.iso}&destinationStatus=&destinationType="><img
					src="/assets/images/flags/small/<c:out value="${destination.countryIso}"/>.png"
					alt="Flag for ${destination.country.name}"
					title="See all destinations in ${destination.country.name}" /></a>&nbsp;${destination.country.name}</td>
			<td>${destination.ecUser.comment}</td>
			<td>${metaDataSize} metadatafile(s)</td>
		</tr>
	</table>
	<c:forEach var="file" items="${metaData}">
		<h3>${file.name}(${file.contentType}).LastModification:
			${file.lastModificationDate}</h3>
		<c:if test="${file.contentType == 'text/plain'}">
${file.stringContent}
</c:if>
		<c:if test="${file.contentType != 'text/plain'}">
			<content:content name="file" outputContentType="text/html"
				thumbnail="false" embedded="true" />
		</c:if>
	</c:forEach>
	<br />
</c:if>
