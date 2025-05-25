import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { OrderDto } from '../../../dtos/order';
import { OrderService } from '../../../services/order.service';
import { CurrencyPipe, DatePipe, NgIf, NgForOf } from '@angular/common';

@Component({
  selector: 'app-past-order-detail',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, RouterLink, NgIf, NgForOf],
  templateUrl: './past-order-detail.component.html',
  styleUrl: './past-order-detail.component.scss'
})
export class PastOrderDetailComponent implements OnInit {
  order: OrderDto | null = null;
  isLoading = true;

  constructor(
    private route: ActivatedRoute,
    private orderService: OrderService
  ) {}

  ngOnInit(): void {
    const orderId = Number(this.route.snapshot.paramMap.get('id'));
    this.orderService.getOrderWithTickets(orderId).subscribe({
      next: order => {
        this.order = order;
        this.isLoading = false;
      },
      error: err => {
        console.error('Failed to load past order', err);
        this.isLoading = false;
      }
    });
  }

  downloadPdf(ticketId: number): void {
    console.log(`Download PDF for ticket ${ticketId}`);
  }
}
