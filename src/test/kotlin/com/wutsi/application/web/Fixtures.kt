package com.wutsi.application.web

import com.wutsi.checkout.manager.dto.Business
import com.wutsi.checkout.manager.dto.BusinessSummary
import com.wutsi.checkout.manager.dto.Order
import com.wutsi.checkout.manager.dto.OrderItem
import com.wutsi.checkout.manager.dto.PaymentMethodSummary
import com.wutsi.checkout.manager.dto.PaymentProviderSummary
import com.wutsi.checkout.manager.dto.Transaction
import com.wutsi.enums.BusinessStatus
import com.wutsi.enums.ChannelType
import com.wutsi.enums.DeviceType
import com.wutsi.enums.DiscountType
import com.wutsi.enums.MeetingProviderType
import com.wutsi.enums.OrderStatus
import com.wutsi.enums.PaymentMethodStatus
import com.wutsi.enums.PaymentMethodType
import com.wutsi.enums.ProductType
import com.wutsi.enums.TransactionType
import com.wutsi.marketplace.manager.dto.DiscountSummary
import com.wutsi.marketplace.manager.dto.Event
import com.wutsi.marketplace.manager.dto.FileSummary
import com.wutsi.marketplace.manager.dto.MeetingProviderSummary
import com.wutsi.marketplace.manager.dto.Offer
import com.wutsi.marketplace.manager.dto.OfferPrice
import com.wutsi.marketplace.manager.dto.OfferSummary
import com.wutsi.marketplace.manager.dto.PictureSummary
import com.wutsi.marketplace.manager.dto.Product
import com.wutsi.marketplace.manager.dto.ProductSummary
import com.wutsi.marketplace.manager.dto.Store
import com.wutsi.marketplace.manager.dto.StoreSummary
import com.wutsi.membership.manager.dto.Category
import com.wutsi.membership.manager.dto.CategorySummary
import com.wutsi.membership.manager.dto.Member
import com.wutsi.membership.manager.dto.MemberSummary
import com.wutsi.membership.manager.dto.Place
import com.wutsi.membership.manager.dto.PlaceSummary
import com.wutsi.platform.payment.GatewayType
import com.wutsi.platform.payment.core.ErrorCode
import com.wutsi.platform.payment.core.Status
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

object Fixtures {
    fun createMemberSummary(id: Long = -1, name: String? = null) = MemberSummary(id, name = name)

    fun createMember(
        id: Long = -1,
        phoneNumber: String = "+237670000010",
        displayName: String = "Waxville",
        business: Boolean = false,
        storeId: Long? = null,
        businessId: Long? = null,
        country: String = "CM",
        superUser: Boolean = false,
        active: Boolean = true,
        pictureUrl: String = "https://static6.depositphotos.com/1005993/633/v/450/depositphotos_6338152-stock-illustration-real-estate-logo.jpg",
        name: String? = null,
    ) = Member(
        id = id,
        active = active,
        name = name,
        phoneNumber = phoneNumber,
        business = business,
        storeId = storeId,
        businessId = businessId,
        country = country,
        email = "info@waxville.com",
        displayName = displayName,
        language = "en",
        pictureUrl = pictureUrl,
        superUser = superUser,
        biography = "Notre mission est de valoriser le pagne en l’utilisant pour créer des modèles branchés pour le rendre accessible aux petits et grands budgets",
        city = Place(
            id = 111,
            name = "Yaounde",
            longName = "Yaounde, Cameroun",
        ),
        category = Category(
            id = 555,
            title = "Achat de vente de detail",
        ),
        facebookId = "ray.sponsible",
        twitterId = "ray.sponsible",
        youtubeId = "ray.sponsible",
        instagramId = "ray.sponsible",
        website = "https://www.ray-sponsible.com",
        whatsapp = true,
    )

    fun createPlaceSummary(id: Long = -1, name: String = "Yaounde") = PlaceSummary(
        id = id,
        name = name,
    )

    fun createCategorySummary(id: Long = -1, title: String = "Art") = CategorySummary(
        id = id,
        title = title,
    )

