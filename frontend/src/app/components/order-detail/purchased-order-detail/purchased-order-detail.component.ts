import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, RouterLink} from '@angular/router';
import { OrderService } from 'src/app/services/order.service';
import { OrderDto } from 'src/app/dtos/order';
import { TicketDto } from 'src/app/dtos/ticket';
import {FormsModule} from "@angular/forms";
import {CurrencyPipe, DatePipe, NgForOf, NgIf} from "@angular/common";
import { TicketService } from 'src/app/services/ticket.service';


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
    RouterLink
  ],
  styleUrls: ['./purchased-order-detail.component.scss']
})
export class PurchasedOrderDetailComponent implements OnInit {
  order: OrderDto | null = null;
  selected: { [ticketId: number]: boolean } = {};
  isLoading = true;
  showConfirmModal = false;

  constructor(
    private route: ActivatedRoute,
    private orderService: OrderService,
    private ticketService: TicketService
  ) {}

  ngOnInit(): void {
    const orderId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadOrder(orderId);
  }

  loadOrder(orderId: number): void {
    this.orderService.getOrderWithTickets(orderId).subscribe({
      next: order => {
        this.order = order;
        this.isLoading = false;
      },
      error: err => {
        console.error('Failed to load order with tickets', err);
        this.isLoading = false;
      }
    });
  }


  refundSelected(): void {
    const ticketIds = this.getSelectedTicketIds();
    if (ticketIds.length === 0) return;

    this.ticketService.refundTickets(ticketIds).subscribe({
      next: refundedTickets => {
        this.selected = {};
        refundedTickets.forEach(ref => {
          const t = this.order!.tickets.find(t => t.id === ref.id);
          if (t) t.status = ref.status;
        });
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

  downloadPdf(ticketId: number): void {
    // Not implemented
  }
}
