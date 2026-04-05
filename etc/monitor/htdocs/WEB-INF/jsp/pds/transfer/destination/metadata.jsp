<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<script>window._validIso=new Set(["AC","AD","AE","AF","AG","AI","AL","AM","AO","AQ","AR","AS","AT","AU","AW","AX","AZ","BA","BB","BD","BE","BF","BG","BH","BI","BJ","BL","BM","BN","BO","BQ","BR","BS","BT","BV","BW","BY","BZ","CA","CC","CD","CF","CG","CH","CI","CK","CL","CM","CN","CO","CP","CR","CU","CV","CW","CX","CY","CZ","DE","DG","DJ","DK","DM","DO","DZ","EA","EE","EG","EH","ER","ES","ET","EU","FI","FJ","FK","FM","FO","FR","GA","GB","GD","GE","GF","GG","GH","GI","GL","GM","GN","GP","GQ","GR","GS","GT","GU","GW","GY","HK","HM","HN","HR","HT","HU","IC","ID","IE","IL","IM","IN","IO","IQ","IR","IS","IT","JE","JM","JO","JP","KE","KG","KH","KI","KM","KN","KP","KR","KW","KY","KZ","LA","LB","LC","LI","LK","LR","LS","LT","LU","LV","LY","MA","MC","MD","ME","MF","MG","MH","MK","ML","MM","MN","MO","MP","MQ","MR","MS","MT","MU","MV","MW","MX","MY","MZ","NA","NC","NE","NF","NG","NI","NL","NO","NP","NR","NU","NZ","OM","PA","PE","PF","PG","PH","PK","PL","PM","PN","PR","PS","PT","PW","PY","QA","RE","RO","RS","RU","RW","SA","SB","SC","SD","SE","SG","SH","SI","SJ","SK","SL","SM","SN","SO","SR","SS","ST","SV","SX","SY","SZ","TA","TC","TD","TF","TG","TH","TJ","TK","TL","TM","TN","TO","TR","TT","TV","TW","TZ","UA","UG","UM","UN","US","UY","UZ","VA","VC","VE","VG","VI","VN","VU","WF","WS","XK","YE","YT","ZA","ZM","ZW"]);</script>

<c:if test="${metaDataSize == 0}">
	<br />
	<div class="alert">
		No Metadata available for Destination
		<c:out value="${destination.name}" />
	</div>
</c:if>

<c:if test="${metaDataSize != 0}">
	<table class="listing">
		<tr>
			<td><a
				href="<bean:message key="destination.basepath"/>?destinationSearch=country=${destination.country.iso}&destinationStatus=&destinationType="><img
					src="https://flagcdn.com/24x18/${fn:toLowerCase(destination.countryIso)}.png" onload="var m=this.src.match(/\/([a-z]{2})\./);if(!m||!window._validIso||!window._validIso.has(m[1].toUpperCase()))this.style.display='none';" onerror="this.style.display='none'"
					alt="Flag for ${destination.country.name}"
					title="See all destinations in ${destination.country.name}" /></a>&nbsp;${destination.country.name}</td>
			<td>${destination.ecUser.comment}</td>
			<td>${metaDataSize} metadatafile(s)</td>
		</tr>
	</table>
	<c:forEach var="file" items="${metaData}">
		<h3>${file.name}(${file.contentType}).LastModification:
			${file.lastModificationDate}</h3>
		<c:if test="${file.contentType == 'text/plain'}">
${file.stringContent}
</c:if>
		<c:if test="${file.contentType != 'text/plain'}">
			<content:content name="file" outputContentType="text/html"
				thumbnail="false" embedded="true" />
		</c:if>
	</c:forEach>
	<br />
</c:if>
