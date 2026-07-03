<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%
    boolean _showFeedbackBtn = true;
    Object _sessionUser = session.getAttribute(ecmwf.web.model.users.User.SESSION_KEY);
    if (_sessionUser instanceof ecmwf.ecpds.master.plugin.http.model.ecuser.WebUser) {
        _showFeedbackBtn = ((ecmwf.ecpds.master.plugin.http.model.ecuser.WebUser) _sessionUser).isShareFeedbackEnabled();
    }
%>

<nav id="topheader" class="topheader navbar py-0" style="background-color:<%=System.getProperty("monitor.color")%>;">
    <div class="container-fluid d-flex align-items-center gap-2 px-3 py-2">

        <c:set var="smw"><tiles:getAsString name="submenu_width" ignore="true"/></c:set>
        <c:if test="${not empty smw and smw != '0'}">
        <div class="d-flex align-items-center gap-2 flex-shrink-0">
            <button class="btn btn-sm btn-outline-light p-0 flex-shrink-0" type="button"
                    data-bs-toggle="offcanvas" data-bs-target="#sidebarMenu" aria-controls="sidebarMenu"
                    title="Toggle navigation" style="width:28px;height:28px;line-height:1;">
                <i class="bi bi-list" style="font-size:1.2rem;"></i>
            </button>
            <span class="header_nav_divider"></span>
        </div>
        </c:if>

        <a class="navbar-brand p-0 me-0 flex-shrink-0" href="/">
            <img src="/assets/images/logo.production.png" alt="Home page" width="140" height="24" id="navLogoFull">
            <img src="/assets/images/logo.icon.png"       alt="Home page" width="34"  height="24" id="navLogoIcon" style="display:none;">
        </a>

        <span class="header_simple_title ms-2" id="navTitle">
            <tiles:getAsString name="title" />
        </span>

        <div class="d-flex align-items-center gap-2 flex-shrink-0 ms-auto">
            <tiles:insert name="submenu_top" />

            <logic:present name="<%=ecmwf.web.model.users.User.SESSION_KEY%>">
                <div class="dropdown">
                    <a class="dropdown-toggle text-white text-decoration-none fw-semibold small"
                       href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                        <i class="bi bi-person-circle me-1"></i><span id="navUserFull"><auth:info property="commonName" /><span class="text-white-50"> (<auth:info property="uid" />)</span></span><span id="navUserShort" style="display:none;"><auth:info property="uid" /></span>
                    </a>
                    <ul class="dropdown-menu dropdown-menu-end shadow">
                        <li><a class="dropdown-item" href="/do/logout">
                            <i class="bi bi-box-arrow-right me-2"></i>Sign Out
                        </a></li>
                    </ul>
                </div>
                <span class="header_nav_divider"></span>
            </logic:present>

            <button id="btnTheme" class="btn btn-sm btn-outline-light p-1 lh-1" onclick="ecpdsToggleTheme()" title="Toggle light/dark theme" style="width:28px;height:28px;">
              <i id="themeIcon" class="bi bi-moon-fill" style="font-size:0.8rem;"></i>
            </button>
            <logic:present name="<%=ecmwf.web.model.users.User.SESSION_KEY%>">
            <% if (_showFeedbackBtn) { %>
            <button class="btn btn-sm btn-outline-light p-1 lh-1" type="button"
                    data-bs-toggle="offcanvas" data-bs-target="#feedbackOffcanvas" aria-controls="feedbackOffcanvas"
                    title="Share feedback" style="width:28px;height:28px;">
              <i class="bi bi-chat-left-text" style="font-size:0.85rem;"></i>
            </button>
            <% } %>
            <a class="btn btn-sm btn-outline-light p-1 lh-1 d-flex align-items-center justify-content-center" href="https://ecmwf.github.io/open-ecpds/" target="_blank" rel="noopener"
               title="OpenECPDS Documentation" style="width:28px;height:28px;">
              <i class="bi bi-book" style="font-size:0.85rem;"></i>
            </a>
            <button class="btn btn-sm btn-outline-light p-1 lh-1" type="button"
                    data-bs-toggle="offcanvas" data-bs-target="#uiHelpOffcanvas" aria-controls="uiHelpOffcanvas"
                    title="Interface help" style="width:28px;height:28px;">
              <i class="bi bi-question-lg" style="font-size:1rem;"></i>
            </button>
            </logic:present>
        </div>

    </div>
    <div class="w-100 location_simple">
        <div><tiles:insert name="location" /></div>
        <a class="btn-about" role="button" data-bs-toggle="modal" data-bs-target="#aboutModal" title="About OpenECPDS">
            <i class="bi bi-info-circle" style="font-size:0.85rem;"></i>
        </a>
    </div>
</nav>

