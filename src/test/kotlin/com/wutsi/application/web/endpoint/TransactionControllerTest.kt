package com.wutsi.application.web.endpoint

import org.junit.jupiter.api.Test

internal class TransactionControllerTest : SeleniumTestSupport() {
    @Test
    fun order() {
        // WHEN
        val link = url("transaction?id=repore-40945-5409540fa")
        navigate(link)

        // THEN
        assertCurrentPageIs("page.transaction")

        // OpenGraph
        assertElementAttribute("head title", "text", "Transaction #40FA | Wutsi")

        assertAppStoreLinksPresent()
    }
}
