package com.api.netflixbackend.service;

import com.api.netflixbackend.dto.request.UserRequest;
import com.api.netflixbackend.dto.response.LoginResponse;
import com.api.netflixbackend.dto.response.MessageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public interface AuthService {

    MessageResponse signup(@Valid UserRequest userRequest);

    LoginResponse login(String email, String password);
}
