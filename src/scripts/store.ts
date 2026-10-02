import { products, shippingCents } from '../data/products';

const money = (cents: number) => new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'EUR' }).format(cents / 100);
let cart: Record<string, number> = {};
try {
  const saved = JSON.parse(localStorage.getItem('ekayn-cart') ?? '{}');
  const version = localStorage.getItem('ekayn-cart-version');
  const legacyIds: Record<string, string> = { '2': '6', '3': '5', '4': '3', '5': '2', '6': '4' };
  products.forEach((product) => { const sourceId = version === '2' ? product.id : Object.keys(legacyIds).find((legacyId) => legacyIds[legacyId] === product.id) ?? product.id; if (Number.isInteger(saved[sourceId]) && saved[sourceId] > 0 && saved[sourceId] <= 99) cart[product.id] = saved[sourceId]; });
  localStorage.setItem('ekayn-cart-version', '2');
} catch { /* storage unavailable */ }
const $ = <T extends Element>(selector: string) => document.querySelector<T>(selector)!;
const menu = $('#menu-panel') as HTMLDialogElement;
const cartPanel = $('#cart-panel') as HTMLDialogElement;

$('#open-menu').addEventListener('click', () => { menu.showModal(); $('#open-menu').setAttribute('aria-expanded', 'true'); });
menu.addEventListener('close', () => $('#open-menu').setAttribute('aria-expanded', 'false'));
$('#open-cart').addEventListener('click', () => cartPanel.showModal());
$('#menu-cart').addEventListener('click', () => { menu.close(); cartPanel.showModal(); });
document.querySelectorAll<HTMLDialogElement>('.side-panel').forEach((panel) => panel.querySelector('.panel-close')?.addEventListener('click', () => panel.close()));

const renderCart = () => {
  const entries = products.filter((product) => cart[product.id]);
  const count = entries.reduce((total, product) => total + cart[product.id], 0);
  const subtotal = entries.reduce((total, product) => total + product.priceCents * cart[product.id], 0);
  const delivery = entries.length ? shippingCents : 0;
  const total = subtotal + delivery;
  $('#cart-count').textContent = String(count);
  const container = $('#cart-items');
  container.replaceChildren();
  if (!entries.length) { const empty = document.createElement('p'); empty.className = 'cart-empty'; empty.textContent = 'Votre panier est vide. Découvrez la collection et ajoutez votre coup de cœur.'; container.append(empty); }
  entries.forEach((product) => {
    const item = document.createElement('div'); item.className = 'cart-item';
    item.innerHTML = `<img src="${product.image}" alt="${product.alt}" width="70" height="95"><div><h3>${product.name}</h3><p>Prix unitaire : ${money(product.priceCents)}</p><p>Total : ${money(product.priceCents * cart[product.id])}</p><div class="quantity"><button aria-label="Diminuer la quantité de ${product.name}">−</button><span>${cart[product.id]}</span><button aria-label="Augmenter la quantité de ${product.name}" ${cart[product.id] >= 99 ? 'disabled' : ''}>+</button><button class="remove">Retirer</button></div></div>`;
    const buttons = item.querySelectorAll('button');
    buttons[0].addEventListener('click', () => { cart[product.id]--; if (!cart[product.id]) delete cart[product.id]; save(); });
    buttons[1].addEventListener('click', () => { cart[product.id] = Math.min(99, cart[product.id] + 1); save(); });
    buttons[2].addEventListener('click', () => { delete cart[product.id]; save(); });
    container.append(item);
  });
  const summary = $('#cart-summary'); summary.replaceChildren();
  if (entries.length) [['Sous-total des bijoux', subtotal], ['Livraison en France', delivery], ['Total à payer', total]].forEach(([label, value], index) => { const row = document.createElement('div'); row.className = index === 2 ? 'summary-total' : 'summary-row'; row.innerHTML = `<span>${label}</span><strong>${money(value as number)}</strong>`; summary.append(row); });
  $('#cart-actions').toggleAttribute('hidden', !entries.length);
  const checkout = $('#card-checkout') as HTMLButtonElement;
  checkout.disabled = !entries.length;
  checkout.textContent = entries.length ? `Passer au paiement · ${money(total)}` : 'Passer au paiement';
};
const save = () => { try { localStorage.setItem('ekayn-cart', JSON.stringify(cart)); } catch { /* storage unavailable */ } renderCart(); };
document.querySelectorAll<HTMLElement>('.add-cart').forEach((button) => button.addEventListener('click', () => { const quantity = Math.min(99, Math.max(1, Number(button.dataset.quantity ?? 1))); cart[button.dataset.id!] = Math.min(99, (cart[button.dataset.id!] ?? 0) + quantity); save(); const toast = $('#toast'); toast.textContent = `${quantity} bijou${quantity > 1 ? 'x' : ''} ajouté${quantity > 1 ? 's' : ''} au panier`; toast.classList.add('visible'); setTimeout(() => toast.classList.remove('visible'), 2500); }));
$('#card-checkout').addEventListener('click', async () => {
  const button = $('#card-checkout') as HTMLButtonElement; const feedback = $('#cart-feedback');
  button.disabled = true; button.textContent = 'Préparation du paiement…'; feedback.textContent = '';
  try {
    const response = await fetch('/api/checkout', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ items: products.filter((product) => cart[product.id]).map((product) => ({ id: product.id, quantity: cart[product.id] })) }) });
    const data = await response.json(); if (!response.ok || typeof data.url !== 'string') throw new Error(data.error || 'Paiement indisponible');
    window.location.assign(data.url);
  } catch (error) { feedback.textContent = error instanceof Error ? error.message : 'Le paiement est indisponible pour le moment.'; renderCart(); }
});
renderCart();
