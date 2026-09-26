import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html'
})
export class LoginComponent implements OnInit {
  isLoginMode = true;

  loginUsername = '';
  loginPassword = '';

  registerUsername = '';
  registerPassword = '';
  registerAccountType = 'SAVINGS';

  errorMessage = '';
  successMessage = '';
  loading = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    if (user) {
      if (user.role === 'ADMIN') {
        this.router.navigate(['/admin']);
      } else {
        this.router.navigate(['/dashboard']);
      }
    }
  }

  toggleMode(): void {
    this.isLoginMode = !this.isLoginMode;
    this.errorMessage = '';
    this.successMessage = '';
  }

  onLogin(): void {
    if (!this.loginUsername || !this.loginPassword) {
      this.errorMessage = 'Please enter both username and password';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    
    this.authService.login({ username: this.loginUsername, password: this.loginPassword }).subscribe({
      next: (res) => {
        this.loading = false;
        if (res.success) {
          if (res.user.role === 'ADMIN') {
            this.router.navigate(['/admin']);
          } else {
            this.router.navigate(['/dashboard']);
          }
        } else {
          this.errorMessage = res.message || 'Login failed';
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Invalid username or password';
        this.cdr.detectChanges();
      }
    });
  }

  onRegister(): void {
    if (!this.registerUsername || !this.registerPassword) {
      this.errorMessage = 'Please enter both username and password';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.authService.register({
      username: this.registerUsername,
      password: this.registerPassword,
      accountType: this.registerAccountType
    }).subscribe({
      next: (res) => {
        this.loading = false;
        if (res && res.success) {
          const accNum = res.accountNumber || (res.account ? res.account.accountNumber : '');
          this.successMessage = accNum
            ? `Account created successfully! Your Account Number is: ${accNum}. Please sign in below.`
            : 'Registration successful! You can now sign in.';
          this.loginUsername = this.registerUsername;
          this.isLoginMode = true;
          this.registerUsername = '';
          this.registerPassword = '';
        } else {
          this.errorMessage = res?.message || 'Registration failed';
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Registration failed. Username may already exist.';
        this.cdr.detectChanges();
      }
    });
  }
}
