<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>

<c:forEach var="product" items="${reqData.productWindow}"
	varStatus="status">
	<td colspan="2" style="white-space: nowrap"><a
		title="Product for <content:content name="product.productTime" ignoreNull="true" defaultValue="*" dateFormatKey="date.format.medium"/>"
		href="/do/monitoring/summary/${product.product}/${product.time}">
			${product.product}<br> <content:content
				name="product.scheduledTime" ignoreNull="true" defaultValue="*"
				dateFormatKey="date.format.time.short" />
	</a><br>${product.time}z</td>
</c:forEach>