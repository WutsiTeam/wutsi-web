package com.wutsi.application.web.endpoint

import com.wutsi.application.web.model.TransactionModel
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/transaction")
class TransactionRestController : AbstractController() {
    @GetMapping
    fun getTransaction(@RequestParam id: String): TransactionModel {
        val tx = checkoutManagerApi.getTransaction(id).transaction
        val business = checkoutManagerApi.getBusiness(tx.businessId).business
        val country = regulationEngine.country(business.country)
        return mapper.toTransactionModel(tx, country)
    }
}
