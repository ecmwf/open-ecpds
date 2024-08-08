<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>

<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %> 
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<tiles:useAttribute name="isInsert" classname="java.lang.String"/>

<script>
function validate(path,message) {
        if (confirm(message)) {
                window.location = path
        }
}
function hideChoosers(layerName) {
        if (layerName!='categoryChooser') hide('categoryChooser');
}
</script>

<table>

<tr>
<td>

<table class="fields">

<c:if test="${isInsert != 'true'}">
<tr><th>Path</th><td>${resourceActionForm.path}<html:hidden property="path"/></td></tr>
</c:if>
<c:if test="${isInsert == 'true'}">
<tr><th>Path</th><td><html:text property="path"/></td></tr>
</c:if>

</table>

</td>

<td width="25">
</td>

<td>

<c:if test="${isInsert != 'true'}">
<% boolean odd; %>
<div id="categoryChooser" class="chooser">
     <table class="listing" border=0>
        <caption>Choose a Web Category to Add</caption>
        <thead>
        <tr>
        <th></th>
        <th class="sorted order1">Name</th></tr></thead>
        <tbody>
        <% odd = true; %>
        <c:forEach var="column" items="${resourceActionForm.categoryOptions}">
        <tr class='<%=(odd?"odd":"even")%>'>
          <td><a href="/do/user/resource/edit/update/${resourceActionForm.id}/addCategory/${column.id}"><img src="/assets/icons/webapp/left_small.gif" alt="Add" title="Add"/></a></td>
          <td><span title="${column.description}">${column.name}</td>
        </tr>
        </tbody>
        <% odd = !odd; %>
        </c:forEach>
        </table>
</div>
</c:if>

</td>
</tr>
<tr><td>

<c:if test="${isInsert != 'true'}">
	<display:table id="category" name="${resourceActionForm.categories}" requestURI="" class="listing">
    	<display:column	property="name" sortable="true" title="Name"/>
 	<display:column property="description" title="Description"/>
	<display:column><a href="javascript:validate('<bean:message key="resource.basepath"/>/edit/update/<c:out value="${resourceActionForm.id}"/>/deleteCategory/<c:out value="${category.id}"/>','<bean:message key="ecpds.resource.deleteCategory.warning" arg0="${category.name}" arg1="${resourceActionForm.path}"/>')"><content:icon key="icon.small.delete" titleKey="button.delete" altKey="button.delete" writeFullTag="true"/></a></display:column>
	<display:caption>Associated Web Categories <a href="#" onClick="toggle_in_place(event,'categoryChooser','');"><content:icon key="icon.small.insert" titleKey="button.insert" altKey="button.insert" writeFullTag="true"/></a></display:caption>
	</display:table>
</c:if>

</td></tr>
</table>
