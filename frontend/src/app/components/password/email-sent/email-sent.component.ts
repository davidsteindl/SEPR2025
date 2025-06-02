import {Component, HostBinding} from '@angular/core';
import {RouterLink} from "@angular/router";

@Component({
  selector: 'app-email-sent',
  imports: [
    RouterLink
  ],
  standalone: true,
  templateUrl: './email-sent.component.html',
  styleUrl: './email-sent.component.scss'
})
export class EmailSentComponent {

  @HostBinding('class') cssClass = 'modal fade';
  @HostBinding('attr.data-bs-backdrop') backdrop = 'static';
  @HostBinding('attr.aria-hidden') hidden = 'true';
  @HostBinding('attr.aria-labelledby') labeledBy = 'email-sent';

}
