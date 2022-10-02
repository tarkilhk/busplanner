package com.tarkil.busplanner.infra.UserLoginEventHandling

import com.google.common.eventbus.AsyncEventBus
import com.tarkil.busplanner.domain.userSessions.UserSession
import org.springframework.stereotype.Component
import java.util.concurrent.Executors


@Component
class NewUserSessionEventBus(newUserSessionListener: NewUserSessionListener) {
    final val eventBus = AsyncEventBus(Executors.newFixedThreadPool(30))

    init {
        eventBus.register(newUserSessionListener)
    }

    fun post(newUserSession: UserSession) {
        eventBus.post(newUserSession)
    }
}