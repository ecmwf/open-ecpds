<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%-- PRG: move flash error from session to request scope, then clear it --%>
<%
  Object _err = session.getAttribute(org.apache.struts.Globals.ERROR_KEY);
  if (_err != null) {
      request.setAttribute(org.apache.struts.Globals.ERROR_KEY, _err);
      session.removeAttribute(org.apache.struts.Globals.ERROR_KEY);
  }
%>

<link href="/assets/css/login.css?v=2026070932" rel="stylesheet" type="text/css"/>

<div id="login-backdrop"></div>

<div id="login-wrap">

  <div id="login-card">

    <div id="login-header">
      <div id="login-logo"><i class="bi bi-cloud-arrow-up-fill"></i></div>
      <h1 id="login-title"><%=System.getProperty("monitor.nickName")%></h1>
      <p id="login-subtitle"><%=System.getProperty("monitor.title")%></p>
    </div>

    <form name="login-form" action="/do/login" method="post" autocomplete="off" id="login-form">

      <%-- Error banner inside card --%>
      <c:if test="${not empty requestScope['org.apache.struts.action.ERROR']}">
        <div class="login-error-banner" id="login-error-banner">
          <i class="bi bi-exclamation-triangle-fill"></i>
          <html:errors/>
        </div>
        <script>setTimeout(function(){var b=document.getElementById('login-error-banner');if(b){b.style.transition='opacity 0.6s';b.style.opacity='0';setTimeout(function(){b.style.display='none';},650);}},4000);</script>
      </c:if>

      <div class="login-field">
        <label for="login-user"><i class="bi bi-person"></i> Username</label>
        <input id="login-user" name="user" type="text" placeholder="Enter your username" autocomplete="username" autofocus>
      </div>

      <c:choose>
        <c:when test="${showPassword and showOtp}">
          <%-- Both password and OTP allowed — keyboard default, numpad optional for OTP --%>
          <div class="login-field">
            <label for="login-pass"><i class="bi bi-lock"></i> Password / OTP
              <span id="otp-count" class="otp-count"></span>
            </label>
            <div class="otp-dots" id="otp-dots" aria-hidden="true" style="display:none;">
              <span></span><span></span><span></span><span></span><span></span><span></span>
            </div>
            <div id="pass-field-wrap">
              <input id="login-pass" name="password" type="password" placeholder="Password or 6-digit one-time code" autocomplete="current-password">
              <div id="login-pass-hint" class="login-pass-hint"></div>
            </div>
          </div>
          <div id="otp-numpad" style="display:none;">
            <div class="numpad-grid">
              <button type="button" class="numpad-btn" data-d="1">1</button>
              <button type="button" class="numpad-btn" data-d="2">2</button>
              <button type="button" class="numpad-btn" data-d="3">3</button>
              <button type="button" class="numpad-btn" data-d="4">4</button>
              <button type="button" class="numpad-btn" data-d="5">5</button>
              <button type="button" class="numpad-btn" data-d="6">6</button>
              <button type="button" class="numpad-btn" data-d="7">7</button>
              <button type="button" class="numpad-btn" data-d="8">8</button>
              <button type="button" class="numpad-btn" data-d="9">9</button>
              <button type="button" class="numpad-btn numpad-back" id="numpad-back" title="Backspace — double-click to clear all">
                <i class="bi bi-backspace-fill"></i>
              </button>
              <button type="button" class="numpad-btn" data-d="0">0</button>
              <button type="button" class="numpad-btn numpad-go" id="numpad-go" title="Sign in">
                <i class="bi bi-box-arrow-in-right"></i>
              </button>
            </div>
          </div>
        </c:when>
        <c:when test="${showOtp}">
          <%-- OTP only — with accessible numpad --%>
          <div class="login-field">
            <label for="login-pass">
              <i class="bi bi-shield-lock"></i> One-Time Passcode
              <span id="otp-count" class="otp-count"></span>
            </label>
            <%-- 6-dot progress display (numpad mode); hidden in keyboard mode --%>
            <div class="otp-dots" id="otp-dots" aria-hidden="true">
              <span></span><span></span><span></span><span></span><span></span><span></span>
            </div>
            <%-- Input: hidden in numpad mode, shown in keyboard mode --%>
            <input id="login-pass" name="password" type="text" inputmode="numeric" maxlength="6"
                   pattern="[0-9]{6}" placeholder="Enter 6-digit code" autocomplete="one-time-code" readonly>
          </div>

          <%-- Numeric keypad (shown by default; JS hides if keyboard mode is stored) --%>
          <div id="otp-numpad">
            <div class="numpad-grid">
              <button type="button" class="numpad-btn" data-d="1">1</button>
              <button type="button" class="numpad-btn" data-d="2">2</button>
              <button type="button" class="numpad-btn" data-d="3">3</button>
              <button type="button" class="numpad-btn" data-d="4">4</button>
              <button type="button" class="numpad-btn" data-d="5">5</button>
              <button type="button" class="numpad-btn" data-d="6">6</button>
              <button type="button" class="numpad-btn" data-d="7">7</button>
              <button type="button" class="numpad-btn" data-d="8">8</button>
              <button type="button" class="numpad-btn" data-d="9">9</button>
              <button type="button" class="numpad-btn numpad-back" id="numpad-back" title="Backspace — double-click to clear all">
                <i class="bi bi-backspace-fill"></i>
              </button>
              <button type="button" class="numpad-btn" data-d="0">0</button>
              <button type="button" class="numpad-btn numpad-go" id="numpad-go" title="Sign in">
                <i class="bi bi-box-arrow-in-right"></i>
              </button>
            </div>
          </div>

        </c:when>
        <c:otherwise>
          <%-- Password only --%>
          <div class="login-field">
            <label for="login-pass"><i class="bi bi-lock"></i> Password</label>
            <div class="login-pass-wrap">
              <input id="login-pass" name="password" type="password" placeholder="Enter your password" autocomplete="current-password">
              <button type="button" id="login-toggle-pass" tabindex="-1" title="Show/hide password">
                <i class="bi bi-eye" id="login-eye"></i>
              </button>
            </div>
          </div>
        </c:otherwise>
      </c:choose>

      <button type="submit" id="login-btn" disabled>
        <i class="bi bi-box-arrow-in-right"></i> Sign in
      </button>

    </form>

    <%-- Card footer strip — shown whenever OTP or password mode is active --%>
    <c:if test="${showOtp or showPassword}">
    <div id="otp-mode-footer">
      <c:if test="${showOtp}">
      <button type="button" class="numpad-switch-btn" id="numpad-to-kb" style="display:none;" title="Use keyboard instead">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="5" width="20" height="14" rx="2"/><path d="M6 9h.01M10 9h.01M14 9h.01M18 9h.01M8 13h.01M12 13h.01M16 13h.01M7 17h10"/></svg>
      </button>
      <button type="button" class="numpad-switch-btn" id="kb-to-numpad" style="display:none;" title="Use numpad instead">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="4" height="4" rx="0.5"/><rect x="10" y="3" width="4" height="4" rx="0.5"/><rect x="17" y="3" width="4" height="4" rx="0.5"/><rect x="3" y="10" width="4" height="4" rx="0.5"/><rect x="10" y="10" width="4" height="4" rx="0.5"/><rect x="17" y="10" width="4" height="4" rx="0.5"/><rect x="3" y="17" width="4" height="4" rx="0.5"/><rect x="10" y="17" width="4" height="4" rx="0.5"/><rect x="17" y="17" width="4" height="4" rx="0.5"/></svg>
      </button>
      <button type="button" class="numpad-switch-btn otp-vis-toggle" id="otp-vis-toggle" title="Show code">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
      </button>
      </c:if>
      <%-- Clear button: always visible, enabled when username or password has content --%>
      <button type="button" class="numpad-switch-btn" id="pass-clear-btn" title="Clear fields" disabled>
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
      </button>
    </div>
    </c:if>

  </div>
