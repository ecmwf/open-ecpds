<%@ page import="ecmwf.ecpds.master.MasterManager" %>
<%
    final boolean automaticLocation = !Boolean.FALSE.equals(request.getAttribute("automaticLocation"));
    final Double overrideLatitude   = (Double) request.getAttribute("overrideLatitude");
    final Double overrideLongitude  = (Double) request.getAttribute("overrideLongitude");
    final String resolvedHost       = (String) request.getAttribute("resolvedHost");
    final Double resolvedLatitude   = (Double) request.getAttribute("resolvedLatitude");
    final Double resolvedLongitude  = (Double) request.getAttribute("resolvedLongitude");
    final String olError            = (String) request.getAttribute("olError");
    final String olSuccess          = (String) request.getAttribute("olSuccess");
    // Coordinates the form fields should be pre-filled with: the stored override in manual mode, otherwise the
    // automatically-resolved location (so switching to manual mode starts from a sensible value instead of 0/0).
    final Double fieldLatitude  = !automaticLocation && overrideLatitude != null ? overrideLatitude : resolvedLatitude;
    final Double fieldLongitude = !automaticLocation && overrideLongitude != null ? overrideLongitude : resolvedLongitude;
%>

<div class="mb-4 px-3 py-3 rounded" style="background:rgba(108,117,125,0.07); border-left:4px solid #6c757d; font-size:0.85rem; color:var(--bs-body-color);">
    <div class="d-flex align-items-start gap-2">
        <i class="bi bi-geo-alt text-secondary flex-shrink-0 mt-1"></i>
        <span>
            The <strong>Origin Location</strong> is the geographic position used to place the
            <strong><%=System.getProperty("monitor.nickName")%></strong> marker on the
            <a href="/do/monitoring/globe">Live Earth</a> globe. By default it is resolved automatically via GeoIP;
            you can also set it manually if the automatic lookup is unavailable or inaccurate (e.g. a private/internal
            IP address).
        </span>
    </div>
</div>

<% if (olError != null) { %>
<div class="alert alert-danger d-flex gap-2 mb-4" role="alert">
    <i class="bi bi-exclamation-circle-fill flex-shrink-0 mt-1"></i>
    <span><%=olError%></span>
</div>
<% } %>

<% if (olSuccess != null) { %>
<div class="alert alert-success d-flex gap-2 mb-4" role="alert">
    <i class="bi bi-check-circle-fill flex-shrink-0 mt-1"></i>
    <span><%=olSuccess%></span>
</div>
<% } %>

