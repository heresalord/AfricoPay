import { Router, Request, Response } from 'express';
import {
  initiateDisbursement,
  getDisbursementStatus,
  WithdrawMode,
} from '../services/paydunyaService';

const router = Router();

// Modes de retrait valides
const VALID_WITHDRAW_MODES: WithdrawMode[] = [
  'orange-money-senegal',
  'wave-senegal',
  'free-money-senegal',
  'expresso-sn',
  'mtn-benin',
  'moov-benin',
  'orange-money-ci',
  'wave-ci',
  'mtn-ci',
  'moov-ci',
  't-money-togo',
  'moov-togo',
  'orange-money-mali',
  'orange-money-burkina',
  'mtn-cameroun',
  'djamo-ci',
  'djamo-sn',
  'paydunya',
];

/**
 * POST /v1/disbursements/send
 *
 * Initie un déboursement (envoi d'argent) vers un compte Mobile Money.
 *
 * Body:
 *   - accountAlias    {string}  Numéro téléphone sans indicatif pays (obligatoire)
 *   - amount          {number}  Montant en XOF (obligatoire)
 *   - withdrawMode    {string}  Opérateur cible (obligatoire)
 *   - callbackUrl     {string}  URL de callback personnalisée (optionnel)
 *   - debitAccountNumber {string} Compte marchand à débiter (optionnel, pour 'paydunya')
 *
 * IMPORTANT : L'API PUSH doit être activée dans le dashboard PayDunya.
 */
router.post('/send', async (req: Request, res: Response) => {
  const {
    accountAlias,
    amount,
    withdrawMode,
    callbackUrl,
    debitAccountNumber,
  } = req.body as {
    accountAlias?: string;
    amount?: number;
    withdrawMode?: string;
    callbackUrl?: string;
    debitAccountNumber?: string;
  };

  // Validation
  if (!accountAlias || typeof accountAlias !== 'string') {
    res.status(400).json({ error: 'Le champ "accountAlias" est requis (numéro de téléphone sans indicatif)' });
    return;
  }
  if (!amount || typeof amount !== 'number' || amount <= 0) {
    res.status(400).json({ error: 'Le champ "amount" est requis et doit être un nombre positif en XOF' });
    return;
  }
  if (!withdrawMode || !VALID_WITHDRAW_MODES.includes(withdrawMode as WithdrawMode)) {
    res.status(400).json({
      error: 'Le champ "withdrawMode" est invalide',
      validModes: VALID_WITHDRAW_MODES,
    });
    return;
  }

  try {
    const result = await initiateDisbursement({
      accountAlias,
      amount,
      withdrawMode: withdrawMode as WithdrawMode,
      callbackUrl,
      debitAccountNumber,
    });

    res.status(202).json({
      success: true,
      transactionId: result.transactionId,
      status: result.status,
      description: result.description,
    });
  } catch (error) {
    const message = error instanceof Error ? error.message : 'Erreur inconnue';
    console.error('[POST /disbursements/send]', message);
    res.status(502).json({ error: 'Erreur lors de l\'initiation du déboursement', detail: message });
  }
});

/**
 * GET /v1/disbursements/:transactionId/status
 *
 * Vérifie le statut d'un déboursement via son transaction_id.
 *
 * Statuts possibles : created | pending | success | failed
 */
router.get('/:transactionId/status', async (req: Request, res: Response) => {
  const transactionId = req.params.transactionId as string;

  if (!transactionId) {
    res.status(400).json({ error: 'transactionId manquant' });
    return;
  }

  try {
    const result = await getDisbursementStatus(transactionId);
    res.json({
      success: true,
      status: result.status,
      transactionId: result.transactionId,
      amount: result.amount,
      description: result.description,
    });
  } catch (error) {
    const message = error instanceof Error ? error.message : 'Erreur inconnue';
    console.error(`[GET /disbursements/${transactionId}/status]`, message);
    res.status(502).json({ error: 'Impossible de récupérer le statut du déboursement', detail: message });
  }
});

export default router;
