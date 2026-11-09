import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { AuthService } from './services/auth.service';

@Component({
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  selector: 'tpe-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  constructor(public auth: AuthService, private router: Router) {}

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
