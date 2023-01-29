package com.wutsi.application.web.service

import org.springframework.http.HttpHeaders
import org.springframework.web.servlet.LocaleResolver
import java.util.Locale
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class LocaleResolverImpl(private val merchantHolder: MerchantHolder) : LocaleResolver {
    override fun resolveLocale(request: HttpServletRequest): Locale {
        val merchant = merchantHolder.get()
        return merchant?.let { Locale(it.language, it.country) }
            ?: request.getHeader(HttpHeaders.ACCEPT_LANGUAGE)?.let { Locale(it) }
            ?: Locale("en")
    }

    override fun setLocale(request: HttpServletRequest, response: HttpServletResponse, locale: Locale) {
    }
}
