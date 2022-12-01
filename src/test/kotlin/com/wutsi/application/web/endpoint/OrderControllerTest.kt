package com.wutsi.application.web.endpoint

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.web.Fixtures
import com.wutsi.application.web.Page
import com.wutsi.checkout.manager.CheckoutManagerApi
import com.wutsi.checkout.manager.dto.CreateOrderItemRequest
import com.wutsi.checkout.manager.dto.CreateOrderRequest
import com.wutsi.checkout.manager.dto.CreateOrderResponse
import com.wutsi.checkout.manager.dto.GetBusinessResponse
import com.wutsi.checkout.manager.dto.SearchPaymentProviderRequest
import com.wutsi.checkout.manager.dto.SearchPaymentProviderResponse
import com.wutsi.enums.ChannelType
import com.wutsi.enums.DeviceType
import com.wutsi.marketplace.manager.MarketplaceManagerApi
import com.wutsi.marketplace.manager.dto.GetProductResponse
import com.wutsi.marketplace.manager.dto.GetStoreResponse
import com.wutsi.membership.manager.MembershipManagerApi
import com.wutsi.membership.manager.dto.GetMemberResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import java.util.UUID

internal class OrderControllerTest : SeleniumTestSupport() {
    @MockBean
    private lateinit var membershipManagerApi: MembershipManagerApi

    @MockBean
    private lateinit var marketplaceManagerApi: MarketplaceManagerApi

    @MockBean
    private lateinit var checkoutManagerApi: CheckoutManagerApi

    private val orderId = UUID.randomUUID().toString()
    private val phoneNumber = "+237670000010"
    private val account = Fixtures.createMember(id = 1, business = true, storeId = 11L, businessId = 111L)
    private val store = Fixtures.createStore(id = account.storeId!!, accountId = account.id)
    private val business =
        Fixtures.createBusiness(id = account.businessId!!, accountId = account.id, country = "CM", currency = "XAF")
    private val product = Fixtures.createProduct(
        id = 11,
        storeId = account.id,
        price = 10000,
        pictures = listOf(
            Fixtures.createPictureSummary(1, "https://i.com/1.png"),
            Fixtures.createPictureSummary(2, "https://i.com/2.png"),
            Fixtures.createPictureSummary(3, "https://i.com/3.png"),
            Fixtures.createPictureSummary(4, "https://i.com/4.png")
        )
    )
    private val mtn = Fixtures.createPaymentProviderSummary(1, "MTN")
    private val orange = Fixtures.createPaymentProviderSummary(2, "Orange")

    @BeforeEach
    override fun setUp() {
        super.setUp()

        doReturn(GetMemberResponse(account)).whenever(membershipManagerApi).getMember(any())

        doReturn(GetStoreResponse(store)).whenever(marketplaceManagerApi).getStore(any())
        doReturn(GetProductResponse(product)).whenever(marketplaceManagerApi).getProduct(any())

        doReturn(GetBusinessResponse(business)).whenever(checkoutManagerApi).getBusiness(any())
        doReturn(CreateOrderResponse(orderId)).whenever(checkoutManagerApi).createOrder(any())
        doReturn(SearchPaymentProviderResponse(listOf(mtn, orange))).whenever(checkoutManagerApi).searchPaymentProvider(
            SearchPaymentProviderRequest(
                country = business.country
            )
        )

        doReturn(SearchPaymentProviderResponse(listOf(mtn))).whenever(checkoutManagerApi).searchPaymentProvider(
            SearchPaymentProviderRequest(
                country = business.country,
                number = phoneNumber
            )
        )
    }

    @Test
    fun index() {
        // WHEN
        navigate(url("order?p=${product.id}&q=3"))
        Thread.sleep(1000)

        // THEN
        assertCurrentPageIs(Page.ORDER)

        // Header
        input("input[name=phoneNumber]", phoneNumber)
        input("input[name=displayName]", "Ray Sponsible")
        input("input[name=email]", "ray.sponsible@gmail.com")
        input("textarea[name=notes]", "This is a note :-)")
        click("#btn-submit")
        Thread.sleep(1000)

        verify(checkoutManagerApi).createOrder(
            CreateOrderRequest(
                deviceType = DeviceType.MOBILE.name,
                channelType = ChannelType.WEB.name,
                businessId = account.businessId!!,
                notes = "This is a note :-)",
                customerEmail = "ray.sponsible@gmail.com",
                customerName = "Ray Sponsible",
                items = listOf(
                    CreateOrderItemRequest(
                        productId = product.id,
                        quantity = 3
                    )
                )
            )
        )
        assertCurrentPageIs(Page.PAYMENT)
    }
}
