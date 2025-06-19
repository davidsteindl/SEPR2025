import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PaymentItem } from "../../dtos/payment-item";
import { CartService } from 'src/app/services/cart.service';
import { ToastrService } from 'ngx-toastr';
import { Router } from '@angular/router';
import { TicketService } from 'src/app/services/ticket.service';
import { OrderGroupDto } from 'src/app/dtos/order';
import { TicketRequestDto } from 'src/app/dtos/ticket';
import {UserService} from "../../services/user.service";
import {User} from "../../dtos/user";

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
  loading = false;
  private readonly FORM_STORAGE_KEY = 'paymentFormData';

  constructor(
    private fb: FormBuilder,
    private cart: CartService,
    private ticketService: TicketService,
    private toastr: ToastrService,
    private router: Router,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    // use this in prod
    this.items = this.cart.getItems();

    // for testing purposes, use test data
    //this.items = TEST_PAYMENT_ITEMS;

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
      street: ['', Validators.required],
      housenumber: ['', Validators.required],
      postalCode: ['', Validators.required],
      city: ['', Validators.required],
    });

    const saved = localStorage.getItem(this.FORM_STORAGE_KEY);
    if (saved) {
      this.paymentForm.patchValue(JSON.parse(saved));
    }

    this.paymentForm.valueChanges.subscribe(val => {
      localStorage.setItem(this.FORM_STORAGE_KEY, JSON.stringify(val));
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

    this.loading = true;

    const reservedTicketIds = this.cart.getReservedTicketIds();

    if (reservedTicketIds && reservedTicketIds.length > 0) {
      const payload: TicketRequestDto = {
        ...this.paymentForm.value,
        showId: this.items[0].showId,
        reservedTicketIds
      };

      this.ticketService.buyReservedTickets(payload)
        .subscribe(this.buildHandler())
        .add(() => (this.loading = false));

    } else {
      this.ticketService.buyTickets(this.items[0].showId, this.items, this.paymentForm.value)
        .subscribe(this.buildHandler())
        .add(() => (this.loading = false));
    }
  }

  private buildHandler() {
    return {
      next: (group: OrderGroupDto) => {
        const order = group.orders[0];
        const dt = new Date(order.createdAt);
        this.toastr.success(
          `Order #${group.id} placed on ${dt.toLocaleString()}`,
          'Payment Complete'
        );
        localStorage.removeItem(this.FORM_STORAGE_KEY);
        this.cart.clearReservedTicketIds?.();
        this.cart.clear();
        this.router.navigate(['/orders']);
      },
      error: (err: any) => {
        const backendErrors = err?.error;

        if (backendErrors?.errors && Array.isArray(backendErrors.errors)) {
          backendErrors.errors.forEach((e: string) => {
            this.toastr.error(e, 'Validation Error');
          });
        } else if (backendErrors?.message) {
          this.toastr.error(backendErrors.message, 'Payment Failed');
        } else {
          this.toastr.error('Error occurred', 'Error');
        }

        console.error('Backend error:', err);
      }
    };
  }


  copyAddressFromProfile(event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    if (!checked) return;

    this.userService.getCurrentUser().subscribe({
      next: (user: User) => {
        this.paymentForm.patchValue({
          firstName: user.firstName || '',
          lastName: user.lastName || '',
          street: user.street || '',
          housenumber: user.housenumber || '',
          postalCode: user.postalCode || '',
          city: user.city || '',
          country: user.country || ''
        });
      },
      error: () => {
        this.toastr.error('Could not load address from profile', 'Error');
      }
    });
  }
}
