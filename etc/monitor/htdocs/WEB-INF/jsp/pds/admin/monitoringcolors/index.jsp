<%@ page session="true" contentType="text/html;charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%
    final String mcError = (String) request.getAttribute("mcError");
    final String mcSuccess = (String) request.getAttribute("mcSuccess");
%>

<div class="mb-4 px-3 py-3 rounded"
     style="background:rgba(108,117,125,0.07);border-left:4px solid #6c757d;font-size:0.85rem;color:var(--bs-body-color);">
    <div class="d-flex align-items-start gap-2">
        <i class="bi bi-palette-fill text-secondary flex-shrink-0 mt-1"></i>
        <span>Customize the status colors shared by monitoring legends, status indicators, charts and related views.
        Changes apply throughout the Monitor UI.</span>
    </div>
</div>

<% if (mcError != null) { %>
<div class="alert alert-danger d-flex align-items-start gap-2" role="alert">
    <i class="bi bi-exclamation-circle-fill mt-1"></i><span><c:out value="<%=mcError%>" /></span>
</div>
<% } %>
<% if (mcSuccess != null) { %>
<div id="monitoringColorsSuccess" class="alert alert-success fade show d-flex align-items-start gap-2" role="status">
    <i class="bi bi-check-circle-fill mt-1"></i><span><c:out value="<%=mcSuccess%>" /></span>
</div>
<script>
    setTimeout(function() {
        var el = document.getElementById('monitoringColorsSuccess');
        if (el) bootstrap.Alert.getOrCreateInstance(el).close();
    }, 5000);
</script>
<% } %>

<form method="post" action="/do/admin/monitoringcolors">
    <c:forEach var="group" items="${colorGroups}">
        <div class="card shadow-sm mb-4">
            <div class="card-header fw-semibold"><i class="bi bi-circle-fill me-2 text-secondary"></i><c:out value="${group}" /></div>
            <div class="table-responsive">
                <table class="table table-sm table-hover align-middle mb-0">
                    <thead>
                        <tr><th scope="col">Status</th><th scope="col" style="width:14rem;">Color / Hex</th><th scope="col">Default</th></tr>
                    </thead>
                    <tbody>
                        <c:forEach var="setting" items="${colorSettings}">
                            <c:if test="${setting.group eq group}">
                                <tr>
                                    <th scope="row" class="fw-normal"><c:out value="${setting.label}" /></th>
                                    <td>
                                        <div class="d-flex align-items-center gap-2">
                                            <input type="color" class="form-control form-control-color"
                                                   name="<c:out value="${setting.key}" />"
                                                   value="<c:out value="${statusColors[setting.key]}" />"
                                                   oninput="var row=this.parentElement;row.querySelector('.mc-color-preview').style.backgroundColor=this.value;row.querySelector('.mc-color-code').textContent=this.value.toUpperCase()"
                                                   title="Choose color for <c:out value="${setting.label}" />">
                                            <span class="mc-color-preview rounded border"
                                                  style="display:inline-block;width:2.5rem;height:1.5rem;background-color:<c:out value="${statusColors[setting.key]}" />;"
                                                  aria-label="Color preview"></span>
                                            <code class="mc-color-code"><c:out value="${statusColors[setting.key]}" /></code>
                                        </div>
                                    </td>
                                    <td>
                                        <span class="mc-color-preview rounded border me-2"
                                              style="display:inline-block;width:2.5rem;height:1.5rem;vertical-align:middle;background-color:<c:out value="${setting.defaultColor}" />;"
                                              aria-label="Default color preview"></span>
                                        <code><c:out value="${setting.defaultColor}" /></code>
                                    </td>
                                </tr>
                            </c:if>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </c:forEach>
    <div class="d-flex flex-wrap gap-2">
        <button type="submit" class="btn btn-primary"><i class="bi bi-check2 me-1"></i>Save Colors</button>
        <button type="submit" class="btn btn-outline-secondary" name="resetDefaults" value="on"
                onclick="return confirm('Reset all monitoring status colors to their built-in defaults?');">
            <i class="bi bi-arrow-counterclockwise me-1"></i>Reset All to Defaults
        </button>
    </div>
</form>
