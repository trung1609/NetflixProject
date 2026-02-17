package com.api.netflixbackend.controller;

import com.api.netflixbackend.dto.request.UserRequest;
import com.api.netflixbackend.dto.response.MessageResponse;
import com.api.netflixbackend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@PreAuthorize( "hasRole('ADMIN')")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<MessageResponse> createUser(@Valid @RequestBody UserRequest userRequest){
        return ResponseEntity.ok(userService.createUser(userRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> updateUser(@PathVariable Long id, @RequestBody UserRequest userRequest){
        return ResponseEntity.ok(userService.updateUser(id, userRequest));
    }
}
