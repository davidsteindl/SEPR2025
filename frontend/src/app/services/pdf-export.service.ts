import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Globals } from '../global/globals';

@Injectable({
  providedIn: 'root'
})
export class PdfExportService {
  private pdfBaseUri: string;

  constructor(private http: HttpClient, private globals: Globals) {
    this.pdfBaseUri = this.globals.backendUri + '/pdf-export';
  }

  exportTicketPdf(ticketId : number): void {
    this.http.get(this.pdfBaseUri + `/tickets/${ticketId}`, { responseType: 'blob' }).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `Ticket${ticketId}.pdf`;
      a.click();
      window.URL.revokeObjectURL(url);
    });
  }

  exportInvoicePdf(orderId : number): void {
    this.http.get(this.pdfBaseUri + `/invoice/${orderId}`, { responseType: 'blob' }).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `Invoice${orderId}.pdf`;
      a.click();
      window.URL.revokeObjectURL(url);
    });
  }

  exportCancelInvoicePdf(orderId : number): void {
    this.http.get(this.pdfBaseUri + `/cancelinvoice/${orderId}`, { responseType: 'blob' }).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `CancellationInvoice${orderId}.pdf`;
      a.click();
      window.URL.revokeObjectURL(url);
    });
  }

}
