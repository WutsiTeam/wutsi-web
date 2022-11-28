package com.wutsi.application.web.endpoint

import com.wutsi.application.web.Page
import com.wutsi.application.web.model.PageModel
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/payment")
class PaymentController : AbstractController() {
    @GetMapping
    fun index(
        @RequestParam(name = "o") orderId: String,
        model: Model
    ): String {
        model.addAttribute("page", createPage())
        return "payment"
    }

    private fun createPage() = PageModel(
        name = Page.PAYMENT,
        title = "Order",
        assetUrl = assetUrl,
        robots = "noindex"
    )
}
