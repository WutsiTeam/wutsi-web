package com.wutsi.application.web.endpoint

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.web.Fixtures
import com.wutsi.application.web.Page
import com.wutsi.enums.ProductType
import com.wutsi.error.ErrorURN
import com.wutsi.marketplace.manager.dto.SearchOfferResponse
import com.wutsi.membership.manager.dto.GetMemberResponse
import com.wutsi.membership.manager.dto.Member
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class UserControllerTest : SeleniumTestSupport() {
    private val offers = listOf(
        Fixtures.createOfferSummary(
            Fixtures.createProductSummary(
                id = 11L,
                title = "This is a nice product",
                "https://www.google.ca/1.png",
                price = 1000L,
            ),
            Fixtures.createOfferPrice(
                11L,
                discountId = 11L,
                savings = 100,
                price = 1000,
                referencePrice = 1500,
            ),
        ),
        Fixtures.createOfferSummary(
            Fixtures.createProductSummary(id = 22L, title = "Product 2", "https://www.google.ca/2.png", price = 2000),
            Fixtures.createOfferPrice(11L, discountId = null, savings = 0, price = 2000, referencePrice = null),
        ),
        Fixtures.createOfferSummary(
            Fixtures.createProductSummary(
                id = 33L,
                title = "Event 3",
                thumbnailUrl = "https://www.google.ca/2.png",
                type = ProductType.EVENT,
                event = Fixtures.createEvent(
                    meetingProvider = Fixtures.createMeetingProviderSummary(),
                ),
            ),
        ),
        Fixtures.createOfferSummary(
            Fixtures.createProductSummary(
                id = 44L,
                title = "Weekly Planner",
                thumbnailUrl = "https://www.google.ca/4.png",
                type = ProductType.DIGITAL_DOWNLOAD,
            ),
        ),
    )

    @BeforeEach
    override fun setUp() {
        super.setUp()

        doReturn(GetMemberResponse(merchant)).whenever(membershipManagerApi).getMember(merchant.id)
        doReturn(GetMemberResponse(merchant)).whenever(membershipManagerApi).getMemberByName(merchant.name!!)

        doReturn(SearchOfferResponse(offers)).whenever(marketplaceManagerApi).searchOffer(any())
    }

    @Test
    fun byId() {
        // Goto user page
        navigate(url("u/${merchant.id}"))
        verify(merchant)
    }

    @Test
    fun byName() {
        // Goto user page
        navigate(url("@${merchant.name}"))
        verify(merchant)
    }

    private fun verify(merchant: Member) {
        assertCurrentPageIs(Page.PROFILE)
        assertElementAttribute("head title", "text", "${merchant.displayName} | Wutsi")
        assertElementAttribute("head meta[name='description']", "content", merchant.biography)
        assertElementAttribute("head meta[property='og:type']", "content", "website")
        assertElementAttribute("head meta[property='og:title']", "content", merchant.displayName)
        assertElementAttribute("head meta[property='og:description']", "content", merchant.biography)
        assertElementAttribute(
            "head meta[property='og:image']",
            "content",
            merchant.pictureUrl,
        )
        assertElementAttributeEndsWith(
            "head meta[property='og:url']",
            "content",
            "/@${merchant.name}",
        )

        assertElementAttributeContains(
            "head link[rel='sitemap']",
            "href",
            "/sitemap.xml?id=${merchant.id}",
        )

        assertElementPresent("#button-facebook")
        assertElementPresent("#button-twitter")
        assertElementPresent("#button-instagram")
        assertElementPresent("#button-youtube")

        assertElementPresent("#product-${offers[0].product.id}")
        assertElementPresent("#product-${offers[1].product.id}")
        assertElementPresent("#product-${offers[2].product.id}")
    }

    @Test
    fun notFound() {
        val ex = createFeignNotFoundException(errorCode = ErrorURN.MEMBER_NOT_FOUND.urn)
        doThrow(ex).whenever(membershipManagerApi).getMember(merchant.id)

        navigate(url("u/${merchant.id}"))
        assertCurrentPageIs(Page.ERROR)
    }
}
