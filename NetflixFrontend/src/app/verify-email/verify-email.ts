import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../shared/services/auth-service';

@Component({
  selector: 'app-verify-email',
  standalone: false,
  templateUrl: './verify-email.html',
  styleUrl: './verify-email.css',
})
export class VerifyEmail implements OnInit {
  loading = true;
  success = false;
  message = '';

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (!token) {
      this.loading = false;
      this.success = false;
      this.message = 'Invalid verification link.No token provided.';
      return;
    }

    console.log('Verifying email with token:', token);

    this.authService.verifyEmail(token).subscribe({
      next: (response: any) => {
        console.log('Verification success response:', response);
        this.loading = false;
        this.success = true;
        this.message = response.message || 'Email verified successfully! You can now log in.';
        console.log('Success state set:', { loading: this.loading, success: this.success, message: this.message });
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Verification error:', err);
        this.loading = false;
        this.success = false;
        this.message = err.error?.error || 'Verification failed. The link may have expired or is invalid.';
        console.log('Error state set:', { loading: this.loading, success: this.success, message: this.message });
        this.cdr.detectChanges();
      }

    });
  }
}
