import {Component, OnInit} from '@angular/core';
import {MessageService} from '../../services/message.service';
import {UserService} from "../../services/user.service";
import {Message} from '../../dtos/message';
import {EventTopTenDto} from '../../dtos/event';
import {AuthService} from '../../services/auth.service';
import {EventService} from "../../services/event.service";
import {eventCategory} from "../../dtos/eventCategory";

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

  private message: Message[];

  selectedCategory: string = 'All';
  categories: eventCategory[] = [];
  topTenEvents: EventTopTenDto[] = [];

  allMessagesRead = false;
  showAllMessages = false;

  page = 0;
  size = 10;
  totalPages = 0;

  constructor(private messageService: MessageService,
              private userService: UserService,
              private eventService: EventService,
              private authService: AuthService) {
  }

  ngOnInit() {
    this.loadMessages();
    this.loadCategories();
    this.loadTopTen();
  }

  getMessage(): Message[] {
    return this.message;
  }

  vanishError() {
    this.error = false;
  }

  private loadMessages() {
    const userId = this.authService.getUserId();

    if (this.showAllMessages) {
      this.messageService.getMessagesPaginated(this.page, this.size).subscribe({
        next: (page) => {
          this.userService.getUnseenMessages(userId).subscribe({
            next: (unseen) => {
              const unseenIds = unseen.map(m => m.id);
              this.message = page.content.map(m => ({
                ...m,
                seen: !unseenIds.includes(m.id)
              }));
              this.allMessagesRead = unseenIds.length === 0;
              this.totalPages = page.totalPages;
            },
            error: error => this.defaultServiceErrorHandling(error)
          });
        },
        error: error => this.defaultServiceErrorHandling(error)
      });
    } else {
      this.userService.getUnseenMessagesPaginated(userId, this.page, this.size).subscribe({
        next: (page) => {
          this.message = page.content.map(m => ({
            ...m,
            seen: false
          }));
          this.allMessagesRead = page.totalElements === 0;
          this.totalPages = page.totalPages;
        },
        error: error => this.defaultServiceErrorHandling(error)
      });
    }
  }

  changePage(newPage: number) {
    if (newPage >= 0 && newPage < this.totalPages) {
      this.page = newPage;
      this.loadMessages();
    }
  }


  toggleShowAllMessages() {
    this.showAllMessages = !this.showAllMessages;
    this.loadMessages();
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
          console.log(events);
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
