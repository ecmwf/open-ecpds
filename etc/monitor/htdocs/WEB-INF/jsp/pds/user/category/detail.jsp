<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>

<tiles:insert page="/WEB-INF/jsp/pds/user/category/data.jsp"/>
<div class="mt-3">
<a href="/do/user/category" class="btn btn-sm btn-outline-secondary">
    <i class="bi bi-arrow-left"></i> All Web Categories
</a>
</div>
