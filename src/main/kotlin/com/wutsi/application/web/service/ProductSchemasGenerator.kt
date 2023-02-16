package com.wutsi.application.web.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.application.web.model.OfferModel
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
class ProductSchemasGenerator(
    private val objectMapper: ObjectMapper,
    @Value("\${wutsi.application.server-url}") private val serverUrl: String,
) {
    fun generate(offer: OfferModel): String {
        val schemas = mutableMapOf<String, Any?>()

        schemas["@context"] = "https://schema.org/"
        schemas["@type"] = "Product"
        schemas["identifier"] = "urn:wutsi:product:${offer.product.id}"
        schemas["name"] = offer.product.title
        schemas["description"] = offer.product.description
        schemas["url"] = "$serverUrl${offer.product.url}"
        schemas["image"] = offer.product.pictures.map { it.originalUrl }
        schemas["sku"] = offer.product.id.toString()
        schemas["offers"] = mapOf(
            "@type" to "Offer",
            "priceCurrency" to offer.product.currency,
            "price" to offer.price.price,
            "priceValidUntil" to offer.price.expires?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            "availability" to if (offer.product.outOfStock) "https://schema.org/OutOfStock" else "https://schema.org/InStock",
        ).filter { it.value != null }

        return objectMapper.writeValueAsString(schemas.filter { it.value != null })
    }
}
