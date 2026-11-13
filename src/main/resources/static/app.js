'use strict';

(() => {
  const $ = (selector) => document.querySelector(selector);
  const $$ = (selector) => [...document.querySelectorAll(selector)];
  const state = {
    menu: null, cart: [], category: 'ALL', search: '', orders: [], quote: null,
    quoteVersion: 0, quoteTimer: null, quoteController: null, placing: false,
    requestId: null, requestFingerprint: null, editing: null, dialogItem: null,
    toastTimer: null, ordersVersion: 0
  };
  const statusLabels = { PLACED: 'Order placed', PREPARING: 'In the kitchen', READY: 'Ready to enjoy', COMPLETED: 'Completed' };
  const statusOrder = Object.keys(statusLabels);
  const currency = new Intl.NumberFormat('en-US', { maximumFractionDigits: 2 });
  const money = (amount) => `${currency.format(Number(amount))} ₸`;
  const node = (tag, className, text) => {
    const element = document.createElement(tag);
    if (className) element.className = className;
    if (text !== undefined) element.textContent = text;
    return element;
  };
  const asset = (id) => `/assets/${['margherita', 'pepperoni', 'garden', 'alfredo', 'pesto', 'lemonade'].includes(id) ? id : 'margherita'}.svg`;
  const findItem = (id) => state.menu?.items.find((item) => item.id === id);
  const showError = (selector, message) => {
    const box = $(selector);
    box.textContent = message || '';
    box.hidden = !message;
  };
  const announce = (message) => {
    clearTimeout(state.toastTimer);
    $('#toast').textContent = message;
    $('#toast').hidden = false;
    state.toastTimer = setTimeout(() => { $('#toast').hidden = true; }, 3800);
  };
  async function api(path, options = {}) {
    const response = await fetch(path, {
      credentials: 'same-origin', ...options,
      headers: { Accept: 'application/json', ...(options.body ? { 'Content-Type': 'application/json' } : {}), ...options.headers }
    });
    const body = await response.json().catch(() => null);
    if (!response.ok) throw new Error(body?.message || `We could not complete that request (${response.status}). Please try again.`);
    if (body === null) throw new Error('The kitchen returned an unexpected response. Please try again.');
    return body;
  }
  function fulfilment() { return $('input[name="fulfillment"]:checked').value; }
  function cartRequest() {
    return {
      items: state.cart.map(({ menuItemId, quantity, toppings }) => ({ menuItemId, quantity, toppings: [...toppings] })),
      discountCode: $('#discount-code').value,
      fulfillment: fulfilment()
    };
  }
  function lineUnitPrice(line) {
    return Number(findItem(line.menuItemId).price) + line.toppings.reduce((sum, id) => sum + Number(state.menu.toppings.find((t) => t.id === id)?.price || 0), 0);
  }
  function persistCart() {
    try { localStorage.setItem('hearth-table-cart-v1', JSON.stringify(state.cart)); } catch { /* Private browsing may disable local storage. */ }
  }
  function restoreCart() {
    try {
      const saved = JSON.parse(localStorage.getItem('hearth-table-cart-v1') || '[]');
      if (!Array.isArray(saved)) return;
      state.cart = saved.slice(0, 30).filter((line) => line && findItem(line.menuItemId) && Number.isInteger(line.quantity) && line.quantity >= 1 && line.quantity <= 10 && Array.isArray(line.toppings)).map((line) => ({
        menuItemId: line.menuItemId, quantity: line.quantity,
        toppings: findItem(line.menuItemId).category === 'DRINK' ? [] : [...new Set(line.toppings.filter((id) => state.menu.toppings.some((t) => t.id === id)))]
      }));
    } catch { state.cart = []; }
  }
  function renderMenu() {
    const filtered = state.menu.items.filter((item) => (state.category === 'ALL' || item.category === state.category) && `${item.name} ${item.description}`.toLowerCase().includes(state.search));
    const grid = $('#menu-grid');
    grid.replaceChildren();
    $('#menu-result-count').textContent = `${filtered.length} ${filtered.length === 1 ? 'favourite' : 'favourites'}`;
    if (!filtered.length) { grid.append(node('div', 'empty-state', 'No dishes found. Try another search or category.')); return; }
    for (const item of filtered) {
      const card = node('article', 'dish-card');
      const image = node('img', 'dish-image');
      image.src = asset(item.id); image.alt = ''; image.dataset.category = item.category; image.loading = 'lazy';
      const content = node('div', 'dish-content');
      content.append(node('div', 'dish-category', item.category === 'DRINK' ? 'A refreshing sip' : item.category === 'PASTA' ? 'A little Italian comfort' : 'Straight from the hearth'), node('h3', '', item.name), node('p', 'dish-description', item.description));
      const bottom = node('div', 'dish-bottom');
      const price = node('span', 'dish-price', money(item.price));
      const add = node('button', 'add-dish', '+'); add.type = 'button'; add.setAttribute('aria-label', `Add ${item.name} to your order`);
      add.addEventListener('click', () => openCustomize(item));
      bottom.append(price, add); content.append(bottom); card.append(image, content); grid.append(card);
    }
  }
  function renderDiscounts() {
    const select = $('#discount-code');
    const previous = select.value;
    select.replaceChildren();
    for (const discount of state.menu.discounts) {
      const option = node('option', '', discount.name);
      option.value = discount.id;
      option.disabled = discount.id === 'PICKUP' && fulfilment() !== 'TAKEAWAY';
      select.append(option);
    }
    if ([...select.options].some((option) => option.value === previous && !option.disabled)) select.value = previous;
    else select.value = 'NONE';
    renderDiscountHint();
  }
  function renderDiscountHint() {
    $('#discount-hint').textContent = state.menu?.discounts.find((item) => item.id === $('#discount-code').value)?.description || '';
  }
  function renderCart() {
    const quantity = state.cart.reduce((sum, line) => sum + line.quantity, 0);
    $('#basket-count').textContent = quantity;
    $('#cart-line-count').textContent = quantity;
    $('#cart-heading').textContent = quantity ? 'A very good choice.' : 'Your table awaits.';
    const container = $('#cart-lines'); container.replaceChildren();
    $('#checkout-form').hidden = !quantity;
    if (!quantity) {
      const empty = node('div', 'empty-cart');
      const plate = node('span', 'empty-plate'); plate.setAttribute('aria-hidden', 'true'); plate.append(node('span', '', '✳'));
      empty.append(plate, node('h3', '', 'Good things start here.'), node('p', '', 'Add a dish from the menu and make yourself at home.'));
      container.append(empty);
    }
    state.cart.forEach((line, index) => {
      const item = findItem(line.menuItemId);
      const row = node('div', 'cart-line');
      const image = node('img', 'cart-line-image'); image.src = asset(item.id); image.alt = '';
      const copy = node('div', 'cart-line-copy');
      const name = node('button', 'cart-line-name', item.name); name.type = 'button'; name.setAttribute('aria-label', `Edit ${item.name}`); name.addEventListener('click', () => openCustomize(item, index));
      copy.append(name, node('p', 'cart-extras', line.toppings.map((id) => state.menu.toppings.find((t) => t.id === id).name).join(', ') || 'Just the way we make it'));
      const bottom = node('div', 'cart-line-bottom');
      const control = node('div', 'quantity-control');
      const minus = node('button', '', '−'); minus.type = 'button'; minus.setAttribute('aria-label', `Decrease ${item.name} quantity`);
      const plus = node('button', '', '+'); plus.type = 'button'; plus.setAttribute('aria-label', `Increase ${item.name} quantity`); plus.disabled = line.quantity >= 10;
      minus.addEventListener('click', () => { if (line.quantity === 1) state.cart.splice(index, 1); else line.quantity--; cartChanged(); });
      plus.addEventListener('click', () => { if (line.quantity < 10) { line.quantity++; cartChanged(); } });
      control.append(minus, node('span', '', line.quantity), plus); bottom.append(control, node('strong', '', money(lineUnitPrice(line) * line.quantity))); copy.append(bottom);
      const remove = node('button', 'remove-line', '×'); remove.type = 'button'; remove.setAttribute('aria-label', `Remove ${item.name}`); remove.addEventListener('click', () => { state.cart.splice(index, 1); cartChanged(); });
      row.append(image, copy, remove); container.append(row);
    });
    if (state.placing) disableCheckout(true);
  }
  function cartChanged() {
    if (state.placing) return;
    persistCart(); renderCart(); queueQuote();
  }
  function queueQuote() {
    state.quoteVersion++; state.quote = null;
    clearTimeout(state.quoteTimer); state.quoteController?.abort();
    $('#place-order').disabled = true;
    $('#quote-subtotal').textContent = '—'; $('#quote-total').textContent = state.cart.length ? 'Calculating…' : '—';
    $('#discount-row').hidden = true; showError('#checkout-error', '');
    if (!state.cart.length) return;
    const version = state.quoteVersion;
    state.quoteTimer = setTimeout(async () => {
      state.quoteController = new AbortController();
      try {
        const quote = await api('/api/orders/quote', { method: 'POST', body: JSON.stringify(cartRequest()), signal: state.quoteController.signal });
        if (version !== state.quoteVersion) return;
        state.quote = quote;
        $('#quote-subtotal').textContent = money(quote.subtotal); $('#quote-total').textContent = money(quote.total);
        $('#quote-discount').textContent = `−${money(quote.discount)}`; $('#discount-row').hidden = !Number(quote.discount);
        $('#place-order').disabled = state.placing;
      } catch (error) {
        if (error.name === 'AbortError' || version !== state.quoteVersion) return;
        $('#quote-total').textContent = 'Unavailable'; showError('#checkout-error', error.message);
        const retry = node('button', '', 'Retry calculation'); retry.type = 'button'; retry.addEventListener('click', queueQuote); $('#checkout-error').append(retry);
      }
    }, 180);
  }
  function openCustomize(item, editing = null) {
    if (state.placing) return;
    if (editing === null && state.cart.length >= 30) { announce('Your order can contain up to 30 different dishes.'); return; }
    state.dialogItem = item; state.editing = editing;
    const line = editing === null ? null : state.cart[editing];
    $('#dialog-title').textContent = item.name; $('#dialog-description').textContent = item.description;
    $('#dialog-image').src = asset(item.id); $('#dialog-quantity').value = line?.quantity || 1;
    $('#toppings-field').hidden = item.category === 'DRINK';
    const options = $('#topping-options'); options.replaceChildren();
    if (item.category !== 'DRINK') for (const topping of state.menu.toppings) {
      const label = node('label', 'topping-option'); const checkbox = node('input'); checkbox.type = 'checkbox'; checkbox.name = 'toppings'; checkbox.value = topping.id; checkbox.checked = Boolean(line?.toppings.includes(topping.id));
      checkbox.addEventListener('change', updateDialogPrice);
      label.append(checkbox, node('span', '', topping.name), node('span', '', `+${money(topping.price)}`)); options.append(label);
    }
    $('#add-to-cart').firstChild.textContent = editing === null ? 'Add to my order ' : 'Save changes ';
    updateDialogPrice(); $('#customize-dialog').showModal(); document.body.classList.add('no-scroll');
  }
  function closeCustomize() { $('#customize-dialog').close(); }
  function updateDialogPrice() {
    const raw = Number($('#dialog-quantity').value); const quantity = Number.isInteger(raw) && raw >= 1 && raw <= 10 ? raw : 1;
    const selectedToppings = $$('#topping-options input:checked').length;
    $$('#topping-options input').forEach((input) => { input.disabled = !input.checked && selectedToppings >= 3; });
    const extras = $$('#topping-options input:checked').reduce((sum, input) => sum + Number(state.menu.toppings.find((t) => t.id === input.value).price), 0);
    $('#dialog-price').textContent = money((Number(state.dialogItem.price) + extras) * quantity);
    $('#dialog-minus').disabled = quantity <= 1; $('#dialog-plus').disabled = quantity >= 10;
  }
  function navigate() {
    const hash = location.hash.slice(1); const view = ['orders', 'about'].includes(hash) ? hash : 'menu';
    $$('.view').forEach((section) => { section.hidden = section.id !== `view-${view}`; });
    $$('[data-view]').forEach((link) => { const current = link.dataset.view === view; link.classList.toggle('active', current); if (current) link.setAttribute('aria-current', 'page'); else link.removeAttribute('aria-current'); });
    if (view === 'orders') loadOrders();
    if (['orders', 'about', 'menu'].includes(hash)) window.scrollTo({ top: 0, behavior: 'instant' });
    if (hash === 'your-order' || hash === 'menu-list') requestAnimationFrame(() => document.getElementById(hash).scrollIntoView({ behavior: 'smooth' }));
  }
  function formatDate(value) {
    const date = new Date(value);
    return Number.isNaN(date.valueOf()) ? '' : new Intl.DateTimeFormat('en-GB', { dateStyle: 'medium', timeStyle: 'short' }).format(date);
  }
  function renderOrders() {
    const container = $('#orders-list'); container.replaceChildren();
    $('#order-count').textContent = state.orders.length; $('#order-count').hidden = !state.orders.length;
    if (!state.orders.length) {
      const empty = node('div', 'empty-state'); empty.append(node('h2', '', 'Your next favourite is waiting.'), node('p', '', 'Place an order and follow its journey right here.'));
      const link = node('a', 'button button-primary', 'Explore the menu ↗'); link.href = '#menu'; empty.append(link); container.append(empty); return;
    }
    const sorted = [...state.orders].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    for (const order of sorted) {
      const article = node('article', 'order-card');
      const header = node('div', 'order-card-header'); const heading = node('div');
      heading.append(node('h2', '', `Order ${String(order.id).slice(0, 8).toUpperCase()}`), node('p', 'order-meta', `${order.customerName} · ${order.fulfillment === 'DINE_IN' ? `Table ${order.tableNumber}` : 'Take away'} · ${formatDate(order.createdAt)}`));
      const badge = node('span', 'status-badge', statusLabels[order.status] || order.status); badge.dataset.status = order.status; header.append(heading, badge); article.append(header);
      const steps = node('ol', 'status-steps'); steps.setAttribute('aria-label', 'Order progress'); const activeIndex = statusOrder.indexOf(order.status);
      statusOrder.forEach((status, index) => { const step = node('li', `${index <= activeIndex ? 'reached' : ''} ${index === activeIndex ? 'current' : ''}`, statusLabels[status]); if (index === activeIndex) step.setAttribute('aria-current', 'step'); steps.append(step); }); article.append(steps);
      const details = node('div', 'order-detail-grid'); const items = node('div'); items.append(node('h3', 'order-detail-title', 'Your good choices'));
      for (const item of order.items) {
        const row = node('div', 'order-item'); const name = node('span', '', `${item.quantity} × ${item.name}`);
        row.append(name, node('strong', '', money(item.lineTotal))); items.append(row);
      }
      if (Number(order.discount)) { const discount = node('div', 'order-item'); discount.append(node('span', 'muted', 'Discount'), node('strong', '', `−${money(order.discount)}`)); items.append(discount); }
      const total = node('div', 'order-total'); total.append(node('span', '', 'Total'), node('strong', '', money(order.total))); items.append(total);
      if (order.note) items.append(node('p', 'order-note', `Kitchen note: ${order.note}`));
      const notifications = node('div');
      for (const [title, messages] of [['A word from the kitchen', order.events], ['Operations dashboard', order.operationsEvents]]) {
        if (!messages?.length) continue;
        notifications.append(node('h3', 'order-detail-title', title));
        const events = node('ul', 'order-notifications');
        for (const event of [...messages].reverse()) {
          const entry = node('li'); entry.append(node('strong', '', event.audience), node('span', '', event.message));
          const time = node('time', '', formatDate(event.occurredAt)); time.dateTime = event.occurredAt; entry.append(time); events.append(entry);
        }
        notifications.append(events);
      }
      details.append(items, notifications); article.append(details);
      const footer = node('div', 'order-card-footer'); const receipts = node('div', 'receipt-links');
      for (const [type, label] of [['customer', 'Your receipt'], ['kitchen', 'Kitchen ticket']]) {
        for (const format of ['html', 'text', 'json']) {
          const link = node('a', '', `${label} · ${format.toUpperCase()}`); link.href = `/api/orders/${encodeURIComponent(order.id)}/receipt?format=${format}&type=${type}`; link.target = '_blank'; link.rel = 'noopener noreferrer'; receipts.append(link);
        }
      }
      footer.append(receipts);
      if (order.status !== 'COMPLETED') {
        const advance = node('button', 'button button-primary', 'Advance demo status →'); advance.type = 'button'; advance.setAttribute('aria-label', `Advance demo status for order ${String(order.id).slice(0, 8)}`);
        advance.addEventListener('click', async () => {
          advance.disabled = true; showError('#orders-error', '');
          try {
            const updated = await api(`/api/orders/${encodeURIComponent(order.id)}/advance`, { method: 'POST', body: '{}' });
            state.orders = state.orders.map((item) => item.id === updated.id ? updated : item); renderOrders(); announce(statusLabels[updated.status] || updated.status);
          } catch (error) { showError('#orders-error', error.message); advance.disabled = false; }
        }); footer.append(advance);
      }
      article.append(footer); container.append(article);
    }
  }
  async function loadOrders() {
    const version = ++state.ordersVersion;
    const refresh = $('#refresh-orders'); refresh.disabled = true; showError('#orders-error', '');
    try {
      const orders = await api('/api/orders');
      if (version !== state.ordersVersion) return;
      state.orders = orders; renderOrders();
    } catch (error) { if (version === state.ordersVersion) showError('#orders-error', error.message); }
    finally { if (version === state.ordersVersion) refresh.disabled = false; }
  }
  function disableCheckout(disabled) {
    $$('#checkout-form input, #checkout-form select, #checkout-form textarea, #cart-lines button').forEach((input) => { input.disabled = disabled; });
    $('#place-order').disabled = disabled || !state.quote;
    $('#place-order').firstElementChild.textContent = disabled ? 'Sending to the kitchen…' : 'Place my order';
  }
  async function placeOrder(event) {
    event.preventDefault();
    if (state.placing || !state.quote || !state.cart.length) return;
    const name = $('#customer-name').value.trim();
    if (name.length < 2) { showError('#checkout-error', 'Please enter a name with at least two characters.'); $('#customer-name').focus(); return; }
    const payload = { ...cartRequest(), customerName: name, tableNumber: fulfilment() === 'DINE_IN' ? Number($('#table-number').value) : null, note: $('#order-note').value.trim() };
    const fingerprint = JSON.stringify(payload);
    if (fingerprint !== state.requestFingerprint) { state.requestId = crypto.randomUUID(); state.requestFingerprint = fingerprint; }
    state.placing = true; disableCheckout(true); showError('#checkout-error', '');
    try {
      const order = await api('/api/orders', { method: 'POST', body: JSON.stringify({ ...payload, requestId: state.requestId }) });
      state.orders = [order, ...state.orders.filter((existing) => existing.id !== order.id)];
      state.cart = []; state.quote = null; state.requestFingerprint = null; state.requestId = null;
      persistCart(); renderCart(); renderOrders(); $('#order-note').value = ''; location.hash = 'orders';
      announce('Your order is in. Something good is on its way!');
    } catch (error) { showError('#checkout-error', error.message); }
    finally { state.placing = false; disableCheckout(false); renderDiscounts(); renderCart(); }
  }
  async function loadMenu() {
    showError('#menu-error', '');
    try {
      state.menu = await api('/api/menu');
      renderMenu(); renderDiscounts(); restoreCart(); renderCart(); queueQuote();
    } catch (error) {
      $('#menu-grid').replaceChildren(); showError('#menu-error', error.message);
      const retry = node('button', '', 'Try again'); retry.type = 'button'; retry.addEventListener('click', loadMenu); $('#menu-error').append(retry);
    }
  }

  for (let table = 1; table <= 30; table++) { const option = node('option', '', `${table}`); option.value = table; $('#table-number').append(option); }
  $$('.category').forEach((button) => button.addEventListener('click', () => {
    state.category = button.dataset.category;
    $$('.category').forEach((item) => { const selected = item === button; item.classList.toggle('active', selected); item.setAttribute('aria-pressed', selected); });
    if (state.menu) renderMenu();
  }));
  $('#menu-search').addEventListener('input', (event) => { state.search = event.target.value.trim().toLowerCase(); if (state.menu) renderMenu(); });
  $$('input[name="fulfillment"]').forEach((radio) => radio.addEventListener('change', () => {
    const takeaway = fulfilment() === 'TAKEAWAY'; $('#table-field').hidden = takeaway; $('#table-number').required = !takeaway;
    $('.checkout-fields').classList.toggle('single-field', takeaway); if (state.menu) renderDiscounts(); queueQuote();
  }));
  $('#discount-code').addEventListener('change', () => { renderDiscountHint(); queueQuote(); });
  $('#checkout-form').addEventListener('submit', placeOrder);
  $('#refresh-orders').addEventListener('click', loadOrders);
  $('#close-dialog').addEventListener('click', closeCustomize);
  $('#customize-dialog').addEventListener('close', () => document.body.classList.remove('no-scroll'));
  $('#customize-dialog').addEventListener('click', (event) => { if (event.target === $('#customize-dialog')) { const rect = event.target.getBoundingClientRect(); if (event.clientX < rect.left || event.clientX > rect.right || event.clientY < rect.top || event.clientY > rect.bottom) closeCustomize(); } });
  $('#dialog-quantity').addEventListener('input', updateDialogPrice);
  $('#dialog-minus').addEventListener('click', () => { $('#dialog-quantity').value = Math.max(1, Number($('#dialog-quantity').value) - 1); updateDialogPrice(); });
  $('#dialog-plus').addEventListener('click', () => { $('#dialog-quantity').value = Math.min(10, Number($('#dialog-quantity').value) + 1); updateDialogPrice(); });
  $('#customize-form').addEventListener('submit', (event) => {
    event.preventDefault();
    if (state.placing) return;
    const line = { menuItemId: state.dialogItem.id, quantity: Number($('#dialog-quantity').value), toppings: $$('#topping-options input:checked').map((input) => input.value).sort() };
    if (!Number.isInteger(line.quantity) || line.quantity < 1 || line.quantity > 10) return;
    if (state.editing !== null) state.cart[state.editing] = line;
    else {
      const existing = state.cart.find((item) => item.menuItemId === line.menuItemId && [...item.toppings].sort().join(',') === line.toppings.join(',') && item.quantity + line.quantity <= 10);
      if (existing) existing.quantity += line.quantity;
      else if (state.cart.length < 30) state.cart.push(line);
      else { announce('Your order can contain up to 30 different dishes.'); return; }
    }
    const message = state.editing === null ? `${state.dialogItem.name} added to your order.` : 'Your dish has been updated.';
    closeCustomize(); cartChanged(); announce(message);
  });
  window.addEventListener('hashchange', navigate);
  navigate(); loadMenu(); if (location.hash !== '#orders') loadOrders();
})();
