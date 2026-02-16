package com.grosso.chat.service;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class EmailService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    );
    private final JavaMailSender mailSender;
    private final static Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    @Async
    public void send(String to, String email) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            helper.setText(email, true);
            helper.setTo(to);
            helper.setSubject("Confirm your email!");
            helper.setFrom("franco.grosso.agustin@gmail.com");
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            LOGGER.error("Failed to send email!", e);
            throw new IllegalStateException("Failed to send email!");
        }
    }

    public boolean validateEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
}
