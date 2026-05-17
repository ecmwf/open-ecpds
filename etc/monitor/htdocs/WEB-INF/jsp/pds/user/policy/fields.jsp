<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:useAttribute name="isInsert" classname="java.lang.String" />

<style>
.ace-panel {
	max-width: 100%;
	overflow: hidden;
	border: solid 1px lightgray;
	border-radius: 4px;
	margin-top: 8px;
	margin-bottom: 4px;
}
.acc-help-btn {
    position: absolute; top: 50%; right: 3rem;
    transform: translateY(-50%);
    color: var(--bs-secondary-color); font-size: 0.9rem; line-height: 1;
    cursor: pointer; z-index: 10;
    transition: color 0.15s;
}
.acc-help-btn:hover { color: var(--bs-primary); }
.acc-help-btn.acc-help-active { color: var(--bs-primary); }
</style>

<script>
function validate(path, message) {
    confirmationDialog({
        title: "Please Confirm",
        message: message,
        onConfirm: function () { window.location = path; },
        onCancel: function () {}
    });
}
</script>

<div class="row g-3">
<div class="col-lg-6">
<div class="card">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-shield-check text-primary"></i>
<span class="fw-semibold">
<c:choose>
  <c:when test="${isInsert == 'true'}">Create Data Policy</c:when>
  <c:otherwise>Edit Data Policy</c:otherwise>
</c:choose>
</span>
</div>
<div class="card-body">
<div class="row g-2 align-items-center">
<div class="col-sm-4"><label class="col-form-label col-form-label-sm fw-semibold text-muted">Name</label></div>
<div class="col-sm-8">
<c:if test="${isInsert != 'true'}">${incomingPolicyActionForm.id}<html:hidden property="id" /></c:if>
<c:if test="${isInsert == 'true'}">
<div class="d-flex align-items-center gap-2">
<input id="id" name="id" type="text" class="form-control form-control-sm"
    pattern="[a-zA-Z0-9]+([_.][a-zA-Z0-9]+)*"
    title="Must start and end with a letter or digit; '_' or '.' allowed as single separators (e.g. policy.read)"
    oninput="validatePatternInput(this, 'id-feedback')">
<span id="id-feedback"></span>
</div>
</c:if>
</div>
</div>
<div class="row g-2 align-items-center mt-1">
<div class="col-sm-4"><label class="col-form-label col-form-label-sm fw-semibold text-muted">Comment</label></div>
<div class="col-sm-8"><html:text property="comment" styleClass="form-control form-control-sm" /></div>
</div>
<div class="row g-2 align-items-center mt-1">
<div class="col-sm-4"><label class="col-form-label col-form-label-sm fw-semibold text-muted">Enabled</label></div>
<div class="col-sm-8"><div class="form-check form-switch mb-0"><html:checkbox property="active" styleClass="form-check-input" /></div></div>
</div>
</div>
</div>
</div>

