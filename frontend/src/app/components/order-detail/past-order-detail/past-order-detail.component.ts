import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import {OrderDto, OrderGroupDetailDto} from '../../../dtos/order';
import { CurrencyPipe, DatePipe, NgIf, NgForOf } from '@angular/common';
import {TicketService} from "../../../services/ticket.service";
import {PdfExportService} from "../../../services/pdf-export.service";

@Component({
  selector: 'app-past-order-detail',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, RouterLink, NgIf, NgForOf],
  templateUrl: './past-order-detail.component.html',
  styleUrl: './past-order-detail.component.scss'
})
export class PastOrderDetailComponent implements OnInit {
  group: OrderGroupDetailDto | null = null;
  isLoading = true;

  constructor(
    private route: ActivatedRoute,
    private ticketService: TicketService,
    private pdfService: PdfExportService,
  ) {}

  ngOnInit(): void {
    const groupId = Number(this.route.snapshot.paramMap.get('id'));
    this.ticketService.getOrderGroupDetails(groupId).subscribe({
      next: res => {
        this.group = res;
        this.isLoading = false;
      },
      error: err => {
        console.error('Failed to load past order group', err);
        this.isLoading = false;
      },
    });
  }

  get sortedOrders(): OrderDto[] {
    return [...(this.group?.orders ?? [])].sort((a, b) =>
      new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
    );
  }

  getOrderLabel(order: OrderDto, index: number): string {
    switch (order.orderType) {
      case 'ORDER': return `Order ${index + 1}`;
      case 'REFUND': return `Refund ${index + 1}`;
      default: return `Order`;
    }
  }


  downloadPdf(ticketId: number): void {
    console.log(`Download PDF for ticket ${ticketId}`);
  }

  exportAnyInvoice(order: OrderDto): void {
    const isCancellation = order.orderType !== 'ORDER';

    if (isCancellation) {
      this.pdfService.exportCancelInvoicePdf(order.id);
    } else {
      this.pdfService.exportInvoicePdf(order.id);
    }
  }
}
