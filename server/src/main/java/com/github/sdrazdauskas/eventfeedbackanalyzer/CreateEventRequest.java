package com.github.sdrazdauskas.eventfeedbackanalyzer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateEventRequest {
    @NotBlank
    @Size(max = EventController.MAX_TITLE_LENGTH)
    private String title;

    @NotBlank
    @Size(max = EventController.MAX_DESCRIPTION_LENGTH)
    private String description;

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
