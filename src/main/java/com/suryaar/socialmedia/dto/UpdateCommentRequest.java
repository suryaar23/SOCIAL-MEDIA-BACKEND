
package com.suryaar.socialmedia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request containing the updated text of an existing comment")
public class UpdateCommentRequest {

    @NotBlank(message = "Comment text is required")
    @Size(max = 250, message = "Comment cannot exceed 250 characters")
    @Schema(
            description = "Updated text content of the comment. Cannot be blank and must not exceed 250 characters.",
            example = "Thanks for sharing!",
            minLength = 1,
            maxLength = 250,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String text;

    public UpdateCommentRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
