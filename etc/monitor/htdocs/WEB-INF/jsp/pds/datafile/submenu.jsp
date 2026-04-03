<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>

<table class="spareBox2">
<tr><th><a href="/do/datafile">Data Storage</a> </th></tr>
<tr><td></td></tr>
   <auth:link basePathKey="datafile.basepath" href="" wrappingTags="tr,td"><i class="bi bi-file-earmark-text"></i> Data Files</auth:link>
   <auth:link basePathKey="metadata.basepath" href="" wrappingTags="tr,td"><i class="bi bi-tags"></i> Meta Data</auth:link>
   <auth:link basePathKey="transfergroup.basepath" href="" wrappingTags="tr,td"><i class="bi bi-collection"></i> Transfer Groups</auth:link>
   <auth:link basePathKey="transferserver.basepath" href="" wrappingTags="tr,td"><i class="bi bi-server"></i> Transfer Servers</auth:link>
   <auth:link basePathKey="retrievalmonitoring.basepath" href="" wrappingTags="tr,td"><i class="bi bi-speedometer2"></i> Retrieval Rates</auth:link>
</table>
