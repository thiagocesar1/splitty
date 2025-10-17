package com.splitty.splittyapi.expenses.repository

import com.splitty.splittyapi.expenses.entity.Expense
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface ExpenseRepository : JpaRepository<Expense, Long> {
    fun findByCode(code: UUID): Optional<Expense>
    fun findByCodeAndActive(code: UUID, active: Boolean): Optional<Expense>
    fun findByGroupId(groupId: Long): List<Expense>
    fun findByGroupIdAndActive(groupId: Long, active: Boolean): List<Expense>

    @Modifying
    @Query("UPDATE Expense e SET e.active = false WHERE e.code = :code")
    fun disableByCode(code: UUID)
}
