package com.wutsi.application.web.service

import com.wutsi.membership.manager.dto.Member
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Service
import org.springframework.web.context.WebApplicationContext

@Service
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
class MerchantHolder {
    private var merchant: Member? = null

    fun set(merchant: Member) {
        this.merchant = merchant
    }

    fun get(): Member? = merchant
}
