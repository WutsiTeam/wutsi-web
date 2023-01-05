package com.wutsi.application.web.endpoint

import com.wutsi.application.web.model.BusinessModel
import com.wutsi.application.web.model.EventModel
import com.wutsi.application.web.model.FileType
import com.wutsi.application.web.model.MemberModel
import com.wutsi.application.web.model.OfferModel
import com.wutsi.application.web.model.OfferPriceModel
import com.wutsi.application.web.model.OrderItemModel
import com.wutsi.application.web.model.OrderModel
import com.wutsi.application.web.model.PaymentProviderModel
import com.wutsi.application.web.model.PictureModel
import com.wutsi.application.web.model.ProductModel
import com.wutsi.application.web.model.TransactionModel
import com.wutsi.application.web.util.DateTimeUtil
import com.wutsi.application.web.util.HandleGenerator
import com.wutsi.checkout.manager.dto.Business
import com.wutsi.checkout.manager.dto.BusinessSummary
import com.wutsi.checkout.manager.dto.Order
import com.wutsi.checkout.manager.dto.PaymentProviderSummary
import com.wutsi.checkout.manager.dto.Transaction
import com.wutsi.enums.ProductType
import com.wutsi.marketplace.manager.dto.Event
import com.wutsi.marketplace.manager.dto.Offer
import com.wutsi.marketplace.manager.dto.OfferSummary
import com.wutsi.marketplace.manager.dto.PictureSummary
import com.wutsi.marketplace.manager.dto.Product
import com.wutsi.marketplace.manager.dto.ProductPriceSummary
import com.wutsi.marketplace.manager.dto.ProductSummary
import com.wutsi.membership.manager.dto.Member
import com.wutsi.platform.core.image.Dimension
import com.wutsi.platform.core.image.Focus
import com.wutsi.platform.core.image.ImageService
import com.wutsi.platform.core.image.Transformation
import com.wutsi.regulation.Country
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service
import java.text.DecimalFormat
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Service
class Mapper(
    private val imageService: ImageService,
) {
    companion object {
        const val PROFILE_PICTURE_WIDTH = 64
        const val PROFILE_PICTURE_HEIGHT = 64
        const val PRODUCT_THUMBNAIL_HEIGHT = 300
        const val PRODUCT_THUMBNAIL_WIDTH = 300
        const val PRODUCT_PICTURE_HEIGHT = 512
        const val PRODUCT_PICTURE_WIDTH = 512
    }

    fun toOrderModel(order: Order, country: Country): OrderModel {
        val fmt = DecimalFormat(country.monetaryFormat)
        return OrderModel(
            id = order.id,
            business = toBusinessModel(order.business),
            customerEmail = order.customerEmail,
            customerName = order.customerName,
            totalPrice = fmt.format(order.totalPrice),
            totalDiscount = fmt.format(order.totalDiscount),
            items = order.items.map {
                OrderItemModel(
                    productId = it.productId,
                    title = it.title,
                    pictureUrl = it.pictureUrl,
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
        url = toMemberUrl(member.id),
        pictureUrl = member.pictureUrl?.let {
            imageService.transform(
                url = it,
                transformation = Transformation(
                    focus = Focus.FACE,
                    dimension = Dimension(
                        width = PROFILE_PICTURE_WIDTH,
                        height = PROFILE_PICTURE_HEIGHT,
                    ),
                ),
            )
        },
        business = business?.let { toBusinessModel(it) },
    )

    fun toMemberUrl(memberId: Long): String =
        "/u/$memberId"

    fun toProductModel(product: ProductSummary, country: Country, merchant: Member) = ProductModel(
        id = product.id,
        title = product.title,
        price = DecimalFormat(country.monetaryFormat).format(product.price),
        url = toProductUrl(product.id, product.title),
        quantity = product.quantity,
        available = product.quantity != null && product.quantity!! > 0,
        thumbnailUrl = product.thumbnailUrl?.let {
            imageService.transform(
                url = it,
                transformation = Transformation(
                    dimension = Dimension(
                        height = PRODUCT_THUMBNAIL_HEIGHT,
                        width = PRODUCT_THUMBNAIL_WIDTH,
                    ),
                ),
            )
        },
        summary = toString(product.summary),
        type = product.type,
        event = if (product.type == ProductType.EVENT.name) toEvent(product.event, country, merchant) else null,
    )

    private fun toExtension(name: String): String? {
        val i = name.lastIndexOf(".")
        return if (i > 0) {
            name.substring(i + 1).uppercase()
        } else {
            null
        }
    }

    fun toProductModel(product: Product, country: Country, merchant: Member) = ProductModel(
        id = product.id,
        title = product.title,
        price = DecimalFormat(country.monetaryFormat).format(product.price),
        summary = toString(product.summary),
        description = toString(product.description),
        available = product.quantity != null && product.quantity!! > 0,
        quantity = product.quantity,
        url = toProductUrl(product.id, product.title),
        thumbnailUrl = product.thumbnail?.url?.let {
            imageService.transform(
                url = it,
                transformation = Transformation(
                    dimension = Dimension(
                        height = PRODUCT_PICTURE_WIDTH,
                        width = PRODUCT_PICTURE_HEIGHT,
                    ),
                ),
            )
        },
        pictures = product.pictures.map { toPictureMapper(it) },
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
        amount = DecimalFormat(country.monetaryFormat).format(tx.amount),
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

    fun toOfferModel(offer: Offer, country: Country, member: Member) = OfferModel(
        product = toProductModel(offer.product, country, member),
        price = toOfferPriceModel(offer.price, country),
    )

    fun toOfferPriceModel(offerPrice: ProductPriceSummary, country: Country) = OfferPriceModel(
        price = DecimalFormat(country.monetaryFormat).format(offerPrice.price),
        referencePrice = offerPrice.referencePrice?.let { DecimalFormat(country.monetaryFormat).format(it) },
        savings = if (offerPrice.savings > 0) DecimalFormat(country.monetaryFormat).format(offerPrice.savings) else null,
        savingsPercentage = if (offerPrice.savingsPercentage > 0) "${offerPrice.savingsPercentage}%" else null,
        expiresHours = offerPrice.expires?.let { getExpiryText(it) },
    )

    fun getExpiryText(date: OffsetDateTime): Int? {
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val duration = Duration.between(date, now)
        return if (duration.toDays() > 2) {
            null
        } else {
            duration.toHours().toInt()
        }
    }

    private fun toPictureMapper(picture: PictureSummary) = PictureModel(
        url = imageService.transform(
            url = picture.url,
            transformation = Transformation(
                dimension = Dimension(
                    height = PRODUCT_PICTURE_WIDTH,
                    width = PRODUCT_PICTURE_HEIGHT,
                ),
            ),
        ),
    )

    private fun toProductUrl(id: Long, title: String): String =
        "/p/$id/" + HandleGenerator.generate(title)

    private fun toString(str: String?): String? =
        if (str.isNullOrEmpty()) {
            null
        } else {
            str
        }
}
