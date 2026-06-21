<!DOCTYPE html>

<%@ page session="true" import ="ecmwf.web.view.taglibs.util.TagUtils"%>
<%
    boolean _showFeedbackBtn = true;
    Object _sessionUser = session.getAttribute(ecmwf.web.model.users.User.SESSION_KEY);
    if (_sessionUser instanceof ecmwf.ecpds.master.plugin.http.model.ecuser.WebUser) {
        _showFeedbackBtn = ((ecmwf.ecpds.master.plugin.http.model.ecuser.WebUser) _sessionUser).isShareFeedbackEnabled();
    }
%>

<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>

<tiles:useAttribute id="title" name="title" ignore="true" classname="java.lang.String"/>
<tiles:useAttribute id="titleKey" name="title.key" ignore="true" classname="java.lang.String"/>
<tiles:useAttribute id="titleBeanName" name="title.bean.name" ignore="true" classname="java.lang.String"/>
<tiles:useAttribute id="titleBeanProperty" name="title.bean.property" ignore="true" classname="java.lang.String"/>
<tiles:useAttribute id="titleExpression" name="title.expression" ignore="true" classname="java.lang.String"/>

<%
	String theTitle = null;
	if (titleExpression!=null) {
		theTitle = TagUtils.parseMultiExpressionText(pageContext,titleExpression);
	} else if (titleBeanName!=null && titleBeanProperty!=null) {
		theTitle = TagUtils.resolveQualifiedName(pageContext,titleBeanName+"."+titleBeanProperty).toString();
	} else if (titleBeanName!=null) {
		theTitle = TagUtils.resolveQualifiedName(pageContext,titleBeanName).toString();
	} else if (titleKey!=null) {
		theTitle = TagUtils.getResource(pageContext,titleKey,"Resource not found");
	} else if (title!=null) {
		theTitle = title;
	} else {
		title="Title not set";
	}
%>

<tiles:insert name="html.head">
	<tiles:put name="title"><%=theTitle%></tiles:put>
</tiles:insert>

<body>

