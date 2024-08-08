<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<a href="/"><%=System.getProperty("monitor.nickName")%> Home</a> &gt; <a href="/do/transfer">Transmission</a> &gt; <a href="/do/transfer/destination">Destinations</a> &gt;
<c:if test="${not empty destination}">${destination.name}</c:if>
