<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%><%-- params: name=filterName, showName=true to append the name as text --%>
<c:set var="_cn" value="${param.name}"/><c:choose
><c:when test="${_cn eq 'zip'}"><i class="bi bi-file-zip text-muted" title="${_cn}" style="font-size:0.85rem"></i></c:when
><c:when test="${_cn eq 'gzip'}"><i class="bi bi-file-earmark-zip text-muted" title="${_cn}" style="font-size:0.85rem"></i></c:when
><c:when test="${_cn eq 'lzma'}"><i class="bi bi-box-seam text-muted" title="${_cn}" style="font-size:0.85rem"></i></c:when
><c:when test="${_cn eq 'bzip2a'}"><i class="bi bi-archive text-muted" title="${_cn}" style="font-size:0.85rem"></i></c:when
><c:when test="${_cn eq 'lbzip2'}"><i class="bi bi-cpu text-muted" title="${_cn}" style="font-size:0.85rem"></i></c:when
><c:when test="${_cn eq 'lz4'}"><i class="bi bi-lightning text-muted" title="${_cn}" style="font-size:0.85rem"></i></c:when
><c:when test="${_cn eq 'snappy'}"><i class="bi bi-lightning-charge text-muted" title="${_cn}" style="font-size:0.85rem"></i></c:when
><c:when test="${_cn eq 'zstd'}"><i class="bi bi-stack text-muted" title="${_cn}" style="font-size:0.85rem"></i></c:when
><c:when test="${not empty _cn and _cn ne 'none'}"><i class="bi bi-file-zip text-muted" title="${_cn}" style="font-size:0.85rem"></i></c:when
></c:choose><c:if test="${param.showName eq 'true'}"> <c:choose
><c:when test="${empty _cn or _cn eq 'none'}"><span class="text-muted fst-italic">none</span></c:when
><c:otherwise>${_cn}</c:otherwise
></c:choose></c:if>
