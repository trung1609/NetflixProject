package com.api.netflixbackend.service.impl;

import com.api.netflixbackend.dto.response.MessageResponse;
import com.api.netflixbackend.dto.response.PageResponse;
import com.api.netflixbackend.dto.response.VideoResponse;
import com.api.netflixbackend.entity.User;
import com.api.netflixbackend.entity.Video;
import com.api.netflixbackend.mapper.VideoMapper;
import com.api.netflixbackend.repository.UserRepository;
import com.api.netflixbackend.repository.VideoRepository;
import com.api.netflixbackend.service.WatchlistService;
import com.api.netflixbackend.util.PaginationUtils;
import com.api.netflixbackend.util.ServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class WatchlistServiceImpl implements WatchlistService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private ServiceUtils serviceUtils;

    @Override
    public MessageResponse addToWatchlist(Long videoId, String email) {

        User user = serviceUtils.getUserByEmailOrThrow(email);

        Video video = serviceUtils.getVideoByIdOrThrow(videoId);

        user.addToWatchList(video);
        userRepository.save(user);
        return new MessageResponse("Video added to watchlist successfully!");
    }

    @Override
    public MessageResponse deleteFromWatchlist(Long videoId, String email) {

        User user = serviceUtils.getUserByEmailOrThrow(email);

        Video video = serviceUtils.getVideoByIdOrThrow(videoId);

        user.removeFromWatchList(video);
        userRepository.save(user);
        return new MessageResponse("Video removed from watchlist successfully!");

    }

    @Override
    public PageResponse<VideoResponse> getWatchlist(String email, int page, int size, String search) {
        User user = serviceUtils.getUserByEmailOrThrow(email);
        Pageable pageable = PaginationUtils.createPageRequest(page, size);
        Page<Video> videoPage;

        if (search != null && !search.trim().isEmpty()) {
            videoPage = videoRepository.searchWatchlistByUserId(user.getId(), search.trim(), pageable);
        } else {
            videoPage = videoRepository.findWatchlistByUserId(user.getId(), pageable);
        }

        return PaginationUtils.toPageResponse(videoPage, VideoMapper::toDTO);
    }
}
