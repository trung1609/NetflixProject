package com.api.netflixbackend.mapper;

import com.api.netflixbackend.dto.request.UserRequest;
import com.api.netflixbackend.dto.response.UserResponse;
import com.api.netflixbackend.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toDTO(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                user.getActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
