package com.tarkil.busplanner.api

import com.tarkil.busplanner.domain.userProfilePersistence.DesiredBusStop
import com.tarkil.busplanner.domain.userSessions.UserSessionManager
import com.tarkil.busplanner.infra.userProfilePersistence.DesiredBusStopRepository
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/desiredbusstops")
class desiredBusStopController(
    val sessionManager: UserSessionManager,
    val desiredBusStopRepository: DesiredBusStopRepository
) {

    @PostMapping("")
    fun newDesiredBusStop(
        @RequestParam(value = "shortName") shortName: String,
        @RequestParam(value = "busNumber") busNumber: String,
        @RequestParam(value = "info") info: String,
        @RequestParam(value = "userNameList") userNameList: List<String>
    ): String {
        val newBusStop = desiredBusStopRepository.save(
            DesiredBusStop(
                shortName = shortName,
                busNumber = busNumber,
                info_hkbus = info
            )
        )

        for (userName in userNameList) {
            if (sessionManager.userRepository.existsByName(userName)) {
                sessionManager.userRepository.findByName(userName).first().attachDesiredBusStop(newBusStop)
                sessionManager.userRepository.save(sessionManager.userRepository.findByName(userName).first())
            }
        }
        return ("Done : id ${newBusStop.desiredBusStopId}")
    }

//    @GetMapping("")
//    fun getAll():MutableSet<String>
//    {
//        val setOfUserNames = mutableSetOf<String>()
//        for(user in sessionManager.userRepository.findAll()) {
//            setOfUserNames.add(user.name)
//        }
//        return setOfUserNames
//    }
}