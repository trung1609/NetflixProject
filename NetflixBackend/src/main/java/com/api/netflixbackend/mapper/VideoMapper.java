package com.api.netflixbackend.mapper;

import com.api.netflixbackend.dto.response.VideoResponse;
import com.api.netflixbackend.entity.Video;
import org.springframework.stereotype.Component;

@Component
public class VideoMapper {
    public static VideoResponse toDTO(Video video) {
        VideoResponse response =  new VideoResponse(
                video.getId(),
                video.getTitle(),
                video.getDescription(),
                video.getDuration(),
                video.getYear(),
                video.getRating(),
                video.getSrc(),
                video.getPoster(),
                video.getPublished(),
                video.getCategories(),
                video.getCreatedAt(),
                video.getUpdatedAt()
        );
        if (video.getIsInWatchlist() != null) {
            response.setIsInWatchlist(video.getIsInWatchlist());
        }
        return response;
    }
}
