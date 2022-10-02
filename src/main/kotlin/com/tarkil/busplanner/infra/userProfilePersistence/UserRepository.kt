package com.tarkil.busplanner.infra.userProfilePersistence

import com.tarkil.busplanner.domain.userProfilePersistence.User
import org.springframework.data.repository.CrudRepository


interface UserRepository : CrudRepository<User, Long> {

    fun findByName(name: String): List<User>

    fun existsByName(name: String): Boolean
}