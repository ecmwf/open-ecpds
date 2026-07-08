<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>

<link href="/assets/css/login.css?v=20260708b" rel="stylesheet" type="text/css"/>

<div id="login-backdrop"></div>

<div id="login-wrap">
  <div id="login-card">

    <div id="login-header">
      <div id="login-logo"><i class="bi bi-cloud-arrow-up-fill"></i></div>
      <h1 id="login-title"><%=System.getProperty("monitor.nickName")%></h1>
      <p id="login-subtitle"><%=System.getProperty("monitor.title")%></p>
    </div>

    <form name="login-form" action="/do/login" method="post" autocomplete="off" id="login-form">

      <c:if test="${not empty requestScope['org.apache.struts.action.ERROR']}">
        <div class="login-error">
          <i class="bi bi-exclamation-triangle-fill"></i>
          <html:errors/>
        </div>
      </c:if>

      <div class="login-field">
        <label for="login-user"><i class="bi bi-person"></i> Username</label>
        <input id="login-user" name="user" type="text" placeholder="Enter your username" autocomplete="username" autofocus>
      </div>

      <c:choose>
        <c:when test="${showPassword and showOtp}">
          <%-- Both password and OTP allowed --%>
          <div class="login-field">
            <label for="login-pass"><i class="bi bi-lock"></i> Password / OTP</label>
            <div class="login-pass-wrap">
              <input id="login-pass" name="password" type="password" placeholder="Password or 6-digit one-time code" autocomplete="current-password">
              <button type="button" id="login-toggle-pass" tabindex="-1" title="Show/hide password">
                <i class="bi bi-eye" id="login-eye"></i>
              </button>
            </div>
            <div id="login-pass-hint" class="login-pass-hint"></div>
          </div>
        </c:when>
        <c:when test="${showOtp}">
          <%-- OTP only — with accessible numpad --%>
          <div class="login-field">
            <label for="login-pass">
              <i class="bi bi-shield-lock"></i> One-Time Passcode
              <span id="otp-count" class="otp-count"></span>
            </label>
            <input id="login-pass" name="password" type="text" inputmode="numeric" maxlength="6"
                   pattern="[0-9]{6}" placeholder="&#xB7; &#xB7; &#xB7; &#xB7; &#xB7; &#xB7;" autocomplete="one-time-code" readonly>
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
              <button type="button" class="numpad-btn numpad-go" id="numpad-go" disabled title="Sign in">
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

      <button type="submit" id="login-btn">
        <i class="bi bi-box-arrow-in-right"></i> Sign in
      </button>

      <%-- Mode toggle (OTP only) — one of the two buttons is visible at a time --%>
      <c:if test="${showOtp}">
      <div style="text-align:center;margin-top:1.2rem;padding-top:1rem;border-top:1px solid #f0f0f0;">
        <button type="button" class="numpad-switch-btn" id="numpad-to-kb" style="display:none;">
          <i class="bi bi-keyboard me-1"></i>Use keyboard instead
        </button>
        <button type="button" class="numpad-switch-btn" id="kb-to-numpad" style="display:none;">
          <i class="bi bi-grid-3x3-gap me-1"></i>Use numpad instead
        </button>
      </div>
      </c:if>

    </form>

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

<c:if test="${showOtp and not showPassword}">
<script>
(function() {
  var inp       = document.getElementById('login-pass');
  var numpad    = document.getElementById('otp-numpad');
  var toKbBtn   = document.getElementById('numpad-to-kb');
  var toNpBtn   = document.getElementById('kb-to-numpad');
  var loginBtn  = document.getElementById('login-btn');
  var goBtn     = document.getElementById('numpad-go');
  var countSpan = document.getElementById('otp-count');
  var backBtn   = document.getElementById('numpad-back');
  var form      = document.getElementById('login-form');
  var _LS_KEY   = 'ecpds-otp-mode';   // 'numpad' (default) | 'keyboard'

  var digitBtns = numpad.querySelectorAll('.numpad-btn[data-d]');

  /* ---------- helpers ---------- */
  function setCount() {
    var n = inp.value.length;
    var full = (n === 6);
    countSpan.textContent = n ? n + ' / 6' : '';
    goBtn.disabled = !full;
    goBtn.classList.toggle('numpad-go-ready', full);
    digitBtns.forEach(function(b) { b.disabled = full; });
    // In keyboard mode the Sign in button is visible — keep it disabled until 6 digits
    if (!inp.readOnly) loginBtn.disabled = !full;
  }

  /* ---------- numpad mode ---------- */
  function enterNumpadMode() {
    inp.readOnly = true;
    inp.classList.add('numpad-input');
    numpad.style.display = '';
    toKbBtn.style.display = '';
    toNpBtn.style.display = 'none';
    loginBtn.style.display = 'none';
    try { localStorage.setItem(_LS_KEY, 'numpad'); } catch(e) {}
  }

  /* ---------- keyboard mode ---------- */
  function enterKeyboardMode() {
    inp.readOnly = false;
    inp.classList.remove('numpad-input');
    numpad.style.display = 'none';
    toKbBtn.style.display = 'none';
    toNpBtn.style.display = '';
    loginBtn.style.display = '';
    loginBtn.disabled = (inp.value.length !== 6);
    inp.focus();
    try { localStorage.setItem(_LS_KEY, 'keyboard'); } catch(e) {}
  }

  /* ---------- digit buttons ---------- */
  numpad.querySelectorAll('.numpad-btn[data-d]').forEach(function(btn) {
    btn.addEventListener('click', function() {
      if (inp.value.length < 6) {
        inp.value += this.getAttribute('data-d');
        setCount();
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
        _backClicks = 0;
      }, 200);
    } else {
      clearTimeout(_backTimer);
      _backClicks = 0;
      inp.value = '';
      setCount();
    }
  });

  /* ---------- go / sign-in button ---------- */
  goBtn.addEventListener('click', function() {
    if (inp.value.length === 6) form.submit();
  });

  /* ---------- toggle buttons ---------- */
  toKbBtn.addEventListener('click', enterKeyboardMode);
  toNpBtn.addEventListener('click', enterNumpadMode);

  /* ---------- keyboard mode: restrict to digits ---------- */
  inp.addEventListener('input', function() {
    if (!inp.readOnly) {
      var pos = this.selectionStart;
      var cleaned = this.value.replace(/\D/g, '').slice(0, 6);
      if (this.value !== cleaned) {
        this.value = cleaned;
        this.setSelectionRange(Math.min(pos, cleaned.length), Math.min(pos, cleaned.length));
      }
      setCount();
    }
  });

  /* ---------- apply saved preference immediately ---------- */
  var saved = 'numpad';
  try { saved = localStorage.getItem(_LS_KEY) || 'numpad'; } catch(e) {}
  if (saved === 'keyboard') {
    enterKeyboardMode();
  } else {
    enterNumpadMode();
  }
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

