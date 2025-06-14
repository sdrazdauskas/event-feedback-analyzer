package com.github.sdrazdauskas.eventfeedbackanalyzer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class EventControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HuggingFaceService huggingFaceService;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public HuggingFaceService huggingFaceService() {
            return Mockito.mock(HuggingFaceService.class);
        }
    }

    @Test
    void createAndGetEvent() throws Exception {
        // Mock sentiment API
        when(huggingFaceService.sendHttpRequest(anyString()))
            .thenReturn(ResponseEntity.ok(List.of(List.of(Map.of("label", "5 stars", "score", 0.99)))));

        // Create event
        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Test Event\",\"description\":\"Test Desc\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Event"));

        // List events
        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Event"));
    }

    @Test
    void addFeedbackAndGetSummary() throws Exception {
        // Mock sentiment API
        when(huggingFaceService.sendHttpRequest(anyString()))
            .thenReturn(ResponseEntity.ok(List.of(List.of(Map.of("label", "5 stars", "score", 0.99)))));

        // Create event
        String eventJson = mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Sentiment Event\",\"description\":\"Desc\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String eventId = eventJson.split("\"id\":\"")[1].split("\"")[0];

        // Add feedback
        mockMvc.perform(post("/events/" + eventId + "/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\"This is great!\"}"))
                .andExpect(status().isOk());

        // Get a summary
        mockMvc.perform(get("/events/" + eventId + "/summary"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Positive")));
    }
}
