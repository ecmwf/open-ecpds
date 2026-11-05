<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<div class="d-flex align-items-center gap-2 mb-3 px-3 py-2 rounded"
     style="background:rgba(108,117,125,0.06); color:var(--bs-body-color); border-left:4px solid #6c757d;">
  <i class="bi bi-chat-quote text-secondary flex-shrink-0"></i>
  <span>Short phrases and documentation articles beneath the Monitor and Data Portal login forms.
  Maintenance notices are managed separately.</span>
  <button type="button" class="btn btn-sm btn-outline-info ms-auto flex-shrink-0"
          data-bs-toggle="offcanvas" data-bs-target="#loginContentGuide" aria-controls="loginContentGuide"
          title="Open Login Page Content Guide" aria-label="Open Login Page Content Guide">
    <i class="bi bi-book"></i><span class="d-none d-sm-inline ms-1">Login Page Content Guide</span>
  </button>
</div>
<c:if test="${not empty loginContentError}"><div class="alert alert-danger"><c:out value="${loginContentError}"/></div></c:if>
<c:if test="${not empty loginContentSuccess}">
<div class="alert alert-success fade show d-flex align-items-center gap-2 mb-4" role="alert" id="login-content-success">
  <i class="bi bi-check-circle-fill flex-shrink-0"></i>
  <span><c:out value="${loginContentSuccess}"/></span>
  <button type="button" class="btn-close ms-auto" data-bs-dismiss="alert" aria-label="Close"></button>
</div>
<script>
setTimeout(function(){
  var alert=document.getElementById('login-content-success');
  if(alert)bootstrap.Alert.getOrCreateInstance(alert).close();
},4000);
</script>
</c:if>
<div class="card border-0 shadow-sm mt-3">
  <div class="card-header d-flex flex-wrap align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-chat-quote text-primary"></i>
    <span class="fw-semibold">Login Page Content</span>
    <div class="ms-auto d-flex flex-wrap align-items-center gap-2">
      <div class="input-group input-group-sm" style="width:auto">
        <span class="input-group-text"><i class="bi bi-search"></i></span>
        <input id="login-content-search" class="form-control" placeholder="Search..." aria-label="Search login messages">
      </div>
      <select id="login-content-page-size" class="form-select form-select-sm" style="width:auto" aria-label="Page size">
        <option value="10">10</option><option value="25" selected>25</option><option value="50">50</option>
      </select>
      <button type="button" class="btn btn-sm btn-outline-success" id="login-content-add">
        <i class="bi bi-plus-circle"></i> Create
      </button>
    </div>
  </div>
  <div class="card-body">
<div class="alert alert-info d-flex align-items-start gap-2" role="alert">
  <i class="bi bi-info-circle flex-shrink-0 mt-1"></i>
  <div><strong>This content is public.</strong> Do not include credentials, internal operational details or personal information.
  Plain text or restricted Markdown and links within the OpenECPDS documentation site are accepted.</div>
