import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TransactionService } from '../../core/services/transaction.service';
import { Transaction } from '../../models/transaction.model';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './transactions.html'
})
export class TransactionsComponent implements OnInit {
  transactions: Transaction[] = [];
  filteredTransactions: Transaction[] = [];
  searchTerm = '';
  filterType = '';
  loading = true;

  constructor(
    private transactionService: TransactionService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadTransactions();
  }

  loadTransactions(): void {
    this.loading = true;
    this.transactionService.getTransactionHistory().subscribe({
      next: (data) => {
        this.transactions = Array.isArray(data) ? data : [];
        this.applyFilters();
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  applyFilters(): void {
    this.filteredTransactions = this.transactions.filter(txn => {
      const matchesSearch = txn.description?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                            txn.reference?.toLowerCase().includes(this.searchTerm.toLowerCase());
      
      const matchesType = this.filterType === '' || txn.transactionType === this.filterType;
      
      return matchesSearch && matchesType;
    });
  }
}
