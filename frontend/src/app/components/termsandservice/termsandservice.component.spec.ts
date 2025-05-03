import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TermsandserviceComponent } from './termsandservice.component';

describe('TermsandserviceComponent', () => {
  let component: TermsandserviceComponent;
  let fixture: ComponentFixture<TermsandserviceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TermsandserviceComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TermsandserviceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
