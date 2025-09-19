<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>

<%
// To use for profiling
request.setAttribute("jsp_date_before", new java.util.Date());
%>

<html>
<head>

<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title><tiles:getAsString name="title" /></title>

<script src="/ace-editor/ace.js" charset="utf-8"></script>
<script src="/ace-editor/ext-language_tools.js" charset="utf-8"></script>
<script src="/ace-editor/ext-beautify.js" charset="utf-8"></script>

<script src="/assets/js/ecpds.js"></script>
<link rel="stylesheet" href="/assets/css/ecpds.css"
	type="text/css">

<link rel="stylesheet" href="/jquery/jquery-ui.min.css">
<script src="/jquery/jquery-3.7.0.min.js"></script>
<script src="/jquery/jquery-ui.min.js"></script>

<link rel="icon" type="image/png" href="/favicon.png">
<link rel="icon" type="image/x-icon" href="/favicon.ico">

</head>