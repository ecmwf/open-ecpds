<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>

<select id="selectdestination" name=""
	onchange="javascript:location.href = this.value;">
	<c:forEach var="destinationOption" items="${destinationOptions}"
		varStatus="stat">
		<c:if test="${destinationOption.name == selectedDestination.name}">
			<option
				value="?mode=${param['mode']}&destinationName=${destinationOption.name}"
				selected>${destinationOption.name}</option>
		</c:if>
		<c:if test="${destinationOption.name != selectedDestination.name}">
			<option
				value="?mode=${param['mode']}&destinationName=${destinationOption.name}">${destinationOption.name}</option>
		</c:if>
	</c:forEach>
</select>
