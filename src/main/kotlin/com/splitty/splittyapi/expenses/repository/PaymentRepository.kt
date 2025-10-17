package com.splitty.splittyapi.expenses.repository

import com.splitty.splittyapi.expenses.entity.Payment
import com.splitty.splittyapi.expenses.entity.PaymentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PaymentRepository : JpaRepository<Payment, Long> {
    fun findByExpenseId(expenseId: Long): List<Payment>
    fun findByGroupMemberId(groupMemberId: Long): List<Payment>
    fun findByStatus(status: PaymentStatus): List<Payment>

    @Modifying
    @Query("UPDATE Payment p SET p.status = :status, p.updateDate = CURRENT_TIMESTAMP WHERE p.id = :id")
    fun updateStatus(id: Long, status: PaymentStatus)
}

