package com.tarkil.busplanner.infra.userProfilePersistence

import com.tarkil.busplanner.domain.userProfilePersistence.DesiredBusStop
import org.springframework.data.repository.CrudRepository

interface DesiredBusStopRepository : CrudRepository<DesiredBusStop, Long> {

//    fun findByUsers(user: User): MutableList<DesiredBusStop>
}