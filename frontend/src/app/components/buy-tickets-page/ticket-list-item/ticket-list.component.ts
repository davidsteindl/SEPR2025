import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { PaymentItem } from 'src/app/dtos/payment-item';

@Component({
  selector: 'app-ticket-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ticket-list.component.html',
  styleUrls: ['./ticket-list.component.scss']
})
export class TicketListComponent {
  @Input() items: PaymentItem[] = [];
  @Output() remove = new EventEmitter<PaymentItem>();

  onRemove(item: PaymentItem) {
    this.remove.emit(item);
  }

  computeTotalPrice(item: PaymentItem): number {
    if (item.type === 'STANDING') {
      return (item.quantity ?? 1) * item.price;
    }
    return item.price;
  }
}
