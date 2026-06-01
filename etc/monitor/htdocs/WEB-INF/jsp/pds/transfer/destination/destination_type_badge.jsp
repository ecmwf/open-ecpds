<%@ page trimDirectiveWhitespaces="true" %><%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<c:if test="${not empty destination.typeText}"><c:choose
><c:when test="${destination.typeText == 'Gold'}"
><a href="/do/transfer/destination?destinationType=${destination.type}&amp;destinationSearch=" class="dest-type-link" title="Show all ${destination.typeText} destinations"><span class="dest-page-type dest-type-gold"><i class="bi bi-trophy-fill"></i> Gold</span></a
></c:when
><c:when test="${destination.typeText == 'Silver'}"
><a href="/do/transfer/destination?destinationType=${destination.type}&amp;destinationSearch=" class="dest-type-link" title="Show all ${destination.typeText} destinations"><span class="dest-page-type dest-type-silver"><i class="bi bi-award-fill"></i> Silver</span></a
></c:when
><c:when test="${destination.typeText == 'Bronze'}"
><a href="/do/transfer/destination?destinationType=${destination.type}&amp;destinationSearch=" class="dest-type-link" title="Show all ${destination.typeText} destinations"><span class="dest-page-type dest-type-bronze"><i class="bi bi-award"></i> Bronze</span></a
></c:when
><c:when test="${destination.typeText == 'Basic'}"
><a href="/do/transfer/destination?destinationType=${destination.type}&amp;destinationSearch=" class="dest-type-link" title="Show all ${destination.typeText} destinations"><span class="dest-page-type dest-type-basic"><i class="bi bi-patch-check"></i> Basic</span></a
></c:when
><c:otherwise
><a href="/do/transfer/destination?destinationType=${destination.type}&amp;destinationSearch=" class="dest-type-link" title="Show all ${destination.typeText} destinations"><span class="dest-page-type">${destination.typeText}</span></a
></c:otherwise
></c:choose></c:if>
