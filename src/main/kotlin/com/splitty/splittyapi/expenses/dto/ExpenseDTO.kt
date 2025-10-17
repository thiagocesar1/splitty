package com.splitty.splittyapi.expenses.dto

import com.splitty.splittyapi.expenses.entity.ExpenseCategory
import java.math.BigDecimal
import java.time.LocalDate

data class CreateExpenseRequest(
    val value: BigDecimal,
    val description: String? = null,
    val category: ExpenseCategory,
    val dueDate: LocalDate,
    val groupCode: String
)

data class UpdateExpenseRequest(
    val value: BigDecimal? = null,
    val description: String? = null,
    val category: ExpenseCategory? = null,
    val active: Boolean? = null
)

data class ExpenseResponse(
    val code: String,
    val value: BigDecimal,
    val description: String?,
    val creationDate: String,
    val category: ExpenseCategory,
    val dueDate: String,
    val groupCode: String,
    val active: Boolean
)