    fun createProductSummary(
        id: Long = -1,
        title: String = "Product",
        thumbnailUrl: String? = null,
        published: Boolean = true,
        price: Long = 15000,
        type: ProductType = ProductType.PHYSICAL_PRODUCT,
        event: Event? = null,
        quantity: Int = 10,
    ) = ProductSummary(
        id = id,
        title = title,
        thumbnailUrl = thumbnailUrl,
        status = if (published) "PUBLISHED" else "DRAFT",
        price = price,
        type = type.name,
        event = event,
        outOfStock = quantity <= 0,
        quantity = quantity,
        url = "/p/$id",
    )

    fun createFileSummary(
        id: Long,
        name: String = "$id.png",
        url: String = "https://www.img.com/$id.png",
        contentSize: Int = 12000,
        contentType: String = "image/png",
    ) = FileSummary(
        id = id,
        url = url,
        name = name,
        contentType = contentType,
        contentSize = contentSize,
        created = OffsetDateTime.of(2020, 1, 1, 10, 30, 0, 0, ZoneOffset.UTC),
    )

    fun createProduct(
        id: Long = -1,
        storeId: Long = -1,
        accountId: Long = -1,
        title: String = "Product A",
        quantity: Int = 10,
        price: Long = 20000L,
        summary: String = "This is a summary",
        description: String = "This is a long description",
        pictures: List<PictureSummary> = emptyList(),
        published: Boolean = true,
        type: ProductType = ProductType.PHYSICAL_PRODUCT,
        event: Event? = null,
        files: List<FileSummary> = emptyList(),
    ) = Product(
        id = id,
        store = StoreSummary(
            id = storeId,
            accountId = accountId,
            currency = "XAF",
        ),
        title = title,
        quantity = quantity,
        price = price,
        summary = summary,
        description = description,
        thumbnail = if (pictures.isEmpty()) null else pictures[0],
        pictures = pictures,
        status = if (published) "PUBLISHED" else "DRAFT",
        type = type.name,
        event = event,
        files = files,
        outOfStock = quantity <= 0,
        url = "/p/$id",
    )

    fun createEvent(
        online: Boolean = true,
        meetingProvider: MeetingProviderSummary? = null,
    ) = Event(
        online = online,
        meetingPassword = "123456",
        meetingId = "1234567890",
        meetingJoinUrl = "https://us04.zoom.us/j/12345678",
        starts = OffsetDateTime.of(2020, 1, 1, 10, 30, 0, 0, ZoneOffset.UTC),
        ends = OffsetDateTime.of(2020, 1, 1, 15, 30, 0, 0, ZoneOffset.UTC),
        meetingProvider = meetingProvider,
    )

    fun createMeetingProviderSummary() = MeetingProviderSummary(
        id = 1000,
        type = MeetingProviderType.ZOOM.name,
        name = "Zoom",
        logoUrl = "https://prod-wutsi.s3.amazonaws.com/static/marketplace-access-server/meeting-providers/zoom.png",
    )

    fun createPictureSummary(
        id: Long = -1,
        url: String = "http://www.google.com/1.png",
    ) = PictureSummary(
        id = id,
        url = url,
    )

    fun createPictureSummaryList(size: Int): List<PictureSummary> {
        val pictures = mutableListOf<PictureSummary>()
        for (i in 0..size) {
            pictures.add(
                PictureSummary(
                    id = i.toLong(),
                    url = "https://img.com/$i.png",
                ),
            )
        }
        return pictures
    }

    fun createStore(id: Long, accountId: Long) = Store(
        id = id,
        accountId = accountId,
    )

    fun createBusiness(id: Long, accountId: Long, country: String, currency: String) = Business(
        id = id,
        accountId = accountId,
        country = country,
        currency = currency,
        totalSales = 30000,
        totalOrders = 100,
    )

    fun createPaymentProviderSummary(id: Long, code: String) = PaymentProviderSummary(
        id = id,
        code = code,
        name = code,
        logoUrl = "https://www.imgs.com/$id.png",
    )

