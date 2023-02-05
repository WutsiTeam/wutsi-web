package com.wutsi.application.web.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.application.web.Page
import com.wutsi.application.web.dto.ChargeOrderRequest
import com.wutsi.application.web.model.PageModel
import com.wutsi.application.web.util.ErrorCode.INVALID_PHONE_NUMBER
import com.wutsi.application.web.util.ErrorCode.ORDER_EXPIRED
import com.wutsi.application.web.util.ErrorCode.TRANSACTION_FAILED
import com.wutsi.application.web.util.ErrorCode.UNEXPECTED
import com.wutsi.checkout.manager.dto.CreateChargeRequest
import com.wutsi.checkout.manager.dto.SearchPaymentProviderRequest
import com.wutsi.enums.PaymentMethodType
import com.wutsi.error.ErrorURN
import com.wutsi.platform.core.error.Error
import com.wutsi.platform.core.error.ErrorResponse
import com.wutsi.platform.core.error.Parameter
import com.wutsi.platform.core.error.ParameterType
import com.wutsi.platform.core.error.exception.BadRequestException
import com.wutsi.platform.payment.core.Status
import feign.FeignException
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

@Controller
@RequestMapping("/payment")
class PaymentController(
    private val objectMapper: ObjectMapper,
    private val messages: MessageSource,
) : AbstractController() {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(PaymentController::class.java)
    }

    @GetMapping
    fun index(
        @RequestParam(name = "o") orderId: String,
        @RequestParam(name = "i") idempotencyKey: String,
        @RequestParam(name = "e", required = false) error: Long? = null,
        @RequestParam(name = "code", required = false) code: String? = null,
        model: Model,
    ): String {
        checkIdempotencyKey(idempotencyKey)

        val order = checkoutManagerApi.getOrder(orderId).order
        val country = regulationEngine.country(order.business.country)
        val merchant = resolveCurrentMerchant(order.business.accountId)
        val paymentProviders = checkoutManagerApi.searchPaymentProvider(
            request = SearchPaymentProviderRequest(
                country = order.business.country,
            ),
        ).paymentProviders

        model.addAttribute("idempotencyKey", idempotencyKey)
        model.addAttribute("page", createPage())
        model.addAttribute("order", mapper.toOrderModel(order, country))
        model.addAttribute("merchant", mapper.toMemberModel(merchant))
        model.addAttribute("error", error?.let { toError(it, code) })
        model.addAttribute(
            "mobileProviders",
            paymentProviders
                .filter { it.type == PaymentMethodType.MOBILE_MONEY.name }
                .map { mapper.toPaymentProviderModel(it) },
        )

        return "payment"
    }

    @PostMapping("/submit")
    fun submit(@ModelAttribute request: ChargeOrderRequest): String {
        logger.add("request_phone_number", request.phoneNumber)
        logger.add("request_payment_method_type", request.paymentMethodType)
        logger.add("request_idempotency_key", request.idempotencyKey)
        logger.add("request_order_id", request.orderId)
        logger.add("request_business_id", request.businessId)

        // Get provider
        val providers = checkoutManagerApi.searchPaymentProvider(
            request = SearchPaymentProviderRequest(
                number = request.phoneNumber,
                type = request.paymentMethodType.name,
            ),
        ).paymentProviders
        logger.add("payment_providers", providers.map { it.code })
        if (providers.size != 1) {
            return redirectToError(request.orderId, INVALID_PHONE_NUMBER)
        }

        // Charge
        try {
            val order = checkoutManagerApi.getOrder(request.orderId).order
            val response = checkoutManagerApi.createCharge(
                request = CreateChargeRequest(
                    email = order.customerEmail,
                    paymentMethodType = request.paymentMethodType.name,
                    paymentMethodOwnerName = order.customerName,
                    paymenMethodNumber = request.phoneNumber,
                    businessId = request.businessId,
                    orderId = request.orderId,
                    paymentProviderId = providers[0].id,
                    idempotencyKey = request.idempotencyKey,
                ),
            )
            logger.add("transaction_id", response.transactionId)
            logger.add("status", response.status)

            return if (response.status == Status.SUCCESSFUL.name) {
                "redirect:/success?t=${response.transactionId}"
            } else {
                "redirect:/processing?t=${response.transactionId}"
            }
        } catch (ex: FeignException) {
            LOGGER.error("Unexpected error", ex)
            try {
                val response = objectMapper.readValue(ex.contentUTF8(), ErrorResponse::class.java)
                return when (response.error.code) {
                    ErrorURN.ORDER_EXPIRED.urn -> redirectToError(request.orderId, ORDER_EXPIRED, ex)
                    else -> redirectToError(request.orderId, TRANSACTION_FAILED, ex)
                }
            } catch (ex1: Throwable) {
                return redirectToError(request.orderId, TRANSACTION_FAILED, ex)
            }
        } catch (ex: Exception) {
            return redirectToError(request.orderId, UNEXPECTED, ex)
        }
    }

    fun redirectToError(orderId: String, error: Long, ex: Exception? = null): String {
        val idempotencyKey = UUID.randomUUID().toString()
        if (error == INVALID_PHONE_NUMBER) {
            return "redirect:/payment?o=$orderId&e=$error&i=$idempotencyKey"
        } else {
            if (ex is FeignException) {
                try {
                    val response = objectMapper.readValue(ex.contentUTF8(), ErrorResponse::class.java)
                    val err = when (response.error.code) {
                        ErrorURN.ORDER_EXPIRED.urn -> ORDER_EXPIRED
                        ErrorURN.TRANSACTION_FAILED.urn -> TRANSACTION_FAILED
                        else -> UNEXPECTED
                    }
                    return "redirect:/payment?o=$orderId&e=$err&i=$idempotencyKey" +
                        "&code=" + (response.error.downstreamCode ?: "")
                } catch (e: Exception) {
                    // Nothing
                }

                return "redirect:/payment?o=$orderId&e=$UNEXPECTED&i=$idempotencyKey"
            } else {
                return "redirect:/payment?o=$orderId&e=$UNEXPECTED&i=$idempotencyKey"
            }
        }
    }

    private fun checkIdempotencyKey(idempotencyKey: String) {
        try {
            UUID.fromString(idempotencyKey)
        } catch (exception: IllegalArgumentException) {
            throw BadRequestException(
                error = Error(
                    code = ErrorURN.IDEMPOTENCY_KEY_NOT_VALID.urn,
                    parameter = Parameter(
                        value = idempotencyKey,
                        type = ParameterType.PARAMETER_TYPE_QUERY,
                    ),
                ),
            )
        }
    }

    private fun toError(error: Long, code: String?): String? = when (error) {
        TRANSACTION_FAILED -> {
            val message1 = messages.getMessage(
                "error-message.transaction-failed",
                emptyArray(),
                LocaleContextHolder.getLocale(),
            )
            val message2 = if (code == null) {
                ""
            } else {
                try {
                    messages.getMessage(
                        "error-message.$code",
                        emptyArray(),
                        LocaleContextHolder.getLocale(),
                    )
                } catch (ex: Exception) {
                    ""
                }
            }
            "$message1 $message2"
        }
        INVALID_PHONE_NUMBER -> messages.getMessage(
            "error-message.no-provider-for-phone-number",
            emptyArray(),
            LocaleContextHolder.getLocale(),
        )
        ORDER_EXPIRED -> messages.getMessage(
            "error-message.order-expired",
            emptyArray(),
            LocaleContextHolder.getLocale(),
        )
        else -> messages.getMessage("error-message.unexpected", emptyArray(), LocaleContextHolder.getLocale())
    }

    private fun createPage() = PageModel(
        name = Page.PAYMENT,
        title = "Order",
        robots = "noindex",
    )
}
