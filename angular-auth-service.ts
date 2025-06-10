/*import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';

const AUTH_API = 'http://localhost:8080/api/auth/';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject: BehaviorSubject<any>;
  public currentUser: Observable<any>;

  constructor(private http: HttpClient) {
    this.currentUserSubject = new BehaviorSubject<any>(this.getUserFromStorage());
    this.currentUser = this.currentUserSubject.asObservable();
  }

  login(email: string, password: string): Observable<any> {
    return this.http.post(
        AUTH_API + 'login',
        {
          email,
          password
        },
        httpOptions
    ).pipe(
        tap(user => {
          this.storeUser(user);
          this.currentUserSubject.next(user);
        })
    );
  }

  register(nom: string, email: string, password: string, role: string = 'USER'): Observable<any> {
    return this.http.post(
        AUTH_API + 'signup',
        {
          nom,
          email,
          password,
          role
        },
        httpOptions
    );
  }

  logout(): void {
    localStorage.removeItem('auth-user');
    this.currentUserSubject.next(null);
  }

  public get currentUserValue(): any {
    return this.currentUserSubject.value;
  }

  isLoggedIn(): boolean {
    const user = this.getUserFromStorage();
    return !!user && !!user.token;
  }

  getToken(): string | null {
    const user = this.getUserFromStorage();
    return user?.token || null;
  }

  getUserRole(): string | null {
    const user = this.getUserFromStorage();
    return user?.role || null;
  }

  private getUserFromStorage(): any {
    const userStr = localStorage.getItem('auth-user');
    if (userStr) {
      try {
        return JSON.parse(userStr);
      } catch (e) {
        localStorage.removeItem('auth-user');
        return null;
      }
    }
    return null;
  }

  private storeUser(user: any): void {
    localStorage.setItem('auth-user', JSON.stringify(user));
  }
}*/