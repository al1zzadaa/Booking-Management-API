package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.dto.request.PrivilegeRequest
import com.example.bookingmanagementapi.entity.UserEntity
import com.example.bookingmanagementapi.enums.Roles
import com.example.bookingmanagementapi.exception.AdminException
import com.example.bookingmanagementapi.exception.NotFoundException
import com.example.bookingmanagementapi.repository.UserRepository
import com.example.bookingmanagementapi.service.impl.AdminServiceImpl
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Component
import spock.lang.Specification

@Component
@RequiredArgsConstructor
class AdminServiceTest extends Specification {

    def userRepository = Mock(UserRepository)

    def adminService = new AdminServiceImpl(
            userRepository
    )


    def "makeAdmin should make user admin successfully"() {
        given:
        def request = new PrivilegeRequest()
        request.setEmail("john@gmail.com")

        def user = new UserEntity()
        user.setRoles([] as Set)

        when:
        adminService.makeAdmin(request)

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        1 * userRepository.save(user)

        user.getRoles().contains(Roles.ROLE_ADMIN)
    }


    def "makeAdmin should throw NotFoundException when user does not exist"() {
        given:
        def request = new PrivilegeRequest()
        request.setEmail("john@gmail.com")

        when:
        adminService.makeAdmin(request)

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.empty()
        0 * userRepository.save(_)

        def exception = thrown(NotFoundException)
        exception.message == "username not found"
    }


    def "makeAdmin should throw AdminException when user is already admin"() {
        given:
        def request = new PrivilegeRequest()
        request.setEmail("john@gmail.com")

        def user = new UserEntity()
        user.setRoles([Roles.ROLE_ADMIN] as Set)

        when:
        adminService.makeAdmin(request)

        then:
        1 * userRepository.findByEmail("john@gmail.com") >> Optional.of(user)
        0 * userRepository.save(_)

        def exception = thrown(AdminException)
        exception.message == "user already admin"
    }
}
