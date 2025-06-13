import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Event, Feedback, SentimentSummary } from './models';
import { environment } from '../environments/environment';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getEvents(): Observable<Event[]> {
    return this.http.get<Event[]>(this.apiUrl);
  }

  createEvent(title: string, description: string): Observable<HttpResponse<Event>> {
    return this.http.post<Event>(this.apiUrl, { title, description }, { observe: 'response' });
  }

  submitFeedback(eventId: string, text: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${eventId}/feedback`, { text });
  }

  getSummary(eventId: string): Observable<SentimentSummary> {
    return this.http.get<SentimentSummary>(`${this.apiUrl}/${eventId}/summary`);
  }
}
