package com.wutsi.application.web.service

import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.stereotype.Service

@Service
class EnvironmentDetector(
    private val env: Environment,
) {
    fun test(): Boolean =
        !prod()

    fun prod(): Boolean =
        env.acceptsProfiles(Profiles.of("prod"))
}
