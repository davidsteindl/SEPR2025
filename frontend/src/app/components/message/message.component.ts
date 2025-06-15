import {ChangeDetectorRef, Component, OnInit, TemplateRef} from '@angular/core';
import {MessageService} from '../../services/message.service';
import {UserService} from "../../services/user.service";
import {Message, MessageCreate} from '../../dtos/message';
import {EventTopTenDto} from '../../dtos/event';
import {NgbModal, NgbPaginationConfig} from '@ng-bootstrap/ng-bootstrap';
import {UntypedFormBuilder} from '@angular/forms';
import {AuthService} from '../../services/auth.service';
import {EventService} from "../../services/event.service";
import {eventCategory} from "../../dtos/eventCategory";
import {Globals} from "../../global/globals";
import {DomSanitizer, SafeResourceUrl} from "@angular/platform-browser";

@Component({
  selector: 'app-message',
  templateUrl: './message.component.html',
  styleUrls: ['./message.component.scss'],
  standalone: false
})
export class MessageComponent implements OnInit {

  error = false;
  errorMessage = '';
  submitted = false;

  currentMessage: Message;
  currentImages: Map<number, SafeResourceUrl>;
  newsImages: File[] | undefined;

  private message: Message[];

  selectedCategory: string = 'All';
  categories: eventCategory[];
  topTenEvents: EventTopTenDto[];

  allMessagesRead = false;
  showAllMessages = false;

  constructor(private messageService: MessageService,
              private userService: UserService,
              private eventService: EventService,
              private ngbPaginationConfig: NgbPaginationConfig,
              private formBuilder: UntypedFormBuilder,
              private cd: ChangeDetectorRef,
              private authService: AuthService,
              private modalService: NgbModal,
              private globals: Globals,
              private sanitizer: DomSanitizer,) {
  }

  ngOnInit() {
    this.loadMessage();
    this.loadCategories();
    this.loadTopTen();
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
        this.currentImages = new Map();
        for (const {id} of this.currentMessage.images) {
          this.messageService.getImageBlob(this.currentMessage.id, id).subscribe({
            next: blob => {
              const url = URL.createObjectURL(blob);
              const ressource = this.sanitizer.bypassSecurityTrustResourceUrl(url);
              this.currentImages.set(id, ressource);
            }
          });
        }
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
      this.createMessage({...this.currentMessage, images: this.newsImages});
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
  private createMessage(message: MessageCreate) {
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

  private loadMessage() {
    const userId = this.authService.getUserId();
    if (this.showAllMessages) {
      this.messageService.getMessage().subscribe({
        next: (messages) => {
          this.message = messages;
          this.allMessagesRead = false;
        },
        error: error => this.defaultServiceErrorHandling(error)
      });
    } else {
      this.userService.getUnseenMessages(userId).subscribe({
        next: (messages) => {
          this.message = messages;
          this.allMessagesRead = messages.length === 0;
          if (messages.length > 0) {
            const messageIds = messages.map(m => m.id);
            this.userService.markMessagesAsSeen(userId, messageIds).subscribe();
          }
        },
        error: error => this.defaultServiceErrorHandling(error)
      });
    }
  }

  toggleShowAllMessages() {
    this.showAllMessages = !this.showAllMessages;
    this.loadMessage();
  }


  newsImage(imageId: number): SafeResourceUrl {
    return this.currentImages.get(imageId);
  }

  onFileSelected(event: Event) {
    const fileInput = event.target as HTMLInputElement;
    if (fileInput.files) {
      this.newsImages = [];
      for (const file of fileInput.files) {
        this.newsImages.push(file);
      }
    }
  }

  private defaultServiceErrorHandling(error: any) {
    console.log(error);
    this.error = true;
    if (typeof error.error === 'object') {
      this.errorMessage = error.error.error ?? error.error.title;
    } else {
      this.errorMessage = error.error;
    }
  }

  private clearForm() {
    this.currentMessage = new Message();
    this.submitted = false;
  }

  private loadCategories() {
    this.eventService.getCategories().subscribe({
      next: (categories: eventCategory[]) => {
        this.categories = categories;
      },
      error: error => {
        this.defaultServiceErrorHandling(error);
      }
    });
  }

  private loadTopTen() {
    this.eventService.getTopTen(this.selectedCategory).subscribe({
      next: (events: EventTopTenDto[]) => {
        this.topTenEvents = events;
        if (events) {
          console.log(events)
        }
      },
      error: error => {
        this.defaultServiceErrorHandling(error);
      }
    });
  }

  onCategoryChange() {
    this.loadTopTen();
  }
}
