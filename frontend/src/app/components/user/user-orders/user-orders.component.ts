import { Component, OnInit } from '@angular/core';
import { PdfExportService } from '../../../services/pdf-export.service';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-user-orders',
  standalone: true,
  templateUrl: './user-orders.component.html',
  styleUrls: ['./user-orders.component.scss'],
  imports: [RouterModule]
})
export class UserOrdersComponent implements OnInit {

  constructor(private pdfService: PdfExportService) {}

  ngOnInit(): void {}

  exportTicket(): void {

    this.pdfService.exportTicketPdf(5);
  }

  exportInvoice(): void {

  }

  exportStorno(): void {

  }
}
