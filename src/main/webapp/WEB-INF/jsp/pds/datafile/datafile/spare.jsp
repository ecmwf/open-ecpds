<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>

<auth:if paths="/do/datafile/datafile/edit/delete_form/${datafile.id}">
	<auth:then>
		<c:if test="${!datafile.deleted}">
			<table class="editSpareBox">
				<tr>
					<th>Edit</th>
				</tr>
				<tr>
					<td><auth:link
							href="/do/datafile/datafile/edit/delete_form/${datafile.id}"
							imageKey="icon.small.delete" ignoreAccessControl="true">Delete</auth:link></td>
				</tr>
			</table>
		</c:if>
	</auth:then>
</auth:if>