</div>
<form id="login-content-editor" method="post" action="<bean:message key="admin.basepath"/>/logincontent">
  <input type="hidden" name="csrf" value="<c:out value="${loginContentCsrf}"/>">
  <label class="form-check mb-3">
    <input class="form-check-input" type="checkbox" name="enabled" value="true" <c:if test="${loginContent.enabled}">checked</c:if>>
    <span class="form-check-label">Enable login page content (including the About OpenECPDS link)</span>
  </label>
  <div id="login-content-rows" hidden>
    <c:forEach var="message" items="${loginContentMessages}">
      <div class="login-content-row border rounded p-3 mb-3">
        <label class="d-block mb-2">Phrase
          <textarea class="form-control" name="phrase" maxlength="160"><c:out value="${message.text}"/></textarea>
        </label>
        <div class="row g-2">
          <label class="col-md-6">Documentation article
            <input class="form-control" name="article" list="login-articles" maxlength="200" placeholder="No article"
                   value="<c:out value="${message.article}"/>">
          </label>
          <label class="col-md-6">Link label
            <input class="form-control" name="label" maxlength="80" value="<c:out value="${message.label}"/>">
          </label>
          <label class="col-md-6">Show on
            <select class="form-select" name="audience">
              <option value="both" <c:if test="${message.audience == 'both'}">selected</c:if>>Both login pages</option>
              <option value="monitor" <c:if test="${message.audience == 'monitor'}">selected</c:if>>Monitor</option>
              <option value="portal" <c:if test="${message.audience == 'portal'}">selected</c:if>>Data Portal</option>
            </select>
          </label>
          <label class="col-md-6">Visibility
            <select class="form-select" name="visible">
              <option value="true" <c:if test="${message.enabled}">selected</c:if>>Enabled</option>
              <option value="false" <c:if test="${not message.enabled}">selected</c:if>>Disabled</option>
            </select>
          </label>
        </div>
        <select name="markdown">
          <option value="false" <c:if test="${not message.markdown}">selected</c:if>>Plain text</option>
          <option value="true" <c:if test="${message.markdown}">selected</c:if>>Restricted Markdown</option>
        </select>
        <div class="mt-2 d-flex gap-2">
          <button type="button" class="btn btn-sm btn-outline-secondary" data-row-op="up">Move up</button>
          <button type="button" class="btn btn-sm btn-outline-secondary" data-row-op="down">Move down</button>
          <button type="button" class="btn btn-sm btn-outline-danger" data-row-op="remove">Remove</button>
        </div>
      </div>
    </c:forEach>
  </div>
  <div class="table-responsive">
    <table id="login-content-table" class="table table-sm table-hover mb-0 align-middle w-100" style="font-size:0.82rem;">
      <thead class="table-secondary"><tr>
        <th>Order</th><th>Visibility</th><th>Phrase</th><th>Documentation article</th><th>Show on</th><th>Actions</th>
      </tr></thead><tbody></tbody>
    </table>
  </div>
  <datalist id="login-articles">
    <option value="why-openecpds/">Why OpenECPDS?</option>
    <option value="concepts/protocols/">Supported protocols</option>
    <option value="use-cases/data-portal/">Data Portal</option>
    <option value="transfer-modules/">Transfer modules</option>
    <option value="architecture/overview/">Architecture</option>
    <option value="getting-started/standalone/">Try OpenECPDS</option>
    <option value="changelog/">Release notes</option>
    <option value="contributing/">Contributing</option>
  </datalist>
  <p class="form-text mt-3">Up to 50 phrases. Use the arrows to set rotation order.
  Create, edit and remove actions update the draft only; click Save to publish changes.</p>
  <div class="d-flex flex-wrap gap-2 mb-3">
    <button type="submit" name="op" value="save" class="btn btn-primary" id="login-content-save">Save</button>
    <div class="input-group flex-nowrap" style="width:auto">
      <select name="previewAudience" class="form-select" aria-label="Preview login page">
        <option value="monitor">Monitor</option>
        <option value="portal">Data Portal</option>
      </select>
      <button type="submit" name="op" value="preview" formtarget="login-content-preview" class="btn btn-outline-primary">
        <i class="bi bi-eye me-1"></i>Preview
      </button>
    </div>
  </div>
</form>
  </div>
</div>
<div class="card border-0 shadow-sm mt-3">
  <div class="card-header d-flex align-items-center gap-2" style="background:var(--bs-secondary-bg)">
    <i class="bi bi-eye text-primary"></i><span class="fw-semibold">Preview</span>
  </div>
  <div class="card-body">
<iframe name="login-content-preview" title="Login content preview" class="border rounded w-100" style="height:220px;"></iframe>
  </div>
