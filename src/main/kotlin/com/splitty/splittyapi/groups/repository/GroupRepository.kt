package com.splitty.splittyapi.groups.repository

import com.splitty.splittyapi.groups.entity.Group
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface GroupRepository : JpaRepository<Group, Long> {

    fun findByCode(code: UUID): Optional<Group>

    fun findByCodeAndActive(code: UUID, active: Boolean): Optional<Group>

    fun existsByCode(code: UUID): Boolean

    @Modifying
    @Transactional
    @Query("UPDATE Group g SET g.active = false WHERE g.code = :code")
    fun disableByCode(@Param("code") code: UUID)

    @Modifying
    @Transactional
    @Query("UPDATE Group g SET g.active = true WHERE g.code = :code")
    fun enableByCode(@Param("code") code: UUID)
}
