import {Component, OnInit} from '@angular/core';
import {OrderDto} from "../../../dtos/order";
import {ActivatedRoute, RouterLink} from "@angular/router";
import {TicketService} from "../../../services/ticket.service";
import {OrderService} from "../../../services/order.service";
import {CurrencyPipe, DatePipe, NgForOf, NgIf} from "@angular/common";
import {FormsModule} from "@angular/forms";

@Component({
  selector: 'app-reserved-order-detail',
  imports: [
    CurrencyPipe,
    FormsModule,
    RouterLink,
    DatePipe,
    NgIf,
    NgForOf
  ],
  templateUrl: './reserved-order-detail.component.html',
  standalone: true,
  styleUrl: './reserved-order-detail.component.scss'
})
export class ReservedOrderDetailComponent implements OnInit {
  order: OrderDto | null = null;
  selected: { [ticketId: number]: boolean } = {};
  isLoading = true;
  showConfirmModal = false;
  confirmAction: 'buy' | 'cancel' | null = null;

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
        console.error('Failed to load order', err);
        this.isLoading = false;
      }
    });
  }

  buySelected(): void {
    const ticketIds = this.getSelectedIds();
    this.ticketService.buyReservedTickets(this.order!.id, ticketIds).subscribe(() => {
      this.loadOrder(this.order!.id);
    });
  }

  cancelSelected(): void {
    const ticketIds = this.getSelectedIds();
    this.ticketService.cancelReservations(ticketIds).subscribe(() => {
      this.loadOrder(this.order!.id);
    });
  }

  getSelectedIds(): number[] {
    return Object.keys(this.selected)
      .filter(id => this.selected[+id])
      .map(id => +id);
  }

  hasSelection(): boolean {
    return Object.values(this.selected).some(selected => selected);
  }


  openConfirm(action: 'buy' | 'cancel'): void {
    if (this.hasSelection()) {
      this.confirmAction = action;
      this.showConfirmModal = true;
    }
  }

  confirmBuy(): void {
    this.buySelected();
    this.cancelConfirm();
  }

  confirmCancel(): void {
    this.cancelSelected();
    this.cancelConfirm();
  }

  cancelConfirm(): void {
    this.showConfirmModal = false;
    this.confirmAction = null;
  }

}
