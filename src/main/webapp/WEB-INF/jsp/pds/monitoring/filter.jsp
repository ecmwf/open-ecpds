<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<div id="filterButton">

	<c:if test="${not empty reqData.filtered}">
		<a title="Filtered out: ${reqData.filtered}"
			href="javascript:toggleFilterDialogue(this)" class="selected"><img
			border=0 src="/assets/icons/webapp/kde31/configure.gif"></a>
	</c:if>

	<c:if test="${empty reqData.filtered}">
		<a title="Filtering options"
			href="javascript:toggleFilterDialogue(this)"><img border=0
			src="/assets/icons/webapp/kde31/configure.gif"></a>
	</c:if>

</div>

<div id="filter">
	<table>
		<tr>
			<th>Status</th>
			<th>Type</th>
			<th>Network</th>
		</tr>
		<tr>
			<td><c:forEach var="status" items="${reqData.statusOptions}">
					<div id="filter_status_${status.first}">
						<a href="javascript:filter('status','${status.first}')">${status.second}</a>
					</div>
				</c:forEach></td>
			<td><c:forEach var="type" items="${reqData.typeOptions}">
					<div id="filter_type_${type.first}">
						<a href="javascript:filter('type','${type.first}')">${type.second}</a>
					</div>
				</c:forEach></td>
			<td><c:forEach var="network" items="${reqData.networkOptions}">
					<div id="filter_network_${network.first}">
						<a href="javascript:filter('network','${network.first}')">${network.second}</a>
					</div>
				</c:forEach></td>
		</tr>
		<tr>
			<td colspan="3" class="foot"><a href="javascript:markAll('')">[all]</a>
				<a href="javascript:markAll('selected')">[none]</a> &nbsp; <a
				href="javascript:applyFilter()">[apply filter]</a> &nbsp; <a
				href="javascript:toggleFilterDialogue()">[close]</a></td>
		</tr>
	</table>
</div>

<script>
	function setFromParameters(values) {
		var bits;
		for (i in values) {
			bits = values[i].split("|");
			type = bits[0]
			for (j = 1; j < bits.length; j++) {
				if (bits[j] != "") {
					filter(type, bits[j]);
				}
			}
		}
	}

	function filter(what, which) {
		var div = document.getElementById("filter_" + what + "_" + which)
		if (div.className == 'selected') {
			div.className = '';
		} else {
			div.className = 'selected';
		}
	}
	function markAll(selected) {
		var children = document.getElementById('filter').getElementsByTagName(
				'div');
		for (i in children) {
			if (children[i].id && children[i].id.substring(0, 7) == 'filter_') {
				children[i].className = selected;
			}
		}
	}
	function applyFilter() {
		var children = document.getElementById('filter').getElementsByTagName(
				'div');
		var dic = {
			"type" : "",
			"status" : "",
			"network" : ""
		};
		for (i in children) {
			if (children[i].id && children[i].id.substring(0, 7) == 'filter_'
					&& children[i].className == 'selected') {
				bits = children[i].id.split("_");
				if (dic[bits[1]] != "") {
					dic[bits[1]] = dic[bits[1]] + "|" + bits[2];
				} else {
					dic[bits[1]] = bits[2];
				}
			}
		}
		var url = "?";
		for (key in dic) {
			url += key + "=" + dic[key] + "&";
		}
		window.location = url;
	}
	function toggleFilterDialogue(e) {
		var dialogue = document.getElementById('filter');
		dialogue.style.top = 50;
		dialogue.style.left = 25;
		if (!dialogue.style.display || dialogue.style.display == ''
				|| dialogue.style.display == 'none') {
			dialogue.style.display = 'block';
		} else {
			dialogue.style.display = 'none';
		}
	}
	function setDisseminationType() {
		markAll('');
		setFromParameters([ 'type|9|10|11|12|13|14|15|16|17|19|21|22|24|25|26|28|29|30' ]);
		applyFilter();
	}
	function setAcquisitionType() {
		markAll('');
		setFromParameters([ 'type|0|1|2|3|4|5|6|7|8|18|23|27' ]);
		applyFilter();
	}

	setFromParameters([ 'status|${monSesForm.status}',
			'type|${monSesForm.type}', 'network|${monSesForm.network}' ])
</script>
