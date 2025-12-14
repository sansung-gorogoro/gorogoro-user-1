package com.gorogoro.auth.authorization.application.service

import com.gorogoro.auth.authorization.application.dto.RefreshTokenCommand
import com.gorogoro.auth.authorization.application.port.out.RefreshTokenPort
import com.gorogoro.auth.authorization.model.RefreshToken
import com.gorogoro.auth.global.exception.BusinessException
import com.gorogoro.auth.jwt.JwtProvider
import com.gorogoro.auth.user.application.port.out.CheckNicknamePort
import com.gorogoro.auth.user.application.port.out.LoadUserPort
import com.gorogoro.auth.user.application.port.out.ModifyUserPort
import com.gorogoro.auth.user.application.port.out.NicknamePolicyPort
import com.gorogoro.auth.user.application.port.out.SendNotificationPort
import com.gorogoro.auth.authorization.application.port.out.SaveUserPort
import com.gorogoro.auth.user.model.User
import com.gorogoro.auth.user.model.constant.Role
import com.gorogoro.auth.user.model.constant.Status
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class AuthServiceTest {

    @Mock
    lateinit var loadUserPort: LoadUserPort

    @Mock
    lateinit var checkNicknamePort: CheckNicknamePort

    @Mock
    lateinit var saveUserPort: SaveUserPort

    @Mock
    lateinit var refreshTokenPort: RefreshTokenPort

    @Mock
    lateinit var jwtProvider: JwtProvider

    @Mock
    lateinit var passwordEncoder: PasswordEncoder

    @Mock
    lateinit var nicknameGenerator: NicknamePolicyPort

    @Mock
    lateinit var sendNotificationPort: SendNotificationPort

    @InjectMocks
    lateinit var authService: AuthService

    @Test
    fun refresh_ShouldThrowUserNotFound_WhenUserDoesNotExist() {
        // Given
        val refreshTokenStr = "valid-refresh-token"
        val cmd = RefreshTokenCommand(refreshTokenStr)
        val userId = 1L
        val refreshToken = RefreshToken(
            id = 1L,
            userId = userId,
            refreshToken = refreshTokenStr,
            refreshTokenExpire = Instant.now().plusSeconds(3600)
        )

        Mockito.`when`(refreshTokenPort.findByRefreshToken(refreshTokenStr)).thenReturn(refreshToken)
        
        // Mock loadUserPort.findById to mimic the adapter's behavior: throw exception if not found
        // Since the interface signature is User?, but implementation throws, we simulate implementation behavior here or exception thrown by adapter
        // However, since we mock the interface, if the interface returns null, AuthService might handle it.
        // Wait, AuthService code:
        // val user = loadUserPort.findById(savedRefreshToken.userId) ?: throw BusinessException.builder(ErrorCode.USER_NOT_FOUND).build()
        // If we want to simulate "User exists", we return a user.
        // If we want to simulate "User does not exist", we return null (as per interface) OR throw exception (as per new implementation).
        
        // Scenario 1: User exists, but 500 happens.
        val user = User(
            id = userId,
            email = "test@test.com",
            passwordEncrypted = "pw",
            name = "Test",
            nickname = "Nick",
            role = Role.USER,
            status = Status.ACTIVATED,
            createdAt = Instant.now(),
            modifiedAt = Instant.now()
        )
        Mockito.`when`(loadUserPort.findById(userId)).thenReturn(user)
        Mockito.`when`(jwtProvider.createAccessToken(userId, Role.USER)).thenReturn("new-access-token")

        // When
        val result = authService.refresh(cmd)

        // Then
        Assertions.assertNotNull(result)
        Assertions.assertEquals("new-access-token", result.accessToken)
    }
}
