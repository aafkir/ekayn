import type { APIRoute } from 'astro';
import { products } from '../../data/products';
import { getRuntimeEnv, json, saveOrder, type OrderItem, type OrderRecord } from '../../lib/server';

export const POST: APIRoute = async ({ request, locals }) => {
  const env = getRuntimeEnv(locals); const secret = env.STRIPE_SECRET_KEY;
  if (!secret) return json({ error: 'Le paiement Stripe n’est pas configuré sur ce serveur.' }, 503);
  try {
    const body = await request.json() as { items?: unknown };
    if (!Array.isArray(body.items) || !body.items.length || body.items.length > 50) return json({ error: 'Panier invalide.' }, 400);
    const seen = new Set<string>(); const items: OrderItem[] = [];
    for (const value of body.items) { const item = value as { id?: unknown; quantity?: unknown }; if (typeof item.id !== 'string' || seen.has(item.id) || !Number.isInteger(item.quantity) || Number(item.quantity) < 1 || Number(item.quantity) > 99) return json({ error: 'Produit ou quantité invalide.' }, 400); const product = products.find((entry) => entry.id === item.id); if (!product) return json({ error: 'Produit inconnu.' }, 400); seen.add(item.id); items.push({ id: product.id, quantity: Number(item.quantity) }); }
    const amountCents = items.reduce((sum, item) => sum + products.find((product) => product.id === item.id)!.priceCents * item.quantity, 0);
    const orderId = crypto.randomUUID(); const createdAt = new Date().toISOString();
    const order: OrderRecord = { id: orderId, items, amountCents, status: 'pending', createdAt, updatedAt: createdAt };
    const params = new URLSearchParams({ mode: 'payment', customer_creation: 'always', billing_address_collection: 'auto', 'metadata[order_id]': orderId, 'client_reference_id': orderId, success_url: `${new URL(request.url).origin}/success?session_id={CHECKOUT_SESSION_ID}`, cancel_url: `${new URL(request.url).origin}/cancel` });
    const countries = String(env.SHIPPING_COUNTRIES ?? 'FR').split(',').map((country: string) => country.trim().toUpperCase()).filter(Boolean);
    countries.forEach((country: string, index: number) => params.set(`shipping_address_collection[allowed_countries][${index}]`, country));
    items.forEach((item, index) => { const product = products.find((entry) => entry.id === item.id)!; params.set(`line_items[${index}][price_data][currency]`, 'eur'); params.set(`line_items[${index}][price_data][unit_amount]`, String(product.priceCents)); params.set(`line_items[${index}][price_data][product_data][name]`, product.name); params.set(`line_items[${index}][price_data][product_data][description]`, product.description.slice(0, 500)); params.set(`line_items[${index}][quantity]`, String(item.quantity)); });
    const response = await fetch('https://api.stripe.com/v1/checkout/sessions', { method: 'POST', headers: { Authorization: `Bearer ${secret}`, 'Content-Type': 'application/x-www-form-urlencoded', 'Idempotency-Key': orderId }, body: params });
    const session = await response.json() as { id?: string; url?: string; error?: { message?: string } };
    if (!response.ok || !session.id || !session.url) return json({ error: session.error?.message ?? 'Stripe n’a pas pu créer la session.' }, 502);
    order.sessionId = session.id; await saveOrder(env, order);
    return json({ url: session.url, orderId });
  } catch { return json({ error: 'Impossible de préparer le paiement.' }, 400); }
};
