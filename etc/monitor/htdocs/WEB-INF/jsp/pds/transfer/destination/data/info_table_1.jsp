<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<script>window._validIso=new Set(["AC","AD","AE","AF","AG","AI","AL","AM","AO","AQ","AR","AS","AT","AU","AW","AX","AZ","BA","BB","BD","BE","BF","BG","BH","BI","BJ","BL","BM","BN","BO","BQ","BR","BS","BT","BV","BW","BY","BZ","CA","CC","CD","CF","CG","CH","CI","CK","CL","CM","CN","CO","CP","CR","CU","CV","CW","CX","CY","CZ","DE","DG","DJ","DK","DM","DO","DZ","EA","EE","EG","EH","ER","ES","ET","EU","FI","FJ","FK","FM","FO","FR","GA","GB","GD","GE","GF","GG","GH","GI","GL","GM","GN","GP","GQ","GR","GS","GT","GU","GW","GY","HK","HM","HN","HR","HT","HU","IC","ID","IE","IL","IM","IN","IO","IQ","IR","IS","IT","JE","JM","JO","JP","KE","KG","KH","KI","KM","KN","KP","KR","KW","KY","KZ","LA","LB","LC","LI","LK","LR","LS","LT","LU","LV","LY","MA","MC","MD","ME","MF","MG","MH","MK","ML","MM","MN","MO","MP","MQ","MR","MS","MT","MU","MV","MW","MX","MY","MZ","NA","NC","NE","NF","NG","NI","NL","NO","NP","NR","NU","NZ","OM","PA","PE","PF","PG","PH","PK","PL","PM","PN","PR","PS","PT","PW","PY","QA","RE","RO","RS","RU","RW","SA","SB","SC","SD","SE","SG","SH","SI","SJ","SK","SL","SM","SN","SO","SR","SS","ST","SV","SX","SY","SZ","TA","TC","TD","TF","TG","TH","TJ","TK","TL","TM","TN","TO","TR","TT","TV","TW","TZ","UA","UG","UM","UN","US","UY","UZ","VA","VC","VE","VG","VI","VN","VU","WF","WS","XK","YE","YT","ZA","ZM","ZW"]);</script>

