import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PaymentItem } from "../../dtos/payment-item";
import { CartService } from 'src/app/services/cart.service';
import { TEST_PAYMENT_ITEMS } from './test-payment-data'; // Import test data

@Component({
  selector: 'app-payment-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './payment-form.component.html',
  styleUrls: ['./payment-form.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaymentFormComponent implements OnInit {
  items: PaymentItem[] = [];

  paymentForm!: FormGroup;

  constructor(private fb: FormBuilder, private cart: CartService) {}

  ngOnInit(): void {
    // use this in prod
    //this.items = this.cart.getItems();

    // for testing purposes, use test data
    this.items = TEST_PAYMENT_ITEMS;
    this.paymentForm = this.fb.group({
      cardNumber: [
        '',
        [
          Validators.required,
          Validators.pattern(/^\d{13,19}$/), // 13–19 digits
        ],
      ],
      expirationDate: [
        '',
        [
          Validators.required,
          Validators.pattern(/^(0[1-9]|1[0-2])\/?([0-9]{2})$/),
        ],
      ],
      securityCode: [
        '',
        [
          Validators.required,
          Validators.pattern(/^\d{3,4}$/),
        ],
      ],

      country: ['', Validators.required],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      address: ['', Validators.required],
      postalCode: [
        '',
        [
          Validators.required,
          Validators.pattern(/^\d{4,10}$/),
        ],
      ],
      city: ['', Validators.required],
    });
  }

  get total(): number {
    return this.items.reduce((sum, it) => {
      const qty = it.type === 'STANDING' ? (it.quantity ?? 0) : 1;
      return sum + it.price * qty;
    }, 0);
  }

  onSubmit(): void {
    if (this.paymentForm.invalid) {
      this.paymentForm.markAllAsTouched();
      return;
    }
    // TODO: emit or call payment API
    console.log('Paying with', this.paymentForm.value, 'for items', this.items);
  }
}
