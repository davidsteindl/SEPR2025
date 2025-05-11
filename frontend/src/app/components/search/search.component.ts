import { Component } from '@angular/core';
import { FormsModule } from "@angular/forms";
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-search',
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.scss']
})
export class SearchComponent {
  activeTab: 'artist' | 'location' | 'event' | 'performance' = 'artist';

  firstname: string = '';
  lastname: string = '';
  stagename: string = '';

  results: any[] = [];


  search(): void {
    console.log('Search clicked');
  }
}
