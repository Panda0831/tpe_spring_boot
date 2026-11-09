import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TaxisService } from '../../services/taxis.service';

@Component({
  selector: 'tpe-tarifs',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './tarifs.component.html',
  styleUrls: ['./tarifs.component.scss'],
})
export class TarifsComponent implements OnInit {
  tarifs: any[] = [];
  loading = false;
  error = '';
  success = '';
  editing: any = null;

  constructor(private taxis: TaxisService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.taxis.getTarifs().subscribe({
      next: (d) => {
        this.tarifs = Array.isArray(d) ? d : (d?.content ?? []);
        this.loading = false;
      },
      error: (e) => {
        this.error = this.msg(e);
        this.loading = false;
      },
    });
  }

  edit(t: any): void {
    this.editing = { id: t.id, tarifBase: t.tarifBase, prixKm: t.prixKm, commissionPourcentage: t.commissionPourcentage };
  }

  cancel(): void {
    this.editing = null;
  }

  save(): void {
    if (!this.editing?.id) return;
    this.error = ''; this.success = '';
    const body = {
      tarifBase: this.editing.tarifBase,
      prixKm: this.editing.prixKm,
      commissionPourcentage: this.editing.commissionPourcentage,
    };
    this.taxis.updateTarif(this.editing.id, body).subscribe({
      next: () => {
        this.success = 'Tarif mis à jour.';
        this.editing = null;
        this.load();
      },
      error: (e) => (this.error = this.msg(e)),
    });
  }

  keys(obj: any): string[] {
    return obj ? Object.keys(obj) : [];
  }

  msg(e: any): string {
    if (e?.status === 401) return 'Non authentifié.';
    if (e?.status === 403) return 'Accès refusé : seul un administrateur/manager (selon config) peut modifier les tarifs.';
    return e?.error?.message || e?.error?.error || e?.message || 'Erreur.';
  }
}
