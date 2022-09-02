package com.wutsi.application.web.endpoint

import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.platform.tenant.dto.Tenant
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.ModelAttribute

abstract class AbstractPageController {
    @Value("\${wutsi.application.asset-url}")
    private lateinit var assetUrl: String

    @Autowired
    private lateinit var tenantProvider: TenantProvider

    @Autowired
    private lateinit var env: Environment

    @ModelAttribute(name = "assetUrl")
    fun getAssetUrl(): String = assetUrl

    @ModelAttribute(name = "pageId")
    fun getPageId(): String = pageId()

    @ModelAttribute(name = "tenant")
    fun getTenant(): Tenant = tenantProvider.get()

    @ModelAttribute(name = "tenantLogoUrl")
    fun getTenantLogoUrl(): String? =
        getTenant().logos.find { it.type == "WORDMARK" }?.url

    abstract fun pageId(): String
}
