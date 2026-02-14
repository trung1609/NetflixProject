package com.api.netflixbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VideoResponse {
    private Long id;
    private String title;
    private String description;
    private Integer duration;
    private Integer year;
    private String rating;
    private String src;
    private String poster;
    private Boolean published;
    private List<String> categories;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean isInWatchlist;

    public VideoResponse(Long id, String title, String description, Integer duration, Integer year, String rating, String src, String poster, Boolean published, List<String> categories, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.year = year;
        this.rating = rating;
        this.src = src;
        this.poster = poster;
        this.published = published;
        this.categories = categories;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
