// Loading ace editor library
var beautify = ace.require("ace/ext/beautify");
var ltools = ace.require("ace/ext/language_tools");
var Range = ace.require("ace/range").Range;

// Hide a layer
function hide(layerName) {
	if (layerName != null)
		document.getElementById(layerName).style.visibility = 'hidden';
}

// Show a layer, hiding other.
function toggle(event, layerName, toHide) {
	d = document.getElementById(layerName);
	d.style.visibility = (d.style.visibility == 'visible') ? 'hidden' : 'visible';
	d.style.top = event.pageY + 10
	d.style.left = event.pageX + 20
	hide(toHide);
}

function toggle_in_place(event, layerName, toHide) {
	d = document.getElementById(layerName);
	d.style.visibility = (d.style.visibility == 'visible') ? 'hidden' : 'visible';
	hide(toHide);
}

/* Function to generate a new password */
function getPassword() {
	var pass = '';
	var str = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789#$';
	for (i = 1; i <= 8; i++) {
		var char = Math.floor(Math.random()
			* str.length + 1);
		pass += str.charAt(char)
	}
	alert("Please take note of the generated password: " + pass + "\nClick the 'Process' button to save it!");
	return pass;
}

function checkValueForType(type, choices, currentLine) {
	const regex = /"([^"]+)"/;
	const matches = currentLine.match(regex);
	var result = null;
	if (matches && matches[1]) {
		const value = matches[1];
		if (type === "Boolean" && ["yes", "no", "true", "false"].indexOf(value.toLowerCase()) === -1) {
			result = "The value should be a boolean (e.g. \"yes\" or \"no\")";
		} else if (type === "Duration") {
			const iso8601DurationRegex = /^([-+]?)P(?:([-+]?[0-9]+)D)?(T(?:([-+]?[0-9]+)H)?(?:([-+]?[0-9]+)M)?(?:([-+]?[0-9]+)(?:[.,]([0-9]{0,9}))?S)?)?$/;
			const integerRegex = /^-?\d+$/;
			result = iso8601DurationRegex.test(value) || integerRegex.test(value) ? null : "The value should be a duration (e.g. \"PT20.345S\", \"PT15M\" or \"PT48H\")";
		} else if (type === "Period") {
			const iso8601PeriodRegex = /^([-+]?)P(?:([-+]?[0-9]+)Y)?(?:([-+]?[0-9]+)M)?(?:([-+]?[0-9]+)W)?(?:([-+]?[0-9]+)D)?$/
			const integerRegex = /^-?\d+$/;
			result = iso8601PeriodRegex.test(value) || integerRegex.test(value) ? null : "The value should be a period (e.g. \"P2Y\", \"P3M\", \"P4W\", \"P5D\", \"P1Y2M3D\", or \"P1Y2M3W4D\")";
		} else if (type === "ByteSize") {
			const byteSizeRegex = /^(\d+)(b|kb|mb|gb|pb|tb|eb)?$/i;
			result = "max-size" === value || byteSizeRegex.test(value) ? null : "The value should be a number of bytes (e.g. \"10MB\" or \"1024B\")";
		} else if (type === "TimeRange") {
			const localTimeRegex = /^(?:[01]\d|2[0-3]):[0-5]\d(:[0-5]\d(\.\d{1,9})?)?$/;
			time = value.split("-");
			result = time.length == 2 && localTimeRegex.test(time[0]) && localTimeRegex.test(time[1]) ? null : "The value should be a time range (e.g. \"10:15-11:25:30\")";
		} else if (type === "Integer") {
			const integerRegex = /^-?\d+$/;
			result = "max-integer" === value || integerRegex.test(value) ? null : "The value should be an integer (e.g. \"12345\")";
		} else if (type === "Long") {
			const integerRegex = /^-?\d+$/;
			result = "max-long" === value || integerRegex.test(value) ? null : "The value should be a long (e.g. \"1234567890\")";
		} else if (type === "Double") {
			const doubleRegex = /^-?\d+(\.\d+)?$/;
			result = "max-double" === value || doubleRegex.test(value) ? null : "The value should be a double (e.g. \"12.34\")";
		}
		if (result === null && choices.length > 0 && choices.indexOf(value) === -1)
			result = "The value must be one of the elements in the following list: " + choices.map(str => '"' + str + '"').join(", ");
	}
	return result !== null ? result + "." : null;
}

