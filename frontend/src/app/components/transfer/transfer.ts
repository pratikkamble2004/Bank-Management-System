import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AccountService } from '../../core/services/account.service';

@Component({
  selector: 'app-transfer',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './transfer.html'
})
export class TransferComponent implements OnInit {
  // Step management
  step: 'form' | 'review' | 'success' | 'failure' = 'form';

  // Sender account details
  accountNumber = '';
  accountType = 'SAVINGS';
  currentBalance = 0;
  loadingAccount = true;

  // Recipient inputs & verification
  toAccountNumber = '';
  recipientName = '';
  recipientAccountType = 'SAVINGS';
  recipientVerified = false;
  isLookingUpRecipient = false;
  recipientError = '';

  // Amount & Note inputs
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

  onAccountNumberChange(): void {
    const clean = this.toAccountNumber ? this.toAccountNumber.trim() : '';
    this.recipientVerified = false;
    this.recipientName = '';

    if (!clean) {
      this.recipientError = '';
      return;
    }

    if (clean === this.accountNumber) {
      this.recipientError = 'You cannot transfer funds to your own account.';
      return;
    }

    if (clean.length === 10) {
      this.lookupRecipient(clean);
    } else if (clean.length > 10) {
      this.recipientError = 'Account number cannot exceed 10 digits.';
    } else {
      this.recipientError = '';
    }
  }

  lookupRecipient(accNum?: string): void {
    const clean = (accNum || this.toAccountNumber || '').trim();
    if (!clean) {
      this.recipientError = 'Please enter an account number.';
      return;
    }
    if (clean === this.accountNumber) {
      this.recipientError = 'You cannot transfer funds to your own account.';
      this.recipientVerified = false;
      return;
    }
    if (clean.length !== 10) {
      this.recipientError = 'Account number must be exactly 10 digits.';
      this.recipientVerified = false;
      return;
    }

    this.isLookingUpRecipient = true;
    this.recipientError = '';
    this.accountService.lookupAccount(clean).subscribe({
      next: (res) => {
        this.isLookingUpRecipient = false;
        if (res && res.success && res.found) {
          this.recipientVerified = true;
          this.recipientName = res.accountHolder || 'Account Holder';
          this.recipientAccountType = res.accountType || 'SAVINGS';
          this.recipientError = '';
        } else {
          this.recipientVerified = false;
          this.recipientName = '';
          this.recipientError = res?.message || 'Recipient account not found. Please verify the account number.';
        }
      },
      error: (err) => {
        this.isLookingUpRecipient = false;
        this.recipientVerified = false;
        this.recipientName = '';
        this.recipientError = err.error?.message || 'Recipient account not found. Please verify the account number.';
      }
    });
  }

  selectQuickAmount(val: number): void {
    this.amount = val;
    this.validateAmount();
  }

  validateAmount(): boolean {
    if (this.amount === null || this.amount === undefined || (this.amount as any) === '') {
      this.amountError = 'Please enter a transfer amount.';
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
    if (this.amount > 500000) {
      this.amountError = 'Single transfer limit is ₹5,00,000.';
      return false;
    }
    this.amountError = '';
    return true;
  }

  get calculatedNewBalance(): number {
    return Math.max(0, (this.currentBalance || 0) - (this.amount || 0));
  }

  goToReview(): void {
    const clean = (this.toAccountNumber || '').trim();
    if (!clean) {
      this.recipientError = 'Please enter recipient account number.';
      return;
    }
    if (clean === this.accountNumber) {
      this.recipientError = 'You cannot transfer funds to your own account.';
      return;
    }
    if (clean.length !== 10) {
      this.recipientError = 'Account number must be exactly 10 digits.';
      return;
    }

    if (!this.validateAmount()) {
      return;
    }

    if (!this.recipientVerified) {
      this.isLookingUpRecipient = true;
      this.accountService.lookupAccount(clean).subscribe({
        next: (res) => {
          this.isLookingUpRecipient = false;
          if (res && res.success && res.found) {
            this.recipientVerified = true;
            this.recipientName = res.accountHolder || 'Account Holder';
            this.recipientAccountType = res.accountType || 'SAVINGS';
            this.recipientError = '';
            this.step = 'review';
          } else {
            this.recipientError = res?.message || 'Recipient account not found.';
          }
        },
        error: (err) => {
          this.isLookingUpRecipient = false;
          this.recipientError = err.error?.message || 'Recipient account not found.';
        }
      });
      return;
    }

    this.step = 'review';
  }

  editDetails(): void {
    this.step = 'form';
  }

  confirmTransfer(): void {
    if (!this.validateAmount() || !this.recipientVerified) {
      this.step = 'form';
      return;
    }

    this.isSubmitting = true;
    this.failureReason = '';

    const note = this.description?.trim() ? this.description.trim() : 'Funds transfer';

    this.accountService.transfer({
      toAccountNumber: this.toAccountNumber.trim(),
      amount: this.amount!,
      description: note
    }).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        if (res && res.success) {
          this.updatedBalance = Number(res.balance) || this.calculatedNewBalance;
          this.currentBalance = this.updatedBalance;
          this.txReference = res.reference || ('TXN-' + Date.now());
          this.txTimestamp = new Date();
          if (res.recipientName) {
            this.recipientName = res.recipientName;
          }
          this.step = 'success';
        } else {
          this.failureReason = res?.message || 'Could not complete the transfer.';
          this.step = 'failure';
        }
      },
      error: (err) => {
        this.isSubmitting = false;
        this.failureReason = err.error?.message || 'Transfer failed. Please check details and try again.';
        this.step = 'failure';
      }
    });
  }

  resetForm(): void {
    this.toAccountNumber = '';
    this.recipientName = '';
    this.recipientVerified = false;
    this.recipientError = '';
    this.amount = null;
    this.description = '';
    this.amountError = '';
    this.step = 'form';
    this.fetchAccountDetails();
  }
}
