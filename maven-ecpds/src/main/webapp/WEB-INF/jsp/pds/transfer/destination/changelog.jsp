<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<c:if test="${empty destination.changeLogList}">
	<br />
	<div class="alert">
		No Change Log available for Destination
		<c:out value="${destination.name}" />
	</div>
</c:if>

<c:if test="${!empty destination.changeLogList}">

	<style>
.scroll-box {
	height: 100px;
	min-height: 100px;
	overflow-y: auto;
	border: 1px solid #ccc;
	padding: 4px;
	resize: vertical;
	overflow: auto;
}
</style>

	<display:table name="${destination.changeLogList}" id="changelog"
		requestURI="" sort="list" pagesize="4" class="listing">
		<display:column title="Date & Time" sortable="true" sortProperty="date">
			<content:content name="changelog.date"
				dateFormatKey="date.format.long.iso" ignoreNull="true" />
		</display:column>
		<display:column title="WebUser" sortable="true">${changelog.webUserId}</display:column>
		<display:column title="Differences">
			<div id="${changelog.changeLogId}">
				<ul>
					<li><a href="#previous">Compared to Previous Instance</a></li>
					<li><a href="#current">Compared to Current Instance</a></li>
				</ul>
				<div id="previous">
					<div class="scroll-box">
						<pre style="margin: 0px 0px 0px 5px;">${changelog.differences}</pre>
					</div>
				</div>
				<div id="current">
					<div class="scroll-box">
						<pre style="margin: 0px 0px 0px 5px;">${changelog.differencesFromCurrent}</pre>
					</div>
				</div>
			</div>
			<script>
				$(function() {
					$("#${changelog.changeLogId}").tabs();
				});
				<c:if test="${empty changelog.differencesFromCurrent}">
				$(function() {
					$("#${changelog.changeLogId}").tabs({
						disabled : [ 1 ]
					});
				});
				</c:if>
			</script>
		</display:column>
	</display:table>

</c:if>
