<%@ page %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<%-- Service Permissions guide offcanvas --%>
<div class="offcanvas offcanvas-end" tabindex="-1" id="permissionGuideOffcanvas"
     aria-labelledby="permissionGuideLabel" style="width:760px;max-width:95vw;">
  <div class="offcanvas-header border-bottom py-2 px-3">
    <h6 class="offcanvas-title mb-0 fw-semibold" id="permissionGuideLabel">
      <i class="bi bi-shield-check me-2 text-primary"></i>Service Permissions &mdash; Guide
    </h6>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>
  <div class="offcanvas-body p-3" style="overflow-y:auto; font-size:0.85rem;">

    <div class="alert alert-info py-2 px-3 mb-3 small d-flex align-items-start gap-2">
      <i class="bi bi-info-circle flex-shrink-0 mt-1"></i>
      <div>
        Each API client is granted access to a service only when at least one of its permission patterns
        matches the requested service name exactly (the pattern must match the <em>whole</em> name, not just
        a substring). Permissions are evaluated as regular expressions.
      </div>
    </div>

    <%-- How matching works --%>
    <p class="fw-semibold mb-1"><i class="bi bi-regex text-primary me-1"></i>How matching works</p>
    <div class="table-responsive mb-3">
      <table class="table table-sm table-bordered small mb-0">
        <thead class="table-light">
          <tr><th>Pattern</th><th>Matches</th><th>Use case</th></tr>
        </thead>
        <tbody>
          <tr><td><code>datafilePut</code></td><td>Exactly <code>datafilePut</code></td><td>Single service checkbox</td></tr>
          <tr><td><code>datafile(.*)</code></td><td>Any service starting with <code>datafile</code></td><td>All datafile services</td></tr>
          <tr><td><code>incoming(.*)</code></td><td>Any service starting with <code>incoming</code></td><td>All incoming services</td></tr>
          <tr><td><code>datafile.*|incoming.*</code></td><td>Datafile <em>or</em> incoming services</td><td>Two groups in one pattern</td></tr>
          <tr><td><code>.*</code></td><td>Every service</td><td>Full unrestricted access</td></tr>
        </tbody>
      </table>
    </div>

    <%-- Known services --%>
    <p class="fw-semibold mb-1"><i class="bi bi-list-check text-primary me-1"></i>Known services</p>
    <p class="small text-muted mb-2">The following services are recognised by the system. Each checkbox stores the exact service name as a permission pattern.</p>
    <div class="table-responsive mb-3">
      <table class="table table-sm table-bordered small mb-0">
        <thead class="table-light">
          <tr><th>Service name</th><th>Group</th><th>Description</th></tr>
        </thead>
        <tbody>
          <tr><td><code>datafilePut</code></td><td>Datafile</td><td>Upload / stage a data file.</td></tr>
          <tr><td><code>datafileSize</code></td><td>Datafile</td><td>Query the size of a stored data file.</td></tr>
          <tr><td><code>datafileDel</code></td><td>Datafile</td><td>Delete a data file from the system.</td></tr>
          <tr><td><code>destinationList</code></td><td>Destination</td><td>List all available destinations.</td></tr>
          <tr><td><code>destinationCountryList</code></td><td>Destination</td><td>List destinations filtered by country.</td></tr>
          <tr><td><code>destinationBackupList</code></td><td>Destination</td><td>List backup destinations.</td></tr>
          <tr><td><code>putDestinationBackup</code></td><td>Destination</td><td>Register or update a destination backup entry.</td></tr>
          <tr><td><code>getDestinationMetaFields</code></td><td>Metadata</td><td>Retrieve the metadata field definitions for a destination.</td></tr>
          <tr><td><code>getDestinationMetaValuesByDestination</code></td><td>Metadata</td><td>Fetch all metadata values associated with a destination.</td></tr>
          <tr><td><code>setDestinationMetaValues</code></td><td>Metadata</td><td>Set or update metadata values for a destination.</td></tr>
          <tr><td><code>incomingUserAdd</code></td><td>Incoming Users</td><td>Create a new incoming (dissemination) user account.</td></tr>
          <tr><td><code>incomingUserAdd2</code></td><td>Incoming Users</td><td>Create an incoming user account (alternative form with extended attributes).</td></tr>
          <tr><td><code>incomingUserList</code></td><td>Incoming Users</td><td>List all incoming user accounts.</td></tr>
          <tr><td><code>incomingUserDel</code></td><td>Incoming Users</td><td>Delete an incoming user account.</td></tr>
          <tr><td><code>incomingAssociationAdd</code></td><td>Incoming Associations</td><td>Associate an incoming user with a destination.</td></tr>
          <tr><td><code>incomingAssociationDel</code></td><td>Incoming Associations</td><td>Remove an association between an incoming user and a destination.</td></tr>
          <tr><td><code>incomingAssociationList</code></td><td>Incoming Associations</td><td>List all incoming-user/destination associations.</td></tr>
          <tr><td><code>incomingCategoryAdd</code></td><td>Incoming Associations</td><td>Add a category to an incoming user.</td></tr>
          <tr><td><code>updateHostOption</code></td><td>Other</td><td>Update a configuration option on a transfer host.</td></tr>
        </tbody>
      </table>
    </div>

    <%-- Checkboxes vs custom patterns --%>
    <p class="fw-semibold mb-1"><i class="bi bi-toggles text-primary me-1"></i>Checkboxes vs. custom patterns</p>
    <ul class="small mb-3 ps-3">
      <li class="mb-1"><strong>Checkboxes</strong> store the exact service name as the permission pattern. They are the quickest way to grant or revoke individual services.</li>
      <li class="mb-1"><strong>Custom patterns</strong> are free-form regex patterns. A single pattern can cover many services at once (e.g. <code>datafile(.*)</code> covers all three datafile services).</li>
      <li class="mb-1">When a custom pattern already covers a known service, the corresponding checkbox is shown as <strong>checked and greyed out</strong>. The checkbox cannot be unchecked independently — remove the custom pattern first.</li>
      <li class="mb-1">Access is granted if <em>any</em> permission pattern (checkbox or custom) matches the requested service name. There is no explicit deny; removing all matching patterns removes access.</li>
    </ul>

    <%-- Tips --%>
    <p class="fw-semibold mb-1"><i class="bi bi-lightbulb text-warning me-1"></i>Tips</p>
    <ul class="small mb-0 ps-3">
      <li class="mb-1">Use <code>.*</code> as a custom pattern only for trusted administrative clients; it grants access to every current and future service.</li>
      <li class="mb-1">Prefer narrow patterns (e.g. <code>datafile(.*)</code>) to limit blast radius if a client secret is compromised.</li>
      <li class="mb-1">After clicking <strong>Apply Changes</strong> the page reloads and shows the updated permissions. Changes are effective immediately.</li>
      <li>The <strong>all / none</strong> links select or deselect all checkboxes within a group — they do not remove existing custom patterns.</li>
    </ul>

  </div>
</div>
