package com.wutsi.application.web.model

import com.wutsi.application.web.util.DateTimeUtil
import com.wutsi.checkout.manager.dto.Business
import com.wutsi.checkout.manager.dto.BusinessSummary
import com.wutsi.checkout.manager.dto.Order
import com.wutsi.checkout.manager.dto.PaymentProviderSummary
import com.wutsi.checkout.manager.dto.Transaction
import com.wutsi.enums.ProductType
import com.wutsi.marketplace.manager.dto.CancellationPolicy
import com.wutsi.marketplace.manager.dto.Event
import com.wutsi.marketplace.manager.dto.Offer
import com.wutsi.marketplace.manager.dto.OfferPrice
import com.wutsi.marketplace.manager.dto.OfferSummary
import com.wutsi.marketplace.manager.dto.PictureSummary
import com.wutsi.marketplace.manager.dto.Product
import com.wutsi.marketplace.manager.dto.ProductSummary
import com.wutsi.marketplace.manager.dto.ReturnPolicy
import com.wutsi.marketplace.manager.dto.Store
import com.wutsi.membership.manager.dto.Member
import com.wutsi.membership.manager.dto.MemberSummary
import com.wutsi.platform.core.image.Dimension
import com.wutsi.platform.core.image.ImageService
import com.wutsi.platform.core.image.Transformation
import com.wutsi.regulation.Country
import com.wutsi.regulation.RegulationEngine
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Service
class Mapper(
    private val imageService: ImageService,
    private val regulationEngine: RegulationEngine,
    @Value("\${wutsi.application.server-url}") private val serverUrl: String,
) {
    companion object {
        const val PRODUCT_THUMBNAIL_WIDTH = 300
        const val PRODUCT_PICTURE_WIDTH = 512
    }

    fun toUrlModel(product: ProductSummary) = UrlModel(
        loc = serverUrl + product.url,
    )

    fun toUrlModel(member: Member) = UrlModel(
        loc = serverUrl + toMemberUrl(member.id, member.name),
    )

    fun toUrlModel(member: MemberSummary) = UrlModel(
        loc = serverUrl + toMemberUrl(member.id, member.name),
    )

    fun toOrderModel(order: Order, country: Country): OrderModel {
        val fmt = country.createMoneyFormat()
        return OrderModel(
            id = order.id,
            business = toBusinessModel(order.business),
            customerEmail = order.customerEmail,
            customerName = order.customerName,
            totalPrice = fmt.format(order.totalPrice),
            totalDiscount = fmt.format(order.totalDiscount),
            totalPriceValue = order.totalPrice,
            items = order.items.map {
                OrderItemModel(
                    productId = it.productId,
                    title = it.title,
                    pictureUrl = it.pictureUrl?.let {
                        imageService.transform(
                            url = it,
                            transformation = Transformation(
                                dimension = Dimension(
                                    width = PRODUCT_PICTURE_WIDTH,
                                ),
                            ),
                        )
                    },
                    quantity = it.quantity,
                    unitPrice = fmt.format(it.unitPrice),
                )
            },
        )
    }

    fun toPaymentProviderModel(provider: PaymentProviderSummary) = PaymentProviderModel(
        logoUrl = provider.logoUrl,
        name = provider.name,
    )

    fun toMemberModel(member: Member, business: Business? = null) = MemberModel(
        id = member.id,
        name = member.name,
        businessId = member.businessId,
        displayName = member.displayName,
        biography = toString(member.biography),
        category = member.category?.title,
        location = member.city?.longName,
        phoneNumber = member.phoneNumber,
        whatsapp = member.whatsapp,
        facebookId = member.facebookId,
        instagramId = member.instagramId,
        twitterId = member.twitterId,
        youtubeId = member.youtubeId,
        website = member.website,
        url = toMemberUrl(member.id, member.name),
        pictureUrl = member.pictureUrl,
        business = business?.let { toBusinessModel(it) },
    )

    fun toMemberUrl(memberId: Long, name: String?): String =
        name?.let { "/@$name" } ?: "/u/$memberId"

    fun toProductModel(product: ProductSummary, country: Country, merchant: Member) = ProductModel(
        id = product.id,
        title = product.title,
        price = country.createMoneyFormat().format(product.price),
        currency = product.currency,
        url = product.url,
        quantity = product.quantity,
        outOfStock = product.outOfStock,
        lowStock = !product.outOfStock && product.quantity != null && product.quantity!! <= regulationEngine.lowStockThreshold(),
        thumbnail = product.thumbnailUrl?.let { toPictureMapper(it, true) },
        summary = toString(product.summary),
        type = product.type,
        event = if (product.type == ProductType.EVENT.name) toEvent(product.event, country, merchant) else null,
    )

    fun toProductModel(product: Product, country: Country, merchant: Member) = ProductModel(
        id = product.id,
        title = product.title,
        price = country.createMoneyFormat().format(product.price),
        currency = product.currency,
        summary = toString(product.summary),
        description = toHtml(product.description),
        quantity = product.quantity,
        outOfStock = product.outOfStock,
        lowStock = !product.outOfStock && product.quantity != null && product.quantity!! <= regulationEngine.lowStockThreshold(),
        url = product.url,
        thumbnail = product.thumbnail?.let { toPictureMapper(it, true) },
        pictures = product.pictures.map { toPictureMapper(it, false) },
        type = product.type,
        event = if (product.type == ProductType.EVENT.name) toEvent(product.event, country, merchant) else null,

        fileTypes = product.files.groupBy { toExtension(it.name) }
            .filter { it.key != null }
            .map {
                FileType(
                    type = it.key!!.uppercase(),
                    count = it.value.size,
                )
            },
    )

    fun toCancellationPolicyModel(policy: CancellationPolicy) = CancellationPolicyModel(
        accepted = policy.accepted,
        message = toString(policy.message),
        windowHours = policy.window,
    )

    fun toReturnPolicyModel(policy: ReturnPolicy) = ReturnPolicyModel(
        accepted = policy.accepted,
        message = toString(policy.message),
        contactWindowDays = policy.contactWindow / 24,
        shipBackWindowDays = policy.shipBackWindow / 24,
    )

    private fun canCancel(product: Product): Boolean =
        product.type != ProductType.DIGITAL_DOWNLOAD.name

    private fun canReturn(product: Product): Boolean =
        product.type == ProductType.PHYSICAL_PRODUCT.name

    private fun toExtension(name: String): String? {
        val i = name.lastIndexOf(".")
        return if (i > 0) {
            name.substring(i + 1).uppercase()
        } else {
            null
        }
    }

    fun toEvent(event: Event?, country: Country, merchant: Member): EventModel? {
        if (event == null) {
            return null
        }

        val locale = LocaleContextHolder.getLocale()
        val dateTimeFormat = DateTimeFormatter.ofPattern(country.dateTimeFormat, locale)
        val dateFormat = DateTimeFormatter.ofPattern(country.dateFormat, locale)
        val timeFormat = DateTimeFormatter.ofPattern(country.timeFormat, locale)
        val starts = event.starts?.let { DateTimeUtil.convert(it, merchant.timezoneId) }
        val ends = event.ends?.let { DateTimeUtil.convert(it, merchant.timezoneId) }

        return EventModel(
            online = event.online,
            meetingProviderLogoUrl = event.meetingProvider?.logoUrl,
            meetingProviderName = event.meetingProvider?.name,
            startDateTime = starts?.format(dateTimeFormat),
            startDate = starts?.format(dateFormat),
            startTime = starts?.format(timeFormat),
            endTime = ends?.format(timeFormat),
        )
    }

    fun toTransactionModel(tx: Transaction, country: Country) = TransactionModel(
        id = tx.id,
        type = tx.type,
        status = tx.status,
        amount = country.monetaryFormat.format(tx.amount),
        amountValue = tx.amount,
        email = tx.email,
    )

    fun toBusinessModel(business: Business) = BusinessModel(
        id = business.id,
        country = business.country,
        currency = business.currency,
        totalOrders = business.totalOrders,
        totalSales = business.totalSales,
    )

    fun toBusinessModel(business: BusinessSummary) = BusinessModel(
        id = business.id,
        country = business.country,
        currency = business.currency,
    )

    fun toOfferModel(offer: OfferSummary, country: Country, member: Member) = OfferModel(
        product = toProductModel(offer.product, country, member),
        price = toOfferPriceModel(offer.price, country),
    )

    fun toOfferModel(offer: Offer, country: Country, member: Member, store: Store) = OfferModel(
        product = toProductModel(offer.product, country, member),
        price = toOfferPriceModel(offer.price, country),
        cancellationPolicy = if (canCancel(offer.product)) {
            toCancellationPolicyModel(store.cancellationPolicy)
        } else {
            CancellationPolicyModel(accepted = false)
        },

        returnPolicy = if (canReturn(offer.product)) {
            toReturnPolicyModel(store.returnPolicy)
        } else {
            ReturnPolicyModel(accepted = false)
        },
    )

    fun toOfferPriceModel(offerPrice: OfferPrice, country: Country): OfferPriceModel {
        val fmt = country.createMoneyFormat()
        return OfferPriceModel(
            price = fmt.format(offerPrice.price),
            referencePrice = offerPrice.referencePrice?.let { fmt.format(it) },
            savings = if (offerPrice.savings > 0) fmt.format(offerPrice.savings) else null,
            savingsPercentage = if (offerPrice.savingsPercentage > 0) "${offerPrice.savingsPercentage}%" else null,
            expires = offerPrice.expires,
            expiresHours = offerPrice.expires?.let { getExpiryHours(it) },
            expiresMinutes = offerPrice.expires?.let { getExpiryMinutes(it) },
        )
    }

    fun getExpiryHours(date: OffsetDateTime): Int? {
        val hours = getExpiryDuration(date).toHours()
        return if (hours in 1..24L) {
            hours.toInt()
        } else {
            null
        }
    }

    fun getExpiryMinutes(date: OffsetDateTime): Int? {
        val minutes = getExpiryDuration(date).toMinutes()
        return if (minutes > 0) {
            minutes.toInt()
        } else {
            null
        }
    }

    fun getExpiryDuration(date: OffsetDateTime): Duration {
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        return Duration.between(now, date)
    }

    private fun toPictureMapper(picture: PictureSummary, thumbnail: Boolean): PictureModel =
        toPictureMapper(picture.url, thumbnail)

    private fun toPictureMapper(url: String, thumbnail: Boolean) = PictureModel(
        url = imageService.transform(
            url = url,
            transformation = Transformation(
                dimension = Dimension(
                    width = if (thumbnail) PRODUCT_THUMBNAIL_WIDTH else PRODUCT_PICTURE_WIDTH,
                ),
            ),
        ),
        originalUrl = url,
    )

    private fun toString(str: String?): String? =
        if (str?.trim().isNullOrEmpty()) {
            null
        } else {
            str
        }

    private fun toHtml(str: String?): String? =
        toString(str)?.replace("\n", "<br/>")
}
