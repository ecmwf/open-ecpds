<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>

<%
// To use for profiling
request.setAttribute("jsp_date_before", new java.util.Date());
%>

<html data-bs-theme="light">
<head>

<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title><tiles:getAsString name="title" /></title>

<!-- Apply saved theme before first paint to prevent flash -->
<script>(function(){var t=localStorage.getItem('ecpds-theme');if(t)document.documentElement.setAttribute('data-bs-theme',t);}());</script>
<!-- Hide page before first paint if we need to restore scroll - prevents visible jump -->
<script>(function(){if(sessionStorage.getItem('scrollY'))document.documentElement.style.visibility='hidden';}());</script>
<!-- Bootstrap 5 -->
<link rel="stylesheet" href="/bootstrap/css/bootstrap.min.css">
<!-- Bootstrap Icons -->
<link rel="stylesheet" href="/bootstrap-icons/bootstrap-icons.min.css">
<!-- Flag Icons (local SVG, best quality) -->
<link rel="stylesheet" href="/flag-icons/css/flag-icons.min.css?v=20260413a">
<!-- DataTables with Bootstrap 5 styling -->
<link rel="stylesheet" href="/datatables/css/dataTables.bootstrap5.min.css">
<!-- jQuery UI (kept for sliders and date pickers) -->
<link rel="stylesheet" href="/jquery/jquery-ui.min.css">
<!-- Application styles (loaded last to override where needed) -->
<link rel="stylesheet" href="/assets/css/ecpds.css?v=20260413g" type="text/css">

<script src="/ace-editor/ace.js" charset="utf-8"></script>
<script src="/ace-editor/ext-language_tools.js" charset="utf-8"></script>
<script src="/ace-editor/ext-beautify.js" charset="utf-8"></script>
<script src="/assets/js/ecpds.js?v=20260413a"></script>

<!-- jQuery (required by jQuery UI, DataTables, and application scripts) -->
<script src="/jquery/jquery-3.7.0.min.js"></script>
<script src="/jquery/jquery-ui.min.js"></script>
<script>
// Preserve jQuery UI tooltip in case Bootstrap overrides $.fn.tooltip
if (typeof $.fn.tooltip === 'function' && typeof $.ui !== 'undefined' && $.ui.tooltip) {
    $.fn.uiTooltip = $.fn.tooltip;
}
</script>
<!-- Bootstrap 5 bundle (includes Popper.js for dropdowns and tooltips) -->
<script src="/bootstrap/js/bootstrap.bundle.min.js"></script>
<!-- DataTables with Bootstrap 5 integration -->
<script src="/datatables/js/dataTables.min.js"></script>
<script src="/datatables/js/dataTables.bootstrap5.min.js"></script>

<script>
// Show a Bootstrap toast notification. type: 'success' | 'danger' | 'warning' | 'info' (default)
function showToast(message, type) {
    type = type || 'info';
    var iconMap = {
        success: 'bi-check-circle-fill text-success',
        danger:  'bi-exclamation-circle-fill text-danger',
        warning: 'bi-exclamation-triangle-fill text-warning',
        info:    'bi-info-circle-fill text-info'
    };
    var icon = iconMap[type] || iconMap.info;
    var id = 'toast-' + Date.now();
    var html = '<div id="' + id + '" class="toast align-items-center border-0 shadow" role="alert" aria-live="assertive" aria-atomic="true">'
             + '<div class="d-flex">'
             + '<div class="toast-body d-flex align-items-center gap-2">'
             + '<i class="bi ' + icon + '"></i>'
             + '<span>' + message + '</span>'
             + '</div>'
             + '<button type="button" class="btn-close me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>'
             + '</div></div>';
    var container = document.getElementById('ecpds-toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'ecpds-toast-container';
        container.className = 'toast-container position-fixed bottom-0 end-0 p-3';
        container.style.zIndex = 9999;
        document.body.appendChild(container);
    }
    container.insertAdjacentHTML('beforeend', html);
    var toastEl = document.getElementById(id);
    var toast = new bootstrap.Toast(toastEl, { delay: 4000 });
    toastEl.addEventListener('hidden.bs.toast', function() { toastEl.remove(); });
    toast.show();
}

// Global handler for association card search - called directly via oninput
function assocSearch(inp) {
    var q = inp.value.toLowerCase();
    var container = inp.closest('.collapse');
    if (!container) return;
    container.querySelectorAll('.assoc-chooser-item').forEach(function(el) {
        el.classList.toggle('assoc-hidden', el.textContent.toLowerCase().indexOf(q) === -1);
    });
}

