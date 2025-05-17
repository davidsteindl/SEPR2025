import {ChangeDetectorRef, Component, OnInit, TemplateRef, ViewChild, ViewChildren} from '@angular/core';
import {MessageService} from '../../services/message.service';
import {Message} from '../../dtos/message';
import {Event} from '../../dtos/event';
import {NgbModal, NgbPaginationConfig} from '@ng-bootstrap/ng-bootstrap';
import {UntypedFormBuilder, NgForm} from '@angular/forms';
import {AuthService} from '../../services/auth.service';
import {EventService} from "../../services/event.service";

@Component({
  selector: 'app-message',
  templateUrl: './message.component.html',
  styleUrls: ['./message.component.scss'],
  standalone: false
})
export class MessageComponent implements OnInit {

  error = false;
  errorMessage = '';
  // After first submission attempt, form validation will start
  submitted = false;

  currentMessage: Message;

  private message: Message[];

  selectedCategory: string = 'All';
  categories: string[];

  allEvents: Event[] = [
    {
      id: 1,
      name: "Title 1",
      description: 'description 1',
      duration: 60,
      date: 'Date 1',
      soldTickets: 200,
      category: 'Music',
      locationId: 1
    },
    {
      id: 2,
      name: "Title 2",
      description: 'description 2',
      duration: 60,
      date: 'Date 2',
      soldTickets: 190,
      category: 'Sport',
      locationId: 2
    },
    {
      id: 3,
      name: "Title 3",
      description: 'description 3',
      duration: 60,
      date: 'Date 3',
      soldTickets: 195,
      category: 'Sport',
      locationId: 3
    },
  ];


  constructor(private messageService: MessageService,
              private eventService: EventService,
              private ngbPaginationConfig: NgbPaginationConfig,
              private formBuilder: UntypedFormBuilder,
              private cd: ChangeDetectorRef,
              private authService: AuthService,
              private modalService: NgbModal) {
  }

  ngOnInit() {
    this.loadMessage();
    this.loadCategories();
  }

  /**
   * Returns true if the authenticated user is an admin
   */
  isAdmin(): boolean {
    return this.authService.getUserRole() === 'ADMIN';
  }

  openAddModal(messageAddModal: TemplateRef<any>) {
    this.currentMessage = new Message();
    this.modalService.open(messageAddModal, {ariaLabelledBy: 'modal-basic-title'});
  }

  openExistingMessageModal(id: number, messageAddModal: TemplateRef<any>) {
    this.messageService.getMessageById(id).subscribe({
      next: res => {
        this.currentMessage = res;
        this.modalService.open(messageAddModal, {ariaLabelledBy: 'modal-basic-title'});
      },
      error: err => {
        this.defaultServiceErrorHandling(err);
      }
    });
  }

  /**
   * Starts form validation and builds a message dto for sending a creation request if the form is valid.
   * If the procedure was successful, the form will be cleared.
   */
  addMessage(form) {
    this.submitted = true;


    if (form.valid) {
      this.currentMessage.publishedAt = new Date().toISOString();
      this.createMessage(this.currentMessage);
      this.clearForm();
    }
  }

  getMessage(): Message[] {
    return this.message;
  }

  /**
   * Error flag will be deactivated, which clears the error message
   */
  vanishError() {
    this.error = false;
  }

  /**
   * Sends message creation request
   *
   * @param message the message which should be created
   */
  private createMessage(message: Message) {
    this.messageService.createMessage(message).subscribe({
        next: () => {
          this.loadMessage();
        },
        error: error => {
          this.defaultServiceErrorHandling(error);
        }
      }
    );
  }

  /**
   * Loads the specified page of message from the backend
   */
  private loadMessage() {
    this.messageService.getMessage().subscribe({
      next: (message: Message[]) => {
        this.message = message;
      },
      error: error => {
        this.defaultServiceErrorHandling(error);
      }
    });
  }


  private defaultServiceErrorHandling(error: any) {
    console.log(error);
    this.error = true;
    if (typeof error.error === 'object') {
      this.errorMessage = error.error.error;
    } else {
      this.errorMessage = error.error;
    }
  }

  private clearForm() {
    this.currentMessage = new Message();
    this.submitted = false;
  }

  get topEvents() {
    let filteredEvents = this.selectedCategory === 'All'
      ? this.allEvents
      : this.allEvents.filter(event => event.category === this.selectedCategory);

    filteredEvents = filteredEvents.sort((a, b) => b.soldTickets - a.soldTickets);
    return filteredEvents.slice(0, 10);
  }

  private loadCategories() {
    this.eventService.getCategories().subscribe({
      next: (categories: string[]) => {
        this.categories = categories;
      },
      error: error => {
        this.defaultServiceErrorHandling(error);
      }
    });
  }


}
