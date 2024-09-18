<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>

<c:forEach var="product" items="${reqData.productWindow}"
	varStatus="status">
	<c:set var="key" value="${product.product}@${product.time}" />
	<c:set var="emails" value="${reqData.contacts[key]}" />
	<c:set var="ECMWFProductsDelay"
		value="Dear colleagues%2C%0A%3C%3C Due to if known%2C please give some information of the reason for the%0Adelay%2C the or otherwise The %3E%3E dissemination of ECMWF %3C%3Catmospheric or%0Awave%3E%3E products for the %3C%3C00%2C 06%2C 12 or 18%3E%3EZ cycle of the%0A%3C%3C%22high-resolution forecast%22 or %22BC high-resolution forecast%22 or %22ensemble forecast%22 or %22Limited-area wave forecast%22%3E%3E will be delayed.%0A%0AAs soon as we have further details we will inform you.%0A%0AFor more up to date information%2C you may refer to ECMWF service status%0Apage at http%3A%2F%2Fwww.ecmwf.int%2Fen%2Fservice-status .%0A%0AOur sincere apologies for the inconvenience caused by this delay.%0A%0AKind regards%0A%0AECMWF Duty Manager" />
	<c:set var="ECMWFProducts"
		value="Dear colleagues,%0D%0A%0D%0AI am pleased to inform you that the problems we encountered earlier%0D%0Awithin the operational production have been resolved and the dissemination of products has started.%0D%0A%0D%0AOur sincere apologies for the inconvenience caused by this delay.%0D%0A%0D%0AKind regards%0D%0A%0D%0AECMWF Duty Manager%0D%0A" />
	<td colspan="2" style="white-space: nowrap"><a
		title="Product for <content:content name="product.productTime" ignoreNull="true" defaultValue="*" dateFormatKey="date.format.medium"/>"
		href="/do/monitoring/summary/${product.product}/${product.time}">
			${product.product}<br> <content:content
				name="product.scheduledTime" ignoreNull="true" defaultValue="*"
				dateFormatKey="date.format.time.short" />
	</a><br> <c:choose>
			<c:when test="${not empty emails}">
				<a
					title="Products Delay Email for ${product.time}-${product.product}"
					target="_blank" style="text-decoration: none" id="delayEmail">+</a>${product.time}z
				<a title="Products Email for ${product.time}-${product.product}"
					target="_blank" style="text-decoration: none" id="productEmail">+</a>
				<script>
					setHrefForSendingEmail(
							document.getElementById('delayEmail'),
							'op_delay_ecmwf@lists.ecmwf.int,ecpds-product-${productStatus.time}-${productStatus.product}@ecmwf.int',
							'ECMWF Products Delay (${product.time}-${product.product})',
							'${ECMWFProductsDelay}');
					setHrefForSendingEmail(
							document.getElementById('productEmail'),
							'op_delay_ecmwf@lists.ecmwf.int,ecpds-product-${productStatus.time}-${productStatus.product}@ecmwf.int',
							'ECMWF Products (${product.time}-${product.product})',
							'${ECMWFProducts}');
				</script>
			</c:when>
			<c:otherwise>
    	    	${product.time}z
  	  		</c:otherwise>
		</c:choose></td>
</c:forEach>