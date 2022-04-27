package com.wutsi.application.web.endpoint

import com.wutsi.analytics.tracking.WutsiTrackingApi
import com.wutsi.analytics.tracking.dto.PushTrackRequest
import com.wutsi.analytics.tracking.dto.Track
import com.wutsi.analytics.tracking.entity.EventType
import com.wutsi.application.shared.service.SharedUIMapper
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.ecommerce.catalog.WutsiCatalogApi
import com.wutsi.ecommerce.catalog.dto.Product
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.account.dto.Account
import com.wutsi.platform.core.image.Dimension
import com.wutsi.platform.core.image.ImageService
import com.wutsi.platform.core.image.Transformation
import com.wutsi.platform.core.tracing.TracingContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID
import javax.servlet.http.HttpServletRequest

@Controller
@RequestMapping("/product")
class ProductController(
    private val catalogApi: WutsiCatalogApi,
    private val accountApi: WutsiAccountApi,
    private val sharedUIMapper: SharedUIMapper,
    private val tenantProvider: TenantProvider,
    private val trackingApi: WutsiTrackingApi,
    private val tracingContext: TracingContext,
    private val httpRequest: HttpServletRequest,
    private val imageService: ImageService,
) : AbstractPageController() {
    companion object {
        const val PAGE_ID = "page.Product"
        private val LOGGER = LoggerFactory.getLogger(ProfileController::class.java)
    }

    override fun pageId() = "page.product"

    @GetMapping
    fun index(@RequestParam id: Long, model: Model): String {
        val product: Product = findProduct(id)
        addOpenGraph(product, model)

        val tenant = tenantProvider.get()
        val productModel = sharedUIMapper.toProductModel(product, tenant)
        val accountModel = sharedUIMapper.toAccountModel(findAccount(product.accountId))
        val correlationId = UUID.randomUUID().toString()

        model.addAttribute("product", productModel)
        model.addAttribute("account", accountModel)
        model.addAttribute("correlationId", correlationId)
        model.addAttribute("shareUrl", "/product/on-share?id=$id&correlation-id=$correlationId")
        track(product, correlationId)
        return "product"
    }

    @GetMapping("/on-share")
    fun onShare(@RequestParam id: Long, @RequestParam(name = "correlation-id") correlationId: String): String {
        val product: Product = findProduct(id)
        track(correlationId, EventType.SHARE, product)
        return "product-share"
    }

    private fun track(product: Product, correlationId: String) {
        track(correlationId, EventType.LOAD)
        track(correlationId, EventType.VIEW, product)
    }

    private fun track(correlationId: String, event: EventType, product: Product? = null) {
        try {
            val url = httpRequest.requestURL
            if (!httpRequest.queryString.isNullOrEmpty())
                url.append('?').append(httpRequest.queryString)

            trackingApi.push(
                request = PushTrackRequest(
                    track = Track(
                        time = System.currentTimeMillis(),
                        tenantId = tenantProvider.tenantId().toString(),
                        deviceId = tracingContext.deviceId(),
                        correlationId = correlationId,
                        accountId = null,
                        productId = product?.id?.toString(),
                        merchantId = product?.accountId?.toString(),
                        page = PAGE_ID,
                        event = event.name,
                        ua = httpRequest.getHeader("User-Agent"),
                        referer = httpRequest.getHeader("Referer"),
                        ip = httpRequest.getHeader("X-Forwarded-For") ?: httpRequest.remoteAddr,
                        url = url.toString()
                    )
                )
            )
        } catch (ex: Exception) {
            LOGGER.warn("Unable to track Event#$event - Product#${product?.id}", ex)
        }
    }

    private fun addOpenGraph(product: Product, model: Model) {
        model.addAttribute("title", product.title)
        model.addAttribute("description", product.summary)
        model.addAttribute("image", product.thumbnail?.url?.let { openGraphImage(it) })
        model.addAttribute("type", "website")
    }

    /**
     * Generate open-graph image following commons specification. See https://kaydee.net/blog/open-graph-image
     *  - Aspect ration: 16:9
     *  - Dimension: 1200x630
     */
    private fun openGraphImage(url: String): String =
        imageService.transform(
            url = url,
            transformation = Transformation(
                dimension = Dimension(height = 630)
            )
        )

    private fun findAccount(id: Long): Account =
        accountApi.getAccount(id).account

    private fun findProduct(id: Long): Product =
        catalogApi.getProduct(id).product
}
