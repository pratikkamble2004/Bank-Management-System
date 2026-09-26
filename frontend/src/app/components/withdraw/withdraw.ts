import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AccountService } from '../../core/services/account.service';

@Component({
  selector: 'app-withdraw',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './withdraw.html'
})
export class WithdrawComponent implements OnInit {
  // Step management
  step: 'form' | 'review' | 'success' | 'failure' = 'form';

  // Withdrawal Mode (UPI Scanner vs Manual UPI ID)
  withdrawMode: 'scanner' | 'manual' = 'scanner';
  isScanning = true;
  scannedQrCode = '';
  upiTerminalId = '';
  pickupCode = '';

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
      this.amountError = 'Please enter a withdrawal amount.';
      return false;
    }
    if (this.amount <= 0) {
      this.amountError = 'Amount must be greater than ₹0.';
      return false;
    }
    if (this.amount > this.currentBalance) {
      this.amountError = `Insufficient balance. Available balance: ${this.formatINR(this.currentBalance)}.`;
      return false;
    }
    if (this.amount > 50000) {
      this.amountError = 'Daily cash withdrawal limit is ₹50,000 per transaction.';
      return false;
    }
    this.amountError = '';
    return true;
  }

  get calculatedNewBalance(): number {
    return Math.max(0, (this.currentBalance || 0) - (this.amount || 0));
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

  simulateQrScan(): void {
    this.isScanning = false;
    this.scannedQrCode = 'upi://pay?pa=ATM-MUM-4001@apexbank&pn=Apex%20ATM%20Kiosk%204001';
    this.upiTerminalId = 'ATM-MUM-4001@apexbank';
  }

  resetScanner(): void {
    this.isScanning = true;
    this.scannedQrCode = '';
    this.upiTerminalId = '';
  }

  confirmWithdraw(): void {
    if (!this.validateAmount()) {
      this.step = 'form';
      return;
    }

    this.isSubmitting = true;
    this.failureReason = '';

    const terminal = this.upiTerminalId ? this.upiTerminalId : 'Apex UPI ATM Scanner';
    const note = this.description?.trim() ? this.description.trim() : `UPI ATM Cash Withdrawal (${terminal})`;

    this.accountService.withdraw({
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
          this.pickupCode = Math.floor(100000 + Math.random() * 900000).toString();
          this.step = 'success';
        } else {
          this.failureReason = res?.message || 'Could not complete the withdrawal.';
          this.step = 'failure';
        }
      },
      error: (err) => {
        this.isSubmitting = false;
        this.failureReason = err.error?.message || 'Could not complete the withdrawal. Please check your balance and try again.';
        this.step = 'failure';
      }
    });
  }

  resetForm(): void {
    this.amount = null;
    this.description = '';
    this.amountError = '';
    this.resetScanner();
    this.pickupCode = '';
    this.step = 'form';
    this.fetchAccountDetails();
  }
}
