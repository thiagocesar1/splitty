package com.splitty.splittyapi.users.service

import com.splitty.splittyapi.users.dto.CreateUserRequest
import com.splitty.splittyapi.users.dto.LoginRequest
import com.splitty.splittyapi.users.dto.UpdatePasswordRequest
import com.splitty.splittyapi.users.dto.UpdateUserRequest
import com.splitty.splittyapi.users.dto.UserResponse
import com.splitty.splittyapi.users.mapper.UserMapper.toEntity
import com.splitty.splittyapi.users.mapper.UserMapper.toResponse
import com.splitty.splittyapi.users.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    fun createUser(request: CreateUserRequest): UserResponse {
        val user = request.toEntity().copy(
            password = passwordEncoder.encode(request.password)
        )
        return userRepository.save(user).toResponse()
    }

    fun findById(id: Long): UserResponse {
        return userRepository.findById(id)
            .orElseThrow { NoSuchElementException("User not found") }
            .toResponse()
    }

    fun findByEmail(email: String): UserResponse {
        return userRepository.findByEmailAndActive(email, true)
            .orElseThrow { NoSuchElementException("User not found") }
            .toResponse()
    }

    fun findByCode(code: UUID): UserResponse {
        return userRepository.findByCodeAndActive(code, true)
            .orElseThrow { NoSuchElementException("User not found") }
            .toResponse()
    }

    fun disableUserByCode(code: UUID) {
        if (!userRepository.existsByCodeAndActive(code, true)) {
            throw NoSuchElementException("User not found")
        }
        userRepository.disable(code)
    }

    fun login(request: LoginRequest): UserResponse {
        val user = userRepository.findByEmailAndActive(request.email, true)
            .orElseThrow { NoSuchElementException("Invalid credentials") }

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw NoSuchElementException("Invalid credentials")
        }

        return user.toResponse()
    }

    fun updateUser(code: UUID, request: UpdateUserRequest): UserResponse {
        val user = userRepository.findByCodeAndActive(code, true)
            .orElseThrow { NoSuchElementException("User not found") }

        val updatedUser = user.copy(
            name = request.name,
            phone = request.phone
        )

        return userRepository.save(updatedUser).toResponse()
    }

    fun updatePassword(code: UUID, request: UpdatePasswordRequest) {
        val user = userRepository.findByCodeAndActive(code, true)
            .orElseThrow { NoSuchElementException("User not found") }

        if (!passwordEncoder.matches(request.currentPassword, user.password)) {
            throw IllegalArgumentException("Current password is incorrect")
        }

        val updatedUser = user.copy(
            password = passwordEncoder.encode(request.newPassword)
        )

        userRepository.save(updatedUser)
    }
}
