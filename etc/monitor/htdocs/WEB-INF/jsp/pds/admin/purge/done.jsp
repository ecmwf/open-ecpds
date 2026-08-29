<%-- Result page shown after successful purge --%>
<%@ page import="java.lang.Boolean" %>
<div class="mb-4 p-3 rounded border border-success-subtle"
     style="background:rgba(25,135,84,0.07); border-left:4px solid #198754 !important;">
    <div class="d-flex align-items-start gap-2">
        <i class="bi bi-check-circle-fill text-success flex-shrink-0 mt-1" style="font-size:1.4rem;"></i>
        <div>
            <strong class="text-success">All data has been marked for deletion</strong>
            <p class="mb-0 mt-1" style="font-size:0.9rem;">
                Every transfer and file in the system is now queued for removal.
            </p>
        </div>
    </div>
</div>

<div class="card mb-4">
    <div class="card-header d-flex align-items-center gap-2">
        <i class="bi bi-info-circle text-primary"></i>
        <strong>Summary</strong>
    </div>
    <div class="card-body">
        <table class="table table-sm table-bordered mb-4" style="max-width:360px;">
            <thead class="table-light">
                <tr><th>Item</th><th class="text-end">Rows marked</th></tr>
            </thead>
            <tbody>
                <tr>
                    <td><i class="bi bi-arrow-repeat me-1 text-secondary"></i>Transfers</td>
                    <td class="text-end fw-bold">${purgedTransfers}</td>
                </tr>
                <tr>
                    <td><i class="bi bi-file-earmark me-1 text-secondary"></i>Files</td>
                    <td class="text-end fw-bold">${purgedFiles}</td>
                </tr>
            </tbody>
        </table>

<%
    final Boolean triggered = (Boolean) request.getAttribute("triggered");
    final Boolean forceDeleteDB = (Boolean) request.getAttribute("forceDeleteDB");
    if (Boolean.TRUE.equals(triggered)) {
%>
        <div class="alert alert-success d-flex gap-2 mb-3">
            <i class="bi bi-lightning-charge-fill flex-shrink-0 mt-1"></i>
            <div>
                <strong>Cleanup triggered — running in the background.</strong>
                The system is removing database records and contacting each data mover to delete
                files from disk. You can safely leave this page — the process continues in the
                background. The duration depends on the volume of data, the number of data movers,
                and network conditions — this could take anywhere from a few minutes to several hours.
            </div>
        </div>
<% } else { %>
        <div class="alert alert-info d-flex gap-2 mb-3">
            <i class="bi bi-clock-history flex-shrink-0 mt-1"></i>
            <div>
                The system will automatically clean up database records and delete files from
                disk on the next scheduled maintenance cycle, typically within a few minutes.
                No manual action is required.
            </div>
        </div>
<% } %>

<% if (Boolean.TRUE.equals(forceDeleteDB)) { %>
        <div class="alert alert-warning d-flex gap-2 mb-3">
            <i class="bi bi-database-x flex-shrink-0 mt-1"></i>
            <div>
                <strong>Database hard-delete started in the background.</strong>
                All transfer records, transfer history and file entries are being removed from the
                database — depending on the volume of data and the number of data movers, this could take anywhere from a few minutes to several hours. Once complete,
                a full disk scan will be triggered on every data mover to remove any unreferenced files.
                Progress is visible in the MasterServer log. No manual action is required.
            </div>
        </div>
<% } %>

    </div>
</div>

<a href="/do/admin" class="btn btn-secondary">
    <i class="bi bi-arrow-left me-1"></i>Back to Admin
</a>
