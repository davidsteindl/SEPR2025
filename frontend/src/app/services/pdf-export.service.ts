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

  exportPdf(data: any): void {
    this.http.post(this.pdfBaseUri, data, { responseType: 'blob' }).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${data.type}.pdf`;
      a.click();
      window.URL.revokeObjectURL(url);
    });
  }
}
