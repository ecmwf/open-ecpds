<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<script>
	var batchTransfers = {};
	var totalInSelection = 0;

	function updateBatchButtons() {
		var anySelected = false;
		for (var id in batchTransfers) {
			if (batchTransfers[id] === true) { anySelected = true; break; }
		}
		document.querySelectorAll('.batch-action-btn').forEach(function (btn) {
			btn.disabled = !anySelected;
		});
	}

	function checkAll(all, reverse) {
		var destName = (document.getElementById('vl-dest-name') || {}).value || '';
		$.getJSON('/do/transfer/destination', { json: 'basketIdList', destinationName: destName }, function(json) {
			var ids = (json && json.ids) ? json.ids.map(String) : [];
			// For None: also clear any extra entries already in batchTransfers (from page visits)
			if (!all && !reverse) {
				Object.keys(batchTransfers).forEach(function(id) { batchTransfers[id] = false; });
			}
			ids.forEach(function(id) {
				if (all) {
					batchTransfers[id] = true;
				} else if (reverse) {
					// undefined and true both mean "currently selected"; false means "deselected".
					// Invert: selected → deselected, deselected → selected.
					batchTransfers[id] = batchTransfers[id] === false;
				} else {
					batchTransfers[id] = false;
				}
			});
			// Refresh visual state of current page rows
			$('#validateTable tbody tr').each(function () {
				var span = $(this).find('span.batch-select');
				if (!span.length) return;
				var id = String(span.data('transfer-id') || span.data('transferId'));
				var tr = $(this);
				var icon = span.find('i');
				if (batchTransfers[id]) {
					tr.addClass('selected');
					icon.removeClass('bi-star').addClass('bi-star-fill text-warning');
				} else {
					tr.removeClass('selected');
					icon.removeClass('bi-star-fill text-warning').addClass('bi-star');
				}
			});
			// Recount
			var count = 0;
			Object.keys(batchTransfers).forEach(function(id) { if (batchTransfers[id]) count++; });
			totalInSelection = count;
			var el = document.getElementById('validateTransferTotal');
			if (el) el.textContent = '(' + count + ' in selection)';
			updateBatchButtons();
		});
	}

	function select(el, id) {
		var isOn = batchTransfers[id] ? false : true;
		batchTransfers[id] = isOn;
		var tr  = $(el).closest('tr');
		var icon = $(el).find('i');
		if (isOn) {
			tr.addClass('selected');
			icon.removeClass('bi-star').addClass('bi-star-fill text-warning');
			totalInSelection++;
		} else {
			tr.removeClass('selected');
			icon.removeClass('bi-star-fill text-warning').addClass('bi-star');
			totalInSelection = Math.max(0, totalInSelection - 1);
		}
		var el = document.getElementById('validateTransferTotal');
		if (el) el.textContent = '(' + totalInSelection + ' in selection)';
		updateBatchButtons();
		_vlUpdateHdrStar();
	}

	function _vlUpdateHdrStar() {
		var hdr = document.getElementById('vl-hdr-star-icon');
		if (!hdr) return;
		var rows = document.querySelectorAll('#validateTable tbody tr');
		if (!rows.length) { hdr.className = 'bi bi-star'; return; }
		var allSelected = true;
		rows.forEach(function (tr) {
			var span = tr.querySelector('span.batch-select');
			if (!span) { allSelected = false; return; }
			var id = String(span.dataset.transferId || span.getAttribute('data-transfer-id'));
			if (!batchTransfers[id]) allSelected = false;
		});
		hdr.className = allSelected ? 'bi bi-star-fill text-warning' : 'bi bi-star';
	}

	function _vlTogglePageSelection() {
		var rows = document.querySelectorAll('#validateTable tbody tr');
		if (!rows.length) return;
		var allSelected = true;
		rows.forEach(function (tr) {
			var span = tr.querySelector('span.batch-select');
			if (!span) { allSelected = false; return; }
			var id = String(span.dataset.transferId || span.getAttribute('data-transfer-id'));
			if (!batchTransfers[id]) allSelected = false;
		});
		rows.forEach(function (tr) {
			var span = tr.querySelector('span.batch-select');
			if (!span) return;
			var id = String(span.dataset.transferId || span.getAttribute('data-transfer-id'));
			var isOn = batchTransfers[id] ? true : false;
			if (allSelected ? isOn : !isOn) select(span, id);
		});
	}

	function transferChange(operation, transfer) {
	    var form = document.validateActionForm;
	    if (operation === 'delete' || operation === 'stop' || operation === 'requeue') {
	        var msg;
	        if (transfer) {
	            msg = 'Are you sure you want to ' + operation + ' transfer ' + transfer + '?';
	        } else {
	            var count = totalInSelection;
	            msg = 'Are you sure you want to ' + operation + ' ' + count + ' selected data transfer' + (count !== 1 ? 's' : '') + '?';
	        }
	        confirmationDialog({
	            title: 'Please Confirm',
	            message: msg,
	            onConfirm: function () {
	                var prefix = (operation === 'delete') ? 'deletions' : 'operations';
	                if (transfer) {
	                    form.action = '/do/transfer/destination/' + prefix
	                            + '/${destinationDetailActionForm.id}/' + operation + '/' + transfer;
	                } else {
	                    form.action = '/do/transfer/destination/' + prefix
	                            + '/${destinationDetailActionForm.id}/' + operation;
	                }
	                form.submit();
	            }
	        });
	        return;
	    }
	    var prefix = (operation === 'delete') ? 'deletions' : 'operations';
	    if (transfer) {
	        form.action = '/do/transfer/destination/' + prefix
	                + '/${destinationDetailActionForm.id}/' + operation + '/' + transfer;
	    } else {
	        form.action = '/do/transfer/destination/' + prefix
	                + '/${destinationDetailActionForm.id}/' + operation;
	    }
	    form.submit();
	}

	function submitEnter(field, e) {
		var keycode = window.event ? window.event.keyCode : (e ? e.which : 0);
		if (keycode === 13) { field.form.submit(); return false; }
		return true;
	}
