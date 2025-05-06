import { NgModule } from '@angular/core';
import { mapToCanActivate, RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { LoginComponent } from './components/login/login.component';
import { AuthGuard } from './guards/auth.guard';
import { MessageComponent } from './components/message/message.component';
import { RegisterComponent } from "./components/register/register.component";
import { TermsandconditionsComponent } from "./components/termsandconditions/termsandconditions.component";
import { ManageAccountsComponent } from './components/manage-accounts/manage-accounts.component';
import { AdminGuard } from './guards/admin.guard';
import {UserComponent} from "./components/user/user.component";
import {UserEditComponent} from "./components/user/user-edit/user-edit.component";


const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  {
    path: 'manage-accounts',
    component: ManageAccountsComponent,
    canActivate: [AdminGuard]
  },
  { path: 'register', component: RegisterComponent },
  { path: 'termsandconditions', component: TermsandconditionsComponent },
  { path: 'message', canActivate: mapToCanActivate([AuthGuard]), component: MessageComponent },
  {path: 'user', component: UserComponent},
  {path: 'user-edit', component: UserEditComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