// Switch destination insert mode panels (Copy / Create / Export)
function selectDestMode(mode, btn) {
    // Update button styles
    document.querySelectorAll('.dest-mode-btn').forEach(function(b) {
        b.classList.remove('btn-primary');
        b.classList.add('btn-outline-secondary');
    });
    btn.classList.remove('btn-outline-secondary');
    btn.classList.add('btn-primary');
    // Show the selected panel, hide the others
    ['copy', 'create', 'export'].forEach(function(m) {
        var panel = document.getElementById('panel-' + m);
        if (panel) panel.classList.toggle('d-none', m !== mode);
    });
    // Show common fields and editor tabs only for Create from Scratch
    var commonFields = document.getElementById('dest-common-fields');
    if (commonFields) commonFields.classList.toggle('d-none', mode !== 'create');
    var editorRow = document.getElementById('dest-editor-tbody');
    if (editorRow) editorRow.classList.toggle('d-none', mode !== 'create');
    var nameRow = document.getElementById('row-name-create');
    if (nameRow) nameRow.classList.toggle('d-none', mode !== 'create');
    // Require name only in create mode so pattern validation blocks submit
    var nameInput = document.getElementById('name');
    if (nameInput) nameInput.required = (mode === 'create');
    // Require toDestination only in copy mode
    var toDestInput = document.getElementById('toDestination');
    if (toDestInput) toDestInput.required = (mode === 'copy');
    // Require fromDestination (source) only in copy mode
    var fromDestInput = document.getElementById('fromDestination');
    if (fromDestInput) fromDestInput.required = (mode === 'copy');
    // Update the hidden actionRequested field for form submission
    var ar = document.getElementById('actionRequested');
    if (ar) ar.value = mode;
}

// Live-filter a <select> listbox from a search input.
// Stores all original options on first call, rebuilds on each keystroke.
function filterSelect(inp, selId) {
    var sel = document.getElementById(selId);
    if (!sel) return;
    if (!sel._allOptions) {
        sel._allOptions = Array.from(sel.options).map(function(o) {
            return { v: o.value, t: o.text };
        });
    }
    var q = inp.value.toLowerCase();
    var prev = sel.value;
    sel.innerHTML = '';
    sel._allOptions.forEach(function(o) {
        if (!q || o.t.toLowerCase().indexOf(q) !== -1) {
            var opt = document.createElement('option');
            opt.value = o.v;
            opt.text = o.t;
            if (o.v === prev) opt.selected = true;
            sel.appendChild(opt);
        }
    });
    // Auto-select first option if nothing is selected
    if (sel.options.length > 0 && sel.selectedIndex === -1) {
        sel.options[0].selected = true;
    }
}

