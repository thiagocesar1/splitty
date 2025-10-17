package com.splitty.splittyapi.expenses.controller

import com.splitty.splittyapi.expenses.dto.CreateExpenseRequest
import com.splitty.splittyapi.expenses.dto.UpdateExpenseRequest
import com.splitty.splittyapi.expenses.dto.ExpenseResponse
import com.splitty.splittyapi.expenses.mapper.ExpenseMapper.toResponse
import com.splitty.splittyapi.expenses.service.ExpenseService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/expenses")
class ExpenseController(
    private val expenseService: ExpenseService
) {

    @PostMapping
    fun createExpense(@RequestBody request: CreateExpenseRequest): ResponseEntity<ExpenseResponse> {
        val expense = expenseService.createExpense(
            value = request.value,
            description = request.description,
            category = request.category,
            dueDate = request.dueDate,
            groupCode = UUID.fromString(request.groupCode)
        )
        val group = expenseService.getGroupById(expense.groupId)
        return ResponseEntity.status(HttpStatus.CREATED).body(expense.toResponse(group))
    }

    @GetMapping("/{code}")
    fun getExpenseByCode(@PathVariable code: UUID): ResponseEntity<ExpenseResponse> {
        val expense = expenseService.findExpenseByCode(code)
        val group = expenseService.getGroupById(expense.groupId)
        return ResponseEntity.ok(expense.toResponse(group))
    }

    @GetMapping("/group/{groupCode}")
    fun getExpensesByGroup(@PathVariable groupCode: UUID): ResponseEntity<List<ExpenseResponse>> {
        val expenses = expenseService.findExpensesByGroup(groupCode)
        return ResponseEntity.ok(expenses.map { expense ->
            val group = expenseService.getGroupById(expense.groupId)
            expense.toResponse(group)
        })
    }

    @GetMapping("/group/{groupCode}/member/{memberCode}")
    fun getExpensesByMemberAndGroup(
        @PathVariable groupCode: UUID,
        @PathVariable memberCode: UUID
    ): ResponseEntity<List<ExpenseResponse>> {
        val expenses = expenseService.findExpensesByMemberAndGroup(memberCode, groupCode)
        return ResponseEntity.ok(expenses.map { expense ->
            val group = expenseService.getGroupById(expense.groupId)
            expense.toResponse(group)
        })
    }

    @PutMapping("/{code}")
    fun updateExpense(
        @PathVariable code: UUID,
        @RequestBody request: UpdateExpenseRequest
    ): ResponseEntity<ExpenseResponse> {
        val expense = expenseService.updateExpense(
            code = code,
            value = request.value,
            description = request.description,
            category = request.category,
            active = request.active
        )
        val group = expenseService.getGroupById(expense.groupId)
        return ResponseEntity.ok(expense.toResponse(group))
    }

    @DeleteMapping("/{code}")
    fun disableExpense(@PathVariable code: UUID): ResponseEntity<Void> {
        expenseService.disableExpense(code)
        return ResponseEntity.noContent().build()
    }
}
