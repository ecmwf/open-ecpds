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
    var modal = document.getElementById('testResultModal');
    var label = document.getElementById('testResultModalLabel');
    if (el && modal) {
      var mover = data.mover ? ' \u2014 DataMover: ' + data.mover : '';
      if (label) label.innerHTML = '<i class="bi bi-terminal me-2"></i>Test Result (' + lang.toUpperCase() + mover + ')';
      if (data.error) {
        el.textContent = 'Error: ' + data.error;
        el.style.color = 'var(--bs-danger)';
      } else {
        el.textContent = data.output && data.output.length > 0 ? data.output : '(empty output)';
        el.style.color = 'var(--bs-body-color)';
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