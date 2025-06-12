import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Event, Feedback, SentimentSummary } from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private apiUrl = 'http://localhost:8080/events';

  constructor(private http: HttpClient) {}

  getEvents(): Observable<Event[]> {
    return this.http.get<Event[]>(this.apiUrl);
  }

  createEvent(title: string, description: string): Observable<Event> {
    return this.http.post<Event>(this.apiUrl, { title, description });
  }

  submitFeedback(eventId: string, text: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${eventId}/feedback`, { text });
  }

  getSummary(eventId: string): Observable<SentimentSummary> {
    return this.http.get<SentimentSummary>(`${this.apiUrl}/${eventId}/summary`);
  }
}
