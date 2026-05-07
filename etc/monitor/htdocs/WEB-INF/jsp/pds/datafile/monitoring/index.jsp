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

<div class="d-flex align-items-center gap-2 mb-3 px-3 py-2 rounded"
style="background:rgba(13,110,253,0.06); font-size:0.9rem; color:var(--bs-body-color); border-left:4px solid #0d6efd;">
<i class="bi bi-speedometer2 text-primary flex-shrink-0"></i>
<span>Use this form to monitor the transfer rates of the Data Retrieval Mechanism.</span>
</div>

<html:form action="/datafile/monitoring">
<div class="card border-0 shadow-sm mb-3">
<div class="card-header fw-semibold" style="background:var(--bs-secondary-bg)">
<i class="bi bi-calendar3 me-2"></i>Date &amp; Time Range
</div>
<div class="card-body">
<div class="row g-3 mb-1">
<div class="col-sm-6">
<label for="fromDate" class="form-label mb-1">From Date</label>
<input type="text" class="form-control form-control-sm"
title="Select a date range to compare over the days" name="fromDate"
id="fromDate">
</div>
<div class="col-sm-6">
<label for="toDate" class="form-label mb-1">To Date</label>
<input type="text" class="form-control form-control-sm" name="toDate"
id="toDate">
</div>
</div>
<div class="row g-3">
<div class="col-sm-6">
<label for="fromTime" class="form-label mb-1">From Time</label>
<html:text property="fromTime" styleId="fromTime"
styleClass="form-control form-control-sm"
title="Select a time range for each day selected above" />
</div>
<div class="col-sm-6">
<label for="toTime" class="form-label mb-1">To Time</label>
<html:text property="toTime" styleId="toTime"
styleClass="form-control form-control-sm" />
</div>
</div>
</div>
</div>

<div class="card border-0 shadow-sm mb-3">
<div class="card-header fw-semibold" style="background:var(--bs-secondary-bg)">
<i class="bi bi-funnel me-2"></i>Filters
</div>
<div class="card-body">
<div class="row g-3">
<div class="col-sm-6">
<label for="caller" class="form-label mb-1">Caller</label>
<html:text property="caller" styleId="caller"
styleClass="form-control form-control-sm"
title="Allow selecting q2diss tasks only (*=everything)" />
<div class="form-text">Use * for all callers.</div>
</div>
<div class="col-sm-6">
<label for="sourceHost" class="form-label mb-1">Source Host</label>
<html:text property="sourceHost" styleId="sourceHost"
styleClass="form-control form-control-sm"
title="Allow selecting the source host (this is the host which was used to run the ecpds command but no necessarily the host used to retrieve the file)" />
<div class="form-text">Use the host that ran the ecpds command, which may differ from the retrieval host.</div>
</div>
</div>
</div>
</div>

<div class="card border-0 shadow-sm mb-3">
<div class="card-header fw-semibold" style="background:var(--bs-secondary-bg)">
<i class="bi bi-server me-2"></i>Granularity
</div>
<div class="card-body">
<div class="form-check form-switch mb-3">
<html:checkbox property="perTransferServer" styleId="perTransferServer"
styleClass="form-check-input"
title="Allow giving the transfer rates per Data Mover rather than accumulating per Transfer Group" />
<label class="form-check-label fw-semibold" for="perTransferServer">Per Data Mover</label>
<div class="form-text mt-1">Show transfer rates per Data Mover instead of aggregating by Transfer Group.</div>
</div>
<div class="row g-3">
<div class="col-sm-6">
<label for="transferServerName" class="form-label mb-1">Data Mover</label>
<bean:define id="servers" name="ratesActionForm"
property="transferServers" type="java.util.Collection" />
<html:select property="transferServerName" styleId="transferServerName"
styleClass="form-select form-select-sm"
title="If the search is per Data Mover then allow selecting a specific one to get the transfer rates per data disk">
<html:options collection="servers" property="name"
labelProperty="value" />
</html:select>
<div class="form-text">Only applies when Per Data Mover is enabled.</div>
</div>
</div>
</div>
</div>

<button type="submit" class="btn btn-primary">
<i class="bi bi-play-fill me-1"></i>Launch Data Analysis
</button>
</html:form>
