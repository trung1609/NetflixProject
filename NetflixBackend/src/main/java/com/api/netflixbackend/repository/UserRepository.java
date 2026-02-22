package com.api.netflixbackend.repository;

import com.api.netflixbackend.entity.User;
import com.api.netflixbackend.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByVerificationToken(String token);

    Optional<User> findByPasswordResetToken(String token, String passwordReset);


    long countByRoleAndActive(Role role, boolean active);

    @Query("select u from User u where " +
            "lower(u.fullName) like lower(concat('%', :search, '%')) or " +
            "lower(u.email) like lower(concat('%', :search, '%') ) ")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    long countByRole(Role role);

    @Query("select v.id from User u join u.watchList v where u.email = :email and v.id in :videoIds")
    Set<Long> findWatchListVideoIds(@Param("email") String email,@Param("videoIds") List<Long> videoIds);
}
