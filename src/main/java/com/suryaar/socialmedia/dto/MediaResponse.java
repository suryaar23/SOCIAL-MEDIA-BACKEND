
package com.suryaar.socialmedia.dto;

import com.suryaar.socialmedia.entity.MediaType;

public class MediaResponse {

    private Long mediaId;
    private MediaType type;
    private String url;
    private String name;
    private Long size;

    public MediaResponse(
            Long mediaId,
            MediaType type,
            String url,
            String name,
            Long size) {

        this.mediaId = mediaId;
        this.type = type;
        this.url = url;
        this.name = name;
        this.size = size;
    }

    public Long getMediaId() {
        return mediaId;
    }

    public MediaType getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public Long getSize() {
        return size;
    }
}
