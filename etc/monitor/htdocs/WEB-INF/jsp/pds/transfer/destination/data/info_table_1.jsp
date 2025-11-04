<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>

<table class="fields" style="width: 700px" border=0>

	<tr>
		<auth:if basePathKey="transferhistory.basepath" paths="/">
			<auth:then>
				<td><a
					href="<bean:message key="destination.basepath"/>?destinationSearch=country=${destination.country.iso}&destinationStatus=&destinationType="><img
						style="margin: 1" align="middle" border="0"
						src="/assets/images/flags/small/${destination.countryIso}.png"
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
					src="/assets/images/flags/small/${destination.countryIso}.png"
					alt="Flag for ${destination.country.name}"
					title="${destination.country.name}" /></td>
				<td>${destination.typeText}</td>
			</auth:else>
		</auth:if>

		<c:if test="${not empty ecpdsCanHandleQueue}">
			<td class="buttons">
				<table>
					<tr>
						<auth:if basePathKey="transferhistory.basepath" paths="/">
							<auth:then>
								<td><a href="javascript:cleanDestination()"><content:icon
											key="icon.trash" altKey="ecpds.destination.clean.all"
											titleKey="ecpds.destination.clean.all" writeFullTag="true" /></a></td>
								<td><a href="javascript:cleanExpiredDestination()"><content:icon
											key="icon.delete" altKey="ecpds.destination.cleanexpired.all"
											titleKey="ecpds.destination.cleanexpired.all"
											writeFullTag="true" /></a></td>
								<td>&nbsp;&nbsp;</td>
							</auth:then>
						</auth:if>
						<td><a href="javascript:restartDestination(false)"><content:icon
									key="icon.requeue" altKey="ecpds.destination.restart"
									titleKey="ecpds.destination.restart" writeFullTag="true" /></a></td>
						<td><a href="javascript:restartDestination(true)"><content:icon
									key="icon.requeue2"
									altKey="ecpds.destination.restart.immediate"
									titleKey="ecpds.destination.restart.immediate"
									writeFullTag="true" /></a></td>
						<td>&nbsp;&nbsp;</td>
						<td><a href="javascript:holdDestination(false)"><content:icon
									key="icon.stop2" altKey="ecpds.destination.hold"
									titleKey="ecpds.destination.hold" writeFullTag="true" /></a></td>
						<td><a href="javascript:holdDestination(true)"><content:icon
									key="icon.stop" altKey="ecpds.destination.hold.immediate"
									titleKey="ecpds.destination.hold.immediate" writeFullTag="true" /></a></td>
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
		<td><input title="Screen Refresh Period (0 is No Refresh)"
			class="small_number" type="text" size="5" name="refreshPeriod"
			value="${destinationDetailActionForm.refreshPeriod}"
			onKeyPress="submitenter(this,event)" /></td>
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
