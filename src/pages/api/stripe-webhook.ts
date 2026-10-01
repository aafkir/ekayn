import type { APIRoute } from 'astro';
import { getOrder, getOrdersStore, getRuntimeEnv, json, saveOrder } from '../../lib/server';

const hex = (bytes: ArrayBuffer) => [...new Uint8Array(bytes)].map((byte) => byte.toString(16).padStart(2, '0')).join('');
const verify = async (payload: string, signature: string, secret: string) => { const values = Object.fromEntries(signature.split(',').map((part) => part.split('='))); const timestamp = Number(values.t); if (!timestamp || Math.abs(Date.now() / 1000 - timestamp) > 300 || !values.v1) return false; const key = await crypto.subtle.importKey('raw', new TextEncoder().encode(secret), { name: 'HMAC', hash: 'SHA-256' }, false, ['sign']); const expected = hex(await crypto.subtle.sign('HMAC', key, new TextEncoder().encode(`${timestamp}.${payload}`))); return expected === values.v1; };

export const POST: APIRoute = async ({ request, locals }) => {
  const env = getRuntimeEnv(locals); const secret = env.STRIPE_WEBHOOK_SECRET; const signature = request.headers.get('stripe-signature'); const payload = await request.text();
  if (!secret || !signature || !(await verify(payload, signature, secret))) return json({ error: 'Signature webhook invalide.' }, 400);
  const event = JSON.parse(payload) as any; const store = getOrdersStore(env); if (!store) return json({ error: 'Le stockage durable ORDERS n’est pas configuré.' }, 503);
  if (await store.get(`event:${event.id}`)) return json({ received: true });
  const session = event.data?.object; const orderId = session?.metadata?.order_id; if (orderId) { const order = await getOrder(env, orderId); if (order) { const paid = event.type === 'checkout.session.completed' || event.type === 'checkout.session.async_payment_succeeded'; order.status = paid ? 'paid' : event.type === 'checkout.session.async_payment_failed' ? 'payment_failed' : event.type === 'checkout.session.expired' ? 'expired' : order.status; order.sessionId = session.id; order.updatedAt = new Date().toISOString(); order.eventIds = [...(order.eventIds ?? []), event.id]; await saveOrder(env, order); } }
  await store.put(`event:${event.id}`, new Date().toISOString()); return json({ received: true });
};
