<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>

<link href="/assets/css/login.css?v=20260405" rel="stylesheet" type="text/css"/>

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

      <div class="login-field">
        <label for="login-pass"><i class="bi bi-lock"></i> Password</label>
        <div class="login-pass-wrap">
          <input id="login-pass" name="password" type="password" placeholder="Enter your password" autocomplete="current-password">
          <button type="button" id="login-toggle-pass" tabindex="-1" title="Show/hide password">
            <i class="bi bi-eye" id="login-eye"></i>
          </button>
        </div>
      </div>

      <button type="submit" id="login-btn">
        <i class="bi bi-box-arrow-in-right"></i> Sign in
      </button>

    </form>

  </div>
</div>

<script>
document.getElementById('login-toggle-pass').addEventListener('click', function() {
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
</script>

