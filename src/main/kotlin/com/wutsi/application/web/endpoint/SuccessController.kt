package com.wutsi.application.web.endpoint

import com.wutsi.application.web.Page
import com.wutsi.application.web.model.PageModel
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/success")
class SuccessController : AbstractController() {
    @GetMapping
    fun index(
        @RequestParam(name = "t") transactionId: String,
        model: Model,
    ): String {
        val tx = checkoutManagerApi.getTransaction(transactionId).transaction
        val merchant = resolveCurrentMerchant(tx.business.accountId)
        val country = regulationEngine.country(tx.business.country)

        model.addAttribute("page", createPage())
        model.addAttribute("merchant", mapper.toMemberModel(merchant))
        model.addAttribute("tx", mapper.toTransactionModel(tx, country))

        return "success"
    }

    private fun createPage() = PageModel(
        name = Page.SUCCESS,
        title = "Success",
        robots = "noindex",
    )
}
