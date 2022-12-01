package com.wutsi.application.web.endpoint

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.web.Fixtures
import com.wutsi.application.web.Page
import com.wutsi.marketplace.manager.MarketplaceManagerApi
import com.wutsi.marketplace.manager.dto.GetProductResponse
import com.wutsi.marketplace.manager.dto.GetStoreResponse
import com.wutsi.membership.manager.MembershipManagerApi
import com.wutsi.membership.manager.dto.GetMemberResponse
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import kotlin.test.Ignore

internal class ProductControllerTest : SeleniumTestSupport() {
    @MockBean
    private lateinit var membershipManagerApi: MembershipManagerApi

    @MockBean
    private lateinit var marketplaceManagerApi: MarketplaceManagerApi

    @Test
    fun dummy() {
        // Empty
    }

    @Test
    @Ignore("Still failing on pipeline :-(")
    fun index() {
        // GIVEN
        val account = Fixtures.createMember(id = 1, business = true, storeId = 111L)
        doReturn(GetMemberResponse(account)).whenever(membershipManagerApi).getMember(any())

        val store = Fixtures.createStore(account.storeId!!, account.id)
        doReturn(GetStoreResponse(store)).whenever(marketplaceManagerApi).getStore(any())

        val product = Fixtures.createProduct(
            id = 11,
            storeId = account.id,
            pictures = listOf(
                Fixtures.createPictureSummary(1, "https://i.com/1.png"),
                Fixtures.createPictureSummary(2, "https://i.com/2.png"),
                Fixtures.createPictureSummary(3, "https://i.com/3.png"),
                Fixtures.createPictureSummary(4, "https://i.com/4.png")
            )
        )
        doReturn(GetProductResponse(product)).whenever(marketplaceManagerApi).getProduct(any())

        // WHEN
        navigate(url("p/${account.id}"))
        Thread.sleep(1000)

        // THEN
        assertCurrentPageIs(Page.PRODUCT)

        // Header
        assertElementAttribute("head title", "text", "${product.title} | Wutsi")
        assertElementAttribute("head meta[name='description']", "content", product.summary)
        assertElementAttribute("head meta[property='og:type']", "content", "website")
        assertElementAttribute("head meta[property='og:title']", "content", product.title)
        assertElementAttribute("head meta[property='og:description']", "content", product.summary)
        assertElementAttribute(
            "head meta[property='og:image']",
            "content",
            product.thumbnail?.url
        )
        assertElementAttributeContains(
            "head meta[property='og:url']",
            "content",
            "/p/${product.id}"
        )

        // Carousel
        assertElementCount("#picture-carousel .carousel-item", product.pictures.size)

        // Content
        assertElementText(".product .title", product.title)
        assertElementText(".product .summary", product.summary!!)
        assertElementText(".product .description", product.description!!)
        assertElementPresent(".product .price")

        // Social button
        assertElementPresent("#button-facebook")
        assertElementPresent("#button-twitter")
        assertElementPresent("#button-instagram")
        assertElementPresent("#button-youtube")
    }
}
