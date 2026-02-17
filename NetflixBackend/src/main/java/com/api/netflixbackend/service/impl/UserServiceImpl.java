package com.api.netflixbackend.service.impl;

import com.api.netflixbackend.dto.request.UserRequest;
import com.api.netflixbackend.dto.response.MessageResponse;
import com.api.netflixbackend.entity.User;
import com.api.netflixbackend.enums.Role;
import com.api.netflixbackend.exception.EmailAlreadyExistsException;
import com.api.netflixbackend.exception.InvalidRoleException;
import com.api.netflixbackend.repository.UserRepository;
import com.api.netflixbackend.service.EmailService;
import com.api.netflixbackend.service.UserService;
import com.api.netflixbackend.util.ServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ServiceUtils serviceUtils;

    @Autowired
    private EmailService emailService;

    @Override
    public MessageResponse createUser(UserRequest userRequest) {
        if (userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists!");
        }
        validateRole(userRequest.getRole());

        User user = new User();
        user.setEmail(userRequest.getEmail());
        user.setFullName(userRequest.getFullName());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setRole(Role.valueOf(userRequest.getRole().toUpperCase()));
        user.setActive(true);
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpiryDate(Instant.now().plusSeconds(86400));
        userRepository.save(user);

        emailService.sendVerificationEmail(userRequest.getEmail(), verificationToken);

        return new MessageResponse("User created successfully!");
    }

    @Override
    public MessageResponse updateUser(Long id, UserRequest userRequest) {
        User user = serviceUtils.getUserByIdOrThrow(id);

        ensureNotLastActiveAdmin(user);
        validateRole(userRequest.getRole());

        user.setFullName(userRequest.getFullName());
        user.setRole(Role.valueOf(userRequest.getRole().toUpperCase()));
        userRepository.save(user);
        return new MessageResponse("User updated successfully!");
    }

    private void ensureNotLastActiveAdmin(User user) {
        if (user.getActive() && user.getRole() == Role.ADMIN) {
            long activeAdminCount = userRepository.countByRoleAndActive(Role.ADMIN, true);
            if (activeAdminCount <= 1){
                throw new RuntimeException("Cannot deactivate last active admin user");
            }
        }


    }

    private void validateRole(String role) {
        if (Arrays.stream(Role.values()).noneMatch(r -> r.name().equalsIgnoreCase(role))) {
            throw new InvalidRoleException("Invalid role!");
        }
    }
}
