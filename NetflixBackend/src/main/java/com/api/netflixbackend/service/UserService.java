package com.api.netflixbackend.service;

import com.api.netflixbackend.dto.request.UserRequest;
import com.api.netflixbackend.dto.response.MessageResponse;
import jakarta.validation.Valid;

import java.net.URI;

public interface UserService {


    MessageResponse createUser(UserRequest userRequest);

    MessageResponse updateUser(Long id, UserRequest userRequest);
}
