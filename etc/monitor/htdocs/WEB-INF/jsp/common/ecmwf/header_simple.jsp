<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>
<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>

<script>
	$(document).ready(function() {
		$(".account").click(function() {
			var X = $(this).attr('id');
			if (X == 1) {
				$(".submenu").hide();
				$(this).attr('id', '0');
			} else {
				$(".submenu").show();
				$(this).attr('id', '1');
			}
		});

		//Mouse click on sub menu
		$(".submenu").mouseup(function() {
			return false
		});

		//Mouse click on my account link
		$(".account").mouseup(function() {
			return false
		});

		//Document Click
		$(document).mouseup(function() {
			$(".submenu").hide();
			$(".account").attr('id', '');
		});
	});
</script>

<style>
.dropdown {
	color: #555;
	margin: 3px -22px 0 0;
	width: 243px;
	position: relative;
	height: 17px;
	text-align: left;
}

.submenu {
	background: #fff;
	position: absolute;
	top: -12px;
	left: -20px;
	z-index: 100;
	width: 235px;
	display: none;
	margin-left: 10px;
	padding: 40px 0 5px;
	border-radius: 6px;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.45);
}

.dropdown li a {
	color: #555555;
	display: block;
	font-family: arial;
	font-weight: bold;
	padding: 6px 15px;
	cursor: pointer;
	text-decoration: none;
}

.dropdown li a:hover {
	background: #155FB0;
	color: #FFFFFF;
	text-decoration: none;
}

a.account {
	font-size: 14px;
	line-height: 16px;
	color: #cccccc;
	position: absolute;
	z-index: 110;
	display: block;
	padding: 11px 0 0 20px;
	height: 28px;
	width: 221px;
	margin: -11px 0 0 -10px;
	text-decoration: none;
	cursor: pointer;
}

.root {
	list-style: none;
	margin: 0px;
	padding: 0px;
	font-size: 11px;
	padding: 11px 0 0 0px;
	border-top: 1px solid #dedede;
}
</style>

<div id="topheader" class="topheader">
	<table width="100%" cellpadding="0" cellspacing="0" height="100"
		border="0">
		<tr>
			<td bgcolor="<%=System.getProperty("monitor.color")%>">
				<table width="100%" border="0">
					<tr>
						<td width="50">&nbsp;</td>
						<td width="240"><a href="/"><img
								src="/assets/images/logo.production.png" border="0"
								alt="Home page" width="140" height="24"></a></td>
						<td><div class="header_simple_title">
								<tiles:getAsString name="title" />
							</div></td>
						<td align="right" valign="middle"><tiles:insert
								name="submenu_top" /></td>
					</tr>
				</table>
			</td>
			<td bgcolor="<%=System.getProperty("monitor.color")%>" colspan=2>
				<logic:present name="<%=ecmwf.web.model.users.User.SESSION_KEY%>">
					<table width="100%">
						<tr>
							<td width="340" align="right">
								<div class="dropdown">
									<a class="account"><auth:info property="commonName" /> <font
										size=-1>(<auth:info property="uid" />)&nbsp;&nbsp;<img
											src="/assets/icons/displaytag/arrow_up.png"></font></a>
									<div class="submenu">
										<ul class="root">
											<li><a href="/do/logout">Sign Out</a></li>
										</ul>
									</div>
								</div>
							</td>
						</tr>
					</table>
				</logic:present>
			</td>
		</tr>
		<tr>
			<td bgcolor="#ffffff" height="1" colspan="3"></td>
		</tr>
		<tr>
			<td bgcolor="#000000" colspan="3"><div class="location_simple">
					<tiles:insert name="location" />
				</div></td>
		</tr>
	</table>
</div>
