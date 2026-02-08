<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>

<script>
	var OLD_STYLE = new Array();

	function checkAll(all, reverse) {
		var form = document.destinationDetailActionForm
		for (i = 0; i < form.length; i++) {
			var field = form.elements[i]
			if (field.type.toLowerCase() == "hidden"
					&& field.name.substr(0, 16) == "selectedTransfer") {
				if (reverse)
					clickField(field)
				else
					setField(field, all ? "on" : "off");
			}
		}
	}

	function setAll() {
		var form = document.destinationDetailActionForm
		for (i = 0; i < form.length; i++) {
			var field = form.elements[i]
			if (field.type.toLowerCase() == "hidden"
					&& field.name.substr(0, 16) == "selectedTransfer") {
				setField(field);
			}
		}
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
	    if (operation === "deactivateHost") {
	        var text = "Are you sure you want to deactivate host " + subOp + " ?";
	        confirmationDialog({
	            title: "Confirm Host Deactivation",
	            message: text,
	            showLoading: true,
	            onConfirm: function () {
	                form.action =
	                    "<bean:message key='destination.basepath'/>/operations/${destinationDetailActionForm.id}/"
	                    + operation + "/" + subOp;
	                form.submit();
	            }
	        });
	        return;
	    }
	    if (operation === "duplicateHost") {
	        var text = "Are you sure you want to duplicate host " + subOp + " ?";
	        confirmationDialog({
	            title: "Confirm Host Duplication",
	            message: text,
	            showLoading: true,
	            onConfirm: function () {
	                form.action =
	                    "<bean:message key='destination.basepath'/>/operations/${destinationDetailActionForm.id}/"
	                    + operation + "/" + subOp;
	                form.submit();
	            }
	        });
	        return;
	    }
	    // No confirmation required → original logic
	    form.action =
	        "<bean:message key='destination.basepath'/>/operations/${destinationDetailActionForm.id}/"
	        + operation + "/" + subOp;
	    form.submit();
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

	function select(image, id) {
		//alert("Selecting "+id+" in element "+image.parentNode.parentNode)
		var form = document.destinationDetailActionForm;
		clickField(form.elements["selectedTransfer(" + id + ")"]);
	}

	function clickField(field) {
		setField(field, field.value == 'on' ? 'off' : 'on');
	}

	function setField(field, value) {
		// If a value is not supplied, the value is not changed, just the color

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
	    var text =
	        "Are you sure you want to remove all deleted, expired, stopped and failed Data Transfers from the " +
	        "${destination.typeText} Destination ${destination.name} and stop it?";
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
	    var text =
	        "Are you sure you want to remove all Data Transfers from the ${destination.typeText} Destination ${destination.name} and stop it?";
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
</script>

