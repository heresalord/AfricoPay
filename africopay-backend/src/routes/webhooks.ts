import { Router, Request, Response } from 'express';
import { verifyPaydunyaHash } from '../services/hashVerifier';

const router = Router();

/**
 * POST /v1/webhooks/paydunya
 *
 * Endpoint IPN (Instant Payment Notification) de PayDunya.
 *
 * PayDunya envoie une requête POST avec les données de la transaction
 * lorsqu'un paiement est confirmé, annulé ou échoué.
 *
 * IMPORTANT : Cette URL doit être publiquement accessible.
 * En développement : utiliser ngrok (`npx ngrok http 3000`)
 *
 * Payload reçu (format JSON) :
 * {
 *   hash: "sha512 du MASTER_KEY",
 *   data: {
 *     invoice: { token, total_amount, status, ... },
 *     customer: { name, email, phone },
 *     custom_data: { ... }
 *   }
 * }
 *
 * Pour les déboursements, le format est légèrement différent.
 */
router.post('/', (req: Request, res: Response) => {
  try {
    const payload = req.body as {
      hash?: string;
      data?: {
        response_code?: string;
        status?: string;
        transaction_id?: string;
        invoice?: {
          token?: string;
          total_amount?: number;
          status?: string;
        };
        customer?: {
          name?: string;
          email?: string;
          phone?: string;
        };
        custom_data?: Record<string, unknown>;
      };
    };

    const { hash, data } = payload;

    // 1. Vérification du hash (authenticité de la notification)
    if (!hash) {
      console.warn('[IPN] Hash manquant dans le payload');
      res.status(400).json({ error: 'Hash manquant' });
      return;
    }

    if (!verifyPaydunyaHash(hash)) {
      console.error('[IPN] Hash invalide — notification non authentifiée');
      res.status(403).json({ error: 'Hash invalide' });
      return;
    }

    // 2. Traitement de la notification
    const status = data?.invoice?.status || data?.status;
    const token = data?.invoice?.token || data?.transaction_id;
    const amount = data?.invoice?.total_amount;
    const customer = data?.customer;
    const responseCode = data?.response_code;

    console.log(`[IPN] Notification reçue — token: ${token}, status: ${status}, montant: ${amount} XOF`);

    // 3. Mise à jour du statut selon le résultat
    switch (status) {
      case 'completed':
      case 'success':
        console.log(`[IPN] ✅ Paiement confirmé — ${token} — ${amount} XOF — client: ${customer?.name}`);
        // TODO: Mettre à jour la transaction en base de données
        // TODO: Notifier l'app Android via WebSocket ou poll
        break;

      case 'cancelled':
        console.log(`[IPN] ⚠️  Paiement annulé — ${token}`);
        // TODO: Marquer la transaction comme annulée
        break;

      case 'failed':
        console.log(`[IPN] ❌ Paiement échoué — ${token} — code: ${responseCode}`);
        // TODO: Marquer la transaction comme échouée
        break;

      default:
        console.warn(`[IPN] Statut inconnu reçu : "${status}"`);
    }

    // PayDunya attend un HTTP 200 pour considérer l'IPN comme reçue
    res.status(200).json({ received: true });
  } catch (error) {
    const message = error instanceof Error ? error.message : 'Erreur inconnue';
    console.error('[IPN] Erreur traitement webhook:', message);
    // On retourne 200 pour éviter que PayDunya renvoie indéfiniment
    res.status(200).json({ received: true, warning: 'Erreur de traitement interne' });
  }
});

export default router;
