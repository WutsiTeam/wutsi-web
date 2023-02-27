package com.wutsi.application.web.model

data class TransactionModel(
    val id: String,
    val type: String,
    val status: String,
    val amount: String,
    val amountValue: Long,
    val email: String?,
)
