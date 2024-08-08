<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:insert name="date.select" />
<tiles:insert name="metadata.select" />

<display:table name="${datafileList}" id="datafile" requestURI=""
	sort="external" defaultsort="2" partialList="true"
	size="${datafileListSize}" pagesize="${recordsPerPage}" class="listing">
	<display:column title="Original" sortable="true">
		<a href="<bean:message key="datafile.basepath"/>/${datafile.id}">${datafile.formattedOriginal}</a>
	</display:column>
	<display:column property="productTime" sortable="true" />
	<display:column property="size" sortable="true" />
	<display:column property="timeStep" title="TS" sortable="true" />
	<display:caption>
			Data Files for MetaData <b>${selectedMetaDataName}</b> = "${selectedMetaDataValue}"
	</display:caption>
</display:table>
