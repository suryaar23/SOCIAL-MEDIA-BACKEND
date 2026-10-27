package com.suryaar.socialmedia.dto;

import jakarta.validation.constraints.Size;
import java.util.List;

public class CreatePostRequest {

    @Size(max = 250, message = "Post text cannot exceed 250 characters")
    private String text;

    private List<Long> mediaIds;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Long> getMediaIds() {
        return mediaIds;
    }

    public void setMediaIds(List<Long> mediaIds) {
        this.mediaIds = mediaIds;
    }
}