import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateRoomsComponent } from './update-rooms.component';

describe('UpdateRoomsComponent', () => {
  let component: UpdateRoomsComponent;
  let fixture: ComponentFixture<UpdateRoomsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UpdateRoomsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UpdateRoomsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
