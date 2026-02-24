import { Routes } from '@angular/router';
import { LoginComponent } from './core/shared/components/login/login.component';
import { RegisterComponent } from './core/shared/components/register/register.component';
import { MainComponent } from './core/main/main.component';

export const routes: Routes = [
    {path: 'main', component: MainComponent,},
    {path: 'login', component: LoginComponent},
    {path: 'register', component: RegisterComponent}
];
