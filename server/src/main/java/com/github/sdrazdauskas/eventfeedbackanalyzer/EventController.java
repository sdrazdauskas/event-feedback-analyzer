package com.github.sdrazdauskas.eventfeedbackanalyzer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;


@OpenAPIDefinition(
    info = @Info(title = "Event Feedback Analyzer API", version = "1.0", description = "API for event feedback and sentiment analysis.")
)
@RestController
@RequestMapping("/events")
public class EventController {
    private final EventRepository eventRepository;
    private final HuggingFaceService huggingFaceService;
    private final FeedbackService feedbackService;

    public EventController(EventRepository eventRepository, HuggingFaceService huggingFaceService) {
        this.eventRepository = eventRepository;
        this.huggingFaceService = huggingFaceService;
        this.feedbackService = new FeedbackService(huggingFaceService);
    }

    @PostMapping
    public Event createEvent(@RequestBody Map<String, String> body) {
        String title = body.get("title");
        String description = body.get("description");
        Event event = new Event(title, description);
        return eventRepository.save(event);
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