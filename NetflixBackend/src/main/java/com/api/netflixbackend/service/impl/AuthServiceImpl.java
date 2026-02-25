package com.api.netflixbackend.service.impl;

import com.api.netflixbackend.dto.request.UserRequest;
import com.api.netflixbackend.dto.response.EmailValidationResponse;
import com.api.netflixbackend.dto.response.LoginResponse;
import com.api.netflixbackend.dto.response.MessageResponse;
import com.api.netflixbackend.entity.User;
import com.api.netflixbackend.enums.Role;
import com.api.netflixbackend.exception.*;
import com.api.netflixbackend.repository.UserRepository;
import com.api.netflixbackend.security.JwtUtil;
import com.api.netflixbackend.service.AuthService;
import com.api.netflixbackend.service.EmailService;
import com.api.netflixbackend.util.ServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ServiceUtils serviceUtils;

    @Override
    public MessageResponse signup(UserRequest userRequest) {

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists!");
        }

        User user = new User();
        user.setEmail(userRequest.getEmail());
        user.setFullName(userRequest.getFullName());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setActive(true);
        user.setRole(Role.USER);
        user.setEmailVerified(false);
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpiryDate(Instant.now().plusSeconds(86400));
        userRepository.save(user);
        emailService.sendVerificationEmail(userRequest.getEmail(), verificationToken);

        return new MessageResponse("Registration successful! Please check your email for verify your account.");

    }

    @Override
    public LoginResponse login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(password, u.getPassword()))
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
        if (!user.getActive()) {
            throw new AccountDeactivatedException("Account is deactivated! Please contact support for assistance.");
        }

        if (!user.getEmailVerified()) {
            throw new EmailNotVerifiedException("Please verify your email address before login in. Check your inbox for the verification link.");
        }

        final String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return new LoginResponse(token, user.getEmail(), user.getFullName(), user.getRole().name());
    }

    @Override
    public EmailValidationResponse validateEmail(String email) {
        boolean exists = userRepository.existsByEmail(email);
        return new EmailValidationResponse(exists, !exists);
    }

    @Override
    public MessageResponse verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token).orElseThrow(() -> new InvalidTokenException("Invalid verification token!"));
        if (user.getVerificationTokenExpiryDate() == null || user.getVerificationTokenExpiryDate().isBefore(Instant.now())) {
            throw new InvalidTokenException("Verification link has expired. Please request a new verification email.");
        }
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiryDate(null);
        userRepository.save(user);
        return new MessageResponse("Email verified successfully! You can now log in to your account.");
    }

    @Override
    public MessageResponse resendVerificationEmail(String email) {
        User user = serviceUtils.getUserByEmailOrThrow(email);
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpiryDate(Instant.now().plusSeconds(86400));
        userRepository.save(user);
        emailService.sendVerificationEmail(email, verificationToken);
        return new MessageResponse("Verification email resent successfully!");
    }

    @Override
    public MessageResponse forgotPassword(String email) {
        User user = serviceUtils.getUserByEmailOrThrow(email);
        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiryDate(Instant.now().plusSeconds(3600));
        userRepository.save(user);
        emailService.sendPasswordResetEmail(email, token);
        return new MessageResponse("Password reset email sent successfully! Please check your inbox for the reset link.");
    }

    @Override
    public MessageResponse resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token, newPassword).orElseThrow(() -> new InvalidTokenException("Invalid reset token!"));
        if (user.getPasswordResetTokenExpiryDate() == null || user.getPasswordResetTokenExpiryDate().isBefore(Instant.now())) {
            throw new InvalidTokenException("Reset link has expired. Please request a new reset link.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiryDate(null);
        userRepository.save(user);
        return new MessageResponse("Password reset successful! You can now log in to your account.");
    }

    @Override
    public MessageResponse changePassword(String email, String currentPassword, String newPassword) {
        User user = serviceUtils.getUserByEmailOrThrow(email);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid current password!");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return new MessageResponse("Password changed successfully!");
    }

    @Override
    public LoginResponse currentUser(String email) {
        User user = serviceUtils.getUserByEmailOrThrow(email);
        return new LoginResponse(
                null,
                user.getEmail(),
                user.getFullName(),
                user.getRole().name()
        );
    }

}
