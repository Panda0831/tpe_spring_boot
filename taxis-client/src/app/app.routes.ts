import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { CoursesComponent } from './components/courses/courses.component';
import { ChauffeursComponent } from './components/chauffeurs/chauffeurs.component';
import { VehiculesComponent } from './components/vehicules/vehicules.component';
import { ClientsComponent } from './components/clients/clients.component';
import { FlotteComponent } from './components/flotte/flotte.component';
import { StatistiquesComponent } from './components/statistiques/statistiques.component';
import { TarifsComponent } from './components/tarifs/tarifs.component';
import { LoginComponent } from './components/login/login.component';
import { AuthGuard } from './auth/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: 'courses', component: CoursesComponent, canActivate: [AuthGuard] },
  { path: 'chauffeurs', component: ChauffeursComponent, canActivate: [AuthGuard] },
  { path: 'vehicules', component: VehiculesComponent, canActivate: [AuthGuard] },
  { path: 'clients', component: ClientsComponent, canActivate: [AuthGuard] },
  { path: 'flotte', component: FlotteComponent, canActivate: [AuthGuard] },
  { path: 'statistiques', component: StatistiquesComponent, canActivate: [AuthGuard] },
  { path: 'tarifs', component: TarifsComponent, canActivate: [AuthGuard] },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' }
];
