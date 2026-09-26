import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin, of, catchError, timeout } from 'rxjs';
import { AccountService } from '../../core/services/account.service';
import { TransactionService } from '../../core/services/transaction.service';
import { Transaction } from '../../models/transaction.model';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin.html'
})
export class AdminComponent implements OnInit {
  stats: any = {
    totalUsers: 0,
    totalAccounts: 0,
    totalTransactions: 0
  };
  accounts: any[] = [];
  transactions: Transaction[] = [];
  loading = true;
  activeTab: 'dashboard' | 'deposit' | 'transfer' | 'accounts' | 'transactions' = 'dashboard';

  // Filters
  accountSearch = '';
  transactionSearch = '';

  // Counter Deposit state
  depositTargetAcc = '';
  depositCustomerName = '';
  depositCustomerType = 'SAVINGS';
  depositCustomerBalance = 0;
  depositVerified = false;
  depositLookingUp = false;
  depositAmount: number | null = null;
  depositDesc = '';
  depositError = '';
  depositStep: 'form' | 'review' | 'success' = 'form';
  depositReceipt: any = null;
  isDepositing = false;
  quickDepositAmounts: number[] = [500, 1000, 2000, 5000, 10000, 25000];

  // Bank Transfer state
  transferFromAcc = '';
  transferFromName = '';
  transferFromBalance = 0;
  transferFromVerified = false;
  transferFromLookingUp = false;

  transferToAcc = '';
  transferToName = '';
  transferToVerified = false;
  transferToLookingUp = false;

  transferAmount: number | null = null;
  transferDesc = '';
  transferError = '';
  transferStep: 'form' | 'review' | 'success' = 'form';
  transferReceipt: any = null;
  isTransferring = false;

  constructor(
    private accountService: AccountService,
    private transactionService: TransactionService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadAdminData();
  }

