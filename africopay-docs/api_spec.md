# AfricoPay — Contrats API REST

**Version :** 0.1.0
**Base URL :** `https://api.africopay.com/v1`
**Authentification :** `X-Device-ID` header + Bearer Token JWT (future)

---

## 1. Enregistrement d'Appareil

### POST `/devices/register`

Enregistre un nouveau terminal lors de sa première activation.

**Requête :**
```json
{
  "deviceId": "AFRICO-ZCS-001-2024",
  "manufacturer": "ZCS",
  "model": "Z90",
  "androidVersion": 13,
  "appVersion": "0.1.0",
  "merchantId": "MERCH-12345",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

**Réponse 201 :**
```json
{
  "success": true,
  "deviceToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "activatedAt": "2024-01-15T10:30:05Z",
  "config": {
    "syncIntervalMinutes": 30,
    "featureFlagsUrl": "/v1/flags"
  }
}
```

**Réponse 409 (déjà enregistré) :**
```json
{
  "success": false,
  "error": "DEVICE_ALREADY_REGISTERED",
  "message": "Ce terminal est déjà enregistré."
}
```

---

## 2. Configuration Distante

### GET `/config`

Récupère la configuration complète pour un terminal.

**Headers :**
```
X-Device-ID: AFRICO-ZCS-001-2024
Authorization: Bearer <token>
```

**Réponse 200 :**
```json
{
  "merchantName": "Boutique Koffi",
  "merchantId": "MERCH-12345",
  "currency": "XOF",
  "language": "fr",
  "receiptFooter": "Merci pour votre achat !",
  "simulationMode": true,
  "enabledPaymentMethods": ["NFC", "EMV", "MOBILE_MONEY", "CASH"],
  "mobileMoneyProviders": ["MTN", "MOOV", "ORANGE", "WAVE"],
  "riskParameters": {
    "maxTransactionAmountXof": 500000,
    "requirePinAboveXof": 25000
  },
  "updatedAt": "2024-01-15T08:00:00Z"
}
```

---

## 3. Feature Flags

### GET `/flags`

Récupère les feature flags actifs pour un terminal.

**Headers :**
```
X-Device-ID: AFRICO-ZCS-001-2024
Authorization: Bearer <token>
```

**Réponse 200 :**
```json
{
  "flags": {
    "enable_nfc": true,
    "enable_emv": true,
    "enable_mag_stripe": true,
    "enable_qr_code": false,
    "enable_mobile_money": true,
    "enable_cash": true,
    "enable_google_pay": false,
    "enable_simulation_mode": true,
    "enable_receipt_printing": true,
    "enable_cloud_sync": false,
    "enable_auto_update": false,
    "enable_dashboard": true,
    "enable_diagnostics": true
  },
  "updatedAt": "2024-01-15T08:00:00Z",
  "expiresAt": "2024-01-15T14:00:00Z"
}
```

---

## 4. Vérification Mise à Jour

### GET `/updates/check`

Vérifie si une nouvelle version de l'APK est disponible.

**Paramètres :**
```
?version=0.1.0&platform=android&arch=arm64
```

**Headers :**
```
X-Device-ID: AFRICO-ZCS-001-2024
Authorization: Bearer <token>
```

**Réponse 200 (mise à jour disponible) :**
```json
{
  "updateAvailable": true,
  "latestVersion": "0.2.0",
  "releaseNotes": "Améliorations de performance et corrections de bugs.",
  "apkUrl": "https://cdn.africopay.com/releases/africopay-0.2.0-release.apk",
  "apkSha256": "a1b2c3d4e5f6...",
  "mandatory": false,
  "releasedAt": "2024-01-20T09:00:00Z"
}
```

**Réponse 200 (à jour) :**
```json
{
  "updateAvailable": false,
  "latestVersion": "0.1.0"
}
```

---

## 5. Synchronisation des Transactions

### POST `/transactions/sync`

Upload les transactions stockées localement vers le backend.

**Headers :**
```
X-Device-ID: AFRICO-ZCS-001-2024
Authorization: Bearer <token>
Content-Type: application/json
```

**Requête :**
```json
{
  "deviceId": "AFRICO-ZCS-001-2024",
  "transactions": [
    {
      "localId": "LOCAL-UUID-001",
      "amount": 15000,
      "currency": "XOF",
      "paymentMethod": "NFC",
      "status": "APPROVED",
      "timestamp": "2024-01-15T10:35:00Z",
      "receiptNumber": "REC-20240115-001",
      "terminalId": "AFRICO-ZCS-001-2024",
      "merchantId": "MERCH-12345",
      "isSimulated": true
    }
  ]
}
```

**Réponse 200 :**
```json
{
  "syncedCount": 1,
  "failedCount": 0,
  "results": [
    {
      "localId": "LOCAL-UUID-001",
      "serverId": "TXN-SERVER-20240115-001",
      "status": "SYNCED"
    }
  ]
}
```

---

## 6. Rapport de Diagnostic

### POST `/diagnostics`

Envoie un rapport de diagnostic matériel du terminal.

**Requête :**
```json
{
  "deviceId": "AFRICO-ZCS-001-2024",
  "timestamp": "2024-01-15T10:30:00Z",
  "hardware": {
    "androidVersion": 13,
    "manufacturer": "ZCS",
    "model": "Z90",
    "cpu": "Qualcomm Snapdragon 429",
    "ramMb": 2048,
    "storageGb": 16,
    "batteryLevel": 85,
    "batteryCharging": false
  },
  "peripherals": {
    "nfc": { "available": true, "enabled": true },
    "emv": { "available": true, "driverVersion": "2.1.0" },
    "magStripe": { "available": true },
    "printer": { "available": true, "paperStatus": "OK" },
    "camera": { "available": true },
    "scanner": { "available": false },
    "gps": { "available": true },
    "bluetooth": { "available": true, "enabled": false },
    "wifi": { "available": true, "connected": true },
    "network": { "type": "WIFI", "signalStrength": -65 }
  },
  "appVersion": "0.1.0",
  "dbVersion": 1
}
```

**Réponse 200 :**
```json
{
  "received": true,
  "diagnosticId": "DIAG-20240115-001"
}
```

---

## 7. Codes d'Erreur

| Code | Description |
|------|-------------|
| `DEVICE_NOT_REGISTERED` | Terminal non enregistré |
| `DEVICE_ALREADY_REGISTERED` | Terminal déjà enregistré |
| `INVALID_TOKEN` | Token d'authentification invalide |
| `TOKEN_EXPIRED` | Token expiré |
| `MERCHANT_NOT_FOUND` | Marchand introuvable |
| `CONFIG_NOT_FOUND` | Configuration non trouvée |
| `SYNC_PARTIAL_FAILURE` | Synchronisation partiellement échouée |
| `RATE_LIMITED` | Trop de requêtes |
| `MAINTENANCE` | Serveur en maintenance |

**Format d'erreur standard :**
```json
{
  "success": false,
  "error": "INVALID_TOKEN",
  "message": "Le token d'authentification est invalide ou expiré.",
  "timestamp": "2024-01-15T10:35:00Z"
}
```

---

## 8. Codes Statut HTTP

| Statut | Usage |
|--------|-------|
| `200 OK` | Succès |
| `201 Created` | Ressource créée |
| `400 Bad Request` | Requête malformée |
| `401 Unauthorized` | Non authentifié |
| `403 Forbidden` | Accès interdit |
| `404 Not Found` | Ressource introuvable |
| `409 Conflict` | Conflit (ex: déjà enregistré) |
| `429 Too Many Requests` | Rate limiting |
| `500 Internal Server Error` | Erreur serveur |
| `503 Service Unavailable` | Maintenance |
