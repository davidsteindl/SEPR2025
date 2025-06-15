import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { ShowService } from 'src/app/services/show.service';
import { Show } from 'src/app/dtos/show';
import { Room } from 'src/app/dtos/room';
import { PaymentItem } from 'src/app/dtos/payment-item';
import { CartService } from 'src/app/services/cart.service';
import { TicketService } from 'src/app/services/ticket.service';
import { ToastrService } from 'ngx-toastr';
import { SeatMapComponent } from "./seat-map/seat-map.component";
import { TicketListComponent } from "./ticket-list-item/ticket-list.component";
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-buy-tickets-page',
  standalone: true,
  imports: [CommonModule, SeatMapComponent, TicketListComponent],
  templateUrl: './buy-tickets-page.component.html',
  styleUrls: ['./buy-tickets-page.component.scss']
})
export class BuyTicketsPageComponent implements OnInit, OnDestroy {

  showId!: number;
  show!: Show;
  room!: Room;
  loadingShow = true;
  loadingRoom = true;

  cartSub!: Subscription;
  currentItems: PaymentItem[] = [];
  showConfirmModal = false;

  constructor(
    private route: ActivatedRoute,
    private showService: ShowService,
    private cartService: CartService,
    private ticketService: TicketService,
    private toastr: ToastrService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Read showId from route parameter
    this.route.paramMap.subscribe((params: ParamMap) => {
      const idStr = params.get('id');
      if (idStr) {
        this.showId = +idStr;

        // load Show
        this.loadingShow = true;
        console.log("FETCHING SHOWS")
        this.showService.getShowById(this.showId).subscribe({

          next: (s: Show) => {
            this.show = s;
            this.loadingShow = false;

            // once we have the show, load its Room plan
            this.loadingRoom = true;
            this.showService.getRoomUsage(this.showId).subscribe({
              next: (r: Room) => {
                this.room = r;
                this.loadingRoom = false;
              },
              error: err => {
                console.error('Error fetching room usage', err);
                this.loadingRoom = false;
              }
            });
          },
          error: err => {
            console.error('Error fetching show', err);
            this.loadingShow = false;
          }
        });
      }
    });

    // subscribe to changes in the CartService
    this.cartSub = this.cartService.items$.subscribe(items => {
      this.currentItems = items;
    });
  }

  ngOnDestroy(): void {
    if (this.cartSub) {
      this.cartSub.unsubscribe();
    }
  }

  onSeatOrStandingSelected(item: PaymentItem) {
    let items = [...this.currentItems];
    const isSeated = item.type === 'SEATED';
    if (isSeated) {
      const idx = items.findIndex(
        i =>
          i.type === 'SEATED' &&
          i.showId === item.showId &&
          i.sectorId === item.sectorId &&
          i.seatId === item.seatId
      );
      if (idx === -1) {
        items.push(item);
      }
    } else {
      // standing
      const idx = items.findIndex(
        i =>
          i.type === 'STANDING' &&
          i.showId === item.showId &&
          i.sectorId === item.sectorId
      );
      if (idx === -1) {
        items.push(item);
      } else {
        // Already in cart as standing, just replace quantity
        items[idx] = item;
      }
    }

    this.cartService.setItems(items);
  }


  // Called by TicketListComponent when user removes an item
  onRemoveItem(item: PaymentItem) {
    let items = [...this.currentItems];
    if (item.type === 'SEATED') {
      items = items.filter(
        i =>
          !(
            i.type === 'SEATED' &&
            i.showId === item.showId &&
            i.sectorId === item.sectorId &&
            i.seatId === item.seatId
          )
      );
    } else {
      items = items.filter(
        i =>
          !(
            i.type === 'STANDING' &&
            i.showId === item.showId &&
            i.sectorId === item.sectorId
          )
      );
    }
    this.cartService.setItems(items);
  }

  onBuyTickets() {
    const items = this.cartService.getItems();
    if (!items || items.length === 0) {
      this.toastr.warning('Your cart is empty. Please select tickets first.');
      return;
    }
    this.router.navigate(['/checkout']);
  }

  onReserveTickets(): void {
    const items = this.cartService.getItems();
    if (!items || items.length === 0) {
      this.toastr.warning('Your cart is empty. Please select tickets first.');
      return;
    }
    this.ticketService.reserveTickets(this.showId, items).subscribe({
      next: () => {
        this.toastr.success('Tickets reserved successfully!');
        this.cartService.clear();
        this.router.navigate(['/orders'], { queryParams: { tab: 'reservations' } });
      },
      error: err => {
        const msg = err.error?.message ?? err.message ?? 'Unknown error';
        this.toastr.error(`Failed to reserve tickets: ${msg}`);
      }
    });
  }

  openReservationConfirm(): void {
    this.showConfirmModal = true;
  }

  confirmReservation(): void {
    this.onReserveTickets();
    this.showConfirmModal = false;
  }

  cancelReservation(): void {
    this.showConfirmModal = false;
  }
}
