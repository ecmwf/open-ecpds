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

<h2>Use this Form to request a Compression Simulation for a
	Destination</h2>

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