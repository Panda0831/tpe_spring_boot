import { Component, OnInit } from '@angular/core';
import { ParkingService } from '../../services/parking.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'tpe-places',
  templateUrl: './places.component.html',
  standalone: true,
  imports: []
})
export class PlacesComponent implements OnInit {
  places: any[] = [];
  zones: string[] = [];
  filtrage = { zone: '', type: '', statut: '' };

  constructor(
    private parkingService: ParkingService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadPlaces();
  }

  loadPlaces(): void {
    this.parkingService.getPlaces().subscribe({
      next: (data) => {
        this.places = data;
        this.zones = [...new Set(data.map(p => p.zone))];
      },
      error: (err) => console.error('Erreur places', err)
    });
  }

  getPlacesFiltrees(): any[] {
    return this.places.filter(place => {
      if (this.filtrage.zone && place.zone !== this.filtrage.zone) return false;
      if (this.filtrage.type && place.type !== this.filtrage.type) return false;
      if (this.filtrage.statut && place.statut !== this.filtrage.statut) return false;
      return true;
    });
  }
}
