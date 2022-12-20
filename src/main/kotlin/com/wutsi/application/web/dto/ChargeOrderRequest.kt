package com.wutsi.application.web.dto

import com.wutsi.enums.PaymentMethodType

data class ChargeOrderRequest(
    val orderId: String = "",
    val phoneNumber: String = "",
    val businessId: Long = -1,
    val idempotencyKey: String = "",
    val paymentMethodType: PaymentMethodType = PaymentMethodType.MOBILE_MONEY,
)
