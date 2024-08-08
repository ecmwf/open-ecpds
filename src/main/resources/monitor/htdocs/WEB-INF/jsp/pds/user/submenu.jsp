<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>

<table class="spareBox2">
<tr><th><a href="/do/user">Access Control</a></th></tr>
<tr><td></td></tr>

<auth:link basePathKey="user.basepath" href="" wrappingTags="tr,td">Web Users</auth:link>
<auth:link basePathKey="category.basepath" href="" wrappingTags="tr,td">Web Categories</auth:link>
<auth:link basePathKey="resource.basepath" href="" wrappingTags="tr,td">Web Resources</auth:link>
<auth:link basePathKey="event.basepath" href="" wrappingTags="tr,td">Web Event Log</auth:link>
<auth:link basePathKey="incoming.basepath" href="" wrappingTags="tr,td">Data Users</auth:link>
<auth:link basePathKey="policy.basepath" href="" wrappingTags="tr,td">Data Policies</auth:link>
<auth:link basePathKey="history.basepath" href="" wrappingTags="tr,td">Data Event Log</auth:link>

</table>
