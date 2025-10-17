package com.splitty.splittyapi.expenses.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "expenses")
data class Expense(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val code: UUID = UUID.randomUUID(),

    @Column(nullable = false, precision = 10, scale = 2)
    val value: BigDecimal,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(nullable = false)
    val creationDate: LocalDateTime = LocalDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val category: ExpenseCategory,

    @Column(nullable = false)
    val dueDate: LocalDate,

    @Column(nullable = false)
    val groupId: Long,

    @Column(nullable = false)
    val active: Boolean = true
)
