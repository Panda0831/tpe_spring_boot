import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { PlacesComponent } from './components/places/places.component';
import { ReservationsComponent } from './components/reservations/reservations.component';
import { AbonnementsComponent } from './components/abonnements/abonnements.component';
import { BorneComponent } from './components/borne/borne.component';
import { StatistiquesComponent } from './components/statistiques/statistiques.component';
import { LoginComponent } from './components/login/login.component';
import { AuthGuard } from './auth/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: 'places', component: PlacesComponent, canActivate: [AuthGuard] },
  { path: 'reservations', component: ReservationsComponent, canActivate: [AuthGuard] },
  { path: 'abonnements', component: AbonnementsComponent, canActivate: [AuthGuard] },
  { path: 'borne', component: BorneComponent, canActivate: [AuthGuard] },
  { path: 'statistiques', component: StatistiquesComponent, canActivate: [AuthGuard] },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' }
];
