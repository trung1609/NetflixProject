package com.api.netflixbackend.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    String storeVideoFile(MultipartFile file);

    String storeImageFile(MultipartFile file);

    ResponseEntity<Resource> serveVideo(String uuid, String rangeHeader);

    ResponseEntity<Resource> serveImage(String uuid);
<<<<<<< HEAD
=======

    boolean deleteVideoFile(String uuid);

    boolean deleteImageFile(String uuid);
>>>>>>> 9b57bb3ee5be4c70dc2f41e9fb97c5f428abcd59
}
