import { ChangeDetectorRef, Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { Subject } from 'rxjs/internal/Subject';
import { ErrorHandlerService } from '../../shared/services/error-handler-service';
import { DialogService } from '../../shared/services/dialog-service';
import { MediaService } from '../../shared/services/media-service';
import { UtilityService } from '../../shared/services/utility-service';
import { NotificationService } from '../../shared/services/notification-service';
import { WatchlistService } from '../../shared/services/watchlist-service';
import { VideoService } from '../../shared/services/video-service';
import { debounceTime } from 'rxjs/internal/operators/debounceTime';
import { distinctUntilChanged } from 'rxjs/internal/operators/distinctUntilChanged';

@Component({
  selector: 'app-my-favorites',
  standalone: false,
  templateUrl: './my-favorites.html',
  styleUrl: './my-favorites.css',
})
export class MyFavorites implements OnInit, OnDestroy {
  allVideos: any = [];
  filteredVideos: any = [];
  loading = true;
  loadingMore = false;
  error = false;
  searchQuery: string = '';

  currentPage = 0;
  pageSize = 5;
  totalElements = 0;
  totalPages = 0;
  hasMoreVideos = true;

  private searchSubject = new Subject<string>();

  constructor(
    private videoService: VideoService,
    private watchlistService: WatchlistService,
    private notification: NotificationService,
    public utilityService: UtilityService,
    public mediaService: MediaService,
    private dialogService: DialogService,
    private errorHandlerService: ErrorHandlerService,
    private cdr: ChangeDetectorRef,
  ) { }

  ngOnInit(): void {
    this.loadVideos();
    this.initializeSearchDebounce();
  }

  ngOnDestroy(): void {
    this.searchSubject.complete();
  }

  initializeSearchDebounce(): void {
    this.searchSubject.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(() => {
      this.performSearch();
    });
  }

  @HostListener('window:scroll')
  onScroll(): void {
    const scrollPosition = window.pageYOffset + window.innerHeight;
    const pageHeight = document.documentElement.scrollHeight;

    if (
      scrollPosition >= pageHeight - 200 &&
      !this.loadingMore &&
      !this.loading &&
      this.hasMoreVideos
    ) {
      this.loadMoreVideos();
    }
  }

  loadMoreVideos() {
    if (this.loadingMore || !this.hasMoreVideos || !Number.isFinite(this.currentPage)) {
      return;
    }

    const nextPage = this.currentPage + 1;

    // Ngăn race condition: cập nhật ngay lập tức
    this.loadingMore = true;
    this.currentPage = nextPage;

    const search = this.searchQuery.trim() || undefined;

    this.watchlistService.getWatchlist(nextPage, this.pageSize, search).subscribe({
      next: (response: any) => {
        // Filter trùng video (do pagination có thể thay đổi sau khi remove)
        const existingIds = new Set(this.allVideos.map((v: any) => v.id));
        const newVideos = response.content.filter((v: any) => !existingIds.has(v.id));

        this.allVideos = [...this.allVideos, ...newVideos];
        this.filteredVideos = [...this.filteredVideos, ...newVideos];
        this.totalElements = response.totalElements;
        this.currentPage = Number.isFinite(response.number) ? response.number : nextPage;
        this.hasMoreVideos = this.allVideos.length < this.totalElements;
        this.loadingMore = false;
        this.cdr.markForCheck();
        console.log('loadMore:', 'loaded:', this.allVideos.length, 'total:', this.totalElements, 'hasMore:', this.hasMoreVideos);
      },
      error: (err) => {
        // Rollback currentPage khi có lỗi
        this.currentPage = this.currentPage - 1;
        this.loadingMore = false;
        this.cdr.markForCheck();
        this.errorHandlerService.handle(err, 'Failed to load more videos');
      },
    });
  }

  loadVideos(page: number = 0) {
    this.error = false;
    this.currentPage = 0;
    this.allVideos = [];
    this.filteredVideos = [];
    const search = this.searchQuery.trim() || undefined;
    this.loading = true;

    this.watchlistService.getWatchlist(page, this.pageSize, search).subscribe({
      next: (response: any) => {
        this.allVideos = response.content;
        this.filteredVideos = response.content;
        this.currentPage = Number.isFinite(response.number) ? response.number : this.currentPage;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        const loadCount = this.allVideos.length;
        this.hasMoreVideos = loadCount < response.totalElements;
        this.cdr.markForCheck();
        console.log('Has more videos:', this.hasMoreVideos);
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading videos:', err);
        this.error = true;
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }

  onSearch() {
    this.searchSubject.next(this.searchQuery);
  }

  private performSearch() {
    this.currentPage = 0;
    this.loadVideos();
  }

  clearSearch() {
    this.searchQuery = '';
    this.currentPage = 0;
    this.loadVideos();
  }

  toggleWatchlist(video: any, event?: Event) {
    if (event) {
      event.stopPropagation();
    }

    const videoId = video.id!;

    this.watchlistService.removeFromWatchlist(videoId).subscribe({
      next: () => {
        this.allVideos = this.allVideos.filter((v: any) => v.id !== videoId);
        this.filteredVideos = this.filteredVideos.filter((v: any) => v.id !== videoId);
        this.totalElements = Math.max(0, this.totalElements - 1);
        this.hasMoreVideos = this.allVideos.length < this.totalElements;
        this.notification.success('Removed from My Favorites');
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.errorHandlerService.handle(err, 'Failed to remove from My Favorites');
      }
    });
  }

  getPosterUrl(video: any) {
    return this.mediaService.getMediaUrl(video, 'image', {
      useCache: true,
    }) || '';
  }

  playVideo(video: any) {
    this.dialogService.openVideoPlayer(video);
  }

  formatDuration(seconds: number | undefined): string {
    return this.utilityService.formatDuration(seconds);
  }
}
