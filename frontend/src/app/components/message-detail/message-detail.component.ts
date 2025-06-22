import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {Message} from '../../dtos/message';
import {MessageService} from '../../services/message.service';
import {UserService} from '../../services/user.service';
import {AuthService} from '../../services/auth.service';
import {DomSanitizer, SafeResourceUrl} from '@angular/platform-browser';

@Component({
  selector: 'app-message-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './message-detail.component.html',
  styleUrl: './message-detail.component.scss'
})
export class MessageDetailComponent implements OnInit {

  message: Message | null = null;
  images: Map<number, SafeResourceUrl> = new Map();
  error = false;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private messageService: MessageService,
    private userService: UserService,
    private authService: AuthService,
    private sanitizer: DomSanitizer,
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const messageId = Number(idParam);
      this.loadMessage(messageId);
    } else {
      this.error = true;
      this.errorMessage = 'Invalid message ID.';
    }
  }

  private loadMessage(id: number): void {
    this.messageService.getMessageById(id).subscribe({
      next: (msg) => {
        this.message = msg;
        this.loadImages();
        this.markAsSeen();
      },
      error: (err) => this.handleError(err)
    });
  }

  private loadImages(): void {
    if (!this.message) return;
    this.images.clear();
    for (const {id} of this.message.images ?? []) {
      this.messageService.getImageBlob(this.message.id, id).subscribe({
        next: (blob) => {
          const url = URL.createObjectURL(blob);
          const safeUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);
          this.images.set(id, safeUrl);
        },
        error: (err) => this.handleError(err)
      });
    }
  }

  private markAsSeen(): void {
    if (!this.message) return;
    const userId = this.authService.getUserId();
    this.userService.markMessageAsSeen(userId, this.message.id).subscribe({
      error: (err) => this.handleError(err)
    });
  }

  getImageUrl(id: number): SafeResourceUrl | undefined {
    return this.images.get(id);
  }

  vanishError(): void {
    this.error = false;
  }

  private handleError(err: any): void {
    console.error(err);
    this.error = true;
    if (typeof err.error === 'object') {
      this.errorMessage = err.error.error ?? err.error.title;
    } else {
      this.errorMessage = err.error;
    }
  }
}
