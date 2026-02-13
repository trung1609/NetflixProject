package com.api.netflixbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VideoStatusResponse {
    private Long totalVideos;
    private Long publishedVideos;
    private Long totalDuration;
}
