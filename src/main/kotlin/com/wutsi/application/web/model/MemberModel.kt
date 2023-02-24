package com.wutsi.application.web.model

data class MemberModel(
    val id: Long = -1,
    val name: String? = null,
    val displayName: String = "",
    val category: String? = null,
    val pictureUrl: String? = null,
    val location: String? = null,
    val biography: String? = null,
    val phoneNumber: String? = null,
    val whatsapp: Boolean = false,
    val website: String? = null,
    val facebookId: String? = null,
    val twitterId: String? = null,
    val youtubeId: String? = null,
    val instagramId: String? = null,
    val url: String = "",
    val businessId: Long? = null,
    val business: BusinessModel? = null,
)
