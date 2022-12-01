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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean

internal class ProductControllerTest : SeleniumTestSupport() {
    @MockBean
    private lateinit var membershipManagerApi: MembershipManagerApi

    @MockBean
    private lateinit var marketplaceManagerApi: MarketplaceManagerApi

    private val account = Fixtures.createMember(id = 1, business = true, storeId = 111L)
    private val store = Fixtures.createStore(account.storeId!!, account.id)

    private val product = Fixtures.createProduct(
        id = 11,
        storeId = account.id,
        pictures = listOf(
            Fixtures.createPictureSummary(1, "https://i.com/1.png"),
            Fixtures.createPictureSummary(2, "https://i.com/2.png"),
            Fixtures.createPictureSummary(3, "https://i.com/3.png"),
            Fixtures.createPictureSummary(4, "https://i.com/4.png")
        )
    )

    @BeforeEach
    override fun setUp() {
        super.setUp()

        doReturn(GetMemberResponse(account)).whenever(membershipManagerApi).getMember(any())

        doReturn(GetStoreResponse(store)).whenever(marketplaceManagerApi).getStore(any())

        doReturn(GetProductResponse(product)).whenever(marketplaceManagerApi).getProduct(any())
    }

    @Test
    fun index() {
        // Goto product page
        navigate(url("p/${account.id}"))
        assertCurrentPageIs(Page.PRODUCT)

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

        assertElementCount("#picture-carousel .carousel-item", product.pictures.size)

        assertElementText(".product .title", product.title)
        assertElementText(".product .summary", product.summary!!)
        assertElementText(".product .description", product.description!!)
        assertElementPresent(".product .price")

        assertElementPresent("#button-facebook")
        assertElementPresent("#button-twitter")
        assertElementPresent("#button-instagram")
        assertElementPresent("#button-youtube")
    }
}
