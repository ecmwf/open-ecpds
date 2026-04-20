<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<script>
	var batchTransfers = {};

	function checkAll(all, reverse) {
		$('#validateTable tbody tr').each(function () {
			var span = $(this).find('span.batch-select');
			if (span.length) {
				var id = String(span.data('transferId'));
				if (id) {
					var current = batchTransfers[id] ? true : false;
					var target  = reverse ? !current : all;
					if (target !== current) select(span[0], id);
				}
			}
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
		} else {
			tr.removeClass('selected');
			icon.removeClass('bi-star-fill text-warning').addClass('bi-star');
		}
	}

	function transferChange(operation, transfer) {
	    var form = document.validateActionForm;
	    if (operation === 'delete' || operation === 'stop' || operation === 'requeue') {
	        var msg = 'Are you sure you want to ' + operation
	                + (transfer ? ' transfer ' + transfer : ' all batch-selected transfers');
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

    <%-- Entries-per-page row --%>
    <div class="d-flex align-items-center gap-2 my-2">
        <span class="text-muted small">Show</span>
        <select id="validatePageLen" class="form-select form-select-sm" style="width:auto">
            <option value="10">10</option>
            <option value="25">25</option>
            <option value="50">50</option>
            <option value="100">100</option>
            <option value="250">250</option>
        </select>
        <span class="text-muted small">entries per page</span>
        <span id="validateTransferTotal" class="text-muted small ms-2"></span>
    </div>

    <%-- DataTable --%>
    <table id="validateTable" class="table table-sm table-hover w-100">
        <thead>
            <tr>
                <th>Err</th>
                <th>Host</th>
                <th>Sched. Time</th>
                <th>Start Time</th>
                <th>Finish Time</th>
                <th>Target</th>
                <th>TS</th>
                <th>%</th>
                <th>Mbits/s</th>
                <th>Status</th>
                <th>Prior</th>
                <th>Actions</th>
                <th>Select</th>
            </tr>
        </thead>
    </table>

    <%-- A/N/R controls --%>
    <div class="mt-1 mb-2 small">
        <a href="javascript:checkAll(true,false)"   title="Select All for batch">A</a> /
        <a href="javascript:checkAll(false,false)"  title="Deselect All from batch">N</a> /
        <a href="javascript:checkAll(false,true)"   title="Reverse batch selection">R</a>
    </div>

    <%-- Messages --%>
    <tiles:insert page="./pds/transfer/destination/data/messages.jsp" />

    <%-- Global actions --%>
    <c:if test="${not empty ecpdsCanHandleQueue}">
        <div class="mt-2">
            <table class="fields">
                <tr>
                    <th>Global Actions</th>
                    <td><a href="javascript:transferChange('requeue')" title="Requeue batch-selected"><i class="bi bi-arrow-repeat"></i> Requeue</a></td>
                    <td><a href="javascript:transferChange('stop')" title="Stop batch-selected"><i class="bi bi-stop-circle text-danger"></i> Stop</a></td>
                    <auth:if basePathKey="destination.basepath"
                             paths="/deletions/${destinationDetailActionForm.id}/delete">
                        <auth:then>
                            <td><a href="javascript:transferChange('delete')" title="Delete batch-selected"><i class="bi bi-trash text-danger"></i> Delete</a></td>
                        </auth:then>
                    </auth:if>
                    <td>&nbsp;&nbsp;</td>
                    <td><a href="javascript:transferChange('increaseTransferPriority')" title="Increase priority"><i class="bi bi-arrow-up"></i></a></td>
                    <td><a href="javascript:transferChange('decreaseTransferPriority')" title="Decrease priority"><i class="bi bi-arrow-down"></i></a></td>
                    <td>
                        <input type="text" id="newPriorityInput" name="newPriority"
                               class="small_number form-control form-control-sm d-inline-block"
                               style="width:3rem"
                               value="${destinationDetailActionForm.newPriority}"
                               onkeypress="submitEnter(this,event)">
                    </td>
                    <td><a href="javascript:transferChange('setTransferPriority')" title="Set priority"><i class="bi bi-check-circle"></i></a></td>
                    <td>&nbsp;&nbsp;</td>
                    <td><a href="javascript:transferChange('extendLifetime')" title="Extend lifetime"><i class="bi bi-clock-history"></i></a></td>
                    <td>&nbsp;&nbsp;</td>
                    <td><a href="javascript:transferChange('clean')" title="Clear selection"><i class="bi bi-x-octagon"></i> Clean</a></td>
                    <td><a href="javascript:transferChange('cancel')" title="Back to destination"><i class="bi bi-arrow-left-circle"></i> Back</a></td>
                </tr>
            </table>
        </div>
    </c:if>
    <c:if test="${empty ecpdsCanHandleQueue}">
        <div class="mt-2">
            <a href="javascript:transferChange('cancel')" title="Back to destination"><i class="bi bi-arrow-left-circle"></i> Back</a>
        </div>
    </c:if>
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

    var table = $('#validateTable').DataTable({
        serverSide: true,
        processing: true,
        pageLength: savedLen,
        ordering: false,
        ajax: {
            url: '/do/transfer/destination?json=validateList',
            type: 'GET',
            data: function (d) {
                d.destinationName = destName;
            }
        },
        columns: [
            { data: 0 }, { data: 1 }, { data: 2 }, { data: 3 }, { data: 4 },
            { data: 5 }, { data: 6 }, { data: 7 }, { data: 8 }, { data: 9 },
            { data: 10 }, { data: 11 }, { data: 12 }
        ],
        columnDefs: [
            { targets: [0,1,2,3,4,5,6,7,8,9,10,11,12], orderable: false },
            { targets: [11], visible: canQueue }
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
                errorDiv.textContent = json.error;
                errorDiv.style.display = 'block';
            } else {
                errorDiv.style.display = 'none';
            }
            // Total count
            if (json && typeof json.recordsTotal !== 'undefined') {
                document.getElementById('validateTransferTotal').textContent =
                    '(' + json.recordsTotal + ' in selection)';
            }
        }
    });

    document.getElementById('validatePageLen').addEventListener('change', function () {
        var len = parseInt(this.value, 10);
        localStorage.setItem(STORAGE_KEY, len);
        table.page.len(len).draw();
    });

    window._validateTable = table;

    // Form submit hook: inject actionTransfer AND selectedTransfer fields.
    // selectedTransfer is session-scoped — Struts auto-populates it before any action runs,
    // so deselecting a transfer here (star off) persists to the session even on cancel/back.
    var frm = document.validateActionForm;
    if (frm) {
        var _orig = HTMLFormElement.prototype.submit.bind(frm);
        frm.submit = function () {
            $(frm).find('input.injected-batch').remove();
            $.each(batchTransfers, function (id, on) {
                var val = on ? 'on' : 'off';
                $('<input>').attr({ type: 'hidden', name: 'actionTransfer(' + id + ')',   value: val, class: 'injected-batch' }).appendTo(frm);
                $('<input>').attr({ type: 'hidden', name: 'selectedTransfer(' + id + ')', value: val, class: 'injected-batch' }).appendTo(frm);
            });
            _orig();
        };
    }
})();
</script>
