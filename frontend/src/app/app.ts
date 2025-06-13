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
  submittingEvent = false;
  submittingFeedback = false;
  showSummary = false;
  showBars = false;

  constructor(private api: ApiService) {
    this.loadEvents();
  }

  loadEvents() {
    this.api.getEvents().subscribe(events => this.events = events);
  }

  createEvent() {
    if (!this.newEvent.title || !this.newEvent.description) return;
    this.submittingEvent = true;
    this.api.createEvent(this.newEvent.title, this.newEvent.description).subscribe({
      next: (response) => {
        // If status is 200, event created
        if (response.status === 200) {
          this.events.push(response.body!);
          this.newEvent = { title: '', description: '' };
          this.message = 'Event created!';
        }
        this.submittingEvent = false;
      },
      error: (err) => {
        if (err.status === 429) {
          this.message = 'Event limit reached. Please delete old events before creating new ones.';
        } else {
          this.message = 'Error creating event.';
        }
        this.submittingEvent = false;
      }
    });
  }

  selectEvent(event: Event) {
    this.selectedEvent = event;
    this.summary = null;
    this.feedbackText = '';
  }

  submitFeedback() {
    if (!this.selectedEvent || !this.feedbackText) return;
    this.submittingFeedback = true;
    this.api.submitFeedback(this.selectedEvent.id, this.feedbackText).subscribe(() => {
      this.message = 'Feedback submitted!';
      this.feedbackText = '';
      this.submittingFeedback = false;
      this.getSummary();
    }, () => {
      this.message = 'Error submitting feedback.';
      this.submittingFeedback = false;
    });
  }

  getSummary() {
    if (!this.selectedEvent) return;
    this.api.getSummary(this.selectedEvent.id).subscribe(summary => this.summary = summary);
  }

  getPositivePercentage(): number {
    if (!this.summary) return 0;
    const total = (this.summary['Positive'] || 0) + (this.summary['Neutral'] || 0) + (this.summary['Negative'] || 0);
    if (total === 0) return 0;
    return Math.round((this.summary['Positive'] || 0) / total * 100);
  }

  getNeutralPercentage(): number {
    if (!this.summary) return 0;
    const total = (this.summary['Positive'] || 0) + (this.summary['Neutral'] || 0) + (this.summary['Negative'] || 0);
    if (total === 0) return 0;
    return Math.round((this.summary['Neutral'] || 0) / total * 100);
  }

  getNegativePercentage(): number {
    if (!this.summary) return 0;
    const total = (this.summary['Positive'] || 0) + (this.summary['Neutral'] || 0) + (this.summary['Negative'] || 0);
    if (total === 0) return 0;
    return Math.round((this.summary['Negative'] || 0) / total * 100);
  }

  getAverageSentimentScore(): number {
    if (!this.summary) return 0;
    const positive = this.summary['Positive'] || 0;
    const neutral = this.summary['Neutral'] || 0;
    const negative = this.summary['Negative'] || 0;
    const total = positive + neutral + negative;
    if (total === 0) return 0;
    return ((positive * 1) + (neutral * 0.5) + (negative * 0)) / total;
  }

  getSentimentPercentage(): number {
    return Math.round(this.getAverageSentimentScore() * 100);
  }
}
