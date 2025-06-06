import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { ShowService } from 'src/app/services/show.service';
import { Show } from 'src/app/dtos/show';
import { Room } from 'src/app/dtos/room';
import { PaymentItem } from 'src/app/dtos/payment-item';
import { CartService } from 'src/app/services/cart.service';
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
onReserveTickets() {
throw new Error('Method not implemented.');
}
  showId!: number;
  show!: Show;
  room!: Room;
  loadingShow = true;
  loadingRoom = true;

  // subscription to cart items
  cartSub!: Subscription;
  currentItems: PaymentItem[] = [];

  constructor(
    private route: ActivatedRoute,
    private showService: ShowService,
    private cartService: CartService,
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

  // Called whenever SeatMapComponent emits a new Selection (PaymentItem)
  onSeatOrStandingSelected(item: PaymentItem) {
    // Check if that exact PaymentItem is already in the cart
    // For seated seats: match on seatId and sectorId and showId
    // For standing: match on sectorId and showId
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
        // not in cart yet -> add
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
        // Already in cart as standing. we just replace quantity
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
    // We assume the cart is already up-to-date in CartService,
    // so just navigate to /checkout
    this.router.navigate(['/checkout']);
  }
}
