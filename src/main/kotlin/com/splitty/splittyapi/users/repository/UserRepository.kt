package com.splitty.splittyapi.users.repository

import com.splitty.splittyapi.users.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.Optional
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<User, Long> {

    fun findByEmail(email: String): Optional<User>

    fun findByCode(code: UUID): Optional<User>

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.active = false WHERE u.id = :id")
    fun disable(@Param("id") id: Long)
}