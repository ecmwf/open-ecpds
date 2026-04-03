<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>

<table class="spareBox2">
<tr><th><a href="/do/user">Access Control</a></th></tr>
<tr><td></td></tr>

<auth:link basePathKey="user.basepath" href="" wrappingTags="tr,td"><i class="bi bi-people"></i> Web Users</auth:link>
<auth:link basePathKey="category.basepath" href="" wrappingTags="tr,td"><i class="bi bi-folder"></i> Web Categories</auth:link>
<auth:link basePathKey="resource.basepath" href="" wrappingTags="tr,td"><i class="bi bi-files"></i> Web Resources</auth:link>
<auth:link basePathKey="event.basepath" href="" wrappingTags="tr,td"><i class="bi bi-journal-text"></i> Web Event Log</auth:link>
<auth:link basePathKey="incoming.basepath" href="" wrappingTags="tr,td"><i class="bi bi-person-badge"></i> Data Users</auth:link>
<auth:link basePathKey="policy.basepath" href="" wrappingTags="tr,td"><i class="bi bi-shield-check"></i> Data Policies</auth:link>
<auth:link basePathKey="history.basepath" href="" wrappingTags="tr,td"><i class="bi bi-list-ul"></i> Data Event Log</auth:link>

</table>
