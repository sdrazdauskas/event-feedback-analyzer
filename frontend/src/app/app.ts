import { Component } from '@angular/core';
import { ApiService } from './api.service';
import { Event, SentimentSummary } from './models';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class AppComponent {
  events: Event[] = [];
  selectedEvent: Event | null = null;
  summary: SentimentSummary | null = null;
  newEvent = { title: '', description: '' };
  feedbackText = '';
  message = '';

  constructor(private api: ApiService) {
    this.loadEvents();
  }

  loadEvents() {
    this.api.getEvents().subscribe(events => this.events = events);
  }

  createEvent() {
    if (!this.newEvent.title || !this.newEvent.description) return;
    this.api.createEvent(this.newEvent.title, this.newEvent.description).subscribe(e => {
      this.events.push(e);
      this.newEvent = { title: '', description: '' };
      this.message = 'Event created!';
    });
  }

  selectEvent(event: Event) {
    this.selectedEvent = event;
    this.summary = null;
    this.feedbackText = '';
  }

  submitFeedback() {
    if (!this.selectedEvent || !this.feedbackText) return;
    this.api.submitFeedback(this.selectedEvent.id, this.feedbackText).subscribe(() => {
      this.message = 'Feedback submitted!';
      this.feedbackText = '';
    });
  }

  getSummary() {
    if (!this.selectedEvent) return;
    this.api.getSummary(this.selectedEvent.id).subscribe(summary => this.summary = summary);
  }
}
