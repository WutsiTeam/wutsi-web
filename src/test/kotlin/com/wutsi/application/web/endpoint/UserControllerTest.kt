package com.wutsi.application.web.endpoint

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.web.Fixtures
import com.wutsi.application.web.Page
import com.wutsi.marketplace.manager.MarketplaceManagerApi
import com.wutsi.membership.manager.MembershipManagerApi
import com.wutsi.membership.manager.dto.GetMemberResponse
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean

internal class UserControllerTest : SeleniumTestSupport() {
    @MockBean
    private lateinit var membershipManagerApi: MembershipManagerApi

    @MockBean
    private lateinit var marketplaceManagerApi: MarketplaceManagerApi

    @Test
    fun account() {
        // GIVEN
        val account = Fixtures.createMember(id = 1, business = true, storeId = 111L)
        doReturn(GetMemberResponse(account)).whenever(membershipManagerApi).getMember(any())

        val products = listOf(
            Fixtures.createProductSummary(id = 11L, title = "This is a nice product", "http://www.google.ca/1.png"),
            Fixtures.createProductSummary(id = 22L, title = "Product 2", "http://www.google.ca/2.png")
        )
        doReturn(products).whenever(marketplaceManagerApi).searchProduct(any())

        // WHEN
        navigate(url("u/${account.id}"))
        Thread.sleep(1000)

        // THEN
        assertCurrentPageIs(Page.PROFILE)

        // Header
        assertElementAttribute("head title", "text", "${account.displayName} | Wutsi")
        assertElementAttribute("head meta[name='description']", "content", account.biography)
        assertElementAttribute("head meta[property='og:type']", "content", "website")
        assertElementAttribute("head meta[property='og:title']", "content", account.displayName)
        assertElementAttribute("head meta[property='og:description']", "content", account.biography)
        assertElementAttribute(
            "head meta[property='og:image']",
            "content",
            account.pictureUrl
        )

        assertElementPresent("#product-${products[0].id}")
        assertElementPresent("#product-${products[1].id}")
//        assertAppStoreLinksPresent()
    }
}
