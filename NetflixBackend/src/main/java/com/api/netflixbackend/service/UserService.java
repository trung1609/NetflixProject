package com.api.netflixbackend.service;

import com.api.netflixbackend.dto.request.UserRequest;
import com.api.netflixbackend.dto.response.MessageResponse;
import com.api.netflixbackend.dto.response.PageResponse;
import com.api.netflixbackend.dto.response.UserResponse;
import jakarta.validation.Valid;

import java.net.URI;

public interface UserService {


    MessageResponse createUser(UserRequest userRequest);

    MessageResponse updateUser(Long id, UserRequest userRequest);

    PageResponse<UserResponse> getUsers(int page, int size, String search);

    MessageResponse deleteUser(Long id, String email);

    MessageResponse toggleUserStatus(Long id, String email);

    MessageResponse changeUserRole(Long id, UserRequest userRequest);
}
