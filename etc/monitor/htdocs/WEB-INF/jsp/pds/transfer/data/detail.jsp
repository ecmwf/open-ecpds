<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>

<tiles:insert page="./pds/transfer/data/data.jsp"/>

<auth:if basePathKey="datatransfer.basepath" paths="">
<auth:then>
<div class="mt-3">
<a href="<bean:message key="datatransfer.basepath"/>" class="btn btn-outline-primary">
    <i class="bi bi-arrow-left me-1"></i>All Data Transfers
</a>
</div>
</auth:then>
</auth:if>
