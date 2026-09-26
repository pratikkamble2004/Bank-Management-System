import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AccountService {
  private apiUrl = '/bank-management/api';

  constructor(private http: HttpClient) {}

  getAccountDetails(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/account`, { withCredentials: true });
  }

  getBalance(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/account/balance`, { withCredentials: true });
  }

  lookupAccount(accountNumber: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/account/lookup?accountNumber=${encodeURIComponent(accountNumber)}`, { withCredentials: true });
  }

  deposit(data: { amount: number; description?: string }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/account/deposit`, data, { withCredentials: true });
  }

  withdraw(data: { amount: number; description?: string }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/account/withdraw`, data, { withCredentials: true });
  }

  transfer(data: { toAccountNumber: string; amount: number; description?: string }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/account/transfer`, data, { withCredentials: true });
  }

  getAdminAccounts(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/admin/accounts`, { withCredentials: true });
  }

  getAdminStats(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/admin/`, { withCredentials: true });
  }

  adminDeposit(data: { targetAccountNumber: string; amount: number; description?: string }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/admin/deposit`, data, { withCredentials: true });
  }

  adminTransfer(data: { fromAccountNumber: string; toAccountNumber: string; amount: number; description?: string }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/admin/transfer`, data, { withCredentials: true });
  }
}
