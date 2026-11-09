import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TaxisService } from '../../services/taxis.service';

@Component({
  selector: 'tpe-chauffeurs',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chauffeurs.component.html',
  styleUrls: ['./chauffeurs.component.scss'],
})
export class ChauffeursComponent implements OnInit {
  chauffeurs: any[] = [];
  vehicules: any[] = [];
  loading = false;
  error = '';
  success = '';
  form: any = { nom: '', prenom: '', telephone: '', numeroPermis: '', dateExpirationPermis: '', statut: 'DISPONIBLE' };
  assign: any = { chauffeurId: null, vehiculeId: null };
  editingId: number | null = null;

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
    this.taxis.getChauffeurs().subscribe({
      next: (d) => {
        this.chauffeurs = this.contentOf(d);
        this.loading = false;
      },
      error: (e) => {
        this.error = this.msg(e);
        this.loading = false;
      },
    });
    this.taxis.getVehicules().subscribe({
      next: (d) => (this.vehicules = this.contentOf(d)),
      error: () => {},
    });
  }

  save(): void {
    this.error = ''; this.success = '';
    const req = this.editingId
      ? this.taxis.updateChauffeur(this.editingId, this.form)
      : this.taxis.createChauffeur(this.form);
    req.subscribe({
      next: () => {
        this.success = this.editingId ? 'Chauffeur modifié.' : 'Chauffeur créé.';
        this.resetForm();
        this.load();
      },
      error: (e) => (this.error = this.msg(e)),
    });
  }

  edit(c: any): void {
    this.editingId = c.id;
    this.form = { nom: c.nom, prenom: c.prenom, telephone: c.telephone, numeroPermis: c.numeroPermis, dateExpirationPermis: c.dateExpirationPermis, statut: c.statut };
  }

  resetForm(): void {
    this.editingId = null;
    this.form = { nom: '', prenom: '', telephone: '', numeroPermis: '', dateExpirationPermis: '', statut: 'DISPONIBLE' };
  }

  setStatut(id: number, statut: string): void {
    this.error = ''; this.success = '';
    this.taxis.changerStatutChauffeur(id, statut).subscribe({
      next: () => { this.success = 'Statut mis à jour.'; this.load(); },
      error: (e) => (this.error = this.msg(e)),
    });
  }

  affecter(): void {
    if (!this.assign.chauffeurId || !this.assign.vehiculeId) return;
    this.error = ''; this.success = '';
    this.taxis.affecterVehicule(this.assign.chauffeurId, this.assign.vehiculeId).subscribe({
      next: () => { this.success = 'Véhicule affecté.'; this.assign = { chauffeurId: null, vehiculeId: null }; this.load(); },
      error: (e) => (this.error = this.msg(e)),
    });
  }

  retirer(chauffeurId: number): void {
    this.error = ''; this.success = '';
    this.taxis.retirerVehicule(chauffeurId).subscribe({
      next: () => { this.success = 'Véhicule retiré.'; this.load(); },
      error: (e) => (this.error = this.msg(e)),
    });
  }

  remove(id: number): void {
    if (!confirm('Supprimer ce chauffeur ?')) return;
    this.error = ''; this.success = '';
    this.taxis.deleteChauffeur(id).subscribe({
      next: () => { this.success = 'Chauffeur supprimé.'; this.load(); },
      error: (e) => (this.error = this.msg(e)),
    });
  }

  msg(e: any): string {
    if (e?.status === 401) return 'Non authentifié.';
    if (e?.status === 403) return 'Accès refusé (permissions insuffisantes).';
    return e?.error?.message || e?.error?.error || e?.message || 'Erreur.';
  }
}
