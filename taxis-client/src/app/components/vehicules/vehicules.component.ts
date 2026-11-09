import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TaxisService } from '../../services/taxis.service';

@Component({
  selector: 'tpe-vehicules',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './vehicules.component.html',
  styleUrls: ['./vehicules.component.scss'],
})
export class VehiculesComponent implements OnInit {
  vehicules: any[] = [];
  loading = false;
  error = '';
  success = '';
  editingId: number | null = null;
  form: any = { immatriculation: '', marque: '', modele: '', annee: new Date().getFullYear(), capacite: 4, kilometrage: 0, statut: 'DISPONIBLE' };

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
      next: (d) => {
        this.vehicules = this.contentOf(d);
        this.loading = false;
      },
      error: (e) => {
        this.error = this.msg(e);
        this.loading = false;
      },
    });
  }

  save(): void {
    this.error = ''; this.success = '';
    const req = this.editingId
      ? this.taxis.updateVehicule(this.editingId, this.form)
      : this.taxis.createVehicule(this.form);
    req.subscribe({
      next: () => {
        this.success = this.editingId ? 'Véhicule modifié.' : 'Véhicule créé.';
        this.resetForm();
        this.load();
      },
      error: (e) => (this.error = this.msg(e)),
    });
  }

  edit(v: any): void {
    this.editingId = v.id;
    this.form = { immatriculation: v.immatriculation, marque: v.marque, modele: v.modele, annee: v.annee, capacite: v.capacite, kilometrage: v.kilometrage, statut: v.statut };
  }

  resetForm(): void {
    this.editingId = null;
    this.form = { immatriculation: '', marque: '', modele: '', annee: new Date().getFullYear(), capacite: 4, kilometrage: 0, statut: 'DISPONIBLE' };
  }

  remove(id: number): void {
    if (!confirm('Supprimer ce véhicule ?')) return;
    this.error = ''; this.success = '';
    this.taxis.deleteVehicule(id).subscribe({
      next: () => { this.success = 'Véhicule supprimé.'; this.load(); },
      error: (e) => (this.error = this.msg(e)),
    });
  }

  msg(e: any): string {
    if (e?.status === 401) return 'Non authentifié.';
    if (e?.status === 403) return 'Accès refusé (permissions insuffisantes).';
    return e?.error?.message || e?.error?.error || e?.message || 'Erreur.';
  }
}
