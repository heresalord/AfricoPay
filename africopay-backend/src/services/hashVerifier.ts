import crypto from 'crypto';
import { paydunyaConfig } from '../config/paydunya';

/**
 * Vérifie l'authenticité d'une notification IPN reçue de PayDunya.
 *
 * PayDunya inclut dans le payload un champ `hash` (SHA-512) calculé
 * à partir du MASTER KEY. On recompute ce hash et on compare.
 *
 * @param receivedHash - Valeur du champ `hash` reçu dans le payload IPN
 * @returns true si le hash est valide (notification authentique)
 */
export function verifyPaydunyaHash(receivedHash: string): boolean {
  const expectedHash = crypto
    .createHash('sha512')
    .update(paydunyaConfig.masterKey)
    .digest('hex');

  // Comparaison en temps constant pour éviter les timing attacks
  try {
    return crypto.timingSafeEqual(
      Buffer.from(receivedHash, 'hex'),
      Buffer.from(expectedHash, 'hex'),
    );
  } catch {
    return false;
  }
}
