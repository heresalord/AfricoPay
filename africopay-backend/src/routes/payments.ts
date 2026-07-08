import { Router, Request, Response } from 'express';
import {
  createCheckoutInvoice,
  getInvoiceStatus,
  PaymentChannel,
  CustomerInfo,
  InvoiceItem,
} from '../services/paydunyaService';

// Opérateurs prioritaires pour AfricoPay (plan approuvé)
export const DEFAULT_CHANNELS: PaymentChannel[] = [
  'card',
  'wave-senegal',
  'orange-money-senegal',
  'mtn-benin',
];

const router = Router();

/**
 * POST /v1/payments/initiate
 *
 * Crée une facture PayDunya et retourne l'URL de la page de paiement sandbox.
 *
 * Body:
 *   - amount       {number}  Montant en XOF (obligatoire)
 *   - description  {string}  Description de la transaction (obligatoire)
 *   - storeName    {string}  Nom de la boutique (optionnel)
 *   - customer     {object}  Infos client (name, email, phone) (optionnel)
 *   - channels     {array}   Canaux de paiement souhaités (optionnel)
 *   - items        {object}  Articles de la facture (optionnel)
 *   - customData   {object}  Données personnalisées (optionnel)
 */
router.post('/initiate', async (req: Request, res: Response) => {
  const {
    amount,
    description,
    storeName,
    customer,
    channels,
    items,
    customData,
  } = req.body as {
    amount?: number;
    description?: string;
    storeName?: string;
    customer?: CustomerInfo;
    channels?: PaymentChannel[];
    items?: Record<string, InvoiceItem>;
    customData?: Record<string, unknown>;
  };

  // Validation
  if (!amount || typeof amount !== 'number' || amount <= 0) {
    res.status(400).json({ error: 'Le champ "amount" est requis et doit être un nombre positif' });
    return;
  }
  if (!description || typeof description !== 'string') {
    res.status(400).json({ error: 'Le champ "description" est requis' });
    return;
  }

  try {
    const result = await createCheckoutInvoice({
      totalAmount: amount,
      description,
      storeName,
      customer,
      channels: channels || DEFAULT_CHANNELS,
      items,
      customData,
      returnUrl: 'https://africopay.com/payment/success',
      cancelUrl: 'https://africopay.com/payment/cancel',
    });

    res.status(201).json({
      success: true,
      token: result.token,
      checkoutUrl: result.checkoutUrl,
    });
  } catch (error) {
    const message = error instanceof Error ? error.message : 'Erreur inconnue';
    console.error('[POST /payments/initiate]', message);
    res.status(502).json({ error: 'Erreur lors de la création de la facture PayDunya', detail: message });
  }
});

/**
 * GET /v1/payments/:token/status
 *
 * Vérifie le statut d'une facture PayDunya par son token.
 *
 * Statuts possibles : pending | completed | cancelled | failed
 */
router.get('/:token/status', async (req: Request, res: Response) => {
  const token = req.params.token as string;

  if (!token) {
    res.status(400).json({ error: 'Token manquant' });
    return;
  }

  try {
    const result = await getInvoiceStatus(token);
    res.json({
      success: true,
      status: result.status,
      token: result.token,
      totalAmount: result.totalAmount,
      customer: result.customer,
      receiptUrl: result.receiptUrl,
    });
  } catch (error) {
    const message = error instanceof Error ? error.message : 'Erreur inconnue';
    console.error(`[GET /payments/${token}/status]`, message);
    res.status(502).json({ error: 'Impossible de récupérer le statut du paiement', detail: message });
  }
});

export default router;
