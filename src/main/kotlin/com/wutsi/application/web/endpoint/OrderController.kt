package com.wutsi.application.web.endpoint

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/order")
class OrderController : AbstractPageController() {
    override fun pageId() = "page.order"

    @GetMapping
    fun index(@RequestParam id: String, model: Model): String {
        addOpenGraph(id, model)
        model.addAttribute("downloadText", "Install the App to view the Order details")
        return "default"
    }

    private fun addOpenGraph(id: String, model: Model) {
        model.addAttribute("title", "Order #" + id.uppercase().takeLast(4))
    }
}
