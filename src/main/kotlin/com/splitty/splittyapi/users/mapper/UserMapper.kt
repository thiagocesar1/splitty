package com.splitty.splittyapi.users.mapper

import com.splitty.splittyapi.users.dto.CreateUserRequest
import com.splitty.splittyapi.users.dto.UserResponse
import com.splitty.splittyapi.users.entity.User

object UserMapper {
    fun User.toResponse() = UserResponse(
        id = id,
        name = name,
        phone = phone,
        email = email,
        code = code.toString(),
        active = active
    )

    fun CreateUserRequest.toEntity() = User(
        name = name,
        phone = phone,
        email = email,
        password = password
    )
}
