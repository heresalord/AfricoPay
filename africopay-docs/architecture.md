# AfricoPay — Architecture Technique

**Version :** 0.1.0

---

## 1. Vue d'Ensemble

AfricoPay adopte une **Clean Architecture** combinée au pattern **MVVM** (Model-View-ViewModel) pour assurer une séparation claire des responsabilités, une testabilité maximale et une évolutivité à long terme.

```
┌─────────────────────────────────────────────┐
│           PRESENTATION LAYER                │
│  Jetpack Compose UI + ViewModels (MVVM)     │
├─────────────────────────────────────────────┤
│              DOMAIN LAYER                   │
│  Use Cases · Entities · Repository Interfaces│
├─────────────────────────────────────────────┤
│               DATA LAYER                    │
│  Room DB · Retrofit · DataStore · HAL Impl  │
├─────────────────────────────────────────────┤
│   HARDWARE ABSTRACTION LAYER (HAL)          │
│  Interfaces manufacturiers indépendantes    │
└─────────────────────────────────────────────┘
```

---

## 2. Structure des Packages

```
com.africopay.pos/
├── core/
│   ├── di/                      # Modules Hilt
│   ├── util/                    # Extensions, constantes
│   ├── logging/                 # Timber configuration
│   └── security/                # Chiffrement PIN, DB
│
├── hal/                         # Hardware Abstraction Layer
│   ├── interfaces/
│   │   ├── PrinterService.kt
│   │   ├── NfcService.kt
│   │   ├── EmvService.kt
│   │   ├── MagStripeService.kt
│   │   ├── ScannerService.kt
│   │   └── CameraService.kt
│   ├── mock/                    # Simulateurs v0.1.0
│   ├── sunmi/                   # Implémentation Sunmi
│   ├── pax/                     # Implémentation PAX
│   ├── zcs/                     # Implémentation ZCS
│   ├── newland/                 # Implémentation Newland
│   └── morefun/                 # Implémentation MoreFun
│
├── data/
│   ├── local/
│   │   ├── db/                  # Room Database
│   │   │   ├── AfricoPayDatabase.kt
│   │   │   ├── dao/
│   │   │   └── entity/
│   │   └── datastore/           # Préférences marchands
│   ├── remote/
│   │   ├── api/                 # Retrofit services
│   │   └── dto/                 # Data Transfer Objects
│   └── repository/              # Implémentations Repository
│
├── domain/
│   ├── model/                   # Entités métier
│   ├── repository/              # Interfaces Repository
│   └── usecase/
│       ├── payment/
│       ├── hardware/
│       ├── transaction/
│       └── config/
│
└── presentation/
    ├── navigation/              # Navigation Compose
    ├── theme/                   # Design System
    ├── home/                    # Écran d'accueil
    ├── payment/                 # Flux de paiement
    ├── receipt/                 # Reçu
    ├── history/                 # Historique
    ├── dashboard/               # Tableau de bord
    ├── diagnostics/             # Diagnostic matériel
    └── settings/                # Paramètres
```

---

## 3. Hardware Abstraction Layer (HAL)

### 3.1 Principe

L'UI ne communique **jamais** directement avec les SDKs des fabricants. Toute interaction matérielle passe par une interface définie dans la couche HAL.

```
ViewModel → UseCase → HAL Interface → Implémentation Fabricant
```

### 3.2 Interfaces HAL

#### PrinterService
```kotlin
interface PrinterService {
    fun isAvailable(): Boolean
    fun getStatus(): PrinterStatus
    suspend fun print(receipt: Receipt): PrintResult
    fun getPaperStatus(): PaperStatus
}
```

#### NfcService
```kotlin
interface NfcService {
    fun isAvailable(): Boolean
    fun isEnabled(): Boolean
    fun startListening(): Flow<NfcEvent>
    fun stopListening()
    suspend fun processCard(cardData: NfcCardData): PaymentResult
}
```

#### EmvService
```kotlin
interface EmvService {
    fun isAvailable(): Boolean
    fun startReading(): Flow<EmvEvent>
    suspend fun processCard(cardData: EmvCardData): PaymentResult
    fun cancelReading()
}
```

#### MagStripeService
```kotlin
interface MagStripeService {
    fun isAvailable(): Boolean
    fun startReading(): Flow<MagStripeEvent>
    suspend fun processCard(cardData: MagStripeData): PaymentResult
}
```

#### ScannerService
```kotlin
interface ScannerService {
    fun isAvailable(): Boolean
    fun startScanning(): Flow<ScanResult>
    fun stopScanning()
}
```

#### CameraService
```kotlin
interface CameraService {
    fun isAvailable(): Boolean
    fun startQrScanning(): Flow<QrResult>
    fun stopScanning()
}
```

### 3.3 Implémentation Mock (v0.1.0)

En version 0.1.0, toutes les interfaces HAL sont implémentées par des simulateurs configurables :

```kotlin
class MockNfcService @Inject constructor(
    private val simulationConfig: SimulationConfig
) : NfcService {
    override suspend fun processCard(cardData: NfcCardData): PaymentResult {
        delay(simulationConfig.processingDelayMs)
        return simulationConfig.nextResult()
    }
}
```

### 3.4 Détection du Fabricant

