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

		if (operation == "stop"
				&& !confirm("Are you sure you want to " + operation
						+ " transfer " + subOp + " ?"))
			return;

		var form = document.destinationDetailActionForm;
		if (subOp) {
			if (operation == "download") {
				window.location.href = "<bean:message key="destination.basepath"/>/operations/${destinationDetailActionForm.id}/"
						+ operation + "/" + subOp;
				return;
			} else {
				form.action = "<bean:message key="destination.basepath"/>/operations/${destinationDetailActionForm.id}/"
						+ operation + "/" + subOp;
			}
		} else {
			form.action = "<bean:message key="destination.basepath"/>/operations/${destinationDetailActionForm.id}/"
					+ operation;
		}

		form.submit();
	}

	function hostChange(operation, subOp) {

		if (operation == "deactivateHost"
				&& !confirm("Are you sure you want to deactivate host " + subOp
						+ " ?"))
			return;

		if (operation == "duplicateHost"
				&& !confirm("Are you sure you want to duplicate host " + subOp
						+ " ?"))
			return;

		var form = document.destinationDetailActionForm
		form.action = "<bean:message key="destination.basepath"/>/operations/${destinationDetailActionForm.id}/"
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

	function cleanDestination() {
		var text = "Are you sure you want to remove all Data Transfers from Destination '${destination.name}' and stop it?";
		form = document.destinationDetailActionForm

		if (confirm(text)) {
			form.action = "<bean:message key="destination.basepath"/>/operations/${destinationDetailActionForm.id}/cleanDestination"
			form.submit()
		}
	}

	function cleanExpiredDestination() {
		var text = "Are you sure you want to remove all deleted, expired, stopped and failed Data Transfers from Destination '${destination.name}'?";
		form = document.destinationDetailActionForm

		if (confirm(text)) {
			form.action = "<bean:message key="destination.basepath"/>/operations/${destinationDetailActionForm.id}/cleanExpiredDestination"
			form.submit()
		}
	}

	function restartDestination(immediate) {
		var immediateText = "IMMEDIATE: The destination and data transfers will be restarted NOW.";
		var gracefulText = "GRACEFUL: We'll wait for all currently running data transfers to be finished and then we'll restart."
		var text = "Are you sure you want to restart the Destination '${destination.name}'? \n"
				+ ((immediate) ? immediateText : gracefulText);

		form = document.destinationDetailActionForm

		if (confirm(text)) {
			form.action = "<bean:message key="destination.basepath"/>/operations/${destinationDetailActionForm.id}/"
			if (immediate)
				form.action += 'immediateRestart';
			else
				form.action += 'gracefulRestart';
			form.submit()
		}
	}

	function holdDestination(immediate) {
		var immediateText = "IMMEDIATE: The destination and data transfers will be stopped NOW.";
		var gracefulText = "GRACEFUL: We'll wait for all currently running data transfers to be finished and then we'll stop."
		var text = "Are you sure you want to put the Destination '${destination.name}' on Hold ?\n"
				+ ((immediate) ? immediateText : gracefulText);

		form = document.destinationDetailActionForm
		if (confirm(text)) {
			form.action = "<bean:message key="destination.basepath"/>/operations/${destinationDetailActionForm.id}/"
			if (immediate)
				form.action += 'immediatePutOnHold';
			else
				form.action += 'gracefulPutOnHold';

			form.submit()
		}
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
