package com.wutsi.application.web.endpoint

import com.wutsi.application.web.Page
import com.wutsi.application.web.model.PageModel
import com.wutsi.checkout.manager.dto.Transaction
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/processing")
class ProcessingController : AbstractController() {
    @GetMapping
    fun index(
        @RequestParam(name = "t") transactionId: String,
        model: Model
    ): String {
        val tx = checkoutManagerApi.getTransaction(transactionId).transaction
        val business = checkoutManagerApi.getBusiness(tx.businessId).business
        val merchant = membershipManagerApi.getMember(business.accountId).member
        val country = regulationEngine.country(business.country)

        model.addAttribute("page", createPage())
        model.addAttribute("merchant", mapper.toMemberModel(merchant))
        model.addAttribute("tx", mapper.toTransactionModel(tx, country))
        model.addAttribute("transactionUrl", toTransactionUrl(tx))
        return "processing"
    }

    private fun toTransactionUrl(tx: Transaction): String =
        "/transaction?id=${tx.id}"

    private fun createPage() = PageModel(
        name = Page.PROCESSING,
        title = "Processing",
        assetUrl = assetUrl,
        robots = "noindex"
    )
}