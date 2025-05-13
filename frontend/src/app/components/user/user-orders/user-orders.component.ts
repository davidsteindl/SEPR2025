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
    const dto = {
      type: 'ticket',
      firstName: 'Max',
      lastName: 'Mustermann',
      eventName: 'Konzert XYZ',
      eventDate: '2025-05-25',
      location: 'Wien Stadthalle',
      price: '59.90'
    };
    this.pdfService.exportPdf(dto);
  }

  exportInvoice(): void {
    const dto = {
      type: 'invoice',
      firstName: 'Max',
      lastName: 'Mustermann',
      eventName: 'Konzert XYZ',
      eventDate: '2025-05-25',
      location: 'Wien Stadthalle',
      price: '59.90'
    };
    this.pdfService.exportPdf(dto);
  }

  exportStorno(): void {
    const dto = {
      type: 'storno',
      firstName: 'Max',
      lastName: 'Mustermann',
      eventName: 'Konzert XYZ',
      eventDate: '2025-05-25',
      location: 'Wien Stadthalle',
      price: '59.90'
    };
    this.pdfService.exportPdf(dto);
  }
}
