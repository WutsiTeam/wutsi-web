package com.wutsi.application.web.endpoint

import com.wutsi.application.web.Page
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value

internal class IndexControllerTest : SeleniumTestSupport() {
    @Value("\${wutsi.application.pinterest.verif-code}")
    private lateinit var pinterestVerifCode: String

    @Test
    fun index() {
        navigate(url(""))

        assertCurrentPageIs(Page.HOME)
        assertElementAttribute("head meta[name='p\\:domain_verify']", "content", pinterestVerifCode)
    }
}
