<div id="filterOperationSuccess" class="alert alert-success fade show d-flex align-items-center gap-2 mt-2" role="status">
	<i class="bi bi-check-circle-fill flex-shrink-0"></i>
	<div>${message}</div>
</div>
<script>
    setTimeout(function() {
        var el = document.getElementById('filterOperationSuccess');
        if (el) bootstrap.Alert.getOrCreateInstance(el).close();
    }, 5000);
</script>
