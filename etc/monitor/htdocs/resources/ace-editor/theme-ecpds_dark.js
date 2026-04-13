define("ace/theme/ecpds_dark",["require","exports","module","ace/lib/dom"],function(e,t,n){"use strict";
t.isDark=true;
t.cssClass="ace-ecpds-dark";
t.cssText=`
.ace-ecpds-dark {
  background-color: var(--bs-body-bg, #1e1e2e);
  color: var(--bs-body-color, #cdd6f4);
}
.ace-ecpds-dark .ace_gutter {
  background: var(--bs-tertiary-bg, #181825);
  color: var(--bs-secondary-color, #6c7086);
  border-right: 1px solid var(--bs-border-color, #313244);
}
.ace-ecpds-dark .ace_gutter-active-line { background-color: var(--bs-secondary-bg, #313244); }
.ace-ecpds-dark .ace_active-line { background: var(--bs-secondary-bg, #313244); }
.ace-ecpds-dark .ace_cursor { color: var(--bs-body-color, #cdd6f4); }
.ace-ecpds-dark .ace_marker-layer .ace_selection { background: rgba(99,130,190,0.35); }
.ace-ecpds-dark .ace_marker-layer .ace_bracket { margin: -1px 0 0 -1px; border: 1px solid var(--bs-border-color, #585b70); }
.ace-ecpds-dark .ace_marker-layer .ace_selected-word { border: 1px solid rgba(99,130,190,0.5); }
.ace-ecpds-dark .ace_print-margin { width: 1px; background: var(--bs-border-color, #313244); }
.ace-ecpds-dark .ace_invisible { color: var(--bs-secondary-color, #585b70); }
.ace-ecpds-dark .ace_keyword,
.ace-ecpds-dark .ace_storage { color: #cba6f7; }
.ace-ecpds-dark .ace_constant.ace_numeric { color: #fab387; }
.ace-ecpds-dark .ace_constant.ace_buildin { color: #fab387; }
.ace-ecpds-dark .ace_string { color: #a6e3a1; }
.ace-ecpds-dark .ace_comment { color: #6c7086; font-style: italic; }
.ace-ecpds-dark .ace_function { color: #89b4fa; }
.ace-ecpds-dark .ace_variable { color: #cdd6f4; }
.ace-ecpds-dark .ace_tag { color: #89dceb; }
.ace-ecpds-dark .ace_type { color: #cba6f7; }
.ace-ecpds-dark .ace_indent-guide { background: url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAACCAYAAACZgbYnAAAAEklEQVQImWNgYGBg+M/AwAAABAAB/jr4AAAAAABJRU5ErkJggg==") right repeat-y; }
`;
var r=e("../lib/dom");r.importCssString(t.cssText,t.cssClass);
});
