package com.splitty.splittyapi.users.dto

data class CreateUserRequest(
    val name: String,
    val phone: String,
    val email: String,
    val password: String
)

data class UserResponse(
    val id: Long?,
    val name: String,
    val phone: String,
    val email: String,
    val code: String,
    val active: Boolean
)
