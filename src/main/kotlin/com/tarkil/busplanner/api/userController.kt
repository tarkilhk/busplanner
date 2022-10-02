package com.tarkil.busplanner.api

import com.tarkil.busplanner.domain.userProfilePersistence.User
import com.tarkil.busplanner.domain.userSessions.UserSessionManager
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/users")
class userController(val sessionManager: UserSessionManager) {

    @PostMapping("/login")
    fun login(@RequestParam(value = "userName") name: String): ResponseEntity<String> {
        if (sessionManager.sessionExistsFor(name)) {
            val mySession = this.sessionManager.getUserSessionForUserName(name)!!
            mySession.setLastQueryTimeToNow()
            return ResponseEntity(mySession.uniqueSessionId, HttpStatus.OK)
        } else {
            val user: User? = this.sessionManager.userRepository.findByName(name).firstOrNull()
            if (user == null) {
                return ResponseEntity("User not found", HttpStatus.INTERNAL_SERVER_ERROR)
                //return user not found + http code
            } else {
                //return sessionId + 200
                return ResponseEntity(this.sessionManager.addNewUserSession(user).uniqueSessionId, HttpStatus.OK)
            }
        }
    }

    @PostMapping("")
    fun newUser(@RequestParam(value = "name") name: String): String {
        sessionManager.userRepository.save(User(name))
        return ("Done")
    }

    @GetMapping("")
    fun getAll(): MutableSet<String> {
        val setOfUserNames = mutableSetOf<String>()
        for (user in sessionManager.userRepository.findAll()) {
            setOfUserNames.add(user.name)
        }
        return setOfUserNames
    }
}