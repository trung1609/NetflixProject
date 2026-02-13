package com.api.netflixbackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "videos")
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 4000)
    private String description;

    private Integer duration;
    private Integer year;
    private String rating;

    @Column(name = "src")
    @JsonIgnore
    private String srcUuid;

    @Column(name = "poster")
    @JsonIgnore
    private String posterUuid;

    @Column(nullable = false)
    private Boolean published = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "video_categories", joinColumns = @JoinColumn(name = "video_id"))
    @Column(name = "category")
    private List<String> categories = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @Transient
    @JsonProperty("isInWatchlist")
    private Boolean isInWatchlist;

    @JsonProperty("src")
    public String getSrc() {
        if (srcUuid != null && !srcUuid.isEmpty()) {
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
            return baseUrl + "/api/files/video/" + srcUuid;
        }
        return null;
    }

    @JsonProperty("poster")
    public String getPoster() {
        if (posterUuid != null && !posterUuid.isEmpty()) {
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
            return baseUrl + "/api/files/image/" + posterUuid;
        }
        return null;
    }
}
