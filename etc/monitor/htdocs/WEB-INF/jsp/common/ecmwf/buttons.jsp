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
<div class="modal fade" id="confirmDialog" tabindex="-1"
     aria-labelledby="confirmDialogLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="confirmDialogLabel">
                    Confirm Deletion of ${entityLabel} ${entityName}
                </h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"
                        aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <p>
                    This action cannot be undone. If you are unsure, please click the
                    Cancel button and carefully review the red warning above.
                </p>
                <p>
                    If you understand the implications and wish to proceed, type
                    <strong>${entityName}</strong> in the box below to confirm the action:
                </p>
                <input type="text" id="confirmInput" class="form-control" />
                <div id="confirmError" class="alert alert-danger mt-2" style="display:none;">
                    <span id="confirmErrorText"></span>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary"
                        data-bs-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-danger"
                        id="confirmDialogSubmitBtn">Confirm</button>
            </div>
        </div>
    </div>
</div>

<c:set var="jsEntityName" value="${fn:escapeXml(entityName)}"/>

<script>
$(document).ready(function () {
    const processBtn = $('button[type="submit"]').first();
    const confirmModal = new bootstrap.Modal(document.getElementById('confirmDialog'));

    processBtn.on("click", function (e) {
        e.preventDefault();
        $("#confirmInput").val("");
        $("#confirmError").hide();
        confirmModal.show();
    });

    $("#confirmDialogSubmitBtn").on("click", function () {
        const val = $("#confirmInput").val().trim();
        const expected = "${jsEntityName}";
        if (val === expected) {
            confirmModal.hide();
            $("#loadingBackdrop").show();
            $("#loadingDiv").show();
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
    });
});
</script>
</c:if>

<button type="submit" onclick="moveToWorkflowStage.value=''"><c:out value="${safeButtonLabel}" /></button>
<button type="submit" name="org.apache.struts.taglib.html.CANCEL">Cancel</button>

<!-- buttons.jsp -->
