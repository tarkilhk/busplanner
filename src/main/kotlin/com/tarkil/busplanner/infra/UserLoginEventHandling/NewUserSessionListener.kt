package com.tarkil.busplanner.infra.UserLoginEventHandling

import com.google.common.eventbus.AllowConcurrentEvents
import com.google.common.eventbus.Subscribe
import com.tarkil.busplanner.domain.userSessions.UserSession
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


@Service
class NewUserSessionListener() {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Subscribe
    @AllowConcurrentEvents
    fun handle(userSession: UserSession) {
        logger.info("Getting an event : ${userSession.user.name} - ${userSession.busStopGroupName}")
        userSession.arrivalTimes.refreshDataLoop()
    }
}