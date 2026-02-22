package com.api.netflixbackend.controller;

import com.api.netflixbackend.dto.response.MessageResponse;
import com.api.netflixbackend.dto.response.PageResponse;
import com.api.netflixbackend.dto.response.VideoResponse;
import com.api.netflixbackend.service.WatchlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {
    @Autowired
    private WatchlistService watchlistService;

    @PostMapping("/{videoId}")
    public ResponseEntity<MessageResponse> addToWatchlist(@PathVariable Long videoId, Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(watchlistService.addToWatchlist(videoId, email));
    }

    @DeleteMapping("/{videoId}")
    public ResponseEntity<MessageResponse> deleteFromWatchlist(@PathVariable Long videoId, Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(watchlistService.deleteFromWatchlist(videoId, email));
    }

    @GetMapping
    public ResponseEntity<PageResponse<VideoResponse>> getWatchlist(@RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "10") int size,
                                                                    @RequestParam(required = false) String search,
                                                                    Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(watchlistService.getWatchlist(email, page, size, search));
    }
}
