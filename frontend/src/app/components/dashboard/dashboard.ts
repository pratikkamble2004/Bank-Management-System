import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { forkJoin, of, catchError, timeout } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { AccountService } from '../../core/services/account.service';
import { TransactionService } from '../../core/services/transaction.service';
import { User } from '../../models/user.model';
import { Transaction } from '../../models/transaction.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.html'
})
export class DashboardComponent implements OnInit {
  currentUser: User | null = null;
  accountNumber = '';
  accountType = 'SAVINGS';
  balance = 0;
  recentTransactions: Transaction[] = [];
  loading = true;

  constructor(
    private authService: AuthService,
    private accountService: AccountService,
    private transactionService: TransactionService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      if (user && user.role === 'ADMIN') {
        this.router.navigate(['/admin']);
        return;
      }
      this.cdr.markForCheck();
    });

    if (!this.currentUser) {
      this.authService.checkSession().subscribe(res => {
        if (res && res.success && res.user) {
          this.currentUser = res.user;
          if (res.user.role === 'ADMIN') {
            this.router.navigate(['/admin']);
            return;
          }
          this.cdr.markForCheck();
        }
      });
    }

    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.cdr.markForCheck();
    forkJoin({
      account: this.accountService.getAccountDetails().pipe(
        timeout(4000),
        catchError(() => of(null))
      ),
      transactions: this.transactionService.getTransactionHistory().pipe(
        timeout(4000),
        catchError(() => of([]))
      )
    }).subscribe({
      next: ({ account, transactions }) => {
        if (account && account.success) {
          this.accountNumber = account.accountNumber || '';
          this.accountType = account.accountType || 'SAVINGS';
          this.balance = Number(account.balance) || 0;
        }
        this.recentTransactions = Array.isArray(transactions) ? transactions.slice(0, 5) : [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  formatINR(val: number): string {
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
}

