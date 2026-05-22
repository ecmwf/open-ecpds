<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<tiles:insert page="/WEB-INF/jsp/pds/user/user/data.jsp"/>
<div class="mt-3 d-flex gap-2 flex-wrap">
<a href="/do/user/user" class="btn btn-outline-primary">
    <i class="bi bi-arrow-left me-1"></i>All Web Users
</a>
<auth:link basePathKey="event.basepath" href="/${user.id}" styleClass="btn btn-outline-primary">Web Event Logs for ${user.id}</auth:link>
</div>
