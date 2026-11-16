'use strict';
const screen = document.querySelector('#screen');
const message = document.querySelector('#message');
const status = document.querySelector('#status');
let state = null;
let busy = false;
let filters = {precinto: '', origen: ''};
const esc = value => String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const button = (text, action, style = 'primary') => `<button type="button" class="btn ${style}" data-action="${action}">${text}</button>`;

async function request(action, values = {}) {
  if (busy) return;
  busy = true;
  screen.setAttribute('aria-busy', 'true');
  document.querySelectorAll('button').forEach(b => b.disabled = true);
  status.textContent = action === 'confirmar' ? 'Registrando la recepción…' : 'Cargando…';
  message.textContent = '';
  try {
    const options = action ? {method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded', 'X-CU28':'1'}, body:new URLSearchParams({accion:action, ...values})} : {};
    const response = await fetch(action ? '/api/accion' : '/api/estado', options);
    const data = await response.json();
    if (data.paso) {
      state = data;
      render();
    } else if (!response.ok) throw new Error(data.error || 'No se pudo completar la solicitud.');
    if (data.error) { message.textContent = data.error; message.className = 'notice error'; }
    else if (data.aviso) { message.textContent = data.aviso; message.className = 'notice'; }
  } catch (error) {
    message.className = 'notice error';
    message.textContent = error.message === 'Failed to fetch' ? 'No se pudo conectar con la aplicación local. Verificá que siga ejecutándose y reintentá.' : error.message;
    if (!state) screen.innerHTML = `<h2>No se pudo cargar la aplicación</h2><p class="muted">Comprobá que el servidor local esté disponible.</p>${button('Reintentar', 'recargar')}`;
  } finally {
    busy = false;
    status.textContent = '';
    screen.setAttribute('aria-busy', 'false');
    document.querySelectorAll('button').forEach(b => b.disabled = b.hasAttribute('data-unavailable'));
  }
}

function render() {
  const s = state, b = s.seleccionado;
  document.querySelector('#session-label').textContent = `Encargado de Bolsines · ${s.cm}`;
  document.querySelector('#progress').style.width = `${s.paso * 20}%`;
  document.querySelectorAll('.step').forEach(e => {
    const active = Number(e.dataset.n) === s.paso;
    e.classList.toggle('active', active);
    if (active) e.setAttribute('aria-current', 'step'); else e.removeAttribute('aria-current');
  });
  const heading = title => `<div class="eyebrow">Paso ${s.paso} de 5</div><h2 tabindex="-1">${title}</h2>`;
  const summary = b ? `<p class="muted">Bolsín <b>#${esc(b.numero)}</b> · ${esc(b.origen)} · Precinto ${esc(b.precinto)}</p>` : '';
  if (s.paso === 1) {
    screen.innerHTML = `${heading('Comisión médica de destino')}<p class="muted">La recepción se registrará en la CM asociada al empleado de la sesión.</p><div class="info"><b>EMPLEADO LOGUEADO</b>${esc(s.empleado)} · Encargado de Bolsines</div><div class="info"><b>CM DESTINO</b>${esc(s.cm)}</div><div class="actions"><span class="muted">Sesión activa</span>${button('Ver bolsines enviados →','listar')}</div>`;
  } else if (s.paso === 2) {
    screen.innerHTML = `${heading('Seleccioná un bolsín')}<p class="muted">Bolsines enviados a ${esc(s.cm)}. Buscá por precinto o comisión médica de origen.</p>
      <form id="filters" class="filters"><label class="field">Número de precinto<input name="precinto" value="${esc(filters.precinto)}" placeholder="Ej. PRE-2026-01" maxlength="100"></label><label class="field">CM de origen<input name="origen" value="${esc(filters.origen)}" placeholder="Ej. Córdoba" maxlength="150"></label><div class="filter-actions"><button class="btn secondary" type="submit">Buscar</button>${button('Mostrar todos','limpiar','danger')}</div></form>
      <form id="selection"><div class="bolsin-list">${s.bolsines.map(item => `<label class="bolsin"><input type="radio" name="id" value="${item.id}" required><span><strong>Bolsín #${esc(item.numero)} <span class="tag">${esc(item.estado)}</span></strong><span class="meta">Origen: ${esc(item.origen)} · Precinto: ${esc(item.precinto)}</span><span class="meta document-count">${item.cantidadRemitos} remito${item.cantidadRemitos === 1 ? '' : 's'} · ${item.cantidadDocumentos} documento${item.cantidadDocumentos === 1 ? '' : 's'}</span></span></label>`).join('') || '<div class="info"><b>SIN RESULTADOS</b>No hay bolsines enviados para esta búsqueda. Podés cambiar los filtros o volver a consultar.</div>'}</div><div class="actions"><span class="muted">${s.bolsines.length} bolsines encontrados</span><button class="btn primary" type="submit" ${s.bolsines.length ? '' : 'disabled data-unavailable'}>Seleccionar bolsín →</button></div></form>`;
  } else if (s.paso === 3) {
    screen.innerHTML = `${heading('Revisá los remitos y la documentación')}${summary}${s.remitos.map(r => `<article class="remito"><h3>Remito #${esc(r.numero)}</h3>${r.documentos.map(d => `<div class="documento"><p>${esc(d.asunto)}</p><span class="meta">Tipo de documentación: ${esc(d.tipo)}</span></div>`).join('')}</article>`).join('')}<div class="notice">Continuá si el contenido del bolsín coincide con lo registrado y corresponde a tu comisión médica.</div><div class="actions">${button('← Elegir otro bolsín','listar','secondary')}${button('El contenido coincide →','revisar')}</div>`;
  } else if (s.paso === 4) {
    screen.innerHTML = `${heading('Confirmar recepción')}${summary}<p class="muted">Al confirmar, se guardará la recepción a nombre de ${esc(s.empleado)}.</p><div class="notice">Bolsín: <b>Recibido en CM Destino</b><br>Remitos: <b>Recibido y Aceptado</b><br>Documentación: <b>Recibida y Aceptada</b></div><div class="actions">${button('← No confirmar, volver','noConfirmar','secondary')}${button('Confirmar recepción','confirmar')}</div>`;
  } else if (s.cancelado) {
    screen.innerHTML = `<div class="empty"><div class="eyebrow">Caso de uso cancelado</div><h2 tabindex="-1">Recepción cancelada</h2><p class="muted">No se registraron cambios en el bolsín.</p>${button('Iniciar otra recepción','iniciar')}</div>`;
  } else {
    screen.innerHTML = `<div class="empty"><div class="success-icon" aria-hidden="true">✓</div><div class="eyebrow">Caso de uso completado</div><h2 tabindex="-1">Recepción registrada</h2><p class="muted">Se guardó la recepción del bolsín #${esc(b.numero)} y se actualizaron sus remitos y documentación.</p><div class="info"><b>NOTIFICACIÓN A ORIGEN</b>El aviso de recepción se simuló en la consola de la aplicación.</div>${button('Iniciar otra recepción','iniciar')}</div>`;
  }
  if (!s.finalizado) screen.insertAdjacentHTML('beforeend', `<div class="cancel-row">${button('Cancelar recepción','cancelar','danger')}</div>`);
  screen.querySelector('h2')?.focus({preventScroll:true});
}

screen.addEventListener('click', event => {
  const target = event.target.closest('[data-action]');
  if (!target || busy) return;
  const action = target.dataset.action;
  if (action === 'limpiar') { filters = {precinto:'', origen:''}; request('buscar', filters); }
  else if (action === 'recargar') request();
  else { if (action === 'iniciar' || action === 'listar') filters = {precinto:'', origen:''}; request(action); }
});
screen.addEventListener('submit', event => {
  event.preventDefault();
  const values = Object.fromEntries(new FormData(event.target));
  if (event.target.getAttribute('id') === 'filters') { filters = values; request('buscar', values); }
  if (event.target.getAttribute('id') === 'selection') request('seleccionar', values);
});
request();
