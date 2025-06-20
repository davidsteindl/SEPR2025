import {Component, OnInit, ViewChild} from '@angular/core';
import {NgForm} from "@angular/forms";
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {MessageCreate} from '../../../dtos/message';
import {MessageService} from '../../../services/message.service';
import {ToastrService} from 'ngx-toastr';
import {ErrorFormatterService} from '../../../services/error-formatter.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-create-message',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './create-message.component.html',
  styleUrl: './create-message.component.scss'
})
export class CreateMessageComponent implements OnInit {
  @ViewChild('messageForm') form!: NgForm;

  currentMessage: MessageCreate = {
    id: 0,
    title: '',
    summary: '',
    text: '',
    publishedAt: '',
    images: []
  };

  newsImages: File[] = [];
  submitted: boolean = false;
  showConfirm: boolean = false;

  private initialMessage: MessageCreate;

  constructor(
    private messageService: MessageService,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService,
    private router: Router
  ) {
    this.initialMessage = {
      id: 0,
      title: '',
      summary: '',
      text: '',
      publishedAt: '',
      images: []
    };
  }

  ngOnInit(): void {
    this.initialMessage = JSON.parse(JSON.stringify(this.currentMessage));
  }

  addMessage(): void {
    this.submitted = true;

    if (this.form.valid) {
      const payload: MessageCreate = {
        id: 0,
        title: this.currentMessage.title,
        summary: this.currentMessage.summary,
        text: this.currentMessage.text,
        publishedAt: new Date().toISOString(),
        images: this.newsImages
      };
      this.messageService.createMessage(payload).subscribe({
        next: () => {
          this.notification.success('Message created successfully!', 'Success');
          this.router.navigate(['/admin']);
        },
        error: (err) => {
          this.notification.error(this.errorFormatter.format(err), 'Error while creating message', {
            enableHtml: true,
            timeOut: 8000,
          });
        }
      });
    }
  }

  onFileSelected(event: Event): void {
    const fileInput = event.target as HTMLInputElement;
    if (fileInput.files) {
      this.newsImages = Array.from(fileInput.files);
    }
  }

  onBackClick(): void {
    if (this.isUnchanged()) {
      this.router.navigate(['/admin']);
    } else {
      this.showConfirm = true;
    }
  }

  stay(): void {
    this.showConfirm = false;
  }

  exit(): void {
    this.showConfirm = false;
    this.router.navigate(['/admin']);
  }

  private isUnchanged(): boolean {
    const unchangedMessage = JSON.stringify({
      ...this.currentMessage,
      images: []
    }) === JSON.stringify(this.initialMessage);
    const unchangedImages = this.newsImages.length === 0;
    return unchangedMessage && unchangedImages;
  }
}
