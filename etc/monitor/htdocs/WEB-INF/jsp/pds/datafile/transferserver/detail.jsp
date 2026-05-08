<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>

<tiles:insert page="/WEB-INF/jsp/pds/datafile/transferserver/data.jsp"/>
<div class="mt-2">
<a href="/do/datafile/transferserver" class="btn btn-sm btn-outline-secondary">
    <i class="bi bi-arrow-left"></i> All Data Movers
</a>
</div>

