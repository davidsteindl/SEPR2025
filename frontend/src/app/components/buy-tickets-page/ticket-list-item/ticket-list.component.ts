import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { PaymentItem } from 'src/app/dtos/payment-item';
import { Room } from "src/app/dtos/room";

@Component({
  selector: "app-ticket-list",
  standalone: true,
  imports: [CommonModule],
  templateUrl: "./ticket-list.component.html",
  styleUrls: ["./ticket-list.component.scss"],
})
export class TicketListComponent {
  @Input() items: PaymentItem[] = [];
  @Input() room?: Room;
  @Output() remove = new EventEmitter<PaymentItem>();

  onRemove(item: PaymentItem) {
    this.remove.emit(item);
  }

  computeTotalPrice(item: PaymentItem): number {
    if (item.type === "STANDING") {
      return (item.quantity ?? 1) * item.price;
    }
    return item.price;
  }

  getSectorName(sectorId: number): string {
    if (!this.room || !this.room.sectors) return sectorId + "";
    const sector = this.room.sectors.find((s) => s.id === sectorId);
    return sector && sector.name
      ? sector.name
      : sector
      ? sector.id + ""
      : sectorId + "";
  }
}
