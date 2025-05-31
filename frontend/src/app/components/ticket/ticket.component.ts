import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { PdfExportService } from '../../services/pdf-export.service';
import { ToastrService } from 'ngx-toastr';
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-ticket',
  templateUrl: './ticket.component.html',
  styleUrls: ['./ticket.component.scss'],
  imports: [NgIf]
})
export class TicketComponent implements OnInit {

  ticketPdfUrl: SafeResourceUrl | null = null;

  constructor(
    private route: ActivatedRoute,
    private sanitizer: DomSanitizer,
    private pdfExportService: PdfExportService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    const ticketId = Number(this.route.snapshot.paramMap.get('id'));
    const randomTicketCode = this.route.snapshot.paramMap.get('randomTicketCode');
    if (!ticketId) {
      this.toastr.error('Ungültige Ticket-ID', 'Fehler');
      return;
    }

    this.pdfExportService.getTicketPdfBlob(ticketId, randomTicketCode).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        this.ticketPdfUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);
      },
      error: () => {
        this.toastr.error('PDF konnte nicht geladen werden.', 'Fehler');
      }
    });
  }
}
