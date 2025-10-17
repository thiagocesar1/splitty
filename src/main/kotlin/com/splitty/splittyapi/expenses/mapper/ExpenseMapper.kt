package com.splitty.splittyapi.expenses.mapper

import com.splitty.splittyapi.expenses.dto.ExpenseResponse
import com.splitty.splittyapi.expenses.entity.Expense
import com.splitty.splittyapi.groups.entity.Group
import java.time.format.DateTimeFormatter

object ExpenseMapper {
    fun Expense.toResponse(group: Group) = ExpenseResponse(
        code = code.toString(),
        value = value,
        description = description,
        creationDate = creationDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        category = category,
        dueDate = dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
        groupCode = group.code.toString(),
        active = active
    )
}

