
package com.suryaar.socialmedia.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PostMediaId implements Serializable {

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "media_id")
    private Long mediaId;

    protected PostMediaId() {
    }

    public PostMediaId(Long postId, Long mediaId) {
        this.postId = postId;
        this.mediaId = mediaId;
    }

    public Long getPostId() {
        return postId;
    }

    public Long getMediaId() {
        return mediaId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof PostMediaId other)) {
            return false;
        }

        return Objects.equals(postId, other.postId)
                && Objects.equals(mediaId, other.mediaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId, mediaId);
    }
}
