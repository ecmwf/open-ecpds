<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>

<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<div class="metadata-strip">
    <c:choose>
    <c:when test="${empty metaDataNameOptions and empty metaDataValueOptions}">
        <div class="metadata-strip-row">
            <span class="metadata-strip-label text-muted">Field</span>
            <span class="badge rounded-pill bg-secondary-subtle text-secondary-emphasis border">None</span>
        </div>
        <div class="metadata-strip-row">
            <span class="metadata-strip-label text-muted">Value</span>
            <span class="badge rounded-pill bg-secondary-subtle text-secondary-emphasis border">None</span>
        </div>
    </c:when>
    <c:otherwise>

    <%-- Metadata name selector --%>
    <c:if test="${not empty metaDataNameOptions}">
        <div class="metadata-strip-row">
            <span class="metadata-strip-label">Field</span>
            <div class="metadata-strip-pills">
                <c:forEach items="${metaDataNameOptions}" var="opt">
                    <c:choose>
                        <c:when test="${opt.name == selectedMetaDataName}">
                            <a class="metadata-pill active" href="?metaDataName=${opt.name}">${opt.name}</a>
                        </c:when>
                        <c:otherwise>
                            <a class="metadata-pill" href="?metaDataName=${opt.name}">${opt.name}</a>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </div>
        </div>
    </c:if>

    <%-- Metadata value selector --%>
    <c:if test="${not empty metaDataValueOptions}">
        <div class="metadata-strip-row">
            <span class="metadata-strip-label">Value</span>
            <div class="metadata-strip-pills">
                <c:forEach items="${metaDataValueOptions}" var="opt">
                    <c:choose>
                        <c:when test="${opt.value == selectedMetaDataValue}">
                            <a class="metadata-pill value active" href="?metaDataName=${selectedMetaDataName}&amp;metaDataValue=${opt.value}">${opt.value}</a>
                        </c:when>
                        <c:otherwise>
                            <a class="metadata-pill value" href="?metaDataName=${selectedMetaDataName}&amp;metaDataValue=${opt.value}">${opt.value}</a>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </div>
        </div>
    </c:if>

    </c:otherwise>
    </c:choose>
</div>
