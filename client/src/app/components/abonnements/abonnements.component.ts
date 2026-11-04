import { Component, OnInit } from '@angular/core';
import { ParkingService } from '../../services/parking.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'tpe-abonnements',
  templateUrl: './abonnements.component.html',
  standalone: true,
  imports: []
})
export class AbonnementsComponent implements OnInit {
  abonnements: any[] = [];
  utilisateurs: any[] = [];

  constructor(
    private parkingService: ParkingService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadAbonnements();
  }

  loadAbonnements(): void {
    this.parkingService.getAbonnements().subscribe({
      next: (data) => this.abonnements = data,
      error: (err) => console.error('Erreur abonnements', err)
    });
  }
}
