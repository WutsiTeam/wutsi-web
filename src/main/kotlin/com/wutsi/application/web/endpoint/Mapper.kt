package com.wutsi.application.web.endpoint

import com.wutsi.application.web.model.MemberModel
import com.wutsi.application.web.model.PictureModel
import com.wutsi.application.web.model.ProductModel
import com.wutsi.application.web.util.HandleGenerator
import com.wutsi.marketplace.manager.dto.PictureSummary
import com.wutsi.marketplace.manager.dto.Product
import com.wutsi.marketplace.manager.dto.ProductSummary
import com.wutsi.membership.manager.dto.Member
import com.wutsi.platform.core.image.Dimension
import com.wutsi.platform.core.image.Focus
import com.wutsi.platform.core.image.ImageService
import com.wutsi.platform.core.image.Transformation
import com.wutsi.regulation.Country
import org.springframework.stereotype.Service
import java.text.DecimalFormat

@Service
class Mapper(
    private val imageService: ImageService
) {
    companion object {
        const val PROFILE_PICTURE_WIDTH = 128
        const val PROFILE_PICTURE_HEIGHT = 128
        const val PRODUCT_THUMBNAIL_HEIGHT = 300
        const val PRODUCT_THUMBNAIL_WIDTH = 300
        const val PRODUCT_PICTURE_HEIGHT = 512
        const val PRODUCT_PICTURE_WIDTH = 512
    }

    fun toMemberModel(member: Member) = MemberModel(
        id = member.id,
        displayName = member.displayName,
        biography = member.biography,
        category = member.category?.title,
        location = member.city?.longName,
        phoneNumber = member.phoneNumber,
        whatsapp = member.whatsapp,
        facebookId = member.facebookId,
        instagramId = member.instagramId,
        twitterId = member.twitterId,
        youtubeId = member.youtubeId,
        website = member.website,
        url = "/u/${member.id}",
        pictureUrl = member.pictureUrl?.let {
            imageService.transform(
                url = it,
                transformation = Transformation(
                    focus = Focus.FACE,
                    dimension = Dimension(
                        width = PROFILE_PICTURE_WIDTH,
                        height = PROFILE_PICTURE_HEIGHT
                    )
                )
            )
        }
    )

    fun toProductModel(product: ProductSummary, country: Country) = ProductModel(
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
                        width = PRODUCT_THUMBNAIL_WIDTH
                    )
                )
            )
        }
    )

    fun toProductModel(product: Product, country: Country) = ProductModel(
        id = product.id,
        title = product.title,
        price = DecimalFormat(country.monetaryFormat).format(product.price),
        summary = product.summary,
        description = product.description,
        available = product.quantity != null && product.quantity!! > 0,
        quantity = product.quantity,
        url = toProductUrl(product.id, product.title),
        thumbnailUrl = product.thumbnail?.url?.let {
            imageService.transform(
                url = it,
                transformation = Transformation(
                    dimension = Dimension(
                        height = PRODUCT_PICTURE_WIDTH,
                        width = PRODUCT_PICTURE_HEIGHT
                    )
                )
            )
        },
        pictures = product.pictures.map { toPictureMapper(it) }
    )

    private fun toPictureMapper(picture: PictureSummary) = PictureModel(
        url = imageService.transform(
            url = picture.url,
            transformation = Transformation(
                dimension = Dimension(
                    height = PRODUCT_PICTURE_WIDTH,
                    width = PRODUCT_PICTURE_HEIGHT
                )
            )
        )
    )

    private fun toProductUrl(id: Long, title: String): String =
        "/p/$id/" + HandleGenerator.generate(title)
}
