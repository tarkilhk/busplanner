package com.tarkil.busplanner.domain.userSessions

import com.tarkil.busplanner.domain.bus.ArrivalTimes
import com.tarkil.busplanner.domain.userProfilePersistence.User
import com.tarkil.busplanner.infra.bus.CityBusHelper
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.time.ZoneId
import java.util.*

class UserSession(val user: User, cityBusHelper: CityBusHelper) {

    val arrivalTimes = ArrivalTimes(cityBusHelper)
    var busStopGroupName = ""
    val uniqueSessionId = if (this.user.name == "pi") "1" else UUID.randomUUID().toString()
    var lastQueryTime: LocalDateTime = now(ZoneId.of("Asia/Hong_Kong"))

    fun changeConfig(newDesiredBusStopGroupName: String) {
        this.arrivalTimes.setToNotLoaded()
        this.arrivalTimes.clearAll()
        this.busStopGroupName = newDesiredBusStopGroupName
        this.arrivalTimes.clearDesiredBusStops()
        this.arrivalTimes.addSeveralDesiredBusStop(user.getAllChosenBusStopsForGroup(newDesiredBusStopGroupName))
    }

    fun setLastQueryTimeToNow() {
        this.lastQueryTime = now(ZoneId.of("Asia/Hong_Kong"))
    }
}