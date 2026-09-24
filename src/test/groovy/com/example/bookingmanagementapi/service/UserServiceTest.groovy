package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.dto.request.UpdateUserRequest
import com.example.bookingmanagementapi.dto.request.UserRequest
import com.example.bookingmanagementapi.dto.response.UserResponse
import com.example.bookingmanagementapi.entity.AccountEntity
import com.example.bookingmanagementapi.entity.UserEntity
import com.example.bookingmanagementapi.enums.Currency
import com.example.bookingmanagementapi.enums.Roles
import com.example.bookingmanagementapi.enums.UserStatus
import com.example.bookingmanagementapi.exception.*
import com.example.bookingmanagementapi.mapper.UserMapper
import com.example.bookingmanagementapi.repository.UserRepository
import com.example.bookingmanagementapi.service.impl.UserServiceImpl
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate

class UserServiceTest extends Specification {

    def userRepository = Mock(UserRepository)
    def userMapper = Mock(UserMapper)
    def passwordEncoder = Mock(PasswordEncoder)
    def accountService = Mock(AccountService)
    def emailVerificationService = Mock(EmailVerificationService)

    def userService = new UserServiceImpl(
            userRepository,
            userMapper,
            passwordEncoder,
            accountService,
            emailVerificationService
    )

    def "registerUser should create user, account and verification token"() {
        given:
        def request = new UserRequest()

        request.setFirstName("John")
        request.setLastName("Doe")
        request.setEmail("john@gmail.com")
        request.setPassword("password123")
        request.setPhoneNumber("+994501234567")
        request.setDateOfBirth(LocalDate.of(2000, 5, 10))
        request.setCurrency(Currency.AZN)

        def encodedPassword = "encoded-password"
        def account = new AccountEntity()

        when:
        userService.registerUser(request)

        then:
        1 * passwordEncoder.encode("password123") >> encodedPassword
        1 * accountService.createDefaultAccount({
            UserEntity user ->
                user.getFirstName() == "John" &&
                        user.getLastName() == "Doe" &&
                        user.getEmail() == "john@gmail.com" &&
                        user.getPassword() == encodedPassword &&
                        user.getPhoneNumber() == "+994501234567" &&
                        user.getDateOfBirth() == LocalDate.of(2000, 5, 10) &&
                        user.getRoles() == Set.of(Roles.ROLE_USER)
        }, Currency.AZN) >> account

        1 * userRepository.save({
            UserEntity user ->
                user.getFirstName() == "John" &&
                        user.getLastName() == "Doe" &&
                        user.getEmail() == "john@gmail.com" &&
                        user.getPassword() == encodedPassword &&
                        user.getRoles() == Set.of(Roles.ROLE_USER) &&
                        user.getAccounts().contains(account)
        })

        1 * emailVerificationService.create({
            UserEntity user ->
                user.getEmail() == "john@gmail.com" &&
                        user.getAccounts().contains(account)
        })
    }

    def "deleteUser should set user status to DELETED"() {
        given:
        def user = new UserEntity(
                id: 1L,
                email: "john@gmail.com",
                isActive: UserStatus.ACTIVE
        )

        when:
        userService.deleteUser(1L)

        then:
        1 * userRepository.findById(1L) >> Optional.of(user)

        user.getIsActive() == UserStatus.DELETED
    }

    def "deleteUser should throw NotFoundException when user does not exist"() {
        when:
        userService.deleteUser(1L)

        then:
        1 * userRepository.findById(1L) >> Optional.empty()
        thrown(NotFoundException)
    }

    def "updateUser should update and save user"() {
        given:
        def user = new UserEntity(
                id: 1L,
                email: "old@gmail.com"
        )

        def request = new UpdateUserRequest()

        when:
        userService.updateUser(1L, request)

        then:
        1 * userRepository.findById(1L) >> Optional.of(user)
        1 * userMapper.updateUser(user, request)
        1 * userRepository.save(user)
    }

    def "updateUser should throw NotFoundException when user does not exist"() {
        given:
        def request = new UpdateUserRequest()

        when:
        userService.updateUser(1L, request)

        then:
        1 * userRepository.findById(1L) >> Optional.empty()
        0 * userMapper._
        0 * userRepository.save(_)

        thrown(NotFoundException)
    }

    def "findById should return mapped user"() {
        given:
        def user = new UserEntity(id: 1L)
        def response = new UserResponse()

        when:
        def result = userService.findById(1L)

        then:
        1 * userRepository.findById(1L) >> Optional.of(user)
        1 * userMapper.toDto(user) >> response

        result == response
    }

    def "findById should throw NotFoundException when user does not exist"() {
        when:
        userService.findById(1L)

        then:
        1 * userRepository.findById(1L) >> Optional.empty()
        0 * userMapper._

        thrown(NotFoundException)
    }

