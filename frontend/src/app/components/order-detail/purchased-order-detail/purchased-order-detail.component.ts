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
  tickets: TicketDto[] = [];
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
    this.loadTickets(orderId);
  }

  loadOrder(orderId: number): void {
    this.orderService.getOrderById(orderId).subscribe({
      next: order => this.order = order,
      error: err => console.error('Failed to load order', err)
    });
  }

  loadTickets(orderId: number): void {
    this.orderService.getTicketsForOrder(orderId, 0, 50).subscribe({
      next: page => {
        this.tickets = page.content;
        this.isLoading = false;
      },
      error: err => {
        console.error('Failed to load tickets', err);
        this.isLoading = false;
      }
    });
  }

  refundSelected(): void {
    const ticketIds = Object.keys(this.selected)
      .filter(id => this.selected[+id])
      .map(id => +id);

    if (ticketIds.length === 0) return;

    this.ticketService.refundTickets(ticketIds).subscribe({
      next: () => {
        this.selected = {};
        this.loadTickets(this.order?.id!);
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
    const ticketIds = Object.keys(this.selected)
      .filter(id => this.selected[+id])
      .map(id => +id);

    this.ticketService.refundTickets(ticketIds).subscribe(() => {
      this.selected = {};
      this.showConfirmModal = false;
      this.loadTickets(this.order!.id);
    });
  }

  cancelRefund(): void {
    this.showConfirmModal = false;
  }


  hasSelection(): boolean {
    return Object.values(this.selected).some(v => v);
  }

  downloadPdf(ticketId: number): void {
  }
}
