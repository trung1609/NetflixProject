package com.api.netflixbackend.entity;

import com.api.netflixbackend.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
@ToString
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Boolean emailVerified = false;

    @Column(unique = true)
    private String verificationToken;

    @Column
    private Instant verificationTokenExpiryDate;

    @Column
    private String passwordResetToken;

    @Column
    private Instant passwordResetTokenExpiryDate;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_watchlist", joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "video_id"))
    private Set<Video> watchList = new HashSet<>();

    public void addToWatchList(Video video){
        this.watchList.add(video);
    }

    public void removeFromWatchList(Video video){
        this.watchList.remove(video);
    }
}
