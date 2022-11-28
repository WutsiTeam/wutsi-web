package com.wutsi.application.web.endpoint

import com.wutsi.application.web.Page
import com.wutsi.application.web.model.CreateOrderModel
import com.wutsi.application.web.model.PageModel
import com.wutsi.checkout.manager.dto.CreateOrderItemRequest
import com.wutsi.checkout.manager.dto.CreateOrderRequest
import com.wutsi.enums.ChannelType
import com.wutsi.enums.DeviceType
import org.springframework.mobile.device.DeviceUtils
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.text.DecimalFormat

@Controller
@RequestMapping("/order")
class OrderController : AbstractController() {
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
    fun submit(@ModelAttribute request: CreateOrderModel): String {
        val orderId = checkoutManagerApi.createOrder(
            request = CreateOrderRequest(
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
        return "redirect:/payment?o=$orderId"
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
