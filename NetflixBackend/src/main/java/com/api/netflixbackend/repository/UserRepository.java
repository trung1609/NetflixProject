package com.api.netflixbackend.repository;

import com.api.netflixbackend.entity.User;
import com.api.netflixbackend.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByVerificationToken(String token);

    Optional<User> findByPasswordResetToken(String token, String passwordReset);


    long countByRoleAndActive(Role role, boolean active);
}
