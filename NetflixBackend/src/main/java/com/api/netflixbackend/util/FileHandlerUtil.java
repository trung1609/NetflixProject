package com.api.netflixbackend.util;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileHandlerUtil {
    private FileHandlerUtil() {
    }

    public static String extractFileExtension(String originalFileName) {
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return fileExtension;
    }

    public static Path findFileByUuid(Path directory, String uuid) throws Exception {
        return Files.list(directory)
                .filter(path -> path.getFileName().toString().startsWith(uuid))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("File not found with uuid: " + uuid));
    }

    public static String detectVideoContentType(String fileName) {
        if (fileName == null) {
            return "video/mp4";
        }
        if (fileName.endsWith(".webm")) return "video/webm";
        if (fileName.endsWith(".ogg")) return "video/ogg";
        if (fileName.endsWith(".mkv")) return "video/x-matroska";
        if (fileName.endsWith(".avi")) return "video/x-msvideo";
        if (fileName.endsWith(".mov")) return "video/quicktime";
        if (fileName.endsWith(".flv")) return "video/x-flv";
        if (fileName.endsWith(".wmv")) return "video/x-ms-wmv";
        if (fileName.endsWith(".m4v")) return "video/x-m4v";
        if (fileName.endsWith(".3gp")) return "video/3gpp";
        if (fileName.endsWith(".mpg") || fileName.endsWith(".mpeg")) return "video/mpeg";
        return "video/mp4";

    }

    public static String detectImageContentType(String fileName) {
        if (fileName == null) {
            return "image/jpeg";
        }
        if (fileName.endsWith(".gif")) return "image/gif";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }

    public static long[] parseRangeHeader(String rangeHeader, long fileLength) {
        String[] ranges = rangeHeader.replace("bytes=", "").split("-");
        long rangeStart = Long.parseLong(ranges[0]);
        long rangeEnd = ranges.length > 1 &&
                !ranges[1].isEmpty() ? Long.parseLong(ranges[1]) : fileLength - 1;

        return new long[]{rangeStart, rangeEnd};
    }

    public static Resource createRangeResource(Path filePath, long rangeStart, long rangeLength) throws Exception {
        RandomAccessFile fileReader = new RandomAccessFile(filePath.toFile(), "r");
        fileReader.seek(rangeStart);

        InputStream partialContentStream =
                new InputStream() {
                    private long totalBytesRead = 0;

                    @Override
                    public int read() throws IOException {
                        if (totalBytesRead >= rangeLength) {
                            fileReader.close();
                            return -1;
                        }

                        totalBytesRead++;
                        return fileReader.read();
                    }

                    @Override
                    public int read(byte[] buffer, int offset, int length) throws IOException {
                        if (totalBytesRead >= rangeLength) {
                            fileReader.close();
                            return -1;
                        }

                        long remainingBytes = rangeLength - totalBytesRead;
                        int bytesToRead = (int) Math.min(length, remainingBytes);

                        int bytesActuallyRead = fileReader.read(buffer, offset, bytesToRead);

                        if (bytesActuallyRead > 0) {
                            totalBytesRead += bytesActuallyRead;
                        }

                        if (totalBytesRead >= rangeLength) {
                            fileReader.close();
                        }

                        return bytesActuallyRead;
                    }

                    @Override
                    public void close() throws IOException {
                        fileReader.close();
                    }
                };

        return new InputStreamResource(partialContentStream) {
            @Override
            public long contentLength() throws IOException {
                return rangeLength;
            }
        };
    }

    public static Resource createFullResource(Path filePath) throws Exception {
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("File not found or not readable: " + filePath);
        }
        return resource;
    }
}