    fun createOrder(
        id: String,
        businessId: Long = -1,
        accountId: Long = -1,
        totalPrice: Long = 100000L,
        status: OrderStatus = OrderStatus.UNKNOWN,
        items: List<OrderItem>? = null,
    ) = Order(
        id = id,
        business = createBusinessSummary(id = businessId, accountId = accountId, currency = "XAF", country = "CM"),
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
        items = items ?: listOf(
            OrderItem(
                productId = 999,
                quantity = 3,
                title = "This is a product",
                pictureUrl = "https://img.com/1.png",
                totalPrice = totalPrice,
                unitPrice = totalPrice / 3,
                totalDiscount = 100,
            ),
        ),
        created = OffsetDateTime.of(2020, 1, 1, 10, 30, 0, 0, ZoneOffset.UTC),
        updated = OffsetDateTime.of(2020, 1, 1, 10, 30, 0, 0, ZoneOffset.UTC),
        expires = OffsetDateTime.of(2100, 1, 1, 10, 30, 0, 0, ZoneOffset.UTC),
    )

    fun createTransaction(
        id: String = UUID.randomUUID().toString(),
        type: TransactionType = TransactionType.CHARGE,
        status: Status = Status.PENDING,
        orderId: String? = null,
        businessId: Long = 1,
        accountId: Long = 1,
        errorCode: ErrorCode? = null,
    ) = Transaction(
        id = id,
        type = type.name,
        orderId = orderId,
        status = status.name,
        description = "This is description",
        currency = "XAF",
        business = createBusinessSummary(id = businessId, accountId = accountId, currency = "XAF", country = "CM"),
        email = "ray.sponsble@gmail.com",
        created = OffsetDateTime.of(2020, 1, 1, 10, 30, 0, 0, ZoneOffset.UTC),
        updated = OffsetDateTime.of(2020, 1, 1, 10, 30, 0, 0, ZoneOffset.UTC),
        amount = 10500,
        errorCode = errorCode?.name,
        customerAccountId = 1111L,
        paymentMethod = Fixtures.createPaymentMethodSummary(""),
        financialTransactionId = "1111-111",
        gatewayTransactionId = "2222-222",
        supplierErrorCode = "xyz",
        net = 10000,
        fees = 500,
        gatewayFees = 250,
        gatewayType = GatewayType.FLUTTERWAVE.name,
    )

    fun createPaymentMethodSummary(
        token: String,
    ) = PaymentMethodSummary(
        token = token,
        provider = createPaymentProvider(),
        number = "+237670000010",
        type = PaymentMethodType.MOBILE_MONEY.name,
        status = PaymentMethodStatus.ACTIVE.name,
        accountId = 111L,
        ownerName = "Ray Sponsible",
    )

    fun createPaymentProvider(
        id: Long = System.currentTimeMillis(),
        type: PaymentMethodType = PaymentMethodType.MOBILE_MONEY,
        code: String = "MTN",
    ) = PaymentProviderSummary(
        id = id,
        code = code,
        type = type.name,
    )

    fun createBusinessSummary(
        id: Long,
        accountId: Long,
        balance: Long = 100000,
        currency: String = "XAF",
        country: String = "CM",
        status: BusinessStatus = BusinessStatus.ACTIVE,
    ) = BusinessSummary(
        id = id,
        balance = balance,
        currency = currency,
        country = country,
        status = status.name,
        accountId = accountId,
    )

    fun createOfferSummary(
        product: ProductSummary,
        price: OfferPrice = createOfferPrice(productId = -1, price = 2000),
    ) = OfferSummary(
        product = product,
        price = price,
    )

    fun createOffer(
        product: Product,
        price: OfferPrice = createOfferPrice(),
    ) = Offer(
        product = product,
        price = price,
    )

    fun createOfferPrice(
        productId: Long = -1,
        discountId: Long? = null,
        savings: Long = 0,
        price: Long = 1500,
        referencePrice: Long? = null,
        expires: OffsetDateTime? = null,
    ) = OfferPrice(
        productId = productId,
        discountId = discountId,
        savings = savings,
        price = price,
        referencePrice = referencePrice,
        expires = expires,
        savingsPercentage = if (referencePrice != null && referencePrice > 0) (100 * savings / referencePrice).toInt() else 0,
    )

    fun createDiscountSummary(id: Long) = DiscountSummary(
        id = id,
        name = "FIN25",
        rate = 25,
        type = DiscountType.SALES.name,
    )
}
