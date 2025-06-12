package com.github.sdrazdauskas.eventfeedbackanalyzer;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class FeedbackService {
    private final HuggingFaceService huggingFaceService;

    @Autowired
    public FeedbackService(HuggingFaceService huggingFaceService) {
        this.huggingFaceService = huggingFaceService;
    }

    public Feedback createFeedback(String text) {
        Sentiment sentiment = analyzeSentiment(text);
        Feedback feedback = new Feedback(text, sentiment);
        return feedback;
    }

    private Sentiment analyzeSentiment(String text) {
        ResponseEntity<Object> response = huggingFaceService.sendHttpRequest(text);
        if (response.getBody() instanceof List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            if (first instanceof List<?> resultList && !resultList.isEmpty()) {
                // Fix max scored label
                int maxStars = 0;
                double maxScore = -1;
                for (Object item : resultList) {
                    if (item instanceof Map<?, ?> itemMap) {
                        int currentStars = Integer.parseInt(((String) itemMap.get("label")).split(" ")[0]);
                        double score = ((Number) itemMap.get("score")).doubleValue();
                        if (score > maxScore) {
                            maxScore = score;
                            maxStars = currentStars;
                        }
                    }
                }
                if (maxStars != 0) {
                    System.out.println("[DEBUG] Chosen label: " + maxStars);
                    if (maxStars < 3) {
                        return Sentiment.Negative;
                    } else if (maxStars == 3) {
                        return Sentiment.Neutral;
                    } else {
                        return Sentiment.Positive;
                    }
                }
            }
        }
        return Sentiment.Neutral; // Default to Neutral if no valid response
    }
}