
import { PaymentItem } from "src/app/dtos/payment-item"; 

export const TEST_PAYMENT_ITEMS: PaymentItem[] = [
  {
    eventName: 'Rock Concert',
    type: 'SEATED',
    price: 49.95,
    sectorId: 1,
    rowNumber: 3,
    columnNumber: 4
  },
  {
    eventName: 'Pop Festival',
    type: 'STANDING',
    price: 29.99,
    sectorId: 2,
    quantity: 4
  },
  {
    eventName: 'Classical Evening',
    type: 'SEATED',
    price: 75.50,
    sectorId: 5,
    rowNumber: 1,
    columnNumber: 12
  },
  {
    eventName: 'Indie Night',
    type: 'STANDING',
    price: 19.00,
    sectorId: 3,
    quantity: 2
  }
];
