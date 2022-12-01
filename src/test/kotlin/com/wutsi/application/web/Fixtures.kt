package com.wutsi.application.web

import com.wutsi.checkout.manager.dto.Business
import com.wutsi.checkout.manager.dto.Discount
import com.wutsi.checkout.manager.dto.Order
import com.wutsi.checkout.manager.dto.OrderItem
import com.wutsi.checkout.manager.dto.PaymentProviderSummary
import com.wutsi.enums.ChannelType
import com.wutsi.enums.DeviceType
import com.wutsi.enums.DiscountType
import com.wutsi.enums.OrderStatus
import com.wutsi.marketplace.manager.dto.PictureSummary
import com.wutsi.marketplace.manager.dto.Product
import com.wutsi.marketplace.manager.dto.ProductSummary
import com.wutsi.marketplace.manager.dto.Store
import com.wutsi.membership.manager.dto.Category
import com.wutsi.membership.manager.dto.CategorySummary
import com.wutsi.membership.manager.dto.Member
import com.wutsi.membership.manager.dto.MemberSummary
import com.wutsi.membership.manager.dto.Place
import com.wutsi.membership.manager.dto.PlaceSummary
import java.time.OffsetDateTime
import java.time.ZoneOffset

object Fixtures {
    fun createMemberSummary() = MemberSummary()

    fun createMember(
        id: Long = -1,
        phoneNumber: String = "+237670000010",
        displayName: String = "Ray Sponsible",
        business: Boolean = false,
        storeId: Long? = null,
        businessId: Long? = null,
        country: String = "CM",
        superUser: Boolean = false,
        active: Boolean = true
    ) = Member(
        id = id,
        active = active,
        phoneNumber = phoneNumber,
        business = business,
        storeId = storeId,
        businessId = businessId,
        country = country,
        email = "ray.sponsible@gmail.com",
        displayName = displayName,
        language = "en",
        pictureUrl = "https://www.img.com/100.png",
        superUser = superUser,
        biography = "This is a biography",
        city = Place(
            id = 111,
            name = "Yaounde",
            longName = "Yaounde, Cameroun"
        ),
        category = Category(
            id = 555,
            title = "Ads"
        ),
        facebookId = "ray.sponsible",
        twitterId = "ray.sponsible",
        youtubeId = "ray.sponsible",
        instagramId = "ray.sponsible",
        website = "https://www.ray-sponsible.com",
        whatsapp = true
    )

    fun createPlaceSummary(id: Long = -1, name: String = "Yaounde") = PlaceSummary(
        id = id,
        name = name
    )

    fun createCategorySummary(id: Long = -1, title: String = "Art") = CategorySummary(
        id = id,
        title = title
    )

    fun createProductSummary(
        id: Long = -1,
        title: String = "Product",
        thumbnailUrl: String? = null,
        published: Boolean = true,
        price: Long = 15000
    ) = ProductSummary(
        id = id,
        title = title,
        thumbnailUrl = thumbnailUrl,
        status = if (published) "PUBLISHED" else "DRAFT",
        price = price
    )

    fun createProduct(
        id: Long = -1,
        storeId: Long = -1,
        title: String = "Product A",
        quantity: Int = 10,
        price: Long = 20000L,
        summary: String = "This is a summary",
        description: String = "This is a long description",
        pictures: List<PictureSummary> = emptyList(),
        published: Boolean = true
    ) = Product(
        id = id,
        storeId = storeId,
        title = title,
        quantity = quantity,
        price = price,
        summary = summary,
        description = description,
        thumbnail = if (pictures.isEmpty()) null else pictures[0],
        pictures = pictures,
        status = if (published) "PUBLISHED" else "DRAFT"
    )

    fun createPictureSummary(
        id: Long = -1,
        url: String = "http://www.google.com/1.png"
    ) = PictureSummary(
        id = id,
        url = url
    )

    fun createPictureSummaryList(size: Int): List<PictureSummary> {
        val pictures = mutableListOf<PictureSummary>()
        for (i in 0..size) {
            pictures.add(
                PictureSummary(
                    id = i.toLong(),
                    url = "https://img.com/$i.png"
                )
            )
        }
        return pictures
    }

    fun createStore(id: Long, accountId: Long) = Store(
        id = id,
        accountId = accountId
    )

    fun createBusiness(id: Long, accountId: Long, country: String, currency: String) = Business(
        id = id,
        accountId = accountId,
        country = country,
        currency = currency
    )

    fun createPaymentProviderSummary(id: Long, code: String) = PaymentProviderSummary(
        id = id,
        code = code,
        name = code,
        logoUrl = "https://www.imgs.com/$id.png"
    )

    fun createOrder(
        id: String,
        businessId: Long = -1,
        totalPrice: Long = 100000L,
        status: OrderStatus = OrderStatus.UNKNOWN
    ) = Order(
        id = id,
        businessId = businessId,
        totalPrice = totalPrice,
        balance = totalPrice,
        status = status.name,
        customerName = "Ray Sponsible",
        customerEmail = "ray.sponsible@gmail.com",
        deviceType = DeviceType.MOBILE.name,
        channelType = ChannelType.WEB.name,
        currency = "XAF",
        notes = "Yo man",
        deviceId = "4309403-43094039-43094309",
        discounts = listOf(
            Discount(
                code = "111",
                amount = 1000,
                rate = 0,
                type = DiscountType.DYNAMIC.name
            )
        ),
        items = listOf(
            OrderItem(
                productId = 999,
                quantity = 3,
                title = "This is a product",
                pictureUrl = "https://img.com/1.png",
                totalPrice = totalPrice,
                unitPrice = totalPrice / 3,
                totalDiscount = 100,
                discounts = listOf(
                    Discount(
                        code = "111",
                        amount = 1000,
                        rate = 0,
                        type = DiscountType.DYNAMIC.name
                    )
                )
            )
        ),
        created = OffsetDateTime.of(2020, 1, 1, 10, 30, 0, 0, ZoneOffset.UTC),
        updated = OffsetDateTime.of(2020, 1, 1, 10, 30, 0, 0, ZoneOffset.UTC),
        expires = OffsetDateTime.of(2100, 1, 1, 10, 30, 0, 0, ZoneOffset.UTC)
    )
}