<!-- About modal (outside <nav> to render at correct z-index) -->
<div class="modal fade" id="aboutModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-header border-0 pb-0">
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <div class="modal-body text-center px-4 pt-0 pb-4">
        <img src="/assets/images/OpenECPDS.svg" alt="OpenECPDS" class="img-fluid mb-3" style="max-height:80px;">
        <p class="text-muted small mb-2">Our mission with OpenECPDS is to keep data moving!</p>
        <ul class="list-unstyled mb-0" style="font-size:0.82rem; line-height:2;">
          <li><span class="badge bg-primary bg-opacity-10 text-primary fw-normal px-2">Inspired by operational excellence</span></li>
          <li><span class="badge bg-primary bg-opacity-10 text-primary fw-normal px-2">Powered by open-source innovation</span></li>
          <li><span class="badge bg-success bg-opacity-10 text-success fw-normal px-2">Acquire from anywhere</span></li>
          <li><span class="badge bg-success bg-opacity-10 text-success fw-normal px-2">Deliver everywhere</span></li>
          <li><span class="badge bg-info bg-opacity-10 text-info fw-normal px-2">Connect with confidence</span></li>
          <li><span class="badge bg-info bg-opacity-10 text-info fw-normal px-2">Share without limits</span></li>
        </ul>
        <div class="mt-3">
          <a href="https://ecmwf.github.io/open-ecpds/" target="_blank" rel="noopener"
             class="btn btn-sm btn-outline-primary">
            <i class="bi bi-book me-1"></i>OpenECPDS Documentation
          </a>
        </div>
      </div>
    </div>
  </div>
</div>
<script>
function ecpdsToggleTheme() {
  var t = document.documentElement.getAttribute('data-bs-theme') === 'dark' ? 'light' : 'dark';
  document.documentElement.setAttribute('data-bs-theme', t);
  localStorage.setItem('ecpds-theme', t);
  var ic = document.getElementById('themeIcon');
  if (ic) ic.className = t === 'dark' ? 'bi bi-sun-fill' : 'bi bi-moon-fill';
  if (typeof ecpdsUpdateAceTheme === 'function') ecpdsUpdateAceTheme(t);
}
(function(){
  var t = document.documentElement.getAttribute('data-bs-theme') || 'light';
  var ic = document.getElementById('themeIcon');
  if (ic) ic.className = t === 'dark' ? 'bi bi-sun-fill' : 'bi bi-moon-fill';
}());
(function() {
  var titleEl    = document.getElementById('navTitle');
  var logoFull   = document.getElementById('navLogoFull');
  var logoIcon   = document.getElementById('navLogoIcon');
  var userFull   = document.getElementById('navUserFull');
  var userShort  = document.getElementById('navUserShort');
  if (!titleEl || !logoFull || !logoIcon) return;
  function adapt() {
    // Step 1: reset everything to full state
    logoFull.style.display  = '';
    logoIcon.style.display  = 'none';
    if (userFull)  userFull.style.display  = '';
    if (userShort) userShort.style.display = 'none';
    titleEl.style.display   = '';
    // Step 2: title clipping → switch to icon-only logo
    if (titleEl.scrollWidth > titleEl.clientWidth + 1) {
      logoFull.style.display = 'none';
      logoIcon.style.display = '';
      // Step 3: still clipping → shorten username
      if (titleEl.scrollWidth > titleEl.clientWidth + 1) {
        if (userFull)  userFull.style.display  = 'none';
        if (userShort) userShort.style.display = '';
        // Step 4: still clipping → hide title entirely
        if (titleEl.scrollWidth > titleEl.clientWidth + 1) {
          titleEl.style.display = 'none';
        }
      }
    }
  }
  var ro = new ResizeObserver(function() { adapt(); });
  ro.observe(document.getElementById('topheader') || document.body);
  adapt();
}());
</script>

<!-- Feedback offcanvas -->
<logic:present name="<%=ecmwf.web.model.users.User.SESSION_KEY%>">
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
      <!-- Overall rating -->
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

      <!-- Comment -->
      <div class="mb-3">
        <label class="form-label fw-semibold mb-1" for="fbkComment">Comments</label>
        <textarea id="fbkComment" class="form-control form-control-sm" rows="3"
          placeholder="Is there anything you would like to tell us? Suggestions, missing features, problems, success stories&hellip;"></textarea>
      </div>

      <!-- One thing -->
      <div class="mb-3">
        <label class="form-label fw-semibold mb-1" for="fbkOneThing">What is the one thing that would make OpenECPDS better for you?</label>
        <textarea id="fbkOneThing" class="form-control form-control-sm" rows="2" placeholder="Optional"></textarea>
      </div>

      <!-- Usage -->
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

      <!-- Component -->
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

      <!-- Recommend -->
      <div class="mb-3">
        <label class="form-label fw-semibold mb-1">Would you recommend OpenECPDS to a colleague?</label>
        <div class="d-flex gap-3">
          <div class="form-check"><input class="form-check-input" type="radio" name="fbkRecommend" id="fbkRecYes" value="yes"><label class="form-check-label" for="fbkRecYes">Yes</label></div>
          <div class="form-check"><input class="form-check-input" type="radio" name="fbkRecommend" id="fbkRecNo"  value="no"><label class="form-check-label" for="fbkRecNo">No</label></div>
        </div>
      </div>

      <!-- Quote permission -->
      <div class="mb-3">
        <div class="form-check">
          <input class="form-check-input" type="checkbox" id="fbkQuoteOk">
          <label class="form-check-label" for="fbkQuoteOk">We may quote my comments anonymously in presentations or documentation.</label>
        </div>
      </div>

      <!-- Anonymous toggle -->
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

      <!-- Validation message -->
      <div id="fbkValidationMsg" class="text-danger small mb-2" style="display:none;"></div>

      <button type="button" id="fbkSubmitBtn" class="btn btn-primary btn-sm w-100">
        <i class="bi bi-send me-1"></i>Submit Feedback
      </button>
    </div>

    <!-- Success state -->
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
</logic:present>
