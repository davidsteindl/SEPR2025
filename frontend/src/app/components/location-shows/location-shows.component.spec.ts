import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LocationShowsComponent } from './location-shows.component';

describe('LocationShowsComponent', () => {
  let component: LocationShowsComponent;
  let fixture: ComponentFixture<LocationShowsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LocationShowsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LocationShowsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
