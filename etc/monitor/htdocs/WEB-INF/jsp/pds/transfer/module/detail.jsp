<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>

<tiles:insert page="/WEB-INF/jsp/pds/transfer/module/data.jsp"/>

<auth:if basePathKey="module.basepath" paths="">
<auth:then>
<div class="mt-3">
<a href="<bean:message key="module.basepath"/>" class="btn btn-outline-primary">
    <i class="bi bi-arrow-left"></i> All Transfer Modules
</a>
</div>
</auth:then>
</auth:if>
