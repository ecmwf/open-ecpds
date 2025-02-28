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
			alert("Error" + e);
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
	    // Get all tables with the class '.sortableTable'
	    var tables = document.querySelectorAll('.sortableTable');
	    var rows = [];
	    // Collect all rows (excluding headers) from all tables
	    tables.forEach(table => {
	        var tableRows = Array.prototype.slice.call(table.rows, 1); // Exclude the header row
	        rows = rows.concat(tableRows); // Merge all rows into the rows array
	    });
	    // Determine if sorting should be ascending or descending
	    var isAscending = tables[0].rows[0].cells[columnIndex].getAttribute("data-order") === "asc";
	    // Sort rows based on the column index
	    rows.sort(function (rowA, rowB) {
	        var cellA = rowA.cells[columnIndex].innerText.trim();
	        var cellB = rowB.cells[columnIndex].innerText.trim();
			if (isDuration(cellA) && isDuration(cellB)) {
			    return isAscending ? getDuration(cellA) - getDuration(cellB) : getDuration(cellB) - getDuration(cellA);
			}
	        if (!isNaN(cellA) && !isNaN(cellB)) {
	            return isAscending ? cellA - cellB : cellB - cellA;
	        }
	        return isAscending ? cellA.localeCompare(cellB) : cellB.localeCompare(cellA);
	    });
	    // Toggle the sorting direction on the header cell
	    tables[0].rows[0].cells[columnIndex].setAttribute("data-order", isAscending ? "desc" : "asc");
	    // Empty the tables (excluding the header)
	    tables.forEach(table => {
	        while (table.rows.length > 1) {
	            table.deleteRow(1);
	        }
	    });
	    // Maintain the correct row distribution logic after sorting
	    var totalRows = rows.length;
	    var halfSize = Math.floor(totalRows / 2); // Ensure consistency with JSP logic
	    if (totalRows % 2 !== 0) {
	        halfSize += 1; // Ensure first table has the extra row when odd
	    }
	    var table1Fragment = document.createDocumentFragment(); // For the first table
	    var table2Fragment = document.createDocumentFragment(); // For the second table
	    // Append sorted rows into two fragments (based on the index of the row)
	    rows.forEach((row, index) => {
	        row.className = (index % 2 === 0) ? 'even' : 'odd'; // Add alternating classes
	        if (index < halfSize) {
	            table1Fragment.appendChild(row); // First table
	        } else {
	            table2Fragment.appendChild(row); // Second table
	        }
	    });
	    // Append the fragments to the respective tables
	    tables[0].appendChild(table1Fragment);
	    if (tables.length > 1) {
	        tables[1].appendChild(table2Fragment);
	    }
	}
</script>

