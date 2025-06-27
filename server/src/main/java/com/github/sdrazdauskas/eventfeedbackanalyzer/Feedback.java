package com.github.sdrazdauskas.eventfeedbackanalyzer;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(length = 500)
    private String text;
    private LocalDateTime timestamp;
    @Enumerated(EnumType.STRING)
    private Sentiment sentiment;
    
    public Feedback() {
        // JPA requires a no-args constructor
        this.text = null;
        this.timestamp = null;
        this.sentiment = Sentiment.Neutral; // Fallback to Neutral sentiment
    }

    public Feedback(String text, Sentiment sentiment) {
        this.text = text;
        this.timestamp = LocalDateTime.now();
        this.sentiment = sentiment;
    }

    public String getText() { return text; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public String getSentiment() {
        return this.sentiment.name();
    }
}
