import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TaxisService {
  private baseUrl = '/api';

  constructor(private http: HttpClient) {}

  // Chauffeurs
  getChauffeurs(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/chauffeurs`);
  }

  // Véhicules
  getVehicules(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/vehicules`);
  }

  // Clients
  getClients(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/clients`);
  }

  // Courses
  getCourses(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/courses`);
  }

  createCourse(course: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/courses`, course);
  }

  // Dispatch
  dispatch(): Observable<any> {
    return this.http.post(`${this.baseUrl}/dispatch/auto`, {});
  }

  // Tarifs
  getTarifs(): Observable<any> {
    return this.http.get(`${this.baseUrl}/tarifs`);
  }

  // Stats
  getRevenueStats(debut?: string, fin?: string): Observable<any> {
    let params = '';
    if (debut) params += `?dateDebut=${debut}`;
    if (fin) params += params ? `&dateFin=${fin}` : `?dateFin=${fin}`;
    return this.http.get<any>(`${this.baseUrl}/stats/revenue${params}`);
  }

  getActivityStats(debut?: string, fin?: string): Observable<any> {
    let params = '';
    if (debut) params += `?dateDebut=${debut}`;
    if (fin) params += params ? `&dateFin=${fin}` : `?dateFin=${fin}`;
    return this.http.get<any>(`${this.baseUrl}/stats/activity${params}`);
  }
}
