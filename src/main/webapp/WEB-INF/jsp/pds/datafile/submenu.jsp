<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>

<table class="spareBox2">
<tr><th><a href="/do/datafile">Data Storage</a> </th></tr>
<tr><td></td></tr>
   <auth:link basePathKey="datafile.basepath" href="" wrappingTags="tr,td">Data Files</auth:link>
   <auth:link basePathKey="metadata.basepath" href="" wrappingTags="tr,td">Meta Datas</auth:link>
   <auth:link basePathKey="transfergroup.basepath" href="" wrappingTags="tr,td">Transfer Groups</auth:link>
   <auth:link basePathKey="transferserver.basepath" href="" wrappingTags="tr,td">Transfer Servers</auth:link>
   <auth:link basePathKey="retrievalmonitoring.basepath" href="" wrappingTags="tr,td">Retrieval Rates</auth:link>
</table>
