export interface Event {
  id: string;
  title: string;
  description: string;
}

export interface Feedback {
  text: string;
  timestamp: string;
}

export interface SentimentSummary {
  [sentiment: string]: number;
}