function getAnnotations(aceEditor, row) {
	var currentLine = aceEditor.session.getLine(row);
	for (var j = 0; j < completions.length; j++) {
		var tipObject = completions[j];
		var withoutSpace = currentLine.replace(/\s/g, "");
		if (withoutSpace === tipObject.caption || withoutSpace.startsWith(tipObject.caption + "=")) {
			var moduleName = tipObject.caption.split(".")[0];
			var tipsText;
			var tipsType;
			var error = checkValueForType(tipObject.type, tipObject.choices, currentLine);
			if (error != null) {
				tipsText = error;
				tipsType = "error";
			} else {
				tipsText = tipObject.tips;
				tipsType = "info";
			}
			return [{
				row: row,
				column: 0,
				text: tipsText,
				type: tipsType,
				module: moduleName
			}];
		}
	}
	// If no match found let's put a tips if it looks like a parameter!
	const pattern = /^[^\s.]+\.[^\s.]+\s*=(.*)$/;
	if (currentLine.length > 0 && pattern.test(currentLine) && !currentLine.startsWith('#')) {
		return [{
			row: row,
			column: 0,
			text: "This option is not recognized.",
			type: "error"
		}];
	}
	return [];
}

function checkEachLine(aceEditor) {
	var editor = aceEditor.session;
	var markers = editor.getMarkers();
	for (var i = 0; i < editor.getLength(); i++) {
		var annotations = getAnnotations(aceEditor, i);
		if (annotations && annotations.length > 0) {
			if (annotations[0].type === "error") {
				editor.addMarker(new Range(i, 0, i, Number.MAX_VALUE), "custom-ace-marker-info", "text");
			} else if (annotations[0].type === "warning") {
				editor.addMarker(new Range(i, 0, i, Number.MAX_VALUE), "custom-ace-marker-warning", "text");
			} else {
				for (var markerId in markers) {
					var marker = markers[markerId];
					if (marker.range && marker.range.start.row === i
						&& (marker.clazz === "custom-ace-marker-info" || marker.clazz === "custom-ace-marker-warning"))
						editor.removeMarker(markerId);
				}
			}
		}
	}
}

function checkIfExist(aceEditor, value) {
	const lines = aceEditor.getValue().split('\n');
	const search = value.substring(0, value.length - 1);
	for (let i = 0; i < lines.length; i++) {
		if (lines[i].startsWith(search)) {
			return true; // Found a match in this line
		}
	}
	return false; // No match found in any line
}

function makeResizable(aceEditor) {
	aceEditor.setValue(aceEditor.getValue().trim());
	aceEditor.selection.clearSelection();
	document.addEventListener("mouseup", function(e) {
		aceEditor.resize();
	});
}

function formatSource(aceEditor) {
	beautify.beautify(aceEditor.session);
}

function clearSource(aceEditor) {
	aceEditor.setValue("");
}

function getEditorProperties(readOnly, autocompletion, name, mode) {
	var editorProperties = ace.edit(name);
	autocompletion = !readOnly && autocompletion;
	editorProperties.setTheme("ace/theme/eclipse");
	editorProperties.session.setMode("ace/mode/" + mode);
	editorProperties.setAutoScrollEditorIntoView(true);
	editorProperties.setOptions({
		showLineNumbers: true,
		readOnly: readOnly,
		showPrintMargin: false,
		minLines: 10,
		maxLines: Infinity,
		enableBasicAutocompletion: autocompletion,
		enableSnippets: true,
		enableLiveAutocompletion: autocompletion,
		tabSize: 2
	});
	return editorProperties;
}

function testSource(aceEditor) {
	try {
		var code = aceEditor.getValue();
		var o = eval(code);
		if (o != null) {
			alert(o.toString());
		} else {
			alert("No output from script");
		}
	} catch (error) {
		alert("Error: " + error.message);
	}
}

function getEditorType(aceEditor) {
	var value = aceEditor.getValue().trim();
	var type = "text";
	if (value.startsWith("$(") && value.endsWith(")")) {
		value = value.substring(2, value.length - 1).trim();
		if (value.startsWith("python:")) {
			value = value.substring(7);
			type = "python";
		} else if (value.startsWith("js:")) {
			value = value.substring(3);
			type = "js";
		} else {
			// This is the default script type
			type = "js";
		}
		aceEditor.setValue(value);
		aceEditor.selection.clearSelection();
	}
	return type;
}

// Lets' populate the help tab!
function getHelpHtmlContent(completions, title) {
	let htmlContent = '<h1>' + title + '</h1><p>';
	var currentGroup;
	for (var j = 0; j < completions.length; j++) {
		var tipObject = completions[j];
		var group = tipObject.caption.split('.')[0];
		if (!currentGroup || group !== currentGroup) {
			htmlContent += '<hr>Group <i>"' + group + '"</i><hr>';
			currentGroup = group;
		}
		htmlContent += '<h3><u>' + tipObject.caption + '</u> (' + tipObject.type + ')</h3><p>' + escapeHtml(tipObject.tips) + '</p>\n';
	}
	return htmlContent + "</p>";
}

function checkKeyIsMatching(event, regexPattern) {
	var regex = new RegExp(regexPattern);
	var key = String.fromCharCode(!event.charCode ? event.which
		: event.charCode);
	if (!regex.test(key)) {
		event.preventDefault();
		return false;
	} else {
		return true;
	}
}

function escapeHtml(text) {
    var element = document.createElement('div');
    var textNode = document.createTextNode(text);
    element.appendChild(textNode);
    return element.innerHTML;
}