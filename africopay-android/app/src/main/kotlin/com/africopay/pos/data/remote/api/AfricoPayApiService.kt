package com.africopay.pos.data.remote.api

import com.africopay.pos.data.remote.dto.DisbursementRequest
import com.africopay.pos.data.remote.dto.DisbursementResponse
import com.africopay.pos.data.remote.dto.DisbursementStatusResponse
import com.africopay.pos.data.remote.dto.PaymentInitRequest
import com.africopay.pos.data.remote.dto.PaymentInitResponse
import com.africopay.pos.data.remote.dto.PaymentStatusResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Interface Retrofit pour les appels vers le Backend AfricoPay.
 *
 * Le backend AfricoPay fait office de proxy sécurisé vers PayDunya.
 * Toutes les requêtes nécessitent l'en-tête X-API-KEY (géré via un intercepteur OkHttp).
 */
interface AfricoPayApiService {

    // ─── Paiements (API PAR PayDunya) ──────────────────────────────────────────

    /**
     * Initie un paiement Mobile Money.
     * Retourne le token et l'URL de la page de paiement PayDunya.
     */
    @POST("v1/payments/initiate")
    suspend fun initiatePayment(
        @Body request: PaymentInitRequest,
    ): PaymentInitResponse

    /**
     * Vérifie le statut d'un paiement via son token PayDunya.
     * Statuts : pending | completed | cancelled | failed
     */
    @GET("v1/payments/{token}/status")
    suspend fun getPaymentStatus(
        @Path("token") token: String,
    ): PaymentStatusResponse

    // ─── Déboursements (API PUSH PayDunya) ─────────────────────────────────────

    /**
     * Initie un envoi d'argent vers un compte Mobile Money.
     */
    @POST("v1/disbursements/send")
    suspend fun sendDisbursement(
        @Body request: DisbursementRequest,
    ): DisbursementResponse

    /**
     * Vérifie le statut d'un déboursement via son transactionId.
     * Statuts : created | pending | success | failed
     */
    @GET("v1/disbursements/{transactionId}/status")
    suspend fun getDisbursementStatus(
        @Path("transactionId") transactionId: String,
    ): DisbursementStatusResponse
}
