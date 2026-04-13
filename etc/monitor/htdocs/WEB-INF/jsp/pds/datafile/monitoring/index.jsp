<%@ page session="true"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<script>
	$(function() {
		$.datepicker.setDefaults({
			dateFormat : 'yy-mm-dd'
		});
		from = $("#fromDate").datepicker({
			defaultDate : "+1w",
			changeMonth : true,
			numberOfMonths : 2,
			maxDate : 0
		}).on("change", function() {
			to.datepicker("option", "minDate", getDate(this));
		}), to = $("#toDate").datepicker({
			defaultDate : "+1w",
			changeMonth : true,
			numberOfMonths : 2,
			maxDate : 0
		}).on("change", function() {
			from.datepicker("option", "maxDate", getDate(this));
		});

		function getDate(element) {
			var date;
			try {
				date = $.datepicker.parseDate("yy-mm-dd", element.value);
			} catch (error) {
				date = null;
			}
			return date;
		}
	});
	$(document).ready(function() {
		var d = new Date();
		d.setDate(d.getDate() - 4);
		$("#fromDate").val($.datepicker.formatDate('yy-mm-dd', d));
		$("#toDate").val($.datepicker.formatDate('yy-mm-dd', new Date()));
	});
</script>

<div class="d-flex align-items-center gap-2 mb-3 px-1 py-2 rounded"
     style="background:rgba(13,110,253,0.06); font-size:0.83rem; color:var(--bs-body-color); border-left:3px solid #0d6efd;">
    <i class="bi bi-speedometer2 text-primary ms-1 flex-shrink-0"></i>
    Use this form to monitor the transfer rates of the Data Retrieval Mechanism.
</div>

<!-- <html:form action="/datafile/monitoring"> -->
<form name="ratesActionForm" action="/do/datafile/monitoring"
	method="post">

	<table class="fields">

		<tr>
			<th>From Date</th>
			<td><input type="text"
				title="Select a date range to compare over the days" name="fromDate"
				id="fromDate"></td>
		</tr>
		<tr>
			<th>To Date</th>
			<td><input type="text" name="toDate" id="toDate"></td>
		</tr>

		<tr>
			<th>From Time</th>
			<td><html:text
					title="Select a time range for each day selected above"
					property="fromTime" /></td>
		</tr>
		<tr>
			<th>To Time</th>
			<td><html:text property="toTime" /></td>
		</tr>

		<tr>
			<th>Caller</th>
			<td><html:text
					title="Allow selecting q2diss tasks only (*=everything)"
					property="caller" /></td>
		</tr>
		<tr>
			<th>Source Host</th>
			<td><html:text
					title="Allow selecting the source host (this is the host which was used to run the ecpds command but no necessarily the host used to retrieve the file)"
					property="sourceHost" /></td>
		</tr>

		<tr>
			<th>Per Transfer Server</th>
			<td><html:checkbox
					title="Allow giving the transfer rates per Transfer Server rather than accumulating per Transfer Group"
					property="perTransferServer" /></td>
		</tr>

		<tr>
			<th>For</th>
			<td><bean:define id="servers" name="ratesActionForm"
					property="transferServers" type="java.util.Collection" /> <html:select
					title="If the search if per Transfer Server then allow selecting a specific one to get the transfer rates per data disk"
					property="transferServerName">
					<html:options collection="servers" property="name"
						labelProperty="value" />
				</html:select></td>
		</tr>
	</table>
	</br>

	<button type="submit">Launch Data Analysis</button>

	</html:form>