package com.wutsi.application.web.endpoint

import com.wutsi.application.web.Page
import com.wutsi.application.web.model.PageModel
import com.wutsi.application.web.service.recaptcha.Recaptcha
import com.wutsi.application.web.servlet.ChannelFilter
import com.wutsi.application.web.util.ErrorCode
import com.wutsi.checkout.manager.dto.CreateOrderItemRequest
import com.wutsi.enums.ChannelType
import com.wutsi.enums.DeviceType
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.mobile.device.DeviceUtils
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID
import javax.servlet.http.HttpServletRequest

@Controller
@RequestMapping("/order")
class OrderController(
    private val httpRequest: HttpServletRequest,
    private val recaptcha: Recaptcha,
    private val messages: MessageSource,

    @Value("\${wutsi.application.google.recaptcha.site-key}") private val recaptchaSiteKey: String,
) : AbstractController() {
    @GetMapping
    fun index(
        @RequestParam(name = "p") productId: Long,
        @RequestParam(name = "q") quantity: Int,
        @RequestParam(name = "e", required = false) error: Long? = null,
        model: Model,
    ): String {
        val offer = marketplaceManagerApi.getOffer(productId).offer
        val merchant = resolveCurrentMerchant(offer.product.store.accountId)
        val store = marketplaceManagerApi.getStore(merchant.storeId!!).store
        val business = checkoutManagerApi.getBusiness(merchant.businessId!!).business
        val country = regulationEngine.country(business.country)
        val offerModel = mapper.toOfferModel(offer, country, merchant, store)

        val fmt = country.createMoneyFormat()
        val subTotal = offer.product.price?.let {
            fmt.format(it * quantity)
        }

        val totalSavings = if (offer.price.savings > 0) {
            fmt.format(offer.price.savings * quantity)
        } else {
            null
        }

        val totalPrice = fmt.format(offer.price.price * quantity)

        model.addAttribute("page", createPage())
        model.addAttribute("offer", offerModel)
        model.addAttribute("quantity", quantity)
        model.addAttribute("merchant", mapper.toMemberModel(merchant))
        model.addAttribute("subTotal", subTotal)
        model.addAttribute("totalSavings", totalSavings)
        model.addAttribute("totalPrice", totalPrice)
        model.addAttribute("error", error?.let { toError(it) })

        return "order"
    }

    @PostMapping("/submit")
    fun submit(
        @ModelAttribute request: com.wutsi.application.web.dto.CreateOrderRequest,
    ): String {
        logger.add("request_business_id", request.businessId)
        logger.add("request_product_id", request.productId)
        logger.add("request_quantity", request.quantity)
        logger.add("request_notes", request.notes.take(10))
        logger.add("request_email", request.email)
        logger.add("request_display_name", request.displayName)

        // Recaptcha
        val recaptchaResponse = httpRequest.getParameter(Recaptcha.REQUEST_PARAMETER)
        logger.add("request_g-recaptcha-response", recaptchaResponse)
        if (!recaptcha.verify(recaptchaResponse)) {
            return "redirect:/order?p=${request.productId}&q=${request.quantity}&e=${ErrorCode.RECAPTCHA}"
        }

        // Order
        val orderId = checkoutManagerApi.createOrder(
            request = com.wutsi.checkout.manager.dto.CreateOrderRequest(
                deviceType = toDeviceType().toString(),
                channelType = toChannelType().toString(),
                businessId = request.businessId,
                customerName = request.displayName,
                customerEmail = request.email,
                notes = request.notes,
                items = listOf(
                    CreateOrderItemRequest(
                        productId = request.productId,
                        quantity = request.quantity,
                    ),
                ),
            ),
        ).orderId
        val idempotencyKey = UUID.randomUUID().toString()
        logger.add("order_id", orderId)
        logger.add("idempotency_key", idempotencyKey)

        return "redirect:/payment?o=$orderId&i=$idempotencyKey"
    }

    private fun toDeviceType(): DeviceType {
        val device = DeviceUtils.getCurrentDevice(request)
        return if (device.isMobile) {
            DeviceType.MOBILE
        } else if (device.isTablet) {
            DeviceType.TABLET
        } else {
            DeviceType.DESKTOP
        }
    }

    private fun toChannelType(): ChannelType {
        val cookie = httpRequest.cookies.find { it.name == ChannelFilter.CHANNEL_COOKIE }
            ?: return ChannelType.WEB

        return try {
            ChannelType.valueOf(cookie.value.uppercase())
        } catch (ex: Throwable) {
            ChannelType.WEB
        }
    }

    private fun createPage() = PageModel(
        name = Page.ORDER,
        title = "Order",
        robots = "noindex",
        recaptchaSiteKey = recaptchaSiteKey,
    )

    private fun toError(error: Long): String? = when (error) {
        ErrorCode.RECAPTCHA -> {
            messages.getMessage(
                "error-message.recaptcha-error",
                emptyArray(),
                LocaleContextHolder.getLocale(),
            )
        }
        else -> messages.getMessage("error-message.unexpected", emptyArray(), LocaleContextHolder.getLocale())
    }
}
