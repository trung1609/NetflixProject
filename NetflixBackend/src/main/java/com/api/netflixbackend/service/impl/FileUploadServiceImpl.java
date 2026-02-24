package com.api.netflixbackend.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.api.netflixbackend.service.FileUploadService;
import com.api.netflixbackend.util.FileHandlerUtil;

import jakarta.annotation.PostConstruct;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    private Path videoStorageLocation;
    private Path imageStorageLocation;

    @Value("${file.uploads.video-dir:uploads/video}")
    String videoDir;

    @Value("${file.uploads.image-dir:uploads/image}")
    String imageDir;

    @PostConstruct
    public void init() {
        this.videoStorageLocation = Paths.get(videoDir).toAbsolutePath().normalize();
        this.imageStorageLocation = Paths.get(imageDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.videoStorageLocation);
            Files.createDirectories(this.imageStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.");
        }
    }

    @Override
    public String storeVideoFile(MultipartFile file) {
        return storeFile(file, videoStorageLocation);
    }

    @Override
    public String storeImageFile(MultipartFile file) {
        return storeFile(file, imageStorageLocation);
    }

    @Override
    public ResponseEntity<Resource> serveVideo(String uuid, String rangeHeader) {
        try {
            Path filePath = FileHandlerUtil.findFileByUuid(videoStorageLocation, uuid);
            Resource resource = FileHandlerUtil.createFullResource(filePath);
            String fileName = resource.getFilename();
            String contentType = FileHandlerUtil.detectVideoContentType(fileName);
            long fileLength = resource.contentLength();

            if (isFullContentRequest(rangeHeader)) {
                return buildFullVideoResponse(resource, contentType, fileLength, fileName);
            }

            return buildPartialVideoResponse(filePath, rangeHeader, contentType, fileName, fileLength);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<Resource> serveImage(String uuid) {
        try {
            Path filePath = FileHandlerUtil.findFileByUuid(imageStorageLocation, uuid);
            Resource resource = FileHandlerUtil.createFullResource(filePath);
            String fileName = resource.getFilename();
            String contentType = FileHandlerUtil.detectImageContentType(fileName);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline: fileName = \"" + fileName + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    private ResponseEntity<Resource> buildPartialVideoResponse(Path filePath, String rangeHeader, String contentType, String fileName, long fileLength) throws Exception {
        long[] range = FileHandlerUtil.parseRangeHeader(rangeHeader, fileLength);
        long rangeStart = range[0];
        long rangeEnd = range[1];
        if (!isValidRange(rangeStart, rangeEnd, fileLength)){
            return buildRangeNotSatisfiableResponse(fileLength);
        }
        long contentLength = rangeEnd - rangeStart + 1;

        Resource rangeResource = FileHandlerUtil.createRangeResource(filePath, rangeStart, rangeEnd);

        return ResponseEntity.status(206)
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline: fileName = \"" + fileName + "\"")
                .header(HttpHeaders.CONTENT_RANGE, "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength))
                .body(rangeResource);
    }

    private ResponseEntity<Resource> buildRangeNotSatisfiableResponse(long fileLength) {
        return ResponseEntity.status(416)
                .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileLength)
                .build();
    }

    private boolean isValidRange(long rangeStart, long rangeEnd, long fileLength) {
        return rangeStart <= rangeEnd && rangeStart >= 0 && rangeEnd < fileLength;
    }

    private ResponseEntity<Resource> buildFullVideoResponse(Resource resource, String contentType, long fileLength, String fileName) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline: fileName = \"" + fileName + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileLength))
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .body(resource);
    }

    private boolean isFullContentRequest(String rangeHeader) {
        return rangeHeader == null || rangeHeader.isEmpty();
    }

    private String storeFile(MultipartFile file, Path storageLocation) {
        String fileExtension = FileHandlerUtil.extractFileExtension(file.getOriginalFilename());
        String uuid = UUID.randomUUID().toString();
        String fileName = uuid + fileExtension;

        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file " + fileName);
            }

            Path targetLocation = storageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return uuid;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file " + fileName + ". Please try again!");
        }
    }

    @Override
    public boolean deleteVideoFile(String uuid) {
        return deleteFile(uuid, videoStorageLocation);
    }

    @Override
    public boolean deleteImageFile(String uuid) {
        return deleteFile(uuid, imageStorageLocation);
    }

    private boolean deleteFile(String uuid, Path storageLocation) {
        try {
            Path filePath = FileHandlerUtil.findFileByUuid(storageLocation, uuid);
            return Files.deleteIfExists(filePath);
        } catch (Exception e) {
            return false;
        }
    }
}
