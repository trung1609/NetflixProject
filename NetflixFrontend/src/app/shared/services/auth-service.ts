import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { BehaviorSubject, tap } from 'rxjs';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl = environment.apiUrl + '/auth';

  private currentUserSubject = new BehaviorSubject<any>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router,
  ) {
    this.loadUserFromStorage();
  }

  loadUserFromStorage() {
    const userData = localStorage.getItem('user');
    if (userData) {
      try {
        const user = JSON.parse(userData);
        this.currentUserSubject.next(user);
      } catch (error) {
        console.error('Failed to parse user data:', error);
        localStorage.removeItem('user');
      }
    }
  }

  passwordMatchValidator(passwordControlName: string): ValidatorFn {
    return (confirmControl: AbstractControl): ValidationErrors | null => {
      if (!confirmControl.parent) {
        return null;
      }
      const password = confirmControl.parent.get(passwordControlName)?.value;
      const confirmPassword = confirmControl.value;
      return password === confirmPassword ? null : { passwordMismatch: true };
    };
  }

  signup(signupData: any) {
    return this.http.post(this.apiUrl + '/signup', signupData);
  }

  verifyEmail(token: string) {
    return this.http.get(this.apiUrl + '/verify-email?token=' + token);
  }

  login(loginData: any) {
    return this.http
      .post(this.apiUrl + '/login', loginData)
      .pipe(tap((response) => this.handleAuthSuccess(response)));
  }

  handleAuthSuccess(authData: any) {
    if (authData?.token) {
      localStorage.setItem('token', authData.token);
    }
    if (authData?.email && authData?.role) {
      const userData = {
        email: authData.email,
        fullName: authData.fullName,
        role: authData.role
      };
      console.log('Storing user data:', userData);
      localStorage.setItem('user', JSON.stringify(userData));
      this.setCurrentUser(userData);
    } else {
      console.warn('Missing email or role in auth response:', authData);
    }
  }

  setCurrentUser(user: any | null) {
    this.currentUserSubject.next(user);
  }

  getCurrentUser() {
    if (!this.currentUserSubject.value) {
      const userData = localStorage.getItem('user');
      if (userData) {
        const user = JSON.parse(userData);
        this.currentUserSubject.next(user);
        return user;
      }
    }
    return this.currentUserSubject.value;
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  redirectBasedOnRole() {
    const targetUrl = this.isAdmin() ? '/admin' : '/home';
    this.router.navigate([targetUrl]);
  }

  isAdmin(): boolean {
    const user = this.getCurrentUser();
    return user?.role === 'ADMIN';
  }

  resendVerificationEmail(email: string) {
    return this.http.post(this.apiUrl + '/resend-verification', { email });
  }
}
