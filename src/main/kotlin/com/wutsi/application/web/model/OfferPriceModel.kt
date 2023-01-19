package com.wutsi.application.web.model

import java.time.OffsetDateTime

data class OfferPriceModel(
    public val price: String,
    public val referencePrice: String?,
    public val savings: String?,
    public val savingsPercentage: String?,
    public val expires: OffsetDateTime?,
    public val expiresHours: Int?,
    public val expiresMinutes: Int?,
)
