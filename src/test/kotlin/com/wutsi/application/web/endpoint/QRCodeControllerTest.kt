package com.wutsi.application.web.endpoint

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.platform.tenant.dto.Tenant
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class QRCodeControllerTest : SeleniumTestSupport() {
    @MockBean
    private lateinit var tenantProvider: TenantProvider

    private val tenant = Tenant(
        id = 1,
        name = "foo"
    )

    @Test
    fun account() {
        doReturn(1L).whenever(tenantProvider).tenantId()
        doReturn(tenant).whenever(tenantProvider).get()

        navigate(url("qr-code/account/1.png"))
    }
}