</script>

<%-- Auth check for queue operations --%>
<auth:if basePathKey="destination.basepath"
    paths="/operations/${destinationDetailActionForm.id}/requeue/">
    <auth:then>
        <c:set var="ecpdsCanHandleQueue" value="true" scope="request" />
    </auth:then>
</auth:if>

<%-- Main form: used by all global-action submits and the DataTable --%>
<form id="validateActionForm" name="validateActionForm" method="GET"
      action="/do/transfer/destination/operations/${destinationDetailActionForm.id}/validate">
    <input type="hidden" name="status"               value="${destinationDetailActionForm.status}">
    <input type="hidden" name="dataStream"            value="${destinationDetailActionForm.dataStream}">
    <input type="hidden" name="dataTime"              value="${destinationDetailActionForm.dataTime}">
    <input type="hidden" name="disseminationStream"   value="${destinationDetailActionForm.disseminationStream}">
    <input type="hidden" name="from"                  value="selection">

    <%-- Hidden inputs carrying DataTable state --%>
    <input type="hidden" id="vl-dest-name"  value="${destinationDetailActionForm.id}">
    <input type="hidden" id="vl-can-queue"  value="${not empty ecpdsCanHandleQueue ? 'true' : 'false'}">

    <%-- Error message shown by drawCallback --%>
    <div id="validateTableError" class="alert alert-warning mt-2 mb-0" style="display:none"></div>

    <%-- Entries-per-page + column mode --%>
    <div class="d-flex align-items-center justify-content-between gap-2 my-2">
        <div class="d-flex align-items-center gap-2">
            <div class="input-group flex-nowrap" style="width:auto" title="Page size">
                <span class="input-group-text px-2"><i class="bi bi-list-ol"></i></span>
                <select id="validatePageLen" class="form-select" style="width:auto">
                    <option value="10">10</option>
                    <option value="25">25</option>
                    <option value="50">50</option>
                    <option value="100">100</option>
                    <option value="250">250</option>
                </select>
            </div>
            <span id="validateTransferTotal" class="text-muted small ms-2"></span>
        </div>
        <div class="dropdown">
            <button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button" id="vlColModeBtn" data-bs-toggle="dropdown" data-bs-auto-close="outside" data-bs-boundary="viewport" aria-expanded="false">
                <i class="bi bi-layout-three-columns me-1"></i>Auto
            </button>
            <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="vlColModeBtn">
                <li><a class="dropdown-item active" href="#" data-col-mode="auto">
                    <i class="bi bi-check me-1"></i><strong>Auto</strong>
                    <small class="d-block text-muted ms-4">Adapts to screen width</small>
                </a></li>
                <li><a class="dropdown-item" href="#" data-col-mode="all">
                    <strong>All</strong>
                    <small class="d-block text-muted ms-0">All columns visible</small>
                </a></li>
                <li><a class="dropdown-item" href="#" data-col-mode="compact">
                    <strong>Compact</strong>
                    <small class="d-block text-muted ms-0">Hides: Err, Host, Sched., TS, %, Mbits/s, Prior</small>
                </a></li>
                <li><a class="dropdown-item" href="#" data-col-mode="small">
                    <strong>Small</strong>
                    <small class="d-block text-muted ms-0">Shows only: Target, Status, Actions, Select</small>
                </a></li>
                <li><hr class="dropdown-divider"></li>
                <li><a class="dropdown-item" href="#" data-col-mode="custom">
                    <strong>Custom</strong>
                    <small class="d-block text-muted ms-0">Choose individual columns</small>
                </a></li>
                <li id="vlCustomColChkPanel" style="display:none;">
                  <div class="px-3 py-2 d-flex flex-column gap-1" style="min-width:160px;">
                    <div class="form-check mb-0"><input class="form-check-input vl-custom-col-chk" type="checkbox" id="vlchk-0"  data-col="0"  checked><label class="form-check-label" for="vlchk-0">Err</label></div>
                    <div class="form-check mb-0"><input class="form-check-input vl-custom-col-chk" type="checkbox" id="vlchk-1"  data-col="1"  checked><label class="form-check-label" for="vlchk-1">Host</label></div>
                    <div class="form-check mb-0"><input class="form-check-input vl-custom-col-chk" type="checkbox" id="vlchk-2"  data-col="2"  checked><label class="form-check-label" for="vlchk-2">Sched. Time</label></div>
                    <div class="form-check mb-0"><input class="form-check-input vl-custom-col-chk" type="checkbox" id="vlchk-3"  data-col="3"  checked><label class="form-check-label" for="vlchk-3">Start Time</label></div>
                    <div class="form-check mb-0"><input class="form-check-input vl-custom-col-chk" type="checkbox" id="vlchk-4"  data-col="4"  checked><label class="form-check-label" for="vlchk-4">Finish Time</label></div>
                    <div class="form-check mb-0"><input class="form-check-input vl-custom-col-chk" type="checkbox" id="vlchk-5"  data-col="5"  checked disabled><label class="form-check-label text-muted" for="vlchk-5">Target <small>(required)</small></label></div>
                    <div class="form-check mb-0"><input class="form-check-input vl-custom-col-chk" type="checkbox" id="vlchk-6"  data-col="6"  checked><label class="form-check-label" for="vlchk-6">TS</label></div>
                    <div class="form-check mb-0"><input class="form-check-input vl-custom-col-chk" type="checkbox" id="vlchk-7"  data-col="7"  checked><label class="form-check-label" for="vlchk-7">%</label></div>
                    <div class="form-check mb-0"><input class="form-check-input vl-custom-col-chk" type="checkbox" id="vlchk-8"  data-col="8"  checked><label class="form-check-label" for="vlchk-8">Mbits/s</label></div>
                    <div class="form-check mb-0"><input class="form-check-input vl-custom-col-chk" type="checkbox" id="vlchk-9"  data-col="9"       ><label class="form-check-label" for="vlchk-9">Size</label></div>
                    <div class="form-check mb-0"><input class="form-check-input vl-custom-col-chk" type="checkbox" id="vlchk-10" data-col="10" checked><label class="form-check-label" for="vlchk-10">Status</label></div>
                    <div class="form-check mb-0"><input class="form-check-input vl-custom-col-chk" type="checkbox" id="vlchk-11" data-col="11" checked><label class="form-check-label" for="vlchk-11">Prior</label></div>
                    <c:if test="${not empty ecpdsCanHandleQueue}">
                    <div class="form-check mb-0"><input class="form-check-input vl-custom-col-chk" type="checkbox" id="vlchk-12" data-col="12" checked disabled><label class="form-check-label text-muted" for="vlchk-12">Actions <small>(required)</small></label></div>
                    </c:if>
                    <div class="form-check mb-0"><input class="form-check-input vl-custom-col-chk" type="checkbox" id="vlchk-13" data-col="13" checked disabled><label class="form-check-label text-muted" for="vlchk-13">Select <small>(required)</small></label></div>
                  </div>
                </li>
            </ul>
        </div>
    </div>

    <%-- DataTable --%>
    <table id="validateTable" class="table table-sm table-hover w-100">
        <thead class="table-light">
            <tr>
                <th>Err</th>
                <th>Host</th>
                <th title="Scheduled Time (UTC)">Sched. Time</th>
                <th title="Start Time (UTC)">Start Time</th>
                <th title="Finish Time (UTC)">Finish Time</th>
                <th>Target</th>
                <th>TS</th>
                <th>%</th>
                <th>Mbits/s</th>
                <th>Size</th>
                <th>Status</th>
                <th>Prior</th>
                <th>Actions</th>
                <th style="cursor:pointer;white-space:nowrap" title="Click to select/unselect all transfers on this page" onclick="_vlTogglePageSelection()"><i id="vl-hdr-star-icon" class="bi bi-star"></i></th>
            </tr>
        </thead>
    </table>

    <%-- A/N/R controls --%>
    <div class="d-flex flex-wrap align-items-center gap-2 mt-2 mb-1">

        <div class="btn-group btn-group-sm">
            <button type="button" class="btn btn-outline-secondary" onclick="checkAll(true,false)" title="Select all for batch"><i class="bi bi-check-all me-1"></i>All</button>
            <button type="button" class="btn btn-outline-secondary" onclick="checkAll(false,false)" title="Deselect all from batch"><i class="bi bi-dash me-1"></i>None</button>
            <button type="button" class="btn btn-outline-secondary" onclick="checkAll(false,true)" title="Invert batch selection"><i class="bi bi-arrow-left-right me-1"></i>Invert</button>
        </div>

        <%-- Global actions --%>
        <c:if test="${not empty ecpdsCanHandleQueue}">
            <div class="vr"></div>
            <c:choose>
              <c:when test="${not empty destination and not destination.hasActiveDisseminationHosts}">
                <span data-bs-toggle="tooltip" title="Requeue unavailable: no active dissemination hosts for this destination" class="d-inline-block">
                  <button type="button" class="btn btn-outline-secondary btn-sm" disabled style="pointer-events:none;"><i class="bi bi-arrow-repeat me-1"></i>Requeue</button>
                </span>
              </c:when>
              <c:otherwise>
                <button type="button" class="btn btn-outline-primary btn-sm batch-action-btn" onclick="transferChange('requeue')" title="Requeue batch-selected"><i class="bi bi-arrow-repeat me-1"></i>Requeue</button>
              </c:otherwise>
            </c:choose>
            <button type="button" class="btn btn-outline-danger btn-sm batch-action-btn"  onclick="transferChange('stop')"    title="Stop batch-selected"><i class="bi bi-stop-circle me-1"></i>Stop</button>
            <auth:if basePathKey="destination.basepath" paths="/deletions/${destinationDetailActionForm.id}/delete">
                <auth:then>
                    <button type="button" class="btn btn-outline-danger btn-sm batch-action-btn" onclick="transferChange('delete')" title="Delete batch-selected"><i class="bi bi-trash me-1"></i>Delete</button>
                </auth:then>
            </auth:if>

            <div class="vr"></div>
            <div class="btn-group btn-group-sm">
                <button type="button" class="btn btn-outline-secondary batch-action-btn" onclick="transferChange('increaseTransferPriority')" title="Increase priority of selected"><i class="bi bi-arrow-up me-1"></i>Priority</button>
                <button type="button" class="btn btn-outline-secondary batch-action-btn" onclick="transferChange('decreaseTransferPriority')" title="Decrease priority of selected"><i class="bi bi-arrow-down"></i></button>
            </div>
            <div class="input-group input-group-sm" style="width:auto">
                <input type="number" id="newPriorityInput" name="newPriority"
                       class="form-control form-control-sm text-center"
                       style="width:4rem" min="0" max="99"
                       value="${destinationDetailActionForm.newPriority}"
                       onkeypress="submitEnter(this,event)">
                <button type="button" class="btn btn-outline-secondary batch-action-btn" onclick="transferChange('setTransferPriority')" title="Set priority of selected to this value"><i class="bi bi-check2 me-1"></i>Set</button>
            </div>

            <div class="vr"></div>
            <button type="button" class="btn btn-outline-secondary btn-sm batch-action-btn" onclick="transferChange('extendLifetime')" title="Extend lifetime"><i class="bi bi-clock-history me-1"></i>Extend</button>
            <button type="button" class="btn btn-outline-secondary btn-sm" onclick="transferChange('clean')"          title="Clear selection"><i class="bi bi-eraser me-1"></i>Clean</button>
        </c:if>

        <div class="vr"></div>
        <button type="button" class="btn btn-outline-secondary btn-sm" onclick="transferChange('cancel')" title="Back to destination"><i class="bi bi-arrow-left me-1"></i>Back</button>

        <button class="btn btn-link btn-sm text-muted p-0 ms-1" type="button"
                data-bs-toggle="collapse" data-bs-target="#vlActionHelp"
                aria-expanded="false" title="How to use these actions">
            <i class="bi bi-info-circle"></i>
        </button>
    </div>

    <div class="collapse mt-1" id="vlActionHelp">
        <div class="card card-body py-2 px-3" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top:3px solid var(--bs-primary,#0d6efd);">
            <strong class="d-block mb-1">Using bulk actions on the selection basket</strong>
            <p class="mb-1">This page shows the transfers currently in your <em>selection basket</em>. Use the controls above to act on them:</p>
            <ul class="mb-1 ps-3">
                <li><strong>All / None / Invert</strong> &mdash; select, deselect, or invert the row-level checkboxes <em>across all pages</em> to refine which transfers a bulk action will affect.</li>
                <li><strong>Requeue</strong> &mdash; re-submit all checked transfers for delivery.</li>
                <li><strong>Stop</strong> &mdash; interrupt the transfer of all checked entries.</li>
                <li><strong>Delete</strong> &mdash; permanently remove all checked transfers.</li>
                <li><strong>Priority <i class="bi bi-arrow-up"></i> / <i class="bi bi-arrow-down"></i></strong> &mdash; increase or decrease the scheduling priority of all checked transfers by one step.</li>
                <li><strong>Set</strong> &mdash; assign the exact priority value entered in the number box to all checked transfers.</li>
                <li><strong>Extend</strong> &mdash; extend the expiry lifetime of all checked transfers.</li>
                <li><strong>Clean</strong> &mdash; clear the entire basket and return to an empty selection.</li>
                <li><strong>Back</strong> &mdash; return to the destination page without changing the basket.</li>
            </ul>
            <p class="mb-0 text-muted">Actions apply to <em>checked</em> rows across all pages. Use <strong>All</strong> first if you want every basket entry to be affected.</p>
        </div>
    </div>

    <%-- Messages --%>
    <tiles:insert page="./pds/transfer/destination/data/messages.jsp" />

</form>

<script>
(function () {
    var STORAGE_KEY = 'validateTransferPageLen';
    var savedLen = parseInt(localStorage.getItem(STORAGE_KEY), 10) || 25;
    var sel = document.getElementById('validatePageLen');
    sel.value = String(savedLen);
    if (!sel.value) { sel.value = '25'; savedLen = 25; }

    var canQueue = document.getElementById('vl-can-queue').value === 'true';
    var destName = document.getElementById('vl-dest-name').value;

    // Suppress DataTables' native alert() for server errors; drawCallback shows them inline.
    $.fn.dataTable.ext.errMode = function () {};

    var table = $('#validateTable').DataTable({
        serverSide: true,
        processing: true,
        pageLength: savedLen,
        lengthChange: false,
        searching: true,
        dom: 'rt<"d-flex justify-content-between align-items-center mt-2"ip>',
        autoWidth: false,
        ordering: true,
        order: [],
        ajax: {
            url: '/do/transfer/destination?json=validateList',
            type: 'GET',
            data: function (d) {
                d.destinationName = destName;
            }
        },
        columns: [
            { data: 0,  width: '40px'  },
            { data: 1,  width: '110px' },
            { data: 2,  width: '130px' },
            { data: 3,  width: '130px' },
            { data: 4,  width: '130px' },
            { data: 5 },
            { data: 6,  width: '55px'  },
            { data: 7,  width: '45px'  },
            { data: 8,  width: '70px'  },
            { data: 9,  width: '80px'  },
            { data: 10, width: '95px'  },
            { data: 11, width: '45px'  },
            { data: 12, width: '95px'  },
            { data: 13, width: '55px'  }
        ],
        columnDefs: [
            { targets: 5,                              className: 'col-target' },
            { targets: [0, 6, 7, 8, 9, 10, 11, 12, 13], className: 'text-nowrap' },
            { targets: [12, 13], orderable: false },
            { targets: 9,  visible: false },
            { targets: 12, orderable: false, visible: canQueue },
            { targets: 13, orderable: false }
        ],
        drawCallback: function (settings) {
            var json = settings.json;
            // Populate batchTransfers from server (all visible = default batch-on)
            if (json && json.selectedIds && json.selectedIds.length) {
                json.selectedIds.forEach(function (id) {
                    if (batchTransfers[String(id)] !== false) {
                        batchTransfers[String(id)] = true;
                    }
                });
            }
            // Restore visual star state
            $('#validateTable tbody tr').each(function () {
                var span = $(this).find('span.batch-select');
                if (span.length) {
                    var id = String(span.data('transfer-id') || span.data('transferId'));
                    var icon = span.find('i');
                    if (batchTransfers[id]) {
                        $(this).addClass('selected');
                        icon.removeClass('bi-star').addClass('bi-star-fill text-warning');
                    } else {
                        $(this).removeClass('selected');
                        icon.removeClass('bi-star-fill text-warning').addClass('bi-star');
                    }
                }
            });
            // Error display
            var errorDiv = document.getElementById('validateTableError');
            if (json && json.error) {
                function esc(s) { return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;'); }
                errorDiv.innerHTML = '<strong>Error:<\/strong> ' + esc(json.error);
                errorDiv.style.display = 'block';
            } else {
                errorDiv.style.display = 'none';
            }
            // Total count — adjust server total by any local deselections not yet submitted
            if (json && typeof json.recordsTotal !== 'undefined') {
                var localFalse = Object.keys(batchTransfers).filter(function(id) { return batchTransfers[id] === false; }).length;
                totalInSelection = Math.max(0, json.recordsTotal - localFalse);
                document.getElementById('validateTransferTotal').textContent =
                    '(' + totalInSelection + ' in selection)';
            }
            updateBatchButtons();
            _vlUpdateHdrStar();
        }
    });

    document.getElementById('validatePageLen').addEventListener('change', function () {
        var len = parseInt(this.value, 10);
        localStorage.setItem(STORAGE_KEY, len);
        table.page.len(len).draw();
    });

    window._validateTable = table;

    // Column-mode dropdown
    var VL_CUSTOM_COL_KEY = 'validateCustomCols';
    var VL_COL_MODE_KEY   = 'validateColMode';
    var _vlCustomCols = (function() {
        try { var s = localStorage.getItem(VL_CUSTOM_COL_KEY); if (s) return JSON.parse(s); } catch(e) {}
        return [0,1,2,3,4,5,6,7,8,10,11<c:if test="${not empty ecpdsCanHandleQueue}">,12</c:if>,13]; // Size(9) hidden by default
    })();
    var _vlColMode = (function() {
        try { return localStorage.getItem(VL_COL_MODE_KEY) || 'auto'; } catch(e) { return 'auto'; }
    })();
    var _VL_MED = [0, 1, 2, 6, 7, 8, 11];
    var _VL_SM  = [3, 4];
    var _VL_AUTO_ALWAYS_HIDE = [9];
    var _VL_COMPACT_HIDE = [0].concat(_VL_MED.filter(function(c){return c!==0;}));
    var _VL_SMALL_HIDE   = _VL_COMPACT_HIDE.concat(_VL_SM);

    function _vlShowCols(hideCols) {
        table.columns().every(function (i) {
            var vis = hideCols.indexOf(i) === -1;
            if (i === 12 && !canQueue) vis = false;
            table.column(i).visible(vis, false);
        });
        table.columns.adjust();
    }

    function _vlApplyCustomCols() {
        table.columns().every(function (i) {
            var vis = _vlCustomCols.indexOf(i) !== -1;
            if (i === 5 || i === 13) vis = true;  // Target, Select: mandatory
            if (i === 12) vis = canQueue;           // Actions: mandatory if canQueue
            table.column(i).visible(vis, false);
        });
        table.columns.adjust();
    }

    function _vlApplyAuto() {
        if (_vlColMode !== 'auto') return;
        var w = window.innerWidth;
        if (w < 768)      _vlShowCols(_VL_AUTO_ALWAYS_HIDE.concat(_VL_MED).concat(_VL_SM));
        else if (w < 992) _vlShowCols(_VL_AUTO_ALWAYS_HIDE.concat(_VL_MED));
        else              _vlShowCols(_VL_AUTO_ALWAYS_HIDE);
    }

    function _vlSyncCustomChkBoxes() {
        document.querySelectorAll('.vl-custom-col-chk').forEach(function(chk) {
            chk.checked = _vlCustomCols.indexOf(+chk.dataset.col) !== -1;
        });
    }

    document.querySelectorAll('.vl-custom-col-chk').forEach(function(chk) {
        chk.addEventListener('change', function() {
            var col = +this.dataset.col;
            var idx = _vlCustomCols.indexOf(col);
            if (this.checked && idx === -1) _vlCustomCols.push(col);
            else if (!this.checked && idx !== -1) _vlCustomCols.splice(idx, 1);
            try { localStorage.setItem(VL_CUSTOM_COL_KEY, JSON.stringify(_vlCustomCols)); } catch(e) {}
            if (_vlColMode === 'custom') _vlApplyCustomCols();
        });
    });

    function _vlApplyMode(mode) {
        var label = mode.charAt(0).toUpperCase() + mode.slice(1);
        $('#vlColModeBtn').html('<i class="bi bi-layout-three-columns me-1"></i>' + label);
        if (mode === 'auto') {
            $('#vlColModeBtn').removeClass('btn-primary').addClass('btn-outline-secondary');
        } else {
            $('#vlColModeBtn').removeClass('btn-outline-secondary').addClass('btn-primary');
        }
        document.getElementById('vlCustomColChkPanel').style.display = (mode === 'custom') ? '' : 'none';
        $('#vlColModeBtn').closest('.dropdown').find('.dropdown-item').each(function() {
            $(this).find('i.bi-check').remove();
            if ($(this).data('col-mode') === mode) $(this).prepend('<i class="bi bi-check me-1"></i>');
        });
        if (mode === 'auto')         _vlApplyAuto();
        else if (mode === 'all')     _vlShowCols([]);
        else if (mode === 'compact') _vlShowCols(_VL_COMPACT_HIDE);
        else if (mode === 'small')   _vlShowCols(_VL_SMALL_HIDE);
        else if (mode === 'custom')  { _vlSyncCustomChkBoxes(); _vlApplyCustomCols(); }
    }

    $(window).on('resize', _vlApplyAuto);
    _vlApplyMode(_vlColMode);

    $('#vlColModeBtn').closest('.dropdown').find('.dropdown-item').on('click', function (e) {
        e.preventDefault();
        var mode = $(this).data('col-mode');
        if (!mode) return;
        _vlColMode = mode;
        try { localStorage.setItem(VL_COL_MODE_KEY, mode); } catch(e) {}
        _vlApplyMode(mode);
    });

    // Sync helper: POST any pending batchTransfers changes, then call callback.
    function _vlSyncBasket(callback) {
        var onIds = [], offIds = [];
        Object.keys(batchTransfers).forEach(function (id) {
            (batchTransfers[id] ? onIds : offIds).push(id);
        });
        $.ajax({
            url: '/do/transfer/destination?json=syncSelection',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                destinationName: destName,
                type: 'basketAction',
                on: onIds.join(','),
                off: offIds.join(',')
            }),
            success: callback,
            error: function () {
                var errorDiv = document.getElementById('validateTableError');
                if (errorDiv) {
                    errorDiv.innerHTML = '<strong>Error:</strong> Failed to sync basket state with the server. Please try again.';
                    errorDiv.style.display = 'block';
                }
            }
        });
    }

    // Intercept "remove from basket" (red-cross) link clicks: sync pending star-deselections
    // first so they are not forgotten when the page navigates away.
    $(document).on('click', 'a[href*="/unselectTransfer/"]', function (e) {
        var hasPending = Object.keys(batchTransfers).some(function (id) { return batchTransfers[id] === false; });
        if (!hasPending) return; // nothing pending — let the browser navigate normally
        e.preventDefault();
        var url = this.href;
        _vlSyncBasket(function () { window.location.href = url; });
    });

    // Form submit hook: pre-sync basket action state as a JSON body POST before submitting.
    // This avoids injecting thousands of actionTransfer/selectedTransfer params into the GET URL,
    // which would cause HTTP 414 (URI Too Long). The JSON body bypasses Jetty's form size limits.
    // Unvisited basket pages are not in batchTransfers but remain "on" in the server session.
    var frm = document.validateActionForm;
    if (frm) {
        var _orig = HTMLFormElement.prototype.submit.bind(frm);
        frm.submit = function () {
            _vlSyncBasket(_orig);
        };
    }
})();
</script>
