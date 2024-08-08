<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<h3>
	Output for <c:out value="${host.nickName}" />
</h3>

<div class="info">
	<pre><div id="includedContent" /></pre>
</div>

<script>
	$(document).ready(
			function() {
				$('#includedContent').load(
						"/do/transfer/host/edit/getOutput/load/${host.name}",
						function(response, status, xhr) {
							if (status == "error" || !response.trim()) {
								$('#includedContent').html(
										"No output available for ${host.nickName}");
							}
						});
			});
</script>
