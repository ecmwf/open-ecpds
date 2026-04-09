<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<c:if test="${not empty destination.typeText}"><c:choose
><c:when test="${destination.typeText == 'Gold'}"
><span class="dest-page-type dest-type-gold"><i class="bi bi-trophy-fill"></i> Gold</span
></c:when
><c:when test="${destination.typeText == 'Silver'}"
><span class="dest-page-type dest-type-silver"><i class="bi bi-award-fill"></i> Silver</span
></c:when
><c:when test="${destination.typeText == 'Bronze'}"
><span class="dest-page-type dest-type-bronze"><i class="bi bi-award"></i> Bronze</span
></c:when
><c:when test="${destination.typeText == 'Basic'}"
><span class="dest-page-type dest-type-basic"><i class="bi bi-patch-check"></i> Basic</span
></c:when
><c:otherwise
><span class="dest-page-type">${destination.typeText}</span
></c:otherwise
></c:choose></c:if>
