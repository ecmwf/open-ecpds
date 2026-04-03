<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>

<script>
	$.datepicker.setDefaults({
		dateFormat : 'yy-mm-dd'
	});
	$(function() {
		$("#date").datepicker({
			defaultDate : "+1w",
			changeMonth : true,
			numberOfMonths : 2,
			maxDate : 0
		})
	});
	$(document).ready(function() {
		$("#date").val($.datepicker.formatDate('yy-mm-dd', new Date()));
	});
</script>

<div class="d-flex align-items-center gap-2 mb-3 px-1 py-2 rounded"
     style="background:rgba(13,110,253,0.06); font-size:0.83rem; color:#495057; border-left:3px solid #0d6efd;">
    <i class="bi bi-funnel text-primary ms-1 flex-shrink-0"></i>
    Use this form to request a Compression Simulation for a Destination.
</div>

<!-- <html:form action="/admin/filter"> -->
<form name="filterActionForm" action="/do/admin/filter" method="post">
	<table class="fields">
		<tr>
			<th>Destination</th>
			<td><bean:define id="destinations" name="filterActionForm"
					property="destinationOptions" type="java.util.Collection" /> <html:select
					property="destination">
					<html:options collection="destinations" property="name"
						labelProperty="name" />
				</html:select></td>
		</tr>
		<tr>
			<th>File Pattern</th>
			<td><html:text property="pattern" /></td>
		</tr>
		<tr>
			<th>Email</th>
			<td><html:text property="email" /></td>
		</tr>
		<tr>
			<th>Data Compression</th>
			<td><bean:define id="filters" name="filterActionForm"
					property="filterNameOptions" type="java.util.Collection" /> <html:select
					property="filter">
					<html:options collection="filters" property="name"
						labelProperty="name" />
				</html:select></td>
		</tr>
		<tr>
			<th>Date</th>
			<td><input type="text" name="date" id="date"></td>
		</tr>
		<tr>
			<th>Include Standby</th>
			<td><html:checkbox property="includeStdby" /></td>
		</tr>
	</table>
	</br>
	<button type="submit">Launch Compression Simulation</button>
	</html:form>