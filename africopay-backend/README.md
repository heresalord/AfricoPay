# AfricoPay Backend

Backend API pour AfricoPay POS — Configuration distante, Feature Flags, Enregistrement des appareils, Gestion des marchands.

## Stack (à définir selon votre choix)

Options disponibles :
- **Supabase / PostgreSQL** (recommandé — compatible avec vos workflows SQL existants)
- **Kotlin Ktor** (langage partagé avec l'application Android)
- **Node.js TypeScript + Express/NestJS**

## Structure Prévue

```
africopay-backend/
├── src/
│   ├── routes/
│   │   ├── devices.ts       # POST /devices/register
│   │   ├── config.ts        # GET  /config
│   │   ├── flags.ts         # GET  /flags
│   │   ├── updates.ts       # GET  /updates/check
│   │   └── transactions.ts  # POST /transactions/sync
│   ├── middleware/
│   │   ├── auth.ts
│   │   └── rateLimiter.ts
│   ├── models/
│   └── utils/
├── supabase/
│   ├── migrations/
│   └── functions/
├── package.json
├── tsconfig.json
├── Dockerfile
└── .env.example
```

## API Endpoints

Voir `/africopay-docs/api_spec.md` pour les contrats complets.

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/v1/devices/register` | Enregistrement terminal |
| GET | `/v1/config` | Configuration marchands |
| GET | `/v1/flags` | Feature flags |
| GET | `/v1/updates/check` | Vérification MAJ |
| POST | `/v1/transactions/sync` | Sync transactions |
| POST | `/v1/diagnostics` | Rapport diagnostic |

## Démarrage Rapide

```bash
# Cloner et installer
npm install

# Variables d'environnement
cp .env.example .env

# Démarrer en développement
npm run dev

# Build production
npm run build && npm start
```

## Variables d'Environnement

```env
DATABASE_URL=postgresql://...
JWT_SECRET=...
PORT=3000
NODE_ENV=development
AFRICOPAY_API_KEY=...
```
