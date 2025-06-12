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

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class EventControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void createAndGetEvent() throws Exception {
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
