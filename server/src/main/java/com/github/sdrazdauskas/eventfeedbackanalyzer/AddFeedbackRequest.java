package com.github.sdrazdauskas.eventfeedbackanalyzer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AddFeedbackRequest {
    @NotBlank
    @Size(max = EventController.MAX_FEEDBACK_LENGTH)
    private String text;

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
}