<style>
#loadingDiv{position:fixed;top:50%;left:50%;width:120px;height:120px;margin:-60px 0 0 -60px;background:var(--bs-body-bg,#fff);border-radius:16px;box-shadow:0 8px 24px rgba(0,0,0,.25);display:flex;align-items:center;justify-content:center;z-index:2147483647;}
.loader{border-radius:50%;width:56px;height:56px;border:6px solid var(--bs-border-color,rgba(0,0,0,.1));border-left-color:var(--bs-primary,#0d6efd);animation:load8 .9s linear infinite;}
@keyframes load8{0%{transform:rotate(0deg);}100%{transform:rotate(360deg);}}
</style>

		<tiles:insert name="header">
			<tiles:put name="title"><%=theTitle%></tiles:put>
			<tiles:put name="submenu_width">100%</tiles:put>
			<tiles:put name="location"><tiles:getAsString name="location"/></tiles:put>
			<tiles:put name="submenu_top"><tiles:getAsString name="submenu_top"/></tiles:put>
		</tiles:insert>

<div class="offcanvas offcanvas-start" tabindex="-1" id="sidebarMenu" aria-labelledby="sidebarMenuLabel">
    <div class="offcanvas-header border-bottom py-2 px-3">
        <span class="fw-semibold small" id="sidebarMenuLabel"><i class="bi bi-layout-sidebar me-2"></i>Navigation</span>
        <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
    </div>
    <div class="offcanvas-body p-0 d-flex flex-column">
        <div class="flex-grow-1 overflow-auto">
            <table class="spareBox2">
                <tr><td><a href="/do/start"><i class="bi bi-house-fill"></i> Home</a></td></tr>
            </table>
            <jsp:include page="/WEB-INF/jsp/pds/submenu.jsp" />
        </div>
        <div class="px-3 py-2" style="border-top:1px solid var(--bs-border-color); flex-shrink:0;">
            <a href="https://github.com/ecmwf/open-ecpds" target="_blank" rel="noopener"
               style="display:flex; align-items:center; gap:6px; font-size:0.78rem; color:var(--bs-secondary-color); text-decoration:none;">
                <i class="bi bi-github"></i> GitHub Repository
                <i class="bi bi-box-arrow-up-right" style="font-size:0.65rem; opacity:0.6;"></i>
            </a>
        </div>
    </div>
</div>

	<div id="loadingDiv"><div class="loader"></div></div>
	<div style="display: none; max-width: none; margin: 0;" id="contentDiv">
	<div id="outerTable" class="content" style="width:100%; padding-left:4px; padding-right:4px;">
		<tiles:get name="content" />
	</div>
	</div>

<!-- UI Help offcanvas -->
<div class="offcanvas offcanvas-end" tabindex="-1" id="uiHelpOffcanvas" aria-labelledby="uiHelpOffcanvasLabel" style="width:360px;max-width:95vw;">
  <div class="offcanvas-header border-bottom py-2 px-3">
    <span class="fw-semibold small" id="uiHelpOffcanvasLabel"><i class="bi bi-question-circle me-2 text-primary"></i>Interface Guide</span>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>
  <div class="offcanvas-body px-3 py-2" style="font-size:0.83rem; overflow-y:auto;">

    <div class="mb-3">
      <div class="fw-semibold mb-1 d-flex align-items-center gap-2"><i class="bi bi-list text-secondary"></i> Navigation Menu</div>
      <p class="mb-1 text-muted">The <strong>&#9776; hamburger button</strong> (top-left) opens the side navigation panel, providing access to all sections and subsections available on the current page.</p>
    </div>

    <hr class="my-2">

    <div class="mb-3">
      <div class="fw-semibold mb-1 d-flex align-items-center gap-2"><i class="bi bi-moon-fill text-secondary"></i> Light / Dark Theme</div>
      <p class="mb-1 text-muted">The <strong>moon / sun button</strong> (top-right) toggles between light and dark display mode. Your preference is remembered across sessions.</p>
    </div>

    <hr class="my-2">

    <div class="mb-3">
      <div class="fw-semibold mb-1 d-flex align-items-center gap-2"><i class="bi bi-info-circle text-primary"></i> Tooltips &amp; Info Icons</div>
      <p class="mb-1 text-muted"><strong>Hover</strong> over any <i class="bi bi-question-circle text-muted"></i> or <i class="bi bi-info-circle text-muted"></i> icon next to a field to read a description. Some icons open a full reference panel when clicked.</p>
    </div>

    <hr class="my-2">

    <div class="mb-3">
      <div class="fw-semibold mb-1 d-flex align-items-center gap-2"><i class="bi bi-list-ol text-secondary"></i> Page Size Selector</div>
      <p class="mb-1 text-muted">The <i class="bi bi-list-ol text-muted"></i> dropdown in table headers controls how many rows are shown per page. Your choice is remembered separately for each table.</p>
    </div>

    <hr class="my-2">

    <div class="mb-3">
      <div class="fw-semibold mb-1 d-flex align-items-center gap-2"><i class="bi bi-arrow-down-up text-secondary"></i> Column Sorting</div>
      <p class="mb-1 text-muted">Click any <strong>column header</strong> to sort. Columns cycle through three states:</p>
      <ul class="mb-1 ps-3 text-muted">
        <li><i class="bi bi-arrow-down-up"></i> <strong>Unsorted</strong> &mdash; natural order</li>
        <li><i class="bi bi-sort-up"></i> <strong>Ascending</strong> &mdash; A &rarr; Z / oldest first</li>
        <li><i class="bi bi-sort-down"></i> <strong>Descending</strong> &mdash; Z &rarr; A / newest first</li>
      </ul>
    </div>

    <hr class="my-2">

    <div class="mb-3">
      <div class="fw-semibold mb-1 d-flex align-items-center gap-2"><i class="bi bi-search text-secondary"></i> Search &amp; Filter Boxes</div>
      <p class="mb-1 text-muted">The <i class="bi bi-search text-muted"></i> search box above a table filters rows instantly as you type. It matches any column. Use it together with the page size selector for faster navigation on large tables.</p>
    </div>

    <hr class="my-2">

    <div class="mb-3">
      <div class="fw-semibold mb-1 d-flex align-items-center gap-2"><i class="bi bi-funnel text-secondary"></i> Query Builder</div>
      <p class="mb-1 text-muted">Some pages (e.g. Transfer History, Data Files) include an advanced <strong>query builder</strong>. Click the <i class="bi bi-sliders2"></i> <strong>Filter</strong> button next to the search box to open the condition panel. Fill in the available condition rows (field, operator, value), then click <strong>Apply &amp; Search</strong> to run the query. You can also type filter expressions directly in the search box.</p>
    </div>

    <hr class="my-2">

    <div class="mb-3">
      <div class="fw-semibold mb-1 d-flex align-items-center gap-2"><i class="bi bi-layout-three-columns text-secondary"></i> Column Visibility</div>
      <p class="mb-1 text-muted">Tables with many columns have a <strong>Columns</strong> dropdown button in the header. Choose a preset (<em>Auto</em>, <em>All</em>, <em>Compact</em>) or <em>Custom</em> to pick individual columns. <em>Auto</em> mode hides less important columns on small screens automatically.</p>
    </div>

    <hr class="my-2">

    <div class="mb-3">
      <div class="fw-semibold mb-1 d-flex align-items-center gap-2"><i class="bi bi-code-square text-secondary"></i> Properties &amp; Script Editors</div>
      <p class="mb-1 text-muted">The code editors for <strong>Properties</strong> and <strong>JavaScript</strong> support syntax highlighting and auto-completion. Press <kbd>Ctrl+Space</kbd> for suggestions. Use the <strong>Format</strong> button to pretty-print the content and <strong>Clear</strong> to empty it. The <i class="bi bi-question-circle text-muted"></i> icon in the accordion header opens the full properties reference &mdash; it automatically highlights the option matching the word at the current cursor position.</p>
    </div>

    <hr class="my-2">

    <div class="mb-3">
      <div class="fw-semibold mb-1 d-flex align-items-center gap-2"><i class="bi bi-plus-circle text-secondary"></i> Association Panels</div>
      <p class="mb-1 text-muted">On edit pages, panels like <em>Dissemination Hosts</em> or <em>Data Policies</em> show current associations as chips. Click <strong>+ Add</strong> to expand a searchable chooser and add new entries. Click the <i class="bi bi-x-lg text-muted"></i> on a chip to remove it.</p>
    </div>

    <hr class="my-2">

    <div class="mb-3">
      <div class="fw-semibold mb-1 d-flex align-items-center gap-2"><i class="bi bi-graph-up text-secondary"></i> Charts &amp; Tables</div>
      <p class="mb-1 text-muted">Pages with both a <strong>chart</strong> and a <strong>table</strong> view have a toggle button to switch between them. The search box and row count selector apply to the table view only.</p>
    </div>
  </div>
</div>

	<tiles:insert name="footer">
<!-- Feedback offcanvas -->
<logic:present name="<%=ecmwf.web.model.users.User.SESSION_KEY%>">
<% if (_showFeedbackBtn) { %>
<div class="offcanvas offcanvas-end" tabindex="-1" id="feedbackOffcanvas" aria-labelledby="feedbackOffcanvasLabel" style="width:380px;max-width:95vw;">
  <div class="offcanvas-header border-bottom">
    <h6 class="offcanvas-title fw-semibold" id="feedbackOffcanvasLabel">
      <i class="bi bi-chat-left-text me-2 text-primary"></i>Share Feedback
    </h6>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>
  <div class="offcanvas-body p-3" style="font-size:0.85rem; overflow-y:auto;">
    <p class="text-muted small mb-3">Help us improve OpenECPDS. Tell us what works well, what doesn't, or what features you would like to see.</p>

    <div id="fbkForm">
      <div class="mb-3">
        <label class="form-label fw-semibold mb-1">Overall rating <span class="text-danger">*</span></label>
        <div class="d-flex align-items-center gap-1" id="fbkStars">
          <span class="fbk-star fs-4" data-val="1" style="cursor:pointer;color:#ccc;" title="Very poor">&#9733;</span>
          <span class="fbk-star fs-4" data-val="2" style="cursor:pointer;color:#ccc;" title="Poor">&#9733;</span>
          <span class="fbk-star fs-4" data-val="3" style="cursor:pointer;color:#ccc;" title="Average">&#9733;</span>
          <span class="fbk-star fs-4" data-val="4" style="cursor:pointer;color:#ccc;" title="Good">&#9733;</span>
          <span class="fbk-star fs-4" data-val="5" style="cursor:pointer;color:#ccc;" title="Excellent">&#9733;</span>
          <span id="fbkRatingLabel" class="ms-2 small text-muted"></span>
        </div>
        <input type="hidden" id="fbkRating" value="">
      </div>
      <div class="mb-3">
        <label class="form-label fw-semibold mb-1" for="fbkComment">Comments</label>
        <textarea id="fbkComment" class="form-control form-control-sm" rows="3"
          placeholder="Is there anything you would like to tell us? Suggestions, missing features, problems, success stories&hellip;"></textarea>
      </div>
      <div class="mb-3">
        <label class="form-label fw-semibold mb-1" for="fbkOneThing">What is the one thing that would make OpenECPDS better for you?</label>
        <textarea id="fbkOneThing" class="form-control form-control-sm" rows="2" placeholder="Optional"></textarea>
      </div>
      <div class="mb-3">
        <label class="form-label fw-semibold mb-1" for="fbkUsage">How are you using OpenECPDS?</label>
        <select id="fbkUsage" class="form-select form-select-sm">
          <option value="">&mdash; select &mdash;</option>
          <option value="Evaluation">Evaluation</option>
          <option value="Production">Production</option>
          <option value="Research">Research</option>
          <option value="Other">Other</option>
        </select>
      </div>
      <div class="mb-3">
        <label class="form-label fw-semibold mb-1" for="fbkComponent">Which component do you use most?</label>
        <select id="fbkComponent" class="form-select form-select-sm">
          <option value="">&mdash; select &mdash;</option>
          <option value="Data dissemination">Data dissemination</option>
          <option value="Data acquisition">Data acquisition</option>
          <option value="Data portal">Data portal</option>
          <option value="Monitoring">Monitoring</option>
        </select>
      </div>
      <div class="mb-3">
        <label class="form-label fw-semibold mb-1">Would you recommend OpenECPDS to a colleague?</label>
        <div class="d-flex gap-3">
          <div class="form-check"><input class="form-check-input" type="radio" name="fbkRecommend" id="fbkRecYes" value="yes"><label class="form-check-label" for="fbkRecYes">Yes</label></div>
          <div class="form-check"><input class="form-check-input" type="radio" name="fbkRecommend" id="fbkRecNo"  value="no"><label class="form-check-label" for="fbkRecNo">No</label></div>
        </div>
      </div>
      <div class="mb-3">
        <div class="form-check">
          <input class="form-check-input" type="checkbox" id="fbkQuoteOk">
          <label class="form-check-label" for="fbkQuoteOk">We may quote my comments anonymously in presentations or documentation.</label>
        </div>
      </div>
      <div class="mb-3 p-2 rounded" style="background:var(--bs-secondary-bg)">
        <div class="d-flex align-items-start gap-2">
          <div class="form-check form-switch mb-0 mt-1">
            <input class="form-check-input" type="checkbox" id="fbkAnonymous" checked>
          </div>
          <div>
            <label class="form-check-label fw-semibold" for="fbkAnonymous">Submit anonymously</label>
            <div class="text-muted small mt-1">Your feedback helps improve OpenECPDS. You may submit it anonymously or associate it with your account if you would like us to follow up with you.</div>
          </div>
        </div>
        <div id="fbkContactRow" class="mt-2" style="display:none;">
          <input type="email" id="fbkContact" class="form-control form-control-sm" placeholder="Contact email (optional)">
        </div>
      </div>
      <div id="fbkValidationMsg" class="text-danger small mb-2" style="display:none;"></div>
      <button type="button" id="fbkSubmitBtn" class="btn btn-primary btn-sm w-100">
        <i class="bi bi-send me-1"></i>Submit Feedback
      </button>
    </div>

    <div id="fbkSuccess" style="display:none;" class="text-center py-4">
      <i class="bi bi-check-circle-fill text-success" style="font-size:2rem;"></i>
      <p class="fw-semibold mt-2 mb-1">Thank you!</p>
      <p class="text-muted small">Your feedback has been received.</p>
      <button type="button" class="btn btn-outline-secondary btn-sm mt-2" id="fbkReset">Submit another</button>
    </div>
  </div>
</div>
<script>
(function(){
  var ratingLabels = ['Very poor','Poor','Average','Good','Excellent'];
  var selectedRating = 0;
  function setRating(val) {
    selectedRating = val;
    document.getElementById('fbkRating').value = val;
    document.getElementById('fbkRatingLabel').textContent = ratingLabels[val-1] || '';
    document.querySelectorAll('#fbkStars .fbk-star').forEach(function(s) {
      s.style.color = parseInt(s.getAttribute('data-val')) <= val ? '#f59e0b' : '#ccc';
    });
  }
  document.querySelectorAll('#fbkStars .fbk-star').forEach(function(s) {
    s.addEventListener('click', function() { setRating(parseInt(this.getAttribute('data-val'))); });
    s.addEventListener('mouseenter', function() {
      var v = parseInt(this.getAttribute('data-val'));
      document.querySelectorAll('#fbkStars .fbk-star').forEach(function(ss) {
        ss.style.color = parseInt(ss.getAttribute('data-val')) <= v ? '#f59e0b' : '#ccc';
      });
    });
  });
  document.getElementById('fbkStars').addEventListener('mouseleave', function() { setRating(selectedRating); });
  document.getElementById('fbkAnonymous').addEventListener('change', function() {
    document.getElementById('fbkContactRow').style.display = this.checked ? 'none' : 'block';
  });
  document.getElementById('fbkSubmitBtn').addEventListener('click', function() {
    var rating = document.getElementById('fbkRating').value;
    var valMsg = document.getElementById('fbkValidationMsg');
    if (!rating) {
      valMsg.textContent = 'Please select a rating before submitting.';
      valMsg.style.display = 'block';
      return;
    }
    valMsg.style.display = 'none';
    var btn = this;
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Sending&hellip;';
    var rec = document.querySelector('input[name="fbkRecommend"]:checked');
    $.ajax({
      url: '/do/feedback/submit',
      method: 'POST',
      data: {
        rating:    rating,
        comment:   document.getElementById('fbkComment').value,
        oneThing:  document.getElementById('fbkOneThing').value,
        usage:     document.getElementById('fbkUsage').value,
        component: document.getElementById('fbkComponent').value,
        recommend: rec ? rec.value : '',
        quoteOk:   document.getElementById('fbkQuoteOk').checked ? 'true' : 'false',
        anonymous: document.getElementById('fbkAnonymous').checked ? 'true' : 'false',
        contact:   document.getElementById('fbkContact') ? document.getElementById('fbkContact').value : ''
      },
      success: function(resp) {
        if (resp && resp.ok) {
          document.getElementById('fbkForm').style.display = 'none';
          document.getElementById('fbkSuccess').style.display = 'block';
        } else {
          valMsg.textContent = 'Submission failed. Please try again.';
          valMsg.style.display = 'block';
          btn.disabled = false;
          btn.innerHTML = '<i class="bi bi-send me-1"></i>Submit Feedback';
        }
      },
      error: function() {
        valMsg.textContent = 'An error occurred. Please try again.';
        valMsg.style.display = 'block';
        btn.disabled = false;
        btn.innerHTML = '<i class="bi bi-send me-1"></i>Submit Feedback';
      }
    });
  });
  document.getElementById('fbkReset').addEventListener('click', function() {
    selectedRating = 0;
    setRating(0);
    document.getElementById('fbkComment').value = '';
    document.getElementById('fbkOneThing').value = '';
    document.getElementById('fbkUsage').value = '';
    document.getElementById('fbkComponent').value = '';
    document.querySelectorAll('input[name="fbkRecommend"]').forEach(function(r){ r.checked = false; });
    document.getElementById('fbkQuoteOk').checked = false;
    document.getElementById('fbkAnonymous').checked = true;
    document.getElementById('fbkContactRow').style.display = 'none';
    document.getElementById('fbkContact').value = '';
    document.getElementById('fbkValidationMsg').style.display = 'none';
    document.getElementById('fbkSubmitBtn').disabled = false;
    document.getElementById('fbkSubmitBtn').innerHTML = '<i class="bi bi-send me-1"></i>Submit Feedback';
    document.getElementById('fbkForm').style.display = 'block';
    document.getElementById('fbkSuccess').style.display = 'none';
  });
}());
</script>
<% } %>
</logic:present>
		<tiles:put name="helpKey"><tiles:getAsString name="helpKey" ignore="true"/></tiles:put>
	</tiles:insert>

<script>
$( function() {
    if (typeof bootstrap !== 'undefined' && typeof bootstrap.Tooltip === 'function') {
        document.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(function(el) { bootstrap.Tooltip.getOrCreateInstance(el); });
    } else {
        // Bootstrap tooltip not available; skip
    }
  } );
  $(window).on('load', function() {
    $("#loadingDiv").remove();
    $("#contentDiv").fadeIn("fast");
});
</script>

</body>

<tiles:get name="html.bottom" />
