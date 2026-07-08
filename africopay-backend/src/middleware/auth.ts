import { Request, Response, NextFunction } from 'express';

/**
 * Middleware d'authentification par API Key pour les appels internes.
 *
 * L'app Android doit inclure l'en-tête :
 *   X-API-KEY: <AFRICOPAY_API_KEY>
 *
 * La valeur est définie dans la variable d'environnement AFRICOPAY_API_KEY.
 */
export function apiKeyAuth(req: Request, res: Response, next: NextFunction): void {
  const apiKey = req.headers['x-api-key'];
  const expectedKey = process.env.AFRICOPAY_API_KEY;

  if (!expectedKey) {
    console.error('[Auth] AFRICOPAY_API_KEY non définie dans les variables d\'environnement');
    res.status(500).json({ error: 'Configuration serveur incorrecte' });
    return;
  }

  if (!apiKey || apiKey !== expectedKey) {
    res.status(401).json({ error: 'Non autorisé — clé API invalide ou manquante' });
    return;
  }

  next();
}
