import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {OrderDto, OrderGroupDetailDto} from 'src/app/dtos/order';
import {FormsModule} from "@angular/forms";
import {CurrencyPipe, DatePipe, LowerCasePipe, NgForOf, NgIf, TitleCasePipe} from "@angular/common";
import { TicketService } from 'src/app/services/ticket.service';
import {PdfExportService} from "../../../services/pdf-export.service";
import {ToastrService} from "ngx-toastr";


@Component({
  selector: 'app-purchased-order-detail',
  templateUrl: './purchased-order-detail.component.html',
  standalone: true,
  imports: [
    FormsModule,
    CurrencyPipe,
    DatePipe,
    NgIf,
    NgForOf,
    LowerCasePipe,
    TitleCasePipe
  ],
  styleUrls: ['./purchased-order-detail.component.scss']
})
export class PurchasedOrderDetailComponent implements OnInit {
  group: OrderGroupDetailDto | null = null;
  selected: { [ticketId: number]: boolean } = {};
  isLoading = true;
  showConfirmModal = false;
  tabFromParent: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private ticketService: TicketService,
    private pdfService: PdfExportService,
    private toastr: ToastrService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const groupId = Number(this.route.snapshot.paramMap.get('id'));
    this.ticketService.getOrderGroupDetails(groupId).subscribe({
      next: res => {
        this.group = res;
        this.isLoading = false;
      },
      error: err => {
        console.error('Failed to load order group', err);
        this.isLoading = false;
      }
    });
  }

  refundSelected(): void {
    const ticketIds = this.getSelectedTicketIds();
    if (ticketIds.length === 0) return;

    this.ticketService.refundTickets(ticketIds).subscribe({
      next: refunded => {
        this.selected = {};
        this.ngOnInit();
        this.toastr.success('Tickets successfully refunded!', 'Refund Complete');
      },
      error: err => {
        console.error('Refund failed', err);
        alert('Refund failed');
      }
    });
  }


  openRefundConfirm(): void {
    if (this.hasSelection()) {
      this.showConfirmModal = true;
    }
  }

  confirmRefund(): void {
    this.refundSelected();
    this.showConfirmModal = false;
  }

  cancelRefund(): void {
    this.showConfirmModal = false;
  }

  getSelectedTicketIds(): number[] {
    return Object.keys(this.selected)
      .filter(id => this.selected[+id])
      .map(id => +id);
  }

  hasSelection(): boolean {
    return Object.values(this.selected).some(v => v);
  }

  get tickets() {
    return this.group?.tickets ?? [];
  }

  get orders() {
    return [...(this.group?.orders ?? [])].sort(
      (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
    );
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

  exportTicket(ticketId: number): void {
    this.pdfService.exportTicketPdf(ticketId);
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
