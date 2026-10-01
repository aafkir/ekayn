import type { APIRoute } from 'astro';
import { getRuntimeEnv } from '../../lib/server';

const attempts = new Map<string, number[]>();
const json = (body: Record<string, unknown>, status = 200) => new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } });

export const POST: APIRoute = async ({ request, clientAddress, locals }) => {
  const now = Date.now(); const key = clientAddress || 'unknown'; const recent = (attempts.get(key) ?? []).filter((time) => now - time < 60 * 60 * 1000);
  if (recent.length >= 5) return json({ error: 'Trop de messages ont été envoyés. Réessayez plus tard.' }, 429);
  attempts.set(key, [...recent, now]);
  try {
    const body = await request.json() as Record<string, string>;
    if (body.website) return json({ message: 'Merci, votre message a bien été envoyé.' });
    const name = body.name?.trim(); const email = body.email?.trim(); const subject = body.subject?.trim(); const message = body.message?.trim();
    if (!name || !subject || !message || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email ?? '')) return json({ error: 'Vérifiez les champs du formulaire.' }, 400);
    const env = getRuntimeEnv(locals);
    const apiKey = env.RESEND_API_KEY; const recipient = env.CONTACT_EMAIL;
    if (!apiKey || !recipient) return json({ error: 'Le formulaire de contact n’est pas encore configuré. Ajoutez RESEND_API_KEY et CONTACT_EMAIL.' }, 503);
    const response = await fetch('https://api.resend.com/emails', { method: 'POST', headers: { Authorization: `Bearer ${apiKey}`, 'Content-Type': 'application/json' }, body: JSON.stringify({ from: env.CONTACT_FROM ?? 'Ekayn <onboarding@resend.dev>', to: [recipient], reply_to: email, subject: `[Ekayn] ${subject}`, text: `Nom : ${name}\nEmail : ${email}\n\n${message}` }) });
    if (!response.ok) return json({ error: 'Le message n’a pas pu être envoyé. Réessayez plus tard.' }, 502);
    return json({ message: 'Votre message a bien été envoyé. Merci !' });
  } catch { return json({ error: 'Requête invalide.' }, 400); }
};
