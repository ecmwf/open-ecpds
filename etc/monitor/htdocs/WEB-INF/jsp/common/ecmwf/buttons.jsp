<%@ page session="true"%>

<!-- buttons.jsp -->

<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<tiles:importAttribute name="operation" />

<c:set var="buttonLabel" value="${empty buttonLabel ? 'Process' : buttonLabel}" />
<c:set var="safeButtonLabel" value="${fn:escapeXml(buttonLabel)}" />

<c:if test="${not empty entityLabel and not empty entityName}">
<div id="confirmDialog"
     title="Confirm Deletion of ${entityLabel} ${entityName}"
     style="display:none;">
    <p>
        This action cannot be undone. If you are unsure, please click the
        Cancel button and carefully review the red warning above.
    </p>
    <p>
        If you understand the implications and wish to proceed, type
        <strong>${entityName}</strong> in the box below to confirm the action:
    </p>
    <input type="text" id="confirmInput" style="width:95%; padding:5px;" />
    <div id="confirmError" class="ui-state-error ui-corner-all"
         style="display:none; margin-top:10px; padding:8px;">
        <span class="ui-icon ui-icon-alert"
              style="float:left; margin-right:6px;"></span>
        <span id="confirmErrorText"></span>
    </div>
</div>

<c:set var="jsEntityName" value="${fn:escapeXml(entityName)}"/>

<script>
$(document).ready(function () {
    const processBtn = $('button[type="submit"]').first();
    processBtn.on("click", function (e) {
        e.preventDefault();
        $("#confirmInput").val("");
        $("#confirmError").hide();
        $("#confirmDialog").dialog({
            modal: true,
            width: 400,
            buttons: {
                "Confirm": function () {
                    const val = $("#confirmInput").val().trim();
                    const expected = "${jsEntityName}";
		    if (val === expected) {
                        $(this).dialog("close");
                        // Show your existing loadingDiv BEFORE form submits
			$("#loadingBackdrop").show();
			$("#loadingDiv").show();
                        // Allow browser to repaint before the real submission
			setTimeout(function () {
				processBtn.off("click");
				processBtn.closest("form")[0].submit();
			}, 50);
                    } else {
                        $("#confirmErrorText").text(
                            'Incorrect confirmation. Please type "' + expected + '".'
                        );
                        $("#confirmError").show();
                    }
                },
                "Cancel": function () {
                    $(this).dialog("close");
                }
            }
        });
    });
});
</script>
</c:if>

<button type="submit" onclick="moveToWorkflowStage.value=''"><c:out value="${safeButtonLabel}" /></button>
<button type="submit" name="org.apache.struts.taglib.html.CANCEL">Cancel</button>

<!-- buttons.jsp -->