<table class="fields" style="width: 700px" border=0>

	<tr>
		<auth:if basePathKey="transferhistory.basepath" paths="/">
			<auth:then>
				<td><a
					href="<bean:message key="destination.basepath"/>?destinationSearch=country=${destination.country.iso}&destinationStatus=&destinationType="><img
						style="margin: 1" align="middle" border="0"
						src="https://flagcdn.com/24x18/${fn:toLowerCase(destination.countryIso)}.png" onload="var m=this.src.match(/\/([a-z]{2})\./);if(!m||!window._validIso||!window._validIso.has(m[1].toUpperCase()))this.style.display='none';" onerror="this.style.display='none'"
						alt="Flag for ${destination.country.name}"
						title="See all destinations in ${destination.country.name}" /></a></td>
				<td><a title="See all destinations in the group"
					href="<bean:message key="destination.basepath"/>?destinationSearch=&destinationStatus=&destinationType=${destination.type}">${destination.typeText}</a></td>
					<td>Notify <c:if test="${destination.mailOnUpdate}">  &nbsp; <content:icon
								key="icon.mail1" altKey="ecpds.destination.mailOnUpdate"
								titleKey="ecpds.destination.mailOnUpdate" writeFullTag="true" />
						</c:if> <c:if test="${destination.mailOnStart}"> &nbsp; <content:icon
									key="icon.mail2" altKey="ecpds.destination.mailOnStart"
									titleKey="ecpds.destination.mailOnStart" writeFullTag="true" />
						</c:if> <c:if test="${destination.mailOnEnd}"> &nbsp; <content:icon
							key="icon.mail2" altKey="ecpds.destination.mailOnEnd"
							titleKey="ecpds.destination.mailOnEnd" writeFullTag="true" />
					</c:if> <c:if test="${destination.mailOnError}"> &nbsp; <content:icon
							key="icon.mail3" altKey="ecpds.destination.mailOnError"
							titleKey="ecpds.destination.mailOnError" writeFullTag="true" />
					</c:if> to
				</td>
				<td><a href="mailto:${destination.userMail}">${destination.userMail}</a></td>
			</auth:then>
			<auth:else>
				<td><img style="margin: 1" align="middle" border="0"
					src="https://flagcdn.com/24x18/${fn:toLowerCase(destination.countryIso)}.png" onload="var m=this.src.match(/\/([a-z]{2})\./);if(!m||!window._validIso||!window._validIso.has(m[1].toUpperCase()))this.style.display='none';" onerror="this.style.display='none'"
					alt="Flag for ${destination.country.name}"
					title="${destination.country.name}" /></td>
				<td>${destination.typeText}</td>
			</auth:else>
		</auth:if>

		<c:if test="${not empty ecpdsCanHandleQueue}">
			<td class="buttons" style="vertical-align: middle;">
				<table>
					<tr>
						<c:set var="statusBase" value="${fn:contains(destination.formattedStatus, '-') ? fn:substringBefore(destination.formattedStatus, '-') : destination.formattedStatus}"/>
						<c:set var="isStopped" value="${statusBase == 'Stopped' or statusBase == 'Initialized' or statusBase == 'NoHosts' or statusBase == 'Interrupted' or statusBase == 'Failed'}"/>
						<c:if test="${isStopped}">
							<auth:if basePathKey="transferhistory.basepath" paths="/">
								<auth:then>
									<td><a href="javascript:cleanDestination()" title="Remove All Data Transfers"><img src="/assets/icons/webapp/trash.png" border="0" /></a></td>
									<td><a href="javascript:cleanExpiredDestination()" title="Remove Deleted, Expired, Stopped, Failed Data Transfers"><img src="/assets/icons/webapp/kde31/delete.png" border="0" /></a></td>
									<td>&nbsp;&nbsp;</td>
								</auth:then>
							</auth:if>
						</c:if>
						<c:if test="${not isStopped}">
							<auth:if basePathKey="transferhistory.basepath" paths="/">
								<auth:then>
									<td><a href="javascript:cleanDestination()"><content:icon
											key="icon.trash" altKey="ecpds.destination.clean.all"
											titleKey="ecpds.destination.clean.all" writeFullTag="true" /></a></td>
									<td><a href="javascript:cleanExpiredDestination()"><content:icon
											key="icon.delete" altKey="ecpds.destination.cleanexpired.all"
											titleKey="ecpds.destination.cleanexpired.all" writeFullTag="true" /></a></td>
									<td>&nbsp;&nbsp;</td>
								</auth:then>
							</auth:if>
						</c:if>
						<c:choose>
							<c:when test="${statusBase == 'Stopped' or statusBase == 'Initialized' or statusBase == 'NoHosts' or statusBase == 'Interrupted' or statusBase == 'Failed'}">
								<td><a href="javascript:startDestination()" title="Start Destination"><i class="bi bi-play-circle-fill text-success" style="font-size:1.1rem;vertical-align:middle;"></i></a></td>
							</c:when>
							<c:otherwise>
								<td><a href="javascript:restartDestination(false)"><content:icon
											key="icon.requeue" altKey="ecpds.destination.restart"
											titleKey="ecpds.destination.restart" writeFullTag="true" /></a></td>
								<td><a href="javascript:restartDestination(true)"><content:icon
											key="icon.requeue2"
											altKey="ecpds.destination.restart.immediate"
											titleKey="ecpds.destination.restart.immediate"
											writeFullTag="true" /></a></td>
							</c:otherwise>
						</c:choose>
						<td>&nbsp;&nbsp;</td>
						<c:if test="${statusBase != 'Stopped' and statusBase != 'Initialized' and statusBase != 'NoHosts' and statusBase != 'Interrupted' and statusBase != 'Failed'}">
						<td><a href="javascript:holdDestination(false)"><content:icon
									key="icon.stop2" altKey="ecpds.destination.hold"
									titleKey="ecpds.destination.hold" writeFullTag="true" /></a></td>
						<td><a href="javascript:holdDestination(true)"><content:icon
									key="icon.stop" altKey="ecpds.destination.hold.immediate"
									titleKey="ecpds.destination.hold.immediate" writeFullTag="true" /></a></td>
						</c:if>
						<c:if test="${destination.dirty}">
							<td>&nbsp;&nbsp;</td>
							<td><content:icon key="icon.dirty"
									altKey="ecpds.destination.dirty"
									titleKey="ecpds.destination.dirty" writeFullTag="true" /></td>
						</c:if>
					</tr>
				</table>
			</td>
		</c:if>
		<td style="vertical-align: middle;">
			<input type="hidden" name="refreshPeriod" id="refreshPeriodVal"
				value="${destinationDetailActionForm.refreshPeriod}"/>
			<div class="d-flex align-items-center gap-1 flex-shrink-0">
				<i class="bi bi-arrow-clockwise text-muted me-1" style="font-size:0.85rem;" title="Auto-refresh interval"></i>
				<button type="button" class="date-pill dest-refresh-pill ${destinationDetailActionForm.refreshPeriod == 0 ? 'active' : ''}" data-value="0">Off</button>
				<button type="button" class="date-pill dest-refresh-pill ${destinationDetailActionForm.refreshPeriod == 60 ? 'active' : ''}" data-value="60">1m</button>
				<button type="button" class="date-pill dest-refresh-pill ${destinationDetailActionForm.refreshPeriod == 300 ? 'active' : ''}" data-value="300">5m</button>
				<button type="button" class="date-pill dest-refresh-pill ${destinationDetailActionForm.refreshPeriod == 600 ? 'active' : ''}" data-value="600">10m</button>
			</div>
			<script>
				document.querySelectorAll('.dest-refresh-pill').forEach(function(btn) {
					btn.addEventListener('click', function() {
						document.getElementById('refreshPeriodVal').value = this.dataset.value;
						document.forms['destinationDetailActionForm'].submit();
					});
				});
			</script>
		</td>
	</tr>

</table>

<script>
	var refresh = ${destinationDetailActionForm.refreshPeriod};
	if (refresh > 0) {
		setTimeout(function() {
			window.location.reload(true);
		}, refresh * 1000);

		setTimeout(function() {
			window.location.reload(true);
		}, refresh * 1000 * 5);

		setTimeout(function() {
			window.location.reload(true);
		}, refresh * 1000 * 15);

		setTimeout(function() {
			window.location.reload(true);
		}, refresh * 1000 * 30);
	}
</script>
