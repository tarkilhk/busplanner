package com.tarkil.busplanner.infra.userProfilePersistence

import com.tarkil.busplanner.domain.userProfilePersistence.DesiredBusStop
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DataAdminService(val userRepository: UserRepository, val desiredBusStopRepository: DesiredBusStopRepository) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun createNewBusStop(desiredBusStop: DesiredBusStop): DesiredBusStop {
        return desiredBusStopRepository.save(desiredBusStop)
    }

    fun attachBusStopToUsers() {
        userRepository
    }
}