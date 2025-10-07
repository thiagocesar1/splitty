package com.splitty.splittyapi.users.service

import com.splitty.splittyapi.users.dto.CreateUserRequest
import com.splitty.splittyapi.users.dto.LoginRequest
import com.splitty.splittyapi.users.dto.UpdatePasswordRequest
import com.splitty.splittyapi.users.dto.UpdateUserRequest
import com.splitty.splittyapi.users.entity.User
import com.splitty.splittyapi.users.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*

class UserServiceTest {
    private lateinit var userService: UserService
    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder

    @BeforeEach
    fun setup() {
        userRepository = mock()
        passwordEncoder = mock()
        userService = UserService(userRepository, passwordEncoder)
    }

    @Test
    fun `when creating user should encode password and save`() {
        // given
        val request = CreateUserRequest(
            name = "Test User",
            email = "test@test.com",
            password = "password123",
            phone = "1234567890"
        )
        val encodedPassword = "encodedPassword"
        val savedUser = User(
            id = 1L,
            name = request.name,
            email = request.email,
            password = encodedPassword,
            phone = request.phone,
            code = UUID.randomUUID(),
            active = true
        )

        whenever(passwordEncoder.encode(request.password)).thenReturn(encodedPassword)
        whenever(userRepository.save(any<User>())).thenReturn(savedUser)

        // when
        val result = userService.createUser(request)

        // then
        verify(passwordEncoder).encode(request.password)
        verify(userRepository).save(any<User>())
        assertEquals(savedUser.name, result.name)
        assertEquals(savedUser.email, result.email)
        assertEquals(savedUser.id, result.id)
    }

    @Test
    fun `when finding by code and user exists should return user`() {
        // given
        val code = UUID.randomUUID()
        val user = User(
            id = 1L,
            name = "Test User",
            email = "test@test.com",
            password = "password",
            phone = "1234567890",
            code = code,
            active = true
        )

        whenever(userRepository.findByCodeAndActive(code, true)).thenReturn(Optional.of(user))

        // when
        val result = userService.findByCode(code)

        // then
        verify(userRepository).findByCodeAndActive(code, true)
        assertEquals(user.name, result.name)
        assertEquals(user.email, result.email)
        assertEquals(user.id, result.id)
    }

    @Test
    fun `when finding by code and user does not exist should throw exception`() {
        // given
        val code = UUID.randomUUID()
        whenever(userRepository.findByCodeAndActive(code, true)).thenReturn(Optional.empty())

        // when/then
        assertThrows<NoSuchElementException> { userService.findByCode(code) }
        verify(userRepository).findByCodeAndActive(code, true)
    }

    @Test
    fun `when login with valid credentials should return user`() {
        // given
        val request = LoginRequest("test@test.com", "password123")
        val user = User(
            id = 1L,
            name = "Test User",
            email = request.email,
            password = "encodedPassword",
            phone = "1234567890",
            code = UUID.randomUUID(),
            active = true
        )

        whenever(userRepository.findByEmailAndActive(request.email, true)).thenReturn(Optional.of(user))
        whenever(passwordEncoder.matches(request.password, user.password)).thenReturn(true)

        // when
        val result = userService.login(request)

        // then
        verify(userRepository).findByEmailAndActive(request.email, true)
        verify(passwordEncoder).matches(request.password, user.password)
        assertEquals(user.email, result.email)
        assertEquals(user.id, result.id)
    }

    @Test
    fun `when login with invalid credentials should throw exception`() {
        // given
        val request = LoginRequest("test@test.com", "wrongpassword")
        val user = User(
            id = 1L,
            name = "Test User",
            email = request.email,
            password = "encodedPassword",
            phone = "1234567890",
            code = UUID.randomUUID(),
            active = true
        )

        whenever(userRepository.findByEmailAndActive(request.email, true)).thenReturn(Optional.of(user))
        whenever(passwordEncoder.matches(request.password, user.password)).thenReturn(false)

        // when/then
        assertThrows<NoSuchElementException> { userService.login(request) }
    }

    @Test
    fun `when updating user should save with new data`() {
        // given
        val code = UUID.randomUUID()
        val request = UpdateUserRequest("New Name", "9876543210")
        val existingUser = User(
            id = 1L,
            name = "Old Name",
            email = "test@test.com",
            password = "password",
            phone = "1234567890",
            code = code,
            active = true
        )
        val updatedUser = existingUser.copy(
            name = request.name,
            phone = request.phone
        )

        whenever(userRepository.findByCodeAndActive(code, true)).thenReturn(Optional.of(existingUser))
        whenever(userRepository.save(any<User>())).thenReturn(updatedUser)

        // when
        val result = userService.updateUser(code, request)

        // then
        verify(userRepository).findByCodeAndActive(code, true)
        verify(userRepository).save(any<User>())
        assertEquals(request.name, result.name)
        assertEquals(request.phone, result.phone)
        assertEquals(existingUser.id, result.id)
    }

    @Test
    fun `when updating password with valid current password should save new password`() {
        // given
        val code = UUID.randomUUID()
        val request = UpdatePasswordRequest("currentPassword", "newPassword")
        val user = User(
            id = 1L,
            name = "Test User",
            email = "test@test.com",
            password = "encodedPassword",
            phone = "1234567890",
            code = code,
            active = true
        )
        val updatedUser = user.copy(password = "newEncodedPassword")

        whenever(userRepository.findByCodeAndActive(code, true)).thenReturn(Optional.of(user))
        whenever(passwordEncoder.matches(request.currentPassword, user.password)).thenReturn(true)
        whenever(passwordEncoder.encode(request.newPassword)).thenReturn("newEncodedPassword")
        whenever(userRepository.save(any<User>())).thenReturn(updatedUser)

        // when
        userService.updatePassword(code, request)

        // then
        verify(userRepository).findByCodeAndActive(code, true)
        verify(passwordEncoder).matches(request.currentPassword, user.password)
        verify(passwordEncoder).encode(request.newPassword)
        verify(userRepository).save(any<User>())
    }

    @Test
    fun `when updating password with invalid current password should throw exception`() {
        // given
        val code = UUID.randomUUID()
        val request = UpdatePasswordRequest("wrongPassword", "newPassword")
        val user = User(
            id = 1L,
            name = "Test User",
            email = "test@test.com",
            password = "encodedPassword",
            phone = "1234567890",
            code = code,
            active = true
        )

        whenever(userRepository.findByCodeAndActive(code, true)).thenReturn(Optional.of(user))
        whenever(passwordEncoder.matches(request.currentPassword, user.password)).thenReturn(false)

        // when/then
        assertThrows<IllegalArgumentException> { userService.updatePassword(code, request) }
    }

    @Test
    fun `when disabling user should update active status`() {
        // given
        val code = UUID.randomUUID()
        whenever(userRepository.existsByCodeAndActive(code, true)).thenReturn(true)
        doNothing().whenever(userRepository).disable(code)

        // when
        userService.disableUserByCode(code)

        // then
        verify(userRepository).existsByCodeAndActive(code, true)
        verify(userRepository).disable(code)
    }

    @Test
    fun `when disabling non-existent user should throw exception`() {
        // given
        val code = UUID.randomUUID()
        whenever(userRepository.existsByCodeAndActive(code, true)).thenReturn(false)

        // when/then
        assertThrows<NoSuchElementException> { userService.disableUserByCode(code) }
        verify(userRepository).existsByCodeAndActive(code, true)
        verify(userRepository, never()).disable(any())
    }
}
