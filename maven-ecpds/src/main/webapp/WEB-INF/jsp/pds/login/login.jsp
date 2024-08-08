<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>

<link href="/assets/css/login.css" rel="stylesheet" type="text/css"/>

<script>
$(document).ready(function() {
        $(".username").focus(function() {
                $(".user-icon").css("left","-48px");
        });
        $(".username").blur(function() {
                $(".user-icon").css("left","0px");
        });
       
        $(".password").focus(function() {
                $(".pass-icon").css("left","-48px");
        });   
        $(".password").blur(function() {
                $(".pass-icon").css("left","0px");
        });
});
</script>

<div id="wrapper">
  <div class="user-icon"></div>
  <div class="pass-icon"></div>

<form name="login-form" class="login-form" action="/do/login" method="post" autocomplete="off">

    <div class="header">
    <h1>Sign in</h1>
    <span>Please type a User name and Password to sign in to your account.</span>
    </div>
        
    <div class="content">
      <input name="user" type="text" class="input username" value="User name" onfocus="this.value=''"/>
      <input name="password" type="password" class="input password" value="Password" onfocus="this.value=''"/>
    </div>

    <div class="message">
    <span><html:errors/></span>
    </div>

    <div class="footer">
      <input type="submit" name="submit" value="Login" class="button"/>
    </div>

</form>

</div>

<div class="gradient"></div>
