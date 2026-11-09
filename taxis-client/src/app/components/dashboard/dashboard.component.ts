import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TaxisService } from '../../services/taxis.service';

@Component({
  selector: 'tpe-dashboard',
  templateUrl: './dashboard.component.html',
  standalone: true,
  imports: [CommonModule, RouterLink],
})
export class DashboardComponent implements OnInit {
  chauffeurs: any[] = [];
  vehicules: any[] = [];
  courses: any[] = [];
  clients: any[] = [];
  revenue: any = null;
  activity: any = null;
  loading = true;
  error = '';

  constructor(private taxis: TaxisService) {}

  ngOnInit(): void {
    this.load();
  }

  private contentOf(page: any): any[] {
    if (Array.isArray(page)) return page;
    if (page && Array.isArray(page.content)) return page.content;
    return [];
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.taxis.getChauffeurs(0, 10).subscribe({
      next: (d) => (this.chauffeurs = this.contentOf(d)),
      error: (e) => (this.error = this.msg(e)),
    });
    this.taxis.getVehicules(0, 10).subscribe({
      next: (d) => (this.vehicules = this.contentOf(d)),
      error: (e) => (this.error = this.msg(e)),
    });
    this.taxis.getCourses(0, 10).subscribe({
      next: (d) => (this.courses = this.contentOf(d)),
      error: (e) => (this.error = this.msg(e)),
    });
    this.taxis.getClients(0, 10).subscribe({
      next: (d) => (this.clients = this.contentOf(d)),
      error: (e) => (this.error = this.msg(e)),
    });
    this.taxis.getRevenueStats().subscribe({
      next: (d) => (this.revenue = d),
      error: (e) => (this.error = this.msg(e)),
    });
    this.taxis.getActivityStats().subscribe({
      next: (d) => {
        this.activity = d;
        this.loading = false;
      },
      error: (e) => {
        this.error = this.msg(e);
        this.loading = false;
      },
    });
  }

  count(page: any): number {
    if (!page) return 0;
    if (Array.isArray(page)) return page.length;
    if (typeof page.totalElements === 'number') return page.totalElements;
    if (Array.isArray(page.content)) return page.content.length;
    return 0;
  }

  keys(obj: any): string[] {
    return obj ? Object.keys(obj) : [];
  }

  format(val: any): string {
    if (val === null || val === undefined) return '-';
    if (typeof val === 'object') return JSON.stringify(val);
    return String(val);
  }

  private msg(e: any): string {
    if (e?.status === 401) return 'Non authentifié. Veuillez vous reconnecter.';
    return e?.error?.message || e?.message || 'Erreur lors du chargement des données.';
  }
}
