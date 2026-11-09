import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TaxisService } from '../../services/taxis.service';

@Component({
  selector: 'tpe-clients',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './clients.component.html',
  styleUrls: ['./clients.component.scss'],
})
export class ClientsComponent implements OnInit {
  clients: any[] = [];
  loading = false;
  error = '';
  success = '';
  editingId: number | null = null;
  form: any = { nom: '', prenom: '', telephone: '', email: '' };

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
    this.taxis.getClients().subscribe({
      next: (d) => {
        this.clients = this.contentOf(d);
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
    const body = { ...this.form };
    if (!body.email) delete body.email;
    const req = this.editingId
      ? this.taxis.updateClient(this.editingId, body)
      : this.taxis.createClient(body);
    req.subscribe({
      next: () => {
        this.success = this.editingId ? 'Client modifié.' : 'Client créé.';
        this.resetForm();
        this.load();
      },
      error: (e) => (this.error = this.msg(e)),
    });
  }

  edit(c: any): void {
    this.editingId = c.id;
    this.form = { nom: c.nom, prenom: c.prenom, telephone: c.telephone, email: c.email || '' };
  }

  resetForm(): void {
    this.editingId = null;
    this.form = { nom: '', prenom: '', telephone: '', email: '' };
  }

  msg(e: any): string {
    if (e?.status === 401) return 'Non authentifié.';
    if (e?.status === 403) return 'Accès refusé (permissions insuffisantes).';
    return e?.error?.message || e?.error?.error || e?.message || 'Erreur.';
  }
}
