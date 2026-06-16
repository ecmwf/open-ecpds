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
