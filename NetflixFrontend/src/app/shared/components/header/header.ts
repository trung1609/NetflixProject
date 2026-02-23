import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { filter, Subscription } from 'rxjs';
import { AuthService } from '../../services/auth-service';
import { DialogService } from '../../services/dialog-service';
import { NavigationEnd, Router } from '@angular/router';
import { E } from '@angular/cdk/keycodes';

@Component({
  selector: 'app-header',
  standalone: false,
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header implements OnInit, OnDestroy {
  @Input() showRouterOutlet: boolean = true;
  currentUser: any = null;

  isAdminMode: boolean = false;
  private routerSubscription: Subscription | null = null;

  constructor(
    private authService: AuthService,
    private dialogService: DialogService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();

    this.updateMode();

    this.routerSubscription = this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe(() => {
        this.updateMode();
      });
  }

  private updateMode(): void {
    this.isAdminMode = this.router.url.startsWith('/admin');
  }

  ngOnDestroy(): void {
    if (this.routerSubscription) {
      this.routerSubscription.unsubscribe();
    }
  }

  isAdmin(): boolean {
    return this.currentUser?.role === 'ADMIN';
  }

  switchMode(): void {
    if (this.isAdminMode) {
      this.router.navigate(['/home']);
    } else {
      this.router.navigate(['/admin']);
    }
  }

  openChangePassword() {
    this.dialogService.openChangePasswordDialog();
  }

  logout() {
    this.dialogService
      .openConfirmation(
        'Logout?',
        'Are you sure you want to logout?',
        'Logout',
        'Cancel',
        'warning',
      )
      .subscribe((confirmed) => {
        if (confirmed) {
          this.authService.logout();
        }
      });
  }
}
