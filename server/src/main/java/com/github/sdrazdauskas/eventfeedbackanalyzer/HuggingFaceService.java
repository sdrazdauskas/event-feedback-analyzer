package com.github.sdrazdauskas.eventfeedbackanalyzer;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HuggingFaceService {
    @Value("${huggingface.api.key}")
    private String key;
    @Value("${huggingface.api.sentiment.url}")
    private String url;

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

    
    public ResponseEntity<Object> postToHuggingFace(String text) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("Authorization", "Bearer " + getKey());
            String body = "{\"inputs\": " + toJsonString(text) + "}";
            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Object> response = restTemplate.exchange(
                getUrl(), HttpMethod.POST, entity, Object.class
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
}
