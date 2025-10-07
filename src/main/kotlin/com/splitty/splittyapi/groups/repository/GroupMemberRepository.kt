package com.splitty.splittyapi.groups.repository

import com.splitty.splittyapi.groups.entity.GroupMember
import com.splitty.splittyapi.groups.entity.GroupRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface GroupMemberRepository : JpaRepository<GroupMember, Long> {

    fun findByGroupIdAndActive(groupId: Long, active: Boolean = true): List<GroupMember>

    fun findByUserIdAndActive(userId: Long, active: Boolean = true): List<GroupMember>

    fun findByUserIdAndRoleAndActive(userId: Long, role: GroupRole, active: Boolean = true): List<GroupMember>

    fun findByGroupIdAndUserIdAndActive(groupId: Long, userId: Long, active: Boolean = true): GroupMember?

    fun findByGroupIdAndRole(groupId: Long, role: GroupRole): List<GroupMember>

    fun existsByGroupIdAndUserIdAndActive(groupId: Long, userId: Long, active: Boolean = true): Boolean

    @Modifying
    @Transactional
    @Query("UPDATE GroupMember gm SET gm.active = false WHERE gm.groupId = :groupId AND gm.userId = :userId")
    fun deactivateGroupMember(@Param("groupId") groupId: Long, @Param("userId") userId: Long)

    @Modifying
    @Transactional
    @Query("UPDATE GroupMember gm SET gm.active = false WHERE gm.groupId = :groupId")
    fun deactivateAllGroupMembers(@Param("groupId") groupId: Long)

    @Modifying
    @Transactional
    @Query("UPDATE GroupMember gm SET gm.active = true WHERE gm.groupId = :groupId")
    fun reactivateAllGroupMembers(@Param("groupId") groupId: Long)
}
