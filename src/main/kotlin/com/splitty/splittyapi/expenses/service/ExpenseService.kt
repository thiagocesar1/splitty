package com.splitty.splittyapi.expenses.service

import com.splitty.splittyapi.expenses.entity.Expense
import com.splitty.splittyapi.expenses.entity.ExpenseCategory
import com.splitty.splittyapi.expenses.repository.ExpenseRepository
import com.splitty.splittyapi.groups.repository.GroupRepository
import com.splitty.splittyapi.groups.repository.GroupMemberRepository
import com.splitty.splittyapi.users.repository.UserRepository
import com.splitty.splittyapi.messaging.producer.PaymentMessageProducer
import com.splitty.splittyapi.messaging.dto.CreatePaymentMessage
import com.splitty.splittyapi.messaging.dto.SplitStrategy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@Service
class ExpenseService(
    private val expenseRepository: ExpenseRepository,
    private val groupRepository: GroupRepository,
    private val groupMemberRepository: GroupMemberRepository,
    private val userRepository: UserRepository,
    private val paymentMessageProducer: PaymentMessageProducer
) {

    @Transactional
    fun createExpense(
        value: BigDecimal,
        description: String?,
        category: ExpenseCategory,
        dueDate: LocalDate,
        groupCode: UUID
    ): Expense {
        val group = groupRepository.findByCodeAndActive(groupCode, true)
            .orElseThrow { NoSuchElementException("Group not found") }

        val savedExpense = saveExpense(value, description, category, dueDate, group.id!!)

        publishPaymentCreationMessages(savedExpense, group)

        return savedExpense
    }

    private fun saveExpense(
        value: BigDecimal,
        description: String?,
        category: ExpenseCategory,
        dueDate: LocalDate,
        groupId: Long
    ): Expense {
        val expense = Expense(
            value = value,
            description = description,
            category = category,
            dueDate = dueDate,
            groupId = groupId
        )
        return expenseRepository.save(expense)
    }

    private fun publishPaymentCreationMessages(
        expense: Expense,
        group: com.splitty.splittyapi.groups.entity.Group
    ) {
        val activeMembers = groupMemberRepository.findByGroupIdAndActive(group.id!!, true)

        if (activeMembers.isEmpty()) {
            throw IllegalStateException("Cannot create expense for group without active members")
        }

        val splitValue = calculateSplitValue(expense.value, activeMembers.size)

        activeMembers.forEachIndexed { index, member ->
            val user = userRepository.findById(member.userId)
                .orElseThrow { NoSuchElementException("User not found: ${member.userId}") }

            val chargedValue = calculateChargedValue(
                splitValue = splitValue,
                totalValue = expense.value,
                index = index,
                totalMembers = activeMembers.size
            )

            val message = buildPaymentMessage(expense, group, member.userId, user.code, chargedValue)

            paymentMessageProducer.sendCreatePaymentMessage(message)
        }
    }

    private fun calculateSplitValue(totalValue: BigDecimal, memberCount: Int): BigDecimal {
        return totalValue.divide(
            BigDecimal(memberCount),
            2,
            java.math.RoundingMode.HALF_UP
        )
    }

    private fun calculateChargedValue(
        splitValue: BigDecimal,
        totalValue: BigDecimal,
        index: Int,
        totalMembers: Int
    ): BigDecimal {
        return if (index == totalMembers - 1) {
            val sumOfPrevious = splitValue.multiply(BigDecimal(index))
            totalValue.subtract(sumOfPrevious)
        } else {
            splitValue
        }
    }

    private fun buildPaymentMessage(
        expense: Expense,
        group: com.splitty.splittyapi.groups.entity.Group,
        memberId: Long,
        memberUserCode: UUID,
        chargedValue: BigDecimal
    ): CreatePaymentMessage {
        return CreatePaymentMessage(
            expenseCode = expense.code,
            expenseValue = expense.value,
            expenseDescription = expense.description,
            expenseCategory = expense.category,
            expenseDueDate = expense.dueDate,
            groupId = group.id!!,
            groupCode = group.code,
            memberId = memberId,
            memberUserCode = memberUserCode,
            chargedValue = chargedValue,
            splitStrategy = SplitStrategy.EQUAL
        )
    }

    fun findExpenseByCode(code: UUID): Expense {
        return expenseRepository.findByCode(code)
            .orElseThrow { NoSuchElementException("Expense not found") }
    }

    fun findExpensesByGroup(groupCode: UUID): List<Expense> {
        val group = groupRepository.findByCodeAndActive(groupCode, true)
            .orElseThrow { NoSuchElementException("Group not found") }
        return expenseRepository.findByGroupIdAndActive(group.id!!, true)
    }

    fun findExpensesByMemberAndGroup(memberCode: UUID, groupCode: UUID): List<Expense> {
        val group = groupRepository.findByCodeAndActive(groupCode, true)
            .orElseThrow { NoSuchElementException("Group not found") }

        val user = userRepository.findByCodeAndActive(memberCode, true)
            .orElseThrow { NoSuchElementException("User not found") }

        val isMember = groupMemberRepository.existsByGroupIdAndUserIdAndActive(group.id!!, user.id!!, true)
        if (!isMember) {
            throw NoSuchElementException("User is not a member of this group")
        }

        return expenseRepository.findByGroupIdAndActive(group.id!!, true)
    }

    @Transactional
    fun disableExpense(code: UUID) {
        findExpenseByCode(code)
        expenseRepository.disableByCode(code)
    }

    @Transactional
    fun updateExpense(
        code: UUID,
        value: BigDecimal?,
        description: String?,
        category: ExpenseCategory?,
        active: Boolean?
    ): Expense {
        val expense = findExpenseByCode(code)

        val updatedExpense = expense.copy(
            value = value ?: expense.value,
            description = description ?: expense.description,
            category = category ?: expense.category,
            active = active ?: expense.active
        )

        return expenseRepository.save(updatedExpense)
    }

    fun getGroupById(groupId: Long) = groupRepository.findById(groupId)
        .orElseThrow { NoSuchElementException("Group not found") }
}
