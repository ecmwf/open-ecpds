<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>

<table background="/assets/images/webapp/shim.gif" bgcolor="#FFCE9C"
	border="0" cellpadding="1" cellspacing="0" width="100%" class="submenu">
	<tbody>
		<tr>
			<td><span class="menuheading">Related</span></td>
		</tr>
		<tr>
			<td>
				<table bgcolor="#ffffff" border="0" cellpadding="3" cellspacing="0"
					width="100%">
					<tr>
						<td>
							<table bgcolor="#ffffff" width="100%">
								<tr>
									<td><auth:link styleClass="menuitem"
											href="/do/datafile/metadata/edit/insert_form"
											imageKey="icon.small.insert">Insert Meta Data</auth:link></td>
								</tr>
								<tr>
									<td><auth:link styleClass="menuitem"
											href="/do/datafile/metadata/edit/update_form/${metadata.id}"
											imageKey="icon.small.update">Edit Meta Data</auth:link></td>
								</tr>
								<tr>
									<td><auth:link styleClass="menuitem"
											href="/do/datafile/metadata/edit/delete_form/${metadata.id}"
											imageKey="icon.small.delete">Delete Meta Data</auth:link></td>
								</tr>
							</table>
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</tbody>
</table>

<br />