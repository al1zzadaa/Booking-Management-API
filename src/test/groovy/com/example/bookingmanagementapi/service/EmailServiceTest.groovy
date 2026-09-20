package com.example.bookingmanagementapi.service

import com.example.bookingmanagementapi.dto.request.MailRequest
import com.example.bookingmanagementapi.exception.EmailSendingException
import com.example.bookingmanagementapi.service.impl.EmailServiceImpl
import jakarta.mail.MessagingException
import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

class EmailServiceTest extends Specification {

    def mailSender = Mock(JavaMailSender)

    def emailService = new EmailServiceImpl(
            mailSender
    )

    def setup() {
        ReflectionTestUtils.setField(
                emailService,
                "from",
                "test@gmail.com"
        )
    }


    def "sendTextEmail should send email successfully"() {
        given:
        def request = new MailRequest()
        request.setTo("receiver@gmail.com")
        request.setSubject("Test subject")
        request.setMessage("Hello, this is a test email")

        when:
        emailService.sendTextEmail(request)

        then:
        1 * mailSender.send({
            it.from == "test@gmail.com"
            it.to == ["receiver@gmail.com"] as String[]
            it.subject == "Test subject"
            it.text == "Hello, this is a test email"
        })
    }


    def "sendEmailWithAttachment should send email with attachment successfully"() {
        given:
        def request = new MailRequest()
        request.setTo("receiver@gmail.com")
        request.setSubject("Test subject")
        request.setMessage("Hello with attachment")

        def file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello file".bytes
        )

        def mimeMessage = Mock(MimeMessage)

        when:
        emailService.sendEmailWithAttachment(request, file)

        then:
        1 * mailSender.createMimeMessage() >> mimeMessage

        1 * mailSender.send(mimeMessage)
    }


    def "sendEmailWithAttachment should throw EmailSendingException when mail sending fails"() {
        given:
        def request = new MailRequest()
        request.setTo("receiver@gmail.com")
        request.setSubject("Test subject")
        request.setMessage("Hello with attachment")

        def file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello file".bytes
        )

        def mimeMessage = Mock(MimeMessage)

        when:
        emailService.sendEmailWithAttachment(request, file)

        then:
        1 * mailSender.createMimeMessage() >> mimeMessage

        1 * mailSender.send(mimeMessage) >> {
            throw new RuntimeException("Mail server error")
        }

        def exception = thrown(RuntimeException)
        exception.message == "Mail server error"
    }

}