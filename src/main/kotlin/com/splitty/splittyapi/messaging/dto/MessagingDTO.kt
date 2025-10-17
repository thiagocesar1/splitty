package com.splitty.splittyapi.messaging.dto

import com.splitty.splittyapi.expenses.entity.ExpenseCategory
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

data class CreatePaymentMessage(
    val expenseCode: UUID,
    val expenseValue: BigDecimal,
    val expenseDescription: String?,
    val expenseCategory: ExpenseCategory,
    val expenseDueDate: LocalDate,
    val groupId: Long,
    val groupCode: UUID,
    val memberId: Long,
    val memberUserCode: UUID,
    val chargedValue: BigDecimal,
    val splitStrategy: SplitStrategy = SplitStrategy.EQUAL
)

enum class SplitStrategy {
    EQUAL,
    PROPORTIONAL,
    CUSTOM
}

data class PaymentNotificationMessage(
    val paymentCode: UUID,
    val expenseCode: UUID,
    val memberUserId: Long,
    val chargedValue: BigDecimal,
    val expenseDescription: String?,
    val dueDate: LocalDate,
    val notificationType: NotificationType = NotificationType.PAYMENT_CREATED
)

enum class NotificationType {
    PAYMENT_CREATED,
    PAYMENT_REMINDER,
    PAYMENT_OVERDUE,
    PAYMENT_CONFIRMED
}
