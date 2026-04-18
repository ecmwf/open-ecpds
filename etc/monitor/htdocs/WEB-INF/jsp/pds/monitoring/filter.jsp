<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<style>
.filter-chip {
  display: inline-block;
  padding: 2px 9px;
  border-radius: 20px;
  font-size: 0.75rem;
  cursor: pointer;
  border: 1px solid #ced4da;
  background: #f8f9fa;
  color: #adb5bd;
  transition: all 0.15s;
  white-space: nowrap;
  user-select: none;
  text-decoration: line-through;
}
.filter-chip:hover { border-color: #adb5bd; }
.filter-chip.selected {
  background: #e8f5e9;
  border-color: #28a745;
  color: #155724;
  font-weight: 500;
  text-decoration: none;
}
.filter-panel {
  position: fixed;
  z-index: 1050;
  min-width: 500px;
  background: var(--bs-tertiary-bg,#e9ecef);
  border: 1px solid var(--bs-border-color);
  border-top: 3px solid var(--bs-primary,#0d6efd) !important;
  border-radius: 0 0 8px 8px;
  box-shadow: 0 8px 28px rgba(0,0,0,0.18), 0 2px 6px rgba(0,0,0,0.10);
  padding: 12px 14px 10px;
  display: none;
}
.filter-col-label {
  font-size: 0.68rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #6c757d;
  margin-bottom: 5px;
}
</style>

<%-- Trigger button --%>
<div style="position:relative; display:inline-block;">
  <button id="btnShowHide" class="btn btn-sm ${not empty reqData.filtered ? 'btn-warning' : 'btn-outline-secondary'}"
          onclick="toggleFilterDialogue()" title="${not empty reqData.filtered ? 'Some items hidden: '.concat(reqData.filtered) : 'Show or hide destinations by status, type or network'}">
    <i class="bi bi-eye-fill me-1"></i>Show/Hide
    <c:if test="${not empty reqData.filtered}">
      <span class="badge bg-danger ms-1" style="font-size:0.65rem;">filtered</span>
    </c:if>
  </button>

  <%-- Dropdown panel --%>
  <div id="filter" class="filter-panel">

    <div class="mb-2" style="font-size:0.78rem; color:#495057;">
      <i class="bi bi-info-circle me-1 text-muted"></i>
      <strong>Highlighted</strong> items are <span class="text-success fw-semibold">shown</span>.
      Click to toggle visibility.
    </div>

    <div class="d-flex gap-4 mb-3">

      <%-- Status column --%>
      <div>
        <div class="filter-col-label">Status</div>
        <div class="d-flex flex-column gap-1">
          <c:forEach var="status" items="${reqData.statusOptions}">
            <div id="filter_status_${status.first}" class="filter-chip selected"
                 onclick="filter('status','${status.first}')">
              <c:out value="${status.second}"/>
            </div>
          </c:forEach>
        </div>
      </div>

      <%-- Type column --%>
      <div style="max-height:220px; overflow-y:auto;">
        <div class="filter-col-label">Type</div>
        <div class="d-flex flex-column gap-1">
          <c:forEach var="type" items="${reqData.typeOptions}">
            <div id="filter_type_${type.first}" class="filter-chip selected"
                 onclick="filter('type','${type.first}')">
              <c:out value="${type.second}"/>
            </div>
          </c:forEach>
        </div>
      </div>

      <%-- Network column --%>
      <div>
        <div class="filter-col-label">Network</div>
        <div class="d-flex flex-column gap-1">
          <c:forEach var="network" items="${reqData.networkOptions}">
            <div id="filter_network_${network.first}" class="filter-chip selected"
                 onclick="filter('network','${network.first}')">
              <c:out value="${network.second}"/>
            </div>
          </c:forEach>
        </div>
      </div>

    </div>

    <%-- Footer actions --%>
    <div class="d-flex gap-2 pt-2 border-top align-items-center flex-wrap">
      <button class="btn btn-sm btn-outline-success" onclick="showAll()" title="Show everything">
        <i class="bi bi-eye-fill me-1"></i>Show All
      </button>
      <button class="btn btn-sm btn-outline-secondary" onclick="hideAll()" title="Hide everything">
        <i class="bi bi-eye-slash-fill me-1"></i>Hide All
      </button>
      <button class="btn btn-sm btn-primary ms-auto" onclick="applyFilter()">
        <i class="bi bi-check2 me-1"></i>Apply
      </button>
      <button class="btn btn-sm btn-outline-secondary" onclick="toggleFilterDialogue()">
        <i class="bi bi-x"></i>
      </button>
    </div>
  </div>
</div>

<script>
  // Initialise: all chips start as selected (= shown).
  // setFromParameters marks the EXCLUDED ones (from URL) as unselected.
  function setFromParameters(values) {
    for (var i in values) {
      var bits = values[i].split("|");
      var type = bits[0];
      for (var j = 1; j < bits.length; j++) {
        if (bits[j] !== "") {
          // This value is in the exclusion list -> mark as NOT selected (hidden)
          var el = document.getElementById("filter_" + type + "_" + bits[j]);
          if (el) el.classList.remove('selected');
        }
      }
    }
  }

  function filter(what, which) {
    var el = document.getElementById("filter_" + what + "_" + which);
    if (el) el.classList.toggle('selected');
  }

  function showAll() {
    document.querySelectorAll('#filter [id^="filter_"]').forEach(function(el) {
      el.classList.add('selected');
    });
  }

  function hideAll() {
    document.querySelectorAll('#filter [id^="filter_"]').forEach(function(el) {
      el.classList.remove('selected');
    });
  }

  function applyFilter() {
    var dic = { type: "", status: "", network: "" };
    // Collect the UNSELECTED chips -> these are excluded by the backend
    document.querySelectorAll('#filter [id^="filter_"]:not(.selected)').forEach(function(el) {
      var bits = el.id.split("_");
      dic[bits[1]] = dic[bits[1]] ? dic[bits[1]] + "|" + bits[2] : bits[2];
    });
    var url = "?";
    for (var key in dic) url += key + "=" + dic[key] + "&";
    window.location = url;
  }

  function toggleFilterDialogue() {
    var panel = document.getElementById('filter');
    var btn = document.getElementById('btnShowHide');
    if (panel.style.display === 'block') { panel.style.display = 'none'; return; }
    if (panel.parentElement !== document.body) { document.body.appendChild(panel); }
    panel.style.position = 'absolute';
    panel.style.zIndex = '9999';
    var r = btn.getBoundingClientRect();
    var sy = window.pageYOffset || document.documentElement.scrollTop;
    var sx = window.pageXOffset || document.documentElement.scrollLeft;
    panel.style.top   = (r.bottom + sy + 4) + 'px';
    panel.style.left  = (r.left + sx) + 'px';
    panel.style.right = 'auto';
    panel.style.display = 'block';
  }

  function setDisseminationType() {
    showAll();
    // Hide everything except dissemination types (9-17,19,21,22,24-26,28-30)
    var show = [9,10,11,12,13,14,15,16,17,19,21,22,24,25,26,28,29,30].map(String);
    document.querySelectorAll('#filter [id^="filter_type_"]').forEach(function(el) {
      var typeId = el.id.replace('filter_type_', '');
      if (show.indexOf(typeId) === -1) el.classList.remove('selected');
    });
    applyFilter();
  }

  function setAcquisitionType() {
    showAll();
    var show = [0,1,2,3,4,5,6,7,8,18,20,21,22,23,27].map(String);
    document.querySelectorAll('#filter [id^="filter_type_"]').forEach(function(el) {
      var typeId = el.id.replace('filter_type_', '');
      if (show.indexOf(typeId) === -1) el.classList.remove('selected');
    });
    applyFilter();
  }

  // Apply saved URL params (exclusion list -> unselect those chips)
  setFromParameters(['status|${monSesForm.status}', 'type|${monSesForm.type}', 'network|${monSesForm.network}']);

  // Close panel on outside click
  document.addEventListener('click', function(e) {
    var panel = document.getElementById('filter');
    var btn = document.getElementById('btnShowHide');
    if (panel && panel.style.display === 'block' && !panel.contains(e.target) && btn && !btn.contains(e.target)) {
      panel.style.display = 'none';
    }
  });
</script>
