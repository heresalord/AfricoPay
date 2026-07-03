# AfricoPay — Schéma de Base de Données

**Version :** 0.1.0
**Moteur :** Room (SQLite avec SQLCipher)
**Version DB :** 1

---

## 1. Table : `transactions`

Stocke chaque transaction de paiement effectuée sur le terminal.

```sql
CREATE TABLE transactions (
    id              TEXT PRIMARY KEY NOT NULL,       -- UUID local
    server_id       TEXT,                            -- ID après sync backend
    amount          INTEGER NOT NULL,                -- Montant en centimes XOF
    currency        TEXT NOT NULL DEFAULT 'XOF',
    payment_method  TEXT NOT NULL,                   -- NFC, EMV, MAG_STRIPE, QR, MOBILE_MONEY, CASH
    mobile_provider TEXT,                            -- MTN, MOOV, ORANGE, WAVE (si Mobile Money)
    status          TEXT NOT NULL,                   -- APPROVED, DECLINED, TIMEOUT, etc.
    decline_reason  TEXT,                            -- Raison si refusé
    timestamp       INTEGER NOT NULL,                -- Unix timestamp millis
    receipt_number  TEXT NOT NULL,
    terminal_id     TEXT NOT NULL,
    merchant_id     TEXT NOT NULL,
    is_simulated    INTEGER NOT NULL DEFAULT 1,      -- 1 = simulation, 0 = réel
    is_synced       INTEGER NOT NULL DEFAULT 0,      -- 1 = synchronisé avec backend
    synced_at       INTEGER,                         -- Unix timestamp de sync
    created_at      INTEGER NOT NULL,
    updated_at      INTEGER NOT NULL
);

CREATE INDEX idx_transactions_timestamp ON transactions(timestamp DESC);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_payment_method ON transactions(payment_method);
CREATE INDEX idx_transactions_is_synced ON transactions(is_synced);
```

**Valeurs `payment_method` :**

| Valeur | Description |
|--------|-------------|
| `NFC` | Carte sans contact |
| `EMV` | Carte à puce |
| `MAG_STRIPE` | Bande magnétique |
| `QR_CODE` | QR Code |
| `MOBILE_MONEY` | Mobile Money |
| `CASH` | Espèces |

**Valeurs `status` :**

| Valeur | Description |
|--------|-------------|
| `APPROVED` | Approuvée |
| `DECLINED` | Refusée |
| `TIMEOUT` | Délai dépassé |
| `NETWORK_ERROR` | Erreur réseau |
| `CANCELLED` | Annulée |
| `CARD_EXPIRED` | Carte expirée |
| `INSUFFICIENT_FUNDS` | Fonds insuffisants |
| `ISSUER_OFFLINE` | Émetteur hors ligne |
| `UNKNOWN_ERROR` | Erreur inconnue |

---

## 2. Table : `receipts`

Stocke le contenu détaillé des reçus imprimables.

```sql
CREATE TABLE receipts (
    id                TEXT PRIMARY KEY NOT NULL,     -- UUID
    transaction_id    TEXT NOT NULL,                 -- FK → transactions.id
    receipt_number    TEXT NOT NULL UNIQUE,
    merchant_name     TEXT NOT NULL,
    merchant_address  TEXT,
    merchant_phone    TEXT,
    merchant_id       TEXT NOT NULL,
    terminal_id       TEXT NOT NULL,
    payment_method    TEXT NOT NULL,
    amount            INTEGER NOT NULL,              -- En centimes
    currency          TEXT NOT NULL DEFAULT 'XOF',
    status            TEXT NOT NULL,
    date_time         INTEGER NOT NULL,              -- Unix timestamp
    footer_text       TEXT,
    is_simulated      INTEGER NOT NULL DEFAULT 1,
    printed_count     INTEGER NOT NULL DEFAULT 0,
    last_printed_at   INTEGER,
    created_at        INTEGER NOT NULL,

    FOREIGN KEY (transaction_id) REFERENCES transactions(id)
);
```

---

## 3. Table : `merchant_profile`

Profil unique du marchand configuré sur ce terminal.

```sql
CREATE TABLE merchant_profile (
    id                TEXT PRIMARY KEY NOT NULL DEFAULT 'SINGLETON',
    merchant_name     TEXT NOT NULL DEFAULT '',
    merchant_id       TEXT NOT NULL DEFAULT '',
    store_address     TEXT NOT NULL DEFAULT '',
    phone             TEXT NOT NULL DEFAULT '',
    currency          TEXT NOT NULL DEFAULT 'XOF',
    language          TEXT NOT NULL DEFAULT 'fr',
    receipt_footer    TEXT NOT NULL DEFAULT 'Merci pour votre achat !',
    simulation_mode   INTEGER NOT NULL DEFAULT 1,   -- 1 = activé
    admin_pin_hash    TEXT NOT NULL DEFAULT '',      -- SHA-256 du PIN admin
    merchant_pin_hash TEXT NOT NULL DEFAULT '',      -- SHA-256 du PIN marchand
    terminal_id       TEXT NOT NULL DEFAULT '',
    app_version       TEXT NOT NULL DEFAULT '0.1.0',
    db_version        INTEGER NOT NULL DEFAULT 1,
    updated_at        INTEGER NOT NULL
);
```

