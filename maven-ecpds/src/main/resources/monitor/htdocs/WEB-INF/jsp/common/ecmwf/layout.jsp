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

<tiles:insert name="html.head">
	<tiles:put name="title"><%=theTitle%></tiles:put>
</tiles:insert>

<tiles:insert name="footer">
   	<tiles:put name="helpKey"><tiles:getAsString name="helpKey" ignore="true"/></tiles:put>
</tiles:insert>

<style>
.loader,
.loader:after {
            border-radius: 50%;
            width: 10em;
            height: 10em;
        }
.loader {
	    margin: auto;
            font-size: 10px;
            position: static;
            text-indent: -9999em;
            border-top: 1.1em solid rgba(255, 255, 255, 0.2);
            border-right: 1.1em solid rgba(255, 255, 255, 0.2);
            border-bottom: 1.1em solid rgba(255, 255, 255, 0.2);
            border-left: 1.1em solid #eeeeee;
            -webkit-transform: translateZ(0);
            -ms-transform: translateZ(0);
            transform: translateZ(0);
            -webkit-animation: load8 1.1s infinite linear;
            animation: load8 1.1s infinite linear;
        }
@-webkit-keyframes load8 {
	    0% {
                -webkit-transform: rotate(0deg);
                transform: rotate(0deg);
            }
            100% {
                -webkit-transform: rotate(360deg);
                transform: rotate(360deg);
            }
        }
@keyframes load8 {
            0% {
                -webkit-transform: rotate(0deg);
                transform: rotate(0deg);
            }
            100% {
                -webkit-transform: rotate(360deg);
                transform: rotate(360deg);
            }
        }
#loadingDiv {
   position:fixed;
    top: 50%;
    left: 50%;
    width:10%;
    height:10%;
    margin-top: -9em; /*set to a negative number 1/2 of your height*/
    margin-left: -9em; /*set to a negative number 1/2 of your width*/
    background-color: #ffffff;
    z-index: 2147483647 !important;
}
/* Increase the width of the suggestion list */
.ace_editor.ace_autocomplete {
    width: 600px; /* Adjust the desired width */
}
.custom-ace-marker-info {
	position: absolute;
	background: #FFCCCB;
}
.custom-ace-marker-warning {
	position: absolute;
	background: #FFCCFF;
}
</style>

<body bgcolor="#ffffff" text="#000000">

			<tiles:insert name="header">
				<tiles:put name="title"><%=theTitle%></tiles:put>		
				<tiles:put name="submenu_width"><tiles:getAsString name="submenu_width"/></tiles:put>		
				<tiles:put name="location"><tiles:getAsString name="location"/></tiles:put>
				<tiles:put name="submenu_top"><tiles:getAsString name="submenu_top"/></tiles:put>
			</tiles:insert>

	<div style="" id="loadingDiv"><div style="" class="loader"></div></div>
	<div style="display: none;" id="contentDiv">
	<table id="outerTable" width="<tiles:getAsString name="page_width"/>" border="0" cellspacing="0" cellpadding="0" bgcolor="#ffffff" >
		<tr><td colspan=2 height=100>&nbsp;</td></tr>
		<tr>
			<td valign="top" width="<tiles:getAsString name="submenu_width"/>">
				<tiles:get name="submenu" />
				<tiles:get name="spare" />
				<tiles:get name="spare2" ignore="true"/>
				<tiles:get name="spare3" ignore="true"/>
			</td>
			<td valign="top" class="content">
				<tiles:insert name="content">
   					<tiles:put name="subcontent"><tiles:getAsString name="subcontent" ignore="true"/></tiles:put>
					<tiles:put name="date.select"><tiles:getAsString name="date.select" ignore="true"/></tiles:put>
					<tiles:put name="destination.select"><tiles:getAsString name="destination.select" ignore="true"/></tiles:put>
					<tiles:put name="metadata.select"><tiles:getAsString name="metadata.select" ignore="true"/></tiles:put>
				</tiles:insert>
			</td>
		</tr>
                <tr><td colspan=2 height=40>&nbsp;</td></tr>
	</table>
	</div>
</body>

<tiles:get name="html.bottom" />

<script>
$( function() {
    $( document ).tooltip();
  } );
$(window).on('load', function() {
    $("#loadingDiv").remove();
    $("#contentDiv").fadeIn("fast");
});
</script>
