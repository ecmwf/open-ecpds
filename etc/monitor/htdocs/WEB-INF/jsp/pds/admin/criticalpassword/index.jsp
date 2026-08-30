<%@ page import="ecmwf.ecpds.master.MasterManager" %>
<%
    final boolean hasPassword = Boolean.TRUE.equals(request.getAttribute("hasPassword"));
    final String capError   = (String) request.getAttribute("capError");
    final String capSuccess = (String) request.getAttribute("capSuccess");
%>

<div class="mb-4 px-3 py-3 rounded" style="background:rgba(108,117,125,0.07); border-left:4px solid #6c757d; font-size:0.85rem; color:var(--bs-body-color);">
    <div class="d-flex align-items-start gap-2">
        <i class="bi bi-key-fill text-secondary flex-shrink-0 mt-1"></i>
        <span>
            The <strong>Critical Password</strong> is required to confirm irreversible administrative actions
            (such as purging all data). It is separate from your normal administrator password and is stored as a
            secure hash in the database. Only administrators can set or renew it.
        </span>
    </div>
</div>

<% if (capError != null) { %>
<div class="alert alert-danger d-flex gap-2 mb-4" role="alert">
    <i class="bi bi-exclamation-circle-fill flex-shrink-0 mt-1"></i>
    <span><%=capError%></span>
</div>
<% } %>

<% if (capSuccess != null) { %>
<div class="alert alert-success d-flex gap-2 mb-4" role="alert">
    <i class="bi bi-check-circle-fill flex-shrink-0 mt-1"></i>
    <span><%=capSuccess%></span>
</div>
<% } %>

<div class="card shadow-sm mb-4" style="max-width:540px;">
    <div class="card-header fw-semibold">
        <i class="bi bi-key-fill me-2"></i>
        <% if (hasPassword) { %>Renew Critical Password<% } else { %>Set Critical Password<% } %>
    </div>
    <div class="card-body">
        <% if (!hasPassword) { %>
        <div class="alert alert-warning mb-3 py-2 px-3 d-flex gap-2" role="alert">
            <i class="bi bi-exclamation-triangle-fill flex-shrink-0 mt-1"></i>
            <span>No Critical Password has been configured yet. Please set one before performing any
            irreversible administrative actions.</span>
        </div>
        <% } %>

        <form method="post" action="/do/admin/criticalpassword" autocomplete="off">

            <% if (hasPassword) { %>
            <div class="mb-3">
                <label for="currentPassword" class="form-label fw-semibold">Current Password</label>
                <input type="password" class="form-control" id="currentPassword" name="currentPassword"
                       autocomplete="current-password" required />
                <div class="form-text text-muted">Enter the existing Critical Password to confirm your identity.</div>
            </div>
            <% } %>

            <div class="mb-3">
                <label for="newPassword" class="form-label fw-semibold">New Password</label>
                <input type="password" class="form-control" id="newPassword" name="newPassword"
                       autocomplete="new-password" minlength="12" required />
                <div class="form-text text-muted">Minimum 12 characters. Choose a strong, unique password.</div>
            </div>

            <div class="mb-4">
                <label for="confirmPassword" class="form-label fw-semibold">Confirm New Password</label>
                <input type="password" class="form-control" id="confirmPassword" name="confirmPassword"
                       autocomplete="new-password" minlength="12" required />
            </div>

            <div class="d-flex gap-2">
                <button type="submit" class="btn btn-warning">
                    <i class="bi bi-key-fill me-1"></i>
                    <% if (hasPassword) { %>Renew Password<% } else { %>Set Password<% } %>
                </button>
                <a href="/do/admin" class="btn btn-outline-secondary">
                    <i class="bi bi-arrow-left me-1"></i>Back to Admin Tasks
                </a>
            </div>
        </form>
    </div>
</div>

<div class="card shadow-sm" style="max-width:540px; border-color: #dee2e6;">
    <div class="card-header text-muted fw-semibold" style="background:rgba(108,117,125,0.07);">
        <i class="bi bi-info-circle me-2"></i>About the Critical Password
    </div>
    <div class="card-body text-muted" style="font-size:0.85rem;">
        <ul class="mb-0 ps-3">
            <li>Stored as a SHA-256 hash in the database — the plaintext is never persisted.</li>
            <li>Independent of your administrator login credentials.</li>
            <li>Required when performing destructive operations such as <em>Purge All Data</em>.</li>
            <li>Only administrators with access to <em>Admin Tasks</em> can set or renew it.</li>
            <li>Renewing the password takes effect immediately for all logged-in administrators.</li>
        </ul>
    </div>
</div>
