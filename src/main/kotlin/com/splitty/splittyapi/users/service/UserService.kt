package com.splitty.splittyapi.users.service

import com.splitty.splittyapi.users.dto.CreateUserRequest
import com.splitty.splittyapi.users.dto.UserResponse
import com.splitty.splittyapi.users.entity.User
import com.splitty.splittyapi.users.mapper.UserMapper.toEntity
import com.splitty.splittyapi.users.mapper.UserMapper.toResponse
import com.splitty.splittyapi.users.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun createUser(request: CreateUserRequest): UserResponse {
        val user = request.toEntity()
        return userRepository.save(user).toResponse()
    }

    fun findById(id: Long): UserResponse {
        return userRepository.findById(id)
            .orElseThrow { NoSuchElementException("User not found") }
            .toResponse()
    }

    fun findByEmail(email: String): UserResponse {
        return userRepository.findByEmail(email)
            .orElseThrow { NoSuchElementException("User not found") }
            .toResponse()
    }

    fun findByCode(code: UUID): UserResponse {
        return userRepository.findByCode(code)
            .orElseThrow { NoSuchElementException("User not found") }
            .toResponse()
    }

    fun disableUser(id: Long) {
        if (!userRepository.existsById(id)) {
            throw NoSuchElementException("User not found")
        }
        userRepository.disable(id)
    }
}
