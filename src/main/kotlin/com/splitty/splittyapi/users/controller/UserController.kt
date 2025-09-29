package com.splitty.splittyapi.users.controller

import com.splitty.splittyapi.users.dto.CreateUserRequest
import com.splitty.splittyapi.users.dto.UserResponse
import com.splitty.splittyapi.users.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    @PostMapping
    fun createUser(@RequestBody request: CreateUserRequest): ResponseEntity<UserResponse> {
        val user = userService.createUser(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(user)
    }

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): ResponseEntity<UserResponse> {
        val user = userService.findById(id)
        return ResponseEntity.ok(user)
    }

    @GetMapping("/email/{email}")
    fun getUserByEmail(@PathVariable email: String): ResponseEntity<UserResponse> {
        val user = userService.findByEmail(email)
        return ResponseEntity.ok(user)
    }

    @GetMapping("/code/{code}")
    fun getUserByCode(@PathVariable code: String): ResponseEntity<UserResponse> {
        val user = userService.findByCode(UUID.fromString(code))
        return ResponseEntity.ok(user)
    }

    @PatchMapping("/{id}/disable")
    fun disableUser(@PathVariable id: Long): ResponseEntity<Unit> {
        userService.disableUser(id)
        return ResponseEntity.noContent().build()
    }
}
