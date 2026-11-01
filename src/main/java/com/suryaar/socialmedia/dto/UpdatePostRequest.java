package com.suryaar.socialmedia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request for updating the text content of an existing post")
public class UpdatePostRequest {

    @Size(max = 250, message = "Post text cannot exceed 250 characters")
    @Schema(
            description = "Updated text content of the post. Maximum 250 characters.",
            example = "Learning Spring Boot and building REST APIs!",
            maxLength = 250,
            nullable = true
    )
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}