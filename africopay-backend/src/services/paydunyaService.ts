import axios from 'axios';
import { paydunyaConfig, paydunyaHeaders } from '../config/paydunya';

// ─── Types ────────────────────────────────────────────────────────────────────

/** Opérateurs Mobile Money supportés en mode sandbox */
export type WithdrawMode =
  | 'orange-money-senegal'
  | 'wave-senegal'
  | 'free-money-senegal'
  | 'expresso-sn'
  | 'mtn-benin'
  | 'moov-benin'
  | 'orange-money-ci'
  | 'wave-ci'
  | 'mtn-ci'
  | 'moov-ci'
  | 't-money-togo'
  | 'moov-togo'
  | 'orange-money-mali'
  | 'orange-money-burkina'
  | 'mtn-cameroun'
  | 'djamo-ci'
  | 'djamo-sn'
  | 'paydunya';

/** Canaux de paiement disponibles pour l'API PAR */
export type PaymentChannel =
  | 'card'
  | 'orange-money-senegal'
  | 'wave-senegal'
  | 'mtn-benin'
  | WithdrawMode;

export interface CustomerInfo {
  name: string;
  email?: string;
  phone?: string;
}

export interface InvoiceItem {
  name: string;
  quantity: number;
  unit_price: number;
  total_price: number;
  description?: string;
}

// ─── Paiement Avec Redirection (PAR) ─────────────────────────────────────────

export interface CreateInvoiceParams {
  totalAmount: number;          // Montant en XOF
  description: string;
  storeName?: string;
  customer?: CustomerInfo;
  channels?: PaymentChannel[];  // null = tous les canaux activés
  items?: Record<string, InvoiceItem>;
  returnUrl?: string;           // URL de retour après paiement réussi
  cancelUrl?: string;           // URL de retour après annulation
  callbackUrl?: string;         // IPN URL (override de la config)
  customData?: Record<string, unknown>;
}

export interface CreateInvoiceResult {
  token: string;
  checkoutUrl: string;
}

/**
 * Crée une facture de paiement PayDunya (API PAR).
 * Retourne le token et l'URL de la page de paiement sandbox.
 */
export async function createCheckoutInvoice(
  params: CreateInvoiceParams,
): Promise<CreateInvoiceResult> {
  const {
    totalAmount,
    description,
    storeName,
    customer,
    channels,
    items,
    returnUrl,
    cancelUrl,
    callbackUrl,
    customData,
  } = params;

  const body: Record<string, unknown> = {
    invoice: {
      total_amount: totalAmount,
      description,
      ...(customer && { customer }),
      ...(channels && { channels }),
      ...(items && { items }),
    },
    store: {
      name: storeName || paydunyaConfig.storeName,
      tagline: paydunyaConfig.storeTagline,
    },
    actions: {
      ...(returnUrl && { return_url: returnUrl }),
      ...(cancelUrl && { cancel_url: cancelUrl }),
      callback_url:
        callbackUrl || `${paydunyaConfig.callbackBaseUrl}/v1/webhooks/paydunya`,
    },
    ...(customData && { custom_data: customData }),
  };

  const endpoint = `${paydunyaConfig.baseUrl}/checkout-invoice/create`;

  const response = await axios.post<{
    response_code: string;
    response_text: string;
    token: string;
    description: string;
  }>(endpoint, body, { headers: paydunyaHeaders() });

  const data = response.data;

  if (data.response_code !== '00') {
    throw new Error(`PayDunya PAR error: ${data.response_text}`);
  }

  const checkoutBase =
    paydunyaConfig.mode === 'live'
      ? 'https://app.paydunya.com/checkout/invoice'
      : 'https://app.paydunya.com/sandbox-checkout/invoice';

  return {
    token: data.token,
    checkoutUrl: `${checkoutBase}/${data.token}`,
  };
}

// ─── Vérification Statut Paiement (PAR) ──────────────────────────────────────

export type PaydunyaInvoiceStatus =
  | 'pending'
  | 'completed'
  | 'cancelled'
  | 'failed';