    def "findAll should return mapped page"() {
        given:
        Pageable pageable = PageRequest.of(0, 10)

        def user1 = new UserEntity(id: 1L)
        def user2 = new UserEntity(id: 2L)

        def response1 = new UserResponse()
        def response2 = new UserResponse()

        def page = new PageImpl<UserEntity>(
                [user1, user2],
                pageable,
                2
        )

        when:
        def result = userService.findAll(pageable)

        then:
        1 * userRepository.findAll(pageable) >> page
        1 * userMapper.toDto(user1) >> response1
        1 * userMapper.toDto(user2) >> response2

        result.content == [response1, response2]
        result.totalElements == 2
    }

    def "findAll should return empty page when no users exist"() {
        given:
        Pageable pageable = PageRequest.of(0, 10)

        def page = new PageImpl<UserEntity>(
                [],
                pageable,
                0
        )

        when:
        def result = userService.findAll(pageable)

        then:
        1 * userRepository.findAll(pageable) >> page
        0 * userMapper._

        result.empty
    }

    def "blockUser should set user status to BLOCKED"() {
        given:
        def user = new UserEntity(
                id: 1L,
                email: "john@gmail.com",
                isActive: UserStatus.ACTIVE
        )

        when:
        userService.blockUser(1L)

        then:
        1 * userRepository.findById(1L) >> Optional.of(user)

        user.getIsActive() == UserStatus.BLOCKED
    }

    def "blockUser should throw NotFoundException when user does not exist"() {
        when:
        userService.blockUser(1L)

        then:
        1 * userRepository.findById(1L) >> Optional.empty()

        thrown(NotFoundException)
    }

    def "unblockUser should set user status to ACTIVE"() {
        given:
        def user = new UserEntity(
                id: 1L,
                email: "john@gmail.com",
                isActive: UserStatus.BLOCKED
        )

        when:
        userService.unblockUser(1L)

        then:
        1 * userRepository.findById(1L) >> Optional.of(user)

        user.getIsActive() == UserStatus.ACTIVE
    }

    def "unblockUser should throw NotFoundException when user does not exist"() {
        when:
        userService.unblockUser(1L)

        then:
        1 * userRepository.findById(1L) >> Optional.empty()

        thrown(NotFoundException)
    }

    def "getCurrentUser should return mapped user"() {
        given:
        def user = new UserEntity(id: 1L)
        def response = new UserResponse()

        when:
        def result = userService.getCurrentUser(1L)

        then:
        1 * userRepository.findById(1L) >> Optional.of(user)
        1 * userMapper.toDto(user) >> response

        result == response
    }

    def "getCurrentUser should throw NotFoundException when user does not exist"() {
        when:
        userService.getCurrentUser(1L)

        then:
        1 * userRepository.findById(1L) >> Optional.empty()
        0 * userMapper._

        thrown(NotFoundException)
    }

    def "validateUserCanBook should do nothing for ACTIVE user"() {
        given:
        def user = new UserEntity(
                id: 1L,
                isActive: UserStatus.ACTIVE
        )

        when:
        userService.validateUserCanBook(1L)

        then:
        1 * userRepository.findById(1L) >> Optional.of(user)
        noExceptionThrown()
    }

    def "validateUserCanBook should throw UserBlockedException for DELETED user"() {
        given:
        def user = new UserEntity(
                id: 1L,
                isActive: UserStatus.DELETED
        )

        when:
        userService.validateUserCanBook(1L)

        then:
        1 * userRepository.findById(1L) >> Optional.of(user)

        def exception = thrown(UserBlockedException)
        exception.message == "User is deleted"
    }

    def "validateUserCanBook should throw UserDeletedException for BLOCKED user"() {
        given:
        def user = new UserEntity(
                id: 1L,
                isActive: UserStatus.BLOCKED
        )

        when:
        userService.validateUserCanBook(1L)

        then:
        1 * userRepository.findById(1L) >> Optional.of(user)

        def exception = thrown(UserDeletedException)
        exception.message == "This user is blocked"
    }

    def "validateUserCanBook should throw UserNotVerifiedException for NOT_VERIFIED user"() {
        given:
        def user = new UserEntity(
                id: 1L,
                isActive: UserStatus.NOT_VERIFIED
        )

        when:
        userService.validateUserCanBook(1L)

        then:
        1 * userRepository.findById(1L) >> Optional.of(user)

        def exception = thrown(UserNotVerifiedException)
        exception.message == "This user is not verified"
    }

    def "validateUserCanBook should throw NotFoundException when user does not exist"() {
        when:
        userService.validateUserCanBook(1L)

        then:
        1 * userRepository.findById(1L) >> Optional.empty()

        thrown(NotFoundException)
    }

