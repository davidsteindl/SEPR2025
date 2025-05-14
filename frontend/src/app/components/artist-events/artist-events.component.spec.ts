import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ArtistEventsComponent } from './artist-events.component';

describe('ArtistEventsComponent', () => {
  let component: ArtistEventsComponent;
  let fixture: ComponentFixture<ArtistEventsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ArtistEventsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ArtistEventsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
