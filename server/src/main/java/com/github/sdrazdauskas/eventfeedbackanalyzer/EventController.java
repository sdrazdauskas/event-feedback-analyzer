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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@CrossOrigin
@OpenAPIDefinition(
    info = @Info(title = "Event Feedback Analyzer API", version = "1.0", description = "API for event feedback and sentiment analysis.")
)
@RestController
@RequestMapping("/events")
public class EventController {
    public static final int MAX_EVENTS = 1000; // Limit for total events in memory
    public static final int MAX_FEEDBACK = 100; // Limit for feedback amount for one event
    public static final int MAX_TITLE_LENGTH = 100; // Limit for event's title length
    public static final int MAX_DESCRIPTION_LENGTH = 500; // Limit for event's description length
    public static final int MAX_FEEDBACK_LENGTH = 500; // Limit for event's feedback length

    public static final String TITLE_LENGTH_ERROR = "Title is required and must be at most " + MAX_TITLE_LENGTH + " characters.";
    public static final String DESCRIPTION_LENGTH_ERROR = "Description is required and must be at most " + MAX_DESCRIPTION_LENGTH + " characters.";
    public static final String EVENT_LIMIT_ERROR = "Event limit reached. Please delete old events before creating new ones.";
    public static final String DUPLICATE_TITLE_ERROR = "An event with this title already exists.";
    public static final String FEEDBACK_LENGTH_ERROR = "Feedback is required and must be at most " + MAX_FEEDBACK_LENGTH + " characters.";
    public static final String FEEDBACK_LIMIT_ERROR = "Feedback limit reached for this event. Please delete old feedback before adding new ones.";
    public static final String EVENT_NOT_FOUND_ERROR = "Event not found.";

    private final EventRepository eventRepository;
    private final FeedbackService feedbackService;

    public EventController(EventRepository eventRepository, FeedbackService feedbackService) {
        this.eventRepository = eventRepository;
        this.feedbackService = feedbackService;
    }

    @Operation(
        summary = "Create a new event",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Event created successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Event.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid input",
                content = @Content(
                    mediaType = "text/plain",
                    schema = @Schema(type = "string", example = TITLE_LENGTH_ERROR)
                )
            ),
            @ApiResponse(
                responseCode = "429",
                description = "Event limit reached",
                content = @Content(
                    mediaType = "text/plain",
                    schema = @Schema(type = "string", example = EVENT_LIMIT_ERROR)
                )
            )
        }
    )
    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody CreateEventRequest body) {
        long eventCount = eventRepository.count();
        if (eventCount >= MAX_EVENTS) {
            return ResponseEntity.status(429).body(EVENT_LIMIT_ERROR);
        }
        String title = body.getTitle();
        String description = body.getDescription();

        // Length validation
        if (title == null || title.length() > MAX_TITLE_LENGTH) {
            return ResponseEntity.badRequest().body(TITLE_LENGTH_ERROR);
        }
        if (description == null || description.length() > MAX_DESCRIPTION_LENGTH) {
            return ResponseEntity.badRequest().body(DESCRIPTION_LENGTH_ERROR);
        }

        // Check for duplicate title
        if (eventRepository.findByTitle(title).isPresent()) {
            return ResponseEntity.badRequest().body(DUPLICATE_TITLE_ERROR);
        }

        Event event = new Event(title, description);
        return ResponseEntity.ok(eventRepository.save(event));
    }

    @Operation(
        summary = "Get all events",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "List of all events",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Event.class)
                )
            )
        }
    )
    @GetMapping
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Operation(
        summary = "Add feedback to an event",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Feedback added successfully"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid input",
                content = @Content(
                    mediaType = "text/plain",
                    schema = @Schema(type = "string", example = FEEDBACK_LENGTH_ERROR)
                )
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Event not found",
                content = @Content(
                    mediaType = "text/plain",
                    schema = @Schema(type = "string", example = EVENT_NOT_FOUND_ERROR)
                )
            ),
            @ApiResponse(
                responseCode = "429",
                description = "Feedback limit reached",
                content = @Content(
                    mediaType = "text/plain",
                    schema = @Schema(type = "string", example = FEEDBACK_LIMIT_ERROR)
                )
            )
        }
    )
    @PostMapping("/{eventId}/feedback")
    public ResponseEntity<?> addFeedback(@PathVariable String eventId, @RequestBody AddFeedbackRequest body) {
        String text = body.getText();
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        // Max events validation
        Event event = eventOpt.get();
        if (event.getFeedbackList().size() >= MAX_FEEDBACK) {
            return ResponseEntity.status(429).body(FEEDBACK_LIMIT_ERROR);
        }
        // Length validation
        if (text == null || text.length() > MAX_FEEDBACK_LENGTH) {
            return ResponseEntity.badRequest().body(FEEDBACK_LENGTH_ERROR);
        }
        event.addFeedback(feedbackService.createFeedback(text));
        eventRepository.save(event);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Get event summary",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Summary returned successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Map.class)
                )
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Event not found",
                content = @Content(
                    mediaType = "text/plain",
                    schema = @Schema(type = "string", example = EVENT_NOT_FOUND_ERROR)
                )
            )
        }
    )
    @GetMapping("/{eventId}/summary")
    public ResponseEntity<?> getSummary(@PathVariable String eventId) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return ResponseEntity.status(404).body(EVENT_NOT_FOUND_ERROR);
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