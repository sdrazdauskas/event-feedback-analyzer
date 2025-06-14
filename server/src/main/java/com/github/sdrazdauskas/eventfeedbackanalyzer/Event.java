package com.github.sdrazdauskas.eventfeedbackanalyzer;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Version;

@Entity
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String title;
    @Column(length = 500)
    private String description;
    @OneToMany(cascade = CascadeType.ALL)
    private List<Feedback> feedbackList = new ArrayList<>();
    @Version
    private Long version;

    public Event() {
        // JPA requires a no-args constructor
    }

    public Event(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<Feedback> getFeedbackList() { return feedbackList; }
    public void addFeedback(Feedback feedback) { this.feedbackList.add(feedback); }
}
