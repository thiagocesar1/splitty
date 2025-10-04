package com.splitty.splittyapi.users.controller

import com.splitty.splittyapi.users.dto.CreateUserRequest
import com.splitty.splittyapi.users.dto.LoginRequest
import com.splitty.splittyapi.users.dto.UpdatePasswordRequest
import com.splitty.splittyapi.users.dto.UpdateUserRequest
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

    @PatchMapping("/code/{code}/disable")
    fun disableUser(@PathVariable code: String): ResponseEntity<Unit> {
        userService.disableUserByCode(UUID.fromString(code))
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<UserResponse> {
        val user = userService.login(request)
        return ResponseEntity.ok(user)
    }

    @PutMapping("/code/{code}")
    fun updateUser(
        @PathVariable code: String,
        @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UserResponse> {
        val user = userService.updateUser(UUID.fromString(code), request)
        return ResponseEntity.ok(user)
    }

    @PutMapping("/code/{code}/password")
    fun updatePassword(
        @PathVariable code: String,
        @RequestBody request: UpdatePasswordRequest
    ): ResponseEntity<Unit> {
        userService.updatePassword(UUID.fromString(code), request)
        return ResponseEntity.noContent().build()
    }
}
