import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { OrderService } from 'src/app/services/order.service';
import { OrderDto } from 'src/app/dtos/order';
import { TicketDto } from 'src/app/dtos/ticket';
import {FormsModule} from "@angular/forms";
import {CurrencyPipe, DatePipe} from "@angular/common";

@Component({
  selector: 'app-purchased-order-detail',
  templateUrl: './purchased-order-detail.component.html',
  standalone: true,
  imports: [
    FormsModule,
    CurrencyPipe,
    DatePipe
  ],
  styleUrls: ['./purchased-order-detail.component.scss']
})
export class PurchasedOrderDetailComponent implements OnInit {
  order: OrderDto | null = null;
  tickets: TicketDto[] = [];
  selected: { [ticketId: number]: boolean } = {};
  isLoading = true;

  constructor(
    private route: ActivatedRoute,
    private orderService: OrderService
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
    alert('Refund not implemented yet');
  }


  downloadPdf(ticketId: number): void {
    alert('PDF download not implemented yet');
  }
}
