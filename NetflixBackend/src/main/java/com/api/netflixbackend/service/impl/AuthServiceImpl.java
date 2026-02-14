package com.api.netflixbackend.service.impl;

import com.api.netflixbackend.dto.request.UserRequest;
import com.api.netflixbackend.dto.response.LoginResponse;
import com.api.netflixbackend.dto.response.MessageResponse;
import com.api.netflixbackend.entity.User;
import com.api.netflixbackend.enums.Role;
import com.api.netflixbackend.exception.AccountDeactivatedException;
import com.api.netflixbackend.exception.BadCredentialsException;
import com.api.netflixbackend.exception.EmailAlreadyExistsException;
import com.api.netflixbackend.exception.EmailNotVerifiedException;
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
}
