package com.splitty.splittyapi.groups.controller

import com.splitty.splittyapi.groups.dto.*
import com.splitty.splittyapi.groups.mapper.GroupMapper.toResponse
import com.splitty.splittyapi.groups.service.GroupService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/groups")
class GroupController(
    private val groupService: GroupService
) {

    @PostMapping
    fun createGroup(@RequestBody request: CreateGroupRequest, @RequestParam creatorCode: String): ResponseEntity<GroupResponse> {
        val group = groupService.createGroupByCreatorCode(request.name, UUID.fromString(creatorCode), request.description, request.currency)
        val creator = groupService.getCreatorByGroup(group)
        return ResponseEntity.status(HttpStatus.CREATED).body(group.toResponse(creator))
    }

    @GetMapping("/{code}")
    fun getGroupByCode(@PathVariable code: UUID): ResponseEntity<GroupResponse> {
        val group = groupService.findGroupByCode(code)
        val creator = groupService.getCreatorByGroup(group)
        return ResponseEntity.ok(group.toResponse(creator))
    }

    @GetMapping("/creator/{creatorCode}")
    fun getGroupsByCreator(@PathVariable creatorCode: UUID): ResponseEntity<List<GroupResponse>> {
        val groups = groupService.findGroupsByCreatorCode(creatorCode)
        return ResponseEntity.ok(groups.map { group ->
            val creator = groupService.getCreatorByGroup(group)
            group.toResponse(creator)
        })
    }

    @PutMapping("/{code}")
    fun updateGroup(@PathVariable code: UUID, @RequestBody request: UpdateGroupRequest): ResponseEntity<GroupResponse> {
        val group = groupService.updateGroup(code, request.name, request.description, request.currency)
        val creator = groupService.getCreatorByGroup(group)
        return ResponseEntity.ok(group.toResponse(creator))
    }

    @DeleteMapping("/{code}")
    fun disableGroup(@PathVariable code: UUID): ResponseEntity<Void> {
        groupService.disableGroup(code)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{code}/enable")
    fun enableGroup(@PathVariable code: UUID): ResponseEntity<Void> {
        groupService.enableGroup(code)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{groupCode}/members")
    fun addMembersToGroup(@PathVariable groupCode: UUID, @RequestBody request: AddMembersRequest): ResponseEntity<List<GroupMemberResponse>> {
        val userCodes = request.userCodes.map { UUID.fromString(it) }
        val members = groupService.addMembersByCode(groupCode, userCodes, request.role)

        return ResponseEntity.status(HttpStatus.CREATED).body(members.map { member ->
            val group = groupService.getGroupById(member.groupId)
            val user = groupService.getUserById(member.userId)
            member.toResponse(group, user)
        })
    }

    @DeleteMapping("/{groupCode}/members/{userCode}")
    fun removeMemberFromGroup(@PathVariable groupCode: UUID, @PathVariable userCode: UUID): ResponseEntity<Void> {
        groupService.removeMemberByCode(groupCode, userCode)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{groupCode}/members")
    fun getGroupMembers(@PathVariable groupCode: UUID): ResponseEntity<List<GroupMemberResponse>> {
        val members = groupService.getGroupMembersByCode(groupCode)
        return ResponseEntity.ok(members.map { member ->
            val user = groupService.getUserById(member.userId)
            member.toResponse(user)
        })
    }

    @GetMapping("/users/{userCode}")
    fun getUserGroups(@PathVariable userCode: UUID): ResponseEntity<List<GroupMemberResponse>> {
        val userGroups = groupService.getUserGroupsByCode(userCode)
        return ResponseEntity.ok(userGroups.map { member ->
            val group = groupService.getGroupById(member.groupId)
            val user = groupService.getUserById(member.userId)
            member.toResponse(group, user)
        })
    }

    @GetMapping("/{groupCode}/owners")
    fun getGroupOwners(@PathVariable groupCode: UUID): ResponseEntity<List<GroupMemberResponse>> {
        val owners = groupService.getGroupOwnersByCode(groupCode)
        return ResponseEntity.ok(owners.map { member ->
            val group = groupService.getGroupById(member.groupId)
            val user = groupService.getUserById(member.userId)
            member.toResponse(group, user)
        })
    }

    @PutMapping("/{groupCode}/members/role")
    fun changeMemberRole(@PathVariable groupCode: UUID, @RequestBody request: ChangeMemberRoleRequest): ResponseEntity<GroupMemberResponse> {
        val member = groupService.changeMemberRoleByCode(groupCode, UUID.fromString(request.userCode), request.role)
        val group = groupService.getGroupById(member.groupId)
        val user = groupService.getUserById(member.userId)
        return ResponseEntity.ok(member.toResponse(group, user))
    }

    @GetMapping("/{groupCode}/members/{userCode}/exists")
    fun checkUserMembership(@PathVariable groupCode: UUID, @PathVariable userCode: UUID): ResponseEntity<Map<String, Boolean>> {
        val isMember = groupService.isUserMemberOfGroupByCode(groupCode, userCode)
        return ResponseEntity.ok(mapOf("isMember" to isMember))
    }
}
