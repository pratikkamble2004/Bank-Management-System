import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of, tap, catchError } from 'rxjs';
import { User } from '../../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = '/bank-management/api/auth';
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {}

  login(credentials: { username: string; password: string }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/login`, credentials, { withCredentials: true }).pipe(
      tap(res => {
        if (res && res.success) {
          this.currentUserSubject.next(res.user);
        }
      })
    );
  }

  register(userData: { username: string; password: string; accountType: string }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/register`, userData, { withCredentials: true });
  }

  logout(): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/logout`, {}, { withCredentials: true }).pipe(
      tap(() => {
        this.currentUserSubject.next(null);
      }),
      catchError(() => {
        this.currentUserSubject.next(null);
        return of({ success: true });
      })
    );
  }

  checkSession(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/me`, { withCredentials: true }).pipe(
      tap({
        next: (res) => {
          if (res && res.success) {
            this.currentUserSubject.next(res.user);
          } else {
            this.currentUserSubject.next(null);
          }
        },
        error: () => {
          this.currentUserSubject.next(null);
        }
      }),
      catchError(() => {
        this.currentUserSubject.next(null);
        return of({ success: false, user: null });
      })
    );
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  setCurrentUser(user: User | null): void {
    this.currentUserSubject.next(user);
  }
}