</div>
<div class="offcanvas offcanvas-end" tabindex="-1" id="loginContentGuide"
     aria-labelledby="loginContentGuideLabel" style="width:760px;max-width:95vw;">
  <div class="offcanvas-header border-bottom py-2 px-3">
    <h6 class="offcanvas-title mb-0 fw-semibold" id="loginContentGuideLabel">
      <i class="bi bi-book me-2 text-info"></i>Login Page Content Guide
    </h6>
    <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
  </div>
  <div class="offcanvas-body p-3" style="overflow-y:auto;font-size:0.85rem;">
    <div class="alert alert-info py-2 px-3 mb-3 small">Login messages are public promotional content,
    not maintenance notices. Never include private or internal-only information.</div>
    <h6 class="fw-semibold">Editing and saving</h6>
    <p>Use <strong>Create</strong> or the row's <strong>Edit</strong> button to open the message dialog.
    <strong>Apply to draft</strong> updates the table; Cancel leaves it unchanged.
    Use the arrows to set rotation order and <strong>Remove</strong> to delete a draft entry.
    Search and page size help browse the list without changing its order. Up to 50 entries are supported.
    Changes take effect only after <strong>Save</strong>.</p>
    <p>Unsaved changes highlight <strong>Save</strong>, with an explanatory tooltip. Leaving, reloading or closing
    the page warns before losing changes, including edits still open in the dialog.
    Preview does not save or clear this warning. The save confirmation dismisses after four seconds.</p>
    <div class="table-responsive mb-3">
      <table class="table table-sm table-bordered small mb-0">
        <thead class="table-light"><tr><th>Field</th><th>Meaning</th></tr></thead>
        <tbody>
          <tr><td>Phrase</td><td>Up to 160 characters, in plain text (default) or restricted Markdown.
          Markdown supports emphasis, lists and documentation links. No headings, tables, images or embedded HTML.</td></tr>
          <tr><td>Documentation article</td><td>Optional documentation-relative path ending in <code>/</code>,
          such as <code>why-openecpds/</code>. External URLs, queries and fragments are not accepted.</td></tr>
          <tr><td>Link label</td><td>Text for the article link, up to 80 characters.</td></tr>
          <tr><td>Show on</td><td>Monitor, Data Portal or both login pages.</td></tr>
          <tr><td>Visibility</td><td>Disable an entry without removing it.</td></tr>
        </tbody>
      </table>
    </div>
    <style>
    #login-content-modal select.form-select { width:100%; }
    #login-content-table .login-content-phrase { max-width:380px; overflow:hidden; white-space:nowrap; text-overflow:ellipsis; }
    </style>
    <h6 class="fw-semibold">Preview and visibility</h6>
    <p>The Create/Edit dialog includes a live single-message preview, including Markdown and the article link.
    Disabled entries can be inspected as samples without enabling them. Nothing is saved by previewing.</p>
    <p>Select <strong>Monitor</strong> or <strong>Data Portal</strong> beside the bottom <strong>Preview</strong> to display unsaved
    content below the editor in the current theme. Preview does not save changes.</p>
    <p>Messages rotate every 8 seconds. Users can pause them; reduced-motion preferences start rotation
    paused. Disabling <strong>Enable login page content</strong> hides the entire area, including
    the About OpenECPDS link. Saved changes appear on new login page requests without a restart.</p>
    <a href="https://ecmwf.github.io/open-ecpds/administration/login-page-content/"
       target="_blank" rel="noopener noreferrer"><i class="bi bi-box-arrow-up-right me-1"></i>Full configuration documentation</a>
  </div>
</div>
<div class="modal fade" id="login-content-modal" tabindex="-1" aria-labelledby="login-content-modal-title" aria-hidden="true">
  <div class="modal-dialog modal-lg"><div class="modal-content">
    <div class="modal-header">
      <h5 class="modal-title" id="login-content-modal-title">Create login message</h5>
      <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
    </div>
    <form id="login-content-message-form">
      <div class="modal-body">
        <div id="login-content-modal-fields"></div>
        <div class="small fw-semibold mb-2">Preview <span class="fw-normal text-muted">(single message, not saved)</span></div>
        <div id="login-content-entry-status" class="small text-muted mb-2" role="status"></div>
        <iframe id="login-content-entry-preview" title="Single login message preview"
                class="border rounded w-100" style="height:180px;" hidden></iframe>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancel</button>
        <button type="submit" class="btn btn-primary">Apply to draft</button>
      </div>
    </form>
  </div></div>
</div>
<template id="login-content-template">
  <div class="login-content-row border rounded p-3 mb-3">
    <label class="d-block mb-2">Phrase <textarea class="form-control" name="phrase" maxlength="160" rows="3" required></textarea></label>
    <label class="d-block mb-2">Format
      <select class="form-select" name="markdown"><option value="false">Plain text</option><option value="true">Restricted Markdown</option></select>
    </label>
    <div class="form-text mb-2">Markdown supports emphasis, lists and documentation links only; no embedded HTML.</div>
    <div class="row g-2">
      <label class="col-md-6">Documentation article <input class="form-control" name="article" list="login-articles" maxlength="200" placeholder="No article"></label>
      <label class="col-md-6">Link label <input class="form-control" name="label" maxlength="80"></label>
      <label class="col-md-6">Show on <select class="form-select" name="audience"><option value="both">Both login pages</option><option value="monitor">Monitor</option><option value="portal">Data Portal</option></select></label>
      <label class="col-md-6">Visibility <select class="form-select" name="visible"><option value="true">Enabled</option><option value="false">Disabled</option></select></label>
    </div>
    <div class="mt-2 d-flex gap-2">
      <button type="button" class="btn btn-sm btn-outline-secondary" data-row-op="up">Move up</button>
      <button type="button" class="btn btn-sm btn-outline-secondary" data-row-op="down">Move down</button>
      <button type="button" class="btn btn-sm btn-outline-danger" data-row-op="remove">Remove</button>
    </div>
  </div>
