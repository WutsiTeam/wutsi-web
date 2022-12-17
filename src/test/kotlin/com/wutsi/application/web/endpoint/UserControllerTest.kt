package com.wutsi.application.web.endpoint

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.web.Fixtures
import com.wutsi.application.web.Page
import com.wutsi.enums.ProductType
import com.wutsi.error.ErrorURN
import com.wutsi.marketplace.manager.MarketplaceManagerApi
import com.wutsi.marketplace.manager.dto.SearchProductResponse
import com.wutsi.membership.manager.MembershipManagerApi
import com.wutsi.membership.manager.dto.GetMemberResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean

internal class UserControllerTest : SeleniumTestSupport() {
    @MockBean
    private lateinit var membershipManagerApi: MembershipManagerApi

    @MockBean
    private lateinit var marketplaceManagerApi: MarketplaceManagerApi

    private val account = Fixtures.createMember(id = 1, business = true, storeId = 111L)
    private val products = listOf(
        Fixtures.createProductSummary(id = 11L, title = "This is a nice product", "https://www.google.ca/1.png"),
        Fixtures.createProductSummary(id = 22L, title = "Product 2", "https://www.google.ca/2.png"),
        Fixtures.createProductSummary(
            id = 333,
            title = "Event 3",
            "https://www.google.ca/2.png",
            type = ProductType.EVENT,
            event = Fixtures.createEvent(
                meetingProvider = Fixtures.createMeetingProviderSummary()
            )
        )
    )

    @BeforeEach
    override fun setUp() {
        super.setUp()

        doReturn(GetMemberResponse(account)).whenever(membershipManagerApi).getMember(account.id)

        doReturn(SearchProductResponse(products)).whenever(marketplaceManagerApi).searchProduct(any())
    }

    @Test
    fun index() {
        // Goto user page
        navigate(url("u/${account.id}"))

        assertCurrentPageIs(Page.PROFILE)
        assertElementAttribute("head title", "text", "${account.displayName} | Wutsi")
        assertElementAttribute("head meta[name='description']", "content", account.biography)
        assertElementAttribute("head meta[property='og:type']", "content", "website")
        assertElementAttribute("head meta[property='og:title']", "content", account.displayName)
        assertElementAttribute("head meta[property='og:description']", "content", account.biography)
        assertElementNotPresent(
            "head meta[property='og:image']"
        )
        assertElementAttributeEndsWith(
            "head meta[property='og:url']",
            "content",
            "/u/${account.id}"
        )

        assertElementPresent("#button-facebook")
        assertElementPresent("#button-twitter")
        assertElementPresent("#button-instagram")
        assertElementPresent("#button-youtube")

        assertElementPresent("#product-${products[0].id}")
        assertElementPresent("#product-${products[1].id}")
        assertElementPresent("#product-${products[2].id}")
    }

    @Test
    fun notFound() {
        val ex = createFeignNotFoundException(errorCode = ErrorURN.MEMBER_NOT_FOUND.urn)
        doThrow(ex).whenever(membershipManagerApi).getMember(account.id)

        navigate(url("u/${account.id}"))
        assertCurrentPageIs(Page.ERROR)
    }
}
