package com.suryaar.socialmedia.dto;

import jakarta.validation.constraints.Size;

public class UpdatePostRequest {

    @Size(max = 250, message = "Post text cannot exceed 250 characters")
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}