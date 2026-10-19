<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>

<tiles:insert page="/WEB-INF/jsp/pds/datafile/datafile/data.jsp"/>
<jsp:include page="/WEB-INF/jsp/pds/datafile/datafile/lifecycle.jsp"/>
<auth:if paths="/do/admin/storagepolicies">
<auth:then>
<a href="/do/admin/storagepolicies?fileId=${datafile.id}" class="btn btn-sm btn-outline-secondary">
    <i class="bi bi-pin-angle me-1"></i>Manage storage hold</a>
</auth:then>
</auth:if>

<auth:if basePathKey="datafile.basepath" paths="">
<auth:then>
<div class="mt-3">
<a href="<bean:message key="datafile.basepath"/>" class="btn btn-outline-primary">
    <i class="bi bi-arrow-left me-1"></i>All Data Files
</a>
</div>
</auth:then>
</auth:if>
