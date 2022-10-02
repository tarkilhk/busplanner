package com.tarkil.busplanner.api

import com.tarkil.busplanner.domain.userSessions.UserSessionManager
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/sessions")
class sessionController(val sessionManager: UserSessionManager) {

    @GetMapping
    fun getAllLiveSessions(): MutableList<HashMap<String, String>> {
        val listOfSessions = mutableListOf<HashMap<String, String>>()

        for (session in sessionManager.getAll()) {
            var tempMap = hashMapOf<String, String>()
            tempMap["user"] = session.user.toString()
            tempMap["sessionId"] = session.uniqueSessionId
            tempMap["lastQueryTime"] = session.lastQueryTime.toString()
            tempMap["busStopGroupName"] = session.busStopGroupName
            tempMap["isLoaded"] = session.arrivalTimes.isLoaded.toString()
            listOfSessions.add(tempMap)
        }
        return listOfSessions
    }

    @GetMapping("/configNames")
    fun getConfigNames(@RequestParam(value = "sessionId") sessionId: String): MutableSet<String> {
        if (sessionManager.sessionIdExists(sessionId)) {
            val setOfConfigNames = mutableSetOf<String>()
            for (desiredBusStop in sessionManager.getUserSessionById(sessionId)!!.user.getAllConfigBusStops()) {
                setOfConfigNames.add(desiredBusStop.shortName)
            }
            return setOfConfigNames
        } else {
            return mutableSetOf("Session $sessionId does not exist, please restart app")
        }
    }

    @PostMapping("/changeConfigName")
    fun changeConfigName(
        @RequestParam(value = "sessionId") sessionId: String,
        @RequestParam(value = "configName") configName: String
    ): ResponseEntity<String> {
        if (sessionManager.sessionIdExists(sessionId)) {
            //TODO : protect from unknown configName
            sessionManager.changeConfig(sessionManager.getUserSessionById(sessionId)!!, configName)
            return ResponseEntity("configName changed to $configName", HttpStatus.OK)
        } else {
            return ResponseEntity("sessionId $sessionId doesn't exist", HttpStatus.UNAUTHORIZED)
        }
    }
}