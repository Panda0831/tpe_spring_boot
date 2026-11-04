import { Component, OnInit } from '@angular/core';
import { ParkingService } from '../../services/parking.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'tpe-dashboard',
  templateUrl: './dashboard.component.html',
  standalone: true,
  imports: []
})
export class DashboardComponent implements OnInit {
  places: any[] = [];
  reservations: any[] = [];
  entreesSorties: any[] = [];
  statsOccupation: any = {};
  statsRevenus: any = {};
  isLoggedIn = false;

  constructor(
    private parkingService: ParkingService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadData();
    this.authService.currentUser.subscribe(user => {
      this.isLoggedIn = user.authenticated === true;
    });
  }

  loadData(): void {
    this.parkingService.getPlaces().subscribe({
      next: (data) => this.places = data,
      error: (err) => console.error('Erreur places', err)
    });

    this.parkingService.getReservations().subscribe({
      next: (data) => this.reservations = data,
      error: (err) => console.error('Erreur reservations', err)
    });

    this.parkingService.getEntreesSorties().subscribe({
      next: (data) => this.entreesSorties = data,
      error: (err) => console.error('Erreur entrees-sorties', err)
    });

    this.parkingService.getStatsOccupation().subscribe({
      next: (data) => this.statsOccupation = data,
      error: (err) => console.error('Erreur stats occupation', err)
    });

    this.parkingService.getStatsRevenus().subscribe({
      next: (data) => this.statsRevenus = data,
      error: (err) => console.error('Erreur stats revenus', err)
    });
  }

  getZones(): any[] {
    // Group places by zone
    const zonesMap = new Map<string, any>();
    this.places.forEach(place => {
      if (!zonesMap.has(place.zone)) {
        zonesMap.set(place.zone, { nom: place.zone, placesTotal: 0, placesOccupées: 0 });
      }
      const zone = zonesMap.get(place.zone)!;
      zone.placesTotal++;
      if (place.statut === 'OCCUPÉE') zone.placesOccupées++;
    });
    
    return Array.from(zonesMap.values()).map(zone => ({
      ...zone,
      tauxOccupation: zone.placesTotal > 0 ? (zone.placesOccupées / zone.placesTotal) * 100 : 0
    }));
  }
}
