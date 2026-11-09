import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TaxisService } from '../../services/taxis.service';

@Component({
  selector: 'tpe-statistiques',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './statistiques.component.html',
  styleUrls: ['./statistiques.component.scss'],
})
export class StatistiquesComponent implements OnInit {
  revenue: any = null;
  activity: any = null;
  dateDebut = '';
  dateFin = '';
  loading = false;
  error = '';

  constructor(private taxis: TaxisService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    const debut = this.dateDebut ? `${this.dateDebut}T00:00:00` : undefined;
    const fin = this.dateFin ? `${this.dateFin}T23:59:59` : undefined;
    this.taxis.getRevenueStats(debut, fin).subscribe({
      next: (d) => (this.revenue = d),
      error: (e) => (this.error = this.msg(e)),
    });
    this.taxis.getActivityStats(debut, fin).subscribe({
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

  keys(obj: any): string[] {
    return obj ? Object.keys(obj) : [];
  }

  format(val: any): string {
    if (val === null || val === undefined) return '-';
    if (typeof val === 'object') return JSON.stringify(val);
    return String(val);
  }

  private msg(e: any): string {
    return e?.error?.message || e?.message || 'Erreur lors du chargement des statistiques.';
  }
}
