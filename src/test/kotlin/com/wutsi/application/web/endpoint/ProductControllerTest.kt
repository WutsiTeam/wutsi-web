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
import com.wutsi.marketplace.manager.dto.GetProductResponse
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

    @BeforeEach
    override fun setUp() {
        super.setUp()

        doReturn(GetMemberResponse(account)).whenever(membershipManagerApi).getMember(any())
    }

    @Test
    fun physicalProduct() {
        // Given
        val product = Fixtures.createProduct(
            id = 11,
            storeId = account.storeId!!,
            accountId = account.id,
            pictures = listOf(
                Fixtures.createPictureSummary(1, "https://i.com/1.png"),
                Fixtures.createPictureSummary(2, "https://i.com/2.png"),
                Fixtures.createPictureSummary(3, "https://i.com/3.png"),
                Fixtures.createPictureSummary(4, "https://i.com/4.png"),
            ),
        )
        doReturn(GetProductResponse(product)).whenever(marketplaceManagerApi).getProduct(product.id)

        // Goto product page
        navigate(url("p/${product.id}"))
        assertCurrentPageIs(Page.PRODUCT)

        assertElementAttribute("head title", "text", "${product.title} | Wutsi")
        assertElementAttribute("head meta[name='description']", "content", product.summary)
        assertElementAttribute("head meta[property='og:type']", "content", "website")
        assertElementAttribute("head meta[property='og:title']", "content", product.title)
        assertElementAttribute("head meta[property='og:description']", "content", product.summary)
        assertElementAttribute(
            "head meta[property='og:image']",
            "content",
            product.thumbnail?.url,
        )
        assertElementAttributeContains(
            "head meta[property='og:url']",
            "content",
            "/p/${product.id}",
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

    @Test
    fun event() {
        // Given
        val product = Fixtures.createProduct(
            id = 11,
            storeId = account.storeId!!,
            accountId = account.id,
            pictures = listOf(
                Fixtures.createPictureSummary(1, "https://i.com/1.png"),
                Fixtures.createPictureSummary(2, "https://i.com/2.png"),
                Fixtures.createPictureSummary(3, "https://i.com/3.png"),
                Fixtures.createPictureSummary(4, "https://i.com/4.png"),
            ),
            type = ProductType.EVENT,
            event = Fixtures.createEvent(
                online = true,
                meetingProvider = Fixtures.createMeetingProviderSummary(),
            ),
        )
        doReturn(GetProductResponse(product)).whenever(marketplaceManagerApi).getProduct(product.id)

        // Goto product page
        navigate(url("p/${product.id}"))
        assertCurrentPageIs(Page.PRODUCT)

        assertElementAttribute("head title", "text", "${product.title} | Wutsi")
        assertElementAttribute("head meta[name='description']", "content", product.summary)
        assertElementAttribute("head meta[property='og:type']", "content", "website")
        assertElementAttribute("head meta[property='og:title']", "content", product.title)
        assertElementAttribute("head meta[property='og:description']", "content", product.summary)
        assertElementAttribute(
            "head meta[property='og:image']",
            "content",
            product.thumbnail?.url,
        )
        assertElementAttributeContains(
            "head meta[property='og:url']",
            "content",
            "/p/${product.id}",
        )

        assertElementCount("#picture-carousel .carousel-item", product.pictures.size)

        assertElementText(".product .title", product.title)
        assertElementText(".product .summary", product.summary!!)
        assertElementText(".product .description", product.description!!)
        assertElementPresent(".product .price")

        assertElementPresent("#product-delivery")
        assertElementPresent("#product-delivery-event-online")

        assertElementPresent("#button-facebook")
        assertElementPresent("#button-twitter")
        assertElementPresent("#button-instagram")
        assertElementPresent("#button-youtube")
    }


    @Test
    fun digitalDownload() {
        // Given
        val product = Fixtures.createProduct(
            id = 11,
            storeId = account.storeId!!,
            accountId = account.id,
            pictures = listOf(
                Fixtures.createPictureSummary(1, "https://i.com/1.png"),
                Fixtures.createPictureSummary(2, "https://i.com/2.png"),
                Fixtures.createPictureSummary(3, "https://i.com/3.png"),
                Fixtures.createPictureSummary(4, "https://i.com/4.png"),
            ),
            type = ProductType.DIGITAL_DOWNLOAD,
            files = listOf(
                Fixtures.createFileSummary(1),
                Fixtures.createFileSummary(2),
                Fixtures.createFileSummary(3, "foo.pdf")
            )
        )
        doReturn(GetProductResponse(product)).whenever(marketplaceManagerApi).getProduct(product.id)

        // Goto product page
        navigate(url("p/${product.id}"))
        assertCurrentPageIs(Page.PRODUCT)

        assertElementAttribute("head title", "text", "${product.title} | Wutsi")
        assertElementAttribute("head meta[name='description']", "content", product.summary)
        assertElementAttribute("head meta[property='og:type']", "content", "website")
        assertElementAttribute("head meta[property='og:title']", "content", product.title)
        assertElementAttribute("head meta[property='og:description']", "content", product.summary)
        assertElementAttribute(
            "head meta[property='og:image']",
            "content",
            product.thumbnail?.url,
        )
        assertElementAttributeContains(
            "head meta[property='og:url']",
            "content",
            "/p/${product.id}",
        )

        assertElementCount("#picture-carousel .carousel-item", product.pictures.size)

        assertElementText(".product .title", product.title)
        assertElementText(".product .summary", product.summary!!)
        assertElementText(".product .description", product.description!!)
        assertElementPresent(".product .price")

        assertElementPresent("#product-delivery")
        assertElementPresent("#product-delivery-digital-download")
    }

    @Test
    fun notFound() {
        val ex = createFeignNotFoundException(errorCode = ErrorURN.PRODUCT_NOT_FOUND.urn)
        doThrow(ex).whenever(marketplaceManagerApi).getProduct(any())

        navigate(url("p/99999"))
        assertCurrentPageIs(Page.ERROR)
    }
}
