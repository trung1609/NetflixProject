import { Component, ElementRef, Inject, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { UtilityService } from '../../services/utility-service';
import { MediaService } from '../../services/media-service';

@Component({
  selector: 'app-video-player',
  standalone: false,
  templateUrl: './video-player.html',
  styleUrl: './video-player.css',
})
<<<<<<< HEAD
export class VideoPlayer {
  
=======
export class VideoPlayer implements OnInit, OnDestroy {
  @ViewChild('videoPlayer', { static: false }) videoElement!: ElementRef<HTMLVideoElement>;

  isPlaying = false;
  currentTime = 0;
  duration = 0;
  volume = 1;
  isMuted = false;
  isFullscreen = false;
  showControls = true;
  controlsTimeout: any;
  private boundFullscreenHandler: any;
  private boundKeydownHandler: any;

  authenticatedVideoUrl: string | null = null;

  // constructor
  constructor(
    public dialogRef: MatDialogRef<VideoPlayer>,
    @Inject(MAT_DIALOG_DATA) public video: any,
    public utilityService: UtilityService,
    private mediaService: MediaService,
  ) {
    this.boundFullscreenHandler = this.onFullscreenChange.bind(this);
    this.boundKeydownHandler = this.onKeydown.bind(this);

    this.loadAuthenticatedVideo();
  }

  // Lifecycle hooks

  ngOnInit(): void {
    this.startControlsTimer();

    document.addEventListener('fullscreenchange', this.boundFullscreenHandler);
    document.addEventListener('keydown', this.boundKeydownHandler);

    this.dialogRef.beforeClosed().subscribe(() => {
      this.cleanup();
    });
  }

  ngOnDestroy(): void {
    this.cleanup();
  }
  // Initialization and cleanup
  private loadAuthenticatedVideo(): void {
    this.authenticatedVideoUrl = this.mediaService.getMediaUrl(this.video.src, 'video');
  }

  private cleanup() {
    if (this.controlsTimeout) {
      clearTimeout(this.controlsTimeout);
      this.controlsTimeout = null;
    }

    document.removeEventListener('fullscreenchange', this.boundFullscreenHandler);
    document.removeEventListener('keydown', this.boundKeydownHandler);

    if (this.videoElement?.nativeElement) {
      const video = this.videoElement.nativeElement;
      video.pause();
      video.currentTime = 0;
      video.src = '';
      video.load();
      this.isPlaying = false;
    }

    if (document.fullscreenElement) {
      document.exitFullscreen().catch(() => {});
    }
  }

  // Event handlers

  onKeydown(event: KeyboardEvent) {
    if (event.target instanceof HTMLInputElement || event.target instanceof HTMLTextAreaElement) {
      return;
    }

    switch (event.key.toLowerCase()) {
      case ' ':
      case 'k':
        event.preventDefault();
        this.togglePlay();
        break;
      case 'arrowleft':
        event.preventDefault();
        this.seekBackward();
        break;
      case 'arrowright':
        event.preventDefault();
        this.seekForward();
        break;
      case 'arrowup':
        event.preventDefault();
        this.increaseVolume();
        break;
      case 'arrowdown':
        event.preventDefault();
        this.decreaseVolume();
        break;
      case 'm':
        event.preventDefault();
        this.toggleMute();
        break;
      case 'f':
        event.preventDefault();
        this.toggleFullscreen();
        break;
      case 'escape':
        if (document.fullscreenElement) {
          event.preventDefault();
          document.exitFullscreen();
        } else {
          this.closePlayer();
        }
        break;
    }
  }

  onFullscreenChange() {
    this.isFullscreen = !!document.fullscreenElement;
  }

  onLoadMetadata() {
    if (this.videoElement?.nativeElement) {
      this.duration = this.videoElement.nativeElement.duration;
    }
  }

  onTimeUpdate() {
    if (this.videoElement?.nativeElement) {
      this.currentTime = this.videoElement.nativeElement.currentTime;
    }
  }

  onMouseMove() {
    this.showControls = true;
    this.startControlsTimer();
  }

  onVideoClick() {
    this.togglePlay();
  }

  onOverlayClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    const clickedOnControl = target.closest('button, input, .progress-container, .center-play');

    if (!clickedOnControl) {
      this.togglePlay();
    }
  }

  onProgressClick(event: MouseEvent) {
    if (!this.videoElement?.nativeElement || !this.duration) return;

    const progressBar = event.currentTarget as HTMLElement;
    const rect = progressBar.getBoundingClientRect();
    const pos = (event.clientX - rect.left) / rect.width;
    const newTime = pos * this.duration;

    this.videoElement.nativeElement.currentTime = newTime;
    this.currentTime = newTime;
  }

  //Video playback controls
  togglePlay() {
    if (!this.videoElement?.nativeElement) return;
    const video = this.videoElement.nativeElement;
    this.pauseAllOtherVideos(video);

    if (video.paused) {
      video
        .play()
        .then(() => {
          this.isPlaying = true;
        })
        .catch((err) => {
          console.error('Error playing video:', err);
          this.isPlaying = false;
        });
    } else {
      video.pause();
      this.isPlaying = false;
    }
  }

  private pauseAllOtherVideos(currentVideo: HTMLVideoElement) {
    const allVideos = document.querySelectorAll('video');
    allVideos.forEach((video: HTMLVideoElement) => {
      if (video !== currentVideo && !video.paused) {
        video.pause();
      }
    });
  }

  seekForward() {
    if (!this.videoElement?.nativeElement) return;

    const video = this.videoElement.nativeElement;
    video.currentTime = Math.min(video.duration, video.currentTime + 10);
  }
  seekBackward() {
    if (!this.videoElement?.nativeElement) return;

    const video = this.videoElement.nativeElement;
    video.currentTime = Math.max(0, video.currentTime - 10);
  }

  // Volume controls
  toggleMute() {
    if (!this.videoElement?.nativeElement) return;

    const video = this.videoElement.nativeElement;
    video.muted = !video.muted;
    this.isMuted = video.muted;
  }

  changeVolume(event: Event) {
    if (!this.videoElement?.nativeElement) return;

    const target = event.target as HTMLInputElement;
    const value = parseFloat(target.value);

    this.setVolume(value);
    this.isMuted = this.volume === 0;
  }

  increaseVolume() {
    if (!this.videoElement?.nativeElement) return;

    const newVolume = Math.min(1, this.volume + 0.1);
    this.setVolume(newVolume);
    this.isMuted = false;
    this.videoElement.nativeElement.muted = false;
  }

  decreaseVolume() {
    if (!this.videoElement?.nativeElement) return;

    const newVolume = Math.max(0, this.volume - 0.1);
    this.setVolume(newVolume);
    this.isMuted = newVolume === 0;
  }

  private setVolume(value: number) {
    if (!this.videoElement?.nativeElement) return;

    const video = this.videoElement.nativeElement;
    video.volume = value;
    this.volume = value;
  }

  // Fullscreen controls
  toggleFullscreen() {
    const container = document.querySelector('.player-container');
    if (!document.fullscreenElement) {
      container?.requestFullscreen();
      this.isFullscreen = true;
    } else {
      document.exitFullscreen();
      this.isFullscreen = false;
    }
  }

  // UI controls
  startControlsTimer() {
    if (this.controlsTimeout) {
      clearTimeout(this.controlsTimeout);
    }
    this.controlsTimeout = setTimeout(() => {
      if (this.isPlaying) {
        this.showControls = false;
      }
    }, 3000);
  }

  closePlayer() {
    this.dialogRef.close();
  }

  // Utility functions
  formatTime(seconds: number): string {
    return this.utilityService.formatDuration(seconds);
  }

  // Getters
  get videoSrc(): string | null {
    return this.authenticatedVideoUrl;
  }

  get progressPercent(): number {
    return this.duration ? (this.currentTime / this.duration) * 100 : 0;
  }

  get volumePercent(): number {
    return this.volume * 100;
  }
>>>>>>> 6c98f07464af061ed160e7530b52d535e7b19ca9
}
