package com.wutsi.application.web.config

import com.wutsi.application.web.service.LocaleResolverImpl
import com.wutsi.application.web.service.MerchantHolder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.LocaleResolver

@Configuration
class LocalizationConfiguration(private val merchantHolder: MerchantHolder) {
    @Bean
    fun localeResolver(): LocaleResolver =
        LocaleResolverImpl(merchantHolder)
}