---

## 4. Table : `hardware_diagnostics`

Historique des rapports de diagnostic matériel.

```sql
CREATE TABLE hardware_diagnostics (
    id                  TEXT PRIMARY KEY NOT NULL,   -- UUID
    timestamp           INTEGER NOT NULL,
    android_version     INTEGER NOT NULL,
    manufacturer        TEXT NOT NULL,
    model               TEXT NOT NULL,
    cpu                 TEXT,
    ram_mb              INTEGER,
    storage_gb          INTEGER,
    battery_level       INTEGER,
    battery_charging    INTEGER,                     -- 1 = en charge
    has_nfc             INTEGER NOT NULL DEFAULT 0,
    nfc_enabled         INTEGER NOT NULL DEFAULT 0,
    has_emv             INTEGER NOT NULL DEFAULT 0,
    emv_driver_version  TEXT,
    has_mag_stripe      INTEGER NOT NULL DEFAULT 0,
    has_printer         INTEGER NOT NULL DEFAULT 0,
    printer_paper_ok    INTEGER NOT NULL DEFAULT 0,
    has_camera          INTEGER NOT NULL DEFAULT 0,
    has_scanner         INTEGER NOT NULL DEFAULT 0,
    has_gps             INTEGER NOT NULL DEFAULT 0,
    has_bluetooth       INTEGER NOT NULL DEFAULT 0,
    bluetooth_enabled   INTEGER NOT NULL DEFAULT 0,
    has_wifi            INTEGER NOT NULL DEFAULT 0,
    wifi_connected      INTEGER NOT NULL DEFAULT 0,
    network_type        TEXT,                        -- WIFI, MOBILE, NONE
    signal_strength     INTEGER,
    created_at          INTEGER NOT NULL
);

CREATE INDEX idx_diagnostics_timestamp ON hardware_diagnostics(timestamp DESC);
```

---

## 5. Table : `configuration_cache`

Cache de la configuration distante (offline-first).

```sql
CREATE TABLE configuration_cache (
    key         TEXT PRIMARY KEY NOT NULL,
    value       TEXT NOT NULL,                       -- JSON sérialisé
    fetched_at  INTEGER NOT NULL,
    expires_at  INTEGER NOT NULL
);
```

**Clés standards :**

| Clé | Contenu |
|-----|---------|
| `remote_config` | Configuration complète du marchand |
| `feature_flags` | Feature flags JSON |
| `update_info` | Informations dernière vérification MAJ |

---

## 6. Table : `sync_queue`

File d'attente pour la synchronisation des transactions offline.

```sql
CREATE TABLE sync_queue (
    id              TEXT PRIMARY KEY NOT NULL,
    transaction_id  TEXT NOT NULL,
    attempts        INTEGER NOT NULL DEFAULT 0,
    last_attempt    INTEGER,
    next_attempt    INTEGER,
    status          TEXT NOT NULL DEFAULT 'PENDING',  -- PENDING, FAILED, DONE
    error_message   TEXT,
    created_at      INTEGER NOT NULL,

    FOREIGN KEY (transaction_id) REFERENCES transactions(id)
);

CREATE INDEX idx_sync_queue_status ON sync_queue(status);
CREATE INDEX idx_sync_queue_next_attempt ON sync_queue(next_attempt);
```

---

## 7. Room Database — Entity Kotlin

### TransactionEntity.kt
```kotlin
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val serverId: String? = null,
    val amount: Long,
    val currency: String = "XOF",
    val paymentMethod: String,
    val mobileProvider: String? = null,
    val status: String,
    val declineReason: String? = null,
    val timestamp: Long,
    val receiptNumber: String,
    val terminalId: String,
    val merchantId: String,
    val isSimulated: Boolean = true,
    val isSynced: Boolean = false,
    val syncedAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long
)
```

### MerchantProfileEntity.kt
```kotlin
@Entity(tableName = "merchant_profile")
data class MerchantProfileEntity(
    @PrimaryKey val id: String = "SINGLETON",
    val merchantName: String = "",
    val merchantId: String = "",
    val storeAddress: String = "",
    val phone: String = "",
    val currency: String = "XOF",
    val language: String = "fr",
    val receiptFooter: String = "Merci pour votre achat !",
    val simulationMode: Boolean = true,
    val adminPinHash: String = "",
    val merchantPinHash: String = "",
    val terminalId: String = "",
    val appVersion: String = "0.1.0",
    val dbVersion: Int = 1,
    val updatedAt: Long
)
```

---

## 8. Migrations Room

```kotlin
// Migrations futures : exemple de v1 → v2
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Ex: ajout d'une colonne exchange_rate sur transactions
        database.execSQL(
            "ALTER TABLE transactions ADD COLUMN exchange_rate REAL DEFAULT 1.0"
        )
    }
}
```

---

## 9. Politique de Rétention

| Table | Durée de conservation |
|-------|----------------------|
| `transactions` | 12 mois (configurable) |
| `receipts` | 12 mois (configurable) |
| `hardware_diagnostics` | 30 jours |
| `configuration_cache` | Jusqu'à expiration |
| `sync_queue` | Purge après sync réussie |

Un job périodique (WorkManager) nettoie les entrées expirées chaque nuit à 02h00.
