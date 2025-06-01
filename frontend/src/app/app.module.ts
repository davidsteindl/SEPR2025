import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { HttpClientModule } from '@angular/common/http';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {HeaderComponent} from './components/header/header.component';
import {FooterComponent} from './components/footer/footer.component';
import {HomeComponent} from './components/home/home.component';
import {LoginComponent} from './components/login/login.component';
import {MessageComponent} from './components/message/message.component';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {httpInterceptorProviders} from './interceptors';
import {ToastrModule} from "ngx-toastr";
import { BuyTicketsPageComponent } from './components/buy-tickets-page/buy-tickets-page.component';
import { SeatMapComponent } from './components/buy-tickets-page/seat-map/seat-map.component';
import { TicketListComponent } from './components/buy-tickets-page/ticket-list-item/ticket-list.component';

@NgModule({
    declarations: [
        AppComponent,
        HeaderComponent,
        FooterComponent,
        HomeComponent,
        LoginComponent,
        MessageComponent
    ],
    bootstrap: [AppComponent],
    imports: [BrowserModule,
        AppRoutingModule,
        ReactiveFormsModule,
        NgbModule,
        BrowserAnimationsModule,
        ToastrModule.forRoot(),
        FormsModule,
        HttpClientModule,
        BuyTicketsPageComponent,
        SeatMapComponent,
        TicketListComponent
    ],
    providers: [httpInterceptorProviders, provideHttpClient(withInterceptorsFromDi())] })
export class AppModule {
}