<c:if test="${productStatus.calculated}">
	<table class="fields">
		<tr>
			<th>Product Name</th>
			<td><a
				href="<bean:message key="monitoring.basepath"/>/summary/${productName}/${productStatus.time}">${productStatus.time}-${productName}</a>
				<c:if test="${not empty step and not empty type}">, Step <u>${step}</u>, Type <u>${type}</u>
				</c:if></td>
			<th>Product Time</th>
			<td>${productStatus.productTime}</td>
			<th>Scheduled</th>
			<td>${productStatus.scheduledTime}</td>
		</tr>
		<tr>
			<th>Last Update</th>
			<td>${productStatus.lastUpdate}</td>
			<th>Arrival</th>
			<td>${productStatus.arrivalTime}</td>
			<th>Status</th>
			<td><img width="12" height="12"
				src="/assets/images/ecpds/g${productStatus.generationStatus}.png"
				border="0"
				title="(${productStatus.generationStatus}) - <bean:message key='ecpds.monitoring.productStatus.${productStatus.generationStatus}'/>">
				${productStatus.generationStatusFormattedCode}</td>
	</table>
	<table>
	    <tr>
	        <td style="vertical-align: top">
	            <c:if test="${empty step}">
	                <c:set var="arrival">Arrival</c:set>
	            </c:if>
	            <c:if test="${not empty step}">
	                <c:set var="arrival">Update</c:set>
	            </c:if>
	            <table class="sortableTable listing">
	                <tr>
	                    <th onclick="sortTable(0)" data-order="asc" title="Sort by Time">T</th>
	                    <th onclick="sortTable(1)" data-order="asc" title="Sort by Step">Step</th>
	                    <th onclick="sortTable(2)" data-order="asc" title="Sort by Type">Type</th>
	                    <th onclick="sortTable(3)" data-order="asc" title="Sort by Notification">Notification</th>
	                    <th onclick="sortTable(4)" data-order="asc" title="Sort by Arrival Time">${arrival}</th>
	                    <th onclick="sortTable(5)" data-order="asc" title="Sort by Schedule Time">Schedule</th>
	                    <th onclick="sortTable(6)" data-order="asc" title="Sort by Before Time">Before</th>
	                    <th></th>
	                </tr>

					<c:set var="totalSteps" value="${fn:length(productStepStatii)}" />
					<c:set var="splitIndex" value="${(totalSteps / 2) >= 0 ? Math.floor(totalSteps / 2) : Math.ceil(totalSteps / 2)}" />

					<c:forEach var="stepStatus" items="${productStepStatii}" varStatus="status">
					    <c:if test="${status.index == splitIndex}">
					        </table>
					        </td>
					        <td style="vertical-align: top">
					        <table class="sortableTable listing">
					        <tr>
								<th onclick="sortTable(0)" data-order="asc" title="Sort by Time">T</th>
								<th onclick="sortTable(1)" data-order="asc" title="Sort by Step">Step</th>
								<th onclick="sortTable(2)" data-order="asc" title="Sort by Type">Type</th>
								<th onclick="sortTable(3)" data-order="asc" title="Sort by Notification">Notification</th>
								<th onclick="sortTable(4)" data-order="asc" title="Sort by Arrival Time">${arrival}</th>
								<th onclick="sortTable(5)" data-order="asc" title="Sort by Schedule Time">Schedule</th>
								<th onclick="sortTable(6)" data-order="asc" title="Sort by Before Time">Before</th>
					            <th></th>
					        </tr>
					    </c:if>
						
					    <!-- Alternate row styling -->
					    <c:choose>
					        <c:when test="${(status.count % 2) > 0}">
					            <tr class="even">
					        </c:when>
					        <c:otherwise>
					            <tr class="odd">
					        </c:otherwise>
					    </c:choose>

					    <td>${stepStatus.time}</td>
					    <td><a title="See history for Product, Time, Step and Type"
					        href="<bean:message key="monitoring.basepath"/>/summary/${stepStatus.product}/${stepStatus.time}/${stepStatus.step}/${stepStatus.type}">${stepStatus.step}</a></td>
					    <td><a title="See history for Product, Time, Step and Type"
					        href="<bean:message key="monitoring.basepath"/>/summary/${stepStatus.product}/${stepStatus.time}/${stepStatus.step}/${stepStatus.type}">${stepStatus.type}</a></td>
					    <td>${stepStatus.generationStatusFormattedCode}</td>
					    <td>
					        <c:choose>
					            <c:when test="${not empty stepStatus.arrivalTime}">
					                <content:content name="stepStatus.arrivalTime"
					                    dateFormatKey="date.format.long.iso" ignoreNull="true"
					                    defaultValue="***" />
					            </c:when>
					            <c:when test="${empty stepStatus.arrivalTime and empty step and stepStatus.generationStatusCode!=''}">
					                <i>Didn't Arrive Yet</i>
					            </c:when>
					            <c:when test="${empty step and stepStatus.generationStatusCode==''}">
					                <i>Notification Missing</i>
					            </c:when>
					            <c:when test="${empty stepStatus.arrivalTime and not empty step}">
					                <i><content:content name="stepStatus.lastUpdate"
					                        dateFormatKey="date.format.long.iso" ignoreNull="true"
					                        defaultValue="***" /></i>
					            </c:when>
					        </c:choose>
					    </td>
					    <td><content:content name="stepStatus.scheduledTime"
					            dateFormatKey="date.format.long.iso" ignoreNull="true"
					            defaultValue="***" /></td>
					    <td>
					        <c:if test="${(empty step or stepStatus.generationStatusCode=='DONE') and stepStatus.generationStatusCode!=''}">
					            ${stepStatus.minutesBeforeSchedule}m
					        </c:if>
					    </td>
					    <td>
					        <c:if test="${empty step or stepStatus.generationStatusCode=='DONE'}">
					            <img width="12" height="12"
					                src="/assets/images/ecpds/g${stepStatus.generationStatus}.png"
					                border="0"
					                title="(${stepStatus.generationStatus}) - <bean:message key='ecpds.monitoring.productStatus.${stepStatus.generationStatus}'/>">
					        </c:if>
					    </td>
					    </tr>
					</c:forEach>
	            </table>
	        </td>
	    </tr>
	</table>
