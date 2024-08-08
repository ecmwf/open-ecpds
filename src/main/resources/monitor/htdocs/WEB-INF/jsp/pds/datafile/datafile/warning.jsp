<%@ taglib uri="/WEB-INF/tld/bean-search.tld" prefix="content" %>

<div class="alert">
  <span class="closebtn" onclick="parent.history.back();">&times;</span>
This operation will result in the following changes:
<ul>
<li>The Data File ${datafile.id} will be set to Deleted</li>
<li>All related Data Transfers will be set to Deleted across all Destinations</li>
<li>All physical files will be removed from all Data Movers</li>
</ul>
If you are completely sure this is what you want, click Process to proceed.
</div>
