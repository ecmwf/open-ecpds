<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<form action="<bean:message key="destination.basepath"/>/deletions/${datatransfer.destinationName}/deleteTransfer/${datatransfer.id}" method="GET">

<div class="alert">
  <span class="closebtn" onclick="parent.history.back();">&times;</span>
This operation will result in the following changes:
<ul>
<li>The Data Transfer ${datatransfer.id} for Destination ${datatransfer.destinationName} will be set to Deleted</li>
<li>In the background, if no other Data Transfer is attached to the related Data File (e.g. Aliases) then all physical files will be removed from the Data Movers</li>
</ul>
If you are completely sure this is what you want, click Process to proceed.
</div>
</br>
<tiles:insert page="/WEB-INF/jsp/common/ecmwf/buttons.jsp"><tiles:put name="operation" value="delete"/></tiles:insert>
</form>