</div>

<c:if test="${showPassword}">
<script>
var _toggleBtn = document.getElementById('login-toggle-pass');
if (_toggleBtn) {
    _toggleBtn.addEventListener('click', function() {
        var inp = document.getElementById('login-pass');
        var eye = document.getElementById('login-eye');
        if (inp.type === 'password') {
            inp.type = 'text';
            eye.className = 'bi bi-eye-slash';
        } else {
            inp.type = 'password';
            eye.className = 'bi bi-eye';
        }
    });
}
</script>
</c:if>

<c:if test="${showOtp}">
<script>
(function() {
  var inp        = document.getElementById('login-pass');
  var userInp    = document.getElementById('login-user');
  var dots       = document.querySelectorAll('#otp-dots span');
  var dotsWrap   = document.getElementById('otp-dots');
  var numpad     = document.getElementById('otp-numpad');
  var toKbBtn    = document.getElementById('numpad-to-kb');
  var SVG_EYE       = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>';
  var SVG_EYE_SLASH = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/><path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/><line x1="1" y1="1" x2="23" y2="23"/></svg>';

  var toNpBtn    = document.getElementById('kb-to-numpad');
  var visToggle  = document.getElementById('otp-vis-toggle');
  var loginBtn   = document.getElementById('login-btn');
  var goBtn      = document.getElementById('numpad-go');
  var countSpan  = document.getElementById('otp-count');
  var backBtn    = document.getElementById('numpad-back');
  var form       = document.getElementById('login-form');
  var passWrap   = document.getElementById('pass-field-wrap'); // null in OTP-only
  var isOtpOnly  = !passWrap;
  var _LS_KEY    = 'ecpds-otp-mode';
  var _visible   = false;

  var digitBtns  = numpad.querySelectorAll('.numpad-btn[data-d]');

  /* ---------- visibility toggle (applies to both modes) ---------- */
  function applyVisibility() {
    visToggle.innerHTML = _visible ? SVG_EYE_SLASH : SVG_EYE;
    visToggle.title = _visible ? 'Hide code' : 'Show code';
    if (inp.readOnly) {
      // Numpad mode: toggle between dots and digit display
      dotsWrap.style.display = _visible ? 'none' : '';
      var digitDisplay = document.getElementById('otp-digit-display');
      if (!digitDisplay) {
        digitDisplay = document.createElement('div');
        digitDisplay.id = 'otp-digit-display';
        digitDisplay.className = 'otp-digit-display';
        dotsWrap.parentNode.insertBefore(digitDisplay, dotsWrap);
      }
      var digits = inp.value.split('');
      while (digits.length < 6) digits.push('_');
      digitDisplay.textContent = digits.join(' ');
      digitDisplay.style.display = _visible ? 'block' : 'none';
    } else {
      // Keyboard mode: toggle input type
      inp.type = _visible ? 'text' : 'password';
    }
  }

  visToggle.addEventListener('click', function() {
    _visible = !_visible;
    applyVisibility();
  });

  /* ---------- helpers ---------- */
  function updateDots() {
    var n = inp.value.length;
    dots.forEach(function(d, i) {
      d.classList.toggle('filled', i < n);
      d.classList.toggle('next',   i === n && n < 6);
    });
  }

  function setCount() {
    var n = inp.value.length;
    var hasUser = userInp.value.trim().length > 0;
    var full = (n === 6) && hasUser;
    countSpan.textContent = n ? n + ' / 6' : '';
    goBtn.classList.toggle('numpad-go-ready', full);
    digitBtns.forEach(function(b) { b.disabled = n >= 6; });
    updateDots();
    // Keep digit display in sync when visible
    if (_visible && inp.readOnly) {
      var dd = document.getElementById('otp-digit-display');
      if (dd) {
        var digits = inp.value.split('');
        while (digits.length < 6) digits.push('_');
        dd.textContent = digits.join(' ');
      }
    }
    // Update tooltips — goBtn uses CSS class not disabled so title always shows
    var tip = !hasUser ? 'Enter a username first'
            : n < 6   ? 'Enter all 6 digits to sign in'
            : 'Sign in';
    goBtn.title = tip;
    if (!inp.readOnly && isOtpOnly) {
      loginBtn.disabled = !full;
      loginBtn.title = tip;
    }
  }

  /* ---------- in combined keyboard mode: show numpad toggle only if field looks like OTP ---------- */
  function updateNpToggle() {
    if (isOtpOnly || inp.readOnly) return;
    var v = inp.value;
    var canSwitch = v.length === 0 || (/^\d+$/.test(v) && v.length <= 6);
    toNpBtn.disabled = !canSwitch;
    toNpBtn.title = canSwitch
      ? 'Switch to numeric keypad for OTP entry'
      : 'Clear the field or enter digits only to use the numpad';
  }

  /* ---------- numpad mode ---------- */
  function enterNumpadMode() {
    inp.readOnly = true;
    if (passWrap) {
      inp.type = 'text';
      passWrap.style.display = 'none';
    }
    inp.style.display = 'none';
    dotsWrap.style.display = _visible ? 'none' : '';
    numpad.style.display = '';
    toKbBtn.style.display = '';
    toNpBtn.style.display = 'none';
    loginBtn.style.display = 'none';
    updateDots();
    setCount();
    applyVisibility();
    try { localStorage.setItem(_LS_KEY, 'numpad'); } catch(e) {}
  }

  /* ---------- keyboard mode ---------- */
  function enterKeyboardMode() {
    inp.readOnly = false;
    if (passWrap) {
      passWrap.style.display = '';
    }
    inp.type = _visible ? 'text' : 'password';
    inp.style.display = '';
    dotsWrap.style.display = 'none';
    var dd = document.getElementById('otp-digit-display');
    if (dd) dd.style.display = 'none';
    numpad.style.display = 'none';
    toKbBtn.style.display = 'none';
    toNpBtn.style.display = '';
    loginBtn.style.display = '';
    if (isOtpOnly) {
      loginBtn.disabled = (inp.value.length !== 6) || !userInp.value.trim();
      loginBtn.title = !userInp.value.trim() ? 'Enter a username first'
                     : inp.value.length !== 6 ? 'Enter all 6 digits to sign in' : '';
    } else {
      loginBtn.disabled = userInp.value.trim().length === 0;
      updateNpToggle();
    }
    inp.focus();
    try { localStorage.setItem(_LS_KEY, 'keyboard'); } catch(e) {}
  }

  /* ---------- digit buttons ---------- */
  numpad.querySelectorAll('.numpad-btn[data-d]').forEach(function(btn) {
    btn.addEventListener('click', function() {
      if (inp.value.length < 6) {
        inp.value += this.getAttribute('data-d');
        setCount();
        inp.dispatchEvent(new Event('input'));
      }
    });
  });

  /* ---------- backspace (double-click = clear all) ---------- */
  var _backClicks = 0, _backTimer = null;
  backBtn.addEventListener('click', function() {
    _backClicks++;
    if (_backClicks === 1) {
      _backTimer = setTimeout(function() {
        inp.value = inp.value.slice(0, -1);
        setCount();
        inp.dispatchEvent(new Event('input'));
        _backClicks = 0;
      }, 200);
    } else {
      clearTimeout(_backTimer);
      _backClicks = 0;
      inp.value = '';
      setCount();
      inp.dispatchEvent(new Event('input'));
    }
  });

  /* ---------- go / sign-in button ---------- */
  goBtn.addEventListener('click', function() {
    if (goBtn.classList.contains('numpad-go-ready')) form.submit();
  });

  /* ---------- toggle buttons ---------- */
  toKbBtn.addEventListener('click', enterKeyboardMode);
  toNpBtn.addEventListener('click', function() {
    // Pre-fill numpad with any digits already typed in combined keyboard mode
    var existing = passWrap ? inp.value : '';
    enterNumpadMode();
    if (existing) {
      for (var i = 0; i < existing.length && i < 6; i++) {
        inp.value = existing.slice(0, i + 1);
      }
      setCount();
      inp.dispatchEvent(new Event('input'));
    }
  });

  /* ---------- keyboard input handling ---------- */
  inp.addEventListener('input', function() {
    if (!inp.readOnly) {
      if (isOtpOnly) {
        // OTP-only: restrict to digits only
        var pos = this.selectionStart;
        var cleaned = this.value.replace(/\D/g, '').slice(0, 6);
        if (this.value !== cleaned) {
          this.value = cleaned;
          this.setSelectionRange(Math.min(pos, cleaned.length), Math.min(pos, cleaned.length));
        }
        setCount();
      } else {
        // Combined: update numpad toggle availability
        updateNpToggle();
      }
    }
  });

  /* ---------- username change re-evaluates OTP readiness ---------- */
  userInp.addEventListener('input', setCount);

  /* ---------- apply saved preference ---------- */
  var defaultMode = isOtpOnly ? 'numpad' : 'keyboard';
  var saved = defaultMode;
  try { saved = localStorage.getItem(_LS_KEY) || defaultMode; } catch(e) {}
  if (saved === 'keyboard') {
    enterKeyboardMode();
  } else {
    enterNumpadMode();
  }
}());
</script>
</c:if>

