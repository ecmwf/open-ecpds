<!DOCTYPE html>

<%@ page session="true" import ="ecmwf.web.view.taglibs.util.TagUtils"%>

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

	<tiles:insert name="footer">
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
