# Netflix Project - Full Stack Application

## 📋 Mục Lục
- [Tổng Quan Dự Án](#-tổng-quan-dự-án)
- [Công Nghệ Sử Dụng](#-công-nghệ-sử-dụng)
- [Cấu Trúc Dự Án](#-cấu-trúc-dự-án)
- [Hệ Thống Upload File](#-hệ-thống-upload-file)
  - [Kiến Trúc](#-kiến-trúc)
  - [Backend Implementation](#-backend-implementation)
  - [Frontend Implementation](#-frontend-implementation)
  - [Video Streaming với Range Request](#-video-streaming-với-range-request)
- [Cài Đặt và Chạy Dự Án](#-cài-đặt-và-chạy-dự-án)
- [API Documentation](#-api-documentation)
- [Troubleshooting](#-troubleshooting)

---

## 🎯 Tổng Quan Dự Án

Netflix Project là một ứng dụng web full-stack mô phỏng nền tảng streaming video, cho phép người dùng xem, quản lý và upload video/hình ảnh. Dự án được xây dựng với kiến trúc client-server, sử dụng Spring Boot cho backend và Angular cho frontend.

### Tính Năng Chính:
- 🎬 **Video Streaming**: Phát video với hỗ trợ Range Request (seek/skip)
- 📤 **Upload File**: Upload video và hình ảnh với progress tracking
- 🖼️ **Quản Lý Media**: CRUD operations cho video và hình ảnh
- 🔐 **Authentication & Authorization**: Bảo mật với JWT
- 👥 **User Management**: Quản lý người dùng và phân quyền
- 📧 **Email Service**: Gửi email thông báo

---

## 🛠️ Công Nghệ Sử Dụng

### Backend (NetflixBackend)
- **Framework**: Spring Boot 4.0.2
- **Language**: Java 17
- **Database**: PostgreSQL
- **ORM**: Hibernate/JPA
- **Security**: Spring Security + JWT (JJWT 0.13.0)
- **Build Tool**: Gradle
- **Email**: Spring Mail (SMTP Gmail)

### Frontend (NetflixFrontend)
- **Framework**: Angular
- **Language**: TypeScript
- **UI Library**: Angular Material
- **HTTP Client**: Angular HttpClient với Progress Tracking

---

## 📁 Cấu Trúc Dự Án

```
NetflixProject/
├── NetflixBackend/           # Spring Boot Backend
│   ├── src/main/java/com/api/netflixbackend/
│   │   ├── controller/       # REST Controllers
│   │   │   └── FileUploadController.java
│   │   ├── service/          # Business Logic
│   │   │   ├── FileUploadService.java
│   │   │   └── impl/FileUploadServiceImpl.java
│   │   ├── util/             # Utility Classes
│   │   │   └── FileHandlerUtil.java
│   │   ├── model/            # Entity Classes
│   │   ├── repository/       # JPA Repositories
│   │   └── config/           # Configuration Classes
│   ├── src/main/resources/
│   │   └── application.yaml  # Configuration
│   └── build.gradle          # Dependencies
│
├── NetflixFrontend/          # Angular Frontend
│   ├── src/app/
│   │   ├── admin/            # Admin Components
│   │   │   └── dialog/
│   │   │       └── manage-video/
│   │   ├── shared/
│   │   │   └── services/
│   │   │       └── media-service.ts
│   │   ├── app.ts            # Root Component
│   │   └── app-routing-module.ts
│   └── package.json
│
└── uploads/                  # File Storage
    ├── videos/               # Video files
    └── images/               # Image files
```

---

## 📤 Hệ Thống Upload File

Hệ thống upload file được thiết kế để xử lý video lớn (lên đến 10GB) và hình ảnh, với khả năng streaming video hiệu quả thông qua Range Request.

### 🏗️ Kiến Trúc

```
┌─────────────┐         ┌──────────────┐         ┌─────────────┐
│   Angular   │────────▶│  Spring Boot │────────▶│ File System │
│   Frontend  │         │   Backend    │         │  (uploads/) │
└─────────────┘         └──────────────┘         └─────────────┘
      │                        │
      │                        │
   Upload                   Process
  Progress                  & Store
   Tracking                with UUID
```

### 🔄 Quy Trình Upload

1. **Client**: Chọn file (video/image) → Tạo FormData → Gửi POST request
2. **Server**: Nhận file → Validate → Generate UUID → Lưu với tên UUID.extension
3. **Response**: Trả về UUID, fileName, size
4. **Storage**: File được lưu tại `uploads/videos/` hoặc `uploads/images/`

---

## 🖥️ Backend Implementation

### 1️⃣ Configuration (application.yaml)

```yaml
spring:
  servlet:
    multipart:
      enabled: true
      max-file-size: 10GB      # Max file size
      max-request-size: 10GB   # Max request size

server:
  tomcat:
    max-swallow-size: -1       # Unlimited
    max-http-form-post-size: -1

file:
  uploads:
    video-dir: uploads/videos  # Video storage path
    image-dir: uploads/images  # Image storage path
```

**Giải thích các tham số:**
- `max-file-size`: Kích thước tối đa của một file đơn lẻ
- `max-request-size`: Kích thước tối đa của toàn bộ request (có thể chứa nhiều file)
- `max-swallow-size: -1`: Không giới hạn kích thước body có thể "swallow" (bỏ qua)
- `max-http-form-post-size: -1`: Không giới hạn kích thước form POST

### 2️⃣ Controller Layer (FileUploadController.java)

```java
@RestController
@RequestMapping("/api/files")
public class FileUploadController {
    
    @Autowired
    private FileUploadService fileUploadService;

    // Upload Video
    @PostMapping("/upload/video")
    public ResponseEntity<Map<String, String>> uploadVideo(
        @RequestParam("file") MultipartFile file) {
        String uuid = fileUploadService.storeVideoFile(file);
        return ResponseEntity.ok(buildUploadResponse(uuid, file));
    }

    // Upload Image
    @PostMapping("/upload/image")
    public ResponseEntity<Map<String, String>> uploadImage(
        @RequestParam("file") MultipartFile file) {
        String uuid = fileUploadService.storeImageFile(file);
        return ResponseEntity.ok(buildUploadResponse(uuid, file));
    }

    // Serve Video với Range Request Support
    @GetMapping("/video/{uuid}")
    public ResponseEntity<Resource> serveVideo(
        @PathVariable String uuid,
        @RequestHeader(value = "Range", required = false) String rangeHeader) {
        return fileUploadService.serveVideo(uuid, rangeHeader);
    }

    // Serve Image
    @GetMapping("/image/{uuid}")
    public ResponseEntity<Resource> serveImage(@PathVariable String uuid) {
        return fileUploadService.serveImage(uuid);
    }

    // Delete Video
    @DeleteMapping("/video/{uuid}")
    public ResponseEntity<Map<String, Object>> deleteVideo(
        @PathVariable String uuid) {
        boolean deleted = fileUploadService.deleteVideoFile(uuid);
        return buildDeleteResponse(deleted);
    }

    // Delete Image
    @DeleteMapping("/image/{uuid}")
    public ResponseEntity<Map<String, Object>> deleteImage(
        @PathVariable String uuid) {
        boolean deleted = fileUploadService.deleteImageFile(uuid);
        return buildDeleteResponse(deleted);
    }
}
```

**Giải thích:**
- `@RequestParam("file") MultipartFile file`: Nhận file từ FormData
- `Range header`: Cho phép video player request một phần của file (seek/skip)
- `@PathVariable String uuid`: Sử dụng UUID để identify file
- Response trả về thông tin file (uuid, fileName, size)

### 3️⃣ Service Layer (FileUploadServiceImpl.java)

#### 📝 Initialization

```java
@Service
public class FileUploadServiceImpl implements FileUploadService {
    
    private Path videoStorageLocation;
    private Path imageStorageLocation;

    @Value("${file.uploads.video-dir:uploads/videos}")
    String videoDir;

    @Value("${file.uploads.image-dir:uploads/images}")
    String imageDir;

    @PostConstruct
    public void init() {
        // Chuyển đổi path thành absolute path
        this.videoStorageLocation = Paths.get(videoDir)
            .toAbsolutePath().normalize();
        this.imageStorageLocation = Paths.get(imageDir)
            .toAbsolutePath().normalize();

        try {
            // Tạo thư mục nếu chưa tồn tại
            Files.createDirectories(this.videoStorageLocation);
            Files.createDirectories(this.imageStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException(
                "Could not create upload directories.");
        }
    }
}
```

**Giải thích:**
- `@PostConstruct`: Method được gọi sau khi bean được khởi tạo
- `toAbsolutePath().normalize()`: Chuẩn hóa path (xử lý `..`, `.`, etc.)
- `Files.createDirectories()`: Tạo tất cả parent directories nếu cần

#### 💾 Store File Logic

```java
private String storeFile(MultipartFile file, Path storageLocation) {
    // 1. Extract file extension
    String fileExtension = FileHandlerUtil
        .extractFileExtension(file.getOriginalFilename());
    
    // 2. Generate UUID
    String uuid = UUID.randomUUID().toString();
    String fileName = uuid + fileExtension;

    try {
        // 3. Validate file
        if (file.isEmpty()) {
            throw new RuntimeException("Empty file: " + fileName);
        }

        // 4. Save file
        Path targetLocation = storageLocation.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, 
            StandardCopyOption.REPLACE_EXISTING);
        
        return uuid;
    } catch (IOException ex) {
        throw new RuntimeException("Failed to store file: " + fileName);
    }
}
```

**Quy trình lưu file:**
1. **Extract Extension**: Lấy phần mở rộng từ tên file gốc (`.mp4`, `.jpg`, etc.)
2. **Generate UUID**: Tạo UUID duy nhất để tránh trùng lặp tên file
3. **Validate**: Kiểm tra file không rỗng
4. **Copy**: Copy file từ InputStream vào storage location
5. **Return UUID**: Trả về UUID để client lưu vào database

**Lý do sử dụng UUID:**
- ✅ Tránh trùng lặp tên file
- ✅ Bảo mật (không để lộ tên file gốc)
- ✅ Dễ dàng tìm kiếm và quản lý
- ✅ Hỗ trợ distributed system

#### 🎬 Video Serving với Range Request

```java
@Override
public ResponseEntity<Resource> serveVideo(String uuid, String rangeHeader) {
    try {
        // 1. Find file by UUID
        Path filePath = FileHandlerUtil.findFileByUuid(
            videoStorageLocation, uuid);
        Resource resource = FileHandlerUtil.createFullResource(filePath);
        
        // 2. Get file metadata
        String fileName = resource.getFilename();
        String contentType = FileHandlerUtil
            .detectVideoContentType(fileName);
        long fileLength = resource.contentLength();

        // 3. Check if full content or partial content request
        if (isFullContentRequest(rangeHeader)) {
            return buildFullVideoResponse(
                resource, contentType, fileLength, fileName);
        }

        // 4. Handle Range Request (206 Partial Content)
        return buildPartialVideoResponse(
            filePath, rangeHeader, contentType, fileName, fileLength);
    } catch (Exception e) {
        return ResponseEntity.notFound().build();
    }
}
```

**Range Request Flow:**

```
Client Request:
GET /api/files/video/abc-123
Range: bytes=0-1023999

Server Response (206 Partial Content):
Content-Type: video/mp4
Content-Range: bytes 0-1023999/50000000
Content-Length: 1024000
Accept-Ranges: bytes
[Video Data Chunk]
```

#### 📦 Build Partial Response (206)

```java
private ResponseEntity<Resource> buildPartialVideoResponse(
    Path filePath, String rangeHeader, String contentType, 
    String fileName, long fileLength) throws Exception {
    
    // 1. Parse Range header
    long[] range = FileHandlerUtil.parseRangeHeader(
        rangeHeader, fileLength);
    long rangeStart = range[0];
    long rangeEnd = range[1];
    
    // 2. Validate range
    if (!isValidRange(rangeStart, rangeEnd, fileLength)) {
        return buildRangeNotSatisfiableResponse(fileLength);
    }
    
    // 3. Calculate content length
    long contentLength = rangeEnd - rangeStart + 1;
    
    // 4. Create range resource
    Resource rangeResource = FileHandlerUtil.createRangeResource(
        filePath, rangeStart, rangeEnd);
    
    // 5. Build response
    return ResponseEntity.status(206) // 206 Partial Content
        .contentType(MediaType.parseMediaType(contentType))
        .header(HttpHeaders.CONTENT_DISPOSITION, 
            "inline: fileName = \"" + fileName + "\"")
        .header(HttpHeaders.CONTENT_RANGE, 
            "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength)
        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
        .header(HttpHeaders.CONTENT_LENGTH, 
            String.valueOf(contentLength))
        .body(rangeResource);
}
```

**HTTP Headers giải thích:**
- `Status 206`: Partial Content - server đang trả về một phần của resource
- `Content-Range`: Chỉ định byte range đang được trả về và tổng size
- `Accept-Ranges: bytes`: Server hỗ trợ range requests
- `Content-Length`: Kích thước của chunk đang được trả về

#### 🔧 Utility Class (FileHandlerUtil.java)

**1. Parse Range Header**

```java
public static long[] parseRangeHeader(String rangeHeader, long fileLength) {
    // Input: "bytes=1000-2000" hoặc "bytes=1000-"
    String[] ranges = rangeHeader.replace("bytes=", "").split("-");
    
    long rangeStart = Long.parseLong(ranges[0]);
    
    // Nếu không có end, lấy đến hết file
    long rangeEnd = ranges.length > 1 && !ranges[1].isEmpty() 
        ? Long.parseLong(ranges[1]) 
        : fileLength - 1;
    
    return new long[]{rangeStart, rangeEnd};
}
```

**2. Create Range Resource**

```java
public static Resource createRangeResource(
    Path filePath, long rangeStart, long rangeLength) throws Exception {
    
    RandomAccessFile fileReader = new RandomAccessFile(
        filePath.toFile(), "r");
    fileReader.seek(rangeStart); // Nhảy đến vị trí start

    InputStream partialContentStream = new InputStream() {
        private long totalBytesRead = 0;

        @Override
        public int read(byte[] buffer, int offset, int length) 
            throws IOException {
            
            if (totalBytesRead >= rangeLength) {
                fileReader.close();
                return -1; // End of stream
            }

            long remainingBytes = rangeLength - totalBytesRead;
            int bytesToRead = (int) Math.min(length, remainingBytes);

            int bytesActuallyRead = fileReader.read(
                buffer, offset, bytesToRead);

            if (bytesActuallyRead > 0) {
                totalBytesRead += bytesActuallyRead;
            }

            if (totalBytesRead >= rangeLength) {
                fileReader.close();
            }

            return bytesActuallyRead;
        }
    };

    return new InputStreamResource(partialContentStream) {
        @Override
        public long contentLength() {
            return rangeLength;
        }
    };
}
```

**Giải thích:**
- `RandomAccessFile`: Cho phép đọc file từ bất kỳ vị trí nào
- `seek(rangeStart)`: Di chuyển file pointer đến byte bắt đầu
- Custom `InputStream`: Đọc đúng số byte được yêu cầu, tự động đóng file khi đủ
- `InputStreamResource`: Wrap InputStream thành Spring Resource

**3. Detect Content Type**

```java
public static String detectVideoContentType(String fileName) {
    if (fileName == null) return "video/mp4";
    if (fileName.endsWith(".webm")) return "video/webm";
    if (fileName.endsWith(".ogg")) return "video/ogg";
    if (fileName.endsWith(".mkv")) return "video/x-matroska";
    if (fileName.endsWith(".avi")) return "video/x-msvideo";
    // ... more formats
    return "video/mp4"; // Default
}
```

**Tại sao quan trọng:**
- Browser cần biết content type để render đúng
- Ảnh hưởng đến cách browser cache và xử lý file
- Hỗ trợ nhiều định dạng video/image khác nhau

---

## 🌐 Frontend Implementation

### 1️⃣ Media Service (media-service.ts)

```typescript
@Injectable({ providedIn: 'root' })
export class MediaService {
  private apiUrl = environment.apiUrl + '/files';

  uploadFile(file: File): Observable<{ 
    progress: number; 
    uuid?: string 
  }> {
    // 1. Create FormData
    const formData = new FormData();
    formData.append('file', file);

    // 2. Determine upload URL
    const isVideo = file.type.startsWith('video/');
    const uploadUrl = isVideo 
      ? `${this.apiUrl}/upload/video` 
      : `${this.apiUrl}/upload/image`;

    // 3. Create HTTP Request with progress tracking
    const req = new HttpRequest('POST', uploadUrl, formData, {
      reportProgress: true, // Enable progress events
    });

    // 4. Send request and transform events
    return this.http.request(req).pipe(
      map((event) => {
        // Upload progress event
        if (event.type === HttpEventType.UploadProgress) {
          const progress = Math.round(
            (100 * event.loaded) / (event.total || 1)
          );
          return { progress };
        } 
        // Response event (upload complete)
        else if (event.type === HttpEventType.Response) {
          const body = event.body as any;
          return { progress: 100, uuid: body?.uuid || '' };
        }
        return { progress: 0 };
      }),
    );
  }

  getMediaUrl(
    mediaValue: any,
    type: 'image' | 'video',
    options?: { useCache?: boolean }
  ): string | null {
    if (!mediaValue) return null;
    
    // If already a full URL, return as is
    if (typeof mediaValue === 'string' && 
        mediaValue.startsWith('http')) {
      return mediaValue;
    }
    
    // Build URL from UUID
    const endpoint = type === 'video' ? 'video' : 'image';
    return `${this.apiUrl}/${endpoint}/${mediaValue}`;
  }
}
```

**HttpEventType Events:**
- `Sent (0)`: Request được gửi
- `UploadProgress (1)`: Đang upload, cung cấp loaded/total bytes
- `ResponseHeader (2)`: Nhận response headers
- `DownloadProgress (3)`: Đang download response
- `Response (4)`: Response hoàn chỉnh

### 2️⃣ Component Usage (manage-video.ts)

```typescript
export class ManageVideo implements OnInit {
  uploadProgress = 0;
  videoPreviewUrl: string | null = null;

  onVideoPicked(ev: Event) {
    const file = (ev.target as HTMLInputElement).files?.[0];
    if (!file) return;

    // Validate file type
    if (!file.type.startsWith('video/')) {
      this.notification.error('Please select a valid video file');
      return;
    }

    // Create local preview URL
    const localBlobUrl = URL.createObjectURL(file);
    this.videoPreviewUrl = localBlobUrl;

    // Upload with progress tracking
    this.uploadProgress = 0;
    this.mediaService.uploadFile(file).subscribe({
      next: ({ progress, uuid }) => {
        this.uploadProgress = progress;
        
        // Upload complete
        if (uuid) {
          this.videoForm.patchValue({ src: uuid });
          this.notification.success('Video uploaded successfully');
        }
      },
      error: (err) => {
        this.notification.error('Failed to upload video');
        this.uploadProgress = 0;
        
        // Clean up preview URL
        if (this.videoPreviewUrl === localBlobUrl) {
          URL.revokeObjectURL(localBlobUrl);
          this.videoPreviewUrl = null;
        }
      }
    });
  }
}
```

**Key Points:**
- `URL.createObjectURL()`: Tạo local preview trước khi upload
- `URL.revokeObjectURL()`: Giải phóng memory khi không cần preview
- Real-time progress tracking với `uploadProgress`
- Store UUID trong form sau khi upload thành công

### 3️⃣ HTML Template

```html
<!-- File Input -->
<input 
  type="file" 
  accept="video/*"
  (change)="onVideoPicked($event)"
  #videoInput
/>

<!-- Upload Progress Bar -->
<mat-progress-bar 
  *ngIf="uploadProgress > 0 && uploadProgress < 100"
  mode="determinate" 
  [value]="uploadProgress"
></mat-progress-bar>

<!-- Video Preview -->
<video 
  *ngIf="videoPreviewUrl"
  [src]="videoPreviewUrl"
  controls
></video>

<!-- Video Player (after upload) -->
<video 
  *ngIf="videoForm.get('src')?.value"
  [src]="mediaService.getMediaUrl(videoForm.get('src')?.value, 'video')"
  controls
></video>
```

---

## 🎥 Video Streaming với Range Request

### Tại Sao Cần Range Request?

**Vấn đề khi không có Range Request:**
- ❌ Phải download toàn bộ video trước khi play
- ❌ Không thể seek/skip đến giữa video
- ❌ Tốn bandwidth và thời gian chờ
- ❌ Không thể play video lớn trên thiết bị có RAM hạn chế

**Lợi ích của Range Request:**
- ✅ Play video ngay lập tức (chỉ cần chunk đầu)
- ✅ Seek/skip mượt mà (request chunk tại vị trí cần)
- ✅ Tiết kiệm bandwidth (chỉ download phần đang xem)
- ✅ Better user experience

### 🔄 Range Request Flow

```
1. Initial Request (Load page)
   Browser → GET /api/files/video/abc-123
   Range: bytes=0-1048575 (request 1MB đầu)

2. Server Response
   ← 206 Partial Content
   Content-Range: bytes 0-1048575/50000000
   [First 1MB of video]

3. User Seeks to 01:30
   Browser → GET /api/files/video/abc-123
   Range: bytes=15000000-16048575

4. Server Response
   ← 206 Partial Content
   Content-Range: bytes 15000000-16048575/50000000
   [Chunk at 01:30 position]

5. Continue Playing
   Browser tự động request tiếp các chunks kế tiếp
```

### 📊 Performance Comparison

| Metric | Without Range Request | With Range Request |
|--------|----------------------|-------------------|
| Time to First Byte | Sau khi download hết | ~100ms |
| Memory Usage | ~500MB (video size) | ~5-10MB (buffer) |
| Seek Time | Không thể | ~200ms |
| Bandwidth | 500MB | ~50MB (chỉ xem 10%) |

### 🧪 Testing Range Request

**Dùng cURL:**
```bash
# Request first 1MB
curl -H "Range: bytes=0-1048575" \
  http://localhost:8080/api/files/video/abc-123 \
  -o output.mp4

# Request from 10MB to 11MB
curl -H "Range: bytes=10485760-11534335" \
  http://localhost:8080/api/files/video/abc-123 \
  -o output-chunk.mp4
```

**Dùng Browser DevTools:**
1. Mở video trong browser
2. F12 → Network tab
3. Observe multiple requests với Range headers
4. Seek video → Xem request mới với Range khác

---

## 🚀 Cài Đặt và Chạy Dự Án

### Prerequisites
- ☕ Java 17+
- 🐘 PostgreSQL
- 📦 Node.js 18+ & npm
- 🔧 Gradle (hoặc dùng wrapper)

### Backend Setup

```bash
# 1. Clone project
git clone <repository-url>
cd NetflixProject/NetflixBackend

# 2. Configure database (application.yaml)
# Update PostgreSQL credentials

# 3. Create database
psql -U postgres
CREATE DATABASE netflix;
\q

# 4. Create upload directories
mkdir -p uploads/videos uploads/images

# 5. Build project
./gradlew build

# 6. Run application
./gradlew bootRun

# Server running at http://localhost:8080
```

### Frontend Setup

```bash
# 1. Navigate to frontend
cd ../NetflixFrontend

# 2. Install dependencies
npm install

# 3. Configure API URL (environment.ts)
# Update apiUrl if needed

# 4. Run development server
ng serve

# Application running at http://localhost:4200
```

---

## 📚 API Documentation

### Upload Endpoints

#### Upload Video
```http
POST /api/files/upload/video
Content-Type: multipart/form-data

Parameters:
- file: Video file (max 10GB)

Response:
{
  "uuid": "abc-123-def-456",
  "fileName": "video.mp4",
  "size": "52428800"
}
```

#### Upload Image
```http
POST /api/files/upload/image
Content-Type: multipart/form-data

Parameters:
- file: Image file

Response:
{
  "uuid": "xyz-789-uvw-012",
  "fileName": "poster.jpg",
  "size": "2048000"
}
```

### Serve Endpoints

#### Get Video (with Range Support)
```http
GET /api/files/video/{uuid}
Headers:
- Range: bytes=start-end (optional)

Response (without Range):
Status: 200 OK
Content-Type: video/mp4
Content-Length: 52428800
Accept-Ranges: bytes
[Full video data]

Response (with Range):
Status: 206 Partial Content
Content-Type: video/mp4
Content-Range: bytes 0-1048575/52428800
Content-Length: 1048576
Accept-Ranges: bytes
[Partial video data]
```

#### Get Image
```http
GET /api/files/image/{uuid}

Response:
Status: 200 OK
Content-Type: image/jpeg
Content-Length: 2048000
[Image data]
```

### Delete Endpoints

#### Delete Video
```http
DELETE /api/files/video/{uuid}

Response:
{
  "success": true,
  "message": "File deleted successfully"
}
```

#### Delete Image
```http
DELETE /api/files/image/{uuid}

Response:
{
  "success": true,
  "message": "File deleted successfully"
}
```

---

## 🔒 Security Considerations

### Current Implementation
- ✅ UUID-based file naming (không để lộ tên file gốc)
- ✅ File type validation
- ✅ Size limits (10GB)
- ✅ JWT authentication (đã implement trong project)

### Recommendations
- 🔐 Add file scanning for malware
- 🔐 Validate file content (không chỉ extension)
- 🔐 Rate limiting cho upload endpoints
- 🔐 Implement file access permissions
- 🔐 Add CORS configuration
- 🔐 Encrypt sensitive files

---

## 🐛 Troubleshooting

### Issue: Upload fails với "Maximum upload size exceeded"
**Solution:** Kiểm tra cấu hình trong `application.yaml`:
```yaml
spring.servlet.multipart.max-file-size: 10GB
spring.servlet.multipart.max-request-size: 10GB
```

### Issue: Video không play được
**Solution:** 
- Kiểm tra content type detection
- Verify Range header support
- Check browser console for errors

### Issue: File không tìm thấy (404)
**Solution:**
- Verify UUID chính xác
- Check file tồn tại trong uploads directory
- Review path configuration

### Issue: Slow upload speed
**Solution:**
- Tăng timeout settings
- Check network bandwidth
- Consider chunked upload implementation

### Issue: CORS errors
**Solution:**
- Add CORS configuration trong Spring Boot
- Verify frontend URL được allow
- Check preflight OPTIONS requests

---

## 📈 Performance Optimization

### Backend
- ✅ Use `@Async` for large file processing
- ✅ Implement file compression
- ✅ Add Redis caching for frequently accessed files
- ✅ Use CDN for static content delivery
- ✅ Implement pagination for file listings

### Frontend
- ✅ Implement lazy loading
- ✅ Add image optimization
- ✅ Use service workers for offline support
- ✅ Implement pagination for large file lists
- ✅ Add debouncing for search/filter operations

---

## 👥 Contributors

- Vũ Minh Trung - Me
---

## 🙏 Acknowledgments

- Spring Boot Documentation
- Angular Documentation
- HTTP Range Request Specification (RFC 7233)
- Material Design Guidelines

---
