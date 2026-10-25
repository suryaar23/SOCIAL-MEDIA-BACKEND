
package com.suryaar.socialmedia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateCommentRequest {

    @NotBlank(message = "Comment text is required")
    @Size(max = 250, message = "Comment cannot exceed 250 characters")
    private String text;

    public CreateCommentRequest() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
