
package com.suryaar.socialmedia.dto;

import com.suryaar.socialmedia.entity.MediaType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing details of an uploaded media file")
public class MediaResponse {

    @Schema(description = "Unique identifier of the media file", example = "101")
    private Long mediaId;

    @Schema(
            description = "Type of media file",
            example = "IMAGE"
    )
    private MediaType type;

    @Schema(
            description = "Temporary presigned URL for accessing the uploaded media. " +
                    "The URL expires after 30 minutes.",
            example = "https://s3.eu-central-003.backblazeb2.com/..."
    )   
    private String url;

    @Schema(
            description = "Original name of the uploaded file",
            example = "profile-image.jpg"
    )
    private String name;

    @Schema(
            description = "Size of the uploaded file in bytes",
            example = "204800"
    )
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
