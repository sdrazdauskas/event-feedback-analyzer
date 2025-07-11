package com.github.sdrazdauskas.eventfeedbackanalyzer;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.github.sdrazdauskas.eventfeedbackanalyzer.EventController.MAX_DESCRIPTION_LENGTH;
import static com.github.sdrazdauskas.eventfeedbackanalyzer.EventController.MAX_TITLE_LENGTH;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class EventControllerValidationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FeedbackService feedbackService;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public FeedbackService feedbackService() {
            FeedbackService mock = Mockito.mock(FeedbackService.class);
            Mockito.when(mock.createFeedback(anyString()))
                .thenReturn(new Feedback("Test", Sentiment.Positive));
            Mockito.when(mock.analyzeSentiment(anyString()))
                .thenReturn(Sentiment.Positive);
            return mock;
        }
    }

    @Test
    void cannotCreateEventWithDuplicateTitle() throws Exception {
        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"UniqueTitle\",\"description\":\"Desc\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"UniqueTitle\",\"description\":\"Another Desc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("already exists")));
    }

    @Test
    void cannotCreateEventWithLongTitleOrDescription() throws Exception {
        String longTitle = "T".repeat(MAX_TITLE_LENGTH + 1);
        String longDesc = "D".repeat(MAX_DESCRIPTION_LENGTH + 1);
        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"" + longTitle + "\",\"description\":\"Desc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(EventController.TITLE_LENGTH_ERROR));
        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Title\",\"description\":\"" + longDesc + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(EventController.DESCRIPTION_LENGTH_ERROR));
    }

    @Test
    void cannotCreateEventWithNullTitleOrDescription() throws Exception {
        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"description\":\"Desc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(EventController.TITLE_LENGTH_ERROR));
        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Title\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(EventController.DESCRIPTION_LENGTH_ERROR));
    }
}
