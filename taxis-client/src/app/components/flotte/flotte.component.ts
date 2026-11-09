import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TaxisService } from '../../services/taxis.service';

@Component({
  selector: 'tpe-flotte',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './flotte.component.html',
  styleUrls: ['./flotte.component.scss'],
})
export class FlotteComponent implements OnInit {
  vehicules: any[] = [];
  vehiculesDisponibles: any[] = [];
  chauffeursEligibles: any[] = [];
  loading = false;
  error = '';

  constructor(private taxis: TaxisService) {}

  ngOnInit(): void {
    this.load();
  }

  contentOf(page: any): any[] {
    if (Array.isArray(page)) return page;
    return page?.content ?? [];
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.taxis.getVehicules().subscribe({
      next: (d) => (this.vehicules = this.contentOf(d)),
      error: (e) => (this.error = this.msg(e)),
    });
    this.taxis.getVehiculesDisponibles().subscribe({
      next: (d) => (this.vehiculesDisponibles = Array.isArray(d) ? d : this.contentOf(d)),
      error: (e) => (this.error = this.msg(e)),
    });
    this.taxis.getChauffeursEligibles().subscribe({
      next: (d) => {
        this.chauffeursEligibles = Array.isArray(d) ? d : this.contentOf(d);
        this.loading = false;
      },
      error: (e) => {
        this.error = this.msg(e);
        this.loading = false;
      },
    });
  }

  msg(e: any): string {
    if (e?.status === 401) return 'Non authentifié.';
    return e?.error?.message || e?.message || 'Erreur lors du chargement de la flotte.';
  }
}
