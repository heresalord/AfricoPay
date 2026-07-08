import path from 'path';
import dotenv from 'dotenv';

// Charge le .env localement d'abord, puis dans le dossier parent (racine du projet)
dotenv.config();
dotenv.config({ path: path.resolve(process.cwd(), '../.env') });
import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import morgan from 'morgan';
import rateLimit from 'express-rate-limit';

import { apiKeyAuth } from './middleware/auth';
import paymentsRouter from './routes/payments';
import disbursementsRouter from './routes/disbursements';
import webhooksRouter from './routes/webhooks';

const app = express();
const PORT = process.env.PORT || 3000;

// ─── Middlewares globaux ───────────────────────────────────────────────────────

app.use(helmet());
app.use(cors());
app.use(morgan('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: true })); // Pour le format IPN application/x-www-form-urlencoded

// Rate limiting global : 100 req/min par IP
app.use(
  rateLimit({
    windowMs: 60 * 1000,
    max: 100,
    message: { error: 'Trop de requêtes — réessayez dans une minute' },
    standardHeaders: true,
    legacyHeaders: false,
  }),
);

// ─── Health Check (non protégé) ───────────────────────────────────────────────

app.get('/health', (_req, res) => {
  res.json({
    status: 'ok',
    service: 'africopay-backend',
    paydunya_mode: process.env.PAYDUNYA_MODE || 'sandbox',
    timestamp: new Date().toISOString(),
  });
});

// ─── Routes Protégées (API Key requis) ────────────────────────────────────────

app.use('/v1/payments', apiKeyAuth, paymentsRouter);
app.use('/v1/disbursements', apiKeyAuth, disbursementsRouter);

// ─── Webhook IPN (non protégé par API Key — appelé par PayDunya) ──────────────
// La sécurité est assurée par la vérification du hash SHA-512

app.use('/v1/webhooks/paydunya', webhooksRouter);

// ─── 404 Handler ──────────────────────────────────────────────────────────────

app.use((_req, res) => {
  res.status(404).json({ error: 'Endpoint introuvable' });
});

// ─── Démarrage ────────────────────────────────────────────────────────────────

app.listen(PORT, () => {
  console.log(`\n🚀 AfricoPay Backend démarré`);
  console.log(`   → Port     : ${PORT}`);
  console.log(`   → Mode     : ${process.env.NODE_ENV || 'development'}`);
  console.log(`   → PayDunya : ${process.env.PAYDUNYA_MODE || 'sandbox'}`);
  console.log(`   → Health   : http://localhost:${PORT}/health\n`);
});

export default app;
