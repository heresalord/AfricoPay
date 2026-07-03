package com.africopay.pos.core.di

import android.content.Context
import android.os.Build
import androidx.room.Room
import com.africopay.pos.data.local.db.AfricoPayDatabase
import com.africopay.pos.domain.model.SimulationConfig
import com.africopay.pos.hal.interfaces.*
import com.africopay.pos.hal.mock.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import com.africopay.pos.BuildConfig

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ─── Database ─────────────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AfricoPayDatabase =
        Room.databaseBuilder(
            context,
            AfricoPayDatabase::class.java,
            AfricoPayDatabase.DATABASE_NAME
        ).build()

    @Provides fun provideTransactionDao(db: AfricoPayDatabase) = db.transactionDao()
    @Provides fun provideReceiptDao(db: AfricoPayDatabase) = db.receiptDao()
    @Provides fun provideMerchantProfileDao(db: AfricoPayDatabase) = db.merchantProfileDao()
    @Provides fun provideHardwareDiagnosticDao(db: AfricoPayDatabase) = db.hardwareDiagnosticDao()

    // ─── Simulation Config ────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideSimulationConfig(): SimulationConfig = SimulationConfig(
        processingDelayMs = 2500L
    )

    // ─── HAL Bindings ─────────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideNfcService(simulationConfig: SimulationConfig): NfcService {
        Timber.tag("HAL").d("Manufacturer: ${Build.MANUFACTURER}")
        return when (Build.MANUFACTURER.lowercase()) {
            // Future: "sunmi" -> SunmiNfcService(context)
            // Future: "pax"   -> PaxNfcService(context)
            else -> MockNfcService(simulationConfig)
        }
    }

    @Provides
    @Singleton
    fun provideEmvService(simulationConfig: SimulationConfig): EmvService =
        MockEmvService(simulationConfig)

    @Provides
    @Singleton
    fun provideMagStripeService(simulationConfig: SimulationConfig): MagStripeService =
        MockMagStripeService(simulationConfig)

    @Provides
    @Singleton
    fun providePrinterService(): PrinterService = MockPrinterService()

    // ─── Networking ───────────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor { message ->
            Timber.tag("OkHttp").d(message)
        }.apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
}
