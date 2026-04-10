<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:if test="${not empty destination.countryIso}"><span class="fi fi-${fn:toLowerCase(destination.countryIso)}" title="${destination.country.name}" style="align-self:center;font-size:1.2em;vertical-align:middle;border-radius:2px;"></span></c:if>
