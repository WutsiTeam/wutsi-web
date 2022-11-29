package com.wutsi.application.web.endpoint

import com.wutsi.application.web.Page
import com.wutsi.application.web.model.PageModel
import com.wutsi.checkout.manager.dto.CreateOrderItemRequest
import com.wutsi.enums.ChannelType
import com.wutsi.enums.DeviceType
import com.wutsi.platform.core.logging.KVLogger
import org.springframework.mobile.device.DeviceUtils
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.text.DecimalFormat
import java.util.UUID

@Controller
@RequestMapping("/order")
class OrderController(
    private val logger: KVLogger
) : AbstractController() {
    @GetMapping
    fun index(
        @RequestParam(name = "p") productId: Long,
        @RequestParam(name = "q") quantity: Int,
        model: Model
    ): String {
        val product = findProduct(productId)
        val store = findStore(product.storeId)
        val merchant = findMember(store.accountId)
        val country = regulationEngine.country(merchant.country)
        val productModel = mapper.toProductModel(product, country)
        val totalPrice = DecimalFormat(country.monetaryFormat).format((product.price ?: 0) * quantity)

        model.addAttribute("page", createPage())
        model.addAttribute("product", productModel)
        model.addAttribute("quantity", quantity)
        model.addAttribute("totalPrice", totalPrice)
        model.addAttribute("merchant", mapper.toMemberModel(merchant))

        return "order"
    }

    @PostMapping("/submit")
    fun submit(@ModelAttribute request: com.wutsi.application.web.dto.CreateOrderRequest): String {
        logger.add("request_business_id", request.businessId)
        logger.add("request_product_id", request.productId)
        logger.add("request_quantity", request.quantity)
        logger.add("request_notes", request.notes.take(10))
        logger.add("request_email", request.email)
        logger.add("request_display_name", request.displayName)

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
                        quantity = request.quantity
                    )
                )
            )
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

    private fun toChannelType(): ChannelType =
        ChannelType.WEB

    private fun createPage() = PageModel(
        name = Page.ORDER,
        title = "Order",
        assetUrl = assetUrl,
        robots = "noindex"
    )
}
