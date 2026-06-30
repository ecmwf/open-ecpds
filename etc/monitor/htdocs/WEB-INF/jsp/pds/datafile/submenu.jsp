<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>

<table class="spareBox2">
<tr><th><a href="/do/datafile">Data Storage</a> </th></tr>
<tr><td style="padding:1px 32px 1px 22px;"><hr style="margin:1px 0;opacity:0.15;border-top:1px solid currentColor;"/></td></tr>
   <auth:link basePathKey="datafile.basepath" href="" wrappingTags="tr,td"><i class="bi bi-file-earmark-text"></i> Data Files</auth:link>
   <auth:link basePathKey="metadata.basepath" href="" wrappingTags="tr,td"><i class="bi bi-tags"></i> Meta Data</auth:link>
   <auth:link basePathKey="transfergroup.basepath" href="" wrappingTags="tr,td"><i class="bi bi-collection"></i> Transfer Groups</auth:link>
   <auth:link basePathKey="transferserver.basepath" href="" wrappingTags="tr,td"><i class="bi bi-server"></i> Data Movers</auth:link>
   <auth:link basePathKey="retrievalmonitoring.basepath" href="" wrappingTags="tr,td"><i class="bi bi-speedometer2"></i> Retrieval Rates</auth:link>
   <auth:link basePathKey="moverdownloads.basepath" href="" wrappingTags="tr,td"><i class="bi bi-grid-3x3"></i> Download Activity</auth:link>
   <auth:link basePathKey="datarates.basepath" href="" wrappingTags="tr,td"><i class="bi bi-bar-chart-line"></i> Data Rates</auth:link>
   <auth:link basePathKey="portaltraffic.basepath" href="" wrappingTags="tr,td"><i class="bi bi-graph-up-arrow"></i> Portal Traffic</auth:link>
</table>
