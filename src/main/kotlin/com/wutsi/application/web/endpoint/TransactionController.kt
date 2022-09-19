package com.wutsi.application.web.endpoint

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/transaction")
class TransactionController : AbstractPageController() {
    override fun pageId() = "page.transaction"

    @GetMapping
    fun index(@RequestParam id: String, model: Model): String {
        model.addAttribute("downloadText", "Install the App to open the transaction")
        addOpenGraph(id, model)
        return "default"
    }

    private fun addOpenGraph(id: String, model: Model) {
        model.addAttribute("title", "Transaction #" + id.uppercase().takeLast(4))
    }
}
