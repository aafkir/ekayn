export type OrderItem = { id: string; quantity: number };
export type OrderRecord = { id: string; sessionId?: string; items: OrderItem[]; amountCents: number; status: string; createdAt: string; updatedAt: string; eventIds?: string[] };
export type RuntimeEnv = Record<string, any>;

export const getRuntimeEnv = (_locals: any): RuntimeEnv => cloudflareEnv as RuntimeEnv;
export const getOrdersStore = (env: RuntimeEnv) => env.ORDERS as { get(key: string, type?: 'json'): Promise<any>; put(key: string, value: string): Promise<void> } | undefined;
export const saveOrder = async (env: RuntimeEnv, order: OrderRecord) => { const store = getOrdersStore(env); if (!store) return false; await store.put(`order:${order.id}`, JSON.stringify(order)); return true; };
export const getOrder = async (env: RuntimeEnv, id: string) => { const store = getOrdersStore(env); return store ? await store.get(`order:${id}`, 'json') as OrderRecord | null : null; };
export const json = (body: Record<string, unknown>, status = 200) => new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } });
import { env as cloudflareEnv } from 'cloudflare:workers';
