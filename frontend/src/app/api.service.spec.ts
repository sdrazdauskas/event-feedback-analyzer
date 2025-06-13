import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ApiService } from './api.service';
import { environment } from '../environments/environment';

describe('ApiService', () => {
  let service: ApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ApiService]
    });
    service = TestBed.inject(ApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should fetch events', () => {
    const dummyEvents = [{ id: '1', title: 'Test', description: 'Desc' }];
    service.getEvents().subscribe(events => {
      expect(events).toEqual(dummyEvents);
    });
    const req = httpMock.expectOne(environment.apiUrl);
    expect(req.request.method).toBe('GET');
    req.flush(dummyEvents);
  });

  afterEach(() => {
    httpMock.verify();
  });
});
