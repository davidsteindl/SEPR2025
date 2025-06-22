import {Component, OnInit} from '@angular/core';
import {OrderGroupDetailDto} from "../../../dtos/order";
import {ActivatedRoute} from "@angular/router";
import {TicketService} from "../../../services/ticket.service";
import {OrderService} from "../../../services/order.service";
import {CurrencyPipe, DatePipe, LowerCasePipe, NgForOf, NgIf, TitleCasePipe} from "@angular/common";
import {FormsModule} from "@angular/forms";
import { CartService } from 'src/app/services/cart.service';
import { Router } from '@angular/router';
import { PaymentItem } from 'src/app/dtos/payment-item';


@Component({
  selector: 'app-reserved-order-detail',
  imports: [
    CurrencyPipe,
    FormsModule,
    DatePipe,
    NgIf,
    NgForOf,
    LowerCasePipe,
    TitleCasePipe
  ],
  templateUrl: './reserved-order-detail.component.html',
  standalone: true,
  styleUrl: './reserved-order-detail.component.scss'
})
export class ReservedOrderDetailComponent implements OnInit {
  group: OrderGroupDetailDto | null = null;
  selected: { [ticketId: number]: boolean } = {};
  isLoading = true;
  showConfirmModal = false;
  confirmAction: 'buy' | 'cancel' | null = null;
  tabFromParent: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private orderService: OrderService,
    private ticketService: TicketService,
    private cartService: CartService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.tabFromParent = this.route.snapshot.queryParamMap.get('tab');
    const groupId = Number(this.route.snapshot.paramMap.get('id'));
    this.ticketService.getOrderGroupDetails(groupId).subscribe({
      next: (res) => {
        this.group = res;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load order group', err);
        this.isLoading = false;
      }
    });
  }


  buySelected(): void {
    const items = this.toPaymentItems();
    const reservedIds = this.getSelectedIds();
    this.cartService.setItems(items);
    this.cartService.setReservedTicketIds(reservedIds);
    this.router.navigate(['/checkout']);
  }

  toPaymentItems(): PaymentItem[] {
    if (!this.group) return [];

    return this.group.tickets
      .filter(t => this.selected[t.id])
      .map(t => ({
        eventName: this.group!.showName,
        type: t.seatId ? 'SEATED' : 'STANDING',
        price: t.price,
        sectorId: t.sectorId,
        seatId: t.seatId ?? undefined,
        quantity: t.seatId ? undefined : 1,
        showId: t.showId
      }));
  }

  cancelSelected(): void {
    const ticketIds = this.getSelectedIds();
    if (ticketIds.length === 0) {
      return;
    }
    this.ticketService.cancelReservations(ticketIds).subscribe(() => {
      ticketIds.forEach(id => delete this.selected[id]);
      this.ngOnInit();
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

  goBack(): void {
    this.router.navigate(['/orders'], {
      queryParams: this.tabFromParent ? { tab: this.tabFromParent } : {}
    });
  }

}
