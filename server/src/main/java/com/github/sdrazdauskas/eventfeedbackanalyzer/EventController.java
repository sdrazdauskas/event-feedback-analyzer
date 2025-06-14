package com.github.sdrazdauskas.eventfeedbackanalyzer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@CrossOrigin
@OpenAPIDefinition(
    info = @Info(title = "Event Feedback Analyzer API", version = "1.0", description = "API for event feedback and sentiment analysis.")
)
@RestController
@RequestMapping("/events")
public class EventController {
    private static final int MAX_EVENTS = 1000; // Limit for total events in memory
    private static final int MAX_FEEDBACK = 100; // Limit for feedback amount for one event
    private static final int MAX_TITLE_LENGTH = 100; // Limit for event's title length
    private static final int MAX_DESCRIPTION_LENGTH = 500; // Limit for event's description length
    private static final int MAX_FEEDBACK_LENGTH = 500; // Limit for event's feedback length

    private final EventRepository eventRepository;
    private final HuggingFaceService huggingFaceService;
    private final FeedbackService feedbackService;

    public EventController(EventRepository eventRepository, HuggingFaceService huggingFaceService) {
        this.eventRepository = eventRepository;
        this.huggingFaceService = huggingFaceService;
        this.feedbackService = new FeedbackService(huggingFaceService);
    }

    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody Map<String, String> body) {
        long eventCount = eventRepository.count();
        if (eventCount >= MAX_EVENTS) {
            return ResponseEntity.status(429).body("Event limit reached. Please delete old events before creating new ones.");
        }
        String title = body.get("title");
        String description = body.get("description");

        // Length validation
        if (title == null || title.length() > MAX_TITLE_LENGTH) {
            return ResponseEntity.badRequest().body("Title is required and must be at most " + MAX_TITLE_LENGTH + " characters.");
        }
        if (description == null || description.length() > MAX_DESCRIPTION_LENGTH) {
            return ResponseEntity.badRequest().body("Description is required and must be at most " + MAX_DESCRIPTION_LENGTH + " characters.");
        }

        Event event = new Event(title, description);
        return ResponseEntity.ok(eventRepository.save(event));
    }

    @GetMapping
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @PostMapping("/{eventId}/feedback")
    public ResponseEntity<?> addFeedback(@PathVariable String eventId, @RequestBody Map<String, String> body) {
        String text = body.get("text");
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Event event = eventOpt.get();
        if (event.getFeedbackList().size() >= MAX_FEEDBACK) {
            return ResponseEntity.status(429).body("Feedback limit reached for this event. Please delete old feedback before adding new ones.");
        }

        // Length validation
        if (text == null || text.length() > MAX_FEEDBACK_LENGTH) {
            return ResponseEntity.badRequest().body("Feedback is required and must be at most " + MAX_FEEDBACK_LENGTH + " characters.");
        }
        event.addFeedback(feedbackService.createFeedback(text));
        eventRepository.save(event);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{eventId}/summary")
    public ResponseEntity<?> getSummary(@PathVariable String eventId) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Event event = eventOpt.get();
        Map<String, Long> summary = new HashMap<>();
        for (Feedback feedback : event.getFeedbackList()) {
            String sentiment = feedback.getSentiment();
            summary.put(sentiment, summary.getOrDefault(sentiment, 0L) + 1);
        }
        return ResponseEntity.ok(summary);
    }
}