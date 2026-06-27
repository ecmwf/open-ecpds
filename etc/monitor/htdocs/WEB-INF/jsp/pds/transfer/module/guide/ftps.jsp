<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ page pageEncoding="UTF-8" %>

<c:set var="_guideId"
       value="${not empty param.guideId ? param.guideId : 'moduleGuideOffcanvas'}"/>

<div class="offcanvas offcanvas-end"
     tabindex="-1"
     id="${_guideId}"
     aria-labelledby="${_guideId}-label"
     style="width:min(720px,68vw);">

    <div class="offcanvas-header border-bottom py-2 px-3">
        <h6 class="offcanvas-title mb-0 fw-semibold" id="${_guideId}-label">
            <i class="bi bi-book me-2 text-info"></i>
            Configuration Guide
        </h6>

        <button type="button"
                class="btn-close"
                data-bs-dismiss="offcanvas"
                aria-label="Close"></button>
    </div>

    <div class="offcanvas-body p-3" style="overflow-y:auto; font-size:0.85rem;">

        <div class="alert alert-info py-2 px-3 mb-3 small d-flex align-items-start gap-2">
            <i class="bi bi-info-circle flex-shrink-0 mt-1"></i>
            <div>
                A configuration guide is not yet available for this module.
            </div>
        </div>

        <p class="small text-muted mb-0">
            Documentation for this module will be added in a future release.
        </p>

    </div>

</div>
