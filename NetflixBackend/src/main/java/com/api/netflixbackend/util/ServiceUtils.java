package com.api.netflixbackend.util;

import com.api.netflixbackend.entity.User;
import com.api.netflixbackend.entity.Video;
import com.api.netflixbackend.exception.ResourceNotFoundException;
import com.api.netflixbackend.repository.UserRepository;
import com.api.netflixbackend.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceUtils {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoRepository videoRepository;

    public User getUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public User getUserByIdOrThrow(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public Video getVideoByIdOrThrow(Long id) {
        return videoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + id));
    }

}
