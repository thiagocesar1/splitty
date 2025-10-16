package com.splitty.splittyapi.groups.mapper

import com.splitty.splittyapi.groups.dto.GroupResponse
import com.splitty.splittyapi.groups.dto.GroupMemberResponse
import com.splitty.splittyapi.groups.entity.Group
import com.splitty.splittyapi.groups.entity.GroupMember
import com.splitty.splittyapi.users.entity.User
import java.time.format.DateTimeFormatter
import java.util.UUID

object GroupMapper {
    fun Group.toResponse(creator: User) = GroupResponse(
        code = code.toString(),
        name = name,
        creatorCode = creator.code.toString(),
        description = description,
        active = active,
        currency = currency
    )

    fun GroupMember.toResponse(group: Group, user: User) = GroupMemberResponse(
        groupCode = group.code.toString(),
        userCode = user.code.toString(),
        role = role,
        joinedAt = joinedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        active = active
    )

    fun GroupMember.toResponse(user: User) = GroupMemberResponse(
        null,
        userCode = user.code.toString(),
        role = role,
        joinedAt = joinedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        active = active
    )
}
