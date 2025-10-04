package com.splitty.splittyapi.users.repository

import com.splitty.splittyapi.users.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {

    fun findByCodeAndActive(code: UUID, active: Boolean): Optional<User>

    fun findByEmailAndActive(email: String, active: Boolean): Optional<User>

    fun existsByCodeAndActive(code: UUID, active: Boolean): Boolean

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.active = false WHERE u.code = :code")
    fun disable(code: UUID)
}