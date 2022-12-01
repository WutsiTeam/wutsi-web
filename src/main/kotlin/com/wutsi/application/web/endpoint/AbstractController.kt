package com.wutsi.application.web.endpoint

import com.wutsi.checkout.manager.CheckoutManagerApi
import com.wutsi.enums.ProductStatus
import com.wutsi.error.ErrorURN
import com.wutsi.marketplace.manager.MarketplaceManagerApi
import com.wutsi.marketplace.manager.dto.Product
import com.wutsi.marketplace.manager.dto.Store
import com.wutsi.membership.manager.MembershipManagerApi
import com.wutsi.membership.manager.dto.Member
import com.wutsi.platform.core.error.Error
import com.wutsi.platform.core.error.exception.NotFoundException
import com.wutsi.regulation.RegulationEngine
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.mobile.device.Device
import org.springframework.mobile.device.DeviceUtils
import org.springframework.web.bind.annotation.ModelAttribute
import javax.servlet.http.HttpServletRequest

abstract class AbstractController {
    @Autowired
    protected lateinit var membershipManagerApi: MembershipManagerApi

    @Autowired
    protected lateinit var marketplaceManagerApi: MarketplaceManagerApi

    @Autowired
    protected lateinit var checkoutManagerApi: CheckoutManagerApi

    @Autowired
    protected lateinit var regulationEngine: RegulationEngine

    @Autowired
    protected lateinit var mapper: Mapper

    @Value("\${wutsi.application.asset-url}")
    protected lateinit var assetUrl: String

    @Value("\${wutsi.application.server-url}")
    protected lateinit var serverUrl: String

    @Autowired
    protected lateinit var request: HttpServletRequest

    @ModelAttribute("device")
    fun device(): Device = DeviceUtils.getCurrentDevice(request)

    @ModelAttribute("assetUrl")
    fun assetUrl() = assetUrl

    protected fun findMember(id: Long): Member {
        val member = membershipManagerApi.getMember(id).member
        if (!member.active) { // Must be active
            throw NotFoundException(
                error = Error(
                    code = ErrorURN.MEMBER_NOT_ACTIVE.urn
                )
            )
        }
        if (!member.business) { // Must be a business account
            throw NotFoundException(
                error = Error(
                    code = ErrorURN.MEMBER_NOT_BUSINESS.urn
                )
            )
        }
        return member
    }

    protected fun findProduct(id: Long): Product {
        val product = marketplaceManagerApi.getProduct(id).product
        if (product.status != ProductStatus.PUBLISHED.name) {
            throw NotFoundException(
                error = Error(
                    code = ErrorURN.PRODUCT_NOT_PUBLISHED.urn
                )
            )
        }
        return product
    }
}
