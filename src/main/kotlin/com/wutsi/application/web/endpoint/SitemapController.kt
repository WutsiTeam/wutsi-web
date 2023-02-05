package com.wutsi.application.web.endpoint

import com.wutsi.application.web.view.SitemapView
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SitemapController(private val view: SitemapView) {
    @ResponseBody
    @GetMapping("/sitemap.xml")
    fun index(): SitemapView = view
}