<c:if test="${isInsert != 'true'}">
<div class="col-lg-6">
<style>
.assoc-card .card-header { display:flex; align-items:center; gap:.4rem; padding:.5rem .75rem; background:var(--bs-tertiary-bg); font-size:.85rem; }
.assoc-card .card-header .ms-auto { margin-left:auto !important; }
.assoc-chip { display:inline-flex; align-items:center; gap:.25rem; background:var(--bs-secondary-bg); border-radius:1rem; padding:.2rem .6rem; font-size:.8rem; margin:.15rem; }
.assoc-chip a { color:var(--bs-secondary-color); text-decoration:none; line-height:1; }
.assoc-chip a:hover { color:#dc3545; }
.assoc-chooser-item { color:var(--bs-body-color); font-size:.82rem; transition:background .15s; }
.assoc-chooser-item:hover { background:var(--bs-secondary-bg); }
.assoc-empty { display:flex; align-items:center; gap:.35rem; color:#856404; background:#fff3cd; border:1px solid #ffc107; border-radius:.25rem; font-size:.8rem; padding:.3rem .5rem; margin:0; }
</style>
<div class="card assoc-card">
  <div class="card-header">
    <i class="bi bi-geo-alt text-secondary"></i>
    <strong>Destinations</strong>
    <button type="button" class="btn btn-sm btn-outline-primary ms-auto"
            data-bs-toggle="collapse" data-bs-target="#destChooser">
      <i class="bi bi-plus-lg"></i> Add
    </button>
  </div>
  <div class="card-body p-2">
    <c:choose>
      <c:when test="${empty incomingPolicyActionForm.destinations}">
        <p class="text-muted small mb-2"><em>No destinations assigned.</em></p>
      </c:when>
      <c:otherwise>
        <div class="d-flex flex-wrap mb-2">
          <c:forEach var="destination" items="${incomingPolicyActionForm.destinations}">
            <span class="assoc-chip">
              <c:choose>
                <c:when test="${not empty destination.comment}"><span title="${destination.comment}">${destination.id}</span></c:when>
                <c:otherwise>${destination.id}</c:otherwise>
              </c:choose>
              <a href="javascript:validate('<bean:message key="policy.basepath"/>/edit/update/<c:out value="${incomingPolicyActionForm.id}"/>/deleteDestination/<c:out value="${destination.name}"/>','<bean:message key="ecpds.policy.deleteDestination.warning" arg0="${destination.name}" arg1="${incomingPolicyActionForm.id}"/>')" title="Remove"><i class="bi bi-x-lg"></i></a>
            </span>
          </c:forEach>
        </div>
      </c:otherwise>
    </c:choose>
    <div class="collapse" id="destChooser">
      <c:choose>
        <c:when test="${empty incomingPolicyActionForm.destinationOptions}">
          <p class="assoc-empty"><i class="bi bi-exclamation-triangle-fill flex-shrink-0"></i> No destinations available to add.</p>
        </c:when>
        <c:otherwise>
          <input type="text" class="form-control form-control-sm assoc-search mb-1" oninput="assocSearch(this)" placeholder="Search destinations...">
          <div style="max-height:180px;overflow-y:auto">
            <c:forEach var="column" items="${incomingPolicyActionForm.destinationOptions}">
              <a href="/do/user/policy/edit/update/${incomingPolicyActionForm.id}/addDestination/${column.name}"
                 class="assoc-chooser-item d-flex align-items-center gap-1 px-2 py-1 rounded text-decoration-none">
                <i class="bi bi-plus-circle text-success small flex-shrink-0"></i>
                <span title="${column.value}">${column.name}</span>
              </a>
            </c:forEach>
          </div>
        </c:otherwise>
      </c:choose>
    </div>
  </div>
</div>
</div>
</c:if>
</div>

<div class="mt-3">
<div class="card">
<div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
<i class="bi bi-sliders text-primary"></i>
<span class="fw-semibold">Options</span>
</div>
<div class="card-body p-2">
<div class="accordion" id="policyOptionsAccordion">
<div class="accordion-item">
<h2 class="accordion-header" id="policyAccHeadProperties" style="position:relative;">
<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#policyAccProperties" aria-expanded="false" aria-controls="policyAccProperties">
Properties
</button>
<span role="button" tabindex="0" class="acc-help-btn" id="policyPropsHelpBtn"
	onclick="openPolicyHelp();" onkeydown="if(event.key==='Enter'||event.key===' ')openPolicyHelp();" title="Open properties reference">
	<i class="bi bi-question-circle"></i>
</span>
</h2>
<div id="policyAccProperties" class="accordion-collapse collapse" aria-labelledby="policyAccHeadProperties" data-bs-parent="#policyOptionsAccordion">
<div class="accordion-body p-2">
<pre id="data" class="ace-panel"><c:out value="${incomingPolicyActionForm.data}" /></pre>
<textarea id="data" name="data" style="display: none;"></textarea>
<div class="d-flex align-items-center gap-2 mt-2">
<button type="button" class="btn btn-sm btn-outline-secondary" onclick="formatSource(editorProperties); return false">Format</button>
<button type="button" class="btn btn-sm btn-outline-secondary" onclick="clearSource(editorProperties); return false">Clear</button>
<small class="text-muted ms-auto"><i class="bi bi-keyboard"></i> Ctrl+Space for completions</small>
</div>
</div>
</div>
</div>
</div>
</div>
</div>
</div>

<%-- Help offcanvas panel --%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="policyHelpOffcanvas"
     aria-labelledby="policyHelpOffcanvasLabel" style="width:min(480px,42vw);">
	<div class="offcanvas-header border-bottom py-2 px-3">
		<h6 class="offcanvas-title mb-0 fw-semibold" id="policyHelpOffcanvasLabel">
			<i class="bi bi-book me-2 text-primary"></i>Properties Reference
		</h6>
		<button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
	</div>
	<div class="offcanvas-body p-0" style="display:flex; flex-direction:column; overflow:hidden;">
		<div id="policyHelpNav" style="flex:0 0 auto; padding:0 1rem;"></div>
		<div id="policyHelpContent" style="padding:0.75rem 1rem; overflow-y:auto; flex:1; min-height:0;"></div>
	</div>
</div>

<script>
var editorProperties = getEditorProperties(false, true, "data", "crystal");
editorProperties.setOptions({minLines: 10, maxLines: 20});

var completions = [
    ${incomingPolicyActionForm.completions}
    ];

$(document).ready(function() {
$('#policyHelpContent').html(getHelpHtmlContent(completions, 'Available Options for this Data Policy'));
var navEl = document.querySelector('#policyHelpContent .help-nav');
if (navEl) document.getElementById('policyHelpNav').appendChild(navEl);
});

var customCompleter = {
  getCompletions: function(editor, session, pos, prefix, callback) {
      var line = session.getLine(editor.getCursorPosition().row);
   completions.forEach(function(completion) {
      completion.value = completion.caption + ' = ""';
    });
      var matchingCompletions = completions.filter(function(completion) {
      return !checkIfExist(editor, completion.value) && (line.length === 0 || completion.value.startsWith(line));
    }).map(function(completion) {
      completion.value = prefix + completion.value.substring(line.length);
      return completion;
    });
      if (matchingCompletions.length > 0) {
        callback(null, matchingCompletions);
      } else {
        callback(null, []);
      }
    }
};

editorProperties.completers = [customCompleter];

function _scrollPolicyHelpToCursor() {
	var row = editorProperties.selection.getCursor().row;
	var line = editorProperties.session.getLine(row) || '';
	line = line.trim();
	if (line && !line.startsWith('#') && !line.startsWith('//')) {
		var eqIdx = line.indexOf('=');
		var paramName = (eqIdx > 0 ? line.substring(0, eqIdx) : line).trim();
		if (paramName) scrollHelpToParam('policyHelpContent', paramName);
	}
}

editorProperties.addEventListener("changeSelection", function (event) {
    editorProperties.session.setAnnotations(
    getAnnotations(editorProperties, editorProperties.selection.getCursor().row));
    checkEachLine(editorProperties);
	var _oc = document.getElementById('policyHelpOffcanvas');
	if (_oc && _oc.classList.contains('show')) _scrollPolicyHelpToCursor();
    });

editorProperties.getSession().on("change", function(e) {
  if (e.action === "insert" && e.lines.length == 1 && e.lines[0] !== '"' && e.lines[0].endsWith('"')) {
    setTimeout(function() {
editorProperties.moveCursorTo(e.end.row, e.end.column - 1);
    editorProperties.selection.clearSelection();
    }, 0);
  }
});

var textareaProperties = $('textarea[name="data"]');
textareaProperties.closest('form').submit(function() {
textareaProperties.val(editorProperties.getSession().getValue());
});

makeResizable(editorProperties);

document.getElementById('policyAccProperties').addEventListener('shown.bs.collapse', function() {
	editorProperties.resize(true);
});

window.openPolicyHelp = function() {
	var el = document.getElementById('policyHelpOffcanvas');
	if (el) bootstrap.Offcanvas.getOrCreateInstance(el).show();
};
var _policyOffcanvasEl = document.getElementById('policyHelpOffcanvas');
if (_policyOffcanvasEl) {
	_policyOffcanvasEl.addEventListener('show.bs.offcanvas', function() {
		var btn = document.getElementById('policyPropsHelpBtn');
		if (btn) btn.classList.add('acc-help-active');
	});
	_policyOffcanvasEl.addEventListener('shown.bs.offcanvas', function() {
		_scrollPolicyHelpToCursor();
	});
	_policyOffcanvasEl.addEventListener('hide.bs.offcanvas', function() {
		var btn = document.getElementById('policyPropsHelpBtn');
		if (btn) btn.classList.remove('acc-help-active');
	});
}

window.addEventListener('resize', function() {
	editorProperties.resize(true);
});

$('#id').bind('keypress', function(event) {
var regex = new RegExp("^[a-zA-Z0-9_.]+$");
var key = String.fromCharCode(!event.charCode ? event.which : event.charCode);
if (!regex.test(key)) {
event.preventDefault();
return false;
}
});
</script>