</template>
<script>
(function(){
  var rows=document.getElementById('login-content-rows'),add=document.getElementById('login-content-add');
  var modal=bootstrap.Modal.getOrCreateInstance(document.getElementById('login-content-modal'));
  var fields=document.getElementById('login-content-modal-fields'),editing=null;
  var entryPreview=document.getElementById('login-content-entry-preview');
  var entryStatus=document.getElementById('login-content-entry-status');
  var previewTimer=null,previewVersion=0;
  var names=['phrase','article','label','audience','visible','markdown'];
  var editor=document.getElementById('login-content-editor');
  var savedSnapshot='',dialogSnapshot='',dialogOpen=false,leaving=false;
  function collect(container){
    return names.map(function(name){return container.querySelector('[name='+name+']').value;});
  }
  function snapshot(){
    return JSON.stringify({enabled:editor.querySelector('[name=enabled]').checked,
      messages:Array.from(rows.children).map(collect)});
  }
  function hasUnsaved(){
    return snapshot()!==savedSnapshot || (dialogOpen&&JSON.stringify(collect(fields))!==dialogSnapshot);
  }
  function updateDirty(){
    var dirty=snapshot()!==savedSnapshot;
    var button=document.getElementById('login-content-save');
    button.classList.toggle('btn-warning',dirty);
    button.classList.toggle('btn-primary',!dirty);
    button.title=dirty?'You have unsaved changes':'';
  }
  window.addEventListener('beforeunload',function(event){
    if(!leaving&&hasUnsaved()){event.preventDefault();event.returnValue='';}
  });
  document.addEventListener('click',function(event){
    if(leaving||!hasUnsaved())return;
    var anchor=event.target.closest('a[href]');if(!anchor)return;
    var href=anchor.getAttribute('href');
    if(!href||href==='#'||href.startsWith('javascript:')||href.startsWith('mailto:')
      ||anchor.target==='_blank'||anchor.hasAttribute('download'))return;
    event.preventDefault();event.stopImmediatePropagation();
    confirmationDialog({
      title:'<i class="bi bi-exclamation-triangle-fill text-warning me-2"></i>Unsaved Changes',
      message:'You have unsaved login content changes. If you leave now, your changes will be lost.',
      confirmText:'Leave without saving',cancelText:'Stay and save',showLoading:false,
      onConfirm:function(){leaving=true;window.location.href=href;}
    });
  },true);
  editor.addEventListener('change',updateDirty);
  editor.addEventListener('submit',function(event){
    if(event.submitter&&event.submitter.value==='save')leaving=true;
  });
  var escape=DataTable.render.text().display;
  var table=new DataTable('#login-content-table',{
    ordering:false,pageLength:25,layout:{topStart:null,topEnd:null,bottomStart:'info',bottomEnd:'paging'},
    columns:[
      {data:'order'},
      {data:'visible',render:function(value){return value==='true'
        ? '<span class="badge bg-success">Enabled</span>' : '<span class="badge bg-secondary">Disabled</span>';}},
      {data:'phrase',render:function(value,type){return type==='display'
        ? '<div class="login-content-phrase" title="'+escape(value)+'">'+escape(value)+'</div>' : value;}},
      {data:'article',render:function(value,type,row){return type==='display'
        ? escape(value)+(row.label?'<div class="small text-muted">'+escape(row.label)+'</div>':'') : value+' '+row.label;}},
      {data:'audience',render:function(value){return value==='both'?'Both login pages':value==='portal'?'Data Portal':'Monitor';}},
      {data:'order',searchable:false,render:function(value){
        var index=value-1;
        return '<div class="d-flex gap-1">'+
          '<button type="button" class="btn btn-sm btn-outline-primary" data-message-op="edit" data-index="'+index+'" title="Edit" aria-label="Edit message"><i class="bi bi-pencil-fill"></i></button>'+
          '<button type="button" class="btn btn-sm btn-outline-secondary" data-message-op="up" data-index="'+index+'" title="Move up" aria-label="Move message up" '+(index===0?'disabled':'')+'><i class="bi bi-arrow-up"></i></button>'+
          '<button type="button" class="btn btn-sm btn-outline-secondary" data-message-op="down" data-index="'+index+'" title="Move down" aria-label="Move message down" '+(value===rows.children.length?'disabled':'')+'><i class="bi bi-arrow-down"></i></button>'+
          '<button type="button" class="btn btn-sm btn-outline-danger" data-message-op="remove" data-index="'+index+'" title="Remove" aria-label="Remove message"><i class="bi bi-trash-fill"></i></button></div>';
      }}
    ]
  });
  function update(){add.disabled=rows.children.length>=50;}
  function previewEntry(){
    clearTimeout(previewTimer);
    var version=++previewVersion;
    entryPreview.hidden=true;
    entryStatus.textContent='Updating preview...';
    if(!fields.querySelector('[name=phrase]').value.trim()){
      entryStatus.textContent='Enter a phrase to preview it.';
      return;
    }
    previewTimer=setTimeout(function(){
      var editor=document.getElementById('login-content-editor');
      var data=new URLSearchParams();
      data.set('csrf',editor.querySelector('[name=csrf]').value);
      data.set('enabled','true');data.set('op','preview');
      names.forEach(function(name){data.set(name,fields.querySelector('[name='+name+']').value);});
      data.set('visible','true');
      data.set('previewAudience',data.get('audience')==='portal'?'portal':'monitor');
      fetch(editor.action,{method:'POST',body:data,headers:{'X-Requested-With':'XMLHttpRequest'}})
        .then(function(response){if(!response.ok)throw new Error();return response.text();})
        .then(function(html){
          if(version!==previewVersion)return;
          if(!html.includes('<title>Login content preview</title>'))throw new Error();
          entryPreview.srcdoc=html;entryPreview.hidden=false;
          entryStatus.textContent=fields.querySelector('[name=visible]').value==='false'
            ? 'Sample only: this message is disabled and will not appear on the login pages.'
            : 'Sample only; use the bottom preview to check audience filtering and rotation.';
        }).catch(function(){
          if(version!==previewVersion)return;
          entryStatus.textContent='Preview unavailable. Check the phrase, article path and link label. Nothing has been saved.';
        });
    },250);
  }
  function redraw(){
    var data=Array.from(rows.children).map(function(row,index){
      var message={order:index+1};
      names.forEach(function(name){message[name]=row.querySelector('[name='+name+']').value;});
      return message;
    });
    table.clear();table.rows.add(data);table.draw(false);update();updateDirty();
  }
  function open(row){
    editing=row;
    fields.replaceChildren(document.getElementById('login-content-template').content.cloneNode(true));
    fields.querySelector('[data-row-op]').parentElement.remove();
    names.forEach(function(name){
      var input=fields.querySelector('[name='+name+']');
      if(row)input.value=row.querySelector('[name='+name+']').value;
    });
    document.getElementById('login-content-modal-title').textContent=row?'Edit login message':'Create login message';
    dialogSnapshot=JSON.stringify(collect(fields));dialogOpen=true;
    modal.show();
    previewEntry();
  }
  document.getElementById('login-content-modal').addEventListener('shown.bs.modal',function(){
    fields.querySelector('[name=phrase]').focus();
  });
  add.addEventListener('click',function(){
    if(rows.children.length>=50)return;
    open(null);
  });
  document.getElementById('login-content-message-form').addEventListener('submit',function(event){
    event.preventDefault();
    var phrase=fields.querySelector('[name=phrase]');
    phrase.setCustomValidity(phrase.value.trim()?'':'Enter a phrase');
    if(!this.reportValidity())return;
    if(!editing&&rows.children.length>=50)return;
    if(!editing){
      rows.appendChild(document.getElementById('login-content-template').content.cloneNode(true));
      editing=rows.lastElementChild;
      editing.querySelector('[name=phrase]').removeAttribute('required');
    }
    names.forEach(function(name){editing.querySelector('[name='+name+']').value=fields.querySelector('[name='+name+']').value.trim();});
    redraw();modal.hide();
  });
  fields.addEventListener('input',function(event){
    event.target.setCustomValidity&&event.target.setCustomValidity('');
    previewEntry();
  });
  document.getElementById('login-content-modal').addEventListener('hidden.bs.modal',function(){
    dialogOpen=false;
    clearTimeout(previewTimer);++previewVersion;
  });
  document.getElementById('login-content-table').addEventListener('click',function(event){
    var button=event.target.closest('[data-message-op]');if(!button)return;
    var row=rows.children[Number(button.dataset.index)],op=button.dataset.messageOp;
    if(op==='edit'){open(row);return;}
    if(op==='remove'){
      confirmationDialog({
        title:'Remove draft message',
        message:'Remove this message from the draft? Click Save to publish the change.',
        allowHtml:false,showLoading:false,confirmText:'Remove',
        onConfirm:function(){row.remove();redraw();add.focus();}
      });
      return;
    }
    if(op==='up'&&row.previousElementSibling)rows.insertBefore(row,row.previousElementSibling);
    if(op==='down'&&row.nextElementSibling)rows.insertBefore(row.nextElementSibling,row);
    redraw();add.focus();
  });
  document.getElementById('login-content-search').addEventListener('input',function(){table.search(this.value).draw();});
  document.getElementById('login-content-page-size').addEventListener('change',function(){table.page.len(Number(this.value)).draw();});
  savedSnapshot=snapshot();
  redraw();
}());
</script>
