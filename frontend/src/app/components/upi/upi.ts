import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { AccountService } from '../../core/services/account.service';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-upi',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './upi.html'
})
export class UpiComponent implements OnInit {
  currentUser: User | null = null;
  upiId = '';
  accountNumber = '';
  accountType = 'SAVINGS';
  balance = 0;
  ifscCode = 'APEX0001001';
  copied = false;
  loading = true;

  constructor(
    private authService: AuthService,
    private accountService: AccountService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.setupUpiId();
    this.loadAccount();
  }

  setupUpiId(): void {
    if (this.currentUser && this.currentUser.username) {
      this.upiId = `${this.currentUser.username.toLowerCase()}@apexbank`;
    } else {
      this.authService.currentUser$.subscribe(user => {
        if (user) {
          this.currentUser = user;
          this.upiId = `${user.username.toLowerCase()}@apexbank`;
          this.cdr.markForCheck();
        }
      });
    }
  }

  loadAccount(): void {
    this.loading = true;
    this.accountService.getAccountDetails().subscribe({
      next: (res) => {
        if (res && res.success) {
          this.accountNumber = res.accountNumber || '';
          this.accountType = res.accountType || 'SAVINGS';
          this.balance = Number(res.balance) || 0;
          if (!this.upiId && res.accountNumber) {
            this.upiId = `user${res.accountNumber.slice(-4)}@apexbank`;
          }
        }
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  maskAccount(acc: string): string {
    if (!acc) return '••••';
    const clean = acc.trim();
    return clean.length >= 4 ? `•••• ${clean.slice(-4)}` : clean;
  }

  formatINR(val: number): string {
    return Number(val).toLocaleString('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 2
    });
  }

  copyUpiId(): void {
    if (!this.upiId) return;
    navigator.clipboard.writeText(this.upiId).then(() => {
      this.copied = true;
      setTimeout(() => {
        this.copied = false;
      }, 3000);
    });
  }
}
