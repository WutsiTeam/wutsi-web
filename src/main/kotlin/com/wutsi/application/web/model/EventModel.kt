package com.wutsi.application.web.model

data class EventModel(
    val online: Boolean,
    val startDateTime: String?,
    val meetingProviderLogoUrl: String?,
    val meetingProviderName: String?,
    val startDate: String?,
    val startTime: String?,
    val endTime: String?,
)
