import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterState } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService) {}

  canActivate(route: ActivatedRouteSnapshot): boolean {
    return this.authService.isAuthenticated();
  }
}