  loadAdminData(): void {
    this.loading = true;
    this.cdr.markForCheck();

    forkJoin({
      stats: this.accountService.getAdminStats().pipe(
        timeout(5000),
        catchError((err) => {
          console.warn('Error loading admin stats', err);
          return of({ totalUsers: 0, totalAccounts: 0, totalTransactions: 0 });
        })
      ),
      accounts: this.accountService.getAdminAccounts().pipe(
        timeout(5000),
        catchError((err) => {
          console.warn('Error loading admin accounts', err);
          return of([]);
        })
      ),
      transactions: this.transactionService.getAdminTransactions().pipe(
        timeout(5000),
        catchError((err) => {
          console.warn('Error loading admin transactions', err);
          return of([]);
        })
      )
    }).subscribe({
      next: ({ stats, accounts, transactions }) => {
        this.stats = stats || { totalUsers: 0, totalAccounts: 0, totalTransactions: 0 };
        this.accounts = Array.isArray(accounts) ? accounts : [];
        this.transactions = Array.isArray(transactions) ? transactions : [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  switchTab(tab: 'dashboard' | 'deposit' | 'transfer' | 'accounts' | 'transactions'): void {
    this.activeTab = tab;
  }

  formatINR(val: number | null | undefined): string {
    if (val === null || val === undefined || isNaN(val)) return '₹0.00';
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

  get totalVaultDeposits(): number {
    if (!this.accounts || this.accounts.length === 0) return 0;
    return this.accounts.reduce((sum, acc) => sum + (Number(acc.balance) || 0), 0);
  }

  get filteredAccounts(): any[] {
    if (!this.accountSearch) return this.accounts;
    const q = this.accountSearch.toLowerCase().trim();
    return this.accounts.filter(acc =>
      (acc.accountNumber && acc.accountNumber.toLowerCase().includes(q)) ||
      (acc.accountType && acc.accountType.toLowerCase().includes(q)) ||
      String(acc.userId).includes(q) ||
      String(acc.id).includes(q)
    );
  }

  get filteredTransactions(): Transaction[] {
    if (!this.transactionSearch) return this.transactions;
    const q = this.transactionSearch.toLowerCase().trim();
    return this.transactions.filter(t =>
      (t.reference && t.reference.toLowerCase().includes(q)) ||
      (t.description && t.description.toLowerCase().includes(q)) ||
      (t.transactionType && t.transactionType.toLowerCase().includes(q)) ||
      String(t.accountId).includes(q)
    );
  }

  // ==========================================
  // COUNTER DEPOSIT METHODS
  // ==========================================
  quickDepositForAccount(accNum: string): void {
    this.depositTargetAcc = accNum;
    this.depositStep = 'form';
    this.lookupDepositAccount();
    this.switchTab('deposit');
  }

  lookupDepositAccount(): void {
    const clean = (this.depositTargetAcc || '').trim();
    this.depositVerified = false;
    this.depositCustomerName = '';

    if (!clean || clean.length !== 10) {
      this.depositError = clean ? 'Account number must be 10 digits' : '';
      return;
    }

    this.depositLookingUp = true;
    this.depositError = '';

    this.accountService.lookupAccount(clean).subscribe({
      next: (res) => {
        this.depositLookingUp = false;
        if (res && res.success && res.found) {
          this.depositVerified = true;
          this.depositCustomerName = res.accountHolder || 'Customer';
          this.depositCustomerType = res.accountType || 'SAVINGS';
          // Find balance if in accounts list
          const found = this.accounts.find(a => a.accountNumber === clean);
          if (found) {
            this.depositCustomerBalance = Number(found.balance) || 0;
          }
        } else {
          this.depositError = res?.message || 'Customer account not found';
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.depositLookingUp = false;
        this.depositError = err.error?.message || 'Customer account not found';
        this.cdr.markForCheck();
      }
    });
  }

  selectDepositAmount(val: number): void {
    this.depositAmount = val;
  }

  proceedDepositReview(): void {
    if (!this.depositVerified || !this.depositTargetAcc) {
      this.depositError = 'Please verify customer account number';
      return;
    }
    if (!this.depositAmount || this.depositAmount <= 0) {
      this.depositError = 'Please enter a valid deposit amount';
      return;
    }
    this.depositError = '';
    this.depositStep = 'review';
  }

  confirmDeposit(): void {
    if (!this.depositTargetAcc || !this.depositAmount) return;

    this.isDepositing = true;
    this.depositError = '';

    const note = this.depositDesc?.trim() ? this.depositDesc.trim() : 'Branch Counter Cash Deposit';

    this.accountService.adminDeposit({
      targetAccountNumber: this.depositTargetAcc.trim(),
      amount: this.depositAmount,
      description: note
    }).subscribe({
      next: (res) => {
        this.isDepositing = false;
        if (res && res.success) {
          this.depositReceipt = {
            reference: res.reference,
            targetAccount: res.targetAccountNumber,
            customerName: res.accountHolder || this.depositCustomerName,
            amount: this.depositAmount,
            newBalance: res.balance,
            date: new Date(),
            note: note
          };
          this.depositStep = 'success';
          this.loadAdminData(); // Refresh metrics
        } else {
          this.depositError = res?.message || 'Deposit failed';
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.isDepositing = false;
        this.depositError = err.error?.message || 'Deposit failed. Please try again.';
        this.cdr.markForCheck();
      }
    });
  }

  resetDepositForm(): void {
    this.depositTargetAcc = '';
    this.depositCustomerName = '';
    this.depositVerified = false;
    this.depositAmount = null;
    this.depositDesc = '';
    this.depositError = '';
    this.depositReceipt = null;
    this.depositStep = 'form';
  }

  // ==========================================
  // BANK TRANSFER METHODS
  // ==========================================
  lookupTransferFrom(): void {
    const clean = (this.transferFromAcc || '').trim();
    this.transferFromVerified = false;
    this.transferFromName = '';

    if (!clean || clean.length !== 10) return;

    this.transferFromLookingUp = true;
    this.accountService.lookupAccount(clean).subscribe({
      next: (res) => {
        this.transferFromLookingUp = false;
        if (res && res.success && res.found) {
          this.transferFromVerified = true;
          this.transferFromName = res.accountHolder || 'Customer';
          const found = this.accounts.find(a => a.accountNumber === clean);
          if (found) {
            this.transferFromBalance = Number(found.balance) || 0;
          }
        }
        this.cdr.markForCheck();
      },
      error: () => {
        this.transferFromLookingUp = false;
        this.cdr.markForCheck();
      }
    });
  }

  lookupTransferTo(): void {
    const clean = (this.transferToAcc || '').trim();
    this.transferToVerified = false;
    this.transferToName = '';

    if (!clean || clean.length !== 10) return;

    this.transferToLookingUp = true;
    this.accountService.lookupAccount(clean).subscribe({
      next: (res) => {
        this.transferToLookingUp = false;
        if (res && res.success && res.found) {
          this.transferToVerified = true;
          this.transferToName = res.accountHolder || 'Customer';
        }
        this.cdr.markForCheck();
      },
      error: () => {
        this.transferToLookingUp = false;
        this.cdr.markForCheck();
      }
    });
  }

  proceedTransferReview(): void {
    if (!this.transferFromVerified || !this.transferToVerified) {
      this.transferError = 'Please verify both source and destination accounts';
      return;
    }
    if (this.transferFromAcc === this.transferToAcc) {
      this.transferError = 'Source and destination accounts cannot be identical';
      return;
    }
    if (!this.transferAmount || this.transferAmount <= 0) {
      this.transferError = 'Please enter a valid transfer amount';
      return;
    }
    if (this.transferFromBalance > 0 && this.transferAmount > this.transferFromBalance) {
      this.transferError = `Insufficient balance in source account. Available: ${this.formatINR(this.transferFromBalance)}`;
      return;
    }
    this.transferError = '';
    this.transferStep = 'review';
  }

  confirmTransfer(): void {
    this.isTransferring = true;
    this.transferError = '';

    const note = this.transferDesc?.trim() ? this.transferDesc.trim() : 'Branch Teller Assisted Transfer';

    this.accountService.adminTransfer({
      fromAccountNumber: this.transferFromAcc.trim(),
      toAccountNumber: this.transferToAcc.trim(),
      amount: this.transferAmount!,
      description: note
    }).subscribe({
      next: (res) => {
        this.isTransferring = false;
        if (res && res.success) {
          this.transferReceipt = {
            reference: res.reference,
            fromAccount: this.transferFromAcc,
            fromName: this.transferFromName,
            toAccount: this.transferToAcc,
            toName: this.transferToName,
            amount: this.transferAmount,
            date: new Date(),
            note: note
          };
          this.transferStep = 'success';
          this.loadAdminData(); // Refresh metrics
        } else {
          this.transferError = res?.message || 'Transfer failed';
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.isTransferring = false;
        this.transferError = err.error?.message || 'Transfer failed. Please check balances and try again.';
        this.cdr.markForCheck();
      }
    });
  }

  resetTransferForm(): void {
    this.transferFromAcc = '';
    this.transferFromName = '';
    this.transferFromVerified = false;
    this.transferToAcc = '';
    this.transferToName = '';
    this.transferToVerified = false;
    this.transferAmount = null;
    this.transferDesc = '';
    this.transferError = '';
    this.transferReceipt = null;
    this.transferStep = 'form';
  }
}
