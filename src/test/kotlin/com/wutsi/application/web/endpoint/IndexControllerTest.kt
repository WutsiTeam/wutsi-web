package com.wutsi.application.web.endpoint

import com.wutsi.application.web.Page
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value

internal class IndexControllerTest : SeleniumTestSupport() {
    @Value("\${wutsi.application.pinterest.verif-code}")
    private lateinit var pinterestVerifCode: String

    @Value("\${wutsi.application.google.site-verification.id}")
    private lateinit var googleSiteVerificationId: String

    @Test
    fun index() {
        navigate(url(""))

        assertCurrentPageIs(Page.HOME)
        assertElementAttribute("head meta[name='p\\:domain_verify']", "content", pinterestVerifCode)
        assertElementAttribute("head meta[name='google-site-verification']", "content", googleSiteVerificationId)

        assertElementAttributeContains(
            "head link[rel='sitemap']",
            "href",
            "/sitemap.xml",
        )
    }
}
