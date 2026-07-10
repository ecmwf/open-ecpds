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

<link href="/assets/css/login.css?v=2026070933" rel="stylesheet" type="text/css"/>

<c:choose>
<c:when test="${loginAnimatedBackground}">
<canvas id="login-backdrop"></canvas>
<script>
(function() {
  var canvas = document.getElementById('login-backdrop');
  var ctx    = canvas.getContext('2d');

  // --- World map as a sparse dot-grid approximation (lat/lon pairs, degrees) ---
  // Simplified coast/landmass points sampled at ~5 degree grid
  var LAND = [
    // North America
    [-140,60],[-130,60],[-120,60],[-110,55],[-100,50],[-90,50],[-80,45],[-75,45],[-70,45],
    [-120,50],[-110,50],[-100,45],[-90,45],[-85,42],[-80,40],[-75,40],[-70,42],[-65,45],
    [-100,40],[-95,38],[-90,38],[-85,38],[-80,38],[-75,38],[-70,38],
    [-105,35],[-100,35],[-95,33],[-90,33],[-85,33],[-80,32],[-75,34],
    [-110,30],[-105,28],[-100,28],[-95,28],[-90,30],[-85,30],[-80,28],
    [-115,25],[-110,23],[-105,20],[-100,20],[-95,20],[-90,18],[-85,12],[-80,10],
    // South America
    [-75,10],[-70,5],[-65,2],[-60,0],[-55,-5],[-50,-10],[-45,-15],[-40,-20],
    [-65,0],[-60,-5],[-55,-10],[-50,-15],[-45,-20],[-40,-25],[-50,-30],[-55,-35],
    [-60,-40],[-65,-45],[-65,-50],[-70,-55],
    [-75,-5],[-80,-5],[-75,-15],[-70,-20],[-65,-25],[-60,-30],
    // Europe
    [-10,36],[0,37],[10,38],[15,38],[20,38],[25,38],[30,36],
    [-10,40],[0,40],[5,42],[10,44],[15,46],[20,46],[25,46],[30,45],
    [-5,44],[0,46],[5,48],[10,50],[15,52],[20,52],[25,50],[30,50],
    [0,52],[5,54],[10,55],[15,56],[20,57],[25,57],[18,60],[15,62],[18,65],
    [-5,50],[0,52],[5,52],[-2,54],[-4,52],[-3,56],[-4,58],[-2,60],
    [20,42],[25,44],[22,42],[25,42],[28,42],[30,42],
    // Africa
    [-18,15],[-15,12],[-12,10],[-10,8],[-8,5],[-5,5],[-2,5],[0,5],
    [5,5],[10,5],[15,5],[20,4],[25,0],[30,-2],[35,-5],[38,-8],
    [35,15],[38,12],[40,10],[42,8],[45,5],[50,10],[45,15],[42,18],
    [38,20],[35,22],[32,25],[30,28],[28,30],[25,32],[22,36],[15,38],
    [10,36],[5,36],[0,36],[-5,35],
    [30,-5],[32,-10],[34,-15],[36,-20],[34,-25],[30,-30],[26,-34],[22,-35],[18,-34],[15,-30],
    [12,-25],[10,-20],[8,-15],[5,-10],[2,-5],[0,0],[2,5],
    // Asia
    [35,38],[40,38],[45,38],[50,38],[55,38],[60,38],[65,38],[70,35],[75,32],[80,28],[85,25],
    [40,42],[45,44],[50,45],[55,46],[60,46],[65,45],[70,45],[75,45],[80,45],[85,45],[90,45],
    [50,50],[55,52],[60,55],[65,55],[70,55],[75,55],[80,55],[85,55],[90,55],[95,55],[100,55],
    [105,50],[110,50],[115,50],[120,50],[125,50],[130,48],[135,48],[140,45],[140,40],
    [100,20],[105,15],[110,12],[115,10],[120,10],[115,22],[120,25],[125,25],
    [80,18],[75,20],[72,22],[70,25],[68,28],[72,32],[76,35],[80,38],
    [100,10],[103,5],[105,2],[108,3],[112,5],[115,8],[120,10],
    // Australia
    [115,-30],[120,-30],[125,-30],[130,-28],[135,-28],[140,-28],[145,-30],[150,-30],[152,-32],
    [150,-35],[148,-38],[145,-38],[140,-35],[135,-32],[130,-32],[125,-32],[120,-35],
    [115,-33],[130,-18],[135,-16],[140,-18],[145,-18],[150,-22],[148,-25],
    // Japan/Korea
    [130,32],[132,34],[134,36],[136,38],[138,38],[140,40],[141,42],
    [126,34],[128,36],[130,38],
    // UK/Ireland
    [-8,54],[-6,54],[-4,56],[-3,58],[-2,58],[0,54],[0,52],[-2,52],
    // Greenland
    [-50,70],[-45,70],[-40,70],[-35,72],[-30,72],[-25,70],[-20,68],[-45,65],[-50,65],
    // Scandinavia
    [8,58],[10,60],[12,62],[14,64],[16,68],[18,70],[20,70],[24,70],[28,72],
    [22,62],[24,60],[26,62],[28,64],[30,65],
  ];

  // --- Fixed "hub" nodes (major data centres / exchange points) ---
  var HUBS = [
    {lat:51.5,  lon:-0.1},   // London
    {lat:48.8,  lon:2.3},    // Paris
    {lat:52.5,  lon:13.4},   // Frankfurt/Berlin
    {lat:40.7,  lon:-74.0},  // New York
    {lat:37.8,  lon:-122.4}, // San Francisco
    {lat:35.7,  lon:139.7},  // Tokyo
    {lat:22.3,  lon:114.2},  // Hong Kong
    {lat:-33.9, lon:151.2},  // Sydney
    {lat:1.3,   lon:103.8},  // Singapore
    {lat:43.7,  lon:7.3},    // ECMWF (Reading/Bologna area)
    {lat:55.7,  lon:12.6},   // Copenhagen
    {lat:-23.5, lon:-46.6},  // São Paulo
    {lat:19.4,  lon:-99.1},  // Mexico City
    {lat:28.6,  lon:77.2},   // New Delhi
  ];

  // Project lat/lon → canvas x/y (equirectangular)
  function project(lat, lon, w, h) {
    var x = ((lon + 180) / 360) * w;
    var y = ((90 - lat) / 180) * h;
    return {x: x, y: y};
  }

  // --- Animated routes ---
  var routes = [];
  function makeRoute() {
    var a = HUBS[Math.floor(Math.random() * HUBS.length)];
    var b = HUBS[Math.floor(Math.random() * HUBS.length)];
    while (b === a) b = HUBS[Math.floor(Math.random() * HUBS.length)];
    return {
      a: a, b: b,
      progress: Math.random(),       // 0..1 along the path
      speed: 0.0008 + Math.random() * 0.001,
      alpha: 0.6 + Math.random() * 0.4
    };
  }
  for (var i = 0; i < 18; i++) routes.push(makeRoute());

  // Quadratic bezier point
  function bezierPoint(t, x0, y0, cx, cy, x1, y1) {
    var mt = 1 - t;
    return {
      x: mt*mt*x0 + 2*mt*t*cx + t*t*x1,
      y: mt*mt*y0 + 2*mt*t*cy + t*t*y1
    };
  }

  // Hub node pulse state
  var hubPulse = HUBS.map(function() { return Math.random() * Math.PI * 2; });

  var raf;
  function resize() {
    canvas.width  = window.innerWidth;
    canvas.height = window.innerHeight;
  }
  window.addEventListener('resize', resize);
  resize();

  function draw() {
    var W = canvas.width, H = canvas.height;
    ctx.clearRect(0, 0, W, H);

    // Background gradient
    var grad = ctx.createLinearGradient(0, 0, W, H);
    grad.addColorStop(0,    '#0d1b2a');
    grad.addColorStop(0.5,  '#1a3a5c');
    grad.addColorStop(1,    '#0d1e3a');
    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, W, H);

    // --- World map dots ---
    ctx.fillStyle = 'rgba(100,160,240,0.45)';
    for (var i = 0; i < LAND.length; i++) {
      var p = project(LAND[i][1], LAND[i][0], W, H);
      ctx.beginPath();
      ctx.arc(p.x, p.y, 1.5, 0, Math.PI * 2);
      ctx.fill();
    }

    var t = Date.now();

    // --- Routes: draw arc trail then moving dot ---
    for (var r = 0; r < routes.length; r++) {
      var route = routes[r];
      var pa = project(route.a.lat, route.a.lon, W, H);
      var pb = project(route.b.lat, route.b.lon, W, H);
      // Control point: arc up proportional to distance
      var cx = (pa.x + pb.x) / 2;
      var cy = (pa.y + pb.y) / 2 - Math.abs(pb.x - pa.x) * 0.35 - 40;

      // Draw faint path
      ctx.beginPath();
      ctx.moveTo(pa.x, pa.y);
      ctx.quadraticCurveTo(cx, cy, pb.x, pb.y);
      ctx.strokeStyle = 'rgba(13,110,253,0.12)';
      ctx.lineWidth = 1;
      ctx.stroke();

      // Advance progress
      route.progress += route.speed;
      if (route.progress > 1) {
        // Respawn as new route
        routes[r] = makeRoute();
        routes[r].progress = 0;
        continue;
      }

      // Moving glowing dot along curve
      var pt = bezierPoint(route.progress, pa.x, pa.y, cx, cy, pb.x, pb.y);
      var dotGrad = ctx.createRadialGradient(pt.x, pt.y, 0, pt.x, pt.y, 6);
      dotGrad.addColorStop(0,   'rgba(120,190,255,' + route.alpha + ')');
      dotGrad.addColorStop(0.4, 'rgba(13,110,253,0.5)');
      dotGrad.addColorStop(1,   'rgba(13,110,253,0)');
      ctx.beginPath();
      ctx.arc(pt.x, pt.y, 6, 0, Math.PI * 2);
      ctx.fillStyle = dotGrad;
      ctx.fill();

      // Bright core
      ctx.beginPath();
      ctx.arc(pt.x, pt.y, 1.8, 0, Math.PI * 2);
      ctx.fillStyle = 'rgba(200,230,255,0.95)';
      ctx.fill();
    }

    // --- Hub nodes: pulsing glow rings ---
    for (var h = 0; h < HUBS.length; h++) {
      hubPulse[h] += 0.025;
      var ph = project(HUBS[h].lat, HUBS[h].lon, W, H);
      var pulse = (Math.sin(hubPulse[h]) + 1) / 2; // 0..1

      // Outer glow ring
      var ringR = 6 + pulse * 8;
      var ringAlpha = 0.15 + pulse * 0.2;
      var nodeGrad = ctx.createRadialGradient(ph.x, ph.y, 0, ph.x, ph.y, ringR);
      nodeGrad.addColorStop(0,   'rgba(100,180,255,' + (ringAlpha + 0.15) + ')');
      nodeGrad.addColorStop(0.5, 'rgba(13,110,253,' + ringAlpha + ')');
      nodeGrad.addColorStop(1,   'rgba(13,110,253,0)');
      ctx.beginPath();
      ctx.arc(ph.x, ph.y, ringR, 0, Math.PI * 2);
      ctx.fillStyle = nodeGrad;
      ctx.fill();

      // Core dot
      ctx.beginPath();
      ctx.arc(ph.x, ph.y, 2.5, 0, Math.PI * 2);
      ctx.fillStyle = 'rgba(180,220,255,0.9)';
      ctx.fill();
    }

    raf = requestAnimationFrame(draw);
  }

  draw();
})();
</script>
</c:when>
<c:otherwise>
<div id="login-backdrop"></div>
</c:otherwise>
</c:choose>

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