export interface InvoiceStatusResult {
  status: PaydunyaInvoiceStatus;
  token: string;
  totalAmount: number;
  customer?: CustomerInfo;
  receiptUrl?: string;
}

/**
 * Vérifie le statut d'une facture PayDunya via son token.
 */
export async function getInvoiceStatus(
  token: string,
): Promise<InvoiceStatusResult> {
  const endpoint = `${paydunyaConfig.baseUrl}/checkout-invoice/confirm/${token}`;

  const response = await axios.get<{
    response_code: string;
    response_text: string;
    status: PaydunyaInvoiceStatus;
    invoice: {
      token: string;
      total_amount: number;
      customer?: CustomerInfo;
      receipt_url?: string;
    };
  }>(endpoint, { headers: paydunyaHeaders() });

  const data = response.data;

  if (data.response_code !== '00') {
    throw new Error(`PayDunya status error: ${data.response_text}`);
  }

  return {
    status: data.status,
    token: data.invoice.token,
    totalAmount: data.invoice.total_amount,
    customer: data.invoice.customer,
    receiptUrl: data.invoice.receipt_url,
  };
}

// ─── API PUSH (Déboursement) ──────────────────────────────────────────────────

export interface DisbursementParams {
  accountAlias: string;    // Numéro téléphone sans indicatif pays (ex: "771234567")
  amount: number;          // Montant en XOF
  withdrawMode: WithdrawMode;
  callbackUrl?: string;
  debitAccountNumber?: string; // Optionnel, uniquement pour withdraw_mode='paydunya'
}

export interface DisbursementResult {
  transactionId: string;
  status: 'created' | 'pending' | 'success' | 'failed';
  description: string;
}

/**
 * Initie un déboursement Mobile Money via l'API PUSH PayDunya.
 */
export async function initiateDisbursement(
  params: DisbursementParams,
): Promise<DisbursementResult> {
  const { accountAlias, amount, withdrawMode, callbackUrl, debitAccountNumber } = params;

  const body: Record<string, unknown> = {
    account_alias: accountAlias,
    amount,
    withdraw_mode: withdrawMode,
    callback_url:
      callbackUrl || `${paydunyaConfig.callbackBaseUrl}/v1/webhooks/paydunya`,
    ...(debitAccountNumber && { debit_account_number: debitAccountNumber }),
  };

  const endpoint = `${paydunyaConfig.baseUrl}/disburse/get-started`;

  const response = await axios.post<{
    response_code: string;
    response_text: string;
    description: string;
    transaction_id: string;
  }>(endpoint, body, { headers: paydunyaHeaders() });

  const data = response.data;

  if (data.response_code !== '00') {
    throw new Error(`PayDunya PUSH error: ${data.response_text}`);
  }

  return {
    transactionId: data.transaction_id,
    status: 'created',
    description: data.description,
  };
}

// ─── Vérification Statut Déboursement ────────────────────────────────────────

export type DisbursementStatus = 'created' | 'pending' | 'success' | 'failed';

export interface DisbursementStatusResult {
  status: DisbursementStatus;
  transactionId: string;
  amount?: number;
  description?: string;
}

/**
 * Vérifie le statut d'un déboursement via son transaction_id.
 */
export async function getDisbursementStatus(
  transactionId: string,
): Promise<DisbursementStatusResult> {
  const endpoint = `${paydunyaConfig.baseUrl}/disburse/check-status/${transactionId}`;

  const response = await axios.get<{
    response_code: string;
    response_text: string;
    status: DisbursementStatus;
    transaction_id: string;
    amount?: number;
    description?: string;
  }>(endpoint, { headers: paydunyaHeaders() });

  const data = response.data;

  if (data.response_code !== '00') {
    throw new Error(`PayDunya disbursement status error: ${data.response_text}`);
  }

  return {
    status: data.status,
    transactionId: data.transaction_id,
    amount: data.amount,
    description: data.description,
  };
}
