import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Place {
  id: number;
  numero: number;
  zone: string;
  type: string;
  statut: string;
}

export interface Reservation {
  id: number;
  utilisateurId: number;
  vehiculeId: number;
  placeId: number;
  dateArriveePrevue: string;
  dateDepartPrevue: string;
  statut: string;
}

@Injectable({ providedIn: 'root' })
export class ParkingService {
  private baseUrl = 'api/parking';

  constructor(private http: HttpClient) {}

  // Places
  getPlaces(): Observable<Place[]> {
    return this.http.get<Place[]>(`${this.baseUrl}/places`);
  }

  getPlace(id: number): Observable<Place> {
    return this.http.get<Place>(`${this.baseUrl}/places/${id}`);
  }

  // Reservations
  getReservations(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/reservations`);
  }

  createReservation(reservation: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/reservations`, reservation);
  }

  updateReservation(id: number, reservation: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/reservations/${id}`, reservation);
  }

  deleteReservation(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/reservations/${id}`);
  }

  // Entrées/Sorties
  getEntreesSorties(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/entrees-sorties`);
  }

  enregistrerEntree(entree: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/entrees-sorties/entree`, entree);
  }

  enregistrerSortie(sortie: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/entrees-sorties/sortie`, sortie);
  }

  // Abonnements
  getAbonnements(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/abonnements`);
  }

  souscrireAbonnement(abonnement: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/abonnements/souscrire`, abonnement);
  }

  // Stats
  getStatsOccupation(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/stats/occupation`);
  }

  getStatsRevenus(days: number = 30): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/stats/revenus?jours=${days}`);
  }
}
