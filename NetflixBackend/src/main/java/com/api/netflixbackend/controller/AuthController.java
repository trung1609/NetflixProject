package com.api.netflixbackend.controller;

import com.api.netflixbackend.dto.request.*;
import com.api.netflixbackend.dto.response.EmailValidationResponse;
import com.api.netflixbackend.dto.response.LoginResponse;
import com.api.netflixbackend.dto.response.MessageResponse;
import com.api.netflixbackend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody
                                                    UserRequest userRequest) {
        return ResponseEntity.ok(authService.signup(userRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login (@Valid @RequestBody LoginRequest request){
        LoginResponse response = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate-email")
    public ResponseEntity<EmailValidationResponse> validateEmail (@RequestParam String email){
        return ResponseEntity.ok(authService.validateEmail(email));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam String token){
        return ResponseEntity.ok(authService.verifyEmail(token));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerificationEmail(@Valid @RequestBody EmailRequest request) {
        return ResponseEntity.ok(authService.resendVerificationEmail(request.getEmail()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody EmailRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request.getEmail()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request.getToken(), request.getNewPassword()));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(Authentication authentication,
                                                          @Valid @RequestBody ChangePasswordRequest request) {
        String email = authentication.getName();
        return ResponseEntity.ok(
                authService.changePassword(
                        email,
                        request.getCurrentPassword(),
                        request.getNewPassword()
                )
        );
    }

    @GetMapping("/current-user")
    public ResponseEntity<LoginResponse> currentUser(Authentication authentication){
        String email = authentication.getName();
        return ResponseEntity.ok(authService.currentUser(email));
    }
}
