import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { PaymentItem } from '../dtos/payment-item';

// this is used to get data from the graphical seat selection to the payment form

@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly CART_KEY = 'cartItems';
  private _items = new BehaviorSubject<PaymentItem[]>([]);
  readonly items$ = this._items.asObservable();
  private reservedTicketIds: number[] | null = null;

  constructor() {
    const saved = localStorage.getItem(this.CART_KEY);
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        this._items.next(parsed);
      } catch (e) {
        console.warn('Could not parse saved cart items:', e);
        localStorage.removeItem(this.CART_KEY);
      }
    }
  }

  setItems(items: PaymentItem[]) {
    this._items.next(items);
    localStorage.setItem(this.CART_KEY, JSON.stringify(items));
    console.log('Cart items updated:', items);
  }

  getItems(): PaymentItem[] {
    return this._items.value;
  }

  clear() {
    this._items.next([]);
    localStorage.removeItem(this.CART_KEY);
  }

  setReservedTicketIds(ids: number[]): void {
    this.reservedTicketIds = ids;
  }

  getReservedTicketIds(): number[] | null {
    return this.reservedTicketIds;
  }

  clearReservedTicketIds(): void {
    this.reservedTicketIds = null;
  }
}
