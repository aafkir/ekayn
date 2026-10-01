import type { APIRoute } from 'astro';
import { getOrder, getRuntimeEnv, json, saveOrder } from '../../lib/server';

export const GET: APIRoute = async ({ request, locals }) => {
  const env = getRuntimeEnv(locals); const secret = env.STRIPE_SECRET_KEY; const sessionId = new URL(request.url).searchParams.get('session_id');
  if (!secret || !sessionId || !/^cs_[A-Za-z0-9_]+$/.test(sessionId)) return json({ error: 'Session de paiement invalide.' }, 400);
  const response = await fetch(`https://api.stripe.com/v1/checkout/sessions/${sessionId}`, { headers: { Authorization: `Bearer ${secret}` } }); const session = await response.json() as any;
  if (!response.ok || !session.id) return json({ error: 'Session introuvable.' }, 404);
  const orderId = session.metadata?.order_id; const order = orderId ? await getOrder(env, orderId) : null; const paid = session.payment_status === 'paid';
  if (order && paid && order.status !== 'paid') { order.status = 'paid'; order.updatedAt = new Date().toISOString(); await saveOrder(env, order); }
  return json({ status: paid ? 'paid' : session.status, paymentStatus: session.payment_status, items: order?.items ?? [], orderId: orderId ?? null });
};
