import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AccountService } from '../../core/services/account.service';

@Component({
  selector: 'app-deposit',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './deposit.html'
})
export class DepositComponent implements OnInit {
  // Step management
  step: 'form' | 'review' | 'success' | 'failure' = 'form';

  // Account details
  accountNumber = '';
  accountType = 'SAVINGS';
  currentBalance = 0;
  loadingAccount = true;

  // Form inputs
  amount: number | null = null;
  description = '';
  amountError = '';

  // Processing state
  isSubmitting = false;

  // Quick Amount presets
  quickAmounts: number[] = [500, 1000, 2000, 5000, 10000];

  // Success / Failure details
  txReference = '';
  txTimestamp: Date = new Date();
  updatedBalance = 0;
  failureReason = '';

  constructor(
    private accountService: AccountService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.fetchAccountDetails();
  }

  fetchAccountDetails(): void {
    this.loadingAccount = true;
    this.accountService.getAccountDetails().subscribe({
      next: (res) => {
        if (res && res.success) {
          this.accountNumber = res.accountNumber || '';
          this.accountType = res.accountType || 'SAVINGS';
          this.currentBalance = Number(res.balance) || 0;
        }
        this.loadingAccount = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loadingAccount = false;
        this.cdr.detectChanges();
      }
    });
  }

  formatINR(val: number | null): string {
    if (val === null || val === undefined || isNaN(val)) {
      return '₹0.00';
    }
    return Number(val).toLocaleString('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 2
    });
  }

  maskAccount(acc: string): string {
    if (!acc) return '••••';
    const clean = acc.trim();
    return clean.length >= 4 ? `•••• ${clean.slice(-4)}` : clean;
  }

  selectQuickAmount(val: number): void {
    this.amount = val;
    this.validateAmount();
  }

  validateAmount(): boolean {
    if (this.amount === null || this.amount === undefined || (this.amount as any) === '') {
      this.amountError = 'Please enter an amount.';
      return false;
    }
    if (this.amount <= 0) {
      this.amountError = 'Amount must be greater than ₹0.';
      return false;
    }
    if (this.amount > 10000000) {
      this.amountError = 'Maximum deposit limit per transaction is ₹1,00,00,000.';
      return false;
    }
    this.amountError = '';
    return true;
  }

  get calculatedNewBalance(): number {
    return (this.currentBalance || 0) + (this.amount || 0);
  }

  goToReview(): void {
    if (!this.validateAmount()) {
      return;
    }
    this.step = 'review';
  }

  editDetails(): void {
    this.step = 'form';
  }

  confirmDeposit(): void {
    if (!this.validateAmount()) {
      this.step = 'form';
      return;
    }

    this.isSubmitting = true;
    this.failureReason = '';

    const note = this.description?.trim() ? this.description.trim() : 'Cash deposit';

    this.accountService.deposit({
      amount: this.amount!,
      description: note
    }).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        if (res && res.success) {
          this.updatedBalance = Number(res.balance) || this.calculatedNewBalance;
          this.currentBalance = this.updatedBalance;
          this.txReference = res.reference || ('REF-' + Date.now());
          this.txTimestamp = new Date();
          this.step = 'success';
        } else {
          this.failureReason = res?.message || 'Could not complete the deposit.';
          this.step = 'failure';
        }
      },
      error: (err) => {
        this.isSubmitting = false;
        this.failureReason = err.error?.message || 'Could not complete the deposit. Please try again.';
        this.step = 'failure';
      }
    });
  }

  resetForm(): void {
    this.amount = null;
    this.description = '';
    this.amountError = '';
    this.step = 'form';
    this.fetchAccountDetails();
  }
}
