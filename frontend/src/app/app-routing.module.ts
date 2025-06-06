import {NgModule} from '@angular/core';
import {mapToCanActivate, RouterModule, Routes} from '@angular/router';
import {HomeComponent} from './components/home/home.component';
import {LoginComponent} from './components/login/login.component';
import {AuthGuard} from './guards/auth.guard';
import {MessageComponent} from './components/message/message.component';
import {RegisterComponent} from './components/register/register.component';
import {TermsandconditionsComponent} from './components/termsandconditions/termsandconditions.component';
import {ManageAccountsComponent} from './components/manage-accounts/manage-accounts.component';
import {AdminGuard} from './guards/admin.guard';
import {UserComponent} from './components/user/user.component';
import {UserEditComponent} from './components/user/user-edit/user-edit.component';
import {SearchComponent} from './components/search/search.component';
import {CreateEventComponent} from './components/create-content/create-event/create-event.component';
import {UpdateEventsComponent} from './components/update-events/update-events.component';
import {CreateArtistComponent} from './components/create-content/create-artist/create-artist.component';
import {CreateShowComponent} from './components/create-content/create-show/create-show.component';
import {CreateLocationComponent} from './components/create-content/create-location/create-location.component';
import {AdminComponent} from './components/admin/admin.component';
import {ArtistEventsComponent} from './components/artist-events/artist-events.component';
import {EventOverviewComponent} from "./components/event-overview/event-overview.component";
import {CreateRoomComponent} from './components/create-content/create-room/create-room.component';
import {RoomComponent} from "./components/room/room.component";
import {RoomEditComponent} from './components/room/room-edit/room-edit.component';
import {LocationShowsComponent} from "./components/location-shows/location-shows.component";
import {PaymentFormComponent} from './components/payment-form/payment-form.component';
import {OrderOverviewComponent} from "./components/order-overview/order-overview.component";
import {RoomUsageComponent} from "./components/room/room-usage/room-usage.component";
import {
  PurchasedOrderDetailComponent
} from "./components/order-detail/purchased-order-detail/purchased-order-detail.component";
import {
  ReservedOrderDetailComponent
} from "./components/order-detail/reserved-order-detail/reserved-order-detail.component";
import {PastOrderDetailComponent} from "./components/order-detail/past-order-detail/past-order-detail.component";
import {EditEventComponent} from "./components/edit-event/edit-event.component";
import {TicketComponent} from "./components/ticket/ticket.component";
import { PasswordComponent } from './components/password/password.component';
import {ResetPasswordComponent} from "./components/password/reset-password/reset-password.component";
import { BuyTicketsPageComponent } from './components/buy-tickets-page/buy-tickets-page.component';

const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'manage-accounts', component: ManageAccountsComponent, canActivate: [AdminGuard]},
  { path: 'register', component: RegisterComponent},
  { path: 'termsandconditions', component: TermsandconditionsComponent},
  { path: 'message', canActivate: mapToCanActivate([AuthGuard]), component: MessageComponent },
  { path: 'checkout', component: PaymentFormComponent, canActivate: [AuthGuard] },
  { path: 'user', component: UserComponent, canActivate: [AuthGuard]},
  { path: 'user-edit', component: UserEditComponent, canActivate: [AuthGuard]},
  { path: 'search', component: SearchComponent, canActivate: [AuthGuard]},
  { path: 'create-event', component: CreateEventComponent, canActivate: [AdminGuard] },
  { path: 'update-events', component: UpdateEventsComponent, canActivate: [AdminGuard] },
  { path: 'events/:id/edit',  component: EditEventComponent, canActivate: [AdminGuard] },
  { path: 'create-artist', component: CreateArtistComponent, canActivate: [AdminGuard] },
  { path: 'create-show', component: CreateShowComponent, canActivate: [AdminGuard] },
  { path: 'create-location', component: CreateLocationComponent, canActivate: [AdminGuard] },
  { path: 'create-room', component: CreateRoomComponent, canActivate: [AdminGuard] },
  { path: 'admin', component: AdminComponent, canActivate: [AdminGuard]},
  { path: 'artists/:id/events', component: ArtistEventsComponent, canActivate: [AuthGuard] },
  { path: 'events/:id/overview', component: EventOverviewComponent, canActivate: [AuthGuard] },
  { path: 'rooms/:id/overview', component: RoomComponent, canActivate: [AdminGuard] },
  { path: 'rooms/:id/edit', component: RoomEditComponent, canActivate: [AdminGuard] },
  { path: 'locations/:id/shows', component: LocationShowsComponent, canActivate: [AuthGuard] },
  { path: 'orders', component: OrderOverviewComponent, canActivate: [AuthGuard] },
  { path: 'shows/:id/room-usage', component: BuyTicketsPageComponent, canActivate: [AuthGuard] },
  { path: 'orders/purchased/:id', component: PurchasedOrderDetailComponent, canActivate: [AuthGuard] },
  { path: 'orders/reserved/:id', component: ReservedOrderDetailComponent, canActivate: [AuthGuard] },
  { path: 'orders/past/:id', component: PastOrderDetailComponent, canActivate: [AuthGuard] },
  { path: 'ticket/:id/:randomTicketCode', component: TicketComponent, canActivate: [AdminGuard] },
  { path: 'forgot-password', component: PasswordComponent},
  { path: 'reset-password', component: ResetPasswordComponent},
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: false})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
