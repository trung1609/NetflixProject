package com.api.netflixbackend.repository;

import com.api.netflixbackend.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    @Query("select v from Video v where " +
            "lower(v.title) like lower(concat('%', :search, '%')) or " +
            "lower(v.description) like lower(concat('%', :search, '%') ) ")
    Page<Video> searchVideos(@Param("search") String search, Pageable pageable);

    @Query("select count(v) from Video v where v.published = true")
    long countPublishedVideos();

    @Query("select coalesce(sum(v.duration)) from Video v")
    long getTotalDuration();

    @Query("select v from Video v where v.published = true and (" +
            "lower(v.title) like lower(concat('%', :search, '%')) or " +
            " lower(v.description) like lower(concat('%', :search, '%') ) ) " +
            "order by v.createdAt desc ")
    Page<Video> searchPublishedVideos(@Param("search") String search, Pageable pageable);


    @Query("select v from Video v where v.published = true order by v.createdAt desc ")
    Page<Video> findPublishedVideos(Pageable pageable);

    @Query("select v from Video v where v.published = true order by function('RANDOM') ")
    List<Video> findRandomPublishedVideos(Pageable pageable);

    @Query("select v from User u join u.watchList v " +
            "where u.id = :userId and v.published = true and (" +
            "lower(v.title) like lower(concat('%', :search, '%') ) or " +
            "lower(v.description) like lower(concat('%', :search, '%') ) ) ")
    Page<Video> searchWatchlistByUserId(@Param("userId") Long id,@Param("search") String search, Pageable pageable);

    @Query("select v from User u join u.watchList v where u.id = :userId and v.published = true")
    Page<Video> findWatchlistByUserId(@Param("userId") Long id, Pageable pageable);
}
