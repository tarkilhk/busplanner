package com.tarkil.busplanner.api

import com.tarkil.busplanner.domain.userProfilePersistence.DesiredBusStop
import com.tarkil.busplanner.infra.userProfilePersistence.DesiredBusStopRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*


@RestController
@RequestMapping("/bus-stops")
class BusStopsController(val desiredBusStopRepository: DesiredBusStopRepository) {

    @GetMapping("names")
    fun getAllBusStops(): MutableList<String> {
        val allBusStopsNames: MutableList<String> = mutableListOf()
        for (busStop: DesiredBusStop in desiredBusStopRepository.findAll()) {
            allBusStopsNames.add(busStop.shortName)
        }
        return allBusStopsNames
    }
}