package com.splitty.splittyapi.users.dto

data class UpdatePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)
