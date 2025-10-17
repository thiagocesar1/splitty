package com.splitty.splittyapi.expenses.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "payments")
data class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val code: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val expenseId: Long,

    @Column(nullable = false)
    val groupMemberId: Long,

    @Column(nullable = false, precision = 10, scale = 2)
    val chargedValue: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val status: PaymentStatus,

    @Column(nullable = false)
    val creationDate: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val updateDate: LocalDateTime = LocalDateTime.now()
)
