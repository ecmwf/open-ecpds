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

<tiles:insert name="html.head">
	<tiles:put name="title"><%=theTitle%></tiles:put>
</tiles:insert>

<tiles:insert name="footer">
   	<tiles:put name="helpKey"><tiles:getAsString name="helpKey" ignore="true"/></tiles:put>
</tiles:insert>

<style>
/* Backdrop (unchanged if you already added it) */
#loadingBackdrop {
    display: none;
    position: fixed;
    inset: 0; /* shorthand for top/right/bottom/left:0 */
    background: rgba(0,0,0,0.35);
    z-index: 2147483646;
}

/* Loader container (the white square) */
#loadingDiv {
    display: none;            /* shown via JS */
    position: fixed;
    top: 50%;
    left: 50%;

    /* square size (tweak as you like) */
    width: 160px;
    height: 160px;

    /* center the box itself */
    margin-left: -80px;       /* -1/2 width */
    margin-top: -80px;        /* -1/2 height */

    background-color: #ffffff;
    border-radius: 12px;
    box-shadow: 0 8px 20px rgba(0,0,0,0.35);
    z-index: 2147483647;

    /* center whatever is inside perfectly */
    display: flex;
    align-items: center;
    justify-content: center;

    /* remove internal padding to avoid shifting the spinner */
    padding: 0;
}

/* Spinner */
.loader,
.loader:after {
    border-radius: 50%;
    width: 80px;   /* reduced from 10em to a fixed size for predictable layout */
    height: 80px;
}

.loader {
    /* remove margin/position tricks; flexbox handles centering */
    margin: 0;
    position: relative;
    text-indent: -9999em; /* harmless, keeps legacy structure */

    /* high-contrast borders */
    border-top: 10px solid rgba(0,0,0,0.15);
    border-right: 10px solid rgba(0,0,0,0.15);
    border-bottom: 10px solid rgba(0,0,0,0.15);
    border-left: 10px solid #333333;

    /* rotate around center only */
    animation: load8 1.1s linear infinite;
    transform: rotate(0deg); /* define baseline for animation */
}

@keyframes load8 {
    0%   { transform: rotate(0deg);   }
    100% { transform: rotate(360deg); }
}

/* If you still target older WebKit, keep the prefixed version too */
@-webkit-keyframes load8 {
    0%   { -webkit-transform: rotate(0deg);   }
    100% { -webkit-transform: rotate(360deg); }
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
	
			<iframe title="sandboxFrame" id="sandboxFrame" style="display:none;"></iframe>

			<tiles:insert name="header">
				<tiles:put name="title"><%=theTitle%></tiles:put>		
				<tiles:put name="submenu_width"><tiles:getAsString name="submenu_width"/></tiles:put>		
				<tiles:put name="location"><tiles:getAsString name="location"/></tiles:put>
				<tiles:put name="submenu_top"><tiles:getAsString name="submenu_top"/></tiles:put>
			</tiles:insert>

	<div id="loadingBackdrop"></div>
	<div id="loadingDiv"><div class="loader"></div></div>
	<div id="confirmationDialog" style="display:none;"><p id="confirmationDialogMessage" style="margin-top:10px;"></p></div>
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
    $("#loadingBackdrop").hide();
    $("#loadingDiv").hide();
    $("#contentDiv").fadeIn("fast");
});
/**
 * Generic confirmation dialog.
 *
 * Usage (simple):
 *   confirmationDialog("Title", "Message HTML...", function onConfirm(){ ... });
 *
 * Usage (options object):
 *   confirmationDialog({
 *     title: "Confirm Action",
 *     message: "Message HTML...",
 *     onConfirm: function(){ ... },
 *     showLoading: true,         // default true
 *     width: 500,                // default 500
 *     confirmText: "Confirm",    // default "Confirm"
 *     cancelText: "Cancel",      // default "Cancel"
 *     onCancel: function(){},    // optional
 *     allowHtml: true            // message is HTML; keep true if passing <br/> etc.
 *   });
 */
function confirmationDialog(arg1, arg2, arg3) {
    // Normalize args to options object
    var opts = {};
    if (typeof arg1 === "object") {
        opts = arg1 || {};
    } else {
        opts.title     = arg1;
        opts.message   = arg2;
        opts.onConfirm = arg3;
    }
    var title        = opts.title || "Please Confirm";
    var message      = opts.message || "";
    var onConfirm    = typeof opts.onConfirm === "function" ? opts.onConfirm : function(){};
    var onCancel     = typeof opts.onCancel === "function"  ? opts.onCancel  : function(){};
    var width        = opts.width || 500;
    var confirmText  = opts.confirmText || "Confirm";
    var cancelText   = opts.cancelText  || "Cancel";
    var showLoading  = (opts.showLoading !== false); // default true
    var allowHtml    = (opts.allowHtml !== false);   // default true
    // Inject message
    if (allowHtml) {
        $("#confirmationDialogMessage").html(message);
    } else {
        $("#confirmationDialogMessage").text(message);
    }
    // Open dialog
    $("#confirmationDialog").dialog({
        modal: true,
        width: width,
        title: title,
        // Ensure ESC can't accidentally close during confirm flow (optional)
        closeOnEscape: true,
        buttons: [
            {
                text: confirmText,
                click: function () {
                    var dlg = $(this);
                    // Prevent double clicks
                    var $buttons = dlg.parent().find(".ui-dialog-buttonpane button");
                    $buttons.prop("disabled", true);

                    // Show global loading overlay if desired
                    if (showLoading) {
                        $("#loadingBackdrop").show();
                        $("#loadingDiv").show();
                    }
                    // Close the dialog to clear the screen
                    dlg.dialog("close");
                    // Allow repaint so overlay is visible before heavy work/navigation
                    setTimeout(function () {
                        try {
                            onConfirm();
                        } finally {
                            // Typically you do NOT hide overlay here because a navigation
                            // to a new page will occur; the new page can hide/remove it
                            // on $(window).on('load', ...) as you already do.
                            //
                            // If your onConfirm does not navigate and you want to
                            // hide the overlay after async work, do it there.
                        }
                    }, 50);
                },
                "class": "confirm-button" // optional to style as danger
            },
            {
                text: cancelText,
                click: function () {
                    // Ensure overlay is not shown if user cancels
                    $("#loadingBackdrop").hide();
                    $("#loadingDiv").hide();
                    $(this).dialog("close");
                    onCancel();
                }
            }
        ],
        // Optional: ensure focus is managed
        open: function () {
            // Focus the confirm button by default (or cancel per your preference)
            var $dlg = $(this).parent();
            $dlg.find(".ui-dialog-buttonpane button:eq(0)").trigger("focus");
        }
    });
}
</script>