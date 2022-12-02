package com.wutsi.application.web.endpoint

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.web.Fixtures
import com.wutsi.application.web.Page
import com.wutsi.checkout.manager.CheckoutManagerApi
import com.wutsi.checkout.manager.dto.CreateOrderResponse
import com.wutsi.checkout.manager.dto.GetOrderResponse
import com.wutsi.checkout.manager.dto.GetTransactionResponse
import com.wutsi.checkout.manager.dto.SearchPaymentProviderResponse
import com.wutsi.enums.TransactionType
import com.wutsi.error.ErrorURN
import com.wutsi.marketplace.manager.MarketplaceManagerApi
import com.wutsi.marketplace.manager.dto.GetProductResponse
import com.wutsi.membership.manager.MembershipManagerApi
import com.wutsi.membership.manager.dto.GetMemberResponse
import com.wutsi.platform.payment.core.Status
import feign.FeignException
import feign.Request
import feign.RequestTemplate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import java.nio.charset.Charset
import java.util.UUID

internal class ProcessingControllerTest : SeleniumTestSupport() {
    @MockBean
    private lateinit var membershipManagerApi: MembershipManagerApi

    @MockBean
    private lateinit var marketplaceManagerApi: MarketplaceManagerApi

    @MockBean
    private lateinit var checkoutManagerApi: CheckoutManagerApi

    private val transactionId = UUID.randomUUID().toString()
    private val account = Fixtures.createMember(id = 1, business = true, storeId = 11L, businessId = 111L)

    private val tx = Fixtures.createTransaction(
        transactionId,
        type = TransactionType.CHARGE,
        status = Status.PENDING
    )

    @BeforeEach
    override fun setUp() {
        super.setUp()

        doReturn(GetMemberResponse(account)).whenever(membershipManagerApi).getMember(any())

        doReturn(GetTransactionResponse(tx)).whenever(checkoutManagerApi).getTransaction(transactionId)
    }

    @Test
    fun `submit payment - SUCCESSFUL`() {
        // Given
        doReturn(Fixtures.createTransaction(status = Status.SUCCESSFUL))
            .whenever(checkoutManagerApi).getTransaction(transactionId, true)

        // Goto order page
        navigate(url("processing?t=$transactionId"))
        Thread.sleep(60000)

        // Check payment page
        assertCurrentPageIs(Page.SUCCESS)
    }

    @Test
    fun `submit payment - TRANSACTION_FAILED`() {
        // Given
        val ex = createFeignConflictException(ErrorURN.TRANSACTION_FAILED.urn)
        doThrow(ex).whenever(checkoutManagerApi).getTransaction(transactionId, true)

        val product = Fixtures.createProduct(
            id = 11,
            storeId = account.storeId!!,
            accountId = account.id,
            price = 10000,
            pictures = listOf(
                Fixtures.createPictureSummary(4, "https://i.com/4.png")
            )
        )
        doReturn(GetProductResponse(product)).whenever(marketplaceManagerApi).getProduct(any())

        val orderId = UUID.randomUUID().toString()
        val order = Fixtures.createOrder(id = orderId, businessId = account.businessId!!, accountId = account.id)
        doReturn(CreateOrderResponse(orderId)).whenever(checkoutManagerApi).createOrder(any())
        doReturn(GetOrderResponse(order)).whenever(checkoutManagerApi).getOrder(orderId)

        val mtn = Fixtures.createPaymentProviderSummary(1, "MTN")
        doReturn(SearchPaymentProviderResponse(listOf(mtn))).whenever(checkoutManagerApi).searchPaymentProvider(any())

        // Goto order page
        navigate(url("processing?t=$transactionId"))
        Thread.sleep(60000)

        // Enter data
        assertCurrentPageIs(Page.PAYMENT)
        assertElementPresent(".error")
    }

    protected fun createFeignConflictException(
        errorCode: String
    ) = FeignException.Conflict(
        "",
        Request.create(
            Request.HttpMethod.POST,
            "https://www.google.ca",
            emptyMap(),
            "".toByteArray(),
            Charset.defaultCharset(),
            RequestTemplate()
        ),
        """
            {
                "error":{
                    "code": "$errorCode"
                }
            }
        """.trimIndent().toByteArray(),
        emptyMap()
    )
}
