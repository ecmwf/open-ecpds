<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>

<script>
	var OLD_STYLE = new Array();
	// Tracks which transfer IDs are selected (id → boolean)
	var selectedTransfers = {};

	function checkAll(all, reverse) {
		$('#destTransferTable tbody tr').each(function () {
			var span = $(this).find('span.star-select');
			if (span.length) {
				var id = String(span.data('transferId'));
				if (id) {
					var current = selectedTransfers[id] ? true : false;
					var target  = reverse ? !current : all;
					if (target !== current) {
						select(span[0], id);
					}
				}
			}
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
	    // No confirmation needed → original logic
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
		document.destinationDetailActionForm.status.value = status
		document.destinationDetailActionForm.submit()
	}

	function setDataStream(stream) {
		document.destinationDetailActionForm.dataStream.value = stream
		document.destinationDetailActionForm.submit()
	}

	function setDataTime(stream) {
		document.destinationDetailActionForm.dataTime.value = stream
		document.destinationDetailActionForm.submit()
	}

	function setDisseminationStream(stream) {
		document.destinationDetailActionForm.disseminationStream.value = stream
		document.destinationDetailActionForm.submit()
	}

	function setDate(date) {
		document.destinationDetailActionForm.date.value = date
		document.destinationDetailActionForm.submit()
	}

	function changeSelect() {
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
	    var savedY = sessionStorage.getItem('hostChangeScrollY');
	    if (savedY !== null) {
	        sessionStorage.removeItem('hostChangeScrollY');
	        window.scrollTo({ top: parseInt(savedY, 10), behavior: 'instant' });
	    }
	    // Intercept all form submits to inject selected-transfer hidden fields
	    var frm = document.destinationDetailActionForm;
	    if (frm) {
	        var _orig = HTMLFormElement.prototype.submit.bind(frm);
	        frm.submit = function () {
	            $(frm).find('input.injected-selection').remove();
	            $.each(selectedTransfers, function (id, on) {
	                $('<input>').attr({
	                    type:  'hidden',
	                    name:  'selectedTransfer(' + id + ')',
	                    value: on ? 'on' : 'off',
	                    class: 'injected-selection'
	                }).appendTo(frm);
	            });
	            _orig();
	        };
	    }
	});
</script>