    def "uploadProfilePhoto should upload JPEG successfully"() {
        given:
        def user = new UserEntity(
                id: 1L,
                email: "john@gmail.com"
        )

        def file = Mock(MultipartFile)

        when:
        userService.uploadProfilePhoto("john@gmail.com", file)

        then:
        1 * file.isEmpty() >> false
        1 * file.getContentType() >> "image/jpeg"
        1 * file.getSize() >> 1024L
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        1 * file.getOriginalFilename() >> "profile.jpg"
        1 * file.getBytes() >> "test image".bytes

        user.getProfilePhotoUrl() != null
        user.getProfilePhotoUrl().startsWith("/uploads/profile-photos/")
        user.getProfilePhotoUrl().endsWith("_profile.jpg")

        cleanup:
        if (user.getProfilePhotoUrl() != null) {
            Files.deleteIfExists(
                    Paths.get(user.getProfilePhotoUrl().substring(1))
            )
        }
    }

    def "uploadProfilePhoto should upload PNG successfully"() {
        given:
        def user = new UserEntity(
                id: 1L,
                email: "john@gmail.com"
        )

        def file = Mock(MultipartFile)

        when:
        userService.uploadProfilePhoto("john@gmail.com", file)

        then:
        1 * file.isEmpty() >> false
        1 * file.getContentType() >> "image/png"
        1 * file.getSize() >> 1024L
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        1 * file.getOriginalFilename() >> "profile.png"
        1 * file.getBytes() >> "test image".bytes

        user.getProfilePhotoUrl() != null
        user.getProfilePhotoUrl().endsWith("_profile.png")

        cleanup:
        if (user.getProfilePhotoUrl() != null) {
            Files.deleteIfExists(
                    Paths.get(user.getProfilePhotoUrl().substring(1))
            )
        }
    }

    def "uploadProfilePhoto should throw ValidationException when file is empty"() {
        given:
        def file = Mock(MultipartFile)

        when:
        userService.uploadProfilePhoto("john@gmail.com", file)

        then:
        1 * file.isEmpty() >> true
        0 * file.getContentType()
        0 * userRepository._

        def exception = thrown(ValidationException)
        exception.message == "File is empty"
    }

    def "uploadProfilePhoto should throw ValidationException for unsupported content type"() {
        given:
        def file = Mock(MultipartFile)

        when:
        userService.uploadProfilePhoto("john@gmail.com", file)

        then:
        1 * file.isEmpty() >> false
        1 * file.getContentType() >> "application/pdf"
        0 * userRepository._

        def exception = thrown(ValidationException)
        exception.message == "Only JPEG and PNG images are allowed"
    }

    def "uploadProfilePhoto should throw ValidationException when content type is null"() {
        given:
        def file = Mock(MultipartFile)

        when:
        userService.uploadProfilePhoto("john@gmail.com", file)

        then:
        1 * file.isEmpty() >> false
        1 * file.getContentType() >> null
        0 * userRepository._

        def exception = thrown(ValidationException)
        exception.message == "Only JPEG and PNG images are allowed"
    }

    def "uploadProfilePhoto should throw ValidationException when file is larger than 5 MB"() {
        given:
        def file = Mock(MultipartFile)

        when:
        userService.uploadProfilePhoto("john@gmail.com", file)

        then:
        1 * file.isEmpty() >> false
        1 * file.getContentType() >> "image/jpeg"
        1 * file.getSize() >> 5 * 1024 * 1024 + 1
        0 * userRepository._

        def exception = thrown(ValidationException)
        exception.message == "File size must not exceed 5 MB"
    }

    def "uploadProfilePhoto should throw NotFoundException when user does not exist"() {
        given:
        def file = Mock(MultipartFile)

        when:
        userService.uploadProfilePhoto("john@gmail.com", file)

        then:
        1 * file.isEmpty() >> false
        1 * file.getContentType() >> "image/jpeg"
        1 * file.getSize() >> 1024L
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.empty()

        thrown(NotFoundException)
    }

    def "deleteProfilePhoto should delete existing photo"() {
        given:
        def user = new UserEntity()
        user.setId(1L)
        user.setEmail("john@gmail.com")
        user.setProfilePhotoUrl("/uploads/profile-photos/test-profile.jpg")

        def path = Paths.get("uploads/profile-photos/test-profile.jpg")
        Files.createDirectories(path.parent)
        Files.write(path, "test".bytes)

        when:
        userService.deleteProfilePhoto("john@gmail.com")

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)

        user.getProfilePhotoUrl() == null
        !Files.exists(path)

        cleanup:
        Files.deleteIfExists(path)
    }

    def "deleteProfilePhoto should do nothing when user has no profile photo"() {
        given:
        def user = new UserEntity(
                id: 1L,
                email: "john@gmail.com",
                profilePhotoUrl: null
        )

        when:
        userService.deleteProfilePhoto("john@gmail.com")

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)

        user.getProfilePhotoUrl() == null
    }

    def "deleteProfilePhoto should throw NotFoundException when user does not exist"() {
        when:
        userService.deleteProfilePhoto("john@gmail.com")

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.empty()

        thrown(NotFoundException)
    }
}