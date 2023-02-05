package com.wutsi.application.web.endpoint

import com.wutsi.application.web.Page
import com.wutsi.application.web.model.PageModel
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class IndexController(
    @Value("\${wutsi.application.pinterest.verif-code}") private val pinterestVerifCode: String,
    @Value("\${wutsi.application.google.site-verification.id}") private val googleSiteVerificationId: String,
) : AbstractController() {
    @GetMapping
    fun index(model: Model): String {
        model.addAttribute("page", createPage())
        return "index"
    }

    private fun createPage() = PageModel(
        name = Page.HOME,
        title = "Wutsi",
        sitemapUrl = "$serverUrl/sitemap.xml",
        pinterestVerifCode = if (pinterestVerifCode.isNullOrEmpty()) null else pinterestVerifCode,
        googleSiteVerificationId = if (googleSiteVerificationId.isNullOrEmpty()) null else googleSiteVerificationId,
    )
}