</c:if>

<c:if test="${!productStatus.calculated}">
	<div class="alert">
		<span class="closebtn" onclick="parent.history.back();">&times;</span>
		No information about this product and cycle yet!
	</div>
</c:if>

</br>

<c:set var="key" value="${productStatus.product}@${productStatus.time}" />
<c:set var="emails" value="${reqData.contacts[key]}" />
<c:set var="ECMWFProductsDelay"
	value="Dear colleagues%2C%0A%3C%3C Due to if known%2C please give some information of the reason for the%0Adelay%2C the or otherwise The %3E%3E dissemination of ECMWF %3C%3Catmospheric or%0Awave%3E%3E products for the %3C%3C00%2C 06%2C 12 or 18%3E%3EZ cycle of the%0A%3C%3C%22high-resolution forecast%22 or %22BC high-resolution forecast%22 or %22ensemble forecast%22 or %22Limited-area wave forecast%22%3E%3E will be delayed.%0A%0AAs soon as we have further details we will inform you.%0A%0AFor more up to date information%2C you may refer to ECMWF service status%0Apage at http%3A%2F%2Fwww.ecmwf.int%2Fen%2Fservice-status .%0A%0AOur sincere apologies for the inconvenience caused by this delay.%0A%0AKind regards%0A%0AECMWF Duty Manager" />
<c:set var="ECMWFProducts"
	value="Dear colleagues,%0D%0A%0D%0AI am pleased to inform you that the problems we encountered earlier%0D%0Awithin the operational production have been resolved and the dissemination of products has started.%0D%0A%0D%0AOur sincere apologies for the inconvenience caused by this delay.%0D%0A%0D%0AKind regards%0D%0A%0D%0AECMWF Duty Manager%0D%0A" />
<c:choose>
    <c:when test="${not empty emails}">
        <img src="/assets/icons/ecpds/mail.png" alt="Emails">

        <a title="Open Outlook" style="text-decoration: none" target="_blank"
            id="delayEmail">Products Delay Email for
            ${productStatus.time}-${productStatus.product}</a>&nbsp;|&nbsp;

        <a title="Open Outlook" style="text-decoration: none"
            target="_blank" id="productEmail">Products Email for
            ${productStatus.time}-${productStatus.product}</a>&nbsp;|&nbsp;

        <a href="javascript:void(0);" 
            title="Click to copy CC Emails" 
            onclick="copyToClipboard('<c:out value="${emails}" />')">
            Copy CC Emails</a>

        <script>
            function copyToClipboard(text) {
                if (!text || text.trim() === 'undefined') {
                    alert("No emails to copy!");
                    return;
                }

                const emailArray = text.split(','); // Split the emails by comma
                const emailCount = emailArray.length; // Count the number of emails

                // Create a textarea element for copying the emails
                const textarea = document.createElement("textarea");
                textarea.value = text;
                document.body.appendChild(textarea);
                textarea.select();
                document.execCommand("copy");
                document.body.removeChild(textarea);

                // Show the number of emails copied
                alert("Number of emails copied to clipboard: " + emailCount);
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
    </c:when>
    <c:otherwise>
    </c:otherwise>
</c:choose>