import { Component, OnInit } from '@angular/core';
import { ParkingService } from '../../services/parking.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'tpe-borne',
  templateUrl: './borne.component.html',
  standalone: true,
  imports: []
})
export class BorneComponent implements OnInit {
  entreesEnCours: any[] = [];
  historique: any[] = [];
  vehiculeImmatriculation = '';

  constructor(
    private parkingService: ParkingService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadDonnees();
  }

  loadDonnees(): void {
    this.parkingService.getEntreesSorties().subscribe({
      next: (data) => {
        this.entreesEnCours = data.filter((e: any) => e.heureSortie == null);
        this.historique = data.filter((e: any) => e.heureSortie != null);
      },
      error: (err) => console.error('Erreur entrees-sorties', err)
    });
  }

  enregistrerEntree(): void {
    if (!this.vehiculeImmatriculation) {
      alert('Veuillez entrer l\'immatriculation');
      return;
    }
    this.parkingService.enregistrerEntree({ immatriculation: this.vehiculeImmatriculation }).subscribe({
      next: () => {
        alert('Entrée enregistrée avec succès');
        this.loadDonnees();
        this.vehiculeImmatriculation = '';
      },
      error: (err) => console.error('Erreur enregistrement entrée', err)
    });
  }
}
