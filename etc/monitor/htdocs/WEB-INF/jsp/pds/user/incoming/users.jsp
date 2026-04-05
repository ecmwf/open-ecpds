<%@ page session="true" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth" %>
<%@ taglib uri="/WEB-INF/tld/displaytag.tld" prefix="display" %> 
<%@ taglib uri="/WEB-INF/tld/fn.tld" prefix="fn" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<script>window._validIso=new Set(["AC","AD","AE","AF","AG","AI","AL","AM","AO","AQ","AR","AS","AT","AU","AW","AX","AZ","BA","BB","BD","BE","BF","BG","BH","BI","BJ","BL","BM","BN","BO","BQ","BR","BS","BT","BV","BW","BY","BZ","CA","CC","CD","CF","CG","CH","CI","CK","CL","CM","CN","CO","CP","CR","CU","CV","CW","CX","CY","CZ","DE","DG","DJ","DK","DM","DO","DZ","EA","EE","EG","EH","ER","ES","ET","EU","FI","FJ","FK","FM","FO","FR","GA","GB","GD","GE","GF","GG","GH","GI","GL","GM","GN","GP","GQ","GR","GS","GT","GU","GW","GY","HK","HM","HN","HR","HT","HU","IC","ID","IE","IL","IM","IN","IO","IQ","IR","IS","IT","JE","JM","JO","JP","KE","KG","KH","KI","KM","KN","KP","KR","KW","KY","KZ","LA","LB","LC","LI","LK","LR","LS","LT","LU","LV","LY","MA","MC","MD","ME","MF","MG","MH","MK","ML","MM","MN","MO","MP","MQ","MR","MS","MT","MU","MV","MW","MX","MY","MZ","NA","NC","NE","NF","NG","NI","NL","NO","NP","NR","NU","NZ","OM","PA","PE","PF","PG","PH","PK","PL","PM","PN","PR","PS","PT","PW","PY","QA","RE","RO","RS","RU","RW","SA","SB","SC","SD","SE","SG","SH","SI","SJ","SK","SL","SM","SN","SO","SR","SS","ST","SV","SX","SY","SZ","TA","TC","TD","TF","TG","TH","TJ","TK","TL","TM","TN","TO","TR","TT","TV","TW","TZ","UA","UG","UM","UN","US","UY","UZ","VA","VC","VE","VG","VI","VN","VU","WF","WS","XK","YE","YT","ZA","ZM","ZW"]);</script>

<form>
	<input class="search" name="search" type="text" placeholder="Search.." title="Search is performed across the Name in case-insensitive" value="${param['search']}">
	<select name="destinationNameForSearch" onchange="form.submit()" title="With access to (Data Policies not taken into account)">
		<c:forEach var="option" items="${destinationOptions}">
			<option value="${option.name}" <c:if test="${destinationNameForSearch == option.name}">SELECTED</c:if>>${option.name}</option>
		</c:forEach>
	</select>	
</form>

<c:if test="${empty users}">
<br/>
<div class="alert">
  No Data Users found based on these criteria!
</div>
</c:if>

<c:if test="${!empty users}">
	<display:table name="${users}" id="user" requestURI="" sort="list" pagesize="25" class="listing">
		<display:column title="Data Login" sortable="true"><a href="<bean:message key="incoming.basepath"/>/${user.id}">${user.id}</a></display:column>
		<display:column title="Comment" sortable="true">${user.comment}</display:column>
		<display:column title="Country" sortable="true"><img src="https://flagcdn.com/16x12/${fn:toLowerCase(user.country.iso)}.png" onload="var m=this.src.match(/\/([a-z]{2})\./);if(!m||!window._validIso||!window._validIso.has(m[1].toUpperCase()))this.style.display='none';" onerror="this.style.display='none'" alt="" class="me-1" style="vertical-align:middle">${user.country.name}</display:column>
		<display:column title="Enabled" sortable="true"><c:if test="${user.active}">yes</c:if><c:if test="${!user.active}"><font color="red">no</font></c:if></display:column>
		<display:column title="TOTP" sortable="true"><c:if test="${user.isSynchronized}">yes</c:if><c:if test="${!user.isSynchronized}">no</c:if></display:column>
		<display:column title="Anonymous" sortable="true">
	    	<c:if test="${user.anonymous}">
	        	<font color="red"><b>yes</b></font>
	    	</c:if>
	    	<c:if test="${!user.anonymous}">
	        	no
	    	</c:if>
		</display:column>
		<display:column title="Sessions" sortable="true">${fn:length(user.incomingConnections)}</display:column>
    	<display:column title="Actions" class="buttons">
    	<auth:link styleClass="menuitem" href="/do/user/incoming/edit/update_form/${user.id}" imageKey="icon.small.update"/>
		<auth:link styleClass="menuitem" href="/do/user/incoming/edit/delete_form/${user.id}" imageKey="icon.small.delete"/>
		</display:column>
	</display:table>
</c:if>
