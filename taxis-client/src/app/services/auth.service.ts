import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { map, tap, catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface User {
  id: number;
  username: string;
  email: string;
  role: string;
  authenticated: boolean;
}

export interface Credentials {
  username: string;
  password: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private currentUserSubject: BehaviorSubject<User>;
  public currentUser: Observable<User>;

  private loginSource = new BehaviorSubject<boolean>(false);
  public loginFailed = this.loginSource.asObservable();

  private apiUrl = 'api';

  constructor(private http: HttpClient) {
    this.currentUserSubject = new BehaviorSubject<User>(JSON.parse(localStorage.getItem('currentUser') || '{}'));
    this.currentUser = this.currentUserSubject.asObservable();
  }

  public get currentUserValue(): User {
    return this.currentUserSubject.value;
  }

  login(credentials: Credentials): Observable<boolean> {
    return this.http.post<User>(`/${this.apiUrl}/login`, credentials)
      .pipe(
        map(user => {
          if (user && user.authenticated) {
            localStorage.setItem('currentUser', JSON.stringify(user));
            this.currentUserSubject.next(user);
            return true;
          } else {
            return false;
          }
        })
      );
  }

  logout(): void {
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next({} as User);
  }

  isAuthenticated(): boolean {
    return this.currentUserValue.authenticated === true;
  }

  getToken(): string | null {
    const user = this.currentUserValue;
    return user.token || null;
  }

  getRole(): string {
    return this.currentUserValue.role || 'USER';
  }
}
