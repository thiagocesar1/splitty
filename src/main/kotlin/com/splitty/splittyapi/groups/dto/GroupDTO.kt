package com.splitty.splittyapi.groups.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.splitty.splittyapi.groups.entity.Currency
import com.splitty.splittyapi.groups.entity.GroupRole

data class CreateGroupRequest(
    val name: String,
    val description: String? = null,
    val currency: Currency = Currency.BRL
)

data class UpdateGroupRequest(
    val name: String,
    val description: String? = null,
    val currency: Currency? = null
)

data class AddMembersRequest(
    val userCodes: List<String>,
    val role: GroupRole = GroupRole.MEMBER
)

data class ChangeMemberRoleRequest(
    val userCode: String,
    val role: GroupRole
)

data class GroupResponse(
    val code: String,
    val name: String,
    val creatorCode: String,
    val description: String?,
    val active: Boolean,
    val currency: Currency
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class GroupMemberResponse(
    val groupCode: String?,
    val userCode: String,
    val role: GroupRole,
    val joinedAt: String,
    val active: Boolean
)
