<%@ page session="true"%>

<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>

<tiles:insert name="date.select" />
<tiles:insert name="metadata.select" />

<%-- Search form --%>
<div class="card border-0 shadow-sm mb-3">
    <div class="card-body py-2 px-3">
        <div class="row g-2 align-items-center">
            <div class="col-auto d-flex align-items-center gap-1">
                <span class="text-nowrap text-muted small">Show</span>
                <select id="datafilePageLen" class="form-select form-select-sm" style="width:auto" title="Entries per page">
                    <option value="10">10</option>
                    <option value="25" selected>25</option>
                    <option value="50">50</option>
                    <option value="100">100</option>
                </select>
                <span class="text-nowrap text-muted small">entries</span>
            </div>
            <div class="col">
                <div class="input-group">
                    <span class="input-group-text text-muted"><i class="bi bi-search"></i></span>
                    <input type="text" id="datafileSearch" class="form-control"
                           placeholder="Filter by original file name (use * and ? as wildcards)"
                           title="Substring search on original file name. Use * for zero or more characters, ? for exactly one.">
                </div>
            </div>
            <div class="col-auto">
                <button id="datafileSearchBtn" class="btn btn-primary">
                    <i class="bi bi-search"></i> Search
                </button>
                <button id="datafileSearchClear" class="btn btn-outline-secondary" title="Clear search">
                    <i class="bi bi-x-lg"></i>
                </button>
            </div>
        </div>
    </div>
</div>

<table id="datafileTable" class="table table-sm table-hover align-middle w-100">
    <thead>
        <tr>
            <th>Original</th>
            <th>Product Time</th>
            <th>Size</th>
            <th>TS</th>
        </tr>
    </thead>
</table>

<script>
(function () {
    var selectedDate      = '<c:out value="${selectedDate}"/>';
    var selectedMetaName  = '<c:out value="${selectedMetaDataName}"/>';
    var selectedMetaValue = '<c:out value="${selectedMetaDataValue}"/>';
    var currentSearch     = '';

    $(document).ready(function () {
        var table = $('#datafileTable').DataTable({
            serverSide: true,
            processing: true,
            ajax: {
                url: '/do/datafile/datafile/list',
                data: function (d) {
                    d.date           = selectedDate;
                    d.metaDataName   = selectedMetaName;
                    d.metaDataValue  = selectedMetaValue;
                    d.datafileSearch = currentSearch;
                }
            },
            order: [[1, 'desc']],
            columns: [
                { title: 'Original',     orderable: true,  render: function (data) { return data; } },
                { title: 'Product Time', orderable: true,  className: 'text-nowrap' },
                { title: 'Size',         orderable: true,  className: 'text-end text-nowrap' },
                { title: 'TS',           orderable: true,  className: 'text-end' }
            ],
            columnDefs: [],
            pageLength: 25,
            lengthMenu: [[10, 25, 50, 100], [10, 25, 50, 100]],
            language: {
                emptyTable:     'No Data Files found for the selected date and metadata.',
                loadingRecords: 'Loading&hellip;',
                processing:     '<i class="bi bi-hourglass-split"></i> Loading&hellip;'
            },
            dom: "t<'d-flex align-items-center mt-2'i<'ms-auto'p>>",
            buttons: []
        });

        $('#datafilePageLen').on('change', function () {
            table.page.len(parseInt(this.value, 10)).draw();
        });

        function doSearch() {
            currentSearch = $('#datafileSearch').val().trim();
            table.ajax.reload();
        }

        $('#datafileSearchBtn').on('click', doSearch);
        $('#datafileSearch').on('keydown', function (e) {
            if (e.key === 'Enter') { doSearch(); }
        });
        $('#datafileSearchClear').on('click', function () {
            $('#datafileSearch').val('');
            currentSearch = '';
            table.ajax.reload();
        });
    });
})();
</script>
