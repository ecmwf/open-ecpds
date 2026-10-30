/* Shared, inclusive date-range filter for daily traffic and rates pages. */
(function() {
  'use strict';

  function iso(date) {
    return date.toISOString().slice(0, 10);
  }

  function month(date, offset) {
    return new Date(Date.UTC(date.getUTCFullYear(), date.getUTCMonth() + offset, 1));
  }

  function parse(value) {
    return new Date(value + 'T00:00:00Z');
  }

  function monthLabel(date) {
    return date.toLocaleDateString('en-GB', { month: 'long', year: 'numeric', timeZone: 'UTC' });
  }

  function pretty(value) {
    return parse(value).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric', timeZone: 'UTC' });
  }

  window.EcpdsDateRange = function(root, options) {
    var self = this;
    var first, last, visibleMonth, draftStart, draftEnd, choosingEnd = false;
    this.start = '';
    this.end = '';
    root.className = 'dropdown ecpds-date-range';
    root.innerHTML =
      '<button type="button" class="btn btn-sm btn-outline-secondary dropdown-toggle" data-bs-toggle="dropdown"'
      + ' data-bs-auto-close="outside" aria-expanded="false" aria-controls="ecpdsRangeMenu" title="Choose dates within the selected period">'
      + '<i class="bi bi-calendar-range me-1" aria-hidden="true"></i><span>Custom range</span></button>'
      + '<div class="dropdown-menu ecpds-range-menu p-3 shadow" id="ecpdsRangeMenu" aria-label="Custom date range">'
      + '<div class="d-flex align-items-center justify-content-between gap-2 mb-2">'
      + '<strong class="small">Select date range</strong><button type="button" class="btn-close" aria-label="Close date range"></button></div>'
      + '<div class="ecpds-range-selection small rounded px-2 py-2 mb-2" role="status" aria-live="polite"></div>'
      + '<div class="d-flex align-items-center justify-content-between gap-2 mb-2">'
      + '<button type="button" class="btn btn-sm btn-outline-secondary ecpds-range-prev" aria-label="Previous month">'
      + '<i class="bi bi-chevron-left" aria-hidden="true"></i></button>'
      + '<select class="form-select form-select-sm w-auto" aria-label="Calendar month"></select>'
      + '<button type="button" class="btn btn-sm btn-outline-secondary ecpds-range-next" aria-label="Next month">'
      + '<i class="bi bi-chevron-right" aria-hidden="true"></i></button></div>'
      + '<div class="ecpds-range-calendars"></div>'
      + '<p class="small text-muted mt-2 mb-2">Choose a start and end date; click the same day twice for one day. '
      + 'Both dates are included. Dates outside the selected Period are disabled.</p>'
      + '<div class="d-flex justify-content-between align-items-center border-top pt-2">'
      + '<button type="button" class="btn btn-sm btn-outline-secondary ecpds-range-reset">Reset to full period</button>'
      + '<button type="button" class="btn btn-sm btn-primary ecpds-range-apply">Apply</button></div></div>';

    var toggle = root.querySelector('[data-bs-toggle]');
    var menu = root.querySelector('.dropdown-menu');
    var calendars = root.querySelector('.ecpds-range-calendars');
    var selection = root.querySelector('.ecpds-range-selection');
    var selector = root.querySelector('select');
    var apply = root.querySelector('.ecpds-range-apply');
    var prev = root.querySelector('.ecpds-range-prev');
    var next = root.querySelector('.ecpds-range-next');

    this.matches = function(value) {
      return !self.start || (value >= self.start && value <= self.end);
    };

    function close() {
      bootstrap.Dropdown.getOrCreateInstance(toggle).hide();
      toggle.focus();
    }

    this.reset = function(notify) {
      self.start = self.end = '';
      toggle.querySelector('span').textContent = 'Custom range';
      toggle.classList.remove('active');
      if (menu.classList.contains('show')) close();
      if (notify !== false) options.onChange();
    };

    function highlight(hover) {
      var end = draftEnd || hover || draftStart;
      var low = draftStart < end ? draftStart : end;
      var high = draftStart > end ? draftStart : end;
      calendars.querySelectorAll('[data-date]').forEach(function(button) {
        var date = button.dataset.date;
        var endpoint = date === draftStart || date === draftEnd;
        button.classList.toggle('ecpds-range-edge', endpoint);
        button.classList.toggle('ecpds-range-between', !!draftStart && date >= low && date <= high && !endpoint);
        button.setAttribute('aria-pressed', String(endpoint || (!!draftStart && date >= low && date <= high)));
      });
    }

    function render() {
      selector.value = iso(visibleMonth).slice(0, 7);
      prev.disabled = visibleMonth <= month(parse(first), 0);
      next.disabled = visibleMonth >= month(parse(last), 0);
      selection.textContent = 'From: ' + pretty(draftStart) + '  \u2192  To: '
        + (draftEnd ? pretty(draftEnd) : 'Choose end date');
      apply.disabled = !draftEnd;
      calendars.innerHTML = '';
      for (var side = 0; side < 2; side++) {
        var current = month(visibleMonth, side);
        var heading = monthLabel(current);
        var panel = document.createElement('div');
        panel.className = 'ecpds-range-calendar';
        panel.innerHTML = '<div class="text-center small fw-semibold mb-2">' + heading + '</div>'
          + '<table class="w-100 text-center" aria-label="' + heading + '">'
          + '<thead><tr>' + ['Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa', 'Su'].map(function(day) {
            return '<th scope="col" class="small text-muted fw-normal pb-1">' + day + '</th>';
          }).join('') + '</tr></thead><tbody></tbody></table>';
        var body = panel.querySelector('tbody');
        var leading = (current.getUTCDay() + 6) % 7;
        var length = month(current, 1);
        length.setUTCDate(0);
        var total = length.getUTCDate();
        for (var cell = 0; cell < Math.ceil((leading + total) / 7) * 7; cell++) {
          if (cell % 7 === 0) body.appendChild(document.createElement('tr'));
          var td = document.createElement('td');
          var day = cell - leading + 1;
          if (day > 0 && day <= total) {
            var value = iso(new Date(Date.UTC(current.getUTCFullYear(), current.getUTCMonth(), day)));
            var button = document.createElement('button');
            button.type = 'button';
            button.className = 'btn ecpds-range-day';
            button.textContent = day;
            button.dataset.date = value;
            button.disabled = value < first || value > last;
            button.setAttribute('aria-label', pretty(value));
            td.appendChild(button);
          }
          body.lastChild.appendChild(td);
        }
        calendars.appendChild(panel);
      }
      highlight();
    }

    toggle.addEventListener('show.bs.dropdown', function(event) {
      var dates = options.getDates().slice().sort();
      if (!dates.length) {
        event.preventDefault();
        return;
      }
      first = dates[0];
      last = dates[dates.length - 1];
      draftStart = self.start || first;
      draftEnd = self.end || last;
      choosingEnd = false;
      // Start near the most recent dates unless a custom range is already selected.
      visibleMonth = month(parse(self.start || last), self.start ? 0 : -1);
      if (visibleMonth < month(parse(first), 0)) visibleMonth = month(parse(first), 0);
      selector.innerHTML = '';
      for (var m = month(parse(first), 0); m <= month(parse(last), 0); m = month(m, 1)) {
        var option = document.createElement('option');
        option.value = iso(m).slice(0, 7);
        option.textContent = monthLabel(m);
        selector.appendChild(option);
      }
      render();
    });

    prev.addEventListener('click', function() { visibleMonth = month(visibleMonth, -1); render(); });
    next.addEventListener('click', function() { visibleMonth = month(visibleMonth, 1); render(); });
    selector.addEventListener('change', function() { visibleMonth = parse(selector.value + '-01'); render(); });
    calendars.addEventListener('click', function(event) {
      var button = event.target.closest('[data-date]');
      if (!button || button.disabled) return;
      var value = button.dataset.date;
      if (!choosingEnd) {
        draftStart = value;
        draftEnd = '';
      } else {
        draftEnd = value < draftStart ? draftStart : value;
        if (value < draftStart) draftStart = value;
      }
      choosingEnd = !choosingEnd;
      render();
      calendars.querySelector('[data-date="' + value + '"]').focus();
    });
    calendars.addEventListener('mouseover', function(event) {
      var button = event.target.closest('[data-date]');
      if (choosingEnd && button && !button.disabled) highlight(button.dataset.date);
    });
    calendars.addEventListener('mouseleave', function() { highlight(); });
    calendars.addEventListener('keydown', function(event) {
      var button = event.target.closest('[data-date]');
      var offsets = { ArrowLeft: -1, ArrowRight: 1, ArrowUp: -7, ArrowDown: 7 };
      if (!button || offsets[event.key] === undefined) return;
      event.preventDefault();
      event.stopPropagation();
      var target = parse(button.dataset.date);
      target.setUTCDate(target.getUTCDate() + offsets[event.key]);
      var value = iso(target);
      if (value < first || value > last) return;
      if (!calendars.querySelector('[data-date="' + value + '"]')) {
        visibleMonth = month(target, 0);
        render();
      }
      calendars.querySelector('[data-date="' + value + '"]').focus();
    });
    root.querySelector('.btn-close').addEventListener('click', close);
    root.querySelector('.ecpds-range-reset').addEventListener('click', function() { self.reset(); });
    apply.addEventListener('click', function() {
      if (!draftEnd) return;
      self.start = draftStart;
      self.end = draftEnd;
      toggle.querySelector('span').textContent = pretty(self.start)
        + (self.start === self.end ? '' : ' \u2013 ' + pretty(self.end));
      toggle.classList.add('active');
      close();
      options.onChange();
    });
  };
})();
