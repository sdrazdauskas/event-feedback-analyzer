package com.github.sdrazdauskas.eventfeedbackanalyzer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HuggingFaceService implements FeedbackService {
    @Value("${huggingface.api.key}")
    private String key;
    @Value("${huggingface.api.sentiment.url}")
    private String url;

    @Override
    public Feedback createFeedback(String text) {
        Sentiment sentiment = analyzeSentiment(text);
        Feedback feedback = new Feedback(text, sentiment);
        return feedback;
    }

    @Override
    public Sentiment analyzeSentiment(String text) {
        ResponseEntity<Object> response = postToHuggingFace(text);
        Object result = response != null ? response.getBody() : null;  
        if (result instanceof List<?> list && !list.isEmpty()) {
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
        System.out.println("[DEBUG] Received non valid response from SentimentModelClient: " + result);
        return Sentiment.Neutral; // Default to Neutral if there was no valid response
    }

    public ResponseEntity<Object> postToHuggingFace(String text) {
        return postToHuggingFace(text, getUrl(), getKey());
    }

    public ResponseEntity<Object> postToHuggingFace(String text, String apiUrl, String apiKey) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("Authorization", "Bearer " + apiKey);
            String body = "{\"inputs\": " + toJsonString(text) + "}";
            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Object> response = restTemplate.exchange(
                apiUrl, HttpMethod.POST, entity, Object.class
            );
            return response;
        }
        catch (Exception e) {
            System.out.println("[DEBUG] Exception in postToHuggingFace: " + e.getMessage());
        }
        return null;
    }

    private String toJsonString(String text) {
        return "\"" + text.replace("\"", "\\\"") + "\"";
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
