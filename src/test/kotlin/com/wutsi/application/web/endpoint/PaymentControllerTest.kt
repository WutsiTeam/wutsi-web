package com.wutsi.application.web.endpoint

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.web.Fixtures
import com.wutsi.application.web.Page
import com.wutsi.checkout.manager.dto.CreateChargeRequest
import com.wutsi.checkout.manager.dto.CreateChargeResponse
import com.wutsi.checkout.manager.dto.CreateOrderResponse
import com.wutsi.checkout.manager.dto.GetOrderResponse
import com.wutsi.checkout.manager.dto.GetTransactionResponse
import com.wutsi.checkout.manager.dto.SearchPaymentProviderResponse
import com.wutsi.enums.PaymentMethodType
import com.wutsi.enums.TransactionType
import com.wutsi.error.ErrorURN
import com.wutsi.marketplace.manager.dto.GetProductResponse
import com.wutsi.membership.manager.dto.GetMemberResponse
import com.wutsi.platform.payment.core.Status
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

internal class PaymentControllerTest : SeleniumTestSupport() {
    private val orderId = UUID.randomUUID().toString()
    private val transactionId = UUID.randomUUID().toString()
    private val localPhoneNumber = "670000010"
    private val phoneNumber = "+237$localPhoneNumber"
    private val idempotencyKey = UUID.randomUUID().toString()
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
    private val tx = Fixtures.createTransaction(
        transactionId,
        type = TransactionType.CHARGE,
        status = Status.PENDING,
        orderId = orderId,
    )

    @BeforeEach
    override fun setUp() {
        super.setUp()

        doReturn(GetMemberResponse(merchant)).whenever(membershipManagerApi).getMember(any())

        doReturn(GetProductResponse(product)).whenever(marketplaceManagerApi).getProduct(any())

        doReturn(CreateOrderResponse(orderId)).whenever(checkoutManagerApi).createOrder(any())
        doReturn(GetOrderResponse(order)).whenever(checkoutManagerApi).getOrder(orderId)

        doReturn(SearchPaymentProviderResponse(listOf(mtn))).whenever(checkoutManagerApi).searchPaymentProvider(any())

        doReturn(GetTransactionResponse(tx)).whenever(checkoutManagerApi).getTransaction(transactionId)
    }

    @Test
    fun `submit payment - SUCCESSFUL`() {
        // Given
        doReturn(CreateChargeResponse(transactionId, Status.SUCCESSFUL.name)).whenever(checkoutManagerApi)
            .createCharge(any())

        // Goto order page
        navigate(url("payment?o=${order.id}&i=$idempotencyKey"))
        assertCurrentPageIs(Page.PAYMENT)
        assertElementAttribute("#btn-submit", "wutsi-track-event", "payment")
        assertElementAttribute("#btn-submit", "wutsi-track-value", "${order.totalPrice}")

        // Enter data
        input("input[name=localPhoneNumber]", phoneNumber)

        // Submit the data
        click("#btn-submit")
        verify(checkoutManagerApi).createCharge(
            CreateChargeRequest(
                email = order.customerEmail,
                paymentMethodType = PaymentMethodType.MOBILE_MONEY.name,
                paymentProviderId = mtn.id,
                businessId = order.business.id,
                orderId = order.id,
                paymentMethodToken = null,
                idempotencyKey = idempotencyKey,
                paymenMethodNumber = phoneNumber,
                paymentMethodOwnerName = order.customerName,
            ),
        )

        // Check payment page
        assertCurrentPageIs(Page.SUCCESS)
    }

    @Test
    fun `submit payment - PENDING`() {
        // Given
        doReturn(CreateChargeResponse(transactionId, Status.PENDING.name)).whenever(checkoutManagerApi)
            .createCharge(any())

        // Goto order page
        navigate(url("payment?o=${order.id}&i=$idempotencyKey"))
        assertCurrentPageIs(Page.PAYMENT)

        // Enter data
        input("input[name=localPhoneNumber]", phoneNumber)

        // Submit the data
        click("#btn-submit")
        verify(checkoutManagerApi).createCharge(
            CreateChargeRequest(
                email = order.customerEmail,
                paymentMethodType = PaymentMethodType.MOBILE_MONEY.name,
                paymentProviderId = mtn.id,
                businessId = order.business.id,
                orderId = order.id,
                paymentMethodToken = null,
                idempotencyKey = idempotencyKey,
                paymenMethodNumber = phoneNumber,
                paymentMethodOwnerName = order.customerName,
            ),
        )

        // Check payment page
        assertCurrentPageIs(Page.PROCESSING)
    }

    @Test
    fun `submit payment - TRANSACTION_FAILED`() {
        // Given
        val ex = createFeignConflictException(ErrorURN.TRANSACTION_FAILED.urn)
        doThrow(ex).whenever(checkoutManagerApi).createCharge(any())

        // Goto order page
        navigate(url("payment?o=${order.id}&i=$idempotencyKey"))
        assertCurrentPageIs(Page.PAYMENT)

        // Enter data
        input("input[name=localPhoneNumber]", phoneNumber)

        // Submit the data
        click("#btn-submit")

        // Check payment page
        assertCurrentPageIs(Page.PAYMENT)
        assertElementPresent(".error")
    }

    @Test
    fun `submit payment - Unexpected Error`() {
        // Given
        doThrow(RuntimeException::class).whenever(checkoutManagerApi).createCharge(any())

        // Goto order page
        navigate(url("payment?o=${order.id}&i=$idempotencyKey"))
        assertCurrentPageIs(Page.PAYMENT)

        // Enter data
        input("input[name=localPhoneNumber]", phoneNumber)

        // Submit the data
        click("#btn-submit")

        // Check payment page
        assertCurrentPageIs(Page.PAYMENT)
        assertElementPresent(".error")
    }
}
