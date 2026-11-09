import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

export interface User {
  username: string;
  authenticated: boolean;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private currentUserSubject: BehaviorSubject<User>;
  public currentUser: Observable<User>;

  constructor(private http: HttpClient) {
    const stored = localStorage.getItem('currentUser');
    this.currentUserSubject = new BehaviorSubject<User>(
      stored ? JSON.parse(stored) : { username: '', authenticated: false }
    );
    this.currentUser = this.currentUserSubject.asObservable();
  }

  public get currentUserValue(): User {
    return this.currentUserSubject.value;
  }

  login(username: string, password: string): Observable<boolean> {
    const basic = btoa(`${username}:${password}`);
    localStorage.setItem('basicAuth', basic);
    // Verify credentials against a protected endpoint
    return this.http.get('/api/chauffeurs', { params: { page: '0', size: '1' } }).pipe(
      map(() => {
        const user: User = { username, authenticated: true };
        localStorage.setItem('currentUser', JSON.stringify(user));
        this.currentUserSubject.next(user);
        return true;
      }),
      catchError(() => {
        localStorage.removeItem('basicAuth');
        localStorage.removeItem('currentUser');
        this.currentUserSubject.next({ username: '', authenticated: false });
        return of(false);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('basicAuth');
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next({ username: '', authenticated: false });
  }

  isAuthenticated(): boolean {
    return !!localStorage.getItem('basicAuth') && this.currentUserValue.authenticated === true;
  }

  getRole(): string {
    return (this.currentUserValue as any)?.role || 'USER';
  }
}
