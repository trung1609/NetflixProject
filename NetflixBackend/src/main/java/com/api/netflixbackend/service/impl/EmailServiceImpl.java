package com.api.netflixbackend.service.impl;

import com.api.netflixbackend.exception.EmailNotVerifiedException;
import com.api.netflixbackend.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Netflix Verification Email");
            String emailBody = verifyEmail(token);
            message.setText(emailBody);
            mailSender.send(message);
            logger.info("Verification email sent to {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send verification email to {}: {}", toEmail, e.getMessage(), e);
            throw new EmailNotVerifiedException("Failed to send verification email.");
        }
    }

    private String verifyEmail(String token) {
        String verificationLink = frontendUrl + "/verify-email?token=" + token;
        return "Welcome to Netflix Clone! \n\n"
                + "Thank you for registering. Please verify your email address by clicking the link below: \n\n"
                + verificationLink
                + "\n\n"
                + "This link will expire in 24 hours.\n\n"
                + "If you didn't create this account, please ignore this email.\n\n"
                + "Best regards,\n"
                + "Netflix Team";
    }

    private String resetPassword(String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        return "Hi, \n\n"
                + "We received a request to reset your password. Click the link below to reset it: \n\n"
                + resetLink
                + "\n\n"
                + "This link will expire in 1 hours\n\n"
                + "If you didn't request a password reset, please ignore this email.\n\n"
                + "Best regards,\n"
                + "Netflix Team";
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Netflix Password Reset Email");
            String emailBody = resetPassword(token);
            message.setText(emailBody);
            mailSender.send(message);
            logger.info("Password reset email sent to {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send password reset email.");
        }
    }
}
