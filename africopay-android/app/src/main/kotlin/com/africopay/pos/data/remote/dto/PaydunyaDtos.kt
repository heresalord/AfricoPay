package com.africopay.pos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─── Paiement PAR — Initiation ────────────────────────────────────────────────

@Serializable
data class PaymentInitRequest(
    val amount: Long,
    val description: String,
    @SerialName("store_name") val storeName: String? = null,
    val customer: CustomerDto? = null,
    val channels: List<String>? = null,
)

@Serializable
data class CustomerDto(
    val name: String,
    val email: String? = null,
    val phone: String? = null,
)

@Serializable
data class PaymentInitResponse(
    val success: Boolean,
    val token: String,
    @SerialName("checkoutUrl") val checkoutUrl: String,
)

// ─── Paiement PAR — Statut ────────────────────────────────────────────────────

@Serializable
data class PaymentStatusResponse(
    val success: Boolean,
    val status: String,   // "pending" | "completed" | "cancelled" | "failed"
    val token: String,
    @SerialName("totalAmount") val totalAmount: Long,
    val customer: CustomerDto? = null,
    @SerialName("receiptUrl") val receiptUrl: String? = null,
)

// ─── Déboursement PUSH — Initiation ──────────────────────────────────────────

@Serializable
data class DisbursementRequest(
    @SerialName("accountAlias") val accountAlias: String,
    val amount: Long,
    @SerialName("withdrawMode") val withdrawMode: String,
    @SerialName("callbackUrl") val callbackUrl: String? = null,
)

@Serializable
data class DisbursementResponse(
    val success: Boolean,
    @SerialName("transactionId") val transactionId: String,
    val status: String,   // "created" | "pending" | "success" | "failed"
    val description: String,
)

// ─── Déboursement PUSH — Statut ───────────────────────────────────────────────

@Serializable
data class DisbursementStatusResponse(
    val success: Boolean,
    val status: String,
    @SerialName("transactionId") val transactionId: String,
    val amount: Long? = null,
    val description: String? = null,
)
