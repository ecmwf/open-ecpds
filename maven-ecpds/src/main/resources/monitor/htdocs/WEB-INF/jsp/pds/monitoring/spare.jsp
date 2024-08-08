<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>


<c:if test="${warningStatiiSize gt 0}">

 <table background="/assets/images/webapp/shim.gif" bgcolor="#FFCECE" border="0" cellpadding="1" cellspacing="0" width="100%" class="submenu">
        <tbody>

        <tr>

          <td><span class="menuheading"> Warning !!! </span></td>
        </tr>
        <tr>
          <td>
            <table bgcolor="#ffffff" border="0" cellpadding="3" cellspacing="0" width="100%">
              <tr>
                <td>

                  <table bgcolor="#ffffff" width="100%">

<c:forEach var="pair" items="${warningStatii}">
<tr>
	<td>
	
	<c:if test="${pair.value.isArrival}">
		<a class="menuitem" href="/do/monitoring/arrival/<c:out value="${pair.name.name.name}/${pair.name.value.value}?mode=${param['mode']}"/>">
			<img src="<bean:message key="image.arrival.status.${pair.value.status}"/>" border="0">	</a>
	</c:if>

	<c:if test="${pair.value.isTransfer}">
		<a class="menuitem" href="/do/monitoring/transfer/<c:out value="${pair.name.name.name}/${pair.name.value.value}?mode=${param['mode']}"/>">
			<img src="<bean:message key="image.transfer.status.${pair.value.status}"/>" border="0">	</a>
	</c:if>
	
	
	</td>
	<td class="topnav1">
	<c:out value="${pair.name.name.name}"/>
	</td>
	<td class="topnav1">
	<c:out value="${pair.name.value.value}"/>
	</td>
</tr>
</c:forEach>
                  </table>

                </td>
              </tr>
            </table>
          </td>

        </tr>
        </tbody>
      </table>

<br />

</c:if>

<c:if test="${warningStatiiSize == 0}">

 <table background="/assets/images/webapp/shim.gif" bgcolor="#CEFFCE" border="0" cellpadding="1" cellspacing="0" width="100%" class="submenu">
        <tbody>

        <tr>

          <td><span class="menuheading"> Ok </span></td>
        </tr>
        <tr>
          <td>
            <table bgcolor="#ffffff" border="0" cellpadding="3" cellspacing="0" width="100%">
              <tr>
                <td>

                  <table bgcolor="#ffffff" width="100%">
					<tr><td class="topnav1">There aren't any arrivals or transfers with status worse than 3.</td></tr>
                  </table>

                </td>
              </tr>
            </table>
          </td>

        </tr>
        </tbody>
      </table>
</c:if>                  

<br/>