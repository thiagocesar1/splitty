package com.splitty.splittyapi.payments.service

import com.splitty.splittyapi.expenses.entity.Expense
import com.splitty.splittyapi.expenses.repository.ExpenseRepository
import com.splitty.splittyapi.expenses.entity.PaymentStatus
import com.splitty.splittyapi.expenses.entity.Payment
import com.splitty.splittyapi.expenses.repository.PaymentRepository
import com.splitty.splittyapi.groups.repository.GroupMemberRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val expenseRepository: ExpenseRepository,
    private val groupMemberRepository: GroupMemberRepository
) {
    private val logger = LoggerFactory.getLogger(PaymentService::class.java)

    @Transactional
    fun createPaymentsForExpense(
        expenseCode: UUID,
        groupId: Long,
        memberIds: List<Long>
    ): List<Payment> {
        logger.info("Creating payments for expense: $expenseCode")

        val expense = expenseRepository.findByCode(expenseCode)
            .orElseThrow { NoSuchElementException("Expense not found: $expenseCode") }

        // Verifica se já existem pagamentos para essa despesa
        val existingPayments = paymentRepository.findByExpenseId(expense.id!!)
        if (existingPayments.isNotEmpty()) {
            logger.warn("Payments already exist for expense: $expenseCode. Skipping creation.")
            return existingPayments
        }

        // Divide o valor igualmente entre os membros
        val splitValue = expense.value.divide(
            BigDecimal(memberIds.size),
            2,
            RoundingMode.HALF_UP
        )

        val payments = mutableListOf<Payment>()

        memberIds.forEachIndexed { index, memberId ->
            // Verifica se o membro existe e está ativo no grupo
            val groupMember = groupMemberRepository.findByGroupIdAndUserIdAndActive(groupId, memberId, true)
                ?: run {
                    logger.warn("Group member not found or inactive: groupId=$groupId, userId=$memberId")
                    return@forEachIndexed
                }

            // Ajusta o último pagamento para compensar arredondamentos
            val chargedValue = if (index == memberIds.size - 1) {
                val sumOfPrevious = splitValue.multiply(BigDecimal(index))
                expense.value.subtract(sumOfPrevious)
            } else {
                splitValue
            }

            val payment = Payment(
                expenseId = expense.id!!,
                groupMemberId = groupMember.id!!,
                chargedValue = chargedValue,
                status = PaymentStatus.CHARGE_SENT
            )

            payments.add(paymentRepository.save(payment))
            logger.info("Created payment ${payment.code} for member $memberId with value $chargedValue")
        }

        logger.info("Successfully created ${payments.size} payments for expense: $expenseCode")
        return payments
    }

    fun findPaymentByCode(code: UUID): Payment {
        return paymentRepository.findById(code.hashCode().toLong())
            .orElseThrow { NoSuchElementException("Payment not found") }
    }

    fun findPaymentsByExpense(expenseId: Long): List<Payment> {
        return paymentRepository.findByExpenseId(expenseId)
    }

    fun findPaymentsByGroupMember(groupMemberId: Long): List<Payment> {
        return paymentRepository.findByGroupMemberId(groupMemberId)
    }

    @Transactional
    fun updatePaymentStatus(paymentId: Long, status: PaymentStatus) {
        paymentRepository.updateStatus(paymentId, status)
        logger.info("Updated payment $paymentId status to $status")
    }

    @Transactional
    fun createPaymentForMember(
        expenseCode: UUID,
        groupId: Long,
        memberId: Long,
        chargedValue: BigDecimal
    ): Payment {
        logger.info("Creating payment for expense: $expenseCode, member: $memberId")

        val expense = expenseRepository.findByCode(expenseCode)
            .orElseThrow { NoSuchElementException("Expense not found: $expenseCode") }

        // Verifica se já existe pagamento para essa despesa e esse membro
        val groupMember = groupMemberRepository.findByGroupIdAndUserIdAndActive(groupId, memberId, true)
            ?: throw NoSuchElementException("Group member not found or inactive: groupId=$groupId, userId=$memberId")

        val existingPayment = paymentRepository.findByExpenseId(expense.id!!)
            .firstOrNull { it.groupMemberId == groupMember.id }

        if (existingPayment != null) {
            logger.warn("Payment already exists for expense: $expenseCode and member: $memberId. Returning existing payment.")
            return existingPayment
        }

        val payment = Payment(
            expenseId = expense.id!!,
            groupMemberId = groupMember.id!!,
            chargedValue = chargedValue,
            status = PaymentStatus.CHARGE_SENT
        )

        val savedPayment = paymentRepository.save(payment)
        logger.info("Created payment ${savedPayment.code} for member $memberId with value $chargedValue")

        return savedPayment
    }
}
