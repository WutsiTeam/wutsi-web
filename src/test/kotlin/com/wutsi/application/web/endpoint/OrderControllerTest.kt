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
import com.wutsi.enums.ChannelType
import com.wutsi.enums.DeviceType
import com.wutsi.marketplace.manager.MarketplaceManagerApi
import com.wutsi.marketplace.manager.dto.GetProductResponse
import com.wutsi.marketplace.manager.dto.GetStoreResponse
import com.wutsi.membership.manager.MembershipManagerApi
import com.wutsi.membership.manager.dto.GetMemberResponse
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

    @Test
    fun index() {
        // GIVEN
        val account = Fixtures.createMember(id = 1, business = true, storeId = 111L, businessId = 11L)
        doReturn(GetMemberResponse(account)).whenever(membershipManagerApi).getMember(any())

        val store = Fixtures.createStore(account.storeId!!, account.id)
        doReturn(GetStoreResponse(store)).whenever(marketplaceManagerApi).getStore(any())

        val product = Fixtures.createProduct(
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
        doReturn(GetProductResponse(product)).whenever(marketplaceManagerApi).getProduct(any())

        val orderId = UUID.randomUUID().toString()
        doReturn(CreateOrderResponse(orderId)).whenever(checkoutManagerApi).createOrder(any())

        // WHEN
        navigate(url("order?p=${product.id}&q=3"))
        Thread.sleep(1000)

        // THEN
        assertCurrentPageIs(Page.ORDER)

        // Header
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
