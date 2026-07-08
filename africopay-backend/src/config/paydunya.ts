/**
 * Configuration PayDunya.
 *
 * Charge les clés API depuis les variables d'environnement.
 * Mode 'sandbox' → endpoints de test PayDunya.
 * Mode 'live'    → endpoints de production PayDunya.
 */

export type PaydunyaMode = 'sandbox' | 'live';

export interface PaydunyaConfig {
  mode: PaydunyaMode;
  masterKey: string;
  privateKey: string;
  publicKey: string;
  token: string;
  baseUrl: string;
  callbackBaseUrl: string;
  storeName: string;
  storeTagline: string;
}

function requireEnv(key: string): string {
  const value = process.env[key];
  if (!value) {
    throw new Error(`[PayDunya Config] Variable d'environnement manquante : ${key}`);
  }
  return value;
}

const mode = (process.env.PAYDUNYA_MODE || 'sandbox') as PaydunyaMode;

export const paydunyaConfig: PaydunyaConfig = {
  mode,
  masterKey: requireEnv('PAYDUNYA_MASTER_KEY'),
  privateKey: requireEnv('PAYDUNYA_PRIVATE_KEY'),
  publicKey: requireEnv('PAYDUNYA_PUBLIC_KEY'),
  token: requireEnv('PAYDUNYA_TOKEN'),
  baseUrl:
    mode === 'live'
      ? 'https://app.paydunya.com/api/v1'
      : 'https://app.paydunya.com/sandbox-api/v1',
  callbackBaseUrl: requireEnv('CALLBACK_BASE_URL'),
  storeName: process.env.STORE_NAME || 'AfricoPay POS',
  storeTagline: process.env.STORE_TAGLINE || 'Votre solution de paiement mobile',
};

/**
 * Headers HTTP requis pour chaque appel à l'API PayDunya.
 */
export function paydunyaHeaders(): Record<string, string> {
  return {
    'Content-Type': 'application/json',
    'PAYDUNYA-MASTER-KEY': paydunyaConfig.masterKey,
    'PAYDUNYA-PRIVATE-KEY': paydunyaConfig.privateKey,
    'PAYDUNYA-TOKEN': paydunyaConfig.token,
  };
}
