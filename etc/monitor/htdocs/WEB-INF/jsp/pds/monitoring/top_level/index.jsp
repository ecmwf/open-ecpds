<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<style>
/*�� Table layout�� */
table.pagelevel {
    border-collapse: collapse;
    font-size: 0.78rem;
}
table.toplevel {
    border-collapse: collapse;
    font-size: 0.78rem;
}
table.toplevel td.holder {
    vertical-align: top;
    border: 0;
    padding: 4px 6px;
}
table.toplevel td {
    border: 1px solid #dee2e6;
    padding: 2px 4px;
    white-space: nowrap;
    text-align: center;
}
table.toplevel tr.titles td {
    background: #e9ecef;
    color: #495057;
    font-weight: 600;
    font-size: 0.72rem;
    text-align: center;
    padding: 3px 4px;
    text-transform: uppercase;
    letter-spacing: 0.03em;
    border: 1px solid #ced4da;
}
table.pagelevel tr.odd td  { background: #fff; }
table.pagelevel tr.even td { background: #f8f9fa; }
table.pagelevel tr:hover td { background: #e8f4fd; }

</style>

<!-- Just in case the MemberStates/Commercial Users would try to access this URL! -->
<auth:if basePathKey="transferhistory.basepath" paths="/">
<auth:then>
</auth:then>
<auth:else>
<c:redirect url="/do/start"/>
</auth:else>
</auth:if>

<script>
function createAndSubmitDynamicForm(action,bcc,subject,body) {
        try{
                var form = document.createElement("form");
                form.setAttribute("target", "_parent");
                form.setAttribute("method", "post");
                form.setAttribute("enctype", "text/plain");
                form.setAttribute("action", action + '?from=' + escape('newops@ecmwf.int') + '&to=' + escape('operators@ecmwf.int') + '&bcc=' + bcc + '&subject=' + escape(subject) + '&body=' + body);
                document.body.appendChild(form);
                form.submit();
        }catch(e){
                showToast("Error: " + e, "danger");
        }
}

/* ── Horizontal / Vertical layout toggle ──
   Horizontal: destinations split across multiple side-by-side sub-tables (default)
   Vertical  : all destinations in one single continuous table (same columns, no side-by-side split)
*/
(function() {
    var origHTML = null;

    function buildSingleTable() {
        var tables = Array.from(document.querySelectorAll('table.pagelevel'));
        if (!tables.length) return null;

        var newTable = document.createElement('table');
        newTable.className = 'pagelevel';

        /* Take the header row from the first sub-table */
        var firstTitle = tables[0].querySelector('tr.titles');
        if (firstTitle) newTable.appendChild(firstTitle.cloneNode(true));

        /* Collect all data rows (skip title rows) from every sub-table */
        var rowIndex = 0;
        tables.forEach(function(tbl) {
            Array.from(tbl.rows).forEach(function(row) {
                if (row.classList.contains('titles')) return;
                var clone = row.cloneNode(true);
                /* Re-apply alternating stripe so the merged table looks consistent */
                clone.className = (rowIndex % 2 === 0) ? 'even' : 'odd';
                rowIndex++;
                newTable.appendChild(clone);
            });
        });

        /* Append a footer title row identical to the header */
        if (firstTitle) newTable.appendChild(firstTitle.cloneNode(true));

        return newTable;
    }

    window.toggleMonitorLayout = function() {
        var toplevel = document.querySelector('table.toplevel');
        var btn      = document.getElementById('btnLayoutToggle');
        if (!toplevel || !btn) return;

        var isHoriz = !toplevel.dataset.vertical;

        if (isHoriz) {
            origHTML = toplevel.outerHTML;
            var vTable = buildSingleTable();
            if (!vTable) return;
            var wrapper = document.createElement('table');
            wrapper.className = 'toplevel';
            wrapper.dataset.vertical = '1';
            var tr = wrapper.insertRow();
            var td = tr.insertCell();
            td.className = 'holder';
            td.appendChild(vTable);
            toplevel.replaceWith(wrapper);
            btn.innerHTML = '<i class="bi bi-layout-sidebar-inset"></i> Split';
            localStorage.setItem('monLayout', 'v');
        } else {
            var tmp = document.createElement('div');
            tmp.innerHTML = origHTML;
            document.querySelector('table.toplevel').replaceWith(tmp.firstChild);
            btn.innerHTML = '<i class="bi bi-layout-three-columns"></i> Single';
            localStorage.setItem('monLayout', 'h');
        }
        /* Re-attach sort handlers to newly built table */
        if (window.attachSortHandlers) window.attachSortHandlers();
    };

    document.addEventListener('DOMContentLoaded', function() {
        if (localStorage.getItem('monLayout') === 'v') {
            window.toggleMonitorLayout();
        }
    });
})();

/* ── Column sort ── */
(function() {
    var sortCol = -1;
    var sortAsc = true;
    var DEST_COLS = 7;

    function getCellKey(row, col) {
        var cell = row.cells[col];
        if (!cell) return '';
        switch (col) {
            case 0: /* Host — text */
            case 1: /* Dest — text */
                return (cell.querySelector('a') || cell).innerText.trim().toLowerCase();
            case 2: /* Outs — numeric; badge = count, check icon = 0 */
                var badge = cell.querySelector('.badge');
                return badge ? (parseInt(badge.innerText, 10) || 0) : 0;
            case 3: /* Q'd — numeric */
                return parseInt(cell.innerText.trim(), 10) || 0;
            case 4: /* Last Tr — use <a title> for full datetime, "None" sorts last */
            case 5: /* Last Er — same */
                var a = cell.querySelector('a');
                return a ? (a.title || 'ZZZ') : 'ZZZ';
            case 6: /* OV — extract numeric level from mon-dot class */
                var dot = cell.querySelector('.mon-dot');
                if (!dot) return 99;
                var mn = dot.className.match(/mon-dot-n(\d+)/);
                if (mn) return -parseInt(mn[1], 10);
                var mp = dot.className.match(/mon-dot-(\d+)/);
                return mp ? parseInt(mp[1], 10) : 99;
            default:
                return cell.innerText.trim().toLowerCase();
        }
    }

    function sortTable(col) {
        if (sortCol === col) { sortAsc = !sortAsc; } else { sortCol = col; sortAsc = true; }

        /* Collect data rows and remember how many are in each sub-table */
        var tables   = Array.from(document.querySelectorAll('table.pagelevel'));
        var sizes    = tables.map(function(t) {
            return Array.from(t.rows).filter(function(r) { return !r.classList.contains('titles'); }).length;
        });
        var allRows  = [];
        tables.forEach(function(t) {
            Array.from(t.rows).forEach(function(r) {
                if (!r.classList.contains('titles')) allRows.push(r);
            });
        });

        /* Sort */
        allRows.sort(function(a, b) {
            var ka = getCellKey(a, col), kb = getCellKey(b, col);
            var cmp = (typeof ka === 'number' && typeof kb === 'number')
                ? ka - kb
                : (ka < kb ? -1 : ka > kb ? 1 : 0);
            return sortAsc ? cmp : -cmp;
        });

        /* Redistribute rows back into their sub-tables, maintaining split sizes */
        var idx = 0;
        tables.forEach(function(tbl, ti) {
            /* Remove existing data rows — use r.remove() so it works whether the row's
               parent is <table> (DOM-built, single mode) or implicit <tbody> (HTML-parsed, split mode) */
            Array.from(tbl.rows).forEach(function(r) {
                if (!r.classList.contains('titles')) r.remove();
            });
            /* Find footer title row (last .titles) to insert before it */
            var titleRows = tbl.querySelectorAll('tr.titles');
            var footer    = titleRows.length > 1 ? titleRows[titleRows.length - 1] : null;
            var count     = sizes[ti];
            for (var i = 0; i < count && idx < allRows.length; i++, idx++) {
                var row = allRows[idx];
                row.className = (i % 2 === 0) ? 'odd' : 'even';
                /* footer.before(row) works regardless of whether rows sit in <tbody> or <table> */
                if (footer) { footer.before(row); } else { tbl.appendChild(row); }
            }
        });

        /* Update sort indicators on every title row */
        tables.forEach(function(tbl) {
            tbl.querySelectorAll('tr.titles').forEach(function(titleRow) {
                Array.from(titleRow.cells).slice(0, DEST_COLS).forEach(function(td, i) {
                    var ind = td.querySelector('.sort-ind');
                    if (ind) ind.remove();
                    if (i === col) {
                        var sp = document.createElement('span');
                        sp.className = 'sort-ind';
                        sp.style.fontSize = '0.65rem';
                        sp.innerHTML = sortAsc ? ' &#9650;' : ' &#9660;';
                        td.appendChild(sp);
                    }
                });
            });
        });
    }

    window.attachSortHandlers = function() {
        document.querySelectorAll('table.pagelevel tr.titles').forEach(function(titleRow) {
            Array.from(titleRow.cells).slice(0, DEST_COLS).forEach(function(td, i) {
                td.style.cursor = 'pointer';
                td.title = (td.getAttribute('title') || '') + (td.getAttribute('title') ? ' — ' : '') + 'Click to sort';
                td.onclick = function() { sortTable(i); };
            });
        });
    };

    document.addEventListener('DOMContentLoaded', window.attachSortHandlers);
})();
</script>

<tiles:insert page="./pds/monitoring/reload.jsp" />

<div class="d-flex flex-wrap align-items-center gap-1 mb-2 py-1 px-1" style="border-bottom:1px solid #dee2e6; font-size:0.82rem;">

  <tiles:insert page="./pds/monitoring/filter.jsp" />

  <span class="text-muted px-1">|</span>

  <a class="btn btn-sm btn-outline-secondary" href="/do/transfer/destination"
     title="Go to the List of Destinations">
    <i class="bi bi-diagram-3"></i> Destinations
  </a>
  <a class="btn btn-sm btn-outline-secondary" href="/do/transfer/host"
     title="Go to the List of Hosts">
    <i class="bi bi-hdd-network"></i> Transfer Hosts
  </a>

  <span class="text-muted px-1">|</span>

  <a class="btn btn-sm btn-outline-secondary"
     href="?type=9|10|11|12|13|14|15|16|17|19|21|22|24|25|26|28|29|30&status=&network=&"
     title="Filter only Dissemination Destinations">
    <i class="bi bi-broadcast"></i> Dissemination
  </a>
  <a class="btn btn-sm btn-outline-secondary"
     href="?type=0|1|2|3|4|5|6|7|8|18|20|21|22|23|27&status=&network=&"
     title="Filter only Acquisition Destinations">
    <i class="bi bi-cloud-download"></i> Acquisition
  </a>

  <span class="text-muted px-1">|</span>

  <form action="/do/monitoring" method="get" class="d-flex align-items-center gap-0 mb-0">
    <div class="input-group input-group-sm" style="width:210px;">
      <span class="input-group-text" title="Filter by application name"><i class="bi bi-app"></i></span>
      <input type="text" name="application" class="form-control"
             placeholder="Application name"
             value="${param.application != null ? param.application : ''}"/>
      <button type="submit" class="btn btn-primary" title="Apply application filter">
        <i class="bi bi-search"></i>
      </button>
    </div>
  </form>

  <span class="text-muted px-1">|</span>

  <a class="btn btn-sm btn-outline-secondary" href="/maps/maps.html"
     title="Show <%=System.getProperty("monitor.nickName")%> Destination Hosts on OpenStreetMap">
    <i class="bi bi-map"></i> Map
  </a>

  <span class="text-muted px-1">|</span>

  <button id="btnLayoutToggle" class="btn btn-sm btn-outline-secondary"
          onclick="toggleMonitorLayout()"
          title="Single: all rows in one table. Split: split into side-by-side tables.">
    <i class="bi bi-layout-three-columns"></i> Single
  </button>

</div>

<table class="toplevel">
	<tr>
		<td class="holder">
		<table class="pagelevel">
			<tr class="titles">
				<tiles:insert
					page="./pds/monitoring/top_level/destination_titles.jsp" />
				<tiles:insert page="./pds/monitoring/top_level/product_titles.jsp" />
			</tr>

			<% boolean odd = false;	%>

			<c:forEach var="d" items="${reqData.destinations}"
				varStatus="fStatus">
				<c:set var="destStatus" value="${reqData.status[d.name]}" />

				<% odd = !odd; %>

				<tr class='<%=(odd?"odd":"even")%>'>

					<td><c:set var="primaryHost" value="${destStatus.primaryHost}" />
					<a
						title="${primaryHost.networkName}: (P:${primaryHost.name},C:${destStatus.currentlyUsedHostName})"
						href="/do/transfer/host/${primaryHost.name}">${primaryHost.networkCode}</a><c:if test="${not destStatus.usingPrimaryHost}">*</c:if></td>

					<td style="text-align: right;"><c:if test="${destStatus.queueSize > 0}">
						<c:set var="statusFilter" value="WAIT" />
					</c:if> <c:if test="${destStatus.queueSize == 0}">
						<c:set var="statusFilter" value="DONE" />
					</c:if> <a style="font-weight:bold" href="/do/transfer/destination/${d.name}?status=${statusFilter}&dataStream=All&disseminationStream=All&fileNameSearch=&date=<content:content name='monSesForm.updated' dateFormatKey='date.format.iso'/>"
						title="${d.comment}">${d.name}</a></td>

					<td><c:if test="${destStatus.badDataTransfersSize gt 0}">
						<a href="/do/monitoring/unsuccessful/${d.name}"
						   title="${d.name}: ${destStatus.badDataTransfersSize} outstanding transfer(s)">
						   <span class="badge rounded-pill bg-danger" style="font-size:0.68rem;">${destStatus.badDataTransfersSize}</span>
						</a>
					</c:if><c:if test="${destStatus.badDataTransfersSize == 0}">
						<i class="bi bi-check2 text-success" style="font-size:0.85rem;"
						   title="${d.name}: no outstanding transfers"></i>
					</c:if></td>

					<td>${destStatus.queueSize}</td>

					<td><c:set var="transL" value="${d.lastTransfer}" /> <c:if
						test="${not empty transL}">
						<c:catch>
							<a title="${transL.realFinishTime}"
								href="/do/transfer/data/${transL.id}"><content:content
								name="transL.realFinishTime"
								dateFormatKey="date.format.time.short" defaultValue="Not Set"
								ignoreNull="true" /></a>
						</c:catch>
					</c:if> <c:if test="${empty transL}">
						<i>None</i>
					</c:if></td>

					<td><c:set var="transE" value="${d.lastError}" /> <c:if
						test="${not empty transE}">
						<c:catch>
							<a title="${transE.failedTime}"
								href="<bean:message key="datatransfer.basepath"/>/${transE.id}"><content:content
								name="transE.failedTime" dateFormatKey="date.format.time.short"
								defaultValue="Not Set" ignoreNull="true" /></a>
						</c:catch>
					</c:if> <c:if test="${empty transE}">
						<i>None</i>
					</c:if></td>

					<td><span class="mon-dot mon-dot-${destStatus.bigSisterStatus lt 0 ? 'n1' : destStatus.bigSisterStatus}"
						title="${empty destStatus.bigSisterStatusComment ? 'OV disabled' : 'OV Status: '.concat(destStatus.bigSisterStatusComment)}"></span></td>

					<!-- End Destination Info -->

					<!-- Per Product Info -->

					<c:forEach var="productStatus" items="${reqData.productWindow}">

						<c:set var="key"
							value="${d.name}@${productStatus.product}@${productStatus.time}" />
						<c:set var="status" value="${reqData.status[key]}" />

						<c:if test="${not empty status}">
							<c:set var="genStatus" value="${status.generationStatus}" />
							<c:set var="arrStatus" value="${status.arrivalStatus}" />
							<c:set var="tranStatus" value="${status.realTimeTransferStatus}" />

							<c:if test="${genStatus == '1'}">
							<td><a class="mon-letter mon-letter-s${arrStatus lt 0 ? '0' : arrStatus}"
								href="/do/monitoring/arrival/${d.name}/${productStatus.product}/${productStatus.time}?date=<content:content name="productStatus.productTime" ignoreNull="true" defaultValue="" dateFormatKey="date.format.iso"/>"
								title="${d.name}: ${productStatus.time}-${productStatus.product} Arrival">a</a></td>
							<td><a class="mon-letter mon-letter-s${tranStatus lt 0 ? '0' : tranStatus}"
								href="/do/monitoring/transfer/${d.name}/${productStatus.product}/${productStatus.time}?date=<content:content name="productStatus.productTime" ignoreNull="true" defaultValue="" dateFormatKey="date.format.iso"/>"
								title="${d.name}: ${productStatus.time}-${productStatus.product} Transfer">t</a></td>
							</c:if>
							<c:if test="${genStatus != '1'}">
								<td>&nbsp;</td>
								<td>&nbsp;</td>
							</c:if>
						</c:if>
						<c:if test="${empty status}">
							<td>&nbsp;</td>
							<td>&nbsp;</td>
						</c:if>

					</c:forEach>


					<!-- End of Per Product Info -->


				</tr>

				<c:if test="${(fStatus.index % reqData.stepsPerColumn) == (reqData.stepsPerColumn - 1) && not fStatus.last}">
					<tr class="titles">
						<tiles:insert page="./pds/monitoring/top_level/destination_titles.jsp" />
						<tiles:insert page="./pds/monitoring/top_level/product_titles.jsp" />
					</tr>
		</table>
		</td>

		<td class="holder">
		<table class="pagelevel">
			<tr class="titles">
				<tiles:insert
					page="./pds/monitoring/top_level/destination_titles.jsp" />
				<tiles:insert page="./pds/monitoring/top_level/product_titles.jsp" />
			</tr>
			</c:if>

			</c:forEach>


			<tr class="titles">
				<tiles:insert
					page="./pds/monitoring/top_level/destination_titles.jsp" />
				<tiles:insert page="./pds/monitoring/top_level/product_titles.jsp" />
			</tr>
		</table>

		</td>
	</tr>
</table>