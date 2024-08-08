<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>

<table class="select">
	<tr>
		<c:forEach items="${metaDataNameOptions}" var="metaDataNameOption"
			varStatus="stat">
			<c:if test="${metaDataNameOption.name == selectedMetaDataName}">
				<td width="100" class="selected"><a
					href="?metaDataName=${metaDataNameOption.name}">${metaDataNameOption.name}</a></td>
			</c:if>
			<c:if test="${metaDataNameOption.name != selectedMetaDataName}">
				<td width="100"><a
					href="?metaDataName=${metaDataNameOption.name}">${metaDataNameOption.name}</a></td>
			</c:if>
		</c:forEach>
	</tr>
</table>

<table class="select">
	<tr>
		<c:forEach items="${metaDataValueOptions}" var="metaDataValueOption"
			varStatus="stat">
			<c:if test="${metaDataValueOption.value == selectedMetaDataValue}">
				<td width="100" class="selected"><a
					href="?metaDataValue=${metaDataValueOption.value}">${metaDataValueOption.value}</a></td>
			</c:if>
			<c:if test="${metaDataValueOption.value != selectedMetaDataValue}">
				<td width="100"><a
					href="?metaDataValue=${metaDataValueOption.value}">${metaDataValueOption.value}</a></td>
			</c:if>
			<c:if test="${stat.count % 20 == 0}">
	</tr>
	<tr>
		</c:if>
		</c:forEach>
	</tr>
</table>
