<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
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
.scrollable-tab {
	height: 300px;
	overflow-y: auto;
	border: solid 1px lightgray;
	border-radius: 4px;
	padding: 8px;
	position: relative;
}
	min-width: 600px;
}
table.fields > tbody > tr > th {
	width: 1%;
	white-space: nowrap;
}
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

<table border=0>
<tr>
<td style="width:1%;white-space:nowrap;vertical-align:top">
<table class="fields">
<c:choose>
    <c:when test="${isInsert == 'true'}">
        <div class="form-info-banner">
            <i class="bi bi-shield-check text-primary flex-shrink-0"></i>
            Create a new Access Policy.
        </div>
    </c:when>
    <c:otherwise>
        <div class="form-info-banner">
            <i class="bi bi-shield-check text-primary flex-shrink-0"></i>
            Edit the Access Policy configuration.
        </div>
    </c:otherwise>
</c:choose>
<c:if test="${isInsert != 'true'}">
<tr>
<th>Name</th>
<td>${incomingPolicyActionForm.id}<html:hidden property="id" /></td>
</tr>
</c:if>
<c:if test="${isInsert == 'true'}">
<tr>
<th>Name</th>
<td>
	<div class="d-flex align-items-center gap-2">
		<input id="id" name="id" type="text"
			pattern="[a-zA-Z0-9]+([_.][a-zA-Z0-9]+)*"
			title="Must start and end with a letter or digit; '_' or '.' allowed as single separators (e.g. policy.read)"
			oninput="validatePatternInput(this, 'id-feedback')">
		<span id="id-feedback"></span>
	</div>
</td>
</tr>
</c:if>
<tr>
<th>Comment</th>
<td><html:text property="comment" /></td>
</tr>
<tr>
<th>Enabled</th>
<td><html:checkbox property="active" /></td>
</tr>
</table>
</td>

<td colspan="2" style="vertical-align:top;padding-left:1rem"><c:if test="${isInsert != 'true'}">
<style>
.assoc-card .card-header { display:flex; align-items:center; gap:.4rem; padding:.5rem .75rem; background:#f8f9fa; font-size:.85rem; }
.assoc-card .card-header .ms-auto { margin-left:auto !important; }
.assoc-chip { display:inline-flex; align-items:center; gap:.25rem; background:#e9ecef; border-radius:1rem; padding:.2rem .6rem; font-size:.8rem; margin:.15rem; }
.assoc-chip a { color:#6c757d; text-decoration:none; line-height:1; }
.assoc-chip a:hover { color:#dc3545; }
.assoc-chooser-item { color:#212529; font-size:.82rem; transition:background .15s; }
.assoc-chooser-item:hover { background:#e9ecef; }
.assoc-empty { display:flex; align-items:center; gap:.35rem; color:#856404; background:#fff3cd; border:1px solid #ffc107; border-radius:.25rem; font-size:.8rem; padding:.3rem .5rem; margin:0; }
</style>
<div class="row g-3 mt-0" style="max-width:480px">

  <%-- Destinations --%>
  <div class="col-12">
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

</div>
</c:if></td>
</tr>

<tr>
<td colspan="3" style="padding:0">
<div class="d-flex align-items-stretch" id="options-row">
<div id="options-label" style="flex:0 0 auto;background:var(--bs-tertiary-bg);border-right:2px solid var(--bs-border-color);font-weight:600;white-space:nowrap;padding:0.4rem 0.6rem">Options</div>
<div style="flex:1;min-width:0;padding:0.4rem 0.6rem">
<div class="accordion" id="policyOptionsAccordion">
<div class="accordion-item">
<h2 class="accordion-header" id="policyAccHeadProperties">
<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#policyAccProperties" aria-expanded="false" aria-controls="policyAccProperties">
Properties
</button>
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
<div class="accordion-item">
<h2 class="accordion-header" id="policyAccHeadHelp">
<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#policyAccHelp" aria-expanded="false" aria-controls="policyAccHelp">
Help
</button>
</h2>
<div id="policyAccHelp" class="accordion-collapse collapse" aria-labelledby="policyAccHeadHelp" data-bs-parent="#policyOptionsAccordion">
<div class="accordion-body p-2">
<div id="policyHelpContent" class="scrollable-tab"></div>
</div>
</div>
</div>
</div>
</div>
</div>
</td>
</tr>
</table>

<script>
var editorProperties = getEditorProperties(false, true, "data", "crystal");
editorProperties.setOptions({minLines: 10, maxLines: 20});

var completions = [
    ${incomingPolicyActionForm.completions}
    ];

$(document).ready(function() {
$('#policyHelpContent').html(getHelpHtmlContent(completions, 'Available Options for this Data Policy'));
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

editorProperties.addEventListener("changeSelection", function (event) {
    editorProperties.session.setAnnotations(
    getAnnotations(editorProperties, editorProperties.selection.getCursor().row));
    checkEachLine(editorProperties);
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

// When Help panel opens, scroll to the parameter at the current cursor position
var policyHelpBtn = document.querySelector('button[data-bs-target="#policyAccHelp"]');
if (policyHelpBtn) {
	policyHelpBtn.addEventListener('click', function() {
		setTimeout(function() {
			if (!document.getElementById('policyAccHelp').classList.contains('show')) return;
			var line = editorProperties.session.getLine(editorProperties.selection.getCursor().row) || '';
			line = line.trim();
			if (line && !line.startsWith('#') && !line.startsWith('//')) {
				var eqIdx = line.indexOf('=');
				var paramName = (eqIdx > 0 ? line.substring(0, eqIdx) : line).trim();
				if (paramName) scrollHelpToParam('policyHelpContent', paramName);
			}
		}, 400);
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
