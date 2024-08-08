<%@ page session="true"%>

<!-- buttons.jsp -->

<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>

<tiles:importAttribute name="operation" />

<button type="submit" onclick="moveToWorkflowStage.value=''">Process</button>
<button type="submit" name="org.apache.struts.taglib.html.CANCEL">Cancel</button>

<!-- buttons.jsp -->
