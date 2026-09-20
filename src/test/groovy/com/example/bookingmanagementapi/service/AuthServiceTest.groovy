package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.dto.request.LoginRequest
import com.example.bookingmanagementapi.dto.request.RefreshRequest
import com.example.bookingmanagementapi.dto.response.AuthResponse
import com.example.bookingmanagementapi.entity.RefreshTokenEntity
import com.example.bookingmanagementapi.entity.UserEntity
import com.example.bookingmanagementapi.enums.Roles
import com.example.bookingmanagementapi.exception.NotFoundException
import com.example.bookingmanagementapi.repository.UserRepository
import com.example.bookingmanagementapi.security.CustomUserDetailsService
import com.example.bookingmanagementapi.security.JwtService
import com.example.bookingmanagementapi.security.RefreshTokenService
import com.example.bookingmanagementapi.service.impl.AuthServiceImpl
import lombok.RequiredArgsConstructor
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestBody
import spock.lang.Specification

@Component
@RequiredArgsConstructor
class AuthServiceTest extends Specification {

    def userRepository = Mock(UserRepository)
    def refreshTokenService = Mock(RefreshTokenService)
    def jwtService = Mock(JwtService)
    def authenticationManager = Mock(AuthenticationManager)
    def customUserDetailsService = Mock(CustomUserDetailsService)

    def authService = new AuthServiceImpl(
            userRepository,
            refreshTokenService,
            jwtService,
            authenticationManager,
            customUserDetailsService
    )


    def "login should authenticate user and return tokens successfully"() {
        given:
        def request = new LoginRequest()
        request.setEmail("john@gmail.com")
        request.setPassword("password123")

        def userDetails = Mock(UserDetails)

        def user = new UserEntity()
        user.setEmail("john@gmail.com")
        user.setPassword("encoded-password")

        def accessToken = "access-token"
        def refreshToken = "refresh-token"

        when:
        def result = authService.login(request)

        then:
        1 * authenticationManager.authenticate(
                { Authentication authentication ->
                    authentication instanceof UsernamePasswordAuthenticationToken &&
                            authentication.principal == "john@gmail.com" &&
                            authentication.credentials == "password123"
                }
        )

        1 * customUserDetailsService.loadUserByUsername("john@gmail.com") >> userDetails

        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)

        1 * jwtService.generateAccessToken(userDetails) >> accessToken
        1 * jwtService.generateRefreshToken(userDetails) >> refreshToken

        1 * refreshTokenService.save(user, refreshToken)

