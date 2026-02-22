package com.api.netflixbackend.service.impl;

import com.api.netflixbackend.dto.request.VideoRequest;
import com.api.netflixbackend.dto.response.MessageResponse;
import com.api.netflixbackend.dto.response.PageResponse;
import com.api.netflixbackend.dto.response.VideoResponse;
import com.api.netflixbackend.dto.response.VideoStatusResponse;
import com.api.netflixbackend.entity.Video;
import com.api.netflixbackend.mapper.VideoMapper;
import com.api.netflixbackend.repository.UserRepository;
import com.api.netflixbackend.repository.VideoRepository;
import com.api.netflixbackend.service.VideoService;
import com.api.netflixbackend.util.PaginationUtils;
import com.api.netflixbackend.util.ServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class VideoServiceImpl implements VideoService {

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceUtils serviceUtils;

    @Override
    public MessageResponse createVideoByAdmin(VideoRequest videoRequest) {
        Video video = new Video();
        video.setTitle(videoRequest.getTitle());
        video.setDescription(videoRequest.getDescription());
        video.setYear(videoRequest.getYear());
        video.setDuration(videoRequest.getDuration());
        video.setRating(videoRequest.getRating());
        video.setSrcUuid(videoRequest.getSrc());
        video.setPosterUuid(videoRequest.getPoster());
        video.setPublished(videoRequest.getPublished());
        video.setCategories(videoRequest.getCategories() != null ? videoRequest.getCategories() : List.of());
        videoRepository.save(video);
        return new MessageResponse("Video created successfully");
    }

    @Override
    public PageResponse<VideoResponse> getAllAdminVideos(int page, int size, String search) {
        Pageable pageable = PaginationUtils.createPageRequest(page, size, "id");
        Page<Video> videoPage;

        if (search != null && !search.trim().isEmpty()) {
            videoPage = videoRepository.searchVideos(search.trim(), pageable);
        } else {
            videoPage = videoRepository.findAll(pageable);
        }

        return PaginationUtils.toPageResponse(videoPage, VideoMapper::toDTO);
    }

    @Override
    public MessageResponse updateVideoByAdmin(Long id, VideoRequest videoRequest) {
        Video video = new Video();
        video.setId(id);
        video.setTitle(videoRequest.getTitle());
        video.setDescription(videoRequest.getDescription());
        video.setYear(videoRequest.getYear());
        video.setDuration(videoRequest.getDuration());
        video.setRating(videoRequest.getRating());
        video.setSrcUuid(videoRequest.getSrc());
        video.setPosterUuid(videoRequest.getPoster());
        video.setPublished(videoRequest.getPublished());
        video.setCategories(videoRequest.getCategories() != null ? videoRequest.getCategories() : List.of());

        videoRepository.save(video);
        return new MessageResponse("Video updated successfully");
    }

    @Override
    public MessageResponse deleteVideoByAdmin(Long id) {
        if (!videoRepository.existsById(id)) {
            throw new IllegalArgumentException("Video not found with id: " + id);
        }
        videoRepository.deleteById(id);
        return new MessageResponse("Video deleted successfully");
    }

    @Override
    public MessageResponse toggleVideoPublishStatusByAdmin(Long id, Boolean value) {
        Video video = serviceUtils.getVideoByIdOrThrow(id);
        video.setPublished(value);
        videoRepository.save(video);
        return new MessageResponse("Video publish status changed successfully");
    }

    @Override
    public VideoStatusResponse getAdminStats() {
        long totalVideos = videoRepository.count();
        long publishedVideos = videoRepository.countPublishedVideos();
        long totalDuration = videoRepository.getTotalDuration();
        return new VideoStatusResponse(totalVideos, publishedVideos, totalDuration);
    }

    @Override
    public PageResponse<VideoResponse> getPublishedVideos(int page, int size, String search, String email) {
        Pageable pageable = PaginationUtils.createPageRequest(page, size, "id");

        Page<Video> videoPage;
        if (search != null && !search.trim().isEmpty()) {
            videoPage = videoRepository.searchPublishedVideos(search.trim(), pageable);
        } else {
            videoPage = videoRepository.findPublishedVideos(pageable);
        }
        List<Video> videos = videoPage.getContent();

        Set<Long> watchListIds = Set.of();

        if (!videos.isEmpty()) {
            try {
                List<Long> videoIds = videos.stream().map(Video::getId).toList();
                watchListIds = userRepository.findWatchListVideoIds(email, videoIds);
            } catch (Exception e) {
                watchListIds = Set.of();
            }
        }

        Set<Long> finalWatchListIds = watchListIds;
        videos.forEach(video -> video.setIsInWatchlist(finalWatchListIds.contains(video.getId())));
        List<VideoResponse> videoResponses = videos.stream().map(VideoMapper::toDTO).toList();
        return PaginationUtils.toPageResponse(videoPage, videoResponses);
    }

    @Override
    public List<VideoResponse> getFeaturedVideos() {
        Pageable pageable = PageRequest.of(0, 5);
        List<Video> videos = videoRepository.findRandomPublishedVideos(pageable);
        return videos.stream().map(VideoMapper::toDTO).toList();
    }
}
