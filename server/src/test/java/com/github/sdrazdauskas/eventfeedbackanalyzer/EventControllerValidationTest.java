package com.github.sdrazdauskas.eventfeedbackanalyzer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class EventControllerValidationTest {
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
    void cannotCreateEventWithDuplicateTitle() throws Exception {
        when(huggingFaceService.sendHttpRequest(anyString()))
            .thenReturn(ResponseEntity.ok(List.of(List.of(Map.of("label", "5 stars", "score", 0.99)))));

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
        String longTitle = "T".repeat(101);
        String longDesc = "D".repeat(501);
        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"" + longTitle + "\",\"description\":\"Desc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("at most 100 characters")));
        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Title\",\"description\":\"" + longDesc + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("at most 500 characters")));
    }

    @Test
    void cannotCreateEventWithNullTitleOrDescription() throws Exception {
        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"description\":\"Desc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Title is required")));
        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Title\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Description is required")));
    }
}
