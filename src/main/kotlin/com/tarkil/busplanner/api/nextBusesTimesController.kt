package com.tarkil.busplanner.api

import com.tarkil.busplanner.domain.userSessions.UserSessionManager
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/busTimes")
class nextBusesTimesController(
    val sessionManager: UserSessionManager
) {

    @GetMapping("/nextFor")
    fun getNextBusesTimesFor(
        @RequestParam(value = "sessionId") sessionId: String
    ): ResponseEntity<HashMap<String, Any>> {

//        var mySession: UserSession? = sessionManager.getUserSessionById(sessionId)
        if (sessionManager.sessionIdExists(sessionId)) {
            return ResponseEntity(
                sessionManager.getUserSessionById(sessionId)!!.arrivalTimes.getResult(),
                HttpStatus.OK
            )
        } else {
            return ResponseEntity(HashMap(), HttpStatus.UNAUTHORIZED)
        }
    }
}