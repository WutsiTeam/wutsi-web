package com.wutsi.application.web.endpoint

import com.wutsi.application.web.model.MemberModel
import com.wutsi.application.web.model.PageModel
import com.wutsi.application.web.model.ProductModel
import com.wutsi.marketplace.manager.MarketplaceManagerApi
import com.wutsi.marketplace.manager.dto.SearchProductRequest
import com.wutsi.membership.manager.MembershipManagerApi
import com.wutsi.membership.manager.dto.Member
import com.wutsi.platform.core.error.Error
import com.wutsi.platform.core.error.exception.NotFoundException
import com.wutsi.platform.core.image.Dimension
import com.wutsi.platform.core.image.Focus
import com.wutsi.platform.core.image.ImageService
import com.wutsi.platform.core.image.Transformation
import com.wutsi.regulation.Country
import com.wutsi.regulation.RegulationEngine
import com.wutsi.workflow.error.ErrorURN
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import java.text.DecimalFormat

@Controller
@RequestMapping("/u")
class UserController(
    private val membershipManagerApi: MembershipManagerApi,
    private val marketplaceManagerApi: MarketplaceManagerApi,
    private val regulationEngine: RegulationEngine,
    private val imageService: ImageService
) : AbstractController() {
    companion object {
        const val PRODUCT_THUMBNAIL_HEIGHT = 300
        const val PRODUCT_THUMBNAIL_WIDTH = 300
    }

    @GetMapping("/{id}")
    fun index(@PathVariable id: Long, model: Model): String {
        val member = membershipManagerApi.getMember(id).member
        if (!member.business) {
            throw NotFoundException(
                error = Error(
                    code = ErrorURN.MEMBER_NOT_BUSINESS.urn
                )
            )
        }

        val country = regulationEngine.country(member.country)

        model.addAttribute("page", createPage(member))
        model.addAttribute("member", toMemberModel(member))
        model.addAttribute("products", findProducts(member, country))

        return "user"
    }

    private fun createPage(member: Member) = PageModel(
        name = "page.Profile",
        title = member.displayName,
        description = member.biography,
        imageUrl = member.pictureUrl,
        assetUrl = assetUrl
    )

    private fun toMemberModel(member: Member) = MemberModel(
        id = member.id,
        displayName = member.displayName,
        biography = member.biography,
        pictureUrl = member.pictureUrl?.let {
            imageService.transform(
                url = it,
                transformation = Transformation(
                    focus = Focus.FACE,
                    dimension = Dimension(width = 64, height = 64)
                )
            )
        },
        category = member.category?.title,
        location = member.city?.longName
    )

    private fun findProducts(member: Member, country: Country): List<ProductModel> {
        if (member.storeId == null) {
            return emptyList()
        }

        val fmt = DecimalFormat(country.monetaryFormat)
        return marketplaceManagerApi.searchProduct(
            request = SearchProductRequest(
                storeId = member.storeId,
                limit = regulationEngine.maxProducts(),
                sortBy = "RECOMMENDED"
            )
        ).products.map {
            ProductModel(
                id = it.id,
                title = it.title,
                price = fmt.format(it.price),
                thumbnailUrl = it.thumbnailUrl?.let {
                    imageService.transform(
                        url = it,
                        transformation = Transformation(
                            dimension = Dimension(height = PRODUCT_THUMBNAIL_HEIGHT, width = PRODUCT_THUMBNAIL_WIDTH),
                            focus = Focus.FACE
                        )
                    )
                }
            )
        }
    }
}
