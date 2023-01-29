package com.wutsi.application.web.endpoint

import com.wutsi.application.web.Page
import com.wutsi.application.web.model.MemberModel
import com.wutsi.application.web.model.PageModel
import com.wutsi.enums.ProductSort
import com.wutsi.marketplace.manager.dto.OfferSummary
import com.wutsi.marketplace.manager.dto.SearchOfferRequest
import com.wutsi.membership.manager.dto.Member
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/u")
class UserController : AbstractController() {
    @GetMapping("/{id}")
    fun index(@PathVariable id: Long, model: Model): String {
        val merchant = resolveCurrentMerchant(id)
        val country = regulationEngine.country(merchant.country)
        val memberModel = mapper.toMemberModel(merchant)
        val offers = findOffers(merchant)

        model.addAttribute("page", createPage(memberModel))
        model.addAttribute("member", memberModel)
        model.addAttribute(
            "offers",
            offers.map {
                mapper.toOfferModel(it, country, merchant)
            },
        )

        return "user"
    }

    private fun createPage(member: MemberModel) = PageModel(
        name = Page.PROFILE,
        title = member.displayName,
        description = member.biography,
        url = "$serverUrl${member.url}",
        imageUrl = member.pictureUrl,
        canonicalUrl = "$serverUrl/u/${member.id}",
        sitemapUrl = "$serverUrl/sitemap.xml?id=${member.id}",
        twitterUserId = member.twitterId,
    )

    private fun findOffers(member: Member): List<OfferSummary> {
        if (member.storeId == null) {
            return emptyList()
        }

        return marketplaceManagerApi.searchOffer(
            request = SearchOfferRequest(
                storeId = member.storeId,
                limit = regulationEngine.maxProducts(),
                sortBy = ProductSort.RECOMMENDED.name,
            ),
        ).offers
    }
}
