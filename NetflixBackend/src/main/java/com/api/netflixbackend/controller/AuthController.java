package com.api.netflixbackend.controller;

import com.api.netflixbackend.dto.request.LoginRequest;
import com.api.netflixbackend.dto.request.UserRequest;
import com.api.netflixbackend.dto.response.LoginResponse;
import com.api.netflixbackend.dto.response.MessageResponse;
import com.api.netflixbackend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
