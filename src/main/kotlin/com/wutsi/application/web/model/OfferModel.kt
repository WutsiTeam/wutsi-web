package com.wutsi.application.web.model

data class OfferModel(
    public val product: ProductModel,
    public val price: OfferPriceModel,
    public val cancellationPolicy: CancellationPolicyModel = CancellationPolicyModel(),
    public val returnPolicy: ReturnPolicyModel = ReturnPolicyModel(),
)
