import { Component, OnInit } from '@angular/core';
import { ParkingService } from '../../services/parking.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'tpe-reservations',
  templateUrl: './reservations.component.html',
  standalone: true,
  imports: []
})
export class ReservationsComponent implements OnInit {
  reservations: any[] = [];
  filtrerPar = 'tous';
  statutOptions = ['EN_ATTENTE', 'CONFIRMEE', 'ANNULEE'];

  constructor(
    private parkingService: ParkingService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadReservations();
  }

  loadReservations(): void {
    this.parkingService.getReservations().subscribe({
      next: (data) => this.reservations = data,
      error: (err) => console.error('Erreur reservations', err)
    });
  }
}
