<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>

<select id="selectdestination" name="" onchange="location.href=this.value;">
    <c:forEach var="destinationOption" items="${destinationOptions}">
        <option value="?mode=${param['mode']}&destinationName=${destinationOption.name}"
            <c:if test="${destinationOption.name eq selectedDestination.name}">selected</c:if>>
            ${destinationOption.name}
        </option>
    </c:forEach>
</select>
