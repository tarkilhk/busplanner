package com.tarkil.busplanner.domain.userSessions

import com.tarkil.busplanner.domain.userProfilePersistence.User
import com.tarkil.busplanner.infra.UserLoginEventHandling.NewUserSessionEventBus
import com.tarkil.busplanner.infra.bus.CityBusHelper
import com.tarkil.busplanner.infra.userProfilePersistence.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId


@Service
class UserSessionManager(
    val newUserSessionEventBus: NewUserSessionEventBus,
    val userRepository: UserRepository,
    val cityBusHelper: CityBusHelper
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val userSessions = mutableListOf<UserSession>()

    fun addNewUserSession(existingUser: User): UserSession {
        val newUserSession = UserSession(existingUser, cityBusHelper)
        this.userSessions.add(newUserSession)
        newUserSessionEventBus.post(newUserSession)
        return newUserSession
    }

    fun getUserSessionForUserName(userName: String): UserSession? {
        if (this.userSessions.size == 0) {
            return null
        } else {
            return this.userSessions.filter { it.user.name == userName }.firstOrNull()
        }
    }

    @Scheduled(fixedDelay = 10_000)
    fun RefreshArrivalTimesForAllSessions() {
        Thread.sleep((5_000..20_000).shuffled().last().toLong())
        for (userSession in userSessions) {
            userSession.arrivalTimes.refreshDataLoop()
            logger.info("Refreshed ArrivalTimes for ${userSession.user.name} - ${userSession.busStopGroupName}")
            for (arrivalTime in userSession.arrivalTimes.getSortedArrivalTimes()) {
                logger.info(arrivalTime.toString())
            }
        }
    }

    @Scheduled(fixedDelay = 600_000)
    fun PruneSessionInactiveForMoreThan30Minutes() {
        val userSessionsToDelete = mutableListOf<UserSession>()
        for (userSession in userSessions) {
            if (userSession.lastQueryTime.plusMinutes(30)
                    .isBefore(LocalDateTime.now(ZoneId.of("Asia/Hong_Kong"))) && !userSession.user.name.equals("pi")
            ) {
                userSessionsToDelete.add(userSession)
            }
        }

        for (userSessionToDelete in userSessionsToDelete) {
            this.removeUserSession(userSessionToDelete)
            logger.info("Pruned userSession ${userSessionToDelete.user.name} - ${userSessionToDelete.busStopGroupName} - Age : ${userSessionToDelete.lastQueryTime}")
            logger.debug("${this.userSessions.size} sessions remaining :")
            for (mySession in this.userSessions) {
                logger.debug("${mySession.user} - ${mySession.busStopGroupName} last activity at ${mySession.lastQueryTime}")
            }
        }
    }

    private fun removeUserSession(userSessionToDelete: UserSession) {
        this.userSessions.remove(userSessionToDelete)
    }

    fun getUserSessionById(sessionId: String): UserSession? {
        if (this.userSessions.size == 0) {
            return null
        } else {
            return this.userSessions.find { it.uniqueSessionId == sessionId }
        }
    }

    fun sessionExistsFor(name: String): Boolean {
        return (userSessions.find { it.user.name == name } != null)
    }

    fun sessionIdExists(sessionId: String): Boolean {
        return (this.userSessions.find { it.uniqueSessionId == sessionId } != null)
    }

    fun getAll(): MutableList<UserSession> {
        return this.userSessions
    }

    fun changeConfig(userSession: UserSession, configName: String) {
        userSession.changeConfig(configName)
        //TODO : confirm that with line below uncommented, bus times start getting refreshed via EventBus mechanism
        newUserSessionEventBus.post(userSession)
    }
}