<c:if test="${showPassword}">
<script>
(function() {
  var userInp  = document.getElementById('login-user');
  var loginBtn = document.getElementById('login-btn');
  if (!userInp || !loginBtn) return;
  loginBtn.title = 'Enter a username first';
  userInp.addEventListener('input', function() {
    var empty = userInp.value.trim().length === 0;
    loginBtn.disabled = empty;
    loginBtn.title = empty ? 'Enter a username first' : '';
  });
}());
</script>
</c:if>

<c:if test="${showPassword and showOtp}">
<script>
document.getElementById('login-pass').addEventListener('input', function() {
    var val = this.value;
    var hint = document.getElementById('login-pass-hint');
    var isOtp = val.length === 6 && /^\d+$/.test(val);
    if (isOtp) {
        hint.innerHTML = '<i class="bi bi-shield-check"></i> One-time passcode (OTP) detected';
        hint.className = 'login-pass-hint login-pass-hint-otp';
    } else if (val.length > 0) {
        hint.innerHTML = '<i class="bi bi-key"></i> Standard password';
        hint.className = 'login-pass-hint login-pass-hint-pwd';
    } else {
        hint.innerHTML = '';
        hint.className = 'login-pass-hint';
    }
});
</script>
</c:if>


<%-- Shared clear button — clears both username and password/OTP fields --%>
<c:if test="${showOtp or showPassword}">
<script>
(function() {
  var passInp  = document.getElementById('login-pass');
  var userInp  = document.getElementById('login-user');
  var clearBtn = document.getElementById('pass-clear-btn');
  if (!clearBtn) return;
  function updateClearBtn() {
    var hasContent = (passInp && passInp.value.length > 0) || (userInp && userInp.value.trim().length > 0);
    clearBtn.disabled = !hasContent;
  }
  if (passInp) passInp.addEventListener('input', updateClearBtn);
  if (userInp) userInp.addEventListener('input', updateClearBtn);
  clearBtn.addEventListener('click', function() {
    if (passInp) { passInp.value = ''; passInp.dispatchEvent(new Event('input')); }
    if (userInp) { userInp.value = ''; userInp.dispatchEvent(new Event('input')); }
    updateClearBtn();
    if (passInp) passInp.focus();
  });
  updateClearBtn();
})();
</script>
</c:if>

<script>
(function() {
  // Prevent mobile browser zoom on reload
  var vp = document.querySelector('meta[name="viewport"]');
  if (vp) vp.setAttribute('content', 'width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no');

  function fixLoginLayout() {
    var header = document.getElementById('topheader');
    var content = document.getElementById('contentDiv') || document.querySelector('.content');
    var wrap = document.getElementById('login-wrap');
    if (!header || !content || !wrap) return;
    var h = Math.round(header.getBoundingClientRect().height);
    content.style.setProperty('padding-top', h + 'px', 'important');
    wrap.style.minHeight = 'calc(100svh - ' + h + 'px - 44px)';
  }
  fixLoginLayout();
  window.addEventListener('resize', fixLoginLayout);
})();
</script>