        result instanceof AuthResponse
        result.accessToken == accessToken
        result.refreshToken == refreshToken
    }


    def "login should rethrow AuthenticationException when authentication fails"() {
        given:
        def request = new LoginRequest()
        request.setEmail("john@gmail.com")
        request.setPassword("wrong-password")

        def exception = new BadCredentialsException("Bad credentials")

        when:
        authService.login(request)

        then:
        1 * authenticationManager.authenticate(_) >> {
            throw exception
        }

        0 * customUserDetailsService.loadUserByUsername(_)
        0 * userRepository.findByEmail(_)
        0 * jwtService._
        0 * refreshTokenService._

        def thrownException = thrown(BadCredentialsException)
        thrownException == exception
    }


    def "login should throw NotFoundException when user does not exist"() {
        given:
        def request = new LoginRequest()
        request.setEmail("john@gmail.com")
        request.setPassword("password123")

        def userDetails = Mock(UserDetails)

        when:
        authService.login(request)

        then:
        1 * authenticationManager.authenticate(_)

        1 * customUserDetailsService.loadUserByUsername("john@gmail.com") >> userDetails

        1 * userRepository.findByEmail("john@gmail.com") >> Optional.empty()

        0 * jwtService._
        0 * refreshTokenService._

        def exception = thrown(NotFoundException)
        exception.message == "User not found"
    }


    def "refreshToken should generate new tokens successfully"() {
        given:
        def request = new RefreshRequest()
        request.setRefreshToken("old-refresh-token")

        def user = new UserEntity()
        user.setEmail("john@gmail.com")
        user.setPassword("encoded-password")
        user.setRoles([Roles.ROLE_USER] as Set)

        def storedToken = new RefreshTokenEntity()
        storedToken.setToken("old-refresh-token")
        storedToken.setUser(user)

        def newAccessToken = "new-access-token"
        def newRefreshToken = "new-refresh-token"

        when:
        def result = authService.refreshToken(request)

        then:
        1 * refreshTokenService.findByToken("old-refresh-token") >> storedToken

        1 * refreshTokenService.validate(storedToken)

        1 * jwtService.isTokenValid(
                "old-refresh-token",
                _
        ) >> true

        1 * jwtService.extractEmail("old-refresh-token") >> "john@gmail.com"

        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)

        1 * jwtService.generateAccessToken(_) >> newAccessToken
        1 * jwtService.generateRefreshToken(_) >> newRefreshToken

        1 * refreshTokenService.deleteByToken("old-refresh-token")

        1 * refreshTokenService.save(user, newRefreshToken)

        result instanceof AuthResponse
        result.accessToken == newAccessToken
        result.refreshToken == newRefreshToken
    }


    def "refreshToken should throw exception when refresh token is invalid"() {
        given:
        def request = new RefreshRequest()
        request.setRefreshToken("invalid-token")

        def user = new UserEntity()
        user.setEmail("john@gmail.com")
        user.setPassword("encoded-password")

        def storedToken = new RefreshTokenEntity()
        storedToken.setToken("invalid-token")
        storedToken.setUser(user)

        when:
        authService.refreshToken(request)

        then:
        1 * refreshTokenService.findByToken("invalid-token") >> storedToken

        1 * refreshTokenService.validate(storedToken)

        1 * jwtService.isTokenValid(
                "invalid-token",
                _
        ) >> false

        0 * jwtService.extractEmail(_)
        0 * userRepository.findByEmail(_)
        0 * jwtService.generateAccessToken(_)
        0 * jwtService.generateRefreshToken(_)

        0 * refreshTokenService.deleteByToken(_)
        0 * refreshTokenService.save(_, _)

        def exception = thrown(RuntimeException)
        exception.message == "Invalid refresh token"
    }


    def "refreshToken should throw exception when user does not exist"() {
        given:
        def request = new RefreshRequest()
        request.setRefreshToken("valid-refresh-token")

        def user = new UserEntity()
        user.setEmail("john@gmail.com")
        user.setPassword("encoded-password")

        def storedToken = new RefreshTokenEntity()
        storedToken.setToken("valid-refresh-token")
        storedToken.setUser(user)

        when:
        authService.refreshToken(request)

        then:
        1 * refreshTokenService.findByToken("valid-refresh-token") >> storedToken

        1 * refreshTokenService.validate(storedToken)

        1 * jwtService.isTokenValid(
                "valid-refresh-token",
                _
        ) >> true

        1 * jwtService.extractEmail("valid-refresh-token") >> "john@gmail.com"

        1 * userRepository.findByEmail("john@gmail.com") >> Optional.empty()

        0 * jwtService.generateAccessToken(_)
        0 * jwtService.generateRefreshToken(_)

        0 * refreshTokenService.deleteByToken(_)
        0 * refreshTokenService.save(_, _)

        def exception = thrown(RuntimeException)
        exception.message == "User not found"
    }


    def "logout should delete refresh token successfully"() {
        given:
        def request = new RefreshRequest()
        request.setRefreshToken("refresh-token")

        def tokenEntity = new RefreshTokenEntity()
        tokenEntity.setToken("refresh-token")

        when:
        authService.logout(request)

        then:
        1 * refreshTokenService.findByToken("refresh-token") >> tokenEntity

        1 * refreshTokenService.deleteByToken("refresh-token")
    }


    def "logout should not delete token when token is not found"() {
        given:
        def request = new RefreshRequest()
        request.setRefreshToken("missing-token")

        when:
        authService.logout(request)

        then:
        1 * refreshTokenService.findByToken("missing-token") >> {
            throw new NotFoundException("Refresh token not found")
        }

        0 * refreshTokenService.deleteByToken(_)

        def exception = thrown(NotFoundException)
        exception.message == "Refresh token not found"
    }
}