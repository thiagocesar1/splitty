package com.splitty.splittyapi.users.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val name: String,

    val phone: String,

    @Column(nullable = false, unique = true)
    val code: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true)
    val email: String,

    val password: String,

    @Column(nullable = false)
    var active: Boolean = true
)
