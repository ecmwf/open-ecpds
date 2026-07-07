// Loading ace editor library
var beautify, ltools, Range;
try {
    beautify = ace.require("ace/ext/beautify");
    ltools   = ace.require("ace/ext/language_tools");
    Range    = ace.require("ace/range").Range;
} catch(e) {}

// Hide a layer
function hide(layerName) {
	var element;
	if (layerName != null && (element = document.getElementById(layerName)) != null)
	  element.style.visibility = 'hidden';
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
			const isValidTimeRange = function(v) {
				const parts = v.trim().split("-");
				return parts.length === 2 && localTimeRegex.test(parts[0]) && localTimeRegex.test(parts[1]);
			};
			result = value.split(",").every(function(t) { return isValidTimeRange(t); })
				? null : "The value should be one or more time ranges separated by commas (e.g. \"10:15-11:25\" or \"10:15-11:25,14:00-16:00\")";
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

function checkEachLine(aceEditor, accordionBtnId) {
	var session = aceEditor.session;
	// Remove all existing validation markers in one pass
	var markers = session.getMarkers();
	for (var markerId in markers) {
		var m = markers[markerId];
		if (m.clazz === "custom-ace-marker-error" || m.clazz === "custom-ace-marker-warning")
			session.removeMarker(markerId);
	}
	var errorCount = 0, warningCount = 0;
	var allAnnotations = [];
	for (var i = 0; i < session.getLength(); i++) {
		var annotations = getAnnotations(aceEditor, i);
		if (annotations && annotations.length > 0) {
			allAnnotations = allAnnotations.concat(annotations);
			if (annotations[0].type === "error") {
				session.addMarker(new Range(i, 0, i, Number.MAX_VALUE), "custom-ace-marker-error", "fullLine");
				errorCount++;
			} else if (annotations[0].type === "warning") {
				session.addMarker(new Range(i, 0, i, Number.MAX_VALUE), "custom-ace-marker-warning", "fullLine");
				warningCount++;
			}
		}
	}
	// Push all annotations to ACE (drives gutter icons for the current cursor row)
	session.setAnnotations(allAnnotations);
	// Update the accordion button badge if an ID was provided
	if (accordionBtnId) {
		updateAccordionErrorBadge(accordionBtnId, errorCount, warningCount);
	}
	return { errors: errorCount, warnings: warningCount };
}

function updateAccordionErrorBadge(btnId, errorCount, warningCount) {
	var btn = document.getElementById(btnId);
	if (!btn) return;
	var badgeId = btnId + '-validation-badge';
	var badge = document.getElementById(badgeId);
	if (errorCount === 0 && warningCount === 0) {
		if (badge) badge.remove();
		return;
	}
	if (!badge) {
		badge = document.createElement('span');
		badge.id = badgeId;
		badge.className = 'acc-props-badge';
		btn.appendChild(badge);
	}
	var html = '';
	if (errorCount > 0) {
		html += '<span class="badge rounded-pill bg-danger">'
			+ '<i class="bi bi-exclamation-circle-fill me-1"></i>' + errorCount + '</span>';
	}
	if (warningCount > 0) {
		html += '<span class="badge rounded-pill bg-warning text-dark' + (errorCount > 0 ? ' ms-1' : '') + '">'
			+ '<i class="bi bi-exclamation-triangle-fill me-1"></i>' + warningCount + '</span>';
	}
	badge.innerHTML = html;
}

// Applies full-line background markers and an accordion badge based on ACE's
// built-in annotations (e.g. from the JS linter). Call on 'changeAnnotation'.
function applyAnnotationMarkers(aceEditor, accordionBtnId) {
	var session = aceEditor.session;
	// Remove existing validation markers
	var markers = session.getMarkers();
	for (var markerId in markers) {
		var m = markers[markerId];
		if (m.clazz === "custom-ace-marker-error" || m.clazz === "custom-ace-marker-warning")
			session.removeMarker(markerId);
	}
	// Gather worst annotation type per row
	var rowSeverity = {};
	var annotations = session.getAnnotations() || [];
	for (var i = 0; i < annotations.length; i++) {
		var a = annotations[i];
		var row = a.row;
		if (a.type === "error") {
			rowSeverity[row] = "error";
		} else if (a.type === "warning" && rowSeverity[row] !== "error") {
			rowSeverity[row] = "warning";
		}
	}
	var errorCount = 0, warningCount = 0;
	for (var r in rowSeverity) {
		if (rowSeverity[r] === "error") {
			session.addMarker(new Range(parseInt(r), 0, parseInt(r), Number.MAX_VALUE), "custom-ace-marker-error", "fullLine");
			errorCount++;
		} else {
			session.addMarker(new Range(parseInt(r), 0, parseInt(r), Number.MAX_VALUE), "custom-ace-marker-warning", "fullLine");
			warningCount++;
		}
	}
	if (accordionBtnId) {
		updateAccordionErrorBadge(accordionBtnId, errorCount, warningCount);
	}
}

// Checks Python syntax using Skulpt (loaded lazily on first use).
// Feeds errors into ACE annotations + full-line markers + accordion badge.
var _pythonCheckTimer = null;
function checkPythonSyntax(aceEditor, accordionBtnId) {
	// Lazy-load Skulpt on first use, then re-invoke
	if (typeof Sk === 'undefined') {
		var s = document.createElement('script');
		s.src = '/ace-editor/skulpt.min.js';
		s.onload = function() { checkPythonSyntax(aceEditor, accordionBtnId); };
		document.head.appendChild(s);
		return;
	}
	var session = aceEditor.session;
	// Clear existing validation markers
	var markers = session.getMarkers();
	for (var markerId in markers) {
		var m = markers[markerId];
		if (m.clazz === "custom-ace-marker-error" || m.clazz === "custom-ace-marker-warning")
			session.removeMarker(markerId);
	}
	var annotations = [];
	try {
		Sk.configure({ output: function() {}, read: function(x) { throw x + ' not found'; } });
		Sk.compile(aceEditor.getValue(), 'input.py', 'exec');
		// No syntax error — clear everything
	} catch (e) {
		// Extract line number: Skulpt SyntaxError exposes $lineno, traceback, or args
		var lineno = 1;
		var msg = 'Syntax error';
		if (e.$lineno) {
			lineno = parseInt(e.$lineno) || 1;
		} else if (e.traceback && e.traceback.length > 0) {
			lineno = e.traceback[0].lineno || 1;
		}
		if (e.$msg) {
			msg = String(e.$msg);
		} else if (e.args && e.args.v && e.args.v[0]) {
			msg = e.args.v[0].v !== undefined ? String(e.args.v[0].v) : String(e.args.v[0]);
		} else if (e.message) {
			msg = e.message;
		}
		var row = Math.max(0, lineno - 1);
		session.addMarker(new Range(row, 0, row, Number.MAX_VALUE), "custom-ace-marker-error", "fullLine");
		annotations.push({ row: row, col: 0, text: msg, type: 'error' });
	}
	session.setAnnotations(annotations);
	if (accordionBtnId) updateAccordionErrorBadge(accordionBtnId, annotations.length, 0);
}

function debouncedCheckPythonSyntax(aceEditor, accordionBtnId) {
	clearTimeout(_pythonCheckTimer);
	_pythonCheckTimer = setTimeout(function() {
		checkPythonSyntax(aceEditor, accordionBtnId);
	}, 600);
}

// Detects whether code looks like JavaScript or Python based on keyword heuristics.
// Returns 'javascript', 'python', or null if inconclusive.
function detectCodeType(code) {
	if (!code || code.trim().length === 0) return null;
	var lines = code.split('\n');
	var jsScore = 0, pyScore = 0;
	var jsPatterns = [
		/\bfunction\b/, /\bvar\s+\w/, /\blet\s+\w/, /\bconst\s+\w/,
		/=>/, /\bconsole\./, /\bdocument\./, /\bwindow\./, /\brequire\s*\(/,
		/\bjQuery\b/, /\$\s*\(/, /\.forEach\s*\(/, /\.map\s*\(/, /\bawait\b/, /\basync\b/
	];
	var pyPatterns = [
		/^\s*def\s+\w/, /^\s*import\s+\w/, /^\s*from\s+\w+\s+import\b/,
		/^\s*class\s+\w/, /^\s*elif\b/, /^\s*except[\s:]/, /^\s*with\s+\w/,
		/^\s*print\s*\(/, /^\s*raise\b/, /^\s*yield\b/
	];
	for (var i = 0; i < lines.length; i++) {
		var line = lines[i];
		for (var j = 0; j < jsPatterns.length; j++) {
			if (jsPatterns[j].test(line)) jsScore++;
		}
		for (var j = 0; j < pyPatterns.length; j++) {
			if (pyPatterns[j].test(line)) pyScore++;
		}
	}
	if (jsScore === 0 && pyScore === 0) return null;
	if (jsScore > pyScore) return 'javascript';
	if (pyScore > jsScore) return 'python';
	// Tie-break: if any strong Python-only signal present, prefer python
	return pyScore > 0 ? 'python' : 'javascript';
}

// Shows or hides the type mismatch warning near the dirType radio buttons.
// mode: 'text' = Plain Text, 'js' = JavaScript, 'python' = Python
function updateDirTypeMismatchWarning(warningId, textId, aceEditor, mode) {
	var warn = document.getElementById(warningId);
	var txt = document.getElementById(textId);
	if (!warn || !txt) return;
	var detected = (mode === 'text' || mode === 'js' || mode === 'python') ? detectCodeType(aceEditor.getValue()) : null;
	if (mode === 'text') {
		if (detected === 'javascript') {
			txt.textContent = 'Content looks like JavaScript \u2014 did you mean to select \u201cJavaScript\u201d?';
			warn.style.display = '';
		} else if (detected === 'python') {
			txt.textContent = 'Content looks like Python \u2014 did you mean to select \u201cPython\u201d?';
			warn.style.display = '';
		} else {
			warn.style.display = 'none';
		}
	} else if (mode === 'js') {
		if (detected === 'python') {
			txt.textContent = 'Content looks like Python \u2014 did you mean to select \u201cPython\u201d?';
			warn.style.display = '';
		} else {
			warn.style.display = 'none';
		}
	} else if (mode === 'python') {
		if (detected === 'javascript') {
			txt.textContent = 'Content looks like JavaScript \u2014 did you mean to select \u201cJavaScript\u201d?';
			warn.style.display = '';
		} else {
			warn.style.display = 'none';
		}
	} else {
		warn.style.display = 'none';
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
	var modeId = aceEditor.session.getMode().$id;
	if (modeId === 'ace/mode/python') {
		formatPythonSource(aceEditor);
	} else {
		beautify.beautify(aceEditor.session);
	}
}

var _ruffWasmModule = null;

async function _loadRuffWasm() {
	if (_ruffWasmModule) return _ruffWasmModule;
	const mod = await import('/ruff-wasm/ruff_wasm.js');
	await mod.default({ module_or_path: '/ruff-wasm/ruff_wasm_bg.wasm' });
	_ruffWasmModule = mod;
	return mod;
}

async function formatPythonSource(aceEditor) {
	try {
		const ruff = await _loadRuffWasm();
		const original = aceEditor.getValue();
		const workspace = new ruff.Workspace({ 'line-length': 88 });
		const formatted = workspace.format(original);
		if (formatted !== original) {
			const pos = aceEditor.getCursorPosition();
			aceEditor.setValue(formatted, -1);
			aceEditor.moveCursorToPosition(pos);
			aceEditor.session.getUndoManager().reset();
			aceEditor.session.getUndoManager().markClean();
		}
	} catch(e) {
		const msg = e.message || String(e);
		// Ruff refuses to format code with syntax errors — surface a clear message rather than the
		// raw byte-range error from the parser.
		const isSyntaxError = /indented block|invalid syntax|SyntaxError|unexpected token|parse error/i.test(msg);
		if (isSyntaxError) {
			showToast('Fix syntax errors before formatting.', 'warning');
		} else {
			showToast('Format error: ' + msg, 'danger');
		}
	}
}

function clearSource(aceEditor) {
	aceEditor.setValue("");
}

var _ecpdsAceEditors = [];

function _ecpdsAceTheme() {
	return document.documentElement.getAttribute('data-bs-theme') === 'dark'
		? 'ace/theme/ecpds_dark' : 'ace/theme/eclipse';
}

function ecpdsUpdateAceTheme(theme) {
	var aceTheme = (theme === 'dark') ? 'ace/theme/ecpds_dark' : 'ace/theme/eclipse';
	_ecpdsAceEditors.forEach(function(ed) { ed.setTheme(aceTheme); });
}

function getEditorProperties(readOnly, autocompletion, name, mode) {
	var editorProperties = ace.edit(name);
	autocompletion = !readOnly && autocompletion;
	editorProperties.setTheme(_ecpdsAceTheme());
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
	_ecpdsAceEditors.push(editorProperties);
	return editorProperties;
}

function testSource(aceEditor) {
  const userCode = aceEditor.getValue();
  // Build HTML content for the iframe
  const iframeHTML = `<!DOCTYPE html>
  <html>
  <body>
    <script>
      (async () => {
        try {
          const result = await (async function() {
            ${userCode}
          })();
          const msg = result != null && result.length > 0 ? result : "No return from script";
          parent.postMessage({ type: 'ecpds-sandbox-result', message: msg, level: 'info' }, '*');
        } catch (e) {
          parent.postMessage({ type: 'ecpds-sandbox-result', message: e.message, level: 'danger' }, '*');
        }
      })();
    <\/script>
  </body>
  </html>`;
  const blob = new Blob([iframeHTML], { type: 'text/html' });
  const iframe = document.getElementById("sandboxFrame");
  iframe.src = URL.createObjectURL(blob);
}

/**
 * Execute a Directory script (JS or Python) on the DataMover via the server-side testScript
 * endpoint, then show the result in the shared testResultModal.
 *
 * @param {object} aceEditor  - ACE editor instance containing the script
 * @param {string} hostId     - Host primary key (path param for the endpoint)
 * @param {string} lang       - "js" or "python"
 * @param {string} [transferId] - Optional DataTransfer ID whose fields will be substituted
 * @param {object} [manualValues] - Optional key→value map for manual placeholder substitution
 */
/** True while a testSourceServer fetch is in flight — prevents double-clicks and annotation re-enables. */
var _testDirRunning = false;

/**
 * Resolve placeholder tokens in a plain-text Directory field on the server and show the result.
 * Calls /do/transfer/host/edit/resolveDirText/{hostId} — no DataMover required.
 *
 * @param {object} aceEditor - ACE editor instance containing the plain-text directory content
 * @param {string} hostId    - Host primary key
 */
function testDirTextOnServer(aceEditor, hostId) {
  if (_testDirRunning) return;
  _testDirRunning = true;
  var text = aceEditor.getValue();
  var btn = document.getElementById('testDir');
  if (btn && !btn.dataset.origLabel) btn.dataset.origLabel = btn.innerHTML;
  var origLabel = btn ? btn.dataset.origLabel : '';
  if (btn) {
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1" role="status"></span>Resolving\u2026';
  }
  var params = new URLSearchParams({ text: text });
  var controller = new AbortController();
  var timeoutId = setTimeout(function() { controller.abort(); }, 30000);
  fetch('/do/transfer/host/edit/resolveDirText/' + encodeURIComponent(hostId), {
    method: 'POST',
    credentials: 'same-origin',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: params.toString(),
    signal: controller.signal
  })
  .then(function(r) { return r.json(); })
  .then(function(data) {
    var el = document.getElementById('testResultContent');
    var fc = document.getElementById('testResultFileContent');
    var modal = document.getElementById('testResultModal');
    var label = document.getElementById('testResultModalLabel');
    var fetchBtn = document.getElementById('testResultFetchBtn');
    // Switch to plain-text pre view
    if (el) { el.style.display = ''; el.style.color = 'var(--bs-body-color)'; }
    if (fc) fc.style.display = 'none';
    if (el && modal) {
      if (label) label.innerHTML = '<i class="bi bi-terminal me-2"></i>Test Result (Plain Text &mdash; resolved)';
      if (data.error) {
        el.textContent = 'Error: ' + data.error;
        el.style.color = 'var(--bs-danger)';
        _showFetchBtn(false);
      } else {
        var output = data.output && data.output.length > 0 ? data.output : '(empty output)';
        _renderWithLineNumbers(el, output);
        var urls = _extractPathsFromOutput(output);
        if (urls.length > 0) {
          _showFetchBtn(true);
          if (fetchBtn) { fetchBtn._fetchUrls = urls; fetchBtn._fetchHostId = hostId; }
          var ta = document.getElementById('testResultUrlEdit');
          if (ta) ta.value = urls.join('\n');
        } else {
          _showFetchBtn(false);
        }
      }
      bootstrap.Modal.getOrCreateInstance(modal).show();
    } else {
      showToast(data.error ? 'Error: ' + data.error : (data.output || '(empty output)'),
                data.error ? 'danger' : 'info');
    }
  })
  .catch(function(err) {
    showToast(err.name === 'AbortError' ? 'Resolve timed out.' : 'Resolve failed: ' + err.message, 'danger');
  })
  .finally(function() {
    clearTimeout(timeoutId);
    _testDirRunning = false;
    if (btn) { btn.disabled = false; btn.innerHTML = origLabel; }
  });
}


function testSourceServer(aceEditor, hostId, lang, transferId, manualValues) {
  if (_testDirRunning) return;
  _testDirRunning = true;
  var script = aceEditor.getValue();
  var btn = document.getElementById('testDir');
  // Use a data attribute for the true original label so we never capture a "Running…" state
  if (btn && !btn.dataset.origLabel) btn.dataset.origLabel = btn.innerHTML;
  var origLabel = btn ? btn.dataset.origLabel : '';
  if (btn) {
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1" role="status"></span>Running\u2026';
  }
  var paramObj = { lang: lang, script: script };
  if (transferId) paramObj.transferId = transferId;
  if (manualValues) paramObj.valuesJson = JSON.stringify(manualValues);
  var params = new URLSearchParams(paramObj);
  var controller = new AbortController();
  var timeoutId = setTimeout(function() { controller.abort(); }, 120000);
  fetch('/do/transfer/host/edit/testScript/' + encodeURIComponent(hostId), {
    method: 'POST',
    credentials: 'same-origin',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: params.toString(),
    signal: controller.signal
  })
  .then(function(r) { return r.json(); })
  .then(function(data) {
    var el = document.getElementById('testResultContent');
    var fc = document.getElementById('testResultFileContent');
    var modal = document.getElementById('testResultModal');
    var label = document.getElementById('testResultModalLabel');
    var fetchBtn = document.getElementById('testResultFetchBtn');
    // Switch to the plain-text pre view (not structured file view)
    if (el) { el.style.display = ''; el.style.color = 'var(--bs-body-color)'; }
    if (fc) fc.style.display = 'none';
    if (el && modal) {
      var mover = data.mover ? ' &mdash; DataMover: ' + data.mover : '';
      if (label) label.innerHTML = '<i class="bi bi-terminal me-2"></i>Test Result (' + lang.toUpperCase() + mover + ')';
      if (data.error) {
        el.textContent = 'Error: ' + data.error;
        el.style.color = 'var(--bs-danger)';
        _showFetchBtn(false);
      } else {
        var output = data.output && data.output.length > 0 ? data.output : '(empty output)';
        _renderWithLineNumbers(el, output);
        // Detect HTTP URLs in the output and show Fetch Content button if found
        var urls = _extractPathsFromOutput(output);
        if (urls.length > 0) {
          _showFetchBtn(true);
          if (fetchBtn) { fetchBtn._fetchUrls = urls; fetchBtn._fetchHostId = hostId; }
          var ta = document.getElementById('testResultUrlEdit');
          if (ta) ta.value = urls.join('\n');
        } else {
          _showFetchBtn(false);
        }
      }
      bootstrap.Modal.getOrCreateInstance(modal).show();
    } else {
      showToast(data.error ? 'Error: ' + data.error : (data.output || '(empty output)'),
                data.error ? 'danger' : 'info');
    }
  })
  .catch(function(err) {
    var msg = err.name === 'AbortError'
      ? 'Test timed out after 2 minutes — the server did not respond in time.'
      : 'Test request failed: ' + err.message;
    showToast(msg, 'danger');
  })
  .finally(function() {
    clearTimeout(timeoutId);
    _testDirRunning = false;
    if (btn) { btn.disabled = false; btn.innerHTML = origLabel; }
  });
}


/** Raw content store for file preview blocks — avoids HTML-attribute quote-escaping issues. */
var _fcRawStore = {};

/** Ordered list of {id, source} for the current file preview — used by the global copy. */
var _fcBlockIds = [];

/** Assemble all file blocks into a single clean text blob and copy to clipboard.
 *  mode = 'raw' | 'pretty'
 *  Line numbers are omitted; blocks are separated by a text banner.
 */
function _fcGlobalCopy(mode) {
  var parts = [];
  _fcBlockIds.forEach(function(b, idx) {
    var block = document.getElementById(b.id);
    var content;
    if (b.error) {
      content = block ? block.innerText : '';
    } else {
      var raw = _fcRawStore[b.id] || '';
      if (mode === 'pretty') {
        content = (block && block.dataset.pretty === '1' && block.dataset.prettyText)
          ? block.dataset.prettyText
          : _fcFormatContent(raw, b.format || '');
      } else {
        content = raw;
      }
    }
    var lineCount = content ? content.trimEnd().split('\n').length : 0;
    var modeLabel = (mode === 'pretty' && b.canPretty) ? 'Pretty' : 'Raw';
    var sep = '='.repeat(72) + '\n'
            + '  [' + (idx + 1) + '/' + _fcBlockIds.length + '] '
            + (b.label ? '[' + b.label + '] ' : '')
            + '[' + modeLabel + '] '
            + lineCount + ' lines  ' + b.source + '\n'
            + '='.repeat(72);
    parts.push(sep + '\n' + content);
  });
  var text = parts.join('\n\n');
  var btn = document.getElementById('testResultCopyBtn');
  navigator.clipboard.writeText(text).then(function() {
    if (btn) {
      var orig = btn.innerHTML;
      btn.innerHTML = '<i class="bi bi-check2 me-1"></i>Copied!';
      setTimeout(function() { btn.innerHTML = orig; }, 1500);
    }
  });
}

/** Format raw content for a given format type — shared by Pretty toggle and global copy. */
function _fcFormatContent(raw, format) {
  if (!raw) return raw;
  try {
    if (format === 'json') return JSON.stringify(JSON.parse(raw), null, 2);
    if (format === 'html') return _indentHtml(raw);
    if (format === 'xml') {
      var parser = new DOMParser();
      var xmlDoc = parser.parseFromString(raw, 'text/xml');
      if (!xmlDoc.querySelector('parsererror')) return _xmlSerialize(xmlDoc.documentElement, 0);
    }
  } catch(e) {}
  return raw;
}

/** Render output text into the <pre> element with styled, non-selectable line numbers. */
function _renderWithLineNumbers(el, text) {
  el.dataset.rawText = text; // stored for the Copy button (line numbers are excluded)
  var lines = text.split('\n');
  var last = lines.length > 1 && lines[lines.length - 1] === '' ? lines.length - 1 : lines.length;
  var width = String(last).length;
  var html = lines.map(function(line, i) {
    if (i >= last) return ''; // drop trailing empty line
    var num = String(i + 1).padStart(width, ' ');
    return '<span style="color:var(--bs-secondary-color);opacity:0.55;user-select:none;'
         + 'border-right:1px solid var(--bs-border-color);padding-right:0.6em;margin-right:0.7em;'
         + 'font-variant-numeric:tabular-nums;">' + num + '</span>'
         + _escHtml(line);
  }).join('\n');
  el.innerHTML = html;
}

/**
 * Detect the format of fetched file content.
 * Returns { format, label, color, stats, canPretty } where color is a Bootstrap theme colour name.
 */
function _detectContentFormat(content) {
  var trimmed = (content || '').trim();
  if (!trimmed) return { format: 'empty', label: 'Empty', color: 'secondary', stats: '', canPretty: false };

  var lines = trimmed.split('\n');
  var totalLines = lines.length;
  var nonEmptyLines = lines.filter(function(l) { return l.trim().length > 0; });
  var lineCount = nonEmptyLines.length;

  // JSON object or array
  var fc = trimmed.charAt(0);
  if (fc === '{' || fc === '[') {
    try {
      var parsed = JSON.parse(trimmed);
      var stats = Array.isArray(parsed)
        ? totalLines + ' lines \u00B7 ' + parsed.length + ' items'
        : totalLines + ' lines \u00B7 ' + Object.keys(parsed).length + ' keys';
      return { format: 'json', label: 'JSON', color: 'success', stats: stats, canPretty: true };
    } catch(e) {}
  }

  // NDJSON / JSON Lines — each non-empty line is valid JSON
  if (lineCount >= 2) {
    var allJson = nonEmptyLines.every(function(l) {
      try { JSON.parse(l); return true; } catch(e) { return false; }
    });
    if (allJson) return { format: 'ndjson', label: 'NDJSON', color: 'success',
      stats: lineCount + ' records', canPretty: false };
  }

  // HTML
  if (/<!doctype\s+html/i.test(trimmed) || /<html[\s>]/i.test(trimmed)) {
    return { format: 'html', label: 'HTML', color: 'warning', stats: totalLines + ' lines', canPretty: true };
  }

  // XML (<?xml … or root element)
  if (trimmed.startsWith('<?xml') || /^<[a-zA-Z][^>]*>/.test(trimmed)) {
    return { format: 'xml', label: 'XML', color: 'warning', stats: totalLines + ' lines', canPretty: true };
  }

  // TSV — check first 8 lines for consistent tab counts
  var sample = nonEmptyLines.slice(0, 8);
  if (sample.length >= 2) {
    var tabCounts = sample.map(function(l) { return (l.match(/\t/g) || []).length; });
    if (tabCounts[0] > 0 && tabCounts.slice(1).every(function(c) { return Math.abs(c - tabCounts[0]) <= 1; })) {
      var cols = tabCounts[0] + 1;
      return { format: 'tsv', label: 'TSV', color: 'info',
        stats: lineCount + ' rows \u00B7 ' + cols + ' columns', canPretty: false };
    }
  }

  // CSV — comma or semicolon
  if (sample.length >= 2) {
    var seps = [',', ';'];
    for (var si = 0; si < seps.length; si++) {
      var sep = seps[si];
      var re = new RegExp(sep.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g');
      var counts = sample.map(function(l) { return (l.match(re) || []).length; });
      if (counts[0] > 0 && counts.slice(1).every(function(c) { return Math.abs(c - counts[0]) <= 1; })) {
        var csvCols = counts[0] + 1;
        return { format: 'csv', label: sep === ',' ? 'CSV' : 'CSV (;)', color: 'info',
          stats: lineCount + ' rows \u00B7 ' + csvCols + ' columns', canPretty: false };
      }
    }
  }

  return { format: 'text', label: 'Plain Text', color: 'secondary', stats: totalLines + ' lines', canPretty: false };
}

/** Toggle pretty-printing for a file content block. Called via inline onclick. */
function _prettyPrintToggle(btn, blockId) {
  var block = document.getElementById(blockId);
  if (!block) return;
  var isPretty = block.dataset.pretty === '1';
  if (isPretty) {
    // Restore original line-by-line view
    block.innerHTML = block.dataset.rawHtml || '';
    block.dataset.pretty = '0';
    btn.innerHTML = '<i class="bi bi-braces me-1"></i>Pretty';
    var rawStats = document.getElementById(blockId + '_stats');
    if (rawStats && block.dataset.rawStats) rawStats.textContent = block.dataset.rawStats;
  } else {
    if (!block.dataset.rawHtml) block.dataset.rawHtml = block.innerHTML;
    var raw = _fcRawStore[blockId] || '';
    var fmt = _fcFormatContent(raw, block.dataset.format || '');
    var fmtLines = fmt.split('\n');
    var prettyLineCount = fmtLines.length;
    var width = String(fmtLines.length).length;
    var html = fmtLines.map(function(line, i) {
      var num = String(i + 1).padStart(width, ' ');
      return '<div class="d-flex px-3 py-0" style="white-space:pre;line-height:1.6;'
           + (i % 2 === 0 ? '' : 'background:rgba(128,128,128,0.15)') + '">'
           + '<span style="color:var(--bs-secondary-color);opacity:0.55;user-select:none;'
           + 'border-right:1px solid var(--bs-border-color);padding-right:0.6em;margin-right:0.7em;'
           + 'flex-shrink:0;font-variant-numeric:tabular-nums;">' + _escHtml(num) + '</span>'
           + '<span style="white-space:pre-wrap;word-break:break-word;">' + _escHtml(line) + '</span>'
           + '</div>';
    }).join('');
    block.innerHTML = html;
    block.dataset.pretty = '1';
    block.dataset.prettyText = fmt;
    // Update stats span to show pretty line count
    var statsSpan = document.getElementById(blockId + '_stats');
    if (statsSpan) {
      if (!block.dataset.rawStats) block.dataset.rawStats = statsSpan.textContent;
      statsSpan.textContent = prettyLineCount + ' lines (pretty)';
    }
    btn.innerHTML = '<i class="bi bi-code-slash me-1"></i>Raw';
  }
}

/** Copy the current content of a file block (pretty or raw) to the clipboard. */
function _fcCopyBlock(btn, blockId) {
  var block = document.getElementById(blockId);
  var text = block && block.dataset.pretty === '1'
    ? (block.dataset.prettyText || '')
    : (_fcRawStore[blockId] || '');
  navigator.clipboard.writeText(text).then(function() {
    var orig = btn.innerHTML;
    btn.innerHTML = '<i class="bi bi-check2 me-1"></i>Copied!';
    setTimeout(function() { btn.innerHTML = orig; }, 1500);
  });
}

/**
 * String-based HTML indenter — works on raw text so <script>, <style>, comments
 * and all content are preserved exactly.  Never touches the DOM.
 */
function _indentHtml(html) {
  // Void elements that must not increase indent
  var VOID = /^(area|base|br|col|embed|hr|img|input|link|meta|param|source|track|wbr)$/i;
  // Raw-text elements whose content must be kept verbatim (not re-indented)
  var RAW  = /^(script|style|textarea|pre)$/i;
  var result = [];
  var depth = 0;

  // Tokenise: alternate between text runs and tags/comments/doctype
  var re = /(<(?:!--[\s\S]*?-->|![^>]*>|[^>]*>))/g;
  var last = 0, match;
  var tokens = [];
  while ((match = re.exec(html)) !== null) {
    if (match.index > last) tokens.push({ text: html.slice(last, match.index) });
    tokens.push({ tag: match[1] });
    last = re.lastIndex;
  }
  if (last < html.length) tokens.push({ text: html.slice(last) });

  var insideRaw = null; // tag name of current raw-text element, or null
  var rawBuffer = [];

  tokens.forEach(function(tok) {
    if (tok.text !== undefined) {
      var t = tok.text;
      if (insideRaw) { rawBuffer.push(t); return; }
      // Indent non-empty text lines
      t.split('\n').forEach(function(line) {
        var l = line.trim();
        if (l) result.push('  '.repeat(depth) + l);
      });
      return;
    }

    var tag = tok.tag;

    // Comments / DOCTYPE — emit as-is at current indent
    if (tag.startsWith('<!--') || tag.startsWith('<!')) {
      if (!insideRaw) {
        tag.split('\n').forEach(function(line, i) {
          result.push((i === 0 ? '  '.repeat(depth) : '  '.repeat(depth + 1)) + line.trim());
        });
      } else {
        rawBuffer.push(tag);
      }
      return;
    }

    // Closing tag
    var closeMatch = tag.match(/^<\/([a-zA-Z][^\s>]*)/);
    if (closeMatch) {
      var closeName = closeMatch[1];
      if (insideRaw && closeName.toLowerCase() === insideRaw) {
        // Flush raw buffer, then emit closing tag
        var rawContent = rawBuffer.join('');
        rawBuffer = [];
        // Indent each line of raw content one level deeper
        rawContent.split('\n').forEach(function(line) {
          if (line.trim()) result.push('  '.repeat(depth + 1) + line.trimEnd());
        });
        insideRaw = null;
        result.push('  '.repeat(depth) + tag);
      } else if (!insideRaw) {
        depth = Math.max(0, depth - 1);
        result.push('  '.repeat(depth) + tag);
      } else {
        rawBuffer.push(tag);
      }
      return;
    }

    // Opening or self-closing tag
    var openMatch = tag.match(/^<([a-zA-Z][^\s/>]*)/);
    if (!openMatch) { result.push('  '.repeat(depth) + tag); return; }
    var tagName = openMatch[1].toLowerCase();
    var isSelfClose = tag.endsWith('/>') || VOID.test(tagName);

    if (insideRaw) { rawBuffer.push(tag); return; }

    result.push('  '.repeat(depth) + tag);
    if (!isSelfClose) {
      if (RAW.test(tagName)) {
        insideRaw = tagName;
        rawBuffer = [];
      } else {
        depth++;
      }
    }
  });

  return result.join('\n');
}

/** Minimal XML serialiser for pretty-printing strict XML (not HTML). */
function _xmlSerialize(node, depth) {
  var indent = '  '.repeat(depth);
  if (node.nodeType === 3) { // text
    var t = node.textContent.trim();
    return t ? indent + t : '';
  }
  if (node.nodeType !== 1) return ''; // skip non-element non-text
  var tag = node.tagName;
  var attrs = Array.prototype.slice.call(node.attributes || []).map(function(a) {
    return ' ' + a.name + '="' + a.value.replace(/"/g, '&quot;') + '"';
  }).join('');
  var children = Array.prototype.slice.call(node.childNodes);
  var inner = children.map(function(c) { return _xmlSerialize(c, depth + 1); })
                      .filter(function(s) { return s.length > 0; });
  if (inner.length === 0) return indent + '<' + tag + attrs + '/>';
  if (inner.length === 1 && inner[0].indexOf('\n') === -1) {
    return indent + '<' + tag + attrs + '>' + inner[0].trim() + '</' + tag + '>';
  }
  return indent + '<' + tag + attrs + '>\n' + inner.join('\n') + '\n' + indent + '</' + tag + '>';
}

/** Extract file paths or URIs from test output — one candidate per line.
 *  Matches any URI scheme (http/ftp/s3/sftp/…) or absolute paths starting with '/'.
 *  Each non-empty line that looks like a single path/URI (no whitespace, no obvious error text) is a candidate.
 */
function _extractPathsFromOutput(text) {
  var seen = {}, paths = [];
  // Match full URIs with any scheme or absolute paths
  var re = /^([a-zA-Z][a-zA-Z0-9+\-.]*:\/\/[^\s"'<>]+|\/[^\s"'<>]+)$/;
  var lines = text.split('\n');
  lines.forEach(function(line) {
    var t = line.trim();
    if (!t || t.startsWith('#') || t.startsWith('Error')) return;
    var m = re.exec(t);
    if (m) {
      var p = m[0].replace(/[.,;)]+$/, '');
      if (!seen[p]) { seen[p] = true; paths.push(p); }
    }
  });
  return paths;
}

/**
 * Fetch the raw content of remote paths/URLs via the DataMover's ECtrans module.
 * POSTs to the dedicated fetchContent endpoint — works for HTTP, FTP, SFTP, S3, etc.
 */
function _fetchUrlContentViaMover(paths, hostId) {
  var fetchBtn = document.getElementById('testResultFetchBtn');
  var origLabel = fetchBtn ? fetchBtn.innerHTML : '';
  if (fetchBtn) {
    fetchBtn.disabled = true;
    fetchBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-1" role="status"></span>Fetching\u2026';
  }

  var params = new URLSearchParams({ sources: paths.slice(0, 5).join('\n'), maxBytes: 1048576 });
  var controller = new AbortController();
  var timeoutId = setTimeout(function() { controller.abort(); }, 120000);

  fetch('/do/transfer/host/edit/fetchContent/' + encodeURIComponent(hostId), {
    method: 'POST',
    credentials: 'same-origin',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: params.toString(),
    signal: controller.signal
  })
  .then(function(r) { return r.json(); })
  .then(function(data) {
    var pre   = document.getElementById('testResultContent');
    var fc    = document.getElementById('testResultFileContent');
    var label = document.getElementById('testResultModalLabel');
    var mover = data.mover ? ' &mdash; DataMover: ' + data.mover : '';
    var count = data.files ? data.files.length : paths.length;
    if (label) label.innerHTML = '<i class="bi bi-file-text me-2 text-info"></i>File Content Preview ('
        + count + ' file' + (count !== 1 ? 's' : '') + mover + ')';

    if (data.error) {
      // Top-level error (no files fetched at all) — show in pre
      if (pre) { pre.textContent = 'Error: ' + data.error; pre.style.color = 'var(--bs-danger)'; pre.style.display = ''; }
      if (fc)  { fc.style.display = 'none'; }
      _showFetchBtn(false);
    } else if (data.files && data.files.length > 0) {
      // Structured per-file results — render as styled blocks
      if (pre) pre.style.display = 'none';
      if (fc)  {
        fc.style.display = '';
        _fcBlockIds = []; // reset for this fetch
        var html = '';

        data.files.forEach(function(f) {
          var hasError = !!f.error;
          var borderCls = hasError ? 'border-danger' : 'border';
          var headerBg  = hasError ? 'bg-danger-subtle' : 'bg-body-tertiary';
          var icon      = hasError ? 'bi-exclamation-triangle-fill text-danger' : 'bi-link-45deg text-info';
          html += '<div class="' + borderCls + ' rounded mx-2 mb-3 mt-2 overflow-hidden">';

          if (!hasError) {
            // Detect format and build header extras (badge + stats + optional pretty-print button)
            var content = f.content || '';
            var det = _detectContentFormat(content);
            var blockId = 'fcBlock_' + Math.random().toString(36).slice(2);
            _fcBlockIds.push({ id: blockId, source: f.source, label: det.label, format: det.format, canPretty: det.canPretty });
            var badgeHtml = '<span class="badge rounded-pill ms-1 bg-' + det.color + '-subtle '
              + 'border border-' + det.color + '-subtle text-' + det.color
              + '" style="font-size:0.68rem;font-weight:600;letter-spacing:0.04em;">'
              + _escHtml(det.label) + '</span>';
            var statsId = blockId + '_stats';
            var statsHtml = det.stats
              ? '<span id="' + statsId + '" class="text-muted ms-2" style="font-size:0.7rem;white-space:nowrap;">' + _escHtml(det.stats) + '</span>'
              : '';
            var prettyHtml = det.canPretty
              ? '<button type="button" class="btn btn-outline-secondary btn-sm ms-auto py-0 px-2 flex-shrink-0"'
                + ' style="font-size:0.7rem;" onclick="_prettyPrintToggle(this,\'' + blockId + '\')">'
                + '<i class="bi bi-braces me-1"></i>Pretty</button>'
              : '';
            var copyHtml = '<button type="button" class="btn btn-outline-secondary btn-sm py-0 px-2 flex-shrink-0'
              + (det.canPretty ? '' : ' ms-auto') + '"'
              + ' style="font-size:0.7rem;" onclick="_fcCopyBlock(this,\'' + blockId + '\')">'
              + '<i class="bi bi-clipboard me-1"></i>Copy</button>';
            html += '<div class="' + headerBg + ' px-3 py-2 border-bottom d-flex align-items-center gap-1 flex-wrap">'
                  + '<i class="bi ' + icon + ' flex-shrink-0" style="font-size:0.9rem"></i>'
                  + '<code class="small text-break user-select-all" style="flex:1 1 auto;min-width:0;">' + _escHtml(f.source) + '</code>'
                  + badgeHtml + statsHtml + prettyHtml + copyHtml
                  + '</div>'
                  + '<div id="' + blockId + '" data-format="' + det.format + '" style="font-family:monospace;font-size:0.82rem;">';
            _fcRawStore[blockId] = content;
          } else {
            var errBlockId = 'fcBlock_' + Math.random().toString(36).slice(2);
            _fcBlockIds.push({ id: errBlockId, source: f.source, error: true });
            html += '<div id="' + errBlockId + '" class="' + headerBg + ' px-3 py-2 border-bottom d-flex align-items-start gap-2">'
                  + '<i class="bi ' + icon + ' flex-shrink-0 mt-1" style="font-size:0.9rem"></i>'
                  + '<code class="small text-break flex-grow-1 user-select-all">' + _escHtml(f.source) + '</code>'
                  + '</div>'
                  + '<div style="font-family:monospace;font-size:0.82rem;">';
          }

          if (hasError) {
            html += '<div class="px-3 py-1 text-danger">' + _escHtml('Error: ' + f.error) + '</div>';
          } else {
            var lines = (f.content || '(empty)').split('\n');
            // Remove a single trailing empty line that split() adds for content ending with \n
            if (lines.length > 1 && lines[lines.length - 1] === '') lines.pop();
            lines.forEach(function(line, i) {
              var num = String(i + 1).padStart(String(lines.length).length, ' ');
              html += '<div class="d-flex px-3 py-0" style="white-space:pre;line-height:1.6;'
                    + (i % 2 === 0 ? '' : 'background:rgba(128,128,128,0.15)') + '">'
                    + '<span style="color:var(--bs-secondary-color);opacity:0.55;user-select:none;'
                    + 'border-right:1px solid var(--bs-border-color);padding-right:0.6em;margin-right:0.7em;'
                    + 'flex-shrink:0;font-variant-numeric:tabular-nums;">' + _escHtml(num) + '</span>'
                    + '<span style="white-space:pre-wrap;word-break:break-word;">' + _escHtml(line) + '</span>'
                    + '</div>';
            });
          }
          html += '</div></div>';
        });

        // Sticky nav bar — only shown when there are 2+ files
        var navHtml = '';
        if (_fcBlockIds.length > 1) {
          navHtml = '<div id="fcNavBar" style="position:sticky;top:0;z-index:10;background:var(--bs-body-bg);'
            + 'border-bottom:1px solid var(--bs-border-color);padding:4px 8px;display:flex;flex-wrap:wrap;gap:4px;">';
          _fcBlockIds.forEach(function(b, idx) {
            // Show just the filename (last path segment) or first 30 chars of URL as label
            var label = b.source.replace(/[?#].*$/, '').split('/').filter(Boolean).pop() || b.source;
            if (label.length > 32) label = label.slice(0, 30) + '\u2026';
            var color = b.error ? 'var(--bs-danger)' : 'var(--bs-info)';
            navHtml += '<button type="button" onclick="(function(){'
              + 'var el=document.getElementById(\'' + b.id + '\');'
              + 'if(el){var fc=el.closest(\'#testResultFileContent\');'
              + 'if(fc){var nav=fc.querySelector(\'#fcNavBar\');var navH=nav?nav.offsetHeight:0;'
              + 'fc.scrollTop=el.closest(\'.border,.border-danger\').offsetTop-fc.offsetTop-navH-4;}}'
              + '})()" style="font-size:0.7rem;padding:1px 8px;border-radius:10px;border:1px solid '
              + color + ';background:transparent;color:' + color + ';cursor:pointer;white-space:nowrap;" '
              + 'title="' + _escHtml(b.source) + '">'
              + (idx + 1) + ' &mdash; ' + _escHtml(label)
              + '</button>';
          });
          navHtml += '</div>';
        }

        // Mixed-format warning: if multiple distinct content types detected across files,
        // warn that an Acquisition host processes one document type at a time.
        var uniqueFormats = _fcBlockIds
          .filter(function(b) { return !b.error && b.label && b.label !== 'Empty'; })
          .map(function(b) { return b.label; })
          .filter(function(v, i, a) { return a.indexOf(v) === i; });
        var warnHtml = '';
        if (uniqueFormats.length > 1) {
          warnHtml = '<div class="mx-2 mt-2 mb-1 alert alert-warning d-flex gap-2 py-2 px-3 mb-0" role="alert" style="font-size:0.8rem;">'
            + '<i class="bi bi-exclamation-triangle-fill flex-shrink-0 mt-1"></i>'
            + '<div><strong>Mixed content types detected: ' + uniqueFormats.map(_escHtml).join(', ') + '.</strong>'
            + ' An Acquisition host uses a single parser configured for one document type. '
            + 'To handle different formats, create a dedicated Acquisition host for each type '
            + 'and assign the appropriate parser to each one.</div>'
            + '</div>';
        }

        fc.innerHTML = navHtml + warnHtml + html;
        // Switch to rich copy controls (Raw/Pretty toggle + Copy All)
        _showFetchBtn(true);
      }
    }
  })
  .catch(function(err) {
    showToast(err.name === 'AbortError' ? 'Fetch timed out.' : 'Fetch failed: ' + err.message, 'danger');
  })
  .finally(function() {
    clearTimeout(timeoutId);
    if (fetchBtn) { fetchBtn.disabled = false; fetchBtn.innerHTML = origLabel; }
  });
}

/** Escape a string for safe insertion into HTML. */
function _escHtml(s) {
  if (!s) return '';
  return String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

/** Show or hide the Preview File Content button and URL edit area. */
function _showFetchBtn(show) {
  var btn      = document.getElementById('testResultFetchBtn');
  var area     = document.getElementById('testResultUrlArea');
  var copyFC   = document.getElementById('testResultCopyControls');
  var copySimp = document.getElementById('testResultCopySimpleBtn');
  if (btn)      btn.style.display      = show ? '' : 'none';
  if (area)     area.style.display     = show ? '' : 'none';
  // When file-content view is active the rich copy controls replace the simple one
  if (copyFC)   copyFC.style.display   = show ? '' : 'none';
  if (copySimp) copySimp.style.display = show ? 'none' : '';
}


(function() {
  document.addEventListener('click', function(e) {
    var btn = e.target.closest('#testResultFetchBtn');
    if (!btn) return;
    // Read URLs from the editable textarea (user may have modified them)
    var ta = document.getElementById('testResultUrlEdit');
    var urls = ta
      ? ta.value.split('\n').map(function(l) { return l.trim(); }).filter(function(l) { return l.length > 0; })
      : (btn._fetchUrls || []);
    if (urls.length === 0) return;
    _fetchUrlContentViaMover(urls, btn._fetchHostId);
  });
  // Clear raw content store when the test result modal closes (memory hygiene)
  var modal = document.getElementById('testResultModal');
  if (modal) {
    modal.addEventListener('hidden.bs.modal', function() { _fcRawStore = {}; _fcBlockIds = []; });
  }
})();


/** Placeholder families that require a live DataTransfer to resolve. */
var _TRANSFER_PLACEHOLDER_RE = /\$(?:dataFile|dataTransfer|destination|country|transferGroup|transferServer)\[|\$moverName\b/;

/**
 * Extract all unique placeholder tokens from a script string, e.g. "$dataFile[original]".
 * Returns an array of unique tokens like ["$dataFile[original]", "$dataTransfer[target]"].
 */
function _extractPlaceholders(script) {
  var re = /\$(?:dataFile|dataTransfer|destination|country|transferGroup|transferServer)\[[^\]]+\]|\$moverName\b/g;
  var seen = {}, result = [];
  var m;
  while ((m = re.exec(script)) !== null) {
    if (!seen[m[0]]) { seen[m[0]] = true; result.push(m[0]); }
  }
  return result;
}

/**
 * Pre-flight check before running a test script.
 * For Dissemination/non-Acquisition hosts: detects transfer-specific placeholders and
 * either presents a DataTransfer picker or a manual-values form before executing.
 *
 * @param {object} aceEditor - ACE editor instance
 * @param {string} hostId    - Host primary key
 * @param {string} lang      - "js" or "python"
 */
function testSourceServerPreflight(aceEditor, hostId, lang) {
  if (_testDirRunning) return;
  var script = aceEditor.getValue();
  var hostTypeEl = document.getElementById('type');
  var hostType = hostTypeEl ? hostTypeEl.value : '';
  var isDissemination = hostType !== 'Acquisition' && hostType !== 'Source';

  // Only intercept if the script has transfer-specific placeholders
  if (!isDissemination || !_TRANSFER_PLACEHOLDER_RE.test(script)) {
    testSourceServer(aceEditor, hostId, lang);
    return;
  }

  var placeholders = _extractPlaceholders(script);

  // Fetch today's transfers for this host
  fetch('/do/transfer/host/edit/recentTransfers/' + encodeURIComponent(hostId), {
    credentials: 'same-origin'
  })
  .then(function(r) { return r.json(); })
  .then(function(transfers) {
    if (transfers && transfers.length > 0) {
      _showPickTransferModal(aceEditor, hostId, lang, transfers, placeholders);
    } else {
      _showManualValuesModal(aceEditor, hostId, lang, placeholders);
    }
  })
  .catch(function() {
    // Network error fetching transfers — fall back to manual entry
    _showManualValuesModal(aceEditor, hostId, lang, placeholders);
  });
}

function _showPickTransferModal(aceEditor, hostId, lang, transfers, placeholders) {
  var listEl = document.getElementById('testPickTransferList');
  var runBtn  = document.getElementById('testPickRunBtn');
  var manBtn  = document.getElementById('testPickManualBtn');
  if (!listEl || !runBtn || !manBtn) { testSourceServer(aceEditor, hostId, lang); return; }

  // Build list
  listEl.innerHTML = '';
  var selectedId = null;
  transfers.forEach(function(t) {
    var item = document.createElement('button');
    item.type = 'button';
    item.className = 'list-group-item list-group-item-action d-flex justify-content-between align-items-start py-2 px-3';
    item.innerHTML = '<div class="me-auto"><div class="fw-semibold" style="font-size:0.85rem">'
      + escapeHtml(t.target || t.dataFileName || t.id)
      + '</div><div class="text-muted" style="font-size:0.75rem">'
      + escapeHtml(t.destination) + ' &mdash; ' + escapeHtml(t.statusCode)
      + '</div></div>'
      + '<span class="badge bg-secondary-subtle text-secondary-emphasis border rounded-pill ms-2" style="font-size:0.7rem">#' + escapeHtml(t.id) + '</span>';
    item.addEventListener('click', function() {
      listEl.querySelectorAll('.list-group-item').forEach(function(el) {
        el.classList.remove('active');
        el.style.color = '';
      });
      item.classList.add('active');
      selectedId = t.id;
      runBtn.disabled = false;
    });
    listEl.appendChild(item);
  });

  runBtn.disabled = true;
  runBtn.onclick = null;
  runBtn.addEventListener('click', function handler() {
    runBtn.removeEventListener('click', handler);
    bootstrap.Modal.getInstance(document.getElementById('testPickTransferModal')).hide();
    testSourceServer(aceEditor, hostId, lang, selectedId, null);
  }, { once: true });

  manBtn.onclick = null;
  manBtn.addEventListener('click', function handler() {
    manBtn.removeEventListener('click', handler);
    bootstrap.Modal.getInstance(document.getElementById('testPickTransferModal')).hide();
    _showManualValuesModal(aceEditor, hostId, lang, placeholders);
  }, { once: true });

  bootstrap.Modal.getOrCreateInstance(document.getElementById('testPickTransferModal')).show();
}

function _showManualValuesModal(aceEditor, hostId, lang, placeholders) {
  var listEl = document.getElementById('testManualValuesList');
  var runBtn  = document.getElementById('testManualRunBtn');
  if (!listEl || !runBtn) { testSourceServer(aceEditor, hostId, lang); return; }

  listEl.innerHTML = '';
  var inputs = {};
  placeholders.forEach(function(ph) {
    var id = 'mv_' + ph.replace(/[^a-zA-Z0-9]/g, '_');
    var row = document.createElement('div');
    row.className = 'mb-2';
    row.innerHTML = '<label class="form-label mb-1" style="font-size:0.82rem;font-family:monospace">' + escapeHtml(ph) + '</label>'
      + '<input type="text" class="form-control form-control-sm" id="' + id + '" placeholder="(leave blank to keep placeholder)">';
    listEl.appendChild(row);
    inputs[ph] = id;
  });

  runBtn.onclick = null;
  runBtn.addEventListener('click', function handler() {
    runBtn.removeEventListener('click', handler);
    var values = {};
    Object.keys(inputs).forEach(function(ph) {
      var val = document.getElementById(inputs[ph]);
      if (val && val.value.trim() !== '') values[ph] = val.value.trim();
    });
    bootstrap.Modal.getInstance(document.getElementById('testManualValuesModal')).hide();
    testSourceServer(aceEditor, hostId, lang, null, Object.keys(values).length > 0 ? values : null);
  }, { once: true });

  bootstrap.Modal.getOrCreateInstance(document.getElementById('testManualValuesModal')).show();
}

function escapeHtml(str) {
  if (str == null) return '';
  return String(str).replace(/[&<>"']/g, function(c) {
    return {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;','\'':'&#39;'}[c];
  });
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
		// Reset the undo history so Ctrl-Z cannot undo the programmatic wrapper-stripping
		aceEditor.session.getUndoManager().reset();
		aceEditor.session.getUndoManager().markClean();
	}
	return type;
}

// Lets' populate the help tab!

/* Find the nearest scrollable ancestor of an element */
function _findScrollableParent(el) {
	var p = el.parentElement;
	while (p && p !== document.body) {
		var s = window.getComputedStyle(p);
		if ((s.overflowY === 'auto' || s.overflowY === 'scroll') && p.scrollHeight > p.clientHeight) return p;
		p = p.parentElement;
	}
	return null;
}

function helpScrollToGroup(groupId) {
	var t = document.getElementById('hgrp-' + groupId);
	if (!t) return;
	/* Works for both .scrollable-tab and offcanvas content divs */
	var c = t.closest('.scrollable-tab') || _findScrollableParent(t);
	if (!c) return;
	var nav = c.querySelector('.help-nav');
	var navH = nav ? nav.getBoundingClientRect().height : 0;
	c.scrollTop += t.getBoundingClientRect().top - c.getBoundingClientRect().top - navH - 16;
}

function getHelpHtmlContent(completions, title) {
	var typeClass = {
		'boolean': 'bg-info text-dark',
		'string':  'bg-secondary',
		'integer': 'bg-warning text-dark',
		'numeric': 'bg-warning text-dark',
		'list':    'bg-primary'
	};

	/* Collect ordered unique groups */
	var groups = [], seen = {};
	completions.forEach(function(o) {
		var g = o.caption.split('.')[0];
		if (!seen[g]) { seen[g] = true; groups.push(g); }
	});

	var html = '<div class="help-content">';

	/* Sticky group nav — only shown when there is more than one group */
	if (groups.length > 1) {
		html += '<div class="help-nav">';
		groups.forEach(function(g) {
			html += '<button type="button" class="help-nav-pill" onclick="helpScrollToGroup(\'' + g + '\')">' + g + '</button>';
		});
		html += '</div>';
	}

	html += '<div class="help-title">' + title + '</div>';

	var currentGroup;
	for (var j = 0; j < completions.length; j++) {
		var o = completions[j];
		var group = o.caption.split('.')[0];
		if (!currentGroup || group !== currentGroup) {
			html += '<div class="help-group-hdr" id="hgrp-' + group + '">' + group + '</div>';
			currentGroup = group;
		}
		var bc = typeClass[o.type] || 'bg-light text-dark';
		html += '<div class="help-row" data-param="' + o.caption + '">'
			+ '<code>' + o.caption + '</code>'
			+ '<span class="help-type badge ' + bc + '">' + o.type + '</span>'
			+ '<span class="help-tip">' + escapeHtml(o.tips || '') + '</span>'
			+ '</div>';
	}
	if (!completions.length) {
		html += '<div style="color:#6c757d; font-size:0.75rem; padding:0.5rem 0;">No options available for this configuration.</div>';
	}
	return html + '</div>';
}

/**
 * Scroll the help panel to the entry matching paramName.
 * Works for both .scrollable-tab divs and offcanvas content divs.
 */
function scrollHelpToParam(helpContainerId, paramName) {
	if (!paramName) return;
	var helpPanel = document.getElementById(helpContainerId);
	if (!helpPanel) return;
	/* Clear any previous highlight */
	helpPanel.querySelectorAll('.help-row--active').forEach(function(el) {
		el.classList.remove('help-row--active');
	});
	var row = helpPanel.querySelector('.help-row[data-param="' + paramName + '"]');
	if (!row) return;
	row.classList.add('help-row--active');
	/* Use the helpPanel itself if it is the scroll container, otherwise walk up */
	var scrollEl = (helpPanel.scrollHeight > helpPanel.clientHeight)
		? helpPanel
		: (_findScrollableParent(row) || helpPanel);
	var containerRect = scrollEl.getBoundingClientRect();
	var rowRect = row.getBoundingClientRect();
	scrollEl.scrollTop = Math.max(0, scrollEl.scrollTop + (rowRect.top - containerRect.top) - 8);
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

function confirmCloseAll(path, count) {
    confirmationDialog({
        title: "Close All Sessions",
        message: count === 1 ? "Are you sure you want to close the active session?" : "Are you sure you want to close all " + count + " active sessions?",
        confirmText: "Close All",
        showLoading: true,
        onConfirm: function () { window.location = path; }
    });
}