package com.wutsi.application.web.endpoint

import com.wutsi.application.web.Page
import com.wutsi.application.web.model.PageModel
import com.wutsi.application.web.util.ErrorCode
import com.wutsi.checkout.manager.dto.Transaction
import com.wutsi.platform.payment.core.Status
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

@Controller
@RequestMapping("/processing")
class ProcessingController : AbstractController() {
    @GetMapping
    fun index(
        @RequestParam(name = "t") transactionId: String,
        model: Model,
    ): String {
        val tx = checkoutManagerApi.getTransaction(transactionId).transaction
        if (tx.status == Status.SUCCESSFUL.name) { // Success
            return "redirect:/success?t=${tx.id}"
        } else if (tx.status == Status.FAILED.name) { // Failure
            return "redirect:/payment?o=${tx.orderId}&code=${tx.errorCode}&e=${ErrorCode.TRANSACTION_FAILED}&i=" + UUID.randomUUID()
                .toString()
        }

        // Pending
        val merchant = resolveCurrentMerchant(tx.business.accountId)
        val country = regulationEngine.country(tx.business.country)

        model.addAttribute("page", createPage())
        model.addAttribute("merchant", mapper.toMemberModel(merchant))
        model.addAttribute("tx", mapper.toTransactionModel(tx, country))
        model.addAttribute("transactionUrl", toTransactionUrl(tx))
        model.addAttribute("idempotencyKey", UUID.randomUUID().toString())

        return "processing"
    }

    private fun toTransactionUrl(tx: Transaction): String =
        "/transaction?id=${tx.id}"

    private fun createPage() = PageModel(
        name = Page.PROCESSING,
        title = "Processing",
        robots = "noindex",
    )
}
