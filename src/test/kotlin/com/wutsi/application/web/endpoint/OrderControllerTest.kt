package com.wutsi.application.web.endpoint

import org.junit.jupiter.api.Test

internal class OrderControllerTest : SeleniumTestSupport() {
    @Test
    fun order() {
        // WHEN
        val link = url("order?id=repore-40945-5409540fa")
        navigate(link)

        // THEN
        assertCurrentPageIs("page.order")

        // OpenGraph
        assertElementAttribute("head title", "text", "Order #40FA | Wutsi")

        assertAppStoreLinksPresent()
    }
}
