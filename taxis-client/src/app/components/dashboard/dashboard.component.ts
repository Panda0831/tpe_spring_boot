import { Component, OnInit } from '@angular/core';
import { TaxisService } from '../../services/taxis.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'tpe-dashboard',
  templateUrl: './dashboard.component.html',
  standalone: true,
  imports: []
})
export class DashboardComponent implements OnInit {
  chauffeurs: any[] = [];
  vehicules: any[] = [];
  courses: any[] = [];
  clients: any[] = [];
  statsRevenue: any = {};
  statsActivity: any = {};
  isLoggedIn = false;

  constructor(
    private taxisService: TaxisService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadData();
    this.authService.currentUser.subscribe(user => {
      this.isLoggedIn = user.authenticated === true;
    });
  }

  loadData(): void {
    this.taxisService.getChauffeurs().subscribe({
      next: (data) => this.chauffeurs = data,
      error: (err) => console.error('Erreur chauffeurs', err)
    });

    this.taxisService.getVehicules().subscribe({
      next: (data) => this.vehicules = data,
      error: (err) => console.error('Erreur vehicules', err)
    });

    this.taxisService.getCourses().subscribe({
      next: (data) => this.courses = data,
      error: (err) => console.error('Erreur courses', err)
    });

    this.taxisService.getClients().subscribe({
      next: (data) => this.clients = data,
      error: (err) => console.error('Erreur clients', err)
    });

    this.taxisService.getRevenueStats().subscribe({
      next: (data) => this.statsRevenue = data,
      error: (err) => console.error('Erreur stats revenue', err)
    });

    this.taxisService.getActivityStats().subscribe({
      next: (data) => this.statsActivity = data,
      error: (err) => console.error('Erreur stats activity', err)
    });
  }
}
