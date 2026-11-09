import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'tpe-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent {
  username = '';
  password = '';
  error = '';
  loading = false;

  constructor(
    private auth: AuthService,
    private router: Router
  ) {
    if (this.auth.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
    }
  }

  submit(): void {
    this.error = '';
    this.loading = true;
    this.auth.login(this.username, this.password).subscribe((ok) => {
      this.loading = false;
      if (ok) {
        this.router.navigate(['/dashboard']);
      } else {
        this.error = 'Identifiants invalides ou serveur indisponible.';
      }
    });
  }
}
