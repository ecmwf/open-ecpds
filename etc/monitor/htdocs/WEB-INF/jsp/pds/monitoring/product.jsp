<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<style>
table.listing td {
	padding: 1pt;
	padding-right: 12pt;
}

table.fields {
	border: 1pt solid #d5d5d5;
}

table.fields td {
	padding: 1pt;
}

th {
    cursor: pointer;
}

/* Status info card at top of page */
.mon-info-card {
    background: #f8f9fa;
    border: 1px solid #dee2e6;
    border-radius: 8px;
    padding: 0.75rem 1rem;
    margin-bottom: 1rem;
    display: flex;
    flex-wrap: wrap;
    gap: 1.5rem;
    align-items: center;
    font-size: 0.85rem;
}
.mon-info-card .mon-field { display: flex; flex-direction: column; }
.mon-info-card .mon-label { font-size: 0.7rem; font-weight: 600; text-transform: uppercase; color: #6c757d; letter-spacing: 0.04em; }
.mon-info-card .mon-value { color: #212529; font-weight: 500; }
.mon-info-card .mon-status { display: flex; align-items: center; gap: 0.4rem; }
</style>

<tiles:insert page="./pds/monitoring/reload.jsp" />

<script>
	function setHrefForSendingEmail(anchor, bcc, subject, body) {
		try {
			anchor.href = 'https://outlook.office365.com/mail/deeplink/compose'
					+ '?subject=' + escape(subject) + '&body=' + body
					+ '&from=' + escape('operators@ecmwf.int') + '&to='
					+ escape('operators@ecmwf.int') + '?bcc=' + bcc;

		} catch (e) {
			showToast("Error: " + e, "danger");
		}
	}
	function isDuration(value) {
	    return /^-?\d+m$/.test(value);  // Matches strings like "-10m" or "300m"
	}
	function getDuration(value) {
	    var match = value.match(/^(-?\d+)(m)$/);  // Matches number followed by "m"
	    if (match) {
	        return parseInt(match[1], 10);  // Return the numeric value (in minutes)
	    }
	    return 0;  // Default to 0 if it's not a valid duration
	}
	function sortTable(columnIndex) {
	    var tables = document.querySelectorAll('.sortableTable');
	    if (!tables.length) return;
	    var th = tables[0].rows[0].cells[columnIndex];
	    var isAscending = th.getAttribute("data-order") === "asc";
	    // Collect all rows from both tables
	    var rows = [];
	    tables.forEach(function(t) {
	        rows = rows.concat(Array.prototype.slice.call(t.tBodies[0].rows));
	    });
	    rows.sort(function (rowA, rowB) {
	        var cellA = rowA.cells[columnIndex].innerText.trim();
	        var cellB = rowB.cells[columnIndex].innerText.trim();
	        if (isDuration(cellA) && isDuration(cellB)) {
	            return isAscending ? getDuration(cellA) - getDuration(cellB) : getDuration(cellB) - getDuration(cellA);
	        }
	        if (!isNaN(cellA) && !isNaN(cellB) && cellA !== '' && cellB !== '') {
	            return isAscending ? cellA - cellB : cellB - cellA;
	        }
	        return isAscending ? cellA.localeCompare(cellB) : cellB.localeCompare(cellA);
	    });
	    // Update data-order and sort icons on both table headers
	    tables.forEach(function(t) {
	        t.rows[0].cells[columnIndex].setAttribute("data-order", isAscending ? "desc" : "asc");
	        Array.prototype.slice.call(t.rows[0].cells).forEach(function(cell) {
	            var icon = cell.querySelector('i.bi');
	            if (icon) { icon.className = 'bi bi-arrow-down-up text-muted'; icon.style.fontSize = '0.6rem'; }
	        });
	        var activeIcon = t.rows[0].cells[columnIndex].querySelector('i.bi');
	        if (activeIcon) { activeIcon.className = 'bi ' + (isAscending ? 'bi-arrow-up' : 'bi-arrow-down') + ' text-primary'; activeIcon.style.fontSize = '0.6rem'; }
	    });
	    // Redistribute rows evenly: first half to table[0], second half to table[1]
	    var half = Math.ceil(rows.length / 2);
	    tables.forEach(function(t) { while (t.tBodies[0].rows.length) t.tBodies[0].deleteRow(0); });
	    rows.forEach(function(row, i) {
	        (i < half ? tables[0] : tables[tables.length > 1 ? 1 : 0]).tBodies[0].appendChild(row);
	    });
	}
	document.addEventListener("DOMContentLoaded", function () {
		sortTable(2); // Sort by 'Type' column when the page loads
	});
</script>

<c:if test="${productStatus.calculated}">
	<div class="mon-info-card">
		<div class="mon-field">
			<span class="mon-label">Product</span>
			<span class="mon-value">
				<a href="<bean:message key="monitoring.basepath"/>/summary/${productName}/${productStatus.time}">${productStatus.time}-${productName}</a><c:if test="${not empty step and not empty type}">, Step <u>${step}</u>, Type <u>${type}</u></c:if>
			</span>
		</div>
		<div class="mon-field">
			<span class="mon-label">Product Time</span>
			<span class="mon-value">${productStatus.productTime}</span>
		</div>
		<div class="mon-field">
			<span class="mon-label">Scheduled</span>
			<span class="mon-value">${productStatus.scheduledTime}</span>
		</div>
		<div class="mon-field">
			<span class="mon-label">Last Update</span>
			<span class="mon-value">
				<c:choose>
					<c:when test="${not empty productStatus.lastUpdate}">${productStatus.lastUpdate}</c:when>
					<c:otherwise><span class="text-muted fst-italic">none</span></c:otherwise>
				</c:choose>
			</span>
		</div>
		<div class="mon-field">
			<span class="mon-label">Arrival</span>
			<span class="mon-value">
				<c:choose>
					<c:when test="${not empty productStatus.arrivalTime}">${productStatus.arrivalTime}</c:when>
					<c:otherwise><span class="text-muted fst-italic">none</span></c:otherwise>
				</c:choose>
			</span>
		</div>
		<div class="mon-field">
			<span class="mon-label">Status</span>
			<span class="mon-value mon-status">
				<span class="mon-dot mon-dot-${productStatus.generationStatus lt 0 ? 'n1' : productStatus.generationStatus}" title="(${productStatus.generationStatus}) - <bean:message key='ecpds.monitoring.productStatus.${productStatus.generationStatus}'/>"></span>
				${productStatus.generationStatusFormattedCode}
			</span>
		</div>
	</div>

	<c:set var="arrival" value="${empty step ? 'Arrival' : 'Update'}"/>
	<c:set var="totalSteps" value="${fn:length(productStepStatii)}"/>
	<c:set var="splitIndex" value="${totalSteps % 2 == 0 ? totalSteps / 2 : (totalSteps / 2) + 1}"/>

	<%-- Shared thead macro --%>
	<div class="d-flex gap-2 align-items-start">
	<div style="flex:1; min-width:0;">
	<table class="sortableTable table table-sm table-striped table-hover table-bordered align-middle" style="font-size:0.78rem; white-space:nowrap;">
		<thead class="table-light">
			<tr>
				<th onclick="sortTable(0)" data-order="asc" style="cursor:pointer;" title="Sort by Time">T <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i></th>
				<th onclick="sortTable(1)" data-order="asc" style="cursor:pointer;" title="Sort by Step">Step <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i></th>
				<th onclick="sortTable(2)" data-order="asc" style="cursor:pointer;" title="Sort by Type">Type <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i></th>
				<th onclick="sortTable(3)" data-order="asc" style="cursor:pointer;" title="Sort by Notification">Notif. <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i></th>
				<th onclick="sortTable(4)" data-order="asc" style="cursor:pointer;" title="Sort by ${arrival} Time">${arrival} <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i></th>
				<th onclick="sortTable(5)" data-order="asc" style="cursor:pointer;" title="Sort by Schedule">Sched. <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i></th>
				<th onclick="sortTable(6)" data-order="asc" style="cursor:pointer;" title="Sort by Before">Before <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i></th>
				<th class="text-center" title="Status"></th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="stepStatus" items="${productStepStatii}" varStatus="loopStatus">
				<c:if test="${loopStatus.index == splitIndex}">
					</tbody></table></div>
					<div style="flex:1; min-width:0;">
					<table class="sortableTable table table-sm table-striped table-hover table-bordered align-middle" style="font-size:0.78rem; white-space:nowrap;">
						<thead class="table-light">
							<tr>
								<th onclick="sortTable(0)" data-order="asc" style="cursor:pointer;" title="Sort by Time">T <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i></th>
								<th onclick="sortTable(1)" data-order="asc" style="cursor:pointer;" title="Sort by Step">Step <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i></th>
								<th onclick="sortTable(2)" data-order="asc" style="cursor:pointer;" title="Sort by Type">Type <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i></th>
								<th onclick="sortTable(3)" data-order="asc" style="cursor:pointer;" title="Sort by Notification">Notif. <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i></th>
								<th onclick="sortTable(4)" data-order="asc" style="cursor:pointer;" title="Sort by ${arrival} Time">${arrival} <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i></th>
								<th onclick="sortTable(5)" data-order="asc" style="cursor:pointer;" title="Sort by Schedule">Sched. <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i></th>
								<th onclick="sortTable(6)" data-order="asc" style="cursor:pointer;" title="Sort by Before">Before <i class="bi bi-arrow-down-up text-muted" style="font-size:0.6rem;"></i></th>
								<th class="text-center" title="Status"></th>
							</tr>
						</thead>
						<tbody>
				</c:if>
				<tr>
						<td class="text-nowrap">${stepStatus.time}</td>
						<td class="text-nowrap">
							<a title="See history for Product, Time, Step and Type"
							   href="<bean:message key="monitoring.basepath"/>/summary/${stepStatus.product}/${stepStatus.time}/${stepStatus.step}/${stepStatus.type}">
								${stepStatus.step}
							</a>
						</td>
						<td class="text-nowrap">
							<a title="See history for Product, Time, Step and Type"
							   href="<bean:message key="monitoring.basepath"/>/summary/${stepStatus.product}/${stepStatus.time}/${stepStatus.step}/${stepStatus.type}">
								${stepStatus.type}
							</a>
						</td>
						<td>
							<c:if test="${not empty stepStatus.generationStatusFormattedCode}">
								<span class="badge rounded-pill
									<c:choose>
										<c:when test="${stepStatus.generationStatusCode == 'DONE'}">bg-success</c:when>
										<c:when test="${stepStatus.generationStatusCode == 'INIT'}">bg-secondary</c:when>
										<c:otherwise>bg-warning text-dark</c:otherwise>
									</c:choose>
								" style="font-size:0.72rem;">${stepStatus.generationStatusFormattedCode}</span>
							</c:if>
						</td>
						<td class="text-nowrap">
							<c:choose>
								<c:when test="${not empty stepStatus.arrivalTime}">
									<content:content name="stepStatus.arrivalTime" dateFormatKey="date.format.long.iso" ignoreNull="true" defaultValue="***"/>
								</c:when>
								<c:when test="${empty stepStatus.arrivalTime and empty step and stepStatus.generationStatusCode!=''}">
									<span class="text-muted fst-italic" style="font-size:0.78rem;">Didn't Arrive Yet</span>
								</c:when>
								<c:when test="${empty step and stepStatus.generationStatusCode==''}">
									<span class="text-danger fst-italic" style="font-size:0.78rem;">Notification Missing</span>
								</c:when>
								<c:when test="${empty stepStatus.arrivalTime and not empty step}">
									<span class="text-muted fst-italic">
										<content:content name="stepStatus.lastUpdate" dateFormatKey="date.format.long.iso" ignoreNull="true" defaultValue="***"/>
									</span>
								</c:when>
							</c:choose>
						</td>
						<td class="text-nowrap">
							<content:content name="stepStatus.scheduledTime" dateFormatKey="date.format.long.iso" ignoreNull="true" defaultValue="***"/>
						</td>
						<td class="text-nowrap text-end">
							<c:if test="${(empty step or stepStatus.generationStatusCode=='DONE') and stepStatus.generationStatusCode!=''}">
								<span style="font-size:0.8rem; font-weight:600;
									<c:choose>
										<c:when test="${stepStatus.minutesBeforeSchedule gt 0}">color:#198754;</c:when>
										<c:when test="${stepStatus.minutesBeforeSchedule lt 0}">color:#dc3545;</c:when>
										<c:otherwise>color:#6c757d;</c:otherwise>
									</c:choose>
								">${stepStatus.minutesBeforeSchedule}m</span>
							</c:if>
						</td>
						<td class="text-center">
							<c:if test="${empty step or stepStatus.generationStatusCode=='DONE'}">
								<span class="mon-dot mon-dot-${stepStatus.generationStatus lt 0 ? 'n1' : stepStatus.generationStatus}"
									  title="(${stepStatus.generationStatus}) - <bean:message key='ecpds.monitoring.productStatus.${stepStatus.generationStatus}'/>"></span>
							</c:if>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
	</div><%-- end two-column flex --%>
</c:if>

<c:if test="${!productStatus.calculated}">
	<div class="alert alert-info d-flex align-items-center gap-2 mt-3" role="alert">
		<i class="bi bi-info-circle-fill"></i>
		No information about this product and cycle yet.
	</div>
</c:if>


<c:set var="key" value="${productStatus.product}@${productStatus.time}" />
<c:set var="emails" value="${reqData.contacts[key]}" />
<c:set var="ECMWFProductsDelay"
	value="Dear colleagues%2C%0A%3C%3C Due to if known%2C please give some information of the reason for the%0Adelay%2C the or otherwise The %3E%3E dissemination of ECMWF %3C%3Catmospheric or%0Awave%3E%3E products for the %3C%3C00%2C 06%2C 12 or 18%3E%3EZ cycle of the%0A%3C%3C%22high-resolution forecast%22 or %22BC high-resolution forecast%22 or %22ensemble forecast%22 or %22Limited-area wave forecast%22%3E%3E will be delayed.%0A%0AAs soon as we have further details we will inform you.%0A%0AFor more up to date information%2C you may refer to ECMWF service status%0Apage at http%3A%2F%2Fwww.ecmwf.int%2Fen%2Fservice-status .%0A%0AOur sincere apologies for the inconvenience caused by this delay.%0A%0AKind regards%0A%0AECMWF Duty Manager" />
<c:set var="ECMWFProducts"
	value="Dear colleagues,%0D%0A%0D%0AI am pleased to inform you that the problems we encountered earlier%0D%0Awithin the operational production have been resolved and the dissemination of products has started.%0D%0A%0D%0AOur sincere apologies for the inconvenience caused by this delay.%0D%0A%0D%0AKind regards%0D%0A%0D%0AECMWF Duty Manager%0D%0A" />
<c:if test="${not empty emails}">
    <div class="card mt-4 border-secondary-subtle">
        <div class="card-header py-2 px-3 d-flex align-items-center gap-2 bg-light">
            <i class="bi bi-envelope-fill text-secondary"></i>
            <span class="fw-semibold text-secondary" style="font-size:0.82rem;">Email Notifications &mdash; ${productStatus.time}-${productStatus.product}</span>
        </div>
        <div class="card-body py-2 px-3 d-flex align-items-center flex-wrap gap-2">
            <a id="delayEmail" target="_blank"
               title="Open Outlook: Products Delay Email for ${productStatus.time}-${productStatus.product}"
               class="btn btn-sm btn-outline-warning">
                <i class="bi bi-exclamation-triangle-fill me-1"></i>Products Delay
            </a>
            <a id="productEmail" target="_blank"
               title="Open Outlook: Products Resumed Email for ${productStatus.time}-${productStatus.product}"
               class="btn btn-sm btn-outline-success">
                <i class="bi bi-check-circle-fill me-1"></i>Products Resumed
            </a>
            <button type="button"
                    class="btn btn-sm btn-outline-secondary"
                    title="Copy CC email addresses to clipboard"
                    onclick="copyToClipboard('<c:out value="${emails}" />')">
                <i class="bi bi-clipboard me-1"></i>Copy CC Emails
            </button>
        </div>
    </div>

    <script>
        function copyToClipboard(text) {
            if (!text || text.trim() === 'undefined') {
                showToast("No emails to copy!", "warning");
                return;
            }
            const emailArray = text.split(',');
            const emailCount = emailArray.length;
            if (navigator.clipboard && window.isSecureContext) {
                navigator.clipboard.writeText(text).then(function() {
                    showToast(emailCount + " email" + (emailCount !== 1 ? "s" : "") + " copied to clipboard", "success");
                }, function() {
                    showToast("Failed to copy to clipboard", "danger");
                });
            } else {
                const textarea = document.createElement("textarea");
                textarea.value = text;
                textarea.style.position = "fixed";
                textarea.style.opacity = "0";
                document.body.appendChild(textarea);
                textarea.select();
                try {
                    document.execCommand("copy");
                    showToast(emailCount + " email" + (emailCount !== 1 ? "s" : "") + " copied to clipboard", "success");
                } catch (e) {
                    showToast("Failed to copy to clipboard", "danger");
                } finally {
                    document.body.removeChild(textarea);
                }
            }
        }

        setHrefForSendingEmail(
            document.getElementById('delayEmail'),
            'op_delay_ecmwf@lists.ecmwf.int,ecpds-product-${productStatus.time}-${productStatus.product}@ecmwf.int',
            'ECMWF Products Delay (${productStatus.time}-${productStatus.product})',
            '${ECMWFProductsDelay}');

        setHrefForSendingEmail(
            document.getElementById('productEmail'),
            'op_delay_ecmwf@lists.ecmwf.int,ecpds-product-${productStatus.time}-${productStatus.product}@ecmwf.int',
            'ECMWF Products (${productStatus.time}-${productStatus.product})',
            '${ECMWFProducts}');
    </script>
</c:if>