package com.api.netflixbackend.service;

import com.api.netflixbackend.dto.response.MessageResponse;
import com.api.netflixbackend.dto.response.PageResponse;
import com.api.netflixbackend.dto.response.VideoResponse;

public interface WatchlistService {
    MessageResponse addToWatchlist(Long videoId, String email);

    MessageResponse deleteFromWatchlist(Long videoId, String email);

    PageResponse<VideoResponse> getWatchlist(String email, int page, int size, String search);
}