Au démarrage, AfricoPay détecte le fabricant via `Build.MANUFACTURER` et injecte l'implémentation correcte via Hilt :

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object HalModule {
    @Provides
    fun provideNfcService(
        @ApplicationContext context: Context,
        simulationConfig: SimulationConfig
    ): NfcService = when (Build.MANUFACTURER.lowercase()) {
        "sunmi"   -> SunmiNfcService(context)
        "pax"     -> PaxNfcService(context)
        "zcs"     -> ZcsNfcService(context)
        "newland" -> NewlandNfcService(context)
        else      -> MockNfcService(simulationConfig)
    }
}
```

---

## 4. Couche Domaine

### 4.1 Cas d'Utilisation Principaux

| Use Case | Description |
|----------|-------------|
| `ProcessPaymentUseCase` | Orchestration du flux de paiement complet |
| `GetAvailablePaymentMethodsUseCase` | Filtrage des méthodes selon le matériel |
| `SimulateTransactionUseCase` | Simulation configurable |
| `GetHardwareStatusUseCase` | Détection et statut des composants |
| `PrintReceiptUseCase` | Impression / affichage du reçu |
| `SaveTransactionUseCase` | Persistance locale |
| `GetTransactionHistoryUseCase` | Récupération + filtrage |
| `GetMerchantProfileUseCase` | Profil marchand |
| `UpdateMerchantSettingsUseCase` | Modification des paramètres |
| `SyncConfigurationUseCase` | Sync configuration distante |
| `CheckForUpdateUseCase` | Vérification mises à jour |

### 4.2 Entités Domaine

```kotlin
data class Transaction(
    val id: String,
    val amount: Long,           // En centimes (XOF)
    val currency: String,       // "XOF"
    val paymentMethod: PaymentMethod,
    val status: TransactionStatus,
    val timestamp: Instant,
    val receiptNumber: String,
    val terminalId: String,
    val merchantId: String,
    val isSimulated: Boolean
)

data class HardwareCapabilities(
    val hasNfc: Boolean,
    val hasEmv: Boolean,
    val hasMagStripe: Boolean,
    val hasCamera: Boolean,
    val hasScanner: Boolean,
    val hasPrinter: Boolean,
    val hasGps: Boolean,
    val androidVersion: Int,
    val manufacturer: String,
    val model: String
)
```

---

## 5. Couche Données

### 5.1 Room Database

```
AfricoPayDatabase (version 1)
├── transaction_table
├── receipt_table
├── hardware_diagnostic_table
├── configuration_cache_table
└── merchant_profile_table
```

### 5.2 Retrofit API Service

```kotlin
interface AfricoPayApiService {
    @GET("config")
    suspend fun getRemoteConfig(@Header("X-Device-ID") deviceId: String): ConfigResponse

    @GET("flags")
    suspend fun getFeatureFlags(@Header("X-Device-ID") deviceId: String): FeatureFlagsResponse

    @POST("devices/register")
    suspend fun registerDevice(@Body request: DeviceRegistrationRequest): DeviceRegistrationResponse

    @GET("updates/check")
    suspend fun checkForUpdate(@Query("version") currentVersion: String): UpdateCheckResponse

    @POST("transactions/sync")
    suspend fun syncTransactions(@Body transactions: List<TransactionDto>): SyncResponse
}
```

---

## 6. Navigation

```
NavGraph
├── HOME → PAYMENT_METHODS
├── PAYMENT_METHODS → PROCESSING
├── PROCESSING → RESULT
├── RESULT → RECEIPT
├── RECEIPT → HOME
├── HOME → HISTORY
├── HOME → DASHBOARD
├── HOME → DIAGNOSTICS
└── HOME → SETTINGS (PIN protégé)
```

---

## 7. Flux de Paiement Complet

```
HomeScreen
  │ Saisie montant
  ↓
PaymentMethodsScreen
  │ GetAvailablePaymentMethodsUseCase
  │ → Filtre selon HardwareCapabilities
  ↓
PaymentScreen (NFC / EMV / MSR / QR / MoMo / Cash)
  │ HAL Service écoute/attend
  ↓
ProcessingScreen
  │ SimulateTransactionUseCase (v0.1.0)
  │ SaveTransactionUseCase
  ↓
ResultScreen (Approuvé / Refusé / Erreur)
  │
  ↓
ReceiptScreen
  │ PrintReceiptUseCase → PrinterService → HAL
  ↓
HomeScreen
```

---

## 8. Sécurité

- PIN administrateur : chiffré avec AES-256 dans DataStore chiffré.
- PIN marchand : idem.
- Base de données Room chiffrée avec SQLCipher.
- Aucun PAN, CVV ou donnée bancaire sensible n'est stocké.
- Les logs ne contiennent jamais de données de carte.

---

## 9. Offline First

```
┌─────────────────────────────────────────────┐
│            REPOSITORY PATTERN               │
│                                             │
│  Remote Source ──(échec)──► Local Cache     │
│       │                         │           │
│       └─────────── sync ────────┘           │
│              (quand connecté)               │
└─────────────────────────────────────────────┘
```

Toutes les transactions sont d'abord sauvegardées localement. La synchronisation distante est tentée en arrière-plan avec retry exponentiel.

---

## 10. Logging (Timber)

```kotlin
Timber.tag("Payment").d("Transaction initiée: $transactionId")
Timber.tag("HAL.NFC").i("Carte détectée")
Timber.tag("HAL.Printer").e("Erreur impression: $error")
```

Niveaux : DEBUG (dev) / INFO (prod) / WARN / ERROR

Les logs sont stockés localement dans un fichier rotatif et exportables via l'interface.
