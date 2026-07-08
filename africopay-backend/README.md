# AfricoPay Backend

Passerelle sécurisée entre l'application Android AfricoPay POS et l'API PayDunya.

## Stack

- **Runtime** : Node.js v22+
- **Langage** : TypeScript 5
- **Framework** : Express 4
- **HTTP Client** : Axios (appels PayDunya)

## Structure

```
src/
├── config/
│   └── paydunya.ts         # Clés API & config sandbox/live
├── services/
│   ├── paydunyaService.ts  # Appels API PayDunya (PAR + PUSH)
│   └── hashVerifier.ts     # Vérification hash IPN (SHA-512)
├── routes/
│   ├── payments.ts         # POST /v1/payments/initiate | GET /:token/status
│   ├── disbursements.ts    # POST /v1/disbursements/send | GET /:id/status
│   └── webhooks.ts         # POST /v1/webhooks/paydunya (IPN)
├── middleware/
│   └── auth.ts             # API Key (X-API-KEY)
└── index.ts                # Entry point Express
```

## Démarrage

```bash
# 1. Cloner et installer
npm install

# 2. Configurer les variables d'environnement
cp .env.example .env
# → Renseigner les clés PayDunya sandbox et CALLBACK_BASE_URL

# 3. Démarrer en développement (hot-reload)
npm run dev

# 4. (Optionnel) Exposer l'IPN avec ngrok
npx ngrok http 3000
# → Copier l'URL ngrok dans .env (CALLBACK_BASE_URL)
```

## Endpoints

| Méthode | Endpoint | Auth | Description |
|---------|----------|------|-------------|
| `GET` | `/health` | ❌ | Health check |
| `POST` | `/v1/payments/initiate` | ✅ X-API-KEY | Créer une facture PayDunya |
| `GET` | `/v1/payments/:token/status` | ✅ X-API-KEY | Statut d'un paiement |
| `POST` | `/v1/disbursements/send` | ✅ X-API-KEY | Déboursement Mobile Money |
| `GET` | `/v1/disbursements/:id/status` | ✅ X-API-KEY | Statut d'un déboursement |
| `POST` | `/v1/webhooks/paydunya` | ❌ (hash SHA-512) | Notification IPN PayDunya |

## Tests Rapides (Sandbox)

```bash
# Health check
curl http://localhost:3000/health

# Initier un paiement (5000 XOF)
curl -X POST http://localhost:3000/v1/payments/initiate \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: changeme_africopay_internal_key" \
  -d '{"amount": 5000, "description": "Test AfricoPay POS"}'

# Déboursement vers Wave Sénégal
curl -X POST http://localhost:3000/v1/disbursements/send \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: changeme_africopay_internal_key" \
  -d '{"accountAlias": "771234567", "amount": 2000, "withdrawMode": "wave-senegal"}'
```

## Opérateurs supportés (sandbox)

Canaux de paiement PAR (prioritaires) : `card`, `wave-senegal`, `orange-money-senegal`, `mtn-benin`

Modes de retrait PUSH disponibles : voir `src/routes/disbursements.ts → VALID_WITHDRAW_MODES`

## Variables d'Environnement

Voir [.env.example](.env.example) pour la liste complète.
