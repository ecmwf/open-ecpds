<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>

<tiles:insert page="/WEB-INF/jsp/pds/transfer/destination/data.jsp"/>

<auth:if basePathKey="destination.basepath" paths="">
<auth:then>
<div class="pt-2">
<a href="<bean:message key="destination.basepath"/>" class="btn btn-sm btn-outline-secondary">
    <i class="bi bi-arrow-left"></i> All Destinations
</a>
</div>
</auth:then>
</auth:if>
