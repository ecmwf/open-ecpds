<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %> 

<table class="spareBox2">
<tr><th><a href="/do/transfer">Transmission</a></th></tr>
<tr><td></td></tr>
	<auth:link basePathKey="datatransfer.basepath" href="" wrappingTags="tr,td">Data Transfers</auth:link>
	<auth:link basePathKey="destination.basepath" href="" wrappingTags="tr,td">Destinations</auth:link>
	<auth:link basePathKey="host.basepath" href="" wrappingTags="tr,td">Transfer Hosts</auth:link>
	<auth:link basePathKey="transferhistory.basepath" href="" wrappingTags="tr,td">Transfer History</auth:link>
	<auth:link basePathKey="method.basepath" href="" wrappingTags="tr,td">Transfer Methods</auth:link>
	<auth:link basePathKey="module.basepath" href="" wrappingTags="tr,td">Transfer Modules</auth:link>
</table>
