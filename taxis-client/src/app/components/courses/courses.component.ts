import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TaxisService } from '../../services/taxis.service';

@Component({
  selector: 'tpe-courses',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './courses.component.html',
  styleUrls: ['./courses.component.scss'],
})
export class CoursesComponent implements OnInit {
  courses: any[] = [];
  clients: any[] = [];
  loading = false;
  error = '';
  success = '';
  form: any = { clientId: null, pointDepart: '', destination: '', distanceKm: null };
  assignForm: any = { clientId: null, pointDepart: '', destination: '', distanceKm: null };

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
    this.taxis.getCourses().subscribe({
      next: (d) => {
        this.courses = this.contentOf(d);
        this.loading = false;
      },
      error: (e) => {
        this.error = this.msg(e);
        this.loading = false;
      },
    });
    this.taxis.getClients().subscribe({
      next: (d) => (this.clients = this.contentOf(d)),
      error: () => {},
    });
  }

  commander(): void {
    this.error = ''; this.success = '';
    this.taxis.createCourse(this.form).subscribe({
      next: () => {
        this.success = 'Course commandée.';
        this.form = { clientId: null, pointDepart: '', destination: '', distanceKm: null };
        this.load();
      },
      error: (e) => (this.error = this.msg(e)),
    });
  }

  creerEtAssigner(): void {
    this.error = ''; this.success = '';
    this.taxis.creerEtAssigner(this.assignForm).subscribe({
      next: (c) => {
        this.success = `Course créée et assignée (ID ${c?.id ?? '?'}).`;
        this.assignForm = { clientId: null, pointDepart: '', destination: '', distanceKm: null };
        this.load();
      },
      error: (e) => (this.error = this.msg(e)),
    });
  }

  dispatch(id: number): void {
    this.action(() => this.taxis.dispatchCourse(id), 'Dispatch effectué.');
  }
  demarrer(id: number): void {
    this.action(() => this.taxis.demarrerCourse(id), 'Course démarrée.');
  }
  terminer(id: number): void {
    this.action(() => this.taxis.terminerCourse(id), 'Course terminée.');
  }
  annuler(id: number): void {
    this.action(() => this.taxis.annulerCourse(id), 'Course annulée.');
  }
  supprimer(id: number): void {
    if (!confirm('Supprimer cette course ?')) return;
    this.error = ''; this.success = '';
    this.taxis.deleteCourse(id).subscribe({
      next: () => { this.success = 'Course supprimée.'; this.load(); },
      error: (e) => (this.error = this.msg(e)),
    });
  }

  private action(call: () => any, okMsg: string): void {
    this.error = ''; this.success = '';
    call().subscribe({
      next: () => { this.success = okMsg; this.load(); },
      error: (e: any) => (this.error = this.msg(e)),
    });
  }

  msg(e: any): string {
    if (e?.status === 401) return 'Non authentifié.';
    if (e?.status === 403) return 'Accès refusé (permissions insuffisantes).';
    return e?.error?.message || e?.error?.error || e?.message || 'Erreur.';
  }
}
