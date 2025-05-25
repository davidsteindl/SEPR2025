import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { OrderService } from '../../../services/order.service';
import { OrderDto } from '../../../dtos/order';
import { CurrencyPipe, DatePipe, NgIf, NgForOf } from '@angular/common';

@Component({
  selector: 'app-refunded-order-detail',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, RouterLink, NgIf, NgForOf],
  templateUrl: './refunded-order-detail.component.html',
  styleUrl: './refunded-order-detail.component.scss'
})
export class RefundedOrderDetailComponent implements OnInit {
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
        console.error('Failed to load refunded order', err);
        this.isLoading = false;
      }
    });
  }
}
