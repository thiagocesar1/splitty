package com.splitty.splittyapi.groups.service

import com.splitty.splittyapi.groups.entity.Group
import com.splitty.splittyapi.groups.entity.GroupMember
import com.splitty.splittyapi.groups.entity.GroupRole
import com.splitty.splittyapi.groups.repository.GroupRepository
import com.splitty.splittyapi.groups.repository.GroupMemberRepository
import com.splitty.splittyapi.users.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class GroupService(
    private val groupRepository: GroupRepository,
    private val groupMemberRepository: GroupMemberRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun createGroupByCreatorCode(name: String, creatorCode: UUID, description: String? = null): Group {
        val creator = userRepository.findByCodeAndActive(creatorCode, true)
            .orElseThrow { NoSuchElementException("Creator user not found") }

        val group = Group(
            name = name,
            description = description
        )
        val savedGroup = groupRepository.save(group)

        GroupMember(
            groupId = savedGroup.id!!,
            userId = creator.id!!,
            role = GroupRole.OWNER,
            joinedAt = LocalDateTime.now()
        ).let { groupMemberRepository.save(it) }

        return savedGroup
    }

    fun findGroupByCode(code: UUID): Group {
        return groupRepository.findByCodeAndActive(code, true)
            .orElseThrow { NoSuchElementException("Group not found") }
    }

    fun findGroupsByCreatorCode(creatorCode: UUID): List<Group> {
        val user = userRepository.findByCodeAndActive(creatorCode, true)
            .orElseThrow { NoSuchElementException("User not found") }

        // Busca grupos onde o usuário é OWNER e que estão ativos
        val ownerMemberships = groupMemberRepository.findByUserIdAndRoleAndActive(user.id!!, GroupRole.OWNER, true)
        return ownerMemberships.mapNotNull { membership ->
            groupRepository.findById(membership.groupId)
                .takeIf { it.isPresent && it.get().active }
                ?.get()
        }
    }

    fun updateGroup(code: UUID, name: String, description: String? = null): Group {
        val group = findGroupByCode(code)
        val updatedGroup = group.copy(
            name = name,
            description = description
        )
        return groupRepository.save(updatedGroup)
    }

    @Transactional
    fun disableGroup(code: UUID) {
        val group = findGroupByCode(code)
        groupMemberRepository.deactivateAllGroupMembers(group.id!!)
        groupRepository.disableByCode(code)
    }

    @Transactional
    fun enableGroup(code: UUID) {
        // Busca o grupo sem filtrar por ativo para poder reabilitá-lo
        val group = groupRepository.findByCode(code)
            .orElseThrow { NoSuchElementException("Group not found") }

        if (group.active) {
            throw IllegalStateException("Group is already active")
        }

        groupRepository.enableByCode(code)
        groupMemberRepository.reactivateAllGroupMembers(group.id!!)
    }

    fun addMembersByCode(groupCode: UUID, userCodes: List<UUID>, role: GroupRole = GroupRole.MEMBER): List<GroupMember> {
        val group = findGroupByCode(groupCode)
        val members = mutableListOf<GroupMember>()

        userCodes.forEach { userCode ->
            val user = userRepository.findByCodeAndActive(userCode, true)
                .orElseThrow { NoSuchElementException("User with code $userCode not found") }

            if (!groupMemberRepository.existsByGroupIdAndUserIdAndActive(group.id!!, user.id!!, true)) {
                val groupMember = GroupMember(
                    groupId = group.id!!,
                    userId = user.id!!,
                    role = role,
                    joinedAt = LocalDateTime.now()
                )
                members.add(groupMemberRepository.save(groupMember))
            }
        }

        return members
    }

    fun removeMemberByCode(groupCode: UUID, userCode: UUID) {
        val group = findGroupByCode(groupCode)
        val user = userRepository.findByCodeAndActive(userCode, true)
            .orElseThrow { NoSuchElementException("User not found") }

        if (!groupMemberRepository.existsByGroupIdAndUserIdAndActive(group.id!!, user.id!!, true)) {
            throw NoSuchElementException("User is not a member of this group")
        }
        groupMemberRepository.deactivateGroupMember(group.id!!, user.id!!)
    }

    fun getGroupMembersByCode(groupCode: UUID): List<GroupMember> {
        val group = findGroupByCode(groupCode)
        return groupMemberRepository.findByGroupIdAndActive(group.id!!, true)
    }

    fun getUserGroupsByCode(userCode: UUID): List<GroupMember> {
        val user = userRepository.findByCodeAndActive(userCode, true)
            .orElseThrow { NoSuchElementException("User not found") }
        return groupMemberRepository.findByUserIdAndActive(user.id!!, true)
    }

    fun getGroupOwnersByCode(groupCode: UUID): List<GroupMember> {
        val group = findGroupByCode(groupCode)
        return groupMemberRepository.findByGroupIdAndRole(group.id!!, GroupRole.OWNER)
    }

    fun changeMemberRoleByCode(groupCode: UUID, userCode: UUID, newRole: GroupRole): GroupMember {
        val group = findGroupByCode(groupCode)
        val user = userRepository.findByCodeAndActive(userCode, true)
            .orElseThrow { NoSuchElementException("User not found") }

        val member = groupMemberRepository.findByGroupIdAndUserIdAndActive(group.id!!, user.id!!, true)
            ?: throw NoSuchElementException("User is not a member of this group")

        val updatedMember = member.copy(role = newRole)
        return groupMemberRepository.save(updatedMember)
    }

    fun isUserMemberOfGroupByCode(groupCode: UUID, userCode: UUID): Boolean {
        val group = findGroupByCode(groupCode)
        val user = userRepository.findByCodeAndActive(userCode, true)
            .orElseThrow { NoSuchElementException("User not found") }

        return groupMemberRepository.existsByGroupIdAndUserIdAndActive(group.id!!, user.id!!, true)
    }

    fun getCreatorByGroup(group: Group): com.splitty.splittyapi.users.entity.User {
        val ownerMembership = groupMemberRepository.findByGroupIdAndRole(group.id!!, GroupRole.OWNER)
            .firstOrNull() ?: throw NoSuchElementException("Group has no owner")

        return userRepository.findById(ownerMembership.userId)
            .orElseThrow { NoSuchElementException("Creator not found") }
    }

    fun getUserById(userId: Long) = userRepository.findById(userId)
        .orElseThrow { NoSuchElementException("User not found") }

    fun getGroupById(groupId: Long) = groupRepository.findById(groupId)
        .orElseThrow { NoSuchElementException("Group not found") }
}
