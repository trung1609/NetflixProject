import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth-service';
import { MatDialogRef } from '@angular/material/dialog';
import { NotificationService } from '../../services/notification-service';
import { ErrorHandlerService } from '../../services/error-handler-service';

@Component({
  selector: 'app-change-password-dialog',
  standalone: false,
  templateUrl: './change-password-dialog.html',
  styleUrl: './change-password-dialog.css',
})
export class ChangePasswordDialog implements OnInit {
  changePasswordForm!: FormGroup;
  loading = false;
  hideCurrent = true;
  hideNew = true;
  hideConfirm = true;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private dialogRef: MatDialogRef<ChangePasswordDialog>,
    private notification: NotificationService,
    private errorHandlerService: ErrorHandlerService,
  ) {
    this.changePasswordForm = this.fb.group({
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: [
        '',
        [Validators.required, this.authService.passwordMatchValidator('newPassword')],
      ],
    });
  }

  ngOnInit(): void {
    // Re-validate confirmPassword when newPassword changes
    this.changePasswordForm.get('newPassword')?.valueChanges.subscribe(() => {
      this.changePasswordForm.get('confirmPassword')?.updateValueAndValidity();
    });
  }

  submit() {
    this.loading = true;
    const formData = this.changePasswordForm.value;
    const data = {
      currentPassword: formData.currentPassword,
      newPassword: formData.newPassword,
    };
    this.authService.changePassword(data).subscribe({
      next: (response: any) => {
        this.loading = false;
        this.notification.success(response.message || 'Password changed successfully');
        this.dialogRef.close(true);
      },
      error: (err) => {
        this.loading = false;
        this.errorHandlerService.handle(err, 'Failed to change password. Please try again.');
      },
    });
  }

  cancel() {
    this.dialogRef.close(false);
  }
}
