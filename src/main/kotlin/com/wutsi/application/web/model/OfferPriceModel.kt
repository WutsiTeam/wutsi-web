package com.wutsi.application.web.model

data class OfferPriceModel(
    public val price: String,
    public val referencePrice: String?,
    public val savings: String?,
    public val savingsPercentage: String?,
    public val expiresHours: Int?,
)
