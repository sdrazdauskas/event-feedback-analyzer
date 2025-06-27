package com.github.sdrazdauskas.eventfeedbackanalyzer;

public interface FeedbackService {
    Feedback createFeedback(String text);
    Sentiment analyzeSentiment(String text);
}