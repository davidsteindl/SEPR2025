import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { PaymentItem } from '../dtos/payment-item';

// this is used to get data from the graphical seat selection to the payment form

@Injectable({ providedIn: 'root' })
export class CartService {
  private _items = new BehaviorSubject<PaymentItem[]>([]);
  readonly items$ = this._items.asObservable();

  setItems(items: PaymentItem[]) {
    this._items.next(items);
  }

  getItems(): PaymentItem[] {
    return this._items.value;
  }

  clear() {
    this._items.next([]);
  }
}