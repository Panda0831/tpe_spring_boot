import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TaxisService {
  private baseUrl = '/api';

  constructor(private http: HttpClient) {}

  private pageParams(page = 0, size = 50): HttpParams {
    return new HttpParams().set('page', page).set('size', size);
  }

  // Chauffeurs
  getChauffeurs(page = 0, size = 50): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/chauffeurs`, { params: this.pageParams(page, size) });
  }
  getChauffeur(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/chauffeurs/${id}`);
  }
  getChauffeursEligibles(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/chauffeurs/eligibles`);
  }
  createChauffeur(body: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/chauffeurs`, body);
  }
  updateChauffeur(id: number, body: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/chauffeurs/${id}`, body);
  }
  deleteChauffeur(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/chauffeurs/${id}`);
  }
  changerStatutChauffeur(id: number, statut: string): Observable<any> {
    return this.http.patch(`${this.baseUrl}/chauffeurs/${id}/statut`, null, {
      params: new HttpParams().set('statut', statut),
    });
  }
  affecterVehicule(chauffeurId: number, vehiculeId: number): Observable<any> {
    return this.http.patch(`${this.baseUrl}/chauffeurs/${chauffeurId}/vehicule/${vehiculeId}`, null);
  }
  retirerVehicule(chauffeurId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/chauffeurs/${chauffeurId}/vehicule`);
  }

  // Véhicules
  getVehicules(page = 0, size = 50): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/vehicules`, { params: this.pageParams(page, size) });
  }
  getVehicule(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/vehicules/${id}`);
  }
  getVehiculesDisponibles(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/vehicules/disponibles`);
  }
  createVehicule(body: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/vehicules`, body);
  }
  updateVehicule(id: number, body: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/vehicules/${id}`, body);
  }
  deleteVehicule(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/vehicules/${id}`);
  }

  // Clients
  getClients(page = 0, size = 50): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/clients`, { params: this.pageParams(page, size) });
  }
  getClient(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/clients/${id}`);
  }
  createClient(body: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/clients`, body);
  }
  updateClient(id: number, body: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/clients/${id}`, body);
  }

  // Courses
  getCourses(page = 0, size = 50): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/courses`, { params: this.pageParams(page, size) });
  }
  getCourse(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/courses/${id}`);
  }
  createCourse(body: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/courses`, body);
  }
  demarrerCourse(id: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/courses/${id}/demarrer`, {});
  }
  terminerCourse(id: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/courses/${id}/terminer`, {});
  }
  annulerCourse(id: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/courses/${id}/annuler`, {});
  }
  deleteCourse(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/courses/${id}`);
  }

  // Dispatch
  dispatchCourse(courseId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/dispatch/courses/${courseId}/dispatcher`, {});
  }
  creerEtAssigner(body: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/dispatch/courses`, body);
  }

  // Tarifs
  getTarifs(): Observable<any> {
    return this.http.get(`${this.baseUrl}/tarifs`);
  }
  getTarif(id: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/tarifs/${id}`);
  }
  updateTarif(id: number, body: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/tarifs/${id}`, body);
  }

  // Stats
  getRevenueStats(dateDebut?: string, dateFin?: string): Observable<any> {
    let params = new HttpParams();
    if (dateDebut) params = params.set('dateDebut', dateDebut);
    if (dateFin) params = params.set('dateFin', dateFin);
    return this.http.get<any>(`${this.baseUrl}/stats/revenue`, { params });
  }
  getActivityStats(dateDebut?: string, dateFin?: string): Observable<any> {
    let params = new HttpParams();
    if (dateDebut) params = params.set('dateDebut', dateDebut);
    if (dateFin) params = params.set('dateFin', dateFin);
    return this.http.get<any>(`${this.baseUrl}/stats/activity`, { params });
  }
}
