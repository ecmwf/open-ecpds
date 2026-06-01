<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>

<script>
	var OLD_STYLE = new Array();
	// Tracks which transfer IDs are selected (id -> boolean)
	var selectedTransfers = {};

	function checkAll(all, reverse) {
		var params = {
			json:                'idList',
			destinationName:     document.getElementById('dt-dest-name').value,
			disseminationStream: document.getElementById('dt-dissStream').value,
			dataStream:          document.getElementById('dt-dataStream').value,
			dataTime:            document.getElementById('dt-dataTime').value,
			status:              document.getElementById('dt-status').value,
			date:                document.getElementById('dt-date').value,
			fileNameSearch:      document.getElementById('dt-search').value
		};
		$.get('/do/transfer/destination', params).done(function (json) {
			if (!json || !json.ids) return;
			// Update only the current filter's IDs — basket entries from other filters
			// are left untouched by using delta mode (not replace mode).
			var addIds = [], delIds = [], delta = 0;
			json.ids.forEach(function (id) {
				var strId = String(id);
				var wasOn = !!selectedTransfers[strId];
				var isOn  = all ? true : (reverse ? !wasOn : false);
				selectedTransfers[strId] = isOn;
				if (isOn)  addIds.push(strId);
				else       delIds.push(strId);
				if (!wasOn && isOn)  delta++;
				if (wasOn  && !isOn) delta--;
			});
			window._clientDirty = true;
			// Merge into existing delta maps so previous star-clicks are not lost.
			window._deltaAdd = window._deltaAdd || {};
			window._deltaDel = window._deltaDel || {};
			addIds.forEach(function (id) { window._deltaAdd[id] = true; delete window._deltaDel[id]; });
			delIds.forEach(function (id) { window._deltaDel[id] = true; delete window._deltaAdd[id]; });
			// Derive count from server-authoritative baseline plus the net change of this
			// operation, so basket entries added in a previous filter/page are not lost.
			window._pendingClientCount = Math.max(0, (window._clientTotal || 0) + delta);
			if (window._destTransferTable) window._destTransferTable.ajax.reload(null, false);
		});
	}

	function setAll() {
		$('#destTransferTable tbody tr').each(function () {
			var span = $(this).find('span.star-select');
			if (span.length) {
				var id = String(span.data('transferId'));
				if (id && !selectedTransfers[id]) {
					select(span[0], id);
				}
			}
		});
	}

	function _updateHdrStar() {
		var rows = $('#destTransferTable tbody tr');
		if (!rows.length) return;
		var allSelected = true;
		rows.each(function () {
			var span = $(this).find('span.star-select');
			if (span.length) {
				var id = String(span.data('transferId'));
				if (!selectedTransfers[id]) { allSelected = false; return false; }
			}
		});
		var icon = document.getElementById('hdr-star-icon');
		if (!icon) return;
		if (allSelected) {
			icon.className = 'bi bi-star-fill text-warning';
		} else {
			icon.className = 'bi bi-star';
		}
	}

	function togglePageSelection() {
		var rows = $('#destTransferTable tbody tr');
		var allSelected = true;
		rows.each(function () {
			var span = $(this).find('span.star-select');
			if (span.length) {
				var id = String(span.data('transferId'));
				if (!selectedTransfers[id]) { allSelected = false; return false; }
			}
		});
		rows.each(function () {
			var span = $(this).find('span.star-select');
			if (span.length) {
				var id = String(span.data('transferId'));
				var isOn = selectedTransfers[id] ? true : false;
				if (allSelected ? isOn : !isOn) {
					select(span[0], id);
				}
			}
		});
		_updateHdrStar();
	}

	function transferChange(operation, subOp) {
	    if (operation === "stop") {
	        var text = "Are you sure you want to stop transfer " + subOp + " ?";
	        confirmationDialog({
	            title: "Confirm Transfer Stop",
	            message: text,
	            showLoading: true,
	            onConfirm: function () {
	                // Continue with transferChange logic
	                var form = document.destinationDetailActionForm;
	                if (subOp) {
	                    if (operation === "download") {
	                        window.location.href =
	                            "<bean:message key='destination.basepath'/>/operations/${destinationDetailActionForm.id}/"
	                            + operation + "/" + subOp;
	                        return;
	                    } else {
	                        form.action =
	                            "<bean:message key='destination.basepath'/>/operations/${destinationDetailActionForm.id}/"
	                            + operation + "/" + subOp;
	                    }
	                } else {
	                    form.action =
	                        "<bean:message key='destination.basepath'/>/operations/${destinationDetailActionForm.id}/"
	                        + operation;
	                }
	                form.submit();
	            }
	        });
	        return;
	    }
	    // No confirmation needed -> original logic
	    var form = document.destinationDetailActionForm;
	    if (subOp) {
	        if (operation === "download") {
	            window.location.href =
	                "<bean:message key='destination.basepath'/>/operations/${destinationDetailActionForm.id}/"
	                + operation + "/" + subOp;
	            return;
	        } else {
	            form.action =
	                "<bean:message key='destination.basepath'/>/operations/${destinationDetailActionForm.id}/"
	                + operation + "/" + subOp;
	        }
	    } else {
	        form.action =
	            "<bean:message key='destination.basepath'/>/operations/${destinationDetailActionForm.id}/"
	            + operation;
	    }
	    form.submit();
	}

	function hostChange(operation, subOp) {
	    var form = document.destinationDetailActionForm;
	    function submitWithScroll() {
	        sessionStorage.setItem('hostChangeScrollY', window.scrollY);
	        form.action =
	            "<bean:message key='destination.basepath'/>/operations/${destinationDetailActionForm.id}/"
	            + operation + "/" + subOp;
	        form.submit();
	    }
	    if (operation === "deactivateHost") {
	        var text = "Are you sure you want to deactivate host " + subOp + " ?";
	        confirmationDialog({
	            title: "Confirm Host Deactivation",
	            message: text,
	            showLoading: true,
	            onConfirm: function () { submitWithScroll(); }
	        });
	        return;
	    }
	    if (operation === "duplicateHost") {
	        var text = "Are you sure you want to duplicate host " + subOp + " ?";
	        confirmationDialog({
	            title: "Confirm Host Duplication",
	            message: text,
	            showLoading: true,
	            onConfirm: function () { submitWithScroll(); }
	        });
	        return;
	    }
	    // No confirmation required
	    submitWithScroll();
	}

	function selectFiltered(operation, transfer) {

		var form = document.destinationDetailActionForm
		if (transfer) {
			form.action = "<bean:message key="destination.basepath"/>/operations/${destinationDetailActionForm.id}/"
					+ operation + "/" + transfer;
		} else {
			form.action = "<bean:message key="destination.basepath"/>/operations/${destinationDetailActionForm.id}/validate/"
					+ operation;
		}

		form.submit();
	}

	function select(el, id) {
		var isOn = selectedTransfers[id] ? false : true;
		selectedTransfers[id] = isOn;
		var tr = $(el).closest('tr');
		var icon = $(el).find('i');
		if (isOn) {
			tr.addClass('selected');
			icon.removeClass('bi-star').addClass('bi-star-fill text-warning');
		} else {
			tr.removeClass('selected');
			icon.removeClass('bi-star-fill text-warning').addClass('bi-star');
		}
		window._clientDirty = true;
		window._clientTotal = (window._clientTotal || 0) + (isOn ? 1 : -1);
		// Track incremental change for delta-mode sync (used when selectedTransfers
		// is sparse, e.g. after a browser Back without a full checkAll).
		if (isOn) {
			(window._deltaAdd = window._deltaAdd || {})[id] = true;
			delete (window._deltaDel = window._deltaDel || {})[id];
		} else {
			(window._deltaDel = window._deltaDel || {})[id] = true;
			delete (window._deltaAdd = window._deltaAdd || {})[id];
		}
		if (typeof window._refreshSelectedCount === 'function') window._refreshSelectedCount();
		_updateHdrStar();
	}

	function clickField(field) {
		setField(field, field.value == 'on' ? 'off' : 'on');
	}

	function setField(field, value) {
		// Legacy helper kept for backward compatibility
		var tr = field.parentNode.parentNode;
		var destValue = (value) ? value : field.value;
		if (destValue == 'on') {
			if (tr.className != 'selected')
				OLD_STYLE[field.name] = tr.className;
			tr.className = 'selected';
		} else {
			if (OLD_STYLE[field.name])
				tr.className = OLD_STYLE[field.name];
		}
		field.value = destValue;
	}

	function setStatus(status) {
		sessionStorage.setItem('hostChangeScrollY', window.scrollY);
		document.destinationDetailActionForm.status.value = status
		document.destinationDetailActionForm.submit()
	}

	function setDataStream(stream) {
		sessionStorage.setItem('hostChangeScrollY', window.scrollY);
		document.destinationDetailActionForm.dataStream.value = stream
		document.destinationDetailActionForm.submit()
	}

	function setDataTime(stream) {
		sessionStorage.setItem('hostChangeScrollY', window.scrollY);
		document.destinationDetailActionForm.dataTime.value = stream
		document.destinationDetailActionForm.submit()
	}

	function setDisseminationStream(stream) {
		sessionStorage.setItem('hostChangeScrollY', window.scrollY);
		document.destinationDetailActionForm.disseminationStream.value = stream
		document.destinationDetailActionForm.submit()
	}

	function setDate(date) {
		sessionStorage.setItem('hostChangeScrollY', window.scrollY);
		document.destinationDetailActionForm.date.value = date
		document.destinationDetailActionForm.submit()
	}

	function changeSelect() {
		sessionStorage.setItem('hostChangeScrollY', window.scrollY);
		document.destinationDetailActionForm.submit()
	}

	function holdDestination(immediate) {
	    var immediateText =
	        "IMMEDIATE: The ongoing data transfers, and associated acquisition host listings, if any, will be interrupted now due to the stopping of the Destination.";
	    var gracefulText =
	        "GRACEFUL: The ongoing data transfers, and associated acquisition host listings, if any, will be allowed to complete before the stopping of the Destination.";
	    // Use HTML with <br/> so the message is readable in the jQuery UI dialog
	    var text =
	        "Are you sure you want to stop the ${destination.typeText} Destination ${destination.name}?<br/><br/>" +
	        (immediate ? immediateText : gracefulText);
	    var form = document.destinationDetailActionForm;
	    confirmationDialog({
	        title: "Confirm Destination Hold",
	        message: text,
	        showLoading: true, // overlay shown automatically on Confirm
	        onConfirm: function () {
	            form.action =
	                "<bean:message key='destination.basepath'/>/operations/${destinationDetailActionForm.id}/" +
	                (immediate ? "immediatePutOnHold" : "gracefulPutOnHold");
	            form.submit();
	        }
	    });
	}

	function cleanExpiredDestination() {
		var stopped = ['Stopped','Initialized','NoHosts','Interrupted','Failed'].indexOf(
			'${destination.formattedStatus}'.replace(/-.*/, '')) >= 0;
	    var text = stopped
	        ? "Are you sure you want to remove all deleted, expired, stopped and failed Data Transfers from the ${destination.typeText} Destination ${destination.name}?"
	        : "Are you sure you want to remove all deleted, expired, stopped and failed Data Transfers from the ${destination.typeText} Destination ${destination.name} and stop it?";
	    var form = document.destinationDetailActionForm;
	    confirmationDialog({
	        title: "Confirm Cleanup of Expired Transfers",
	        message: text,
	        showLoading: true,
	        onConfirm: function () {
	            form.action =
	                "<bean:message key='destination.basepath'/>/operations/${destinationDetailActionForm.id}/cleanExpiredDestination";
	            form.submit();
	        }
	    });
	}

	function cleanDestination() {
		var stopped = ['Stopped','Initialized','NoHosts','Interrupted','Failed'].indexOf(
			'${destination.formattedStatus}'.replace(/-.*/, '')) >= 0;
	    var text = stopped
	        ? "Are you sure you want to remove all Data Transfers from the ${destination.typeText} Destination ${destination.name}?"
	        : "Are you sure you want to remove all Data Transfers from the ${destination.typeText} Destination ${destination.name} and stop it?";
	    var form = document.destinationDetailActionForm;
	    confirmationDialog({
	        title: "Confirm Destination Cleanup",
	        message: text,
	        showLoading: true,
	        onConfirm: function () {
	            form.action =
	                "<bean:message key='destination.basepath'/>/operations/${destinationDetailActionForm.id}/cleanDestination";
	            form.submit();
	        }
	    });
	}
	
	function startDestination() {
	    var form = document.destinationDetailActionForm;
	    confirmationDialog({
	        title: "Confirm Destination Start",
	        message: "Are you sure you want to start the ${destination.typeText} Destination ${destination.name}?",
	        showLoading: true,
	        onConfirm: function () {
	            form.action =
	                "<bean:message key='destination.basepath'/>/operations/${destinationDetailActionForm.id}/gracefulRestart";
	            form.submit();
	        }
	    });
	}

	function restartDestination(immediate) {
	    var immediateText =
	        "IMMEDIATE: The ongoing data transfers, and associated acquisition host listings, if any, will be interrupted now due to the restarting of the Destination.";
	    var gracefulText =
	        "GRACEFUL: The ongoing data transfers, and associated acquisition host listings, if any, will be allowed to complete before the restarting of the Destination.";
	    var text =
	        "Are you sure you want to restart the ${destination.typeText} Destination ${destination.name}?<br/><br/>" +
	        (immediate ? immediateText : gracefulText);
	    var form = document.destinationDetailActionForm;
	    confirmationDialog({
	        title: "Confirm Destination Restart",
	        message: text,
	        showLoading: true, // overlay shown automatically
	        onConfirm: function () {
	            form.action =
	                "<bean:message key='destination.basepath'/>/operations/${destinationDetailActionForm.id}/" +
	                (immediate ? "immediateRestart" : "gracefulRestart");
	            form.submit();
	        }
	    });
	}

	function submitenter(myfield, e) {
		var keycode;
		if (window.event)
			keycode = window.event.keyCode;
		else if (e)
			keycode = e.which;
		else
			return true;
		if (keycode == 13) {
			myfield.form.submit();
			return false;
		} else
			return true;
	}
	$(document).ready(function () {
	    // Intercept all form submits: sync the client-side selection to the server
	    // via a small AJAX POST, then trigger the original GET form submit.
	    // This avoids injecting hundreds of selectedTransfer params into the GET URL
	    // which would exceed Jetty's request header size limit (HTTP 431).
	    var frm = document.destinationDetailActionForm;
	    if (frm) {
	        var _orig = HTMLFormElement.prototype.submit.bind(frm);
	        frm.submit = function () {
	            // Only sync when the client has changed the selection (_clientDirty).
	            // When the server is already authoritative (e.g. after a browser Back),
	            // selectedTransfers only contains the current page's IDs, so syncing would
	            // wrongly wipe the server's full selection down to just the current page.
	            if (!window._clientDirty) {
	                _orig();
	                return;
	            }
	            var dest = document.getElementById('dt-dest-name').value;
	            var payload;
	            if (window._fullSyncNeeded) {
	                // Replace mode: checkAll() ran so selectedTransfers has all IDs.
	                var ids = [];
	                $.each(selectedTransfers, function (id, on) { if (on) ids.push(id); });
	                payload = { destinationName: dest, type: 'replace', ids: ids.join(',') };
	            } else {
	                // Delta mode: only star-clicks were made; send add/del changes so the
	                // server's full selection (from the last basket visit) is preserved.
	                payload = { destinationName: dest, type: 'delta',
	                            add: Object.keys(window._deltaAdd || {}).join(','),
	                            del: Object.keys(window._deltaDel || {}).join(',') };
	            }
	            // Send as JSON body so Jetty's maxFormContentSize limit does not apply —
	            // the dispatcher reads json=syncSelection from the URL query string.
	            $.ajax({
	                url:         '/do/transfer/destination?json=syncSelection',
	                type:        'POST',
	                contentType: 'application/json',
	                data:        JSON.stringify(payload)
	            }).always(function () {
	                _orig();
	            });
	        };
	    }
	});
</script>

