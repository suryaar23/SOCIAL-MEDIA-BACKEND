
package com.suryaar.socialmedia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request containing the text of a comment to be created")
public class CreateCommentRequest {

    @NotBlank(message = "Comment text is required")
    @Size(max = 250, message = "Comment cannot exceed 250 characters")
    @Schema(
            description = "Text content of the comment. Cannot be blank and must not exceed 250 characters.",
            example = "Great post!",
            minLength = 1,
            maxLength = 250,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String text;

    public CreateCommentRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
