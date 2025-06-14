import { Component } from '@angular/core';
import { ApiService } from './api.service';
import { Event, SentimentSummary } from './models';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

const DEFAULT_MESSAGE_TIMEOUT = 3000;

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
  messageType: 'success' | 'error' = 'success';
  private messageTimeout: any;
  submittingEvent = false;
  submittingFeedback = false;
  showSummary = false;
  showBars = false;

  constructor(private api: ApiService) {
    this.loadEvents();
  }

  loadEvents() {
    this.api.getEvents().subscribe({
      next: events => this.events = events,
      error: err => {
        if (err.status === 0) {
          this.showMessage('Cannot connect to the server. Please check your network or try again later.', 0, 'error');
        } else {
          this.showMessage('Could not load events. Please try again later.', DEFAULT_MESSAGE_TIMEOUT, 'error');
        }
        this.events = [];
      }
    });
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
          this.showMessage('Event created!', DEFAULT_MESSAGE_TIMEOUT, 'success');
        }
        this.submittingEvent = false;
      },
      error: (err) => {
        switch (err.status) {
          case 400:
            this.showMessage(err.error || 'Invalid event data. Please check your input.', DEFAULT_MESSAGE_TIMEOUT, 'error');
            break;
          case 429:
            this.showMessage('Event limit reached. Please try again later.', DEFAULT_MESSAGE_TIMEOUT, 'error');
            break;
          default:
            this.showMessage('Error creating event.', DEFAULT_MESSAGE_TIMEOUT, 'error');
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
    this.api.submitFeedback(this.selectedEvent.id, this.feedbackText).subscribe({
      next: (response) => {
        if (response.status === 200) {
          this.showMessage('Feedback submitted successfully!', DEFAULT_MESSAGE_TIMEOUT, 'success');
          this.feedbackText = '';
          
          this.getSummary();
        }
        this.submittingFeedback = false;
      },
      error: (err) => {
        switch (err.status) {
          case 400:
            this.showMessage(err.error || 'Invalid feedback data. Please check your input.', DEFAULT_MESSAGE_TIMEOUT, 'error');
            break;
          case 429:
            this.showMessage('Feedback limit reached for this event. Please try again later.', DEFAULT_MESSAGE_TIMEOUT, 'error');
            break;
          default:
            this.showMessage('Error submitting feedback.', DEFAULT_MESSAGE_TIMEOUT, 'error');
        }
        this.submittingFeedback = false;
      }
    });
  }

  getSummary() {
    if (!this.selectedEvent) return;
    this.api.getSummary(this.selectedEvent.id).subscribe({
      next: summary => this.summary = summary,
      error: err => {
        if (err.status === 0) {
          this.showMessage('Cannot connect to the server. Please check your network or try again later.', 0, 'error');
        } else {
          this.showMessage('Could not load summary. Please try again later.', DEFAULT_MESSAGE_TIMEOUT, 'error');
        }
        this.summary = null;
      }
    });
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

  showMessage(msg: string, duration: number = DEFAULT_MESSAGE_TIMEOUT, type: 'success' | 'error' = 'success') {
    this.message = msg;
    this.messageType = type;
    if (this.messageTimeout) {
      clearTimeout(this.messageTimeout);
    }
    if (duration && duration > 0) {
      this.messageTimeout = setTimeout(() => {
        this.message = '';
      }, duration);
    }
  }
}