<div class="card shadow-sm mb-4" style="max-width:640px;">
    <div class="card-header fw-semibold d-flex align-items-center gap-2">
        <i class="bi bi-geo-alt"></i>
        <span>Origin Location</span>
        <button class="btn btn-link btn-sm text-muted p-0" type="button"
            data-bs-toggle="collapse" data-bs-target="#olInfo"
            aria-expanded="false" title="About this section">
          <i class="bi bi-info-circle"></i>
        </button>
    </div>
    <div class="collapse" id="olInfo">
      <div class="px-3 py-2 border-bottom border-top" style="font-size:0.82rem; background:var(--bs-tertiary-bg,#e9ecef); border-top-width:3px!important; border-top-color:var(--bs-primary,#0d6efd)!important;">
        <strong class="d-block mb-1">About the Origin Location</strong>
        <ul class="mb-0 ps-3">
            <li><strong>Automatic mode</strong> &mdash; resolves the coordinates via a GeoIP lookup against this
            server's own hostname/IP address, the same way any other Transfer Host's location is resolved. This
            lookup may not be available for private/internal IP ranges.</li>
            <li><strong>Manual mode</strong> &mdash; uncheck <em>Automatic Location</em> to supply exact coordinates,
            either by typing them directly or by clicking <em>Pick on map</em>.</li>
            <li>This location only affects the origin marker on the <em>Live Earth</em> globe; it has no effect on
            data transfers themselves.</li>
        </ul>
      </div>
    </div>
    <div class="card-body">

        <div class="field-grid mb-3">
            <div class="field-row"><div class="field-label">Hostname/IP</div><div class="field-value">
                <% if (resolvedHost != null) { %><span class="val-code"><%=resolvedHost%></span>
                <% } else { %><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">Unresolved</span><% } %>
            </div></div>
            <div class="field-row"><div class="field-label">Estimated Location</div><div class="field-value">
                <% if (resolvedLatitude != null && resolvedLongitude != null) { %>
                <span class="val-num"><%=String.format("%.4f", resolvedLatitude)%> / <%=String.format("%.4f", resolvedLongitude)%></span>
                <% } else { %><span class="badge rounded-pill border fw-normal bg-body-tertiary text-muted fst-italic">No geolocation available</span><% } %>
            </div></div>
        </div>

        <% if (!automaticLocation && resolvedLatitude == null) { %>
        <div class="alert alert-warning mb-3 py-2 px-3 d-flex gap-2" role="alert">
            <i class="bi bi-exclamation-triangle-fill flex-shrink-0 mt-1"></i>
            <span>The origin location is currently set manually because the automatic GeoIP lookup could not resolve
            it for this server.</span>
        </div>
        <% } %>

        <form method="post" action="/do/admin/origin" id="originLocationForm">
        <div class="row g-3 align-items-end">
        <div class="col-sm-12">
        <div class="d-flex align-items-center gap-2 flex-wrap">
        <div class="form-check form-switch mb-0">
        <input type="checkbox" class="form-check-input" id="automaticLocationField" name="automaticLocation"
               <%=automaticLocation ? "checked" : ""%> />
        <label class="form-check-label" for="automaticLocationField">Automatic Location</label>
        </div>
        <i class="bi bi-question-circle text-muted" style="cursor:pointer;font-size:0.8em" data-bs-toggle="popover" data-bs-placement="right" data-bs-content="Try to get the latitude/longitude from this server's IP address" tabindex="0"></i>
        </div>
        </div>
        <div class="col-sm-4">
        <label for="latitudeField" class="form-label mb-1">Latitude (&deg;)</label>
        <input type="text" class="form-control form-control-sm" id="latitudeField" name="latitude"
               value="<%=fieldLatitude != null ? fieldLatitude : ""%>" <%=automaticLocation ? "disabled" : ""%> />
        </div>
        <div class="col-sm-4">
        <label for="longitudeField" class="form-label mb-1">Longitude (&deg;)</label>
        <input type="text" class="form-control form-control-sm" id="longitudeField" name="longitude"
               value="<%=fieldLongitude != null ? fieldLongitude : ""%>" <%=automaticLocation ? "disabled" : ""%> />
        </div>
        <div class="col-sm-4">
        <button type="button" id="pickOnMapBtn" class="btn btn-sm btn-outline-secondary w-100"
                onclick="openMapPicker()" <%=automaticLocation ? "disabled" : ""%>
                title="Click a point on the map to set coordinates">
            <i class="bi bi-map me-1"></i>Pick on map
        </button>
        </div>
        </div>
        <div class="d-flex gap-2 mt-4">
            <button type="submit" class="btn btn-primary">
                <i class="bi bi-check-lg me-1"></i>Save
            </button>
        </div>
        </form>
    </div>
</div>

<%-- Map coordinate picker modal --%>
<div class="modal fade" id="mapPickerModal" tabindex="-1" aria-labelledby="mapPickerModalLabel" aria-hidden="true">
<div class="modal-dialog modal-lg modal-dialog-centered">
<div class="modal-content">
<div class="modal-header py-2">
    <h6 class="modal-title" id="mapPickerModalLabel"><i class="bi bi-geo-alt-fill me-2 text-primary"></i>Pick Location on Map</h6>
    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
</div>
<div class="modal-body p-0" style="position:relative;">
    <div id="mapPickerMap" style="height:420px; width:100%;"></div>
    <div id="mapPickerCoords" style="
        position:absolute; bottom:10px; left:50%; transform:translateX(-50%);
        background:rgba(20,20,20,0.82); color:#e8e8e8; border-radius:6px;
        padding:0.3rem 0.75rem; font-size:0.78rem; font-family:monospace;
        pointer-events:none; backdrop-filter:blur(4px);
        border:1px solid rgba(255,255,255,0.1);
        white-space:nowrap;">
        Click anywhere to set location
    </div>
</div>
<div class="modal-footer py-2">
    <span class="text-muted small me-auto"><i class="bi bi-info-circle me-1"></i>Click anywhere on the map to place the pin</span>
    <button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal">Cancel</button>
    <button type="button" class="btn btn-sm btn-primary" id="mapPickerConfirm" onclick="_confirmMapPicker()" disabled>
        <i class="bi bi-check-lg me-1"></i>Confirm
    </button>
</div>
</div>
</div>
</div>

<link rel="stylesheet" href="/openlayer/ol.css"/>
<script src="/openlayer/ol.js"></script>
<script>
    function toggleLocationFields() {
        var checkbox = document.getElementById("automaticLocationField");
        var latitude = document.getElementById("latitudeField");
        var longitude = document.getElementById("longitudeField");
        var pickBtn  = document.getElementById("pickOnMapBtn");
        var disabled = checkbox.checked;
        latitude.disabled = disabled;
        longitude.disabled = disabled;
        if (pickBtn) pickBtn.disabled = disabled;
    }

    /* ---- Map coordinate picker -------------------------------- */
    var _pickerMap = null, _pickerPin = null, _pickerSrc = null, _pickedLat = null, _pickedLon = null;

    function _pickerUpdateDisplay(lat, lon) {
        var el = document.getElementById('mapPickerCoords');
        if (el) el.textContent = '\uD83D\uDCCD  ' + lat.toFixed(6) + '\u00b0,  ' + lon.toFixed(6) + '\u00b0';
    }

    function openMapPicker() {
        var autoChk = document.getElementById('automaticLocationField');
        if (autoChk && autoChk.checked) return;

        var modalEl = document.getElementById('mapPickerModal');
        bootstrap.Modal.getOrCreateInstance(modalEl).show();

        /* Init map after Bootstrap's fade animation (350ms) */
        setTimeout(function() {
            if (!window.ol) return;
            if (_pickerMap) { _pickerMap.updateSize(); return; }

            _pickerSrc = new ol.source.Vector();
            _pickerPin = new ol.Feature();
            _pickerPin.setStyle(new ol.style.Style({
                image: new ol.style.Circle({
                    radius: 8,
                    fill: new ol.style.Fill({ color: '#0d6efd' }),
                    stroke: new ol.style.Stroke({ color: '#fff', width: 2.5 })
                })
            }));

            var initLat = parseFloat(document.getElementById('latitudeField').value);
            var initLon = parseFloat(document.getElementById('longitudeField').value);
            var hasInit = isFinite(initLat) && isFinite(initLon) && (initLat !== 0 || initLon !== 0);
            if (hasInit) {
                _pickerPin.setGeometry(new ol.geom.Point(ol.proj.fromLonLat([initLon, initLat])));
                _pickerSrc.addFeature(_pickerPin);
                _pickedLat = initLat; _pickedLon = initLon;
                _pickerUpdateDisplay(initLat, initLon);
                document.getElementById('mapPickerConfirm').disabled = false;
            }

            _pickerMap = new ol.Map({
                target: 'mapPickerMap',
                controls: ol.control.defaults.defaults({ rotate: false }),
                layers: [
                    new ol.layer.Tile({ source: new ol.source.OSM({ attributions: [] }) }),
                    new ol.layer.Vector({ source: _pickerSrc, zIndex: 10 })
                ],
                view: new ol.View({
                    center: hasInit ? ol.proj.fromLonLat([initLon, initLat]) : ol.proj.fromLonLat([10, 48]),
                    zoom: hasInit ? 8 : 3
                })
            });
            _pickerMap.getTargetElement().style.cursor = 'crosshair';
            _pickerMap.on('click', function(evt) {
                var lonLat = ol.proj.toLonLat(evt.coordinate);
                _pickedLon = Math.round(lonLat[0] * 1e6) / 1e6;
                _pickedLat = Math.round(lonLat[1] * 1e6) / 1e6;
                _pickerPin.setGeometry(new ol.geom.Point(evt.coordinate));
                if (_pickerSrc.getFeatures().length === 0) _pickerSrc.addFeature(_pickerPin);
                _pickerUpdateDisplay(_pickedLat, _pickedLon);
                document.getElementById('mapPickerConfirm').disabled = false;
            });
        }, 350);
    }

    function _confirmMapPicker() {
        if (_pickedLat === null) return;
        document.getElementById('latitudeField').value  = _pickedLat.toFixed(6);
        document.getElementById('longitudeField').value = _pickedLon.toFixed(6);
        bootstrap.Modal.getInstance(document.getElementById('mapPickerModal')).hide();
    }

    document.getElementById('automaticLocationField').addEventListener('change', toggleLocationFields);
</script>
