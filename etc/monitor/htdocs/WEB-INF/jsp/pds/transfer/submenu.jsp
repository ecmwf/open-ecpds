<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %> 

<table class="spareBox2">
<tr><th><a href="/do/transfer">Transmission</a></th></tr>
<tr><td></td></tr>
	<auth:link basePathKey="datatransfer.basepath" href="" wrappingTags="tr,td"><i class="bi bi-arrow-left-right"></i> Data Transfers</auth:link>
	<auth:link basePathKey="destination.basepath" href="" wrappingTags="tr,td"><i class="bi bi-geo-alt"></i> Destinations</auth:link>
	<auth:link basePathKey="host.basepath" href="" wrappingTags="tr,td"><i class="bi bi-pc-display"></i> Transfer Hosts</auth:link>
	<auth:link basePathKey="transferhistory.basepath" href="" wrappingTags="tr,td"><i class="bi bi-clock-history"></i> Transfer History</auth:link>
	<auth:link basePathKey="method.basepath" href="" wrappingTags="tr,td"><i class="bi bi-diagram-3"></i> Transfer Methods</auth:link>
	<auth:link basePathKey="module.basepath" href="" wrappingTags="tr,td"><i class="bi bi-puzzle"></i> Transfer Modules</auth:link>
</table>
