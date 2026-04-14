<!DOCTYPE html>

<%@ page session="true" import ="ecmwf.web.view.taglibs.util.TagUtils"%>

<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>

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

<style>
#loadingDiv{position:fixed;top:50%;left:50%;width:120px;height:120px;margin:-60px 0 0 -60px;background:var(--bs-body-bg,#fff);border-radius:16px;box-shadow:0 8px 24px rgba(0,0,0,.25);display:flex;align-items:center;justify-content:center;z-index:2147483647;}
.loader{border-radius:50%;width:56px;height:56px;border:6px solid var(--bs-border-color,rgba(0,0,0,.1));border-left-color:var(--bs-primary,#0d6efd);animation:load8 .9s linear infinite;}
@keyframes load8{0%{transform:rotate(0deg);}100%{transform:rotate(360deg);}}
</style>

<tiles:insert name="html.head">
	<tiles:put name="title"><%=theTitle%></tiles:put>
</tiles:insert>

<tiles:insert name="footer">
	<tiles:put name="helpKey"><tiles:getAsString name="helpKey" ignore="true"/></tiles:put>
</tiles:insert>

<body bgcolor="#ffffff" text="#000000">

			<tiles:insert name="header">
				<tiles:put name="title"><%=theTitle%></tiles:put>
				<tiles:put name="submenu_width">100%</tiles:put>
				<tiles:put name="location"><tiles:getAsString name="location"/></tiles:put>
				<tiles:put name="submenu_top"><tiles:getAsString name="submenu_top"/></tiles:put>
			</tiles:insert>

	<div style="" id="loadingDiv"><div style="" class="loader"></div></div>
	<div style="display: none;" id="contentDiv">
	<div id="outerTable" class="content" style="width:100%;">
		<tiles:get name="content" />
	</div>

</body>

<tiles:get name="html.bottom" />

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
