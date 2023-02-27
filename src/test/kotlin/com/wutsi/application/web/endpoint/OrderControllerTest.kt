package com.wutsi.application.web.endpoint

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.web.Fixtures
import com.wutsi.application.web.Page
import com.wutsi.application.web.service.recaptcha.Recaptcha
import com.wutsi.checkout.manager.dto.CreateOrderItemRequest
import com.wutsi.checkout.manager.dto.CreateOrderRequest
import com.wutsi.checkout.manager.dto.CreateOrderResponse
import com.wutsi.checkout.manager.dto.GetBusinessResponse
import com.wutsi.checkout.manager.dto.GetOrderResponse
import com.wutsi.checkout.manager.dto.SearchPaymentProviderResponse
import com.wutsi.enums.ChannelType
import com.wutsi.enums.DeviceType
import com.wutsi.marketplace.manager.dto.GetOfferResponse
import com.wutsi.marketplace.manager.dto.GetProductResponse
import com.wutsi.membership.manager.dto.GetMemberResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import java.util.UUID

internal class OrderControllerTest : SeleniumTestSupport() {
    @MockBean
    private lateinit var recaptcha: Recaptcha

    private val orderId = UUID.randomUUID().toString()
    private val product = Fixtures.createProduct(
        id = 11,
        storeId = merchant.storeId!!,
        accountId = merchant.id,
        price = 10000,
        pictures = listOf(
            Fixtures.createPictureSummary(1, "https://i.com/1.png"),
            Fixtures.createPictureSummary(2, "https://i.com/2.png"),
            Fixtures.createPictureSummary(3, "https://i.com/3.png"),
            Fixtures.createPictureSummary(4, "https://i.com/4.png"),
        ),
    )

    private val order = Fixtures.createOrder(id = orderId, businessId = merchant.businessId!!, accountId = merchant.id)

    private val mtn = Fixtures.createPaymentProviderSummary(1, "MTN")
    private val orange = Fixtures.createPaymentProviderSummary(2, "Orange")

    @BeforeEach
    override fun setUp() {
        super.setUp()

        doReturn(GetMemberResponse(merchant)).whenever(membershipManagerApi).getMember(any())

        doReturn(GetProductResponse(product)).whenever(marketplaceManagerApi).getProduct(any())

        val offer = Fixtures.createOffer(product)
        doReturn(GetOfferResponse(offer)).whenever(marketplaceManagerApi).getOffer(any())

        doReturn(GetBusinessResponse(business)).whenever(checkoutManagerApi).getBusiness(any())

        doReturn(CreateOrderResponse(orderId)).whenever(checkoutManagerApi).createOrder(any())
        doReturn(GetOrderResponse(order)).whenever(checkoutManagerApi).getOrder(orderId)

        doReturn(SearchPaymentProviderResponse(listOf(mtn, orange))).whenever(checkoutManagerApi)
            .searchPaymentProvider(any())

        doReturn(true).whenever(recaptcha).verify(any())
    }

    @Test
    fun `submit order`() {
        // Goto order page
        navigate(url("order?p=${product.id}&q=3"))
        assertCurrentPageIs(Page.ORDER)
        assertElementNotPresent(".error")

        // Enter data
        input("input[name=displayName]", "Ray Sponsible")
        input("input[name=email]", "ray.sponsible@gmail.com")
        input("input[name=confirm]", "ray.sponsible@gmail.com")
        input("textarea[name=notes]", "This is a note :-)")

        // Submit the data
        scrollToBottom()
        Thread.sleep(1000)
        assertElementAttribute("#btn-submit", "wutsi-track-event", "order")
        click("#btn-submit")

        verify(checkoutManagerApi).createOrder(
            CreateOrderRequest(
                deviceType = DeviceType.DESKTOP.name,
                channelType = ChannelType.WEB.name,
                businessId = merchant.businessId!!,
                notes = "This is a note :-)",
                customerEmail = "ray.sponsible@gmail.com",
                customerName = "Ray Sponsible",
                items = listOf(
                    CreateOrderItemRequest(
                        productId = product.id,
                        quantity = 3,
                    ),
                ),
            ),
        )

        // Check payment page
        assertCurrentPageIs(Page.PAYMENT)
    }

    @Test
    fun `recaptcha error`() {
        // Given
        doReturn(false).whenever(recaptcha).verify(any())

        // Goto order page
        navigate(url("order?p=${product.id}&q=3"))
        assertCurrentPageIs(Page.ORDER)

        // Enter data
        input("input[name=displayName]", "Ray Sponsible")
        input("input[name=email]", "ray.sponsible@gmail.com")
        input("input[name=confirm]", "ray.sponsible@gmail.com")
        input("textarea[name=notes]", "This is a note :-)")

        // Submit the data
        scrollToBottom()
        Thread.sleep(1000)
        click("#btn-submit")

        verify(checkoutManagerApi, never()).createOrder(any())

        // Check payment page
        assertCurrentPageIs(Page.ORDER)
        assertElementPresent(".error")
    }

    @Test
    fun notFound() {
        val ex = createFeignNotFoundException(errorCode = "xx")
        doThrow(ex).whenever(marketplaceManagerApi).getOffer(any())

        navigate(url("order?p=999999&q=3"))
        assertCurrentPageIs(Page.ERROR)
    }
}
