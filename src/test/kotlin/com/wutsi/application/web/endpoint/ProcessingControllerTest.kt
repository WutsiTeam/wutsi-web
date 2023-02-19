package com.wutsi.application.web.endpoint

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.web.Fixtures
import com.wutsi.application.web.Page
import com.wutsi.checkout.manager.dto.GetOrderResponse
import com.wutsi.checkout.manager.dto.GetTransactionResponse
import com.wutsi.checkout.manager.dto.SearchPaymentProviderResponse
import com.wutsi.enums.TransactionType
import com.wutsi.marketplace.manager.dto.GetProductResponse
import com.wutsi.membership.manager.dto.GetMemberResponse
import com.wutsi.platform.payment.core.ErrorCode
import com.wutsi.platform.payment.core.Status
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

internal class ProcessingControllerTest : SeleniumTestSupport() {
    private val transactionId = UUID.randomUUID().toString()
    private val orderId = UUID.randomUUID().toString()

    private val tx = Fixtures.createTransaction(
        transactionId,
        type = TransactionType.CHARGE,
        status = Status.PENDING,
    )

    val product = Fixtures.createProduct(
        id = 11,
        storeId = merchant.storeId!!,
        accountId = merchant.id,
        price = 10000,
        pictures = listOf(
            Fixtures.createPictureSummary(4, "https://i.com/4.png"),
        ),
    )

    @BeforeEach
    override fun setUp() {
        super.setUp()

        doReturn(GetMemberResponse(merchant)).whenever(membershipManagerApi).getMember(any())
        doReturn(GetTransactionResponse(tx)).whenever(checkoutManagerApi).getTransaction(transactionId)

        doReturn(GetProductResponse(product)).whenever(marketplaceManagerApi).getProduct(any())

        val order = Fixtures.createOrder(id = orderId, businessId = merchant.businessId!!, accountId = merchant.id)
        doReturn(GetOrderResponse(order)).whenever(checkoutManagerApi).getOrder(any())

        val mtn = Fixtures.createPaymentProviderSummary(1, "MTN")
        doReturn(SearchPaymentProviderResponse(listOf(mtn))).whenever(checkoutManagerApi).searchPaymentProvider(any())
    }

    @Test
    fun `submit payment - PENDING then SUCCESSFUL`() {
        // Given
        doReturn(GetTransactionResponse(Fixtures.createTransaction(id = transactionId, status = Status.SUCCESSFUL)))
            .whenever(checkoutManagerApi).getTransaction(transactionId, true)

        // Goto order page
        navigate(url("processing?t=$transactionId"))
        Thread.sleep(60000)

        // Check payment page
        assertCurrentPageIs(Page.SUCCESS)
    }

    @Test
    fun `submit payment - SUCCESSFUL`() {
        // Given
        doReturn(GetTransactionResponse(Fixtures.createTransaction(id = transactionId, status = Status.SUCCESSFUL)))
            .whenever(checkoutManagerApi).getTransaction(transactionId)

        // Goto order page
        navigate(url("processing?t=$transactionId"))

        // Check payment page
        assertCurrentPageIs(Page.SUCCESS)
    }

    @Test
    fun `submit payment - PENDING then TRANSACTION_FAILED`() {
        // Given
        doReturn(
            GetTransactionResponse(
                Fixtures.createTransaction(
                    status = Status.FAILED,
                    errorCode = ErrorCode.APPROVAL_REJECTED,
                    orderId = orderId,
                ),
            ),
        )
            .whenever(checkoutManagerApi).getTransaction(transactionId, true)

        // Goto order page
        navigate(url("processing?t=$transactionId"))
        Thread.sleep(60000)

        // Enter data
        assertCurrentPageIs(Page.PAYMENT)
        assertElementPresent(".error")
    }

    @Test
    fun `submit payment - TRANSACTION_FAILED`() {
        // Given
        doReturn(
            GetTransactionResponse(
                Fixtures.createTransaction(
                    id = transactionId,
                    status = Status.FAILED,
                    errorCode = ErrorCode.APPROVAL_REJECTED,
                    orderId = orderId,
                ),
            ),
        )
            .whenever(checkoutManagerApi).getTransaction(transactionId)

        // Goto order page
        navigate(url("processing?t=$transactionId"))

        // Enter data
        assertCurrentPageIs(Page.PAYMENT)
        assertElementPresent(".error")
    }
}
