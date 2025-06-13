import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {OrderDto, OrderGroupDetailDto} from '../../../dtos/order';
import {CurrencyPipe, DatePipe, NgIf, NgForOf, TitleCasePipe, LowerCasePipe} from '@angular/common';
import {TicketService} from "../../../services/ticket.service";
import {PdfExportService} from "../../../services/pdf-export.service";

@Component({
  selector: 'app-past-order-detail',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, NgIf, NgForOf, TitleCasePipe, LowerCasePipe],
  templateUrl: './past-order-detail.component.html',
  styleUrl: './past-order-detail.component.scss'
})
export class PastOrderDetailComponent implements OnInit {
  group: OrderGroupDetailDto | null = null;
  isLoading = true;
  tabFromParent: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private ticketService: TicketService,
    private pdfService: PdfExportService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.tabFromParent = this.route.snapshot.queryParamMap.get('tab');

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
    const orders = this.sortedOrders;

    let orderCount = 0;
    let refundCount = 0;

    for (let i = 0; i <= index; i++) {
      if (orders[i].orderType === 'ORDER') {
        orderCount++;
      } else if (orders[i].orderType === 'REFUND') {
        refundCount++;
      }
    }

    const isLast = index === orders.length - 1;

    if (order.orderType === 'ORDER') {
      if (orderCount === 1) {
        return 'Original Order';
      } else if (isLast) {
        return 'Current Order';
      } else {
        return `Updated Order #${orderCount - 1}`;
      }
    } else if (order.orderType === 'REFUND') {
      return `Refund #${refundCount}`;
    } else {
      return 'Order';
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

  goBack(): void {
    this.router.navigate(['/orders'], {
      queryParams: this.tabFromParent ? { tab: this.tabFromParent } : {}
    });
  }
}