$(document).ready(function() {
    // Restore scroll position after a DisplayTag sort-link page reload
    var savedScroll = sessionStorage.getItem('scrollY');
    if (savedScroll !== null) {
        sessionStorage.removeItem('scrollY');
        document.documentElement.style.scrollBehavior = 'auto';
        window.scrollTo(0, parseInt(savedScroll, 10));
        document.documentElement.style.scrollBehavior = '';
        document.documentElement.style.visibility = '';
    } else {
        document.documentElement.style.visibility = '';
    }
    $(document).on('click', 'th.sortable a, th.order1 a, th.order2 a', function() {
        sessionStorage.setItem('scrollY', window.scrollY);
    });

    // Receive messages from the testSource() sandbox iframe (ecpds.js)
    window.addEventListener('message', function(e) {
        if (e.data && e.data.type === 'ecpds-sandbox-result') {
            showToast(e.data.message, e.data.level || 'info');
        }
    });

    // Association card: save scroll position before add/remove navigation
    $(document).on('click', '.assoc-card a', function() {
        sessionStorage.setItem('scrollY', window.scrollY);
        setTimeout(function() { sessionStorage.removeItem('scrollY'); }, 3000);
    });

    // Suppress DataTables browser alert() errors - log to console instead.
    $.fn.dataTable.ext.errMode = 'none';
    $(document).on('error.dt', function(e, settings, techNote, message) {
        console.warn('DataTables warning (tn/' + techNote + '):', message);
    });

    // Apply Bootstrap table classes to all listing tables
    $('table.listing').addClass('table table-sm table-hover table-bordered');
    // Apply DataTables only to tables NOT already managed by DisplayTag.
    // DisplayTag signals its presence via:
    //   .pagebanner   - server-side paging wrapper
    //   th.sortable / th.sorted - sortable column headers
    //   tr.empty      - "Nothing found" colspan row (empty list)
    //   <caption>     - explicit caption element (belt-and-suspenders)
    // Tables that should never use DataTables can add class="no-dt".
    $('table.listing').each(function() {
        var $t = $(this);
        if ($t.hasClass('no-dt')) return;
        var hasDisplayTagPaging   = $t.closest('div, section, td').find('.pagebanner').length > 0;
        var hasDisplayTagSorting  = $t.find('th.sortable, th.sorted').length > 0;
        var hasDisplayTagCaption  = $t.children('caption').length > 0;
        var hasDisplayTagEmpty    = $t.find('tr.empty').length > 0;
        if (!hasDisplayTagPaging && !hasDisplayTagSorting && !hasDisplayTagCaption && !hasDisplayTagEmpty) {
            try {
                $t.DataTable({
                    paging:    false,
                    searching: false,
                    ordering:  true,
                    info:      false
                });
            } catch(e) {
                console.warn('DataTables init failed for table:', $t.attr('id'), e);
            }
        } else {
            $t.addClass('table-striped');
        }
    });
    // Bootstrap tooltip initialisation for elements using data-bs-toggle="tooltip"
    $('[data-bs-toggle="tooltip"]').each(function() {
        new bootstrap.Tooltip(this);
    });
    // Bootstrap popover initialisation for elements using data-bs-toggle="popover"
    $('[data-bs-toggle="popover"]').each(function() {
        new bootstrap.Popover(this, { trigger: 'click', html: false });
    });
    // Dismiss any open popover when clicking elsewhere
    $(document).on('click', function(e) {
        if (!$(e.target).closest('[data-bs-toggle="popover"]').length) {
            $('[data-bs-toggle="popover"]').each(function() {
                bootstrap.Popover.getInstance(this) && bootstrap.Popover.getInstance(this).hide();
            });
        }
    });

    // Unified icon replacement: all /assets/icons/ images - Bootstrap Icons.
    // Order matters - more specific patterns must come before generic ones.
    // Each entry: [srcFragment, bootstrapIconClass, colorClass]
    // colorClass is used for action icons outside the sidebar.
    var iconMap = [
        // ECPDS action icons
        ['curve_arrow2',       'bi-lightning-charge-fill',     'text-warning'],
        ['curve_arrow',        'bi-arrow-clockwise',           'text-primary'],
        ['stop2',              'bi-pause-circle',              'text-secondary'],
        ['stop',               'bi-stop-circle-fill',          'text-danger'],
        ['arrow_up',           'bi-arrow-up-circle',           'text-success'],
        ['arrow_down',         'bi-arrow-down-circle',         'text-warning'],
        ['red_up',             'bi-arrow-up',                  'text-secondary'],
        ['red_down',           'bi-arrow-down',                'text-secondary'],
        ['duplicate',          'bi-copy',                      'text-secondary'],
        ['extend_lifetime',    'bi-clock-history',             'text-info'],
        ['messagebox_warning', 'bi-exclamation-triangle-fill', 'text-warning'],
        ['window_new',         'bi-eraser',                    'text-secondary'],
        ['ktorrent',           'bi-download',                  'text-primary'],
        ['favorites',          'bi-star',                      'text-warning'],
        ['timespan',           'bi-clock-history',             'text-secondary'],
        ['timeline',           'bi-bar-chart-steps',           'text-secondary'],
        ['chart_off',          'bi-bar-chart',                 'text-muted'],
        ['chart',              'bi-bar-chart',                 'text-primary'],
        ['somebody',           'bi-person',                    'text-secondary'],
        ['anonymous',          'bi-person-dash',               'text-muted'],
        ['today',              'bi-calendar-check',            'text-primary'],
        ['mail',               'bi-envelope',                  'text-primary'],
        // KDE webapp icons - specific patterns first
        ['view_text',          'bi-file-text',                 'text-secondary'],
        ['monitor_ok',         'bi-check-circle-fill',         'text-success'],
        ['button_ok',          'bi-check-circle-fill',         'text-success'],
        ['monitor_error',      'bi-x-circle-fill',             'text-danger'],
        ['button_cancel',      'bi-x-circle',                  'text-secondary'],
        ['delete',             'bi-trash',                     'text-danger'],
        ['trash',              'bi-trash-fill',                'text-danger'],
        ['new',                'bi-plus-circle',               'text-success'],
        ['editcopy',           'bi-files',                     'text-secondary'],
        ['update',             'bi-pencil',                    'text-primary'],
        ['search',             'bi-search',                    'text-secondary'],
        ['configure',          'bi-gear-fill',                 'text-secondary'],
        ['undo',               'bi-arrow-counterclockwise',    'text-secondary'],
        ['warning',            'bi-exclamation-triangle-fill', 'text-warning'],
        ['help',               'bi-question-circle',           'text-info'],
        ['konqueror',          'bi-speedometer2',              'text-secondary'],
        ['book',               'bi-journal-text',              'text-secondary'],
        ['juk',                'bi-paperclip',                 'text-secondary'],
        ['square_small',       'bi-square',                    'text-muted'],
        ['url2',               'bi-link-45deg',                'text-primary'],
        ['left_small',         'bi-arrow-left',                'text-secondary'],
        ['right_small',        'bi-arrow-right',               'text-secondary'],
        ['left',               'bi-arrow-left',                'text-secondary'],
        ['right',              'bi-arrow-right',               'text-secondary'],
        ['up',                 'bi-arrow-up',                  'text-secondary'],
        ['down',               'bi-arrow-down',                'text-secondary']
    ];

    $('img[src*="/assets/icons/"]').each(function() {
        var $img = $(this);
        var src = $img.attr('src') || '';
        var tooltip = $img.attr('title') || $img.attr('alt') || '';
        var titleAttr = tooltip ? ' title="' + $('<div>').text(tooltip).html() + '"' : '';
        var onclick = $img.attr('onclick') || '';
        var onclickAttr = onclick ? ' onclick="' + onclick.replace(/"/g, '&quot;') + '" style="cursor:pointer"' : '';
        var inSidebar = $img.closest('#sidebarMenu').length > 0;
        for (var i = 0; i < iconMap.length; i++) {
            if (src.indexOf(iconMap[i][0]) !== -1) {
                var cls = inSidebar
                    ? 'bi ' + iconMap[i][1] + ' sidebar-icon'
                    : 'bi ' + iconMap[i][1] + ' action-icon ' + iconMap[i][2];
                $img.replaceWith('<i class="' + cls + '"' + titleAttr + onclickAttr + '></i>');
                break;
            }
        }
    });
});

function validateMailInput(input) {
    var fb = document.getElementById('userMailFeedback');
    if (!fb) return;
    var val = input.value.trim();
    if (val === '') {
        fb.innerHTML = '';
    } else if (input.validity.valid) {
        fb.innerHTML = '<i class="bi bi-check-circle-fill text-success" title="Valid email address"></i>';
    } else {
        fb.innerHTML = '<i class="bi bi-x-circle-fill text-danger" title="Invalid email address"></i>';
    }
}

function validatePatternInput(input, feedbackId) {
    var fb = document.getElementById(feedbackId);
    if (!fb) return;
    var val = input.value.trim();
    if (val === '') {
        fb.innerHTML = '';
    } else if (input.validity.valid) {
        fb.innerHTML = '<i class="bi bi-check-circle-fill text-success"></i>';
    } else {
        fb.innerHTML = '<i class="bi bi-x-circle-fill text-danger" title="' + (input.title || 'Invalid value') + '"></i>';
    }
}

function isValidIPv4(val) {
    var parts = val.split('.');
    if (parts.length !== 4) return false;
    return parts.every(function(p) {
        if (!/^\d+$/.test(p)) return false;
        var n = parseInt(p, 10);
        return n >= 0 && n <= 255 && p === String(n);
    });
}

function isValidIPv6(val) {
    var re = /^(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]+|::(ffff(:0{1,4})?:)?((25[0-5]|(2[0-4]|1?\d)?\d)\.){3}(25[0-5]|(2[0-4]|1?\d)?\d)|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1?\d)?\d)\.){3}(25[0-5]|(2[0-4]|1?\d)?\d))$/;
    return re.test(val);
}

function isValidHostname(val) {
    if (val.endsWith('.') || val.length > 253) return false;
    return val.split('.').every(function(label) {
        return label.length >= 1 && label.length <= 63 &&
               /^[A-Za-z0-9]([A-Za-z0-9\-]*[A-Za-z0-9])?$/.test(label);
    });
}

function validateHostInput(input) {
    var fb = document.getElementById('hostFeedback');
    if (!fb) return;
    var val = input.value.trim();
    if (val === '') { fb.innerHTML = ''; return; }
    var valid = isValidIPv4(val) || isValidIPv6(val) || isValidHostname(val);
    fb.innerHTML = valid
        ? '<i class="bi bi-check-circle-fill text-success"></i>'
        : '<i class="bi bi-x-circle-fill text-danger" title="Not a valid hostname, IPv4 or IPv6 address"></i>';
}
</script>

<link rel="icon" type="image/png" href="/favicon.png">
<link rel="icon" type="image/x-icon" href="/favicon.ico">

</head>