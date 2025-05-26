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

@NgModule({
    declarations: [
        AppComponent,
        HeaderComponent,
        FooterComponent,
        HomeComponent,
        LoginComponent,
        MessageComponent,
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
    ],
    providers: [httpInterceptorProviders, provideHttpClient(withInterceptorsFromDi())] })
export class AppModule {
}
