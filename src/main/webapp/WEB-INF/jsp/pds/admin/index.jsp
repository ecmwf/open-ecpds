<%@ taglib uri="/WEB-INF/tld/auth2-taglib.tld" prefix="auth"%>

<style>
p {
	Font-size: 14px;
}

.menu h3 {
	font-size: 14pt;
	font-family: "Open Sans", "Helvetica Neue", Helvetica, Arial, sans-serif;
	margin: 0;
}

.menu ul {
	list-style-type: none;
	margin: 0;
	padding: 0;
}

.menu li {
	font-size: 12pt;
	font-family: "Open Sans", "Helvetica Neue", Helvetica, Arial, sans-serif;
	border-bottom: 1px solid #ccc;
}

.menu li:last-child {
	border: none;
}

.menu li a {
	text-decoration: none;
	color: #000;
	display: block;
	width: 300px;
}

.menu li a:hover {
	text-decoration: underline;
}

h3 {
	font-family: "Open Sans", "Helvetica Neue", Helvetica, Arial, sans-serif;
	Font-size: 16pt;
	width: 100%;
}

li {
	font-family: "Open Sans", "Helvetica Neue", Helvetica, Arial, sans-serif;
	font-size: 14px;
}
</style>

<div style="width: 600px; padding: 0px;">
	<h3>Administration Tasks</h3>
	<p>Various tools for Administrators.</p>

	<div class="menu">
		<h3 class="menu">Options</h3>
		<p>
		<ul>
			<auth:link basePathKey="admin.basepath" href="/filter"
				wrappingTags="li">Compress Files</auth:link>
			<auth:link basePathKey="admin.basepath" href="/requeue"
				wrappingTags="li">Outstanding Files</auth:link>
			<auth:link basePathKey="admin.basepath" href="/upload"
				wrappingTags="li">Upload Files</auth:link>
		</ul>
	</div>
</div>